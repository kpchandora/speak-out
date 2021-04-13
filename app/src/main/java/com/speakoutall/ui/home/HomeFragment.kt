package com.speakoutall.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.speakoutall.R
import com.speakoutall.api.RetrofitBuilder
import com.speakoutall.auth.Type
import com.speakoutall.common.EventObserver
import com.speakoutall.common.Result
import com.speakoutall.events.PostEventTypes
import com.speakoutall.events.PostEvents
import com.speakoutall.extensions.*
import com.speakoutall.posts.PostsRepository
import com.speakoutall.posts.view.OnPostOptionsClickListener
import com.speakoutall.posts.view.PostOptionsDialog
import com.speakoutall.posts.view.PostRecyclerViewAdapter
import com.speakoutall.posts.create.PostData
import com.speakoutall.posts.view.PostClickEventListener
import com.speakoutall.ui.MainActivity
import com.speakoutall.ui.NavBadgeListener
import com.speakoutall.users.ActionType
import com.speakoutall.utils.AppPreference
import com.speakoutall.utils.Constants
import com.speakoutall.utils.ImageUtils
import com.speakoutall.utils.Utils
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.layout_toolbar.view.*
import timber.log.Timber

class HomeFragment : Fragment(), MainActivity.BottomIconDoubleClick {

    private val mHomeViewModel: HomeViewModel by activityViewModels {
        val appPreference = AppPreference
        HomeViewModel(
            appPreference,
            PostsRepository(RetrofitBuilder.apiService, appPreference)
        ).createFactory()
    }

    private lateinit var mPostsAdapter: PostRecyclerViewAdapter
    private lateinit var mPreference: AppPreference
    private lateinit var dialog: PostOptionsDialog
    private var isLoading = false
    private var postEvents: PostEvents? = null
    private var key: Long = 0L
    private var mBadgeListener: NavBadgeListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mBadgeListener = context as? NavBadgeListener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPreference = AppPreference

        mPostsAdapter = PostRecyclerViewAdapter(mHomeViewModel.mPostList)
        postEvents = PostEvents(requireContext()) {
            val postId: String = it.getStringExtra(PostEvents.POST_ID) ?: ""
            when (it.extras?.getInt(PostEvents.EVENT_TYPE)) {
                PostEventTypes.CREATE,
                PostEventTypes.FOLLOW,
                PostEventTypes.UN_FOLLOW,
                PostEventTypes.USER_DETAILS_UPDATE -> {
                    key = 0
                    mHomeViewModel.mPostList.clear()
                    mHomeViewModel.getFeed(key)
                }
                PostEventTypes.DELETE -> mPostsAdapter.deletePost(postId)
                PostEventTypes.LIKE -> mPostsAdapter.addLike(postId)
                PostEventTypes.REMOVE_LIKE -> mPostsAdapter.removeLike(postId)
                PostEventTypes.ADD_BOOKMARK -> mPostsAdapter.addBookmark(postId)
                PostEventTypes.REMOVE_BOOKMARK -> mPostsAdapter.removeBookmark(postId)
            }
        }

        when {
            !mPreference.isLoggedIn() -> {
                findNavController().navigate(HomeFragmentDirections.actionNavigationHomeToSignInFragment())
            }
            !mPreference.isUsernameProcessComplete() -> {
                findNavController().navigate(
                    HomeFragmentDirections.actionNavigationHomeToUserNameFragment(
                        type = Type.Create,
                        username = null
                    )
                )
            }
            else -> {
                mHomeViewModel.getFeed(key)
                mHomeViewModel.getCount()
            }
        }
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

        setUpWithAppBarConfiguration(view)?.let {
            it.toolbar_title.gone()
            view.toolbar_title_home.visible()
        }

        dialog = PostOptionsDialog(requireContext())
        mPostsAdapter.mEventListener = mPostEventsListener
        fragment_home_rv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = mPostsAdapter

            viewTreeObserver.addOnPreDrawListener {
                startPostponedEnterTransition()
                true
            }
        }
        postponeEnterTransition()
        fragment_home_rv.doOnPreDraw {
            startPostponedEnterTransition()
        }

        fragment_home_rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (isLoading || key == Constants.INVALID_KEY) return
                if (dy > 0) {
                    (recyclerView.layoutManager as LinearLayoutManager).let {
                        val visibleItems = it.childCount
                        val totalItemsCount = it.itemCount
                        val firstVisibleItemPosition = it.findFirstVisibleItemPosition()
                        if (visibleItems + firstVisibleItemPosition >= totalItemsCount) {
                            mHomeViewModel.getFeed(key)
                            isLoading = true
                        }
                    }
                }
            }
        })

        observeViewModels()

        swipe_home.setOnRefreshListener {
            key = 0
            mHomeViewModel.mPostList.clear()
            mPostsAdapter.notifyDataSetChanged()
            mHomeViewModel.getFeed(key)
        }
    }

    override fun onDestroyView() {
        mPostsAdapter.mEventListener = null
        super.onDestroyView()
    }

    override fun onDestroy() {
        postEvents?.dispose()
        super.onDestroy()
    }

    private fun observeViewModels() {
        mHomeViewModel.posts.observe(viewLifecycleOwner, Observer {
            if (mHomeViewModel.mPostList.isEmpty()) {
                view_empty_home_posts.visible()
            } else {
                view_empty_home_posts.gone()
            }
            swipe_home.isRefreshing = false
            isLoading = false
            key = it.key
            mPostsAdapter.notifyDataSetChanged()
        })

        mHomeViewModel.postsError.observe(viewLifecycleOwner, EventObserver {
            isLoading = false
            swipe_home.isRefreshing = false
            showShortToast(it)
        })

        mHomeViewModel.likePost.observe(viewLifecycleOwner, EventObserver {
            if (it is Result.Error) {
                mPostsAdapter.removeLike(it.data?.postId ?: "")
            }
        })

        mHomeViewModel.unlikePost.observe(viewLifecycleOwner, EventObserver {
            if (it is Result.Error) {
                mPostsAdapter.addLike(it.data?.postId ?: "")
            }
        })

        mHomeViewModel.deletePost.observe(viewLifecycleOwner, EventObserver {
            if (it is Result.Success) {
                mPostsAdapter.deletePost(it.data.postId)
                if (mHomeViewModel.mPostList.isEmpty()) {
                    view_empty_home_posts.visible()
                } else {
                    view_empty_home_posts.gone()
                }
            }

            if (it is Result.Error) {
                showShortToast("Failed to delete post")
            }
        })

        mHomeViewModel.removeBookmark.observe(viewLifecycleOwner, EventObserver {
            if (it is Result.Error) {
                mPostsAdapter.addBookmark(it.data ?: "")
            }
        })

        mHomeViewModel.addBookmark.observe(viewLifecycleOwner, EventObserver {
            if (it is Result.Error) {
                mPostsAdapter.removeBookmark(it.data ?: "")
            }
        })

        mHomeViewModel.count.observe(viewLifecycleOwner, EventObserver {
            if (it is Result.Success) {
                mBadgeListener?.updateBadgeVisibility(it.data > 0)
            }
        })

    }

    private fun navigateToProfile(
        postData: PostData,
        profileImageView: ImageView
    ) {
        val action = HomeFragmentDirections.actionHomeToProfileFragment(
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

    private fun navigateToUsersList(postData: PostData) {
        findNavController().navigate(
            HomeFragmentDirections.actionHomeToUsersListFragment(
                id = postData.postId,
                actionType = ActionType.Likes
            )
        )
    }

    private val mPostEventsListener = object : PostClickEventListener {
        override fun onLike(position: Int, postData: PostData) {
            mHomeViewModel.likePost(postData)
        }

        override fun onRemoveLike(position: Int, postData: PostData) {
            mHomeViewModel.unlikePost(postData)
        }

        override fun onProfileClick(postData: PostData, profileImageView: ImageView) {
            navigateToProfile(postData, profileImageView)
        }

        override fun onLikedUsersClick(postData: PostData) {
            navigateToUsersList(postData)
        }

        override fun onMenuClick(postData: PostData, position: Int) {
            dialog.setListener(mPostsOptionsClickListener)
            dialog.show()
            dialog.setPost(postData)
        }

        override fun onBookmarkAdd(postData: PostData) {
            mHomeViewModel.addBookmark(postId = postData.postId, postedBy = postData.userId)
        }

        override fun onBookmarkRemove(postId: String) {
            mHomeViewModel.removeBookmark(postId)
        }

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
        override fun onSave(post: PostData) {
            ImageUtils.saveImageToDevice(post.postImageUrl, requireContext())
                .withDefaultSchedulers()
                .subscribe({
                    Timber.d("Home Main Thread: ${Looper.getMainLooper() == Looper.myLooper()}")
                    if (it)
                        showShortToast("Saved Successfully")
                    else
                        showShortToast("Failed to save image")
                }, {
                    showShortToast(it.message ?: "")
                })

        }
    }

    override fun doubleClick() {
        fragment_home_rv.layoutManager?.smoothScrollToPosition(fragment_home_rv, null, 0)
    }

}