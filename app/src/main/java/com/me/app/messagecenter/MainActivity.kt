package com.me.app.messagecenter

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.me.app.messagecenter.compose.InputTextWithSubmit
import com.me.app.messagecenter.dto.PayInfo
import com.me.app.messagecenter.dto.payInfoFromBmcSms
import com.me.app.messagecenter.ui.theme.MessageCenterTheme
import com.me.app.messagecenter.util.AppDatabase
import com.me.app.messagecenter.util.db
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (!it) {
                // RECEIVE_SMS 权限被拒绝
                Toast.makeText(this, "没有 RECEIVE_SMS 权限", Toast.LENGTH_SHORT).show()
            }
        }
    private val requestReadSmsForLoadPayInfo =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                readSmsHistory()
            } else {
                Toast.makeText(this, "没有 READ_SMS 权限, 无法读取历史消费记录", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = Room.databaseBuilder(this, AppDatabase::class.java, "MessageCenter")
            .fallbackToDestructiveMigration()
            .build()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // 如果没有 RECEIVE_SMS 权限，则请求权限
            requestPermissionLauncher.launch(Manifest.permission.RECEIVE_SMS)
        }

        setContent {
            MessageCenterTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val scope = rememberCoroutineScope()
                    var filter by remember { mutableStateOf("") }
                    val payInfoList =
                        db.payInfoDao().flow().collectAsState(initial = emptyList()).value

                    val clean: () -> Unit = {
                        scope.launch {
                            db.payInfoDao().deleteAll()
                        }
                    }
                    val loadHistory: () -> Unit = {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                            != PackageManager.PERMISSION_GRANTED
                        ) {
                            requestReadSmsForLoadPayInfo.launch(Manifest.permission.READ_SMS)
                        } else {
                            readSmsHistory()
                        }
                    }

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Button(onClick = loadHistory) {
                                Text(text = "加载")
                            }

                            Button(onClick = clean) {
                                Text(text = "清空")
                            }
                        }

                        InputTextWithSubmit(
                            label = "过滤",
                            onSubmit = { filter = it }
                        )

                        ShowPayInfo(data = payInfoList, filter = filter)
                    }
                }
            }
        }
    }

    private val readSmsHistory: () -> Unit = {
        // val contentResolver: ContentResolver = contentResolver
        // 查询收件箱，可以替换为其他类型的 Uri
        val uri: Uri = Uri.parse("content://sms/inbox")
        // 查询条件，查询来自95559的短信
        val selection = "address = '95559'"

        CoroutineScope(Dispatchers.IO).launch {
            val s = System.currentTimeMillis()
            val history = db.payInfoDao().selectAll()
            val set = HashSet<String>()
            history.forEach { set.add(it.information) }

            // 查询短信数据库
            val cursor: Cursor? = contentResolver.query(uri, null, selection, null, "date desc")

            // 遍历查询结果
            var i = 0
            var save = 0
            cursor?.use { c ->
                while (c.moveToNext()) {
                    i++
                    val body = c.getString(12)
                    if (!set.contains(body)) {
                        val payInfo = payInfoFromBmcSms(body, c.getLong(4))
                        if (payInfo == null) {
                            println("skip by null, body: $body")
                            continue
                        }

                        payInfo.apply {
                            println("find history $this")
                            db.payInfoDao().insert(this)
                            set.add(body)
                            save++
                        }
                    } else {
                        // println("skip by history $body...")
                    }
                }
            }
            val e = System.currentTimeMillis()
            println("耗时 ${e - s}ms, 处理数据库记录 ${history.size}条, 短信库记录 ${i}条, 持久化 ${save}条")
        }
    }
}

@Composable
fun ShowPayInfo(data: List<PayInfo>, filter: String) {
    SelectionContainer(modifier = Modifier.fillMaxSize()) {
        val not = filter.startsWith("!!!")
        val key = if (not) filter.substring(3) else filter
        val pattern: (String) -> Boolean = { key.isBlank() || it.contains(key).xor(not) }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(data.filter { pattern(it.information) }) { PayInfoRow(info = it) }
        }
    }
}

private val payInfoTime: (String) -> String = {
    val time = LocalDateTime.parse(it)
    val today = LocalDate.now()

    var text = it.replace("T", " ")
    if (time.year == today.year) {
        text = text.substring(5)
        // if (time.toLocalDate() == today) {
        if (time.month == today.month && time.dayOfMonth == today.dayOfMonth) {
            text = text.substring(6)
        }
    }
    text
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PayInfoRow(info: PayInfo) {
    Card(
        modifier = Modifier.padding(8.dp)
            .also { if (info.revenue) it.background(Color.Green) },
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(0.8f)) {
                Text(text = "${payInfoTime(info.time)} ${info.platform}")
                Text(text = info.place, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            val color = if (info.revenue) Color.Green
            else Color.Unspecified
            Text(
                text = info.money,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                color = color
            )
        }
    }
}
