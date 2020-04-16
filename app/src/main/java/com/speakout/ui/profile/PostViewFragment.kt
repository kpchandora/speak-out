package com.speakout.ui.profile

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager

import com.speakout.R
import com.speakout.common.EventObserver
import com.speakout.extensions.showShortToast
import com.speakout.extensions.withDefaultSchedulers
import com.speakout.posts.OnPostOptionsClickListener
import com.speakout.posts.PostOptionsDialog
import com.speakout.posts.create.PostData
import com.speakout.ui.home.HomePostRecyclerViewAdapter
import com.speakout.ui.home.HomeViewModel
import com.speakout.ui.home.PostClickEventListener
import com.speakout.users.ActionType
import com.speakout.utils.ImageUtils
import kotlinx.android.synthetic.main.fragment_post_view.*
import timber.log.Timber


class PostViewFragment : Fragment() {

    private val safeArgs: PostViewFragmentArgs by navArgs()
    private val profileViewModel: ProfileViewModel by viewModels()
    private val mPostsAdapter = HomePostRecyclerViewAdapter()
    private val homeViewModel: HomeViewModel by navGraphViewModels(R.id.profile_navigation)
    private lateinit var dialog: PostOptionsDialog

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
        dialog = PostOptionsDialog(requireContext())
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
        homeViewModel.deletePost.observe(viewLifecycleOwner, EventObserver {
            if (it) {
                showShortToast("Deleted Successfully")
            } else {
                showShortToast("Failed to delete post")
            }
        })
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
            val action = PostViewFragmentDirections.actionPostViewFragmentToUsersListFragment(
                postData.userId,
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

    private val mPostsOptionsClickListener = object : OnPostOptionsClickListener {
        override fun onCopy(post: PostData) {
            val clipboard =
                requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Content", post.content)
            clipboard.setPrimaryClip(clip)
            showShortToast("Copied Successfully")
        }

        override fun onDelete(post: PostData) {
            homeViewModel.deletePost(post)
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

}
