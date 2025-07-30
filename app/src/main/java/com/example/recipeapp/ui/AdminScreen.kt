package com.example.recipeapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.recipeapp.model.DummyRecipe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    onBack: () -> Unit,
    onRecipeDetailNavigate: (Int, Int) -> Unit // Callback for navigating to recipe detail
) {
    var pendingRecipes by remember { mutableStateOf<List<DummyRecipe>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Fetch pending recipes from backend
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val response = withContext(Dispatchers.IO) {
                URL("http://10.0.2.2/MyRecipeAppRestApi/get_pending_recipes.php").readText()
            }
            val json = JSONObject(response)
            if (!json.optBoolean("success", false)) {
                errorMessage = json.optString("error", "Failed to load pending recipes.")
            } else {
                val arr = json.getJSONArray("recipes")
                val list = mutableListOf<DummyRecipe>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        DummyRecipe(
                            id = obj.optInt("id"),
                            name = obj.optString("name"),
                            ingredients = try { JSONArray(obj.optString("ingredients", "[]")).let { ingArr -> List(ingArr.length()) { ingArr.getString(it) } } } catch (_: Exception) { emptyList() },
                            instructions = try { JSONArray(obj.optString("instructions", "[]")).let { instArr -> List(instArr.length()) { instArr.getString(it) } } } catch (_: Exception) { emptyList() },
                            prepTimeMinutes = obj.optInt("prepTimeMinutes"),
                            cookTimeMinutes = obj.optInt("cookTimeMinutes"),
                            servings = obj.optInt("servings"),
                            difficulty = obj.optString("difficulty"),
                            cuisine = obj.optString("cuisine"),
                            caloriesPerServing = null,
                            tags = try { JSONArray(obj.optString("tags", "[]")).let { tagArr -> List(tagArr.length()) { tagArr.getString(it) } } } catch (_: Exception) { emptyList() },
                            userId = obj.optInt("userId"),
                            image = obj.optString("image"),
                            rating = null,
                            reviewCount = null,
                            mealType = null,
                            isPublic = obj.optString("visibility") == "Public",
                            isApproved = obj.optInt("isApproved", 0) == 1
                        )
                    )
                }
                pendingRecipes = list
                errorMessage = null
            }
        } catch (e: Exception) {
            errorMessage = "Failed to load pending recipes.\n${e.message}"
        } finally {
            isLoading = false
        }
    }

    // Track loading and error state for each recipe
    var recipeLoadingStates by remember { mutableStateOf(mapOf<Int, Boolean>()) }
    var recipeErrorStates by remember { mutableStateOf(mapOf<Int, String?>()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Approval") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (errorMessage != null) {
                Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
            } else if (pendingRecipes.isEmpty()) {
                Text("No recipes pending approval.")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(pendingRecipes) { recipe: DummyRecipe ->
                        val isLoadingRecipe = recipeLoadingStates[recipe.id] == true
                        val errorRecipe = recipeErrorStates[recipe.id]
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable {
                                    onRecipeDetailNavigate(recipe.id, recipe.userId ?: -1)
                                },
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(modifier = Modifier.padding(16.dp)) {
                                recipe.image?.let { img: String ->
                                    Image(
                                        painter = rememberAsyncImagePainter(img),
                                        contentDescription = recipe.name,
                                        modifier = Modifier.size(64.dp).clip(MaterialTheme.shapes.medium),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(Modifier.width(12.dp))
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(recipe.name, style = MaterialTheme.typography.titleMedium)
                                    Text("Servings: ${recipe.servings}", style = MaterialTheme.typography.bodySmall)
                                    Text("Cook Time: ${recipe.cookTimeMinutes} min", style = MaterialTheme.typography.bodySmall)
                                    Text("Prep Time: ${recipe.prepTimeMinutes ?: "-"} min", style = MaterialTheme.typography.bodySmall)
                                    Text("Cuisine: ${recipe.cuisine ?: "-"}", style = MaterialTheme.typography.bodySmall)
                                    Text("Difficulty: ${recipe.difficulty ?: "-"}", style = MaterialTheme.typography.bodySmall)
                                    Text("Tags: ${recipe.tags?.joinToString(", ") ?: "-"}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            Row(modifier = Modifier.padding(16.dp)) {
                                Button(onClick = {
                                    scope.launch {
                                        recipeLoadingStates = recipeLoadingStates + (recipe.id to true)
                                        recipeErrorStates = recipeErrorStates - recipe.id

                                        android.util.Log.d("AdminScreen", "Starting approve request for recipe ${recipe.id}")

                                        val client = OkHttpClient()
                                        val body = FormBody.Builder().add("recipeId", recipe.id.toString()).build()
                                        val request = Request.Builder()
                                            .url("http://10.0.2.2/MyRecipeAppRestApi/approve_recipe.php")
                                            .post(body)
                                            .build()

                                        try {
                                            android.util.Log.d("AdminScreen", "Sending request to: ${request.url}")
                                            val response = withContext(Dispatchers.IO) { client.newCall(request).execute() }
                                            val responseBody = response.body?.string()

                                            android.util.Log.d("AdminScreen", "Response code: ${response.code}")
                                            android.util.Log.d("AdminScreen", "Response successful: ${response.isSuccessful}")
                                            android.util.Log.d("AdminScreen", "Raw response: '$responseBody'")

                                            if (response.isSuccessful && responseBody != null) {
                                                try {
                                                    val json = org.json.JSONObject(responseBody)
                                                    android.util.Log.d("AdminScreen", "Parsed JSON success: ${json.optBoolean("success", false)}")
                                                    android.util.Log.d("AdminScreen", "JSON error field: '${json.optString("error", "NOT_FOUND")}'")

                                                    if (json.optBoolean("success", false)) {
                                                        android.util.Log.d("AdminScreen", "Recipe ${recipe.id} approved successfully")
                                                        pendingRecipes = pendingRecipes.filter { it.id != recipe.id }
                                                        recipeErrorStates = recipeErrorStates - recipe.id
                                                    } else {
                                                        val errorMsg = json.optString("error", "Unknown error occurred")
                                                        android.util.Log.d("AdminScreen", "Server returned error: '$errorMsg'")
                                                        val displayErrorMsg = if (errorMsg.isBlank() || errorMsg.equals("null", ignoreCase = true) || errorMsg.equals("Unknown error", ignoreCase = true)) "An unexpected error occurred. Please try again." else errorMsg
                                                        recipeErrorStates = recipeErrorStates + (recipe.id to displayErrorMsg)
                                                    }
                                                } catch (jsonException: Exception) {
                                                    android.util.Log.e("AdminScreen", "JSON parsing failed: ${jsonException.message}")
                                                    android.util.Log.e("AdminScreen", "Response body was: '$responseBody'")
                                                    recipeErrorStates = recipeErrorStates + (recipe.id to "Invalid response from server")
                                                }
                                            } else {
                                                android.util.Log.e("AdminScreen", "HTTP request failed. Code: ${response.code}")
                                                recipeErrorStates = recipeErrorStates + (recipe.id to "Server request failed (${response.code})")
                                            }
                                        } catch (e: Exception) {
                                            android.util.Log.e("AdminScreen", "Network exception: ${e.javaClass.simpleName}: ${e.message}", e)
                                            recipeErrorStates = recipeErrorStates + (recipe.id to "Network error: ${e.message ?: "Unknown error"}")
                                        } finally {
                                            recipeLoadingStates = recipeLoadingStates + (recipe.id to false)
                                        }
                                    }
                                }, enabled = !isLoadingRecipe) {
                                    if (isLoadingRecipe) CircularProgressIndicator(modifier = Modifier.size(16.dp)) else Text("Approve")
                                }
                                Spacer(Modifier.width(8.dp))
                                Button(onClick = {
                                    scope.launch {
                                        recipeLoadingStates = recipeLoadingStates + (recipe.id to true)
                                        recipeErrorStates = recipeErrorStates - recipe.id
                                        val client = OkHttpClient()
                                        val body = FormBody.Builder().add("recipeId", recipe.id.toString()).build()
                                        val request = Request.Builder()
                                            .url("http://10.0.2.2/MyRecipeAppRestApi/decline_recipe.php")
                                            .post(body)
                                            .build()
                                        try {
                                            //Your approve button's network request will run on a background thread, as required by Android.
                                            // eto sinabi ni ChatGPT i used this to avoid network on main thread exception and for the approve and decline buttons to work properly
                                            val response = withContext(Dispatchers.IO) { client.newCall(request).execute() }
                                            // used for approve and decline buttons in admin screen

                                            val responseBody = response.body?.string()
                                            android.util.Log.d("AdminScreen", "Approve/Decline response: $responseBody")
                                            if (response.isSuccessful && responseBody != null) {
                                                val json = org.json.JSONObject(responseBody)
                                                if (json.optBoolean("success", false)) {
                                                    pendingRecipes = pendingRecipes.filter { it.id != recipe.id }
                                                    recipeErrorStates = recipeErrorStates - recipe.id // Clear error on success
                                                } else {
                                                    val errorRaw = if (json.has("error")) json.get("error") else null
                                                    val errorMsg = when {
                                                        errorRaw == null -> "An unexpected error occurred. Please try again."
                                                        errorRaw is String && (errorRaw.isBlank() || errorRaw.equals("null", ignoreCase = true) || errorRaw.equals("Unknown error", ignoreCase = true)) -> "An unexpected error occurred. Please try again."
                                                        errorRaw is String -> errorRaw
                                                        else -> errorRaw.toString()
                                                    }
                                                    val displayErrorMsg = errorMsg
                                                    recipeErrorStates = recipeErrorStates + (recipe.id to displayErrorMsg)
                                                }
                                            } else {
                                                recipeErrorStates = recipeErrorStates + (recipe.id to "Failed to decline recipe.")
                                            }
                                        } catch (e: Exception) {
                                            recipeErrorStates = recipeErrorStates + (recipe.id to "Error: ${e.message}")
                                        } finally {
                                            recipeLoadingStates = recipeLoadingStates + (recipe.id to false)
                                        }
                                    }
                                }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error), enabled = !isLoadingRecipe) {
                                    if (isLoadingRecipe) CircularProgressIndicator(modifier = Modifier.size(16.dp)) else Text("Decline")
                                }
                            }
                            errorRecipe?.let {
                                val showError = it != null && it.isNotBlank() && !it.equals("null", ignoreCase = true)
                                if (showError) {
                                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
