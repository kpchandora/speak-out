package com.speakout.users

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager

import com.speakout.R
import kotlinx.android.synthetic.main.users_list_fragment.*

class UsersListFragment : Fragment() {

    companion object {
        const val TAG = "UsersListFragment"

        fun newInstance() = UsersListFragment()

    }

    private val usersListViewModel: UsersListViewModel by viewModels()
    private val mAdapter = UsersListAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        usersListViewModel.getPosts("")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.users_list_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        users_list_rv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }
        usersListViewModel.usersList.observe(viewLifecycleOwner, Observer {
            mAdapter.updateData(it)
        })

    }

}
