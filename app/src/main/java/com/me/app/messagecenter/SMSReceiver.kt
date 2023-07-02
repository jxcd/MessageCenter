package com.me.app.messagecenter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.me.app.messagecenter.dto.PayInfo
import com.me.app.messagecenter.util.db
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class SMSReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        val messageMap = LinkedHashMap<String, StringBuilder>()

        // 整合同一个sender的message, 避免过长导致分开解析
        smsMessages.forEach { smsMessage ->
            val sender = smsMessage.displayOriginatingAddress
            val message = smsMessage.messageBody
            println("$message, sender $sender")
            messageMap.computeIfAbsent(sender) { StringBuilder() }.append(message)
        }

        // 在这里处理短信内容
        messageMap.forEach { (sender, message) ->
            if (sender == "95559" &&
                message.startsWith("您尾号*0000的卡于")
            ) {
                payInfo(message.toString())?.also {
                    CoroutineScope(Dispatchers.IO).launch {
                        db.payInfoDao().insert(it)
                        println("insert $it")
                    }
                }
            }
        }
    }

    // 交通银行短信提醒
    private val pattern =
        """(\d{2}月\d{2}日\d{2}:\d{2}).*?在(.+?)网上支付(\d+\.\d+)元.*?余额为(\d+\.\d+)元""".toRegex()
    // private val formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日HH:mm")

    private val payInfo: (String) -> PayInfo? = { message: String ->
        pattern.find(message)?.groupValues?.let {
            PayInfo(
                place = it[2],
                // time = LocalDateTime.parse("${LocalDate.now().year}年${it[1]}", formatter),
                time = "${LocalDate.now().year}-${it[1].replace("月", "-").replace("日", "T")}",
                money = it[3],
                balance = it[4],
                information = message,
                source = "SMS: 95559"
            )
        }
    }

}