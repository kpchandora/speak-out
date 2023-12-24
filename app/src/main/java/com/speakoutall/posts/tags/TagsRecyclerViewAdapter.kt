package com.speakoutall.posts.tags

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.speakoutall.R
import com.speakoutall.databinding.ItemTagLayoutBinding
import com.speakoutall.extensions.gone
import com.speakoutall.extensions.visible
import java.util.concurrent.atomic.AtomicBoolean

class TagsRecyclerViewAdapter : RecyclerView.Adapter<TagsRecyclerViewAdapter.TagsHolder>() {

    private val mTagsList = ArrayList<Tag>()
    private val mSelectedTags = hashSetOf<String>()
    private var mListener: OnTagClickListener? = null
    val isLoading = AtomicBoolean(false)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TagsHolder {
        val binding =
            ItemTagLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TagsHolder(binding)
    }

    override fun getItemCount() = mTagsList.size

    override fun getItemId(position: Int): Long {
        return mTagsList[position].id
    }

    override fun onBindViewHolder(holder: TagsHolder, position: Int) {
        holder.apply {
            bind(mTagsList[position])
            binding.itemTagsMainLayout.setOnClickListener {
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

    inner class TagsHolder(val binding: ItemTagLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(tag: Tag) {
            binding.itemTagNameTv.text = tag.tag

            tag.used?.let {
                binding.itemTagAddLayout.gone()
                if (it > 0) {
                    binding.itemTagPostsCountTv.visible()
                    binding.itemTagPostsCountTv.text =
                        "$it ${
                            binding.root.context.resources.getQuantityString(
                                R.plurals.number_of_posts,
                                it.toInt()
                            ).lowercase()
                        }"
                } else {
                    binding.itemTagPostsCountTv.gone()
                }
            } ?: kotlin.run {
                binding.itemTagAddLayout.visible()
                binding.itemTagPostsCountTv.gone()
                if (tag.uploading == true) {
                    binding.itemAddTagProgress.visible()
                    binding.itemAddTagBtn.gone()
                } else {
                    binding.itemAddTagProgress.gone()
                    binding.itemAddTagBtn.visible()
                }
            }

            binding.root.background = if (isSelected(tag.tag))
                ContextCompat.getDrawable(
                    binding.root.context,
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