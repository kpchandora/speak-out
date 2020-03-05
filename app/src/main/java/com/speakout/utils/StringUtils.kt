package com.speakout.utils

object StringUtils {

    object UserDetails {
        const val name = "name"
        const val email = "email"
        const val username = "username"
        const val lastLogin = "last_login"
        const val phoneNumber = "phone_number"
        const val isLoggedIn = "is_logged_in"
        const val providerType = "provider_type"
        const val isEmailVerified = "is_email_verified"
        const val photoUrl = "photo_url"
    }

    object DatabaseRefs {
        const val userDetailsRef = "user_details"
        const val usernamesRef = "user_names"
        const val postsRef = "posts"
        const val tags = "tags"
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