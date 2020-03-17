package com.speakout.ui.profile

import android.app.Activity
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.speakout.R
import com.speakout.extensions.getScreenSize
import com.speakout.extensions.gone
import com.speakout.extensions.loadImage
import com.speakout.extensions.visible
import com.speakout.posts.create.PostData
import com.speakout.ui.home.HomePostViewHolder
import kotlinx.android.synthetic.main.item_home_post_layout.view.*
import kotlinx.android.synthetic.main.item_profile_post_layout.view.*

class ProfilePostsAdapter : RecyclerView.Adapter<ProfilePostsAdapter.ProfilePostsViewHolder>() {

    private val mPostsList = ArrayList<PostData>()

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
    }

    fun updateData(list: List<PostData>){
        mPostsList.clear()
        mPostsList.addAll(list)
        notifyDataSetChanged()
    }

    inner class ProfilePostsViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        fun bind(post: PostData) {
            view.item_profile_post_iv.loadImage(
                post.postImageUrl,
                R.drawable.ic_waiting
            )
        }
    }

}