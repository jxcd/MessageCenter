package com.me.app.messagecenter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony

class SMSReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        for (smsMessage in smsMessages) {
            val messageBody = smsMessage.messageBody
            val sender = smsMessage.displayOriginatingAddress
            // 在这里处理短信内容
            println("$messageBody, sender $sender")
        }
    }

}