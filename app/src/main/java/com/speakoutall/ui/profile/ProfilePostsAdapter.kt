package com.speakoutall.ui.profile

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.speakoutall.R
import com.speakoutall.databinding.ItemProfilePostLayoutBinding
import com.speakoutall.extensions.getScreenSize
import com.speakoutall.extensions.loadImage
import com.speakoutall.posts.create.PostData

class ProfilePostsAdapter(private val mPostsList: ArrayList<PostData>) :
    RecyclerView.Adapter<ProfilePostsAdapter.ProfilePostsViewHolder>() {

    var mListener: ProfilePostClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfilePostsViewHolder {
        val binding =
            ItemProfilePostLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = ProfilePostsViewHolder(binding)
        (parent.context as? Activity)?.getScreenSize()?.let {
            holder.binding.itemProfilePostIv.layoutParams.height = it.widthPixels / 3
        }
        holder.mListener = this.mListener
        return holder
    }

    override fun getItemCount() = mPostsList.size

    override fun onBindViewHolder(holder: ProfilePostsViewHolder, position: Int) {
        holder.bind(mPostsList[position])
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

    class ProfilePostsViewHolder(val binding: ItemProfilePostLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var mListener: ProfilePostClickListener? = null

        init {
            binding.root.setOnClickListener {
                mListener?.onPostClick(
                    binding.root.tag as PostData,
                    binding.itemProfilePostIv,
                    adapterPosition
                )
            }
        }

        fun bind(post: PostData) {
            binding.root.tag = post
            binding.itemProfilePostIv.loadImage(
                post.postImageUrl,
                R.drawable.ic_waiting
            )
            binding.itemProfilePostIv.transitionName = post.postId
        }
    }

}