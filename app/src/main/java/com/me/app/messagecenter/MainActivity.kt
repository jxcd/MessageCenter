package com.me.app.messagecenter

import android.Manifest
import android.content.pm.PackageManager
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

    private val readSmsHistory: () -> Unit = { PayInfoParseFromBcSms.readFromSms(contentResolver) }
}
