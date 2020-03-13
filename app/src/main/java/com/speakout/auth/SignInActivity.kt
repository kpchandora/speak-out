package com.speakout.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.speakout.R
import com.speakout.extensions.*
import com.speakout.ui.MainActivity
import com.speakout.utils.FirebaseUtils
import com.speakout.utils.AppPreference
import kotlinx.android.synthetic.main.activity_sign_in.*

class SignInActivity : AppCompatActivity() {


    companion object {
        private const val RC_SIGN_IN = 101
    }

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private val mUserViewModel: UserViewModel by viewModels()
    private lateinit var mPreference: AppPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        mPreference = AppPreference
        mUserViewModel.saveUserDetailsObserver.observe(this, Observer {
            if (it) {
                openActivity(UserNameActivity::class.java)
                hideProgress()
                finish()
            } else {
                mPreference.clearUserDetails()
                showShortToast("Failed")
                hideProgress()
                FirebaseUtils.signOut()
            }
        })

        mUserViewModel.getUserDataObserver.observe(this, Observer {
            it?.apply {
                mPreference.saveUserDetails(this)
                if (username.isNotNullOrEmpty()) {
                    openActivity(MainActivity::class.java)
                } else {
                    openActivity(UserNameActivity::class.java)
                }
                hideProgress()
                finish()
            } ?: FirebaseUtils.signOut().also {
                hideProgress()
                showShortToast("Something went wrong, please try again")
            }
        })

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.google_sign_in_key))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
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

    override fun onStart() {
        super.onStart()
        FirebaseUtils.currentUser()?.let {
            openActivity(MainActivity::class.java)
            finish()
        }
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
                            mPreference.saveUserDetails(model)
                            mUserViewModel.saveUserDetails(model)
                        } else {
                            mUserViewModel.getUserData(uid = uid)
                        }
                    }
                } else {
                    showShortToast("Failed")
                    hideProgress()
                }
            }
    }

}
