package com.example.childmonitor.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager

class CallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

            when (state) {
                TelephonyManager.EXTRA_STATE_RINGING -> {
                    // المكالمة الواردة
                }
                TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                    // المكالمة مفتوحة
                }
                TelephonyManager.EXTRA_STATE_IDLE -> {
                    // المكالمة انتهت
                }
            }
        }
    }
}
