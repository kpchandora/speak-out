package com.speakout.ui.notifications

import android.content.Context
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
import com.speakout.ui.MainActivity
import com.speakout.ui.NavBadgeListener
import com.speakout.users.UsersListViewModel
import com.speakout.utils.Constants
import kotlinx.android.synthetic.main.fragment_notifications.*
import kotlinx.android.synthetic.main.layout_toolbar.view.*

class NotificationsFragment : Fragment(), MainActivity.BottomIconDoubleClick {

    private lateinit var safeArgs: NotificationsFragmentArgs
    private val notificationsViewModel: NotificationsViewModel by viewModels {
        NotificationsViewModel(NotificationRepository(RetrofitBuilder.apiService)).createFactory()
    }
    private lateinit var adapter: NotificationsAdapter
    private var mNotificationEvents: NotificationEvents? = null
    private var key: Long = 0L
    private var isLoading = false
    private var mBadgeListener: NavBadgeListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mBadgeListener = context as? NavBadgeListener
    }

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
        notificationsViewModel.getNotifications(key)
        notificationsViewModel.updateActions()
        mNotificationEvents = NotificationEvents(requireContext()) {
            refreshData()
        }
        mBadgeListener?.updateBadgeVisibility(false)
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
        setUpWithAppBarConfiguration(view)?.toolbar_title?.text =
            getString(R.string.title_notifications)

        rv_notification.setHasFixedSize(true)
        rv_notification.layoutManager = LinearLayoutManager(requireContext())
        rv_notification.adapter = adapter

        swipe_notifications.setOnRefreshListener {
            adapter.notifyDataSetChanged()
            refreshData()
        }

        notificationsViewModel.notifications.observe(viewLifecycleOwner, Observer {
            swipe_notifications.isRefreshing = false
            isLoading = false
            key = it.key
            adapter.notifyDataSetChanged()
        })

        notificationsViewModel.error.observe(viewLifecycleOwner, EventObserver {
            swipe_notifications.isRefreshing = false
            isLoading = false
            showShortToast(it)
        })

        rv_notification.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (isLoading || key == Constants.INVALID_KEY) return
                if (dy > 0) {
                    (recyclerView.layoutManager as LinearLayoutManager).let {
                        val visibleItems = it.childCount
                        val totalItemsCount = it.itemCount
                        val firstVisibleItemPosition = it.findFirstVisibleItemPosition()
                        if (visibleItems + firstVisibleItemPosition >= totalItemsCount) {
                            notificationsViewModel.getNotifications(key)
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

    private fun refreshData() {
        key = 0
        notificationsViewModel.mNotifications.clear()
        notificationsViewModel.getNotifications(key)
        notificationsViewModel.updateActions()
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

    override fun doubleClick() {
        rv_notification.layoutManager?.smoothScrollToPosition(rv_notification, null, 0)
    }

}