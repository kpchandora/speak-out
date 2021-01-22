package com.speakout.users

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

import com.speakout.R
import com.speakout.auth.UserMiniDetails
import com.speakout.common.EventObserver
import com.speakout.common.Result
import com.speakout.events.*
import com.speakout.extensions.setUpToolbar
import com.speakout.ui.profile.ProfileViewModel
import kotlinx.android.synthetic.main.users_list_fragment.*
import timber.log.Timber

class UsersListFragment : Fragment() {

    companion object {
        const val TAG = "UserListFragment"
    }

    private val safeArgs: UsersListFragmentArgs by navArgs()
    private val usersListViewModel: UsersListViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()
    private val mAdapter = UsersListAdapter()
    private var mUserEvents: UserEvents? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when (safeArgs.actionType) {
            ActionType.Likes -> {
                usersListViewModel.getLikesList(safeArgs.id!!)
            }
            ActionType.Followers -> {
                usersListViewModel.getFollowersList(safeArgs.id!!)
            }
            ActionType.Followings -> {
                usersListViewModel.getFollowingsList(safeArgs.id!!)
            }
        }

        mUserEvents = UserEvents(requireContext()) {
            val userId = it.getStringExtra(UserEvents.USER_ID) ?: return@UserEvents
            when (it.getIntExtra(UserEvents.EVENT_TYPE, -1)) {
                UserEventType.UN_FOLLOW -> {
                    mAdapter.showFollow(userId)
                }
                UserEventType.FOLLOW -> {
                    mAdapter.showFollowing(userId)
                }
                UserEventType.DIALOG_UN_FOLLOW -> {
                    mAdapter.showFollow(userId)
                    profileViewModel.unFollowUser(userId)
                }
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
        setUpToolbar(view)
        mAdapter.mListener = mUserClickListener
        users_list_rv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }
        observeViewModels()
    }

    private fun observeViewModels() {
        usersListViewModel.followersList.observe(viewLifecycleOwner, Observer {
            if (it is Result.Success) {
                mAdapter.addData(it.data)
            }
        })

        usersListViewModel.followingsList.observe(viewLifecycleOwner, Observer {
            if (it is Result.Success) {
                mAdapter.addData(it.data)
            }
        })

        usersListViewModel.likesList.observe(viewLifecycleOwner, Observer {
            if (it is Result.Success) {
                mAdapter.addData(it.data)
            }
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
        userMiniDetails: UserMiniDetails,
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
        override fun onUserClick(userMiniDetails: UserMiniDetails, profileImageView: ImageView) {
            navigateToProfile(userMiniDetails, profileImageView)
        }

        override fun onFollowClick(userMiniDetails: UserMiniDetails) {
            profileViewModel.followUser(userMiniDetails.userId)
        }

        override fun onUnFollowClick(userMiniDetails: UserMiniDetails) {
            val action = UsersListFragmentDirections.actionUsersListFragmentToUnFollowDialog(
                profileUrl = userMiniDetails.photoUrl,
                userId = userMiniDetails.userId,
                isFrom = TAG,
                username = userMiniDetails.username!!
            )
            findNavController().navigate(action)
        }
    }

}
