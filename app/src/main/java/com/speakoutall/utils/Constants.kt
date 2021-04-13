package com.speakoutall.utils

object Constants {

    const val INVALID_KEY = -1L

    object UserDetails {
        const val userId = "user_id"
        const val name = "name"
        const val email = "email"
        const val username = "username"
        const val lastLogin = "last_login"
        const val phoneNumber = "phone_number"
        const val isLoggedIn = "is_logged_in"
        const val providerType = "provider_type"
        const val isEmailVerified = "is_email_verified"
        const val photoUrl = "photo_url"
        const val lastUserDetailsUpdate = "last_updated_time"
        const val usernameProcess = "username_process"
    }

    object DatabaseRefs {
        const val userDetailsRef = "user_details"
        const val usernamesRef = "user_names"
        const val postsRef = "posts"
        const val tags = "tags"
        const val followers = "followers"
        const val followings = "followings"
    }

    object IntentStrings {

        object CreatePost {
            const val REQUEST_CODE = 101
        }

        object BottomDialogActivity {
            const val REQUEST_CODE = 150
        }
    }

}