package com.speakout.ui.home

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
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
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.speakout.R
import com.speakout.auth.Type
import com.speakout.common.EventObserver
import com.speakout.common.Result
import com.speakout.extensions.setUpWithAppBarConfiguration
import com.speakout.extensions.showShortToast
import com.speakout.extensions.visible
import com.speakout.extensions.withDefaultSchedulers
import com.speakout.posts.view.OnPostOptionsClickListener
import com.speakout.posts.view.PostOptionsDialog
import com.speakout.posts.view.PostRecyclerViewAdapter
import com.speakout.posts.create.PostData
import com.speakout.posts.view.PostClickEventListener
import com.speakout.ui.MainActivity
import com.speakout.ui.MainViewModel
import com.speakout.users.ActionType
import com.speakout.utils.AppPreference
import com.speakout.utils.ImageUtils
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.layout_toolbar.view.*
import timber.log.Timber

class HomeFragment : Fragment(), MainActivity.BottomIconDoubleClick {

    private val mHomeViewModel: HomeViewModel by activityViewModels()
    private val mPostsAdapter = PostRecyclerViewAdapter()
    private lateinit var mPreference: AppPreference
    private lateinit var dialog: PostOptionsDialog
    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("onCreate")
        mPreference = AppPreference

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
                mHomeViewModel.getFeed()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        sharedElementReturnTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
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

        Timber.d("User Id ${AppPreference.getUserId()}")

        fragment_home_rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (isLoading) return
                if (dy > 0) {
                    (recyclerView.layoutManager as LinearLayoutManager).let {
                        val visibleItems = it.childCount
                        val totalItemsCount = it.itemCount
                        val firstVisibleItemPosition = it.findFirstVisibleItemPosition()
                        val lastVisibleItemPosition = it.findLastVisibleItemPosition()
                        Timber.d(
                            "visibleItems: $visibleItems, totalItemsCount: $totalItemsCount, " +
                                    "firstVisibleItemPosition: $firstVisibleItemPosition, lastVisibleItemPosition: $lastVisibleItemPosition"
                        )
                        if (visibleItems + firstVisibleItemPosition >= totalItemsCount) {
                            mHomeViewModel.getFeed()
                            isLoading = true
                        }
                        if (totalItemsCount - 1 == lastVisibleItemPosition) {
                            Timber.d("Load more data new")
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

    private fun observeViewModels() {
        mHomeViewModel.posts.observe(viewLifecycleOwner, EventObserver {
            if (it is Result.Success) {
                isLoading = false
                mPostsAdapter.updatePosts(it.data)
            }

            if (it is Result.Error) {
                Timber.d("Failed to fetch posts: ${it.error}")
            }
        })

        mHomeViewModel.likePost.observe(viewLifecycleOwner, EventObserver {
            if (it is Result.Error) {
                mPostsAdapter.likePostFail(it.data!!)
            }
        })


        mHomeViewModel.unlikePost.observe(viewLifecycleOwner, EventObserver {
            if (it is Result.Error) {
                mPostsAdapter.unlikePostFail(it.data!!)
            }
        })

        mHomeViewModel.deletePost.observe(viewLifecycleOwner, EventObserver {
            if (it is Result.Success) {
                Timber.d("Delete Success: ${it.data.postId}")
                mPostsAdapter.deletePost(it.data)
                showShortToast("Deleted Successfully")
            }

            if (it is Result.Error) {
                Timber.d("Delete Failed: ${it.data?.postId}")
                showShortToast("Failed to delete post")
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

    private val mPostEventsListener = object :
        PostClickEventListener {
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

        override fun onMenuClick(postData: PostData, position: Int) {
            dialog.setListener(mPostsOptionsClickListener)
            dialog.show()
            dialog.setPost(postData)
        }
    }


    private val mPostsOptionsClickListener = object :
        OnPostOptionsClickListener {
        override fun onCopy(post: PostData) {
            val clipboard =
                requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Content", post.content)
            clipboard.setPrimaryClip(clip)
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