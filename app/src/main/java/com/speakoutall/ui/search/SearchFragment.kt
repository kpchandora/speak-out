package com.speakoutall.ui.search

import android.graphics.Rect
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ImageView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.speakoutall.api.RetrofitBuilder
import com.speakoutall.auth.UsersItem
import com.speakoutall.common.EventObserver
import com.speakoutall.common.Result
import com.speakoutall.databinding.FragmentSearchBinding
import com.speakoutall.extensions.createFactory
import com.speakoutall.extensions.gone
import com.speakoutall.extensions.isNotNullOrEmpty
import com.speakoutall.extensions.visible
import com.speakoutall.ui.MainActivity
import com.speakoutall.users.UsersRepository
import com.speakoutall.utils.AppPreference
import timber.log.Timber

class SearchFragment : Fragment() {

    private val searchViewModel: SearchViewModel by viewModels() {
        SearchViewModel(UsersRepository(RetrofitBuilder.apiService, AppPreference)).createFactory()
    }
    private val mAdapter = SearchAdapter()
    private var usersCount = 0
    private var _binding: FragmentSearchBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding?.run {
            lottieSearchUserAnimation.speed = 2f
            lottieSearchUserAnimation.gone()
            usersListRv.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(context)
                adapter = mAdapter
            }
            searchView.setOnQueryTextListener(DebouncingQueryTextListener(
                runBefore = {
                    if (it.isNullOrEmpty()) {
                        usersCount = 0
                        mAdapter.updateData(emptyList())
                    }
                    if (lottieSearchEmptyAnimation.isAnimating) {
                        lottieSearchEmptyAnimation.pauseAnimation()
                        lottieSearchEmptyAnimation.gone()
                    }
                    if (it.isNotNullOrEmpty()) {
                        if (usersCount == 0 && !lottieSearchUserAnimation.isAnimating) {
                            lottieSearchUserAnimation.visible()
                            lottieSearchUserAnimation.progress = 0f
                            lottieSearchUserAnimation.playAnimation()
                        }
                    } else {
                        lottieSearchUserAnimation.pauseAnimation()
                        lottieSearchUserAnimation.gone()
                    }
                },
                onDebouncingQueryTextChange = {
                    if (it.isNotNullOrEmpty()) {
                        searchViewModel.searchUsers(it!!.toLowerCase())
                    }
                }
            ))

            searchViewModel.searchUsers.observe(viewLifecycleOwner, EventObserver {
                lottieSearchUserAnimation.pauseAnimation()
                lottieSearchUserAnimation.gone()
                if (it is Result.Success) {
                    usersCount = it.data.size
                    if (it.data.isEmpty()) {
                        lottieSearchEmptyAnimation.visible()
                        lottieSearchEmptyAnimation.playAnimation()
                        mAdapter.updateData(emptyList())
                    } else {
                        mAdapter.updateData(it.data)
                    }
                } else {
                    usersCount = 0
                }
            })
        }

        mAdapter.mListener = mUserClickListener
    }

    override fun onStart() {
        super.onStart()
        Timber.d("onStart")
        _binding?.fragmentSearchRoot?.viewTreeObserver?.addOnGlobalLayoutListener(
            mKeyboardVisibilityListener
        )
    }

    override fun onStop() {
        super.onStop()
        Timber.d("onStop")
        _binding?.fragmentSearchRoot?.viewTreeObserver?.removeOnGlobalLayoutListener(
            mKeyboardVisibilityListener
        )
    }

    private fun navigateToProfile(
        userMiniDetails: UsersItem,
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
        override fun onUserClick(userMiniDetails: UsersItem, profileImageView: ImageView) {
            navigateToProfile(userMiniDetails, profileImageView)
        }
    }
}
