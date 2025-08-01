package com.example.recipeapp.model

data class DummyRecipe(
    val id: Int,
    val name: String,
    val ingredients: List<String>,
    val instructions: List<String>,
    val prepTimeMinutes: Int?,
    val cookTimeMinutes: Int?,
    val servings: Int?,
    val difficulty: String?,
    val cuisine: String?,
    val caloriesPerServing: Int?,
    val tags: List<String>?,
    val userId: Int?,
    val image: String?,
    val rating: Double?,
    val reviewCount: Int?,
    val mealType: List<String>?,
    val isPublic: Boolean = false,
    val isApproved: Boolean = false
)
