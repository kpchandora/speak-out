package com.speakout.posts.tags

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.speakout.R
import com.speakout.extensions.gone
import com.speakout.extensions.visible
import kotlinx.android.synthetic.main.item_tag_layout.view.*
import java.util.concurrent.atomic.AtomicBoolean

class TagsRecyclerViewAdapter : RecyclerView.Adapter<TagsRecyclerViewAdapter.TagsHolder>() {

    private val mTagsList = ArrayList<Tag>()
    private val mSelectedTags = hashSetOf<String>()
    private var mListener: OnTagClickListener? = null
    val isLoading = AtomicBoolean(false)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagsHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_tag_layout, parent, false)
        return TagsHolder(view)
    }

    override fun getItemCount() = mTagsList.size

    override fun getItemId(position: Int): Long {
        return mTagsList[position].id
    }

    override fun onBindViewHolder(holder: TagsHolder, position: Int) {
        holder.apply {
            bind(mTagsList[position])
            view.item_tags_main_layout.setOnClickListener {
                toggleSelection(mTagsList[adapterPosition].tag)
                notifyItemChanged(adapterPosition)
                mListener?.onTagClick(mTagsList[adapterPosition])
            }
        }
    }

    fun removeTag(tag: Tag) {
        mSelectedTags.remove(tag.tag)
        if (!isLoading.get()) {
            mTagsList.forEachIndexed { index: Int, t: Tag ->
                if (tag.id == t.id) {
                    notifyItemChanged(index)
                }
            }
        }
    }

    fun setListener(listener: OnTagClickListener) {
        mListener = listener
    }

    fun setData(list: List<Tag>) {
        mTagsList.clear()
        mTagsList.addAll(list)
        notifyDataSetChanged()
    }

    inner class TagsHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(tag: Tag) {
            view.item_tag_name_tv.text = tag.tag

            if ((tag.used ?: 0) > 0) {
                view.item_tag_posts_count_tv.visible()
                view.item_tag_posts_count_tv.text = "${(tag.used ?: 0)} posts"
            } else {
                view.item_tag_posts_count_tv.gone()
            }

            view.background = if (isSelected(tag.tag))
                ContextCompat.getDrawable(
                    view.context,
                    R.drawable.dr_round_bg_blue_50
                )
            else null
        }


        private fun isSelected(tag: String): Boolean = mSelectedTags.contains(tag)

        fun toggleSelection(tag: String) {
            if (isSelected(tag)) {
                mSelectedTags.remove(tag)
            } else {
                mSelectedTags.add(tag)
            }
        }

    }
}