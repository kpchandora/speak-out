package com.speakout.ui.profile

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.speakout.R
import com.speakout.extensions.getScreenSize
import com.speakout.extensions.loadImage
import com.speakout.posts.create.PostData
import kotlinx.android.synthetic.main.item_profile_post_layout.view.*

class ProfilePostsAdapter : RecyclerView.Adapter<ProfilePostsAdapter.ProfilePostsViewHolder>() {

    private val mPostsList = ArrayList<PostData>()
    var mListener: ProfilePostClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfilePostsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_profile_post_layout, parent, false)
        val holder = ProfilePostsViewHolder(view = view)
        (parent.context as? Activity)?.getScreenSize()?.let {
            holder.view.item_profile_post_iv.layoutParams.height = it.widthPixels / 3
        }
        return holder
    }

    override fun getItemCount() = mPostsList.size

    override fun onBindViewHolder(holder: ProfilePostsViewHolder, position: Int) {
        holder.bind(mPostsList[position])
        holder.mListener = this.mListener
    }

    fun updateData(list: List<PostData>) {
        mPostsList.clear()
        mPostsList.addAll(list)
        notifyDataSetChanged()
    }

    class ProfilePostsViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        var mListener: ProfilePostClickListener? = null

        init {
            view.setOnClickListener {
                mListener?.onPostClick(view.tag as PostData, view.item_profile_post_iv, adapterPosition)
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