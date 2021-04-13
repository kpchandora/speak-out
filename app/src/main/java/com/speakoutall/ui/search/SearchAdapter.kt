package com.speakoutall.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.speakoutall.R
import com.speakoutall.auth.UsersItem
import com.speakoutall.extensions.gone
import com.speakoutall.extensions.loadImage
import kotlinx.android.synthetic.main.item_users_list.view.*

class SearchAdapter : RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {

    private val usersList = ArrayList<UsersItem>()
    var mListener: OnSearchUserClickListener? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_users_list, parent, false)
        return SearchViewHolder(view)
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

    class SearchViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        var mListener: OnSearchUserClickListener? = null

        init {
            view.setOnClickListener {
                mListener?.onUserClick(
                    view.tag as UsersItem,
                    view.item_users_list_profile_iv
                )
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

                cv_follow.gone()
                item_users_list_username_tv.text = user.username
                item_users_list_name_tv.text = user.name
            }
        }
    }

}