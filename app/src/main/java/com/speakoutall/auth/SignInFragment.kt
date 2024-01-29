package com.speakoutall.auth

import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthSettings
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.iid.FirebaseInstanceId
import com.speakoutall.R
import com.speakoutall.api.RetrofitBuilder
import com.speakoutall.common.Result
import com.speakoutall.databinding.FragmentSignInBinding
import com.speakoutall.extensions.*
import com.speakoutall.ui.MainActivity
import com.speakoutall.users.UsersRepository
import com.speakoutall.utils.AppPreference
import com.speakoutall.utils.AppUpdateManager
import com.speakoutall.utils.FirebaseUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.concurrent.TimeUnit

class SignInFragment : Fragment() {

    companion object {
        private const val RC_SIGN_IN = 101
        private const val COUNTRY_CODE = "+91"
        private const val TAG_SEND_OTP = 1
        private const val TAG_VERIFY_OTP = 2
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
    private var verificationId: String = ""

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
        binding?.btnConfirm?.tag = TAG_SEND_OTP

        mUserViewModel.userDetails.observe(requireActivity(), Observer {
            if (it is Result.Success) {
                AppUpdateManager(requireActivity()).checkAndUpdate()
                it.data.apply {
                    binding?.signInProgress?.gone()
                    mPreference.setLoggedIn()
                    mPreference.saveUserDetails(this)
                    (activity as? MainActivity)?.updateToken()
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

        binding?.run {
            etNumber.doOnTextChanged { text, start, count, after ->
                (etNumber.filters.firstOrNull() as? InputFilter.LengthFilter)?.let {
                    btnConfirm.isEnabled = it.max == (text?.length ?: 0)
                }
            }
        }

        binding?.run {
            btnConfirm.setOnClickListener { view ->
                etNumber.text?.toString()?.let {
                    if (view.tag == TAG_SEND_OTP) {
                        sendOtp(it)
                        btnConfirm.text = getString(R.string.sending_otp)
                        btnConfirm.isEnabled = false
                    }
                    if (view.tag == TAG_VERIFY_OTP) {
                        showProgress()
                        verifyOtp(it)
                    }
                }
            }
        }
    }

    private fun showProgress() {
        binding?.signInProgress?.visible()
//        binding?.signInButton?.gone()
    }

    private fun hideProgress() {
        binding?.signInProgress?.gone()
//        binding?.signInButton?.visible()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
                account?.let {
                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    signInWithPhoneAuthCredential(credential)
                }
            } catch (e: ApiException) {
                FirebaseCrashlytics.getInstance().recordException(e)
                e.printStackTrace()
                hideProgress()
            }
        }
    }

    private fun signInWithPhoneAuthCredential(credential: AuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task: Task<AuthResult> ->
                if (task.isSuccessful) {
                    task.result?.user?.apply {
//                            if (it.isSuccessful) {
//                                GlobalScope.launch {
//                                    userRepository.updateFcmToken(it.result?.token ?: "")
//                                }
//                            }
                        val isNewUser = task.result!!.additionalUserInfo?.isNewUser == true
                        val model = UserDetails(
                            userId = uid,
                            name = displayName,
                            photoUrl = photoUrl?.toString(),
                            phoneNumber = phoneNumber,
                            email = email.orEmpty(),
                            creationTimeStamp = metadata?.creationTimestamp,
                            lastSignInTimestamp = metadata?.lastSignInTimestamp
                        )
                        mUserViewModel.saveUserDetails(model, isNewUser)
                    } ?: kotlin.run {
                        Timber.e(task.exception)
                        showShortToast("Failed")
                        hideProgress()
                        FirebaseUtils.signOut(requireActivity())
                    }
                } else {
                    Timber.e("Unsuccessful: ${task.exception}")
                    showShortToast("Failed")
                    hideProgress()
                }
            }
    }

    private fun sendOtp(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("$COUNTRY_CODE$phoneNumber")
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(requireActivity())
            .setCallbacks(callback)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun verifyOtp(otp: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId, otp)
        signInWithPhoneAuthCredential(credential)
    }

    private val callback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(exception: FirebaseException) {
            showShortToast(getString(R.string.error_something_went_wrong))
            Timber.e(exception)
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken,
        ) {
            this@SignInFragment.verificationId = verificationId
            binding?.run {
                etNumber.text?.clear()
                etNumber.hint = getString(R.string.otp)
                val filter = InputFilter.LengthFilter(6)
                etNumber.filters = arrayOf(filter)
                btnConfirm.text = getString(R.string.confirm)
                btnConfirm.tag = TAG_VERIFY_OTP
            }
        }
    }

}
