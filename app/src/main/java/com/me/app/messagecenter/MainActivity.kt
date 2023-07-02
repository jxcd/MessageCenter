package com.me.app.messagecenter

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.me.app.messagecenter.dto.PayInfo
import com.me.app.messagecenter.ui.theme.MessageCenterTheme
import com.me.app.messagecenter.util.AppDatabase
import com.me.app.messagecenter.util.db
import kotlinx.coroutines.launch

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

                    Column {
                        Button(onClick = load) {
                            Text(text = "刷新")
                        }

                        Button(onClick = clean) {
                            Text(text = "清空")
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
        for (info in data) {
            Row {
                Text(text = "${info.time} ${info.money}")
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MessageCenterTheme {
        Greeting("Android")
    }
}