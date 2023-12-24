package com.speakoutall.auth

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.iid.FirebaseInstanceId

import com.speakoutall.R
import com.speakoutall.api.RetrofitBuilder
import com.speakoutall.common.Result
import com.speakoutall.databinding.FragmentSignInBinding
import com.speakoutall.extensions.*
import com.speakoutall.users.UsersRepository
import com.speakoutall.utils.AppPreference
import com.speakoutall.utils.AppUpdateManager
import com.speakoutall.utils.FirebaseUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SignInFragment : Fragment() {

    companion object {
        private const val RC_SIGN_IN = 101
    }

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private val mUserViewModel: UserViewModel by viewModels {
        UserViewModel(UsersRepository(RetrofitBuilder.apiService, AppPreference)).createFactory()
    }
    private val userRepository by lazy {
        UsersRepository(RetrofitBuilder.apiService, AppPreference)
    }
    private lateinit var mPreference: AppPreference
    private var binding: FragmentSignInBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpWithAppBarConfiguration(view, R.id.signInFragment)
        binding?.toolbarContainer?.toolbarTitle?.text = "SignIn"

        mPreference = AppPreference

        mUserViewModel.saveUserDetails.observe(requireActivity(), Observer {
            if (it is Result.Success) {
                binding?.signInProgress?.gone()
                mPreference.setLoggedIn()
                mPreference.saveUserDetails(it.data)
                val action = SignInFragmentDirections.actionSignInFragmentToNavigationHome()
                findNavController().navigate(action)
            } else {
                hideProgress()
                showShortToast("Failed")
                FirebaseUtils.signOut(requireActivity())
            }
        })

        mUserViewModel.userDetails.observe(requireActivity(), Observer {
            if (it is Result.Success) {
                AppUpdateManager(requireActivity()).checkAndUpdate()
                it.data.apply {
                    binding?.signInProgress?.gone()
                    mPreference.setLoggedIn()
                    mPreference.saveUserDetails(this)
                    if (username.isNotNullOrEmpty() && username?.equals(userId) != true) {
                        mPreference.setUsernameProcessComplete()
                        val action = SignInFragmentDirections.actionSignInFragmentToNavigationHome()
                        findNavController().navigate(action)
                    } else {
                        val action =
                            SignInFragmentDirections.actionSignInFragmentToUserNameFragment(
                                type = Type.Create,
                                username = null
                            )
                        findNavController().navigate(action)
                    }
                }
            } else {
                FirebaseUtils.signOut(requireActivity()).also {
                    hideProgress()
                    showShortToast("Something went wrong, please try again")
                }
            }
        })

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()


        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        auth = FirebaseAuth.getInstance()

        binding?.signInButton?.setOnClickListener {
            showProgress()
            startActivityForResult(
                googleSignInClient.signInIntent,
                RC_SIGN_IN
            )
        }
    }

    private fun showProgress() {
        binding?.signInProgress?.visible()
        binding?.signInButton?.gone()
    }

    private fun hideProgress() {
        binding?.signInProgress?.gone()
        binding?.signInButton?.visible()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                FirebaseCrashlytics.getInstance().recordException(e)
                e.printStackTrace()
                hideProgress()
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task: Task<AuthResult> ->
                if (task.isSuccessful) {
                    task.result?.user?.apply {
                        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener {
                            if (it.isSuccessful) {
                                GlobalScope.launch {
                                    userRepository.updateFcmToken(it.result?.token ?: "")
                                }
                            }
                            if (task.result!!.additionalUserInfo?.isNewUser == true) {
                                val model = UserDetails(
                                    userId = uid,
                                    name = displayName,
                                    photoUrl = photoUrl?.toString(),
                                    phoneNumber = phoneNumber,
                                    email = email,
                                    creationTimeStamp = metadata?.creationTimestamp,
                                    lastSignInTimestamp = metadata?.lastSignInTimestamp
                                )
                                mUserViewModel.saveUserDetails(model)
                            } else {
                                mUserViewModel.getUserData(uid = uid)
                            }
                        }

                    } ?: kotlin.run {
                        showShortToast("Failed")
                        hideProgress()
                        FirebaseUtils.signOut(requireActivity())
                    }
                } else {
                    showShortToast("Failed")
                    hideProgress()
                }
            }
    }

}
