package com.example.recipeapp.util

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.recipeapp.ui.ProfileScreen

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProfileScreen(
                userName = "User", // Replace with actual user name
                userEmail = "user@email.com", // Replace with actual user email
                userRole = "user", // Replace with actual user role
                onLogout = {
                    // handle logout logic
                },
                onHomeClick = {
                    // handle home navigation
                },
                onSearchClick = {
                    // handle search navigation
                },
                onMyRecipeClick = {
                    // handle pantry navigation
                },
                onProfileClick = {
                    // handle profile navigation
                },
                onAddRecipe = {
                    // handle add recipe navigation
                },
                onAdminApproval = {
                    // handle admin approval navigation
                }
            )
        }
    }
}

