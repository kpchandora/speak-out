package com.speakoutall.posts.tags

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.speakoutall.R
import kotlinx.android.synthetic.main.item_selected_tags_layout.view.*
import java.util.*

class SelectedTagsRecyclerViewAdapter() :
    RecyclerView.Adapter<SelectedTagsRecyclerViewAdapter.SelectedTagsHolder>() {

    private val selectedTagsList = mutableListOf<Tag>()
    private var mListener: OnTagClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedTagsHolder {
        val view =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_selected_tags_layout, parent, false)
        return SelectedTagsHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    override fun getItemId(position: Int) = selectedTagsList[position].id

    override fun getItemCount() = selectedTagsList.size

    override fun onBindViewHolder(holder: SelectedTagsHolder, position: Int) {
        holder.apply {
            bind(selectedTagsList[adapterPosition])
            view.item_selected_tag_chip.setOnCloseIconClickListener {
                mListener?.onTagClick(selectedTagsList[adapterPosition])
                selectedTagsList.remove(selectedTagsList[adapterPosition])
                notifyItemRemoved(adapterPosition)
            }
        }
    }

    fun setListener(listener: OnTagClickListener) {
        mListener = listener
    }

    fun removeTag(tag: Tag) {
        val index = selectedTagsList.indexOf(tag)
        selectedTagsList.remove(tag)
        if (index != -1) {
            notifyItemRemoved(index)
        }
    }

    fun addTag(tag: Tag) {
        selectedTagsList.add(0, tag)
        notifyItemInserted(0)
//        notifyItemMoved(1, selectedTagsList.size - 1)
    }

    inner class SelectedTagsHolder(val view: View) : RecyclerView.ViewHolder(view) {
        fun bind(tag: Tag) {
            view.item_selected_tag_chip.text = tag.tag.toLowerCase(Locale.getDefault())
        }
    }
}