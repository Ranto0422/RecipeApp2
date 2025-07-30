package com.example.recipeapp.util

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.recipeapp.ui.AdminScreen

class AdminActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AdminScreen(
                onBack = {
                    // handle back navigation
                },
                onRecipeDetailNavigate = { recipeId, userId ->
                    // handle navigation to recipe detail, e.g. startActivity or navigate
                }
            )
        }
    }
}
