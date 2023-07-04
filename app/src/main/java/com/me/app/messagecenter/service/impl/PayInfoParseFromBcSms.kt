package com.me.app.messagecenter.service.impl

import com.me.app.messagecenter.dto.PayInfo
import com.me.app.messagecenter.service.MessageParse
import java.time.LocalDate

// 交通银行(Bank of Communications)短信提醒
object PayInfoParseFromBcSms : MessageParse<PayInfo> {
    private val pattern =
        """(\d{2}月\d{2}日\d{2}:\d{2}).*?(.+?)(\d+\.\d+)元.*?余额为(\d+\.\d+)元""".toRegex()

    // private val platformKey = listOf("支付宝", "（特约）美团消费", "京东支付", "拼多多支付", "抖音支付")
    private val platformMap =
        mapOf("支付宝" to "支付宝", "美团" to "美团", "京东支付" to "京东", "拼多多支付" to "拼多多", "抖音支付" to "抖音")
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
}