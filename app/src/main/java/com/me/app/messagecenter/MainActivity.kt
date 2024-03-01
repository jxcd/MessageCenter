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
import com.me.app.messagecenter.compose.payinfo.PageInfoPage
import com.me.app.messagecenter.compose.payinfo.ReadSmsForLoadPayInfo
import com.me.app.messagecenter.ui.theme.MessageCenterTheme
import com.me.app.messagecenter.util.AppDatabase
import com.me.app.messagecenter.util.db
import com.me.app.messagecenter.util.requestPermissionCallback
import com.me.app.messagecenter.util.requestPermissionLauncher

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = Room.databaseBuilder(this, AppDatabase::class.java, "MessageCenter")
            .fallbackToDestructiveMigration()
            .build()
        permissionOnInit()

        setContent {
            MessageCenterTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PageInfoPage(contentResolver = contentResolver)
                }
            }
        }
    }

    private fun permissionOnInit() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestReceiveSmsPermission.launch(Manifest.permission.RECEIVE_SMS)
        }

        requestPermissionLauncher[ReadSmsForLoadPayInfo] = requestReadSmsForLoadPayInfo
    }

    private val requestReceiveSmsPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (!it) Toast.makeText(this, "没有 RECEIVE_SMS 权限", Toast.LENGTH_SHORT).show()
        }
    private val requestReadSmsForLoadPayInfo =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            requestPermissionCallback[ReadSmsForLoadPayInfo]?.apply { this(it) }
        }
}
