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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import androidx.compose.runtime.*
import com.example.recipeapp.model.DummyRecipe
import com.example.recipeapp.util.*
import com.example.recipeapp.ui.SearchScreen

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
            var isLuckyLoading by remember { mutableStateOf(false) }
            var luckyError by remember { mutableStateOf<String?>(null) }
            var recipes by remember { mutableStateOf<List<DummyRecipe>>(emptyList()) }
            var isLoading by remember { mutableStateOf(false) }
            var errorMessage by remember { mutableStateOf<String?>(null) }
            val coroutineScope = rememberCoroutineScope()

            // Fetch recipes from DummyJSON API (once)
            LaunchedEffect(Unit) {
                isLoading = true
                try {
                    val response = withContext(Dispatchers.IO) {
                        URL("https://dummyjson.com/recipes").readText()
                    }
                    val json = JSONObject(response)
                    val arr = json.getJSONArray("recipes")
                    val list = mutableListOf<DummyRecipe>()
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        list.add(
                            DummyRecipe(
                                id = obj.getInt("id"),
                                name = obj.getString("name"),
                                ingredients = obj.getJSONArray("ingredients").toStringList(),
                                instructions = obj.getJSONArray("instructions").toStringList(),
                                prepTimeMinutes = obj.optIntOrNull("prepTimeMinutes"),
                                cookTimeMinutes = obj.optIntOrNull("cookTimeMinutes"),
                                servings = obj.optIntOrNull("servings"),
                                difficulty = obj.optStringOrNull("difficulty"),
                                cuisine = obj.optStringOrNull("cuisine"),
                                caloriesPerServing = obj.optIntOrNull("caloriesPerServing"),
                                tags = obj.optJSONArrayOrNull("tags")?.toStringList(),
                                userId = obj.optIntOrNull("userId"),
                                image = obj.optStringOrNull("image"),
                                rating = obj.optDoubleOrNull("rating"),
                                reviewCount = obj.optIntOrNull("reviewCount"),
                                mealType = obj.optJSONArrayOrNull("mealType")?.toStringList()
                            )
                        )
                    }
                    recipes = list
                } catch (e: Exception) {
                    errorMessage = "Failed to load recipes."
                    e.printStackTrace()
                } finally {
                    isLoading = false
                }
            }
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
                        },
                        onLuckyClick = {
                            coroutineScope.launch {
                                isLuckyLoading = true
                                luckyError = null
                                try {
                                    val ids = recipes.map { it.id }
                                    if (ids.isNotEmpty()) {
                                        val randomId = ids.random()
                                        val response = withContext(Dispatchers.IO) {
                                            URL("https://dummyjson.com/recipes/$randomId").readText()
                                        }
                                        val obj = JSONObject(response)
                                        val luckyRecipe = DummyRecipe(
                                            id = obj.getInt("id"),
                                            name = obj.getString("name"),
                                            ingredients = obj.getJSONArray("ingredients").toStringList(),
                                            instructions = obj.getJSONArray("instructions").toStringList(),
                                            prepTimeMinutes = obj.optIntOrNull("prepTimeMinutes"),
                                            cookTimeMinutes = obj.optIntOrNull("cookTimeMinutes"),
                                            servings = obj.optIntOrNull("servings"),
                                            difficulty = obj.optStringOrNull("difficulty"),
                                            cuisine = obj.optStringOrNull("cuisine"),
                                            caloriesPerServing = obj.optIntOrNull("caloriesPerServing"),
                                            tags = obj.optJSONArrayOrNull("tags")?.toStringList(),
                                            userId = obj.optIntOrNull("userId"),
                                            image = obj.optStringOrNull("image"),
                                            rating = obj.optDoubleOrNull("rating"),
                                            reviewCount = obj.optIntOrNull("reviewCount"),
                                            mealType = obj.optJSONArrayOrNull("mealType")?.toStringList()
                                        )
                                        navController.navigate("detail/${luckyRecipe.id}")
                                    } else {
                                        luckyError = "No recipes available."
                                    }
                                } catch (e: Exception) {
                                    luckyError = "Failed to load random recipe."
                                } finally {
                                    isLuckyLoading = false
                                }
                            }
                        },
                        isLuckyLoading = isLuckyLoading,
                        luckyError = luckyError,
                        onSearchClick = { navController.navigate("search") }
                    )
                }
                composable(
                    "search"
                ) {
                    SearchScreen(
                        onRecipeClick = { recipe ->
                            navController.navigate("detail/${recipe.id}")
                        },
                        onBack = { navController.popBackStack() }
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
