package com.speakoutall.ui.profile

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.speakoutall.R
import com.speakoutall.extensions.getScreenSize
import com.speakoutall.extensions.loadImage
import com.speakoutall.posts.create.PostData
import kotlinx.android.synthetic.main.item_profile_post_layout.view.*

class ProfilePostsAdapter(private val mPostsList: ArrayList<PostData>) :
    RecyclerView.Adapter<ProfilePostsAdapter.ProfilePostsViewHolder>() {

    var mListener: ProfilePostClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfilePostsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_profile_post_layout, parent, false)
        val holder = ProfilePostsViewHolder(view = view)
        (parent.context as? Activity)?.getScreenSize()?.let {
            holder.view.item_profile_post_iv.layoutParams.height = it.widthPixels / 3
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

    class ProfilePostsViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        var mListener: ProfilePostClickListener? = null

        init {
            view.setOnClickListener {
                mListener?.onPostClick(
                    view.tag as PostData,
                    view.item_profile_post_iv,
                    adapterPosition
                )
            }
        }

        fun bind(post: PostData) {
            view.tag = post
            view.item_profile_post_iv.loadImage(
                post.postImageUrl,
                R.drawable.ic_waiting
            )
            view.item_profile_post_iv.transitionName = post.postId
        }
    }

}