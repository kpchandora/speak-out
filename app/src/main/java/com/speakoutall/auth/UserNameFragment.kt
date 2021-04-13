package com.speakoutall.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.speakoutall.R
import com.speakoutall.api.RetrofitBuilder
import com.speakoutall.common.EventObserver
import com.speakoutall.common.Result
import com.speakoutall.extensions.*
import com.speakoutall.ui.MainActivity
import com.speakoutall.users.UsersRepository
import com.speakoutall.utils.AppPreference
import kotlinx.android.synthetic.main.fragment_user_name.*
import kotlinx.android.synthetic.main.layout_toolbar.view.*
import timber.log.Timber


class UserNameFragment : Fragment() {

    private val mUserViewModel: UserViewModel by viewModels() {
        UserViewModel(UsersRepository(RetrofitBuilder.apiService, AppPreference)).createFactory()
    }
    private var username = ""
    private val userNameRegex = "^[a-z0-9_]{1,25}\$".toRegex()
    private val safeArgs: UserNameFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_name, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        username = safeArgs.username ?: ""

        if (safeArgs.type == Type.Edit) {
            setUpToolbar(view)?.toolbar_title?.text = "${safeArgs.type} Username"
            fragment_username_et.setText(username)
            fragment_username_et.setSelection(username.length)
            fragment_username_et.setDrawableEnd(R.drawable.ic_check)
            fragment_username_next_btn.text = getString(R.string.update)
        } else {
            setUpWithAppBarConfiguration(view, R.id.userNameFragment)?.toolbar_title?.text =
                "${safeArgs.type} Username"
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        requireActivity().finish()
                    }

                })
        }

        fragment_username_next_btn.isEnabled = false

        mUserViewModel.username.observe(viewLifecycleOwner, Observer {
            if (it is Result.Success) {
                if (it.data) {
                    fragment_username_et.setDrawableEnd(R.drawable.ic_check)
                    fragment_username_next_btn.isEnabled = true
                    fragment_username_til.error = null
                } else {
                    fragment_username_til.error = "Username is already taken"
                }
            } else {
                showShortToast(getString(R.string.error_something_went_wrong))
            }
        })

        mUserViewModel.updateUserDetails.observe(viewLifecycleOwner, EventObserver {
            (requireActivity() as MainActivity).hideProgress()
            if (it is Result.Success) {
                AppPreference.saveUserDetails(UserDetails(username = username))
                if (safeArgs.type == Type.Edit) {
                    findNavController().previousBackStackEntry?.savedStateHandle?.set(
                        "username",
                        username
                    )
                    findNavController().navigateUp()
                } else {
                    Timber.d("Details updated")
                    AppPreference.setUsernameProcessComplete()
                    findNavController().navigate(UserNameFragmentDirections.actionUserNameFragmentToNavigationHome())
                }
            } else {
                showShortToast(getString(R.string.error_something_went_wrong))
            }
        })

        fragment_username_et.setSmallCaseFilter()

        fragment_username_et.doOnTextChanged { text: CharSequence?, start: Int,
                                               count: Int, after: Int ->

            fragment_username_et.removeDrawableEnd()

            text?.let {
                fragment_username_next_btn.disable()
                if (userNameRegex.matches(text)) {
                    if (text.length < 3) {
                        fragment_username_til.error = "Username is too short"
                    } else {
                        fragment_username_til.error = null
                        username = text.toString()
                        mUserViewModel.isUsernamePresent(username)
                    }
                } else {
                    fragment_username_til.error = "Please enter a valid username"
                }
            }

        }

        fragment_username_next_btn.setOnClickListener {
            (requireActivity() as MainActivity).showProgress()
            mUserViewModel.updateUserDetails(
                UsersItem(
                    userId = AppPreference.getUserId(),
                    username = username
                )
            )
        }
    }

}