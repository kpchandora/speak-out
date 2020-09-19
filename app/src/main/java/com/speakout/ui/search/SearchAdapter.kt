package com.speakout.ui.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.speakout.R
import com.speakout.auth.UserMiniDetails
import com.speakout.extensions.gone
import com.speakout.extensions.loadImage
import com.speakout.extensions.loadImageWithCallback
import com.speakout.extensions.visible
import com.speakout.users.OnUserClickListener
import kotlinx.android.synthetic.main.item_users_list.view.*

class SearchAdapter : RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {

    private val usersList = ArrayList<UserMiniDetails>()
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

    fun updateData(list: List<UserMiniDetails>) {
        usersList.clear()
        usersList.addAll(list)
        notifyDataSetChanged()
    }

    class SearchViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        var mListener: OnSearchUserClickListener? = null
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