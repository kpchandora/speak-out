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
import com.speakout.common.Result
import kotlinx.android.synthetic.main.users_list_fragment.*
import timber.log.Timber

class UsersListFragment : Fragment() {

    private val safeArgs: UsersListFragmentArgs by navArgs()
    private val usersListViewModel: UsersListViewModel by viewModels()
    private val mAdapter = UsersListAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when (safeArgs.actionType) {
            ActionType.Likes -> {
                usersListViewModel.getLikesList(safeArgs.id ?: "")
            }
            ActionType.Followers -> {
                usersListViewModel.getFollowersList(safeArgs.id ?: "")
            }
            ActionType.Followings -> {
                usersListViewModel.getFollowingsList(safeArgs.id ?: "")
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
                mAdapter.updateData(it.data)
            }
        })

        usersListViewModel.followingsList.observe(viewLifecycleOwner, Observer {
            if (it is Result.Success) {
                mAdapter.updateData(it.data)
            }
        })

        usersListViewModel.likesList.observe(viewLifecycleOwner, Observer {
            if (it is Result.Success) {
                mAdapter.updateData(it.data)
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

    private val mUserClickListener = object : OnUserClickListener {
        override fun onUserClick(userMiniDetails: UserMiniDetails, profileImageView: ImageView) {
            navigateToProfile(userMiniDetails, profileImageView)
        }
    }

}
