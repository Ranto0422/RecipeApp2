package com.example.recipeapp.util

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.recipeapp.model.DummyRecipe
import com.example.recipeapp.ui.RecipeListScreen

class RecipeListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // You may want to pass recipes and other state via intent or a ViewModel
            RecipeListScreen(
                onAddRecipe = {
                    // handle add recipe click
                },
                onRecipeClick = { recipe: DummyRecipe, isUserRecipe: Boolean ->
                    // handle recipe click, e.g. navigate to details
                },
                onLuckyClick = {
                    // handle lucky click
                },
                onProfileClick = {
                    // handle profile click
                },
                isLuckyLoading = false,
                luckyError = null,
                onSearchClick = {
                    // handle search click
                },
                onHomeClick = {
                    // handle home click
                },
                onMyRecipeClick = {
                    // handle my recipe click
                }
            )
        }
    }
}
