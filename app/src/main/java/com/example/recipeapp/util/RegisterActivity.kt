package com.example.recipeapp.util

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.recipeapp.ui.RegisterScreen

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RegisterScreen(
                onRegister = { name, email, password ->
                    // register logic here
                },
                onLoginClick = {
                    // navigate to login
                }
            )
        }
    }
}

