package com.speakout.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.speakout.R
import com.speakout.extensions.setUpToolbar
import com.speakout.extensions.setUpWithAppBarConfiguration

class NotificationsFragment : Fragment() {

    private lateinit var safeArgs: NotificationsFragmentArgs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        safeArgs = NotificationsFragmentArgs.fromBundle(arguments!!)
        if (safeArgs.isFromDeepLink) {
            val action =
                NotificationsFragmentDirections.actionNotificationToPostView(
                    isFromNotification = true,
                    postId = safeArgs.postId
                )
            findNavController().navigate(action)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notifications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpWithAppBarConfiguration(view)
    }

}