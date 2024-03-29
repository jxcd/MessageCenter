package com.me.app.messagecenter.compose.payinfo

import android.Manifest
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.me.app.messagecenter.R
import com.me.app.messagecenter.compose.InputTextWithSubmit
import com.me.app.messagecenter.compose.common.SimpleDialog
import com.me.app.messagecenter.dto.PayInfo
import com.me.app.messagecenter.service.impl.PayInfoParseFromBcSms
import com.me.app.messagecenter.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

const val ReadSmsForLoadPayInfo = "ReadSmsForLoadPayInfo"

@Composable
fun PageInfoPage(
    contentResolver: ContentResolver,
) {
    var filter by remember { mutableStateOf("") }
    val payInfoList =
        db.payInfoDao().flow().collectAsState(initial = emptyList()).value
    val context = LocalContext.current

    LaunchedEffect(ReadSmsForLoadPayInfo) {
        requestPermissionCallback[ReadSmsForLoadPayInfo] = {
            if (it) {
                PayInfoParseFromBcSms.readFromSms(contentResolver)
            } else {
                Toast.makeText(context, "没有 READ_SMS 权限, 无法读取历史消费记录", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val clean: () -> Unit = {
        CoroutineScope(Dispatchers.IO).launch {
            db.payInfoDao().deleteAll()
        }
    }
    val loadHistory: () -> Unit = {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher[ReadSmsForLoadPayInfo]?.launch(Manifest.permission.READ_SMS)
        } else {
            PayInfoParseFromBcSms.readFromSms(contentResolver)
        }
    }


    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        InputTextWithSubmit(
            label = "过滤",
            onSubmit = { filter = it }
        )

        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Button(onClick = loadHistory) { Text(text = "加载") }
                    Button(onClick = clean) { Text(text = "清空") }
                }
            }

            item { PayStatistics(data = payInfoList) }

            val not = filter.startsWith("!!!")
            val key = if (not) filter.substring(3) else filter
            val pattern: (String) -> Boolean =
                { key.isBlank() || it.contains(key).xor(not) }

            items(payInfoList.filter { pattern(it.information) }) { PayInfoRow(info = it) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PayStatistics(data: List<PayInfo>) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        var countDay by remember { mutableStateOf(BigDecimal.ZERO) }
        var totalDay by remember { mutableStateOf(BigDecimal.ZERO) }
        var countWeek by remember { mutableStateOf(BigDecimal.ZERO) }
        var totalWeek by remember { mutableStateOf(BigDecimal.ZERO) }
        var countMonth by remember { mutableStateOf(BigDecimal.ZERO) }
        var totalMonth by remember { mutableStateOf(BigDecimal.ZERO) }
        var today by remember { mutableStateOf(LocalDate.now()) }

        LaunchedEffect(LocalDate.now()) { today = LocalDate.now() }

        LaunchedEffect(data.hashCode()) {
            today = LocalDate.now()

            BigDecimal.ZERO.also {
                countDay = it
                totalDay = it
                countWeek = it
                totalWeek = it
                countMonth = it
                totalMonth = it
            }

            data.forEach {
                if (it.ignoreStatistics) return@forEach

                val date = LocalDateTime.parse(it.time).toLocalDate()
                val money = BigDecimal(it.money).let { m -> if (it.revenue) -m else m }
                val count = BigDecimal.ONE.let { v -> if (it.revenue) -v else v }

                if (isSameMonth(date, today)) {
                    countMonth += count
                    totalMonth += money

                    if (isSameWeek(date, today)) {
                        countWeek += count
                        totalWeek += money

                        if (date == today) {
                            countDay += count
                            totalDay += money
                        }
                    }
                }
            }
        }

        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "今日消费 ${countDay}笔, 共 $totalDay")
            Text(
                text = "本周消费 ${countWeek}笔, 共 $totalWeek, 平均每日消费 " +
                        "${div(totalWeek, today.dayOfWeek.value, 2)}"
            )
            Text(
                text =
                "本月消费 ${countMonth}笔, 共 ${totalMonth}, 平均每日消费 " +
                        "${div(totalMonth, today.dayOfMonth, 2)}"
            )
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PayInfoRow(info: PayInfo) {
    var details by remember { mutableStateOf(false) }
    var editRemark by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.padding(8.dp)
            .clickable {
                details = !details
                if (details) editRemark = false
            }
            .pointerInput(Unit) {
                detectTapGestures(onLongPress = { showDialog = true })
            }
            .also { if (info.revenue) it.background(Color.Green) },
    ) {
        val icon = when (info.platform) {
            "支付宝" -> R.drawable.icon_zfb
            "微信" -> R.drawable.icon_wx
            "美团" -> R.drawable.icon_mt
            "京东" -> R.drawable.icon_jd
            "三星" -> R.drawable.icon_samsungpay
            "拼多多" -> R.drawable.icon_pdd
            "抖音" -> R.drawable.icon_dy
            else -> R.drawable.icon_money
        }

        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(0.8f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = info.platform,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(text = payInfoTime(info.time))
                }
                Text(
                    text = info.place,
                    maxLines = if (details) 5 else 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (details) {
                    val remark = info.remark
                    if (remark.isBlank() || editRemark) {
                        InputTextWithSubmit(label = "备注", defaultValue = remark, onSubmit = {
                            CoroutineScope(Dispatchers.IO).launch {
                                db.payInfoDao().updateRemark(info.id, it)
                                editRemark = false
                            }
                        })
                    } else {
                        Text(text = remark, modifier = Modifier.clickable { editRemark = true })
                    }
                }


            }
            val color = if (info.ignoreStatistics) Color.Gray
            else if (info.revenue) Color.Green
            else Color.Unspecified
            Text(
                text = "￥${info.money}",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                color = color,
                modifier = Modifier.clickable {
                    CoroutineScope(Dispatchers.IO).launch {
                        db.payInfoDao().toggleIgnoreStatistics(info.id, !info.ignoreStatistics)
                    }
                }
            )
        }

        if (showDialog) {
            val close = { showDialog = false }
            SimpleDialog(
                title = "确认删除?",
                text = info.information,
                onConfirm = {
                    CoroutineScope(Dispatchers.IO).launch {
                        db.payInfoDao().delete(info)
                        close()
                    }
                },
                onDismiss = close
            )
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
