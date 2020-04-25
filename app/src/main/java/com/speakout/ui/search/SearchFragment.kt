package com.speakout.ui.search

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager

import com.speakout.R
import com.speakout.auth.UserMiniDetails
import com.speakout.common.EventObserver
import com.speakout.extensions.gone
import com.speakout.extensions.isNotNullOrEmpty
import com.speakout.extensions.visible
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.fragment_search.users_list_rv


class SearchFragment : Fragment() {

    private val searchViewModel: SearchViewModel by viewModels()
    private val mAdapter = SearchAdapter()
    private var postsCount = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lottie_search_user_animation.speed = 2f
        lottie_search_user_animation.gone()
        mAdapter.mListener = mUserClickListener
        users_list_rv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }

        search_view.setOnQueryTextListener(DebouncingQueryTextListener(
            runBefore = {
                if (lottie_search_empty_animation.isAnimating) {
                    lottie_search_empty_animation.pauseAnimation()
                    lottie_search_empty_animation.gone()
                }
                if (it.isNotNullOrEmpty()) {
                    if (postsCount == 0 && !lottie_search_user_animation.isAnimating) {
                        lottie_search_user_animation.visible()
                        lottie_search_user_animation.progress = 0f
                        lottie_search_user_animation.playAnimation()
                    }
                } else {
                    lottie_search_user_animation.pauseAnimation()
                    lottie_search_user_animation.gone()
                }
            },
            onDebouncingQueryTextChange = {
                if (it.isNotNullOrEmpty()) {
                    searchViewModel.searchUsers(it!!)
                } else {
                    postsCount = 0
                    mAdapter.updateData(emptyList())
                }
            }
        ))

        searchViewModel.searchUsers.observe(viewLifecycleOwner, EventObserver {
            lottie_search_user_animation.pauseAnimation()
            lottie_search_user_animation.gone()
            postsCount = it.size
            if (it.isEmpty()) {
                lottie_search_empty_animation.visible()
                lottie_search_empty_animation.playAnimation()
                mAdapter.updateData(emptyList())
            } else {
                mAdapter.updateData(it)
            }
        })
    }

    private fun navigateToProfile(
        userMiniDetails: UserMiniDetails,
        profileImageView: ImageView
    ) {
        val action = SearchFragmentDirections.actionSearchFragmentToNavigationProfile(
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

    private val mUserClickListener = object : OnSearchUserClickListener {
        override fun onUserClick(userMiniDetails: UserMiniDetails, profileImageView: ImageView) {
            navigateToProfile(userMiniDetails, profileImageView)
        }
    }
}
