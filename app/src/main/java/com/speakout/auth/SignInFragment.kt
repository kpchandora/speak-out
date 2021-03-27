package com.speakout.auth

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
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
import com.google.firebase.iid.FirebaseInstanceId

import com.speakout.R
import com.speakout.api.RetrofitBuilder
import com.speakout.common.Result
import com.speakout.extensions.*
import com.speakout.users.UsersRepository
import com.speakout.utils.AppPreference
import com.speakout.utils.FirebaseUtils
import kotlinx.android.synthetic.main.fragment_sign_in.*
import kotlinx.android.synthetic.main.layout_toolbar.view.*
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sign_in, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpWithAppBarConfiguration(view, R.id.signInFragment)?.toolbar_title?.text = "SignIn"

        mPreference = AppPreference

        mUserViewModel.saveUserDetails.observe(requireActivity(), Observer {
            if (it is Result.Success) {
                sign_in_progress.gone()
                mPreference.setLoggedIn()
                mPreference.saveUserDetails(it.data)
                val action = SignInFragmentDirections.actionSignInFragmentToNavigationHome()
                findNavController().navigate(action)
            } else {
                hideProgress()
                showShortToast("Failed")
                FirebaseUtils.signOut()
            }
        })

        mUserViewModel.userDetails.observe(requireActivity(), Observer {
            if (it is Result.Success) {
                it.data.apply {
                    sign_in_progress.gone()
                    mPreference.setLoggedIn()
                    mPreference.saveUserDetails(this)
                    if (username.isNotNullOrEmpty() && username?.equals(userId) != true) {
                        mPreference.setUsernameProcessComplete()
                        val action = SignInFragmentDirections.actionSignInFragmentToNavigationHome()
                        findNavController().navigate(action)
                    } else {
                        val action =
                            SignInFragmentDirections.actionSignInFragmentToUserNameFragment(
                                Type.Create,
                                null
                            )
                        findNavController().navigate(action)
                    }
                }
            } else {
                FirebaseUtils.signOut().also {
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

        signInButton.setOnClickListener {
            showProgress()
            startActivityForResult(
                googleSignInClient.signInIntent,
                RC_SIGN_IN
            )
        }
    }

    private fun showProgress() {
        sign_in_progress.visible()
        signInButton.gone()
    }

    private fun hideProgress() {
        sign_in_progress.gone()
        signInButton.visible()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
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
                        FirebaseUtils.signOut()
                    }
                } else {
                    showShortToast("Failed")
                    hideProgress()
                }
            }
    }

}
