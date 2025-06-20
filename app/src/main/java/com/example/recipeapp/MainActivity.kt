package com.example.recipeapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.recipeapp.model.Recipe
import com.example.recipeapp.ui.RecipeListScreen
import androidx.compose.runtime.mutableStateListOf
import com.example.recipeapp.model.Ingredient
import com.google.gson.reflect.TypeToken
import com.google.gson.Gson

class MainActivity : ComponentActivity() {

    // Sample data for demonstration purposes
    private val recipes = mutableStateListOf(
        Recipe(1, "Pancakes", listOf(
            Ingredient("Flour", "100", "grams"),
            Ingredient("Eggs", "2", "pieces"),
            Ingredient("Milk", "200", "ml")
        ), "Mix and cook."),
        Recipe(2, "Salad", listOf(
            Ingredient("Lettuce", "1", "head"),
            Ingredient("Tomato", "2", "pieces"),
            Ingredient("Cucumber", "1", "piece")
        ), "Chop and mix.")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val addRecipeLauncher = registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val name = data?.getStringExtra("name") ?: return@registerForActivityResult
                val ingredientsJson = data.getStringExtra("ingredients_json") ?: return@registerForActivityResult
                val directions = data.getStringExtra("directions") ?: return@registerForActivityResult
                val ingredientType = object : TypeToken<List<Ingredient>>() {}.type
                val ingredients: List<Ingredient> = Gson().fromJson(ingredientsJson, ingredientType)
                val newId = (recipes.maxOfOrNull { it.id } ?: 0) + 1
                recipes.add(
                    Recipe(
                        newId,
                        name,
                        ingredients,
                        directions
                    )
                )
            }
        }
        setContent {
            RecipeListScreen(
                recipes = recipes,
                onAddRecipe = {
                    val intent = Intent(this, com.example.recipeapp.ui.AddRecipeActivity::class.java)
                    addRecipeLauncher.launch(intent)
                },
                onRecipeClick = { recipe ->
                    val intent = Intent(this, com.example.recipeapp.ui.RecipeDetailActivity::class.java)
                    intent.putExtra("recipe_json", Gson().toJson(recipe))
                    startActivity(intent)
                }
            )
        }
    }
}
