package com.speakout.ui

import android.graphics.Color
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.speakout.R
import com.speakout.extensions.openActivity
import com.speakout.posts.create.CreateNewPostActivity
import com.speakout.utils.AppPreference

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        window.navigationBarColor = Color.parseColor("#20111111");

        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
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
        navView.setupWithNavController(navController)
        navView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_new_post -> {
                    openActivity(CreateNewPostActivity::class.java)
                    return@setOnNavigationItemSelectedListener false
                }
                R.id.navigation_profile -> {
                    navController.navigate(
                        it.itemId,
                        bundleOf("user_id" to AppPreference.getUserId())
                    )
                }
                else -> {
                    navController.navigate(it.itemId)
                }
            }
            true
        }
    }
}
