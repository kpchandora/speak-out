package com.speakout.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.speakout.R
import com.speakout.common.EventObserver
import com.speakout.common.Result
import com.speakout.extensions.setUpToolbar
import com.speakout.extensions.setUpWithAppBarConfiguration
import com.speakout.extensions.showShortToast
import com.speakout.notification.NotificationResponse
import kotlinx.android.synthetic.main.fragment_notifications.*
import timber.log.Timber

class NotificationsFragment : Fragment() {

    private lateinit var safeArgs: NotificationsFragmentArgs
    private val notificationsViewModel: NotificationsViewModel by viewModels()
    private lateinit var adapter: NotificationsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        safeArgs = NotificationsFragmentArgs.fromBundle(arguments!!)
        if (safeArgs.isFromDeepLink) {
            navigateToPostView(safeArgs.postId)
        }
        notificationsViewModel.getNotifications()
        adapter = NotificationsAdapter(mNotificationListener)
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
        rv_notification.setHasFixedSize(true)
        rv_notification.layoutManager = LinearLayoutManager(requireContext())
        rv_notification.adapter = adapter
        notificationsViewModel.notifications.observe(viewLifecycleOwner, EventObserver {
            if (it is Result.Success) {
                adapter.updateData(it.data)
            }
            if (it is Result.Error) {
                showShortToast("Failed")
            }
        })

    }

    private fun navigateToPostView(postId: String) {
        val action =
            NotificationsFragmentDirections.actionNotificationToPostView(
                isFromNotification = true,
                postId = postId
            )
        findNavController().navigate(action)
    }

    private val mNotificationListener = object : NotificationsClickListener {
        override fun onPostClick(notification: NotificationResponse) {
            navigateToPostView(notification.postId ?: "")
        }

        override fun onProfileClick(notification: NotificationResponse, imageView: ImageView) {
            val action = NotificationsFragmentDirections.actionNotificationToProfileNavigation(
                userId = notification.userId,
                profileUrl = notification.photoUrl ?: "",
                transitionTag = notification.timestamp.toString(),
                username = notification.username
            )
            val extras = FragmentNavigatorExtras(
                imageView to notification.timestamp.toString()
            )
            findNavController().navigate(action, extras)
        }
    }

}