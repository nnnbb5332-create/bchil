package com.example.childmonitor.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            for (message in messages) {
                val senderNumber = message.originatingAddress
                val messageBody = message.messageBody
                val timestamp = message.timestampMillis

                // معالجة الرسالة
            }
        }
    }
}
