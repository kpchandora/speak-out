package com.speakoutall.users

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.speakoutall.R
import com.speakoutall.api.RetrofitBuilder
import com.speakoutall.auth.UsersItem
import com.speakoutall.common.EventObserver
import com.speakoutall.common.Result
import com.speakoutall.events.*
import com.speakoutall.extensions.createFactory
import com.speakoutall.extensions.setUpToolbar
import com.speakoutall.extensions.showShortToast
import com.speakoutall.ui.profile.ProfileViewModel
import com.speakoutall.ui.profile.UnFollowDialog
import com.speakoutall.ui.profile.UnFollowDialogModel
import com.speakoutall.utils.AppPreference
import com.speakoutall.utils.Constants
import kotlinx.android.synthetic.main.layout_toolbar.view.*
import kotlinx.android.synthetic.main.users_list_fragment.*

class UsersListFragment : Fragment() {

    companion object {
        const val TAG = "UserListFragment"
    }

    private val safeArgs: UsersListFragmentArgs by navArgs()
    private val usersListViewModel: UsersListViewModel by viewModels() {
        val appPreference = AppPreference
        UsersListViewModel(
            appPreference,
            UsersRepository(
                RetrofitBuilder.apiService,
                appPreference
            )
        ).createFactory()
    }
    private val profileViewModel: ProfileViewModel by viewModels() {
        val appPreference = AppPreference
        ProfileViewModel(
            appPreference,
            UsersRepository(RetrofitBuilder.apiService, appPreference)
        ).createFactory()
    }
    private lateinit var mAdapter: UsersListAdapter
    private var mUserEvents: UserEvents? = null
    private var isLoading = false
    private var key: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAdapter = UsersListAdapter(usersListViewModel.mUsersList)
        loadData()
        mUserEvents = UserEvents(requireContext()) {
            val userId = it.getStringExtra(UserEvents.USER_ID) ?: return@UserEvents
            when (it.getIntExtra(UserEvents.EVENT_TYPE, -1)) {
                UserEventType.UN_FOLLOW -> {
                    mAdapter.showFollow(userId)
                }
                UserEventType.FOLLOW -> {
                    mAdapter.showFollowing(userId)
                }
            }
        }
    }

    private fun loadData() {
        when (safeArgs.actionType) {
            ActionType.Likes -> {
                usersListViewModel.getLikesList(safeArgs.id!!, key)
            }
            ActionType.Followers -> {
                usersListViewModel.getFollowersList(safeArgs.id!!, key)
            }
            ActionType.Followings -> {
                usersListViewModel.getFollowingsList(safeArgs.id!!, key)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.users_list_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar(view)?.toolbar_title?.text = safeArgs.actionType.name
        mAdapter.mListener = mUserClickListener
        users_list_rv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }
        users_list_rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (isLoading || key == Constants.INVALID_KEY) return
                if (dy > 0) {
                    (recyclerView.layoutManager as LinearLayoutManager).let {
                        val visibleItems = it.childCount
                        val totalItemsCount = it.itemCount
                        val firstVisibleItemPosition = it.findFirstVisibleItemPosition()
                        if (visibleItems + firstVisibleItemPosition >= totalItemsCount) {
                            loadData()
                            isLoading = true
                        }
                    }
                }
            }
        })
        observeViewModels()
    }

    private fun observeViewModels() {
        usersListViewModel.usersList.observe(viewLifecycleOwner, Observer {
            isLoading = false
            key = it.key
            mAdapter.notifyDataSetChanged()
        })

        usersListViewModel.error.observe(viewLifecycleOwner, EventObserver {
            isLoading = false
            showShortToast(it)
        })

        profileViewModel.followUser.observe(viewLifecycleOwner, EventObserver {
            if (it is Result.Success) {
                PostEvents.sendEvent(
                    context = requireContext(),
                    event = PostEventTypes.FOLLOW
                )
                sendProfileEvent(it.data.userId, ProfileEventTypes.FOLLOW)
            }
            if (it is Result.Error) {
                mAdapter.showFollow(it.data!!.userId)
            }
        })

        profileViewModel.unFollowUser.observe(viewLifecycleOwner, EventObserver {
            if (it is Result.Success) {
                PostEvents.sendEvent(
                    context = requireContext(),
                    event = PostEventTypes.UN_FOLLOW
                )
                sendProfileEvent(it.data.userId, ProfileEventTypes.UN_FOLLOW)
            }
            if (it is Result.Error) {
                mAdapter.showFollowing(it.data!!.userId)
            }
        })

    }

    private fun navigateToProfile(
        userMiniDetails: UsersItem,
        profileImageView: ImageView
    ) {
        val action = UsersListFragmentDirections.actionUsersListFragmentToNavigationProfile(
            userId = userMiniDetails.userId,
            username = userMiniDetails.username,
            transitionTag = userMiniDetails.name,
            profileUrl = userMiniDetails.photoUrl
        )

        val extras = FragmentNavigatorExtras(
            profileImageView to (userMiniDetails.name!!)
        )
        findNavController().navigate(action, extras)
    }

    override fun onDestroy() {
        mUserEvents?.dispose()
        super.onDestroy()
    }

    private fun sendProfileEvent(userId: String, type: Int) {
        ProfileEvents.sendEvent(
            context = requireContext(),
            userId = userId,
            eventType = type
        )
    }

    private val mUserClickListener = object : OnUserClickListener {
        override fun onUserClick(userMiniDetails: UsersItem, profileImageView: ImageView) {
            navigateToProfile(userMiniDetails, profileImageView)
        }

        override fun onFollowClick(userMiniDetails: UsersItem) {
            profileViewModel.followUser(userMiniDetails.userId)
        }

        override fun onUnFollowClick(userMiniDetails: UsersItem) {
            val model = UnFollowDialogModel(
                userId = userMiniDetails.userId, username = userMiniDetails.username ?: "",
                isFrom = TAG, profileUrl = userMiniDetails.photoUrl
            )
            val dialog = UnFollowDialog.newInstance(model)
            dialog.setListener(object : UnFollowDialog.UnFollowDialogListener {
                override fun onUnFollow(userId: String) {
                    mAdapter.showFollow(userId)
                    profileViewModel.unFollowUser(userId)
                }
            })
            dialog.show(requireActivity().supportFragmentManager, TAG)
        }
    }

}
