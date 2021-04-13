package com.speakout.ui

import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.FirebaseInstanceId
import com.speakout.BuildConfig
import com.speakout.MobileNavigationDirections
import com.speakout.R
import com.speakout.api.RetrofitBuilder
import com.speakout.users.UsersRepository
import com.speakout.utils.AppPreference
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.Exception

class MainActivity : BaseActivity(), NavBadgeListener {

    companion object {
        private const val DAYS_FOR_FLEXIBLE_UPDATE = 5
        private const val APP_UPDATE_REQUEST_CODE = 10
    }

    private lateinit var navController: NavController
    private var currentFragmentId = 0
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        bottomNavigationView = findViewById(R.id.nav_view)

        navController = findNavController(R.id.nav_host_fragment)

        bottomNavigationView.setupWithNavController(navController)
        bottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> {
                    navController.popBackStack(R.id.navigation_home, false)
                }
                R.id.navigation_new_post -> {
                    navController.navigate(R.id.create_post_navigation)
                    return@setOnNavigationItemSelectedListener false
                }
                R.id.navigation_profile -> {
                    navController.navigate(
                        MobileNavigationDirections.actionGlobalProfileFragment(
                            userId = AppPreference.getUserId(),
                            username = AppPreference.getUserUniqueName(),
                            profileUrl = AppPreference.getPhotoUrl(),
                            transitionTag = null
                        )
                    )
                }
                else -> {
                    navController.navigate(it.itemId)
                }
            }
            true
        }

        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            currentFragmentId = destination.id
            handleNavigationVisibility(currentFragmentId)
        }

        bottomNavigationView.setOnNavigationItemReselectedListener {
            val hostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
            if (hostFragment is NavHostFragment) {
                val currentFragment = hostFragment.childFragmentManager.fragments.first()
                if (currentFragment is BottomIconDoubleClick)
                    currentFragment.doubleClick()
            }
        }
        if (AppPreference.isLoggedIn()) {
            FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
                GlobalScope.launch {
                    UsersRepository(
                        RetrofitBuilder.apiService,
                        AppPreference
                    ).updateFcmToken(it.token)
                }
            }
        }

        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancelAll()

        FirebaseAuth.getInstance().currentUser?.let {
            FirebaseDatabase.getInstance().getReference("app")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                        FirebaseCrashlytics.getInstance().recordException(error.toException())
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        try {
                            checkUpdate(snapshot.child("appVersion").value.toString().toInt())
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                })
        }
    }

    override fun updateBadgeVisibility(isVisible: Boolean) {
        bottomNavigationView.getOrCreateBadge(R.id.notificationFragment).let {
            it.isVisible = isVisible
            it.backgroundColor = ContextCompat.getColor(this@MainActivity, R.color.primary_dark)
        }
    }

    private fun handleNavigationVisibility(id: Int) {
        when (id) {
            R.id.profileEditFragment,
            R.id.userNameFragment,
            R.id.signInFragment,
            R.id.createNewPostFragment,
            R.id.tagsFragment -> navAnimGone()
            else -> navAnimVisible()
        }
    }

    fun navAnimVisible() {
        if (bottomNavigationView.visibility == View.GONE) {
            bottomNavigationView.visibility = View.VISIBLE
            bottomNavigationView.animation = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        }
    }

    fun navAnimGone() {
        if (bottomNavigationView.visibility == View.VISIBLE) {
            bottomNavigationView.visibility = View.GONE
            bottomNavigationView.animation =
                AnimationUtils.loadAnimation(this, R.anim.slide_down)
        }
    }

    override fun onBackPressed() {
        if (currentFragmentId == R.id.navigation_home) {
            finish()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        AppPreference.clearFirebaseToken()
        super.onDestroy()
    }

    private fun checkUpdate(version: Int) {

        if (version > BuildConfig.VERSION_CODE) {
            val appUpdateManager = AppUpdateManagerFactory.create(this)
            val appUpdateInfoTask = appUpdateManager.appUpdateInfo
            appUpdateInfoTask.addOnSuccessListener {

                if (it.installStatus() == InstallStatus.DOWNLOADED) {
                    appUpdateManager.completeUpdate()
                    return@addOnSuccessListener
                }

                if (it.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS
                    && it.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
                ) {
                    appUpdateManager.startUpdateFlowForResult(
                        it,
                        this,
                        AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build(),
                        APP_UPDATE_REQUEST_CODE
                    )
                    return@addOnSuccessListener
                }

                //Check for flexible update and staleness

                val listener = InstallStateUpdatedListener { installState ->
                    if (installState.installStatus() == InstallStatus.DOWNLOADED) {
                        appUpdateManager.completeUpdate()
                    }
                }
                appUpdateManager.registerListener(listener)
                appUpdateManager.startUpdateFlowForResult(
                    it,
                    this,
                    AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build(),
                    APP_UPDATE_REQUEST_CODE
                )

                //Check for immediate update
//                if (it.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) &&
//                    it.updatePriority > 3
//                ) {
//                    appUpdateManager.startUpdateFlowForResult(
//                        it,
//                        this,
//                        AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build(),
//                        APP_UPDATE_REQUEST_CODE
//                    )
//                    return@addOnSuccessListener
//                }
            }
        }
    }

    interface BottomIconDoubleClick {
        fun doubleClick()
    }

}
