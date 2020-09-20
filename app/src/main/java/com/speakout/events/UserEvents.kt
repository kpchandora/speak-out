package com.speakout.events

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class UserEvents(
    private val mContext: Context,
    block: (intent: Intent) -> Unit
) : EventBroadcastManager(block) {

    companion object {
        const val USER_EVENTS = "event.user"
        const val USER_ID = "data.userId"
        const val EVENT_TYPE = "data.type"

        fun sendEvent(context: Context, userId: String, type: Int) {
            val data = Intent(USER_EVENTS).also {
                it.putExtra(USER_ID, userId)
                it.putExtra(EVENT_TYPE, type)
            }
            LocalBroadcastManager.getInstance(context).sendBroadcast(data)
        }
    }

    init {
        LocalBroadcastManager.getInstance(mContext)
            .registerReceiver(this, IntentFilter(USER_EVENTS))
    }

    fun dispose() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this)
    }

}

object UserEventType {
    const val FOLLOW = 1
    const val UN_FOLLOW = 2
}