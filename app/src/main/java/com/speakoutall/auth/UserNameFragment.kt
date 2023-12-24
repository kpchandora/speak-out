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
import com.speakoutall.databinding.FragmentUserNameBinding
import com.speakoutall.extensions.*
import com.speakoutall.ui.MainActivity
import com.speakoutall.users.UsersRepository
import com.speakoutall.utils.AppPreference
import timber.log.Timber


class UserNameFragment : Fragment() {

    private val mUserViewModel: UserViewModel by viewModels() {
        UserViewModel(UsersRepository(RetrofitBuilder.apiService, AppPreference)).createFactory()
    }
    private var username = ""
    private val userNameRegex = "^[a-z0-9_]{1,25}\$".toRegex()
    private val safeArgs: UserNameFragmentArgs by navArgs()
    private var binding: FragmentUserNameBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserNameBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        username = safeArgs.username ?: ""

        binding?.run {
            toolbarContainer.toolbarTitle.text = "${safeArgs.type} Username"
            if (safeArgs.type == Type.Edit) {
                setUpToolbar(view)
                fragmentUsernameEt.setText(username)
                fragmentUsernameEt.setSelection(username.length)
                fragmentUsernameEt.setDrawableEnd(R.drawable.ic_check)
                fragmentUsernameNextBtn.text = getString(R.string.update)
            } else {
                setUpWithAppBarConfiguration(view, R.id.userNameFragment)
                requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
                    object : OnBackPressedCallback(true) {
                        override fun handleOnBackPressed() {
                            requireActivity().finish()
                        }

                    })
            }

            fragmentUsernameNextBtn.isEnabled = false
        }

        mUserViewModel.username.observe(viewLifecycleOwner, Observer {
            if (it is Result.Success) {
                binding?.run {
                    if (it.data) {
                        fragmentUsernameEt.setDrawableEnd(R.drawable.ic_check)
                        fragmentUsernameNextBtn.isEnabled = true
                        fragmentUsernameTil.error = null
                    } else {
                        fragmentUsernameTil.error = "Username is already taken"
                    }
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

        binding?.fragmentUsernameEt?.setSmallCaseFilter()

        binding?.fragmentUsernameEt?.doOnTextChanged { text: CharSequence?, start: Int,
                                                       count: Int, after: Int ->

            binding?.fragmentUsernameEt?.removeDrawableEnd()

            text?.let {
                binding?.fragmentUsernameNextBtn?.disable()
                if (userNameRegex.matches(text)) {
                    if (text.length < 3) {
                        binding?.fragmentUsernameTil?.error = "Username is too short"
                    } else {
                        binding?.fragmentUsernameTil?.error = null
                        username = text.toString()
                        mUserViewModel.isUsernamePresent(username)
                    }
                } else {
                    binding?.fragmentUsernameTil?.error = "Please enter a valid username"
                }
            }

        }

        binding?.fragmentUsernameNextBtn?.setOnClickListener {
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
