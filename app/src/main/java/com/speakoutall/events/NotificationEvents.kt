package com.speakoutall.events

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class NotificationEvents(
    private val mContext: Context,
    block: (intent: Intent) -> Unit
) : EventBroadcastManager(block) {

    companion object {
        const val NOTIFICATION_EVENT = "event.notification"

        fun sendEvent(context: Context) {
            LocalBroadcastManager.getInstance(context).sendBroadcast(Intent(NOTIFICATION_EVENT))
        }
    }

    init {
        LocalBroadcastManager.getInstance(mContext)
            .registerReceiver(this, IntentFilter(NOTIFICATION_EVENT))
    }

    fun dispose() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this)
    }

}