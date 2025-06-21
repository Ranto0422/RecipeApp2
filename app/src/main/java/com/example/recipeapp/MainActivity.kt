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
import androidx.core.net.toUri

class MainActivity : ComponentActivity() {

    // Sample data for demonstration purposez ito yung need palatan using db and api
    private val recipes = mutableStateListOf(
        Recipe(1, "Pancakes", listOf(
            Ingredient("Flour", "100", "grams"),
            Ingredient("Eggs", "2", "pieces"),
            Ingredient("Milk", "200", "ml")
        ), "Mix and cook.", imageUri = "android.resource://com.example.recipeapp/drawable/sample_pancake"),
        Recipe(2, "Salad", listOf(
            Ingredient("Lettuce", "1", "head"),
            Ingredient("Tomato", "2", "pieces"),
            Ingredient("Cucumber", "1", "piece")
        ), "Chop and mix.", imageUri = "android.resource://com.example.recipeapp/drawable/sample_salad")
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
                val imageUri = data.getStringExtra("imageUri")
                if (imageUri != null && imageUri.startsWith("content://")) {
                    try {
                        val uri = imageUri.toUri()
                        contentResolver.takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                        try {
                            val inputStream = contentResolver.openInputStream(uri)
                            if (inputStream == null) {
                                android.widget.Toast.makeText(this, "Image URI not accessible", android.widget.Toast.LENGTH_LONG).show()
                            } else {
                                inputStream.close()
                            }
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(this, "Cannot open image: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                        }
                    } catch (e: SecurityException) {
                        e.printStackTrace()
                        android.widget.Toast.makeText(this, "Permission error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                    }
                }
                val ingredientType = object : TypeToken<List<Ingredient>>() {}.type
                val ingredients: List<Ingredient> = Gson().fromJson(ingredientsJson, ingredientType)
                val newId = (recipes.maxOfOrNull { it.id } ?: 0) + 1
                recipes.add(
                    Recipe(
                        newId,
                        name,
                        ingredients,
                        directions,
                        imageUri = imageUri
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
