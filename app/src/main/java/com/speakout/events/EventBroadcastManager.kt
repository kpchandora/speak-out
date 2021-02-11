package com.speakout.events

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

abstract class EventBroadcastManager(private val block: (intent: Intent) -> Unit) :
    BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        p1?.let {
            block(it)
        }
    }
}
