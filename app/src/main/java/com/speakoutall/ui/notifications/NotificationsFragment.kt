package com.speakoutall.ui.notifications

import android.app.NotificationManager
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
import com.speakoutall.R
import com.speakoutall.api.RetrofitBuilder
import com.speakoutall.common.EventObserver
import com.speakoutall.events.NotificationEvents
import com.speakoutall.extensions.*
import com.speakoutall.notification.NotificationRepository
import com.speakoutall.notification.NotificationsItem
import com.speakoutall.ui.MainActivity
import com.speakoutall.ui.NavBadgeListener
import com.speakoutall.utils.Constants
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
        (requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancelAll()
        rv_notification.setHasFixedSize(true)
        rv_notification.layoutManager = LinearLayoutManager(requireContext())
        rv_notification.adapter = adapter

        swipe_notifications.setOnRefreshListener {
            view_empty_notifications.gone()
            adapter.notifyDataSetChanged()
            refreshData()
        }

        swipe_notifications.isRefreshing = true
        notificationsViewModel.notifications.observe(viewLifecycleOwner, Observer {
            swipe_notifications.isRefreshing = false
            isLoading = false
            key = it.key
            adapter.notifyDataSetChanged()
            if (notificationsViewModel.mNotifications.isEmpty()) {
                view_empty_notifications.visible()
            } else {
                view_empty_notifications.gone()
            }
            notifications_pb.gone()
        })

        notificationsViewModel.error.observe(viewLifecycleOwner, EventObserver {
            swipe_notifications.isRefreshing = false
            isLoading = false
            notifications_pb.gone()
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

    override fun onResume() {
        super.onResume()
        mBadgeListener?.updateBadgeVisibility(false)
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