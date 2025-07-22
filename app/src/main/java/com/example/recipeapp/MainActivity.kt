package com.example.recipeapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import com.example.recipeapp.model.Ingredient
import com.google.gson.reflect.TypeToken
import com.google.gson.Gson
import androidx.core.net.toUri
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
import com.example.recipeapp.ui.LoginScreen
import com.example.recipeapp.ui.RegisterScreen
import com.example.recipeapp.ui.ProfileScreen
import com.example.recipeapp.ui.RecipeListScreen
import com.example.recipeapp.ui.AdminScreen
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog

@OptIn()
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val addRecipeLauncher = registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val ingredientsJson = data?.getStringExtra("ingredients_json") ?: return@registerForActivityResult
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
                // Add recipe logic here
            }
        }
        setContent {
            val navController = rememberNavController()
            var isLuckyLoading by remember { mutableStateOf(false) }
            var luckyError by remember { mutableStateOf<String?>(null) }
            var recipes by remember { mutableStateOf<List<DummyRecipe>>(emptyList()) }
            var loggedInUser by remember { mutableStateOf<Map<String, String?>?>(null) }
            var isGuest by remember { mutableStateOf(false) }
            var showLoginPrompt by remember { mutableStateOf(false) }
            val context = LocalContext.current
            val coroutineScope = rememberCoroutineScope()
            var pendingRecipes by remember { mutableStateOf<List<DummyRecipe>>(emptyList()) }

            LaunchedEffect(recipes) {
                pendingRecipes = recipes.filter { it.isPublic && !it.isApproved }
            }

            LaunchedEffect(Unit) {
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
                } catch (_: Exception) {}
            }
            NavHost(
                navController = navController,
                startDestination = "login"
            ) {
                composable("login") {
                    LoginScreen(
                        onLogin = { email, password ->
                            isGuest = false
                            coroutineScope.launch {
                                val result = com.example.recipeapp.ui.loginUser(context, email, password)
                                if (result != null && result.optBoolean("success")) {
                                    val user = result.optJSONObject("user")
                                    loggedInUser = mapOf(
                                        "name" to user?.optString("name", "User"),
                                        "email" to user?.optString("email", "user@email.com"),
                                        "role" to user?.optString("role", "user")
                                    )
                                    navController.navigate("list")
                                }
                            }
                        },
                        onRegisterClick = { navController.navigate("register") },
                        onGuestClick = {
                            isGuest = true
                            loggedInUser = null
                            navController.navigate("list")
                        }
                    )
                }
                composable("register") {
                    RegisterScreen(
                        onRegister = { name, email, password ->
                            navController.navigate("login")
                        },
                        onLoginClick = { navController.popBackStack() }
                    )
                }
                composable("list") {
                    RecipeListScreen(
                        onAddRecipe = {
                            if (isGuest) {
                                showLoginPrompt = true
                            } else {
                                val intent = Intent(this@MainActivity, com.example.recipeapp.ui.AddRecipeActivity::class.java)
                                addRecipeLauncher.launch(intent)
                            }
                        },
                        onRecipeClick = { recipe: DummyRecipe ->
                            navController.navigate("detail/${recipe.id}")
                        },
                        onLuckyClick = {
                            if (isGuest) {
                                showLoginPrompt = true
                            } else {
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
                                    } catch (_: Exception) {
                                        luckyError = "Failed to load random recipe."
                                    } finally {
                                        isLuckyLoading = false
                                    }
                                }
                            }
                        },
                        onProfileClick = {
                            if (isGuest) {
                                showLoginPrompt = true
                            } else {
                                navController.navigate("profile")
                            }
                        },
                        isLuckyLoading = isLuckyLoading,
                        luckyError = luckyError,
                        onSearchClick = { navController.navigate("search") }
                    )
                }
                composable("search") {
                    SearchScreen(
                        onRecipeClick = { recipe: DummyRecipe ->
                            navController.navigate("detail/${recipe.id}")
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("detail/{recipeId}") { backStackEntry ->
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
                composable("admin") {
                    if (isGuest) {
                        showLoginPrompt = true
                    } else {
                        AdminScreen(
                            pendingRecipes = pendingRecipes,
                            onApprove = { recipe ->
                                recipes = recipes.map {
                                    if (it.id == recipe.id) it.copy(isApproved = true) else it
                                }
                                pendingRecipes = pendingRecipes.filter { it.id != recipe.id }
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }
                }
                composable("profile") {
                    if (isGuest) {
                        showLoginPrompt = true
                    } else {
                        ProfileScreen(
                            userName = loggedInUser?.get("name") ?: "User",
                            userEmail = loggedInUser?.get("email") ?: "user@email.com",
                            userRole = loggedInUser?.get("role") ?: "user",
                            onLogout = {
                                loggedInUser = null
                                navController.navigate("login") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onHomeClick = { navController.navigate("list") },
                            onSearchClick = { navController.navigate("search") },
                            onPantryClick = { /* TODO: Implement pantry navigation */ },
                            onProfileClick = { navController.navigate("profile") },
                            onAddRecipe = {
                                val intent = Intent(this@MainActivity, com.example.recipeapp.ui.AddRecipeActivity::class.java)
                                addRecipeLauncher.launch(intent)
                            },
                            onAdminApproval = {
                                navController.navigate("admin")
                            }
                        )
                    }
                }
            }

            if (showLoginPrompt) {
                AlertDialog(
                    onDismissRequest = { showLoginPrompt = false },
                    title = { Text("Login Required") },
                    text = { Text("Please login to continue this action.") },
                    confirmButton = {
                        Button(onClick = {
                            showLoginPrompt = false
                            navController.navigate("login")
                        }) {
                            Text("Login")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showLoginPrompt = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}
