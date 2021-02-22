package com.speakout.ui.home

import android.annotation.SuppressLint
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.speakout.R
import com.speakout.api.RetrofitBuilder
import com.speakout.auth.Type
import com.speakout.common.EventObserver
import com.speakout.common.Result
import com.speakout.events.PostEventTypes
import com.speakout.events.PostEvents
import com.speakout.extensions.*
import com.speakout.posts.PostsRepository
import com.speakout.posts.view.OnPostOptionsClickListener
import com.speakout.posts.view.PostOptionsDialog
import com.speakout.posts.view.PostRecyclerViewAdapter
import com.speakout.posts.create.PostData
import com.speakout.posts.view.PostClickEventListener
import com.speakout.ui.MainActivity
import com.speakout.users.ActionType
import com.speakout.utils.AppPreference
import com.speakout.utils.ImageUtils
import com.speakout.utils.Utils
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
    private var hasMoreData = true
    private var nextPageNumber = 1
    private var postEvents: PostEvents? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPreference = AppPreference
        Timber.d("HomeFragment onCreate")
        mPostsAdapter = PostRecyclerViewAdapter(mHomeViewModel.mPostList)
        postEvents = PostEvents(requireContext()) {
            val postId: String = it.getStringExtra(PostEvents.POST_ID) ?: ""
            when (it.extras?.getInt(PostEvents.EVENT_TYPE)) {
                PostEventTypes.CREATE,
                PostEventTypes.FOLLOW,
                PostEventTypes.UN_FOLLOW,
                PostEventTypes.USER_DETAILS_UPDATE -> {
                    nextPageNumber = 1
                    mHomeViewModel.mPostList.clear()
                    mHomeViewModel.getFeed(nextPageNumber)
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
                mHomeViewModel.getFeed(nextPageNumber)
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
            it.title = ""
            view.toolbar_title_tv.visible()
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
                if (isLoading || !hasMoreData) return
                if (dy > 0) {
                    (recyclerView.layoutManager as LinearLayoutManager).let {
                        val visibleItems = it.childCount
                        val totalItemsCount = it.itemCount
                        val firstVisibleItemPosition = it.findFirstVisibleItemPosition()
                        if (visibleItems + firstVisibleItemPosition >= totalItemsCount) {
                            mHomeViewModel.getFeed(nextPageNumber)
                            isLoading = true
                        }
                    }
                }
            }
        })

        observeViewModels()
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
            isLoading = false
            hasMoreData = it.posts.size == HomeViewModel.FEED_POSTS_COUNT
            nextPageNumber = it.pageNumber + 1
            mPostsAdapter.notifyDataSetChanged()
        })

        mHomeViewModel.postsError.observe(viewLifecycleOwner, EventObserver {
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

        override fun onBookmarkAdd(postId: String) {
            mHomeViewModel.addBookmark(postId)
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