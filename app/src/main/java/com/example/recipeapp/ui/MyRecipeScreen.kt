package com.example.recipeapp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.recipeapp.model.UserRecipe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL


@Composable
fun MyRecipeScreen(
    userId: Int?,
    onRecipeClick: (UserRecipe) -> Unit,
    onEditRecipe: (UserRecipe) -> Unit = {}, // Add edit callback
    modifier: Modifier = Modifier,
    onHomeClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onAddRecipe: () -> Unit = {},
    onMyRecipeClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    reloadTrigger: Int = 0
) {
    var recipes by remember { mutableStateOf<List<UserRecipe>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Get context at the top level of the Composable
    val context = LocalContext.current

    // Fetch user recipes from PHP backend
    LaunchedEffect(userId, reloadTrigger) { // Add reloadTrigger to effect
        isLoading = true
        try {
            val response = withContext(Dispatchers.IO) {
                URL("http://10.0.2.2/MyRecipeAppRestApi/get_user_recipes.php?userId=$userId").readText()
            }
            // Debug: print raw response
            println("Raw response: $response")
            val json = JSONObject(response)
            if (!json.optBoolean("success", false)) {
                errorMessage = json.optString("error", "Failed to load recipes.")
            } else {
                val arr = json.getJSONArray("recipes")
                val list = mutableListOf<UserRecipe>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val isApprovedInt = obj.optInt("isApproved", 0)
                    list.add(
                        UserRecipe(
                            id = obj.optInt("id"),
                            name = obj.optString("name"),
                            ingredients = try { JSONArray(obj.optString("ingredients", "[]")).let { ingArr -> List(ingArr.length()) { ingArr.getString(it) } } } catch (_: Exception) { emptyList() },
                            instructions = try { JSONArray(obj.optString("instructions", "[]")).let { instArr -> List(instArr.length()) { instArr.getString(it) } } } catch (_: Exception) { emptyList() },
                            servings = obj.optInt("servings"),
                            tags = try { JSONArray(obj.optString("tags", "[]")).let { tagArr -> List(tagArr.length()) { tagArr.getString(it) } } } catch (_: Exception) { emptyList() },
                            imageUrl = obj.optString("image"),
                            cookTimeMinutes = obj.optInt("cookTimeMinutes"),
                            prepTimeMinutes = obj.optInt("prepTimeMinutes"),
                            cuisine = obj.optString("cuisine"),
                            difficulty = obj.optString("difficulty"),
                            userId = obj.optInt("userId"),
                            visibility = obj.optString("visibility"),
                            isApproved = isApprovedInt
                        )
                    )
                }
                recipes = list
                errorMessage = if (recipes.isEmpty()) "No recipes found." else null
            }
        } catch (e: Exception) {
            errorMessage = "Failed to load recipes.\n${e.message}"
            println("Error: ${e.message}")
            e.printStackTrace()
        } finally {
            isLoading = false
        }
    }

    // Auto-refresh recipes every 30 seconds
    LaunchedEffect(userId) {
        while (true) {
            delay(30000) // 30 seconds
            if (userId != null) {
                try {
                    val response = withContext(Dispatchers.IO) {
                        URL("http://10.0.2.2/MyRecipeAppRestApi/get_user_recipes.php?userId=$userId").readText()
                    }
                    val json = JSONObject(response)
                    if (json.optBoolean("success", false)) {
                        val arr = json.getJSONArray("recipes")
                        val list = mutableListOf<UserRecipe>()
                        for (i in 0 until arr.length()) {
                            val obj = arr.getJSONObject(i)
                            val isApprovedInt = obj.optInt("isApproved", 0)
                            list.add(
                                UserRecipe(
                                    id = obj.optInt("id"),
                                    name = obj.optString("name"),
                                    ingredients = try { JSONArray(obj.optString("ingredients", "[]")).let { ingArr -> List(ingArr.length()) { ingArr.getString(it) } } } catch (_: Exception) { emptyList() },
                                    instructions = try { JSONArray(obj.optString("instructions", "[]")).let { instArr -> List(instArr.length()) { instArr.getString(it) } } } catch (_: Exception) { emptyList() },
                                    servings = obj.optInt("servings"),
                                    tags = try { JSONArray(obj.optString("tags", "[]")).let { tagArr -> List(tagArr.length()) { tagArr.getString(it) } } } catch (_: Exception) { emptyList() },
                                    imageUrl = obj.optString("image"),
                                    cookTimeMinutes = obj.optInt("cookTimeMinutes"),
                                    prepTimeMinutes = obj.optInt("prepTimeMinutes"),
                                    cuisine = obj.optString("cuisine"),
                                    difficulty = obj.optString("difficulty"),
                                    userId = obj.optInt("userId"),
                                    visibility = obj.optString("visibility"),
                                    isApproved = isApprovedInt
                                )
                            )
                        }
                        // Only update if there are actual changes to prevent unnecessary recompositions
                        if (recipes != list) {
                            recipes = list
                        }
                    }
                } catch (e: Exception) {
                    // Silently handle auto-refresh errors to avoid disrupting user experience
                    println("Auto-refresh error: ${e.message}")
                }
            }
        }
    }

    val listState = rememberLazyListState()

    Scaffold(
        bottomBar = {
            BottomNavBar(
                onHomeClick = onHomeClick,
                onSearchClick = onSearchClick,
                onAddRecipe = onAddRecipe,
                onMyRecipeClick = onMyRecipeClick,
                onProfileClick = onProfileClick
            )
        }
    ) { innerPadding ->
        Column(modifier = modifier.fillMaxSize().padding(innerPadding)) {
            Text(
                text = "My Recipes",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(16.dp)
            )
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (errorMessage != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(recipes) { recipe ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    onRecipeClick(recipe)
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    recipe.visibility == "Public" && recipe.isApproved == 0 -> MaterialTheme.colorScheme.primaryContainer // Pending
                                    recipe.visibility == "Public" && recipe.isApproved == 1 -> MaterialTheme.colorScheme.primaryContainer // Approved
                                    recipe.visibility == "Public" && recipe.isApproved == -1 -> MaterialTheme.colorScheme.errorContainer // Declined
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                val imageUrl = if (recipe.imageUrl.startsWith("http://localhost")) {
                                    recipe.imageUrl.replace("http://localhost", "http://10.0.2.2")
                                } else recipe.imageUrl
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = recipe.name,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = recipe.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    when {
                                        recipe.visibility == "Public" && recipe.isApproved == -1 -> {
                                            Text(
                                                text = "Declined by Admin",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                        recipe.visibility == "Public" && recipe.isApproved == 1 -> {
                                            Text(
                                                text = "Approved",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        recipe.visibility.trim().equals("Only Me", ignoreCase = true) -> {
                                            Text(
                                                text = "Only Me",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                        }
                                        recipe.visibility == "Public" && recipe.isApproved == 0 -> {
                                            Text(
                                                text = "Pending Approval",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        else -> {
                                            Text(
                                                text = "Pending Approval",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Servings: ${recipe.servings}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "Cook Time: ${recipe.cookTimeMinutes ?: "-"} min",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                // Edit Button
                                Button(
                                    onClick = {
                                        // If approved, set isApproved to 0
                                        val updatedRecipe = if (recipe.isApproved == 1) recipe.copy(isApproved = 0) else recipe
                                        // Launch RecipeEditActivity instead of AddRecipeActivity
                                        val intent = android.content.Intent(context, com.example.recipeapp.ui.RecipeEditActivity::class.java)
                                        intent.putExtra("recipe_id", updatedRecipe.id)
                                        intent.putExtra("user_id", updatedRecipe.userId)
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Text("Edit")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}