package com.example.recipeapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
fun RecipeDetailScreen(recipeId: Int, onBack: (() -> Unit)? = null) {
    var recipe by remember { mutableStateOf<DummyRecipe?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(recipeId) {
        isLoading = true
        try {
            val response = withContext(Dispatchers.IO) {
                URL("https://dummyjson.com/recipes/$recipeId").readText()
            }
            val obj = JSONObject(response)
            recipe = DummyRecipe(
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
        } catch (e: Exception) {
            errorMessage = "Failed to load recipe."
        } finally {
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            errorMessage != null -> Text(errorMessage!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
            recipe != null -> {
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
                            recipe!!.name,
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
                            recipe!!.image?.let { img ->
                                Image(
                                    painter = rememberAsyncImagePainter(img),
                                    contentDescription = recipe!!.name,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(220.dp)
                                        .clip(MaterialTheme.shapes.medium),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(Modifier.height(16.dp))
                            }
                            // Chips for cuisine, difficulty, meal type
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                recipe!!.cuisine?.let {
                                    androidx.compose.material3.AssistChip(
                                        onClick = {},
                                        label = { Text(it) },
                                        shape = MaterialTheme.shapes.small
                                    )
                                }
                                recipe!!.difficulty?.let {
                                    androidx.compose.material3.AssistChip(
                                        onClick = {},
                                        label = { Text(it) },
                                        shape = MaterialTheme.shapes.small
                                    )
                                }
                                recipe!!.mealType?.firstOrNull()?.let {
                                    androidx.compose.material3.AssistChip(
                                        onClick = {},
                                        label = { Text(it) },
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
                                InfoColumn("Prep", "${recipe!!.prepTimeMinutes ?: "-"} min")
                                InfoColumn("Cook", "${recipe!!.cookTimeMinutes ?: "-"} min")
                                InfoColumn("Serves", "${recipe!!.servings ?: "-"}")
                                InfoColumn("Cal", "${recipe!!.caloriesPerServing ?: "-"}")
                            }
                            Spacer(Modifier.height(16.dp))
                            // Rating and Reviews
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                recipe!!.rating?.let {
                                    androidx.compose.material3.AssistChip(
                                        onClick = {},
                                        label = { Text("★ $it") },
                                        shape = MaterialTheme.shapes.small
                                    )
                                }
                                recipe!!.reviewCount?.let {
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
                                recipe!!.ingredients.forEach {
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
                                recipe!!.instructions.forEachIndexed { i, step ->
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
