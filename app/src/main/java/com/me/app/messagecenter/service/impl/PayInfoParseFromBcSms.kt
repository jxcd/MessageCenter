package com.me.app.messagecenter.service.impl

import android.content.ContentResolver
import android.database.Cursor
import com.me.app.messagecenter.dto.PayInfo
import com.me.app.messagecenter.service.MessageParse
import com.me.app.messagecenter.service.SmsReader
import com.me.app.messagecenter.util.db
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

// 交通银行(Bank of Communications)短信提醒
object PayInfoParseFromBcSms : MessageParse<PayInfo>, SmsReader {
    private val pattern =
        """(\d{2}月\d{2}日\d{2}:\d{2}).*?(.+?)(\d+\.\d+)元.*?余额为(\d+\.\d+)元""".toRegex()

    // private val platformKey = listOf("支付宝", "（特约）美团消费", "京东支付", "拼多多支付", "抖音支付")
    private val platformMap =
        mapOf(
            "支付宝" to "支付宝",
            "微信" to "微信",
            "财付通" to "微信",
            "美团" to "美团",
            "京东支付" to "京东",
            "京东金融" to "京东",
            "SamsungPay" to "三星",
            "拼多多支付" to "拼多多",
            "抖音支付" to "抖音"
        )
    private val revenueKey = listOf("转入", "退费", "退款", "退货")

    override fun parse(message: String): PayInfo? {
        return pattern.find(message)?.groupValues?.let {
            val where = it[2].replace("支付宝支付宝", "支付宝").replace("（特约）", "").trim()
            // val platform = platformKey.find { key -> where.contains(key) }.orEmpty()
            val platform = platformMap.keys
                .firstOrNull { key -> where.contains(key) }?.let { key -> platformMap[key] }
                .orEmpty()

            val revenue = revenueKey.find { key -> where.contains(key) } != null
            val place = if (where.startsWith("在")) where.substring(1) else where

            PayInfo(
                platform = platform,
                place = place.trim(),
                // time = LocalDateTime.parse("${LocalDate.now().year}年${it[1]}", formatter),
                time = "${LocalDate.now().year}-${it[1].replace("月", "-").replace("日", "T")}",
                revenue = revenue,
                money = it[3],
                balance = it[4],
                information = message,
                source = "SMS: 95559",
            )
        }
    }

    override fun readFromSms(contentResolver: ContentResolver) {
        // 查询条件，查询来自95559的短信
        val selection = "address = '95559'"

        CoroutineScope(Dispatchers.IO).launch {
            val s = System.currentTimeMillis()
            val history = db.payInfoDao().selectAll()
            val set = HashSet<String>()
            history.forEach { set.add(it.information) }

            // 查询短信数据库
            val cursor: Cursor? =
                contentResolver.query(SmsReader.smsUri, null, selection, null, "date desc")

            // 遍历查询结果
            var i = 0
            var save = 0
            cursor?.use { c ->
                while (c.moveToNext()) {
                    i++
                    val body = c.getString(12)
                    if (!set.contains(body)) {
                        val payInfo = parse(body)?.apply { this.timestamp = c.getLong(4) }
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