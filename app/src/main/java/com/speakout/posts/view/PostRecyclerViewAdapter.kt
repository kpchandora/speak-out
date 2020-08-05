package com.speakout.posts.view

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.speakout.R
import com.speakout.extensions.getScreenSize
import com.speakout.posts.PostMiniDetails
import com.speakout.posts.create.PostData
import com.speakout.utils.AppPreference
import kotlinx.android.synthetic.main.item_post_layout.view.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class PostRecyclerViewAdapter : RecyclerView.Adapter<PostViewHolder>() {

    private val mPostsList = ArrayList<PostData>()
    var mEventListener: PostClickEventListener? = null
    private val userId = AppPreference.getUserId()
    private val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post_layout, parent, false)
        val holder = PostViewHolder(view, simpleDateFormat)
        holder.userId = userId

        val screenSize = (parent.context as? Activity)?.getScreenSize()
        screenSize?.let {
            holder.view.item_post_image_iv.layoutParams.height = it.widthPixels
        }
        return holder
    }

    override fun getItemCount() = mPostsList.size

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.apply {
            mEventListener = this@PostRecyclerViewAdapter.mEventListener
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
            } ?: super.onBindViewHolder(holder, position, payloads)
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }

    }

    fun deletePost(postData: PostMiniDetails) {
        var matchedPost: PostData? = null
        mPostsList.forEachIndexed { index, post ->
            if (postData.postId == post.postId) {
                notifyItemRemoved(index)
                matchedPost = post
                return@forEachIndexed
            }
        }
        matchedPost?.let {
            mPostsList.remove(it)
        }
    }

    fun likePostFail(postMiniDetails: PostMiniDetails) {
        mPostsList.forEachIndexed { index, postDataItem ->
            if (postMiniDetails.postId == postDataItem.postId) {
                postDataItem.isLikedBySelf = false
                postDataItem.likesCount--
                notifyItemChanged(index, postDataItem)
            }
        }
    }

    fun unlikePostFail(postMiniDetails: PostMiniDetails) {
        mPostsList.forEachIndexed { index, postDataItem ->
            if (postMiniDetails.postId == postDataItem.postId) {
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