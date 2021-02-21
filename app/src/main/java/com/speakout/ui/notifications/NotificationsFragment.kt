package com.speakout.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.speakout.R
import com.speakout.api.RetrofitBuilder
import com.speakout.common.EventObserver
import com.speakout.common.Result
import com.speakout.events.NotificationEvents
import com.speakout.extensions.createFactory
import com.speakout.extensions.setUpWithAppBarConfiguration
import com.speakout.extensions.showShortToast
import com.speakout.notification.NotificationRepository
import com.speakout.notification.NotificationsItem
import kotlinx.android.synthetic.main.fragment_notifications.*

class NotificationsFragment : Fragment() {

    private lateinit var safeArgs: NotificationsFragmentArgs
    private val notificationsViewModel: NotificationsViewModel by viewModels {
        NotificationsViewModel(NotificationRepository(RetrofitBuilder.apiService)).createFactory()
    }
    private lateinit var adapter: NotificationsAdapter
    private var mNotificationEvents: NotificationEvents? = null
    private var nextPageNumber = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        safeArgs = NotificationsFragmentArgs.fromBundle(arguments!!)
        if (safeArgs.isFromDeepLink) {
            navigateToPostView(safeArgs.postId)
        }
        notificationsViewModel.getNotifications(nextPageNumber)
        adapter = NotificationsAdapter(mNotificationListener)
        mNotificationEvents = NotificationEvents(requireContext()) {
            if (nextPageNumber > 1) nextPageNumber--
            notificationsViewModel.getNotifications(nextPageNumber)
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
        rv_notification.setHasFixedSize(true)
        rv_notification.layoutManager = LinearLayoutManager(requireContext())
        rv_notification.adapter = adapter
        notificationsViewModel.notifications.observe(viewLifecycleOwner, EventObserver {
            if (it is Result.Success) {
                adapter.updateData(it.data.notifications)
            }
            if (it is Result.Error) {
                showShortToast("Failed")
            }
        })

    }

    override fun onDestroy() {
        mNotificationEvents?.dispose()
        super.onDestroy()
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
        override fun onPostClick(notification: NotificationsItem) {
            navigateToPostView(notification.postId ?: "")
        }

        override fun onProfileClick(notification: NotificationsItem, imageView: ImageView) {
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