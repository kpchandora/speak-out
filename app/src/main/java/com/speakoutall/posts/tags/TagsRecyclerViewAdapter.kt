package com.speakoutall.posts.tags

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.speakoutall.R
import com.speakoutall.extensions.gone
import com.speakoutall.extensions.visible
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
                val item = mTagsList[adapterPosition]
                if (item.used ?: -1 > -1) { //used is not null
                    toggleSelection(item.tag)
                    notifyItemChanged(adapterPosition)
                    mListener?.onTagClick(item)
                } else { //used is null
                    item.uploading = true
                    notifyItemChanged(adapterPosition)
                    mListener?.onAddNewTag(item)
                }
            }
        }
    }

    fun tagAdded(tag: Tag) {
        mTagsList.forEachIndexed { index: Int, t: Tag ->
            if (tag.id == t.id) {
                t.used = 0
                notifyItemChanged(index)
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
        @SuppressLint("SetTextI18n")
        fun bind(tag: Tag) {
            view.item_tag_name_tv.text = tag.tag

            tag.used?.let {
                view.item_tag_add_layout.gone()
                if (it > 0) {
                    view.item_tag_posts_count_tv.visible()
                    view.item_tag_posts_count_tv.text =
                        "$it ${view.context.resources.getQuantityString(
                            R.plurals.number_of_posts,
                            it.toInt()
                        ).toLowerCase()}"
                } else {
                    view.item_tag_posts_count_tv.gone()
                }
            } ?: kotlin.run {
                view.item_tag_add_layout.visible()
                view.item_tag_posts_count_tv.gone()
                if (tag.uploading == true) {
                    view.item_add_tag_progress.visible()
                    view.item_add_tag_btn.gone()
                } else {
                    view.item_add_tag_progress.gone()
                    view.item_add_tag_btn.visible()
                }
            }

            view.background = if (isSelected(tag.tag))
                ContextCompat.getDrawable(
                    view.context,
                    R.drawable.dr_round_bg_blue50_8dp
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