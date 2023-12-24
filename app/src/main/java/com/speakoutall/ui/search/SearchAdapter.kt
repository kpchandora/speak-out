package com.speakoutall.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.speakoutall.R
import com.speakoutall.auth.UsersItem
import com.speakoutall.databinding.ItemUsersListBinding
import com.speakoutall.extensions.gone
import com.speakoutall.extensions.loadImage

class SearchAdapter : RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {

    private val usersList = ArrayList<UsersItem>()
    var mListener: OnSearchUserClickListener? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val binding =
            ItemUsersListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchViewHolder(binding)
    }

    override fun getItemCount() = usersList.size

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.apply {
            mListener = this@SearchAdapter.mListener
            bind(usersList[position])
        }
    }

    fun updateData(list: List<UsersItem>) {
        usersList.clear()
        usersList.addAll(list)
        notifyDataSetChanged()
    }

    class SearchViewHolder(val binding: ItemUsersListBinding) : RecyclerView.ViewHolder(binding.root) {
        var mListener: OnSearchUserClickListener? = null

        init {
            binding.root.setOnClickListener {
                mListener?.onUserClick(
                    binding.root.tag as UsersItem,
                    binding.itemUsersListProfileIv
                )
            }
        }

        fun bind(user: UsersItem) {
            binding.root.run {
                tag = user
                binding.itemUsersListProfileIv.transitionName = user.userId
                binding.itemUsersListProfileIv.loadImage(
                    user.photoUrl,
                    placeholder = R.drawable.ic_account_circle_grey,
                    makeRound = true
                )

                binding.cvFollow.gone()
                binding.itemUsersListUsernameTv.text = user.username
                binding.itemUsersListNameTv.text = user.name
            }
        }
    }

}