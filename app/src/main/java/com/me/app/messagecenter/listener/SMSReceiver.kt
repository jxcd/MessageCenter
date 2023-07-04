package com.me.app.messagecenter.listener

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.me.app.messagecenter.dto.payInfoFromBmcSms
import com.me.app.messagecenter.util.db
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SMSReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        val messageMap = LinkedHashMap<String, SmsMessageInfo>()

        // 整合同一个sender的message, 避免过长导致分开解析
        smsMessages.forEach { smsMessage ->
            val sender = smsMessage.displayOriginatingAddress
            val message = smsMessage.messageBody
            println("$message, sender $sender")
            messageMap.computeIfAbsent(sender) { SmsMessageInfo(smsMessage.timestampMillis) }
                .append(message)
        }

        // 在这里处理短信内容
        messageMap.forEach { (sender, smsMessage) ->
            if (sender == "95559" &&
                smsMessage.message.startsWith("您尾号*0000的卡于")
            ) {
                payInfoFromBmcSms(smsMessage.toString(), smsMessage.timestamp)?.also {
                    CoroutineScope(Dispatchers.IO).launch {
                        db.payInfoDao().insert(it)
                        println("insert $it")
                    }
                }
            }
        }
    }

}

private data class SmsMessageInfo(
    val timestamp: Long,
    val message: StringBuilder = StringBuilder()
) {
    fun append(text: String) {
        message.append(text)
    }

}