package com.speakout.ui.bookmark

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.speakout.R
import com.speakout.api.RetrofitBuilder
import com.speakout.common.EventObserver
import com.speakout.databinding.FragmentBookmarksPostBinding
import com.speakout.extensions.createFactory
import com.speakout.extensions.setUpToolbar
import com.speakout.extensions.showShortToast
import com.speakout.posts.PostsRepository
import com.speakout.ui.profile.ProfilePostsAdapter
import com.speakout.utils.AppPreference
import com.speakout.utils.Constants
import kotlinx.android.synthetic.main.layout_toolbar.view.*

class BookmarksPostFragment : Fragment() {

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBookmarksViewModel.getBookmarks(key)
        mPostsAdapter = ProfilePostsAdapter(mBookmarksViewModel.mPostList)
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
            isLoading = false
            key = it.key
            mPostsAdapter.notifyDataSetChanged()
        })

        mBookmarksViewModel.postsError.observe(viewLifecycleOwner, EventObserver {
            isLoading = false
            showShortToast(it)
        })

    }


}