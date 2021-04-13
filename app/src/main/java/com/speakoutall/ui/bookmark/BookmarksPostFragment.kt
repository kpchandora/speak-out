package com.speakoutall.ui.bookmark

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.speakoutall.R
import com.speakoutall.api.RetrofitBuilder
import com.speakoutall.common.EventObserver
import com.speakoutall.databinding.FragmentBookmarksPostBinding
import com.speakoutall.events.PostEventTypes
import com.speakoutall.events.PostEvents
import com.speakoutall.extensions.*
import com.speakoutall.posts.PostsRepository
import com.speakoutall.posts.create.PostData
import com.speakoutall.ui.MainActivity
import com.speakoutall.ui.profile.ProfilePostClickListener
import com.speakoutall.ui.profile.ProfilePostsAdapter
import com.speakoutall.utils.AppPreference
import com.speakoutall.utils.Constants
import kotlinx.android.synthetic.main.fragment_bookmarks_post.*
import kotlinx.android.synthetic.main.layout_toolbar.view.*

class BookmarksPostFragment : Fragment(), MainActivity.BottomIconDoubleClick {

    private lateinit var mBinding: FragmentBookmarksPostBinding

    private val mBookmarksViewModel: BookmarksViewModel by viewModels() {
        BookmarksViewModel(
            PostsRepository(
                RetrofitBuilder.apiService,
                AppPreference
            )
        ).createFactory()
    }

    private var isLoading = false
    private var key: Long = 0L
    private lateinit var mPostsAdapter: ProfilePostsAdapter
    private var postEvents: PostEvents? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBookmarksViewModel.getBookmarks(key)
        mPostsAdapter = ProfilePostsAdapter(mBookmarksViewModel.mPostList)
        mPostsAdapter.mListener = mListener
        postEvents = PostEvents(requireContext()) {
            val postId: String = it.getStringExtra(PostEvents.POST_ID) ?: ""
            when (it.extras?.getInt(PostEvents.EVENT_TYPE)) {
                PostEventTypes.DELETE,
                PostEventTypes.REMOVE_BOOKMARK -> mPostsAdapter.deletePost(postId)
                PostEventTypes.LIKE -> mPostsAdapter.addLike(postId)
                PostEventTypes.REMOVE_LIKE -> mPostsAdapter.removeLike(postId)
                PostEventTypes.ADD_BOOKMARK -> {
                    key = 0
                    mBookmarksViewModel.mPostList.clear()
                    mBookmarksViewModel.getBookmarks(key)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentBookmarksPostBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar(view)?.toolbar_title?.text = getString(R.string.text_bookmarks)

        mBinding.rvBookmarks.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (isLoading || key == Constants.INVALID_KEY) return
                if (dy > 0) {
                    (recyclerView.layoutManager as LinearLayoutManager).let {
                        val visibleItems = it.childCount
                        val totalItemsCount = it.itemCount
                        val firstVisibleItemPosition = it.findFirstVisibleItemPosition()
                        if (visibleItems + firstVisibleItemPosition >= totalItemsCount) {
                            mBookmarksViewModel.getBookmarks(key)
                            isLoading = true
                        }
                    }
                }
            }
        })

        mBinding.rvBookmarks.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(context, 3)
            adapter = mPostsAdapter
        }

        mBookmarksViewModel.posts.observe(viewLifecycleOwner, Observer {
            if (mBookmarksViewModel.mPostList.isEmpty()) {
                view_empty_bookmarks_posts.visible()
            } else {
                view_empty_bookmarks_posts.gone()
            }
            mBinding.swipeBookmarks.isRefreshing = false
            isLoading = false
            key = it.key
            mPostsAdapter.notifyDataSetChanged()
        })

        mBookmarksViewModel.postsError.observe(viewLifecycleOwner, EventObserver {
            isLoading = false
            mBinding.swipeBookmarks.isRefreshing = false
            showShortToast(it)
        })

        mBinding.swipeBookmarks.setOnRefreshListener {
            key = 0
            mBookmarksViewModel.mPostList.clear()
            mPostsAdapter.notifyDataSetChanged()
            mBookmarksViewModel.getBookmarks(key)
        }

    }

    override fun doubleClick() {
        mBinding.rvBookmarks.layoutManager?.smoothScrollToPosition(mBinding.rvBookmarks, null, 0)
    }

    private val mListener = object : ProfilePostClickListener {
        override fun onPostClick(postData: PostData, postImageView: ImageView, position: Int) {
            val action =
                BookmarksPostFragmentDirections.actionBookmarksToPostViewFragment(
                    isFromNotification = true, postId = postData.postId
                )
            findNavController().navigate(action)
        }
    }

    override fun onDestroy() {
        postEvents?.dispose()
        super.onDestroy()
    }

}