package com.speakout.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.speakout.users.UsersListViewModel
import kotlinx.android.synthetic.main.fragment_notifications.*

class NotificationsFragment : Fragment() {

    private lateinit var safeArgs: NotificationsFragmentArgs
    private val notificationsViewModel: NotificationsViewModel by viewModels {
        NotificationsViewModel(NotificationRepository(RetrofitBuilder.apiService)).createFactory()
    }
    private lateinit var adapter: NotificationsAdapter
    private var mNotificationEvents: NotificationEvents? = null
    private var nextPageNumber = 1
    private var isLoading = false
    private var hasMoreData = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        safeArgs = NotificationsFragmentArgs.fromBundle(arguments!!)
        if (safeArgs.isFromDeepLink) {
            navigateToPostView(safeArgs.postId)
        }
        adapter = NotificationsAdapter(
            listener = mNotificationListener,
            notifications = notificationsViewModel.mNotifications
        )
        notificationsViewModel.getNotifications(nextPageNumber)
        mNotificationEvents = NotificationEvents(requireContext()) {
            nextPageNumber = 1
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

        notificationsViewModel.notifications.observe(viewLifecycleOwner, Observer {
            isLoading = false
            nextPageNumber = it.pageNumber + 1
            hasMoreData = it.notifications.size == NotificationsViewModel.MAX_SIZE
            adapter.notifyDataSetChanged()
        })

        notificationsViewModel.error.observe(viewLifecycleOwner, EventObserver {
            isLoading = false
            showShortToast(it)
        })

        rv_notification.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (isLoading || !hasMoreData) return
                if (dy > 0) {
                    (recyclerView.layoutManager as LinearLayoutManager).let {
                        val visibleItems = it.childCount
                        val totalItemsCount = it.itemCount
                        val firstVisibleItemPosition = it.findFirstVisibleItemPosition()
                        if (visibleItems + firstVisibleItemPosition >= totalItemsCount) {
                            notificationsViewModel.getNotifications(nextPageNumber)
                            isLoading = true
                        }
                    }
                }
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