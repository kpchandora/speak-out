package com.speakout.ui.home

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.speakout.R
import com.speakout.extensions.getScreenSize
import com.speakout.posts.create.PostData
import com.speakout.utils.AppPreference
import kotlinx.android.synthetic.main.item_home_post_layout.view.*
import timber.log.Timber

class HomePostRecyclerViewAdapter : RecyclerView.Adapter<HomePostViewHolder>() {

    private val mPostsList = ArrayList<PostData>()
    var mEventListener: PostClickEventListener? = null
    private val userId = AppPreference.getUserId()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomePostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_home_post_layout, parent, false)
        val holder = HomePostViewHolder(view)
        holder.userId = userId

        val screenSize = (parent.context as? Activity)?.getScreenSize()
        screenSize?.let {
            holder.view.item_home_post_image_iv.layoutParams.height = it.widthPixels
        }
        return holder
    }

    override fun getItemCount() = mPostsList.size

    override fun onBindViewHolder(holder: HomePostViewHolder, position: Int) {
        holder.apply {
            mEventListener = this@HomePostRecyclerViewAdapter.mEventListener
            bind(mPostsList[position])
        }
    }


    override fun onBindViewHolder(
        holder: HomePostViewHolder,
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

    fun likePostFail(postData: PostData) {
        Timber.d("likePostFail Content: ${postData.content}")
        mPostsList.forEachIndexed { index, postDataItem ->
            if (postData.postId == postDataItem.postId) {
                postDataItem.likesSet.remove(userId)
                notifyItemChanged(index, postDataItem)
            }
        }
    }

    fun unlikePostFail(postData: PostData) {
        Timber.d("unlikePostFail Content: ${postData.content}")
        Timber.d("")
        mPostsList.forEachIndexed { index, postDataItem ->
            if (postData.postId == postDataItem.postId) {
                postDataItem.likesSet.add(userId)
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