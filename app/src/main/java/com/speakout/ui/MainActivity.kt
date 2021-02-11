package com.speakout.ui

import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.iid.FirebaseInstanceId
import com.speakout.*
import com.speakout.api.RetrofitBuilder
import com.speakout.ui.home.HomeFragment
import com.speakout.users.UsersRepository
import com.speakout.utils.AppPreference
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber

class MainActivity : BaseActivity() {

    private lateinit var navController: NavController
    private var currentFragmentId = 0
    private lateinit var bottomNavigationView: BottomNavigationView
    private var mHomeFragment: HomeFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        window.navigationBarColor = Color.parseColor("#20111111");
        setContentView(R.layout.activity_main)
        bottomNavigationView = findViewById(R.id.nav_view)

        navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_search,
                R.id.navigation_new_post,
                R.id.notificationFragment,
                R.id.navigation_profile
            )
        )
//        setupActionBarWithNavController(navController, appBarConfiguration)
        bottomNavigationView.setupWithNavController(navController)
        bottomNavigationView.setOnNavigationItemSelectedListener {
            Timber.d("setOnNavigationItemSelectedListener: ${it.title}")
            when (it.itemId) {
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
            Timber.d("addOnDestinationChangedListener: ${destination.label}")
            handleNavigationVisibility(currentFragmentId)
        }

        bottomNavigationView.setOnNavigationItemReselectedListener {
            Timber.d("setOnNavigationItemReselectedListener: ${it.title}")
            val hostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
            if (hostFragment is NavHostFragment) {
                val currentFragment = hostFragment.childFragmentManager.fragments.first()
                if (currentFragment is BottomIconDoubleClick)
                    currentFragment.doubleClick()
            }
        }

//        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { task ->
//            if (!task.isSuccessful) {
//                Timber.e("Failed")
//            }
//
//            try {
//                // Get new Instance ID token
//                val token = task.result?.token
//                Timber.d("Token: $token")
//
//                GlobalScope.launch {
//                    UsersRepository(RetrofitBuilder.apiService, AppPreference).updateFcmToken(
//                        token ?: ""
//                    )
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }

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
            Timber.d("onBackPressed: $currentFragmentId")
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        AppPreference.clearFirebaseToken()
        super.onDestroy()
    }

    interface BottomIconDoubleClick {
        fun doubleClick()
    }
}
