package com.speakout.ui.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager

import com.speakout.R
import com.speakout.posts.create.PostData
import com.speakout.ui.home.HomePostRecyclerViewAdapter
import com.speakout.ui.home.HomeViewModel
import com.speakout.ui.home.PostClickEventListener
import com.speakout.ui.profile.postview.PostViewFragmentArgs
import com.speakout.ui.profile.postview.PostViewFragmentDirections
import com.speakout.users.ActionType
import kotlinx.android.synthetic.main.fragment_post_view.*


class PostViewFragment : Fragment() {

    private val safeArgs: PostViewFragmentArgs by navArgs()
    private val profileViewModel: ProfileViewModel by viewModels()
    private val mPostsAdapter = HomePostRecyclerViewAdapter()
    private val homeViewModel: HomeViewModel by navGraphViewModels(R.id.profile_navigation)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_post_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mPostsAdapter.mEventListener = mPostEventsListener
        fragment_post_view_rv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = mPostsAdapter
        }

        mPostsAdapter.updatePosts(homeViewModel.getPosts())
        fragment_post_view_rv.scrollToPosition(safeArgs.itemPosition)
        observeViewModels()

    }

    private fun observeViewModels() {
        
    }

    private val mPostEventsListener = object : PostClickEventListener {
        override fun onLike(position: Int, postData: PostData) {
            homeViewModel.likePost(postData)
        }

        override fun onDislike(position: Int, postData: PostData) {
            homeViewModel.unlikePost(postData)
        }

        override fun onProfileClick(postData: PostData, profileImageView: ImageView) {
            findNavController().navigateUp()
        }

        override fun onLikedUsersClick(postData: PostData) {
            val action =
                PostViewFragmentDirections.actionPostViewFragmentToUsersListFragment(
                    postData.userId,
                    ActionType.Likes
                )
            findNavController().navigate(action)
        }
    }

}
