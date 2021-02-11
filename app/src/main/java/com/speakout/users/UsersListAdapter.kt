package com.speakout.users

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.speakout.R
import com.speakout.auth.UserMiniDetails
import com.speakout.extensions.gone
import com.speakout.extensions.loadImage
import com.speakout.extensions.visible
import com.speakout.utils.AppPreference
import kotlinx.android.synthetic.main.item_users_list.view.*

class UsersListAdapter : RecyclerView.Adapter<UsersListAdapter.UsersListViewHolder>() {

    private val usersList = ArrayList<UserMiniDetails>()
    var mListener: OnUserClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersListViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_users_list, parent, false)
        return UsersListViewHolder(view)
    }

    override fun getItemCount() = usersList.size

    override fun onBindViewHolder(holder: UsersListViewHolder, position: Int) {
        holder.apply {
            mListener = this@UsersListAdapter.mListener
            bind(usersList[position])
        }
    }

    override fun onBindViewHolder(
        holder: UsersListViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty()) {
            (payloads[0] as UserMiniDetails).let {
                holder.bind(it)
                return
            }
        }

        super.onBindViewHolder(holder, position, payloads)
    }

    fun addData(list: List<UserMiniDetails>) {
        usersList.clear()
        usersList.addAll(list)
        notifyDataSetChanged()
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

        fun bind(user: UserMiniDetails) {
            view.apply {
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
                    cv_follow.setOnClickListener {
                        if (user.isFollowedBySelf!!) {
                            mListener?.onUnFollowClick(user)
                        } else {
                            user.isFollowedBySelf = true
                            showFollowing()
                            mListener?.onFollowClick(user)
                        }
                    }
                }

                fl_profile.setOnClickListener {
                    mListener?.onUserClick(user, item_users_list_profile_iv)
                }

                ll_details.setOnClickListener {
                    mListener?.onUserClick(user, item_users_list_profile_iv)
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