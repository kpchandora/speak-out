package com.speakout.ui.profile

import android.annotation.SuppressLint
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
import com.speakout.common.EventObserver
import com.speakout.common.Result
import com.speakout.events.NotificationEvents
import com.speakout.events.PostEventTypes
import com.speakout.events.PostEvents
import com.speakout.extensions.*
import com.speakout.posts.view.OnPostOptionsClickListener
import com.speakout.posts.view.PostOptionsDialog
import com.speakout.posts.create.PostData
import com.speakout.posts.view.PostRecyclerViewAdapter
import com.speakout.ui.home.HomeViewModel
import com.speakout.posts.view.PostClickEventListener
import com.speakout.users.ActionType
import com.speakout.utils.ImageUtils
import com.speakout.utils.Utils
import kotlinx.android.synthetic.main.fragment_post_view.*
import timber.log.Timber


class PostViewFragment : Fragment() {

    private val safeArgs: PostViewFragmentArgs by navArgs()
    private val mPostsAdapter = PostRecyclerViewAdapter()
    private val navHomeViewModel: HomeViewModel by navGraphViewModels(R.id.profile_navigation)
    private val singleHomeViewModel: HomeViewModel by viewModels()
    private lateinit var mHomeViewModel: HomeViewModel
    private lateinit var dialog: PostOptionsDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (safeArgs.isFromNotification) {
            mHomeViewModel = singleHomeViewModel
            mHomeViewModel.getSinglePost(safeArgs.postId)
        } else {
            mHomeViewModel = navHomeViewModel
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_post_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar(view)
        dialog = PostOptionsDialog(requireContext())
        mPostsAdapter.mEventListener = mPostEventsListener
        fragment_post_view_rv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = mPostsAdapter
        }

        mHomeViewModel.singlePost.observe(viewLifecycleOwner, EventObserver {
            progressBar.gone()
            if (it is Result.Success) {
                mPostsAdapter.updatePosts(listOf(it.data))
            }
            if (it is Result.Error) {
                showShortToast("Failed to fetch post")
            }
        })

        if (!safeArgs.isFromNotification) {
            mPostsAdapter.updatePosts(mHomeViewModel.getProfilePosts())
            fragment_post_view_rv.scrollToPosition(safeArgs.itemPosition)
        } else {
            progressBar.visible()
        }
        observeViewModels()
    }

    private fun observeViewModels() {
        mHomeViewModel.deletePost.observe(viewLifecycleOwner, EventObserver {
            if (it is Result.Success) {
                sendPostEvents(it.data.postId, PostEventTypes.DELETE)
                Timber.d("Delete Success: ${it.data.postId}")
                mPostsAdapter.deletePost(it.data.postId)
                showShortToast("Deleted Successfully")
                if (safeArgs.isFromNotification) {
                    sendNotificationEvents()
                    findNavController().navigateUp()
                }
            }

            if (it is Result.Error) {
                Timber.d("Delete Failed: ${it.data?.postId}")
                showShortToast("Failed to delete post")
            }
        })

        mHomeViewModel.likePost.observe(viewLifecycleOwner, EventObserver {
            if (it is Result.Success) {
                sendPostEvents(it.data.postId, PostEventTypes.LIKE)
            }
            if (it is Result.Error) {
                mPostsAdapter.removeLike(it.data?.postId ?: "")
            }
        })

        mHomeViewModel.unlikePost.observe(viewLifecycleOwner, EventObserver {
            if (it is Result.Success) {
                sendPostEvents(it.data.postId, PostEventTypes.REMOVE_LIKE)
            }
            if (it is Result.Error) {
                mPostsAdapter.addLike(it.data?.postId ?: "")
            }
        })

    }

    private val mPostEventsListener = object : PostClickEventListener {
        override fun onLike(position: Int, postData: PostData) {
            mHomeViewModel.likePost(postData)
        }

        override fun onRemoveLike(position: Int, postData: PostData) {
            mHomeViewModel.unlikePost(postData)
        }

        override fun onProfileClick(postData: PostData, profileImageView: ImageView) {
            findNavController().navigateUp()
        }

        override fun onLikedUsersClick(postData: PostData) {
            val action = PostViewFragmentDirections.actionPostViewFragmentToUsersListFragment(
                postData.postId,
                ActionType.Likes
            )
            findNavController().navigate(action)
        }

        override fun onMenuClick(postData: PostData, position: Int) {
            dialog.setListener(mPostsOptionsClickListener)
            dialog.show()
            dialog.setPost(postData)
        }
    }

    private fun sendPostEvents(postId: String, eventType: Int) {
        PostEvents.sendEvent(
            context = requireContext(),
            postId = postId,
            event = eventType
        )
    }

    private fun sendNotificationEvents() {
        NotificationEvents.sendEvent(requireContext())
    }

    private val mPostsOptionsClickListener = object :
        OnPostOptionsClickListener {
        override fun onCopy(post: PostData) {
            Utils.copyText(requireContext(), post.content)
            showShortToast("Copied Successfully")
        }

        override fun onDelete(post: PostData) {
            mHomeViewModel.deletePost(post)
        }

        @SuppressLint("CheckResult")
        override fun onSave(post: PostData) {
            Timber.d("Save post")
            ImageUtils.saveImageToDevice(post.postImageUrl, requireContext())
                .withDefaultSchedulers()
                .subscribe({
                    if (it)
                        showShortToast("Saved Successfully")
                    else
                        showShortToast("Failed to save image")
                }, {
                    showShortToast(it.message ?: "")
                })
        }
    }
}
