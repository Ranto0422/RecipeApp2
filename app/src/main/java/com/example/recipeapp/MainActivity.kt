package com.example.recipeapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import com.example.recipeapp.model.Recipe
import com.example.recipeapp.ui.RecipeListScreen
import androidx.compose.runtime.mutableStateListOf
import com.example.recipeapp.model.Ingredient
import com.google.gson.reflect.TypeToken
import com.google.gson.Gson
import androidx.core.net.toUri
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.compose.rememberNavController
import com.example.recipeapp.ui.RecipeDetailScreen

@OptIn(ExperimentalAnimationApi::class)
class MainActivity : ComponentActivity() {

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
                // Here you would typically add the new recipe to your API or database
            }
        }
        setContent {
            val navController = rememberAnimatedNavController()
            AnimatedNavHost(
                navController = navController,
                startDestination = "list"
            ) {
                composable(
                    "list"
                ) {
                    RecipeListScreen(
                        onAddRecipe = {
                            val intent = Intent(this@MainActivity, com.example.recipeapp.ui.AddRecipeActivity::class.java)
                            addRecipeLauncher.launch(intent)
                        },
                        onRecipeClick = { recipe ->
                            navController.navigate("detail/${recipe.id}")
                        }
                    )
                }
                composable(
                    "detail/{recipeId}"
                ) { backStackEntry ->
                    val recipeId = backStackEntry.arguments?.getString("recipeId")?.toIntOrNull()
                    if (recipeId != null) {
                        RecipeDetailScreen(
                            recipeId = recipeId,
                            onBack = { navController.popBackStack() }
                        )
                    } else {
                        Text("Invalid recipe ID")
                    }
                }
            }
        }
    }
}
