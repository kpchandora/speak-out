package com.speakout.users

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.speakout.R
import com.speakout.auth.UsersItem
import com.speakout.extensions.gone
import com.speakout.extensions.loadImage
import com.speakout.extensions.visible
import com.speakout.utils.AppPreference
import kotlinx.android.synthetic.main.item_users_list.view.*

class UsersListAdapter(private val usersList: ArrayList<UsersItem>) :
    RecyclerView.Adapter<UsersListAdapter.UsersListViewHolder>() {

    var mListener: OnUserClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersListViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_users_list, parent, false)
        return UsersListViewHolder(view).also {
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

    class UsersListViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        var mListener: OnUserClickListener? = null

        init {
            view.cv_follow.setOnClickListener {
                val user = view.tag as UsersItem
                if (user.isFollowedBySelf!!) {
                    mListener?.onUnFollowClick(user)
                } else {
                    user.isFollowedBySelf = true
                    showFollowing()
                    mListener?.onFollowClick(user)
                }
            }
            view.fl_profile.setOnClickListener {
                val user = view.tag as UsersItem
                mListener?.onUserClick(user, view.item_users_list_profile_iv)
            }

            view.ll_details.setOnClickListener {
                val user = view.tag as UsersItem
                mListener?.onUserClick(user, view.item_users_list_profile_iv)
            }

        }

        fun bind(user: UsersItem) {
            view.apply {
                tag = user
                item_users_list_profile_iv.transitionName = user.userId
                item_users_list_profile_iv.loadImage(
                    user.photoUrl,
                    placeholder = R.drawable.ic_account_circle_grey,
                    makeRound = true
                )
                item_users_list_username_tv.text = user.username
                item_users_list_name_tv.text = user.name

                if (user.isFollowedBySelf == null || user.userId == AppPreference.getUserId()) {
                    cv_follow.gone()
                } else {
                    cv_follow.visible()
                    if (user.isFollowedBySelf!!) {
                        showFollowing()
                    } else {
                        showFollow()
                    }
                }
            }
        }

        private fun showFollow() {
            view.tv_follow.text = view.context.getString(R.string.follow)
            view.tv_follow.background =
                ContextCompat.getDrawable(view.context, R.drawable.dr_follow_bg)
            view.tv_follow.setTextColor(ContextCompat.getColor(view.context, R.color.white))
        }

        private fun showFollowing() {
            view.tv_follow.text = view.context.getString(R.string.following)
            view.tv_follow.background =
                ContextCompat.getDrawable(view.context, R.drawable.dr_unfollow_bg)
            view.tv_follow.setTextColor(ContextCompat.getColor(view.context, R.color.black))
        }

    }
}