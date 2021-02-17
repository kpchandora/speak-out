package com.speakout.ui.search

import android.graphics.Rect
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ImageView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager

import com.speakout.R
import com.speakout.api.RetrofitBuilder
import com.speakout.auth.UserMiniDetails
import com.speakout.common.EventObserver
import com.speakout.common.Result
import com.speakout.extensions.createFactory
import com.speakout.extensions.gone
import com.speakout.extensions.isNotNullOrEmpty
import com.speakout.extensions.visible
import com.speakout.ui.MainActivity
import com.speakout.users.UsersRepository
import com.speakout.utils.AppPreference
import kotlinx.android.synthetic.main.fragment_search.*
import timber.log.Timber


class SearchFragment : Fragment() {

    private val searchViewModel: SearchViewModel by viewModels() {
        SearchViewModel(UsersRepository(RetrofitBuilder.apiService, AppPreference)).createFactory()
    }
    private val mAdapter = SearchAdapter()
    private var usersCount = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lottie_search_user_animation.speed = 2f
        lottie_search_user_animation.gone()

        mAdapter.mListener = mUserClickListener
        users_list_rv.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = mAdapter
        }

        search_view.setOnQueryTextListener(DebouncingQueryTextListener(
            runBefore = {
                if (it.isNullOrEmpty()) {
                    usersCount = 0
                    mAdapter.updateData(emptyList())
                }
                if (lottie_search_empty_animation.isAnimating) {
                    lottie_search_empty_animation.pauseAnimation()
                    lottie_search_empty_animation.gone()
                }
                if (it.isNotNullOrEmpty()) {
                    if (usersCount == 0 && !lottie_search_user_animation.isAnimating) {
                        lottie_search_user_animation.visible()
                        lottie_search_user_animation.progress = 0f
                        lottie_search_user_animation.playAnimation()
                    }
                } else {
                    lottie_search_user_animation.pauseAnimation()
                    lottie_search_user_animation.gone()
                }
            },
            onDebouncingQueryTextChange = {
                if (it.isNotNullOrEmpty()) {
                    searchViewModel.searchUsers(it!!)
                }
            }
        ))

        searchViewModel.searchUsers.observe(viewLifecycleOwner, EventObserver {
            lottie_search_user_animation.pauseAnimation()
            lottie_search_user_animation.gone()
            if (it is Result.Success) {
                usersCount = it.data.size
                if (it.data.isEmpty()) {
                    lottie_search_empty_animation.visible()
                    lottie_search_empty_animation.playAnimation()
                    mAdapter.updateData(emptyList())
                } else {
                    mAdapter.updateData(it.data)
                }
            } else {
                usersCount = 0
            }
        })
    }

    override fun onStart() {
        super.onStart()
        Timber.d("onStart")
        fragment_search_root.viewTreeObserver.addOnGlobalLayoutListener(mKeyboardVisibilityListener)
    }

    override fun onStop() {
        super.onStop()
        Timber.d("onStop")
        fragment_search_root.viewTreeObserver.removeOnGlobalLayoutListener(
            mKeyboardVisibilityListener
        )
    }

    private fun navigateToProfile(
        userMiniDetails: UserMiniDetails,
        profileImageView: ImageView
    ) {
        val action = SearchFragmentDirections.actionSearchFragmentToNavigationProfile(
            userId = userMiniDetails.userId,
            username = userMiniDetails.username,
            transitionTag = userMiniDetails.name,
            profileUrl = userMiniDetails.photoUrl
        )

        val extras = FragmentNavigatorExtras(
            profileImageView to (userMiniDetails.name!!)
        )
        findNavController().navigate(action, extras)
    }

    private var mKeyboardVisible = false

    private val mKeyboardVisibilityListener = ViewTreeObserver.OnGlobalLayoutListener {
        val rect = Rect()
        view?.getWindowVisibleDisplayFrame(rect)
        val screenHeight = view?.rootView?.height ?: 0
        val keypadHeight = screenHeight - rect.bottom

        val isKeyboardVisible = keypadHeight > screenHeight * 0.15

        if (mKeyboardVisible != isKeyboardVisible) {
            Timber.d("IsKeyboardVisible: $isKeyboardVisible")
            if (isKeyboardVisible) {
                (activity as? MainActivity)?.navAnimGone()
            } else {
                (activity as? MainActivity)?.navAnimVisible()
            }
        }

        mKeyboardVisible = isKeyboardVisible

    }

    private val mUserClickListener = object : OnSearchUserClickListener {
        override fun onUserClick(userMiniDetails: UserMiniDetails, profileImageView: ImageView) {
            navigateToProfile(userMiniDetails, profileImageView)
        }
    }
}
