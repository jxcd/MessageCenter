package com.me.app.messagecenter

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.me.app.messagecenter.dto.PayInfo
import com.me.app.messagecenter.ui.theme.MessageCenterTheme
import com.me.app.messagecenter.util.AppDatabase
import com.me.app.messagecenter.util.db
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

const val PERMISSION_REQUEST_SMS = 1

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // RECEIVE_SMS 权限已授予，可以执行相应操作
                // ...
            } else {
                // RECEIVE_SMS 权限被拒绝
                Toast.makeText(this, "没有 RECEIVE_SMS 权限", Toast.LENGTH_SHORT).show()
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
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECEIVE_SMS),
                PERMISSION_REQUEST_SMS
            )
            // 如果没有 RECEIVE_SMS 权限，则请求权限
            requestPermissionLauncher.launch(Manifest.permission.RECEIVE_SMS)
        }

        setContent {
            MessageCenterTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val scope = rememberCoroutineScope()
                    val payInfoList: MutableList<PayInfo> = remember { mutableStateListOf() }

                    val load: () -> Unit = {
                        scope.launch {
                            db.payInfoDao().selectAll().also {
                                payInfoList.clear()
                                payInfoList.addAll(it)
                            }
                        }
                    }

                    val clean: () -> Unit = {
                        scope.launch {
                            db.payInfoDao().deleteAll()
                            load()
                        }
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(0.8f),
                            horizontalArrangement = Arrangement.SpaceAround,
                        ) {
                            Button(onClick = load) {
                                Text(text = "刷新")
                            }

                            Button(onClick = clean) {
                                Text(text = "清空")
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        ShowPayInfo(data = payInfoList)
                    }

                }
            }
        }
    }
}

@Composable
fun ShowPayInfo(data: List<PayInfo>) {
    Column {
        SelectionContainer {
            for (info in data) {
                PayInfoRow(info = info)
            }
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
        modifier = Modifier.padding(8.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(0.8f)) {
                Text(text = payInfoTime(info.time))
                Text(text = info.place, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Text(
                text = info.money,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MessageCenterTheme {
        PayInfoRow(
            PayInfo(
                time = "2023/07/02T23:52",
                money = "8.88",
                place = "消费地点"
            )
        )
    }
}