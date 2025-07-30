package com.example.recipeapp.model

// Data class for user-uploaded recipes

data class UserRecipe(
    val id: Int,
    val name: String,
    val ingredients: List<String>,
    val instructions: List<String>,
    val servings: Int,
    val tags: List<String>,
    val imageUrl: String,
    val cookTimeMinutes: Int?,
    val prepTimeMinutes: Int?,
    val cuisine: String?,
    val difficulty: String?,
    val userId: Int?,
    val visibility: String,
    val isApproved: Int // Change from Boolean to Int
)
