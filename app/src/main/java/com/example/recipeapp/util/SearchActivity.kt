package com.example.recipeapp.util

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.recipeapp.model.DummyRecipe
import com.example.recipeapp.ui.SearchScreen

class SearchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SearchScreen(
                onRecipeClick = { recipe: DummyRecipe ->
                    // handle recipe click this move to recipe detail screen
                },
                onBack = {
                    // handle back navigation
                }
            )
        }
    }
}

