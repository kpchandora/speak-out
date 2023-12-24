package com.speakoutall.users

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.speakoutall.R
import com.speakoutall.auth.UsersItem
import com.speakoutall.databinding.ItemUsersListBinding
import com.speakoutall.extensions.gone
import com.speakoutall.extensions.loadImage
import com.speakoutall.extensions.visible
import com.speakoutall.utils.AppPreference

class UsersListAdapter(private val usersList: ArrayList<UsersItem>) :
    RecyclerView.Adapter<UsersListAdapter.UsersListViewHolder>() {

    var mListener: OnUserClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersListViewHolder {
        val binding =
            ItemUsersListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UsersListViewHolder(binding).also {
            it.mListener = mListener
        }
    }

    override fun getItemCount() = usersList.size

    override fun onBindViewHolder(holder: UsersListViewHolder, position: Int) {
        holder.bind(usersList[position])
    }

    override fun onBindViewHolder(
        holder: UsersListViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty()) {
            (payloads[0] as UsersItem).let {
                holder.bind(it)
                return
            }
        }

        super.onBindViewHolder(holder, position, payloads)
    }

    fun showFollowing(userId: String) {
        usersList.forEachIndexed { index, userMiniDetails ->
            if (userMiniDetails.userId == userId) {
                userMiniDetails.isFollowedBySelf = true
                notifyItemChanged(index, userMiniDetails)
            }
        }
    }

    fun showFollow(userId: String) {
        usersList.forEachIndexed { index, userMiniDetails ->
            if (userMiniDetails.userId == userId) {
                userMiniDetails.isFollowedBySelf = false
                notifyItemChanged(index, userMiniDetails)
            }
        }
    }

    class UsersListViewHolder(val binding: ItemUsersListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var mListener: OnUserClickListener? = null

        init {
            binding.cvFollow.setOnClickListener {
                val user = binding.root.tag as UsersItem
                if (user.isFollowedBySelf!!) {
                    mListener?.onUnFollowClick(user)
                } else {
                    user.isFollowedBySelf = true
                    showFollowing()
                    mListener?.onFollowClick(user)
                }
            }
            binding.flProfile.setOnClickListener {
                val user = binding.root.tag as UsersItem
                mListener?.onUserClick(user, binding.itemUsersListProfileIv)
            }

            binding.llDetails.setOnClickListener {
                val user = binding.root.tag as UsersItem
                mListener?.onUserClick(user, binding.itemUsersListProfileIv)
            }

        }

        fun bind(user: UsersItem) {
            binding.root.apply {
                tag = user
                binding.itemUsersListProfileIv.transitionName = user.userId
                binding.itemUsersListProfileIv.loadImage(
                    user.photoUrl,
                    placeholder = R.drawable.ic_account_circle_grey,
                    makeRound = true
                )
                binding.itemUsersListUsernameTv.text = user.username
                binding.itemUsersListNameTv.text = user.name

                if (user.isFollowedBySelf == null || user.userId == AppPreference.getUserId()) {
                    binding.cvFollow.gone()
                } else {
                    binding.cvFollow.visible()
                    if (user.isFollowedBySelf!!) {
                        showFollowing()
                    } else {
                        showFollow()
                    }
                }
            }
        }

        private fun showFollow() {
            binding.tvFollow.text = binding.root.context.getString(R.string.follow)
            binding.tvFollow.background =
                ContextCompat.getDrawable(binding.root.context, R.drawable.dr_follow_bg)
            binding.tvFollow.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    R.color.colorFollowBgText
                )
            )
        }

        private fun showFollowing() {
            binding.tvFollow.text = binding.root.context.getString(R.string.following)
            binding.tvFollow.background =
                ContextCompat.getDrawable(binding.root.context, R.drawable.dr_unfollow_bg)
            binding.tvFollow.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    R.color.colorFollowingBgText
                )
            )
        }

    }
}