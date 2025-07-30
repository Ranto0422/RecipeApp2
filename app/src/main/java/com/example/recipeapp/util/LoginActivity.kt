package com.example.recipeapp.util

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.recipeapp.ui.LoginScreen

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoginScreen(
                onLogin = { email, password ->
                    // login logic here
                },
                onRegisterClick = {
                    // navigate to register
                },
                onGuestClick = {
                    // continue as guest
                }
            )
        }
    }
}

