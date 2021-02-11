package com.speakout.events

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class ProfileEvents(
    private val mContext: Context,
    block: (intent: Intent) -> Unit
) : EventBroadcastManager(block) {

    companion object {
        const val PROFILE_EVENT = "event.profile"
        const val EVENT_TYPE = "data.type"
        const val USER_ID = "data.userId"

        fun sendEvent(context: Context, userId: String, eventType: Int) {
            val data = Intent(PROFILE_EVENT).also {
                it.putExtra(EVENT_TYPE, eventType)
                it.putExtra(USER_ID, userId)
            }
            LocalBroadcastManager.getInstance(context).sendBroadcast(data)
        }
    }

    init {
        LocalBroadcastManager.getInstance(mContext).registerReceiver(
            this, IntentFilter(PROFILE_EVENT)
        )
    }

    fun dispose() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this)
    }

}

object ProfileEventTypes {
    const val FOLLOW = 1
    const val UN_FOLLOW = 2
    const val DETAILS_UPDATE = 3
    const val CREATE_POST = 4
    const val DIALOG_UN_FOLLOW = 5
    const val DELETE_POST = 6
}