package com.speakout.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.speakout.R
import com.speakout.posts.create.PostData
import com.speakout.ui.MainViewModel
import com.speakout.users.ActionType
import com.speakout.utils.AppPreference
import kotlinx.android.synthetic.main.fragment_home.*
import timber.log.Timber

class HomeFragment : Fragment() {

    private val mHomeViewModel: HomeViewModel by activityViewModels()
    private val mPostsAdapter = HomePostRecyclerViewAdapter()
    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mHomeViewModel.getPosts("I6BSfzDRIAccVU4VzflwXTOJIDN2")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mPostsAdapter.mEventListener = mPostEventsListener
        fragment_home_rv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = mPostsAdapter
        }

        Timber.d("User Id ${AppPreference.getUserId()}")

        observeViewModels()
    }

    override fun onDestroyView() {
        mPostsAdapter.mEventListener = null
        super.onDestroyView()
    }

    private fun observeViewModels() {
        mHomeViewModel.posts.observe(viewLifecycleOwner, Observer {
            mPostsAdapter.updatePosts(it)
        })

        mHomeViewModel.likePost.observe(viewLifecycleOwner,
            Observer { b: Boolean ->
                Timber.d("likePost Content: $b")
//                if (!pair.first) {
//                    mPostsAdapter.likePostFail(pair.second)
//                }
            })

        mHomeViewModel.unlikePost.observe(viewLifecycleOwner,
            Observer { b: Boolean ->
                Timber.d("unlikePost Content: $b")
//                if (b) {
//                    mPostsAdapter.unlikePostFail(pair.second)
//                }
            })

    }

    private fun navigateToProfile(
        postData: PostData,
        profileImageView: ImageView
    ) {
        val action = HomeFragmentDirections.actionHomeToProfileFragment(
            userId = postData.userId,
            profileUrl = postData.userImageUrl,
            transitionTag = postData.postId,
            username = postData.username
        )
        val extras = FragmentNavigatorExtras(
            profileImageView to postData.postId
        )
        findNavController().navigate(action, extras)
    }

    private fun navigateToUsersList(postData: PostData) {
        findNavController().navigate(
            HomeFragmentDirections.actionHomeToUsersListFragment(
                userId = postData.userId,
                actionType = ActionType.Likes
            )
        )
    }

    private val mPostEventsListener = object : PostClickEventListener {
        override fun onLike(position: Int, postData: PostData) {
            mHomeViewModel.likePost(postData)
        }

        override fun onDislike(position: Int, postData: PostData) {
            mHomeViewModel.unlikePost(postData)
        }

        override fun onProfileClick(postData: PostData, profileImageView: ImageView) {
            navigateToProfile(postData, profileImageView)
        }

        override fun onLikedUsersClick(postData: PostData) {
            navigateToUsersList(postData)
        }
    }

}