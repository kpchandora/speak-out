package com.speakout.ui.home

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieDrawable
import com.speakout.R
import com.speakout.extensions.getScreenSize
import com.speakout.posts.create.PostData
import kotlinx.android.synthetic.main.item_home_post_layout.view.*

class HomePostRecyclerViewAdapter : RecyclerView.Adapter<HomePostViewHolder>() {

    private val mPostsList = ArrayList<PostData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomePostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_home_post_layout, parent, false)
        val holder = HomePostViewHolder(view)

        val screenSize = (parent.context as? Activity)?.getScreenSize()
        screenSize?.let {
            holder.view.item_home_post_image_iv.layoutParams.height = it.widthPixels
        }
        return holder
    }

    override fun getItemCount() = mPostsList.size

    fun updatePosts(list: List<PostData>) {
        mPostsList.clear()
        mPostsList.addAll(list)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: HomePostViewHolder, position: Int) {
        holder.apply {
            bind(mPostsList[position])
        }
    }
}