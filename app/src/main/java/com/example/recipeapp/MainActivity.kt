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
import com.example.recipeapp.ui.MyRecipeScreen
import com.example.recipeapp.ui.AddRecipeActivity


class MainActivity : ComponentActivity() {
    // Handles the result from AddRecipeActivity, including image URI permission and ingredient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val addRecipeLauncher = registerForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
        ) { result ->
            // Handles the result from AddRecipeActivity
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
                // TODO Add recipe logic here
            }
        }
        setContent {
            // Main composable content, sets up navigation and state for the app
            val navController = rememberNavController()
            var isLuckyLoading by remember { mutableStateOf(false) } // Tracks loading state for "I'm Feeling Lucky" feature
            var luckyError by remember { mutableStateOf<String?>(null) } // Stores error message for im feling lucky feature
            var recipes by remember { mutableStateOf<List<DummyRecipe>>(emptyList()) } // Holds all loaded recipes
            var loggedInUser by remember { mutableStateOf<Map<String, String?>?>(null) } // Stores logged in user info
            var isGuest by remember { mutableStateOf(false) } // Tracks if user is a guest
            var showLoginPrompt by remember { mutableStateOf(false) }
            val context = LocalContext.current
            val coroutineScope = rememberCoroutineScope()
            var pendingRecipes by remember { mutableStateOf<List<DummyRecipe>>(emptyList()) } // Recipes pending admin approval

            // Updates pendingRecipes whenever recipes change
            LaunchedEffect(recipes) {
                pendingRecipes = recipes.filter { it.isPublic && !it.isApproved }
            }

            // Loadz recipes from remote API on first launch
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
                                        "userId" to user?.optString("userId"), // <-- use userId
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
                    val context = LocalContext.current
                    RecipeListScreen(
                        onAddRecipe = {
                            if (isGuest) {
                                showLoginPrompt = true
                            } else {
                                val userId = loggedInUser?.get("userId")?.toIntOrNull() ?: -1
                                val intent = Intent(context, AddRecipeActivity::class.java)
                                intent.putExtra("userId", userId)
                                addRecipeLauncher.launch(intent)
                            }
                        },
                        onRecipeClick = { recipe: DummyRecipe, isUserRecipe: Boolean ->
                            if (isUserRecipe && recipe.userId != null) {
                                navController.navigate("userDetail/${recipe.id}/${recipe.userId}")
                            } else {
                                navController.navigate("detail/${recipe.id}")
                            }
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
                        onMyRecipeClick = {
                            navController.navigate("myrecipes")
                        },
                        isLuckyLoading = isLuckyLoading,
                        luckyError = luckyError,
                        onSearchClick = { navController.navigate("search") },
                        onHomeClick = { navController.navigate("list") }
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
                        val recipe = DummyRecipe(
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
                            userId = null,
                            image = null,
                            rating = null,
                            reviewCount = null,
                            mealType = null,
                            isPublic = true,
                            isApproved = true
                        )
                        RecipeDetailScreen(
                            recipe = recipe,
                            isUserRecipe = false,
                            onBack = { navController.popBackStack() }
                        )
                    } else {
                        Text("Invalid recipe ID")
                    }
                }
                composable("userDetail/{recipeId}/{userId}") { backStackEntry ->
                    val recipeId = backStackEntry.arguments?.getString("recipeId")?.toIntOrNull()
                    val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull()
                    if (recipeId != null && userId != null) {
                        val recipe = DummyRecipe(
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
                            userId = userId,
                            image = null,
                            rating = null,
                            reviewCount = null,
                            mealType = null,
                            isPublic = true,
                            isApproved = true
                        )
                        RecipeDetailScreen(
                            recipe = recipe,
                            isUserRecipe = true,
                            onBack = { navController.popBackStack() }
                        )
                    } else {
                        Text("Invalid recipe or user ID")
                    }
                }
                composable("admin") {
                    if (isGuest) {
                        showLoginPrompt = true
                    } else {
                        AdminScreen(
                            onBack = { navController.popBackStack() },
                            onRecipeDetailNavigate = { recipeId, userId ->
                                navController.navigate("adminDetail/$recipeId/$userId")
                            }
                        )
                    }
                }
                composable("adminDetail/{recipeId}/{userId}") { backStackEntry ->
                    val recipeId = backStackEntry.arguments?.getString("recipeId")?.toIntOrNull()
                    val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull()
                    if (recipeId != null && userId != null) {
                        // Pass a minimal DummyRecipe object for user-uploaded recipe
                        val recipe = DummyRecipe(
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
                            userId = userId,
                            image = null,
                            rating = null,
                            reviewCount = null,
                            mealType = null,
                            isPublic = true,
                            isApproved = true
                        )
                        RecipeDetailScreen(
                            recipe = recipe,
                            isUserRecipe = true,
                            onBack = { navController.popBackStack() }
                        )
                    } else {
                        Text("Invalid recipe or user ID")
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
                            onMyRecipeClick = {
                                navController.navigate("myrecipes")
                            },
                            onProfileClick = { navController.navigate("profile") },
                            onAddRecipe = {
                                val userId = loggedInUser?.get("userId")?.toIntOrNull() ?: -1
                                val intent = Intent(this@MainActivity, com.example.recipeapp.ui.AddRecipeActivity::class.java)
                                intent.putExtra("userId", userId)
                                addRecipeLauncher.launch(intent)
                            },
                            onAdminApproval = {
                                navController.navigate("admin")
                            }
                        )
                    }
                }
                composable("myrecipes") {
                    val userId = loggedInUser?.get("userId")?.toIntOrNull() ?: -1
                    MyRecipeScreen(
                        userId = userId,
                        onRecipeClick = { recipe ->
                            navController.navigate("userDetail/${recipe.id}/$userId")
                        },
                        onEditRecipe = { recipe ->
                            val intent = Intent(context, AddRecipeActivity::class.java)
                            intent.putExtra("edit_mode", true)
                            intent.putExtra("recipe_id", recipe.id)
                            intent.putExtra("user_id", recipe.userId)
                            intent.putExtra("name", recipe.name)
                            intent.putExtra("ingredients", ArrayList(recipe.ingredients))
                            intent.putExtra("instructions", ArrayList(recipe.instructions))
                            intent.putExtra("servings", recipe.servings)
                            intent.putExtra("tags", ArrayList(recipe.tags))
                            intent.putExtra("imageUrl", recipe.imageUrl)
                            intent.putExtra("cookTimeMinutes", recipe.cookTimeMinutes)
                            intent.putExtra("prepTimeMinutes", recipe.prepTimeMinutes)
                            intent.putExtra("cuisine", recipe.cuisine)
                            intent.putExtra("difficulty", recipe.difficulty)
                            intent.putExtra("visibility", recipe.visibility)
                            intent.putExtra("isApproved", recipe.isApproved)
                            addRecipeLauncher.launch(intent)
                        },
                        onHomeClick = { navController.navigate("list") },
                        onSearchClick = { navController.navigate("search") },
                        onAddRecipe = {
                            val userId = loggedInUser?.get("userId")?.toIntOrNull() ?: -1
                            val intent = Intent(this@MainActivity, com.example.recipeapp.ui.AddRecipeActivity::class.java)
                            intent.putExtra("userId", userId)
                            addRecipeLauncher.launch(intent)
                        },
                        onMyRecipeClick = { navController.navigate("myrecipes") },
                        onProfileClick = { navController.navigate("profile") }
                    )
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
