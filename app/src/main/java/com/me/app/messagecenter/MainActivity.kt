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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.me.app.messagecenter.compose.page.PageInfoPage
import com.me.app.messagecenter.service.impl.PayInfoParseFromBcSms
import com.me.app.messagecenter.ui.theme.MessageCenterTheme
import com.me.app.messagecenter.util.AppDatabase
import com.me.app.messagecenter.util.db
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = Room.databaseBuilder(this, AppDatabase::class.java, "MessageCenter")
            .fallbackToDestructiveMigration()
            .build()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestReceiveSmsPermission.launch(Manifest.permission.RECEIVE_SMS)
        }

        setContent {
            MessageCenterTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val loadHistory: () -> Unit = {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                            != PackageManager.PERMISSION_GRANTED
                        ) {
                            requestReadSmsForLoadPayInfo.launch(Manifest.permission.READ_SMS)
                        } else {
                            readSmsHistory()
                        }
                    }

                    PageInfoPage(loadHistory = loadHistory)
                }
            }
        }
    }

    private val requestReceiveSmsPermission =
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
                        val payInfo =
                            PayInfoParseFromBcSms.parse(body)?.apply { this.timestamp = c.getLong(4) }
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
