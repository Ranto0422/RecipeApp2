package com.example.recipeapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.recipeapp.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import org.json.JSONArray
import com.example.recipeapp.model.DummyRecipe

@Composable
fun RecipeDetailScreen(recipe: DummyRecipe, isUserRecipe: Boolean = false, onBack: (() -> Unit)? = null) {
    var loadedRecipe by remember { mutableStateOf<DummyRecipe?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(recipe.id, isUserRecipe, recipe.userId) {
        isLoading = true
        try {
            val response = withContext(Dispatchers.IO) {
                if (isUserRecipe && recipe.userId != null) {
                    URL("http://10.0.2.2/MyRecipeAppRestApi/get_user_recipes.php?userId=${recipe.userId}").readText()
                } else {
                    URL("https://dummyjson.com/recipes/${recipe.id}").readText()
                }
            }
            val obj = if (isUserRecipe && recipe.userId != null) {
                val arr = JSONObject(response).getJSONArray("recipes")
                (0 until arr.length()).map { arr.getJSONObject(it) }.find { it.getInt("id") == recipe.id }
            } else {
                JSONObject(response)
            }
            if (obj != null) {
                val ingredientsList = if (isUserRecipe && recipe.userId != null) {
                    try {
                        val arr = JSONArray(obj.getString("ingredients"))
                        List(arr.length()) { i ->
                            val ingObj = arr.getJSONObject(i)
                            val name = ingObj.optString("name", "")
                            val quantity = ingObj.optString("quantity", "")
                            val unit = ingObj.optString("unit", "")
                            listOf(name, quantity, unit).filter { it.isNotEmpty() }.joinToString(" ")
                        }
                    } catch (e: Exception) { emptyList() }
                } else {
                    obj.getJSONArray("ingredients").toStringList()
                }
                val instructionsList = if (isUserRecipe && recipe.userId != null) {
                    listOf(obj.optString("instructions", ""))
                } else {
                    obj.getJSONArray("instructions").toStringList()
                }
                var imageUrl = obj.optString("image", "")
                if (isUserRecipe && imageUrl.startsWith("http://localhost")) {
                    imageUrl = imageUrl.replace("http://localhost", "http://10.0.2.2")
                }
                val tagsList = if (isUserRecipe && recipe.userId != null) {
                    obj.optString("tags", "").split(",").map { it.trim() }.filter { it.isNotEmpty() }
                } else {
                    obj.optJSONArrayOrNull("tags")?.toStringList() ?: emptyList()
                }
                loadedRecipe = DummyRecipe(
                    id = obj.getInt("id"),
                    name = obj.getString("name"),
                    ingredients = ingredientsList,
                    instructions = instructionsList,
                    prepTimeMinutes = obj.optIntOrNull("prepTimeMinutes"),
                    cookTimeMinutes = obj.optIntOrNull("cookTimeMinutes"),
                    servings = obj.optIntOrNull("servings"),
                    difficulty = obj.optStringOrNull("difficulty"),
                    cuisine = obj.optStringOrNull("cuisine"),
                    caloriesPerServing = obj.optIntOrNull("caloriesPerServing"),
                    tags = tagsList,
                    userId = obj.optIntOrNull("userId"),
                    image = imageUrl,
                    rating = obj.optDoubleOrNull("rating"),
                    reviewCount = obj.optIntOrNull("reviewCount"),
                    mealType = null,
                    isPublic = true,
                    isApproved = true
                )
                errorMessage = null
            } else {
                errorMessage = "Recipe not found."
            }
        } catch (e: Exception) {
            errorMessage = "Failed to load recipe details.\n${e.message}"
        } finally {
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            errorMessage != null -> Text(errorMessage!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
            loadedRecipe != null -> {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Top App Bar with Back Button and Title
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(top = 8.dp, start = 4.dp, end = 8.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { onBack?.invoke() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            loadedRecipe!!.name,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 2,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Main Content
                    androidx.compose.foundation.rememberScrollState().let { scrollState ->
                        Column(
                            modifier = Modifier
                                .verticalScroll(scrollState)
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            loadedRecipe!!.image?.let { img ->
                                Image(
                                    painter = rememberAsyncImagePainter(img),
                                    contentDescription = loadedRecipe!!.name,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(220.dp)
                                        .clip(MaterialTheme.shapes.medium),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(Modifier.height(16.dp))
                            }
                            // Chips for cuisine, difficulty, meal type, and tags
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                loadedRecipe!!.cuisine?.let {
                                    if (it.isNotEmpty()) androidx.compose.material3.AssistChip(
                                        onClick = {},
                                        label = { Text(it) },
                                        shape = MaterialTheme.shapes.small
                                    )
                                }
                                loadedRecipe!!.difficulty?.let {
                                    if (it.isNotEmpty()) androidx.compose.material3.AssistChip(
                                        onClick = {},
                                        label = { Text(it) },
                                        shape = MaterialTheme.shapes.small
                                    )
                                }
                                loadedRecipe!!.mealType?.firstOrNull()?.let {
                                    if (it.isNotEmpty()) androidx.compose.material3.AssistChip(
                                        onClick = {},
                                        label = { Text(it) },
                                        shape = MaterialTheme.shapes.small
                                    )
                                }
                                // Show tags as chips
                                loadedRecipe!!.tags?.forEach {
                                    val tagText = it.trim().replace("[", "").replace("]", "").replace("\"", "")
                                    if (tagText.isNotEmpty()) androidx.compose.material3.AssistChip(
                                        onClick = {},
                                        label = { Text(tagText) },
                                        shape = MaterialTheme.shapes.small
                                    )
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            // Info Row
                            Row(
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                InfoColumn("Prep", "${loadedRecipe!!.prepTimeMinutes ?: "-"} min")
                                InfoColumn("Cook", "${loadedRecipe!!.cookTimeMinutes ?: "-"} min")
                                InfoColumn("Serves", "${loadedRecipe!!.servings ?: "-"}")
                                InfoColumn("Cal", "${loadedRecipe!!.caloriesPerServing ?: "-"}")
                            }
                            Spacer(Modifier.height(16.dp))
                            // Rating and Reviews
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                loadedRecipe!!.rating?.let {
                                    androidx.compose.material3.AssistChip(
                                        onClick = {},
                                        label = { Text("★ $it") },
                                        shape = MaterialTheme.shapes.small
                                    )
                                }
                                loadedRecipe!!.reviewCount?.let {
                                    Text("($it reviews)", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            Spacer(Modifier.height(20.dp))
                            // Ingredients
                            Text("Ingredients", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(8.dp))
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                loadedRecipe!!.ingredients.forEach {
                                    Text("• $it", style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                            Spacer(Modifier.height(20.dp))
                            // Instructions
                            Text("Instructions", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(8.dp))
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                loadedRecipe!!.instructions.forEachIndexed { i, step ->
                                    Text("${i + 1}. $step", style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                            Spacer(Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
