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
import kotlinx.android.synthetic.main.users_list_fragment.*
import timber.log.Timber

class UsersListFragment : Fragment() {

    companion object {
        const val TAG = "UsersListFragment"

        fun newInstance() = UsersListFragment()

    }

    private val safeArgs: UsersListFragmentArgs by navArgs()
    private val usersListViewModel: UsersListViewModel by viewModels()
    private val mAdapter = UsersListAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        usersListViewModel.getPosts("")
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
        usersListViewModel.usersList.observe(viewLifecycleOwner, Observer {
            mAdapter.updateData(it)
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
