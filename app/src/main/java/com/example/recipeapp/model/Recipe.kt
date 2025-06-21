package com.example.recipeapp.model

data class Recipe(
    val id: Int,
    val title: String,
    val ingredients: List<Ingredient>,
    val directions: String,
    val imageUri: String? = null

)