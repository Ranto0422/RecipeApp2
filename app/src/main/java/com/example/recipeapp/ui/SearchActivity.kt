package com.example.recipeapp.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.ExperimentalFoundationApi // For FlowRow
import androidx.compose.foundation.layout.Arrangement.spacedBy // For FlowRow
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.platform.LocalContext

import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.recipeapp.R
import com.example.recipeapp.model.DummyRecipe
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.clickable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.graphics.Shape
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import com.example.recipeapp.util.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items


class SearchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                SearchScreen()
            }
        }
    }
}

@SuppressLint("ContextCastToActivity")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun SearchScreen(onRecipeClick: (DummyRecipe) -> Unit = {}, onBack: () -> Unit = {}) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<DummyRecipe>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            isLoading = true
            errorMessage = null
            try {
                val response = withContext(Dispatchers.IO) {
                    URL("https://dummyjson.com/recipes/search?q=${searchQuery}").readText()
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
                searchResults = list
                if (list.isEmpty()) {
                    errorMessage = "No recipes found."
                }
            } catch (e: Exception) {
                errorMessage = "Failed to load recipes."
                searchResults = emptyList()
            } finally {
                isLoading = false
            }
        } else {
            searchResults = emptyList()
            errorMessage = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onBack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search recipes...") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = MaterialTheme.shapes.extraLarge
            )
        }
        Spacer(Modifier.height(16.dp))
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (errorMessage != null) {
            Text(errorMessage!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(searchResults) { recipe: DummyRecipe ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable { onRecipeClick(recipe) },
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(modifier = Modifier.padding(8.dp)) {
                            recipe.image?.let { img: String ->
                                Image(
                                    painter = rememberAsyncImagePainter(img),
                                    contentDescription = recipe.name,
                                    modifier = Modifier.size(64.dp).clip(MaterialTheme.shapes.medium),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(recipe.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                recipe.cuisine?.let { cuisine -> Text("Cuisine: $cuisine", style = MaterialTheme.typography.bodySmall) }
                                recipe.difficulty?.let { diff -> Text("Difficulty: $diff", style = MaterialTheme.typography.bodySmall) }
                                recipe.tags?.let { tags -> Text("Tags: ${tags.joinToString(", ")}", style = MaterialTheme.typography.bodySmall) }
                            }
                        }
                    }
                }
            }
        }
    }
}



@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun SearchScreenPreview() {
    MaterialTheme {
        SearchScreen()
    }
}
