package com.speakoutall.posts.view

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.speakoutall.databinding.ItemPostLayoutBinding
import com.speakoutall.extensions.getScreenSize
import com.speakoutall.posts.create.PostData
import timber.log.Timber
import kotlin.collections.ArrayList

class PostRecyclerViewAdapter(private val mPostsList: ArrayList<PostData>) :
    RecyclerView.Adapter<PostViewHolder>() {

    var mEventListener: PostClickEventListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding =
            ItemPostLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = PostViewHolder(binding, mEventListener)

        val screenSize = (parent.context as? Activity)?.getScreenSize()
        screenSize?.let {
            binding.postBg.layoutParams.height = it.widthPixels
        }
        return holder
    }

    override fun getItemCount() = mPostsList.size

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.apply {
            bind(mPostsList[position])
        }
    }


    override fun onBindViewHolder(
        holder: PostViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty()) {
            (payloads[0] as? PostData)?.let {
                Timber.d("BindView Content: ${it.content}")
                //TODO Only update like content; do not call bind again
                holder.bind(it)
                return
            }
        }
        super.onBindViewHolder(holder, position, payloads)
    }

    fun deletePost(postId: String) {
        var matchedPost: PostData? = null
        mPostsList.forEachIndexed { index, post ->
            if (postId == post.postId) {
                notifyItemRemoved(index)
                matchedPost = post
                return@forEachIndexed
            }
        }
        matchedPost?.let {
            mPostsList.remove(it)
        }
    }

    fun removeLike(postId: String) {
        mPostsList.forEachIndexed { index, postDataItem ->
            if (postId == postDataItem.postId) {
                postDataItem.isLikedBySelf = false
                postDataItem.likesCount--
                notifyItemChanged(index, postDataItem)
                return@forEachIndexed
            }
        }
    }

    fun addLike(postId: String) {
        mPostsList.forEachIndexed { index, postDataItem ->
            if (postId == postDataItem.postId) {
                postDataItem.isLikedBySelf = true
                postDataItem.likesCount++
                notifyItemChanged(index, postDataItem)
                return@forEachIndexed
            }
        }
    }

    fun addBookmark(postId: String) {
        mPostsList.forEachIndexed { index, postDataItem ->
            if (postId == postDataItem.postId) {
                postDataItem.isBookmarkedBySelf = true
                notifyItemChanged(index, postDataItem)
                return@forEachIndexed
            }
        }
    }

    fun removeBookmark(postId: String) {
        mPostsList.forEachIndexed { index, postDataItem ->
            if (postId == postDataItem.postId) {
                postDataItem.isBookmarkedBySelf = false
                notifyItemChanged(index, postDataItem)
                return@forEachIndexed
            }
        }
    }

    fun updatePosts(list: List<PostData>) {
        mPostsList.addAll(list)
        notifyDataSetChanged()
    }


}