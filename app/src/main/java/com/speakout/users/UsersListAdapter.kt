package com.speakout.users

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.speakout.R
import com.speakout.auth.UserMiniDetails
import com.speakout.extensions.gone
import com.speakout.extensions.loadImageWithCallback
import com.speakout.extensions.visible
import com.speakout.posts.create.PostData
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


    fun updateData(list: List<UserMiniDetails>) {
        usersList.clear()
        usersList.addAll(list)
        notifyDataSetChanged()
    }

    class UsersListViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        var mListener: OnUserClickListener? = null

        fun bind(user: UserMiniDetails) {
            view.apply {
                item_user_list_bg.gone()
                item_users_list_profile_iv.transitionName = user.userId
                item_users_list_profile_iv.loadImageWithCallback(user.photoUrl ?: "",
                    makeRound = true,
                    onSuccess = {
                        item_user_list_bg.visible()
                    },
                    onFailed = {
                        item_users_list_profile_iv.setImageDrawable(
                            ContextCompat.getDrawable(
                                view.context,
                                R.drawable.ic_account_circle_grey
                            )
                        )
                        item_user_list_bg.visible()
                    })

                item_users_list_username_tv.text = user.username
                item_users_list_name_tv.text = user.name

                setOnClickListener {
                    mListener?.onUserClick(
                        user,
                        item_users_list_profile_iv
                    )
                }

            }
        }
    }
}