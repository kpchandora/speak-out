package com.speakout.events

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.annotation.IntDef
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.lang.annotation.ElementType

class PostEvents(
    private val mContext: Context,
    block: (intent: Intent) -> Unit
) :
    EventBroadcastManager(block) {

    companion object {
        const val POST_CREATION_EVENT = "event.post"
        const val EVENT_TYPE = "data.type"
        const val POST_ID = "data.postId"

        fun sendEvent(context: Context, event: Int, postId: String? = null) {
            val data = Intent(POST_CREATION_EVENT).also {
                it.putExtra(EVENT_TYPE, event)
                it.putExtra(POST_ID, postId)
            }
            LocalBroadcastManager.getInstance(context).sendBroadcast(data)
        }

    }

    init {
        LocalBroadcastManager.getInstance(mContext)
            .registerReceiver(this, IntentFilter(POST_CREATION_EVENT))
    }

    fun dispose() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this)
    }

}

object PostEventTypes {
    const val LIKE = 0
    const val DELETE = 1
    const val CREATE = 2
    const val REMOVE_LIKE = 3
    const val FOLLOW = 4
    const val UN_FOLLOW = 5
    const val USER_DETAILS_UPDATE = 6
}
