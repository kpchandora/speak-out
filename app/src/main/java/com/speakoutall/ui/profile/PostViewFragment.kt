package com.speakoutall.ui.profile

import android.annotation.SuppressLint
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
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.speakoutall.R
import com.speakoutall.api.RetrofitBuilder
import com.speakoutall.common.EventObserver
import com.speakoutall.common.Result
import com.speakoutall.databinding.FragmentPostViewBinding
import com.speakoutall.events.*
import com.speakoutall.extensions.*
import com.speakoutall.posts.PostsRepository
import com.speakoutall.posts.view.OnPostOptionsClickListener
import com.speakoutall.posts.view.PostOptionsDialog
import com.speakoutall.posts.create.PostData
import com.speakoutall.posts.view.PostRecyclerViewAdapter
import com.speakoutall.ui.home.HomeViewModel
import com.speakoutall.posts.view.PostClickEventListener
import com.speakoutall.users.ActionType
import com.speakoutall.utils.AppPreference
import com.speakoutall.utils.ImageUtils
import com.speakoutall.utils.Utils
import timber.log.Timber


class PostViewFragment : Fragment() {

    private val safeArgs: PostViewFragmentArgs by navArgs()
    private lateinit var mPostsAdapter: PostRecyclerViewAdapter
    private val navHomeViewModel: HomeViewModel by navGraphViewModels(R.id.profile_navigation) {
        val appPreference = AppPreference
        HomeViewModel(
            appPreference,
            PostsRepository(RetrofitBuilder.apiService, appPreference)
        ).createFactory()
    }
    private val singleHomeViewModel: HomeViewModel by viewModels {
        val appPreference = AppPreference
        HomeViewModel(
            appPreference,
            PostsRepository(RetrofitBuilder.apiService, appPreference)
        ).createFactory()
    }
    private lateinit var mHomeViewModel: HomeViewModel
    private lateinit var dialog: PostOptionsDialog
    private var _binding: FragmentPostViewBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (safeArgs.isFromNotification) {
            mHomeViewModel = singleHomeViewModel
            mHomeViewModel.getSinglePost(safeArgs.postId)
        } else {
            mHomeViewModel = navHomeViewModel
        }
        mPostsAdapter = PostRecyclerViewAdapter(mHomeViewModel.mPostList)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPostViewBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar(view)
        _binding?.toolbarContainer?.toolbarTitle?.text = "Posts"
        dialog = PostOptionsDialog(requireContext())
        mPostsAdapter.mEventListener = mPostEventsListener
        _binding?.fragmentPostViewRv?.run {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = mPostsAdapter
        }

        mHomeViewModel.singlePost.observe(viewLifecycleOwner, EventObserver {
            _binding?.progressBar?.gone()
            if (it is Result.Success) {
                mPostsAdapter.updatePosts(listOf(it.data))
            }
            if (it is Result.Error) {
                showShortToast("Failed to fetch post")
            }
        })

        if (!safeArgs.isFromNotification) {
            mHomeViewModel.posts.observe(viewLifecycleOwner, Observer {
                _binding?.progressBar?.gone()
                mPostsAdapter.notifyDataSetChanged()
            })
            _binding?.fragmentPostViewRv?.scrollToPosition(safeArgs.itemPosition)
        }
        observeViewModels()
    }

    private fun observeViewModels() {
        mHomeViewModel.deletePost.observe(viewLifecycleOwner, EventObserver {
            if (it is Result.Success) {
                sendPostEvents(it.data.postId, PostEventTypes.DELETE)
                ProfileEvents.sendEvent(
                    context = requireContext(),
                    userId = it.data.postId,
                    eventType = ProfileEventTypes.DELETE_POST
                )
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

        mHomeViewModel.addBookmark.observe(viewLifecycleOwner, EventObserver {
            if (it is Result.Success) {
                sendPostEvents(it.data, PostEventTypes.ADD_BOOKMARK)
            }
            if (it is Result.Error) {
                mPostsAdapter.removeBookmark(it.data!!)
            }
        })

        mHomeViewModel.removeBookmark.observe(viewLifecycleOwner, EventObserver {
            if (it is Result.Success) {
                sendPostEvents(it.data, PostEventTypes.REMOVE_BOOKMARK)
            }
            if (it is Result.Error) {
                mPostsAdapter.addBookmark(it.data!!)
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
            val action = PostViewFragmentDirections.actionGlobalProfileFragment(
                userId = postData.userId,
                profileUrl = postData.photoUrl,
                transitionTag = postData.postId,
                username = postData.username
            )
            val extras = FragmentNavigatorExtras(
                profileImageView to postData.postId
            )
            findNavController().navigate(action, extras)
        }

        override fun onLikedUsersClick(postData: PostData) {
            val action = PostViewFragmentDirections.actionPostViewFragmentToUsersListFragment(
                postData.postId,
                ActionType.Likes
            )
            findNavController().navigate(action)
        }

        override fun onMenuClick(
            postData: PostData,
            view: View
        ) {
            dialog.setListener(mPostsOptionsClickListener)
            dialog.show()
            dialog.setPost(postData)
            dialog.setPostView(view)
        }

        override fun onBookmarkAdd(postData: PostData) {
            mHomeViewModel.addBookmark(postId = postData.postId, postedBy = postData.userId)
        }

        override fun onBookmarkRemove(postId: String) {
            mHomeViewModel.removeBookmark(postId)
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

    private val mPostsOptionsClickListener = object : OnPostOptionsClickListener {
        override fun onCopy(post: PostData) {
            Utils.copyText(requireContext(), post.content)
            showShortToast("Copied Successfully")
        }

        override fun onDelete(post: PostData) {
            mHomeViewModel.deletePost(post)
        }

        @SuppressLint("CheckResult")
        override fun onSave(post: PostData, view: View?) {
            view?.let {
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
}
