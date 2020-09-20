package com.speakout.posts.view

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.speakout.R
import com.speakout.extensions.getScreenSize
import com.speakout.posts.PostMiniDetails
import com.speakout.posts.create.PostData
import kotlinx.android.synthetic.main.item_post_layout.view.*
import timber.log.Timber
import kotlin.collections.ArrayList

class PostRecyclerViewAdapter : RecyclerView.Adapter<PostViewHolder>() {

    private val mPostsList = ArrayList<PostData>()
    var mEventListener: PostClickEventListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post_layout, parent, false)
        val holder = PostViewHolder(view, mEventListener)

        val screenSize = (parent.context as? Activity)?.getScreenSize()
        screenSize?.let {
            holder.view.item_post_image_iv.layoutParams.height = it.widthPixels
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
        position: Int, payloads: MutableList<Any>
    ) {

        if (payloads.isNotEmpty()) {
            (payloads[0] as? PostData)?.let {
                Timber.d("BindView Content: ${it.content}")
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
            }
        }
    }

    fun addLike(postId: String) {
        mPostsList.forEachIndexed { index, postDataItem ->
            if (postId == postDataItem.postId) {
                postDataItem.isLikedBySelf = true
                postDataItem.likesCount++
                notifyItemChanged(index, postDataItem)
            }
        }
    }

    fun updatePosts(list: List<PostData>) {
        mPostsList.clear()
        mPostsList.addAll(list)
        notifyDataSetChanged()
    }


}