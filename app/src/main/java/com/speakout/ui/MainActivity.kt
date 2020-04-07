package com.speakout.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.activity.viewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.speakout.*
import com.speakout.extensions.addFragment
import com.speakout.extensions.openActivity
import com.speakout.posts.create.CreateNewPostActivity
import com.speakout.users.UsersListFragment
import com.speakout.utils.AppPreference
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var navController: NavController
    private var currentFragmentId = 0
    private lateinit var bottomNavigationView: BottomNavigationView

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
                R.id.navigation_notifications,
                R.id.navigation_profile
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        bottomNavigationView.setupWithNavController(navController)
        bottomNavigationView.setOnNavigationItemSelectedListener {
            Timber.d("setOnNavigationItemSelectedListener: ${it.title}")
            when (it.itemId) {
                R.id.navigation_new_post -> {
                    openActivity(CreateNewPostActivity::class.java)
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
    }

    private fun navAnimVisible() {
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

}

interface BottomIconDoubleClick {
    fun doubleClick()
}