package com.example.recipeapp.util

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.recipeapp.ui.RecipeDetailScreen

class RecipeDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // You may want to pass recipeId via intent or a ViewModel
            val recipeId = intent.getIntExtra("recipeId", -1)
            val isUserRecipe = intent.getBooleanExtra("isUserRecipe", false)
            val userId = intent.getIntExtra("userId", -1)
            if (recipeId != -1) {
                val recipe = com.example.recipeapp.model.DummyRecipe(
                    id = recipeId,
                    name = "",
                    ingredients = emptyList(),
                    instructions = emptyList(),
                    prepTimeMinutes = null,
                    cookTimeMinutes = null,
                    servings = null,
                    difficulty = null,
                    cuisine = null,
                    caloriesPerServing = null,
                    tags = emptyList(),
                    userId = if (userId != -1) userId else null,
                    image = null,
                    rating = null,
                    reviewCount = null,
                    mealType = null,
                    isPublic = true,
                    isApproved = true
                )
                RecipeDetailScreen(
                    recipe = recipe,
                    isUserRecipe = isUserRecipe,
                    onBack = {
                        // handle back navigation
                    }
                )
            }
        }
    }
}
