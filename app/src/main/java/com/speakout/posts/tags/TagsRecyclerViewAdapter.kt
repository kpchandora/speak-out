package com.speakout.posts.tags

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.speakout.R
import kotlinx.android.synthetic.main.item_tag_layout.view.*

class TagsRecyclerViewAdapter : RecyclerView.Adapter<TagsRecyclerViewAdapter.TagsHolder>() {

    private val mTagsList = ArrayList<Tag>()
    private val mSelectedTags = hashSetOf<String>()
    private var mListener: OnTagClickListener? = null

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
                toggleSelection(mTagsList[position].tag)
                mListener?.onTagClick(mTagsList[position])
                notifyItemChanged(position)
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

//            view.item_tag_icon_tv.text =
//                if (isSelected(tag.tag)) view.context.getString(R.string.tick_text)
//                else view.context.getString(R.string.hash_text)

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