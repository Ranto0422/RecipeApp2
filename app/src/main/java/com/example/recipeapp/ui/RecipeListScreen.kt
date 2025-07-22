package com.example.recipeapp.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateMapOf
import com.example.recipeapp.model.Recipe
import com.example.recipeapp.model.Ingredient
import com.example.recipeapp.model.DummyRecipe
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.recipeapp.R
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Alignment
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import android.content.Intent
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import com.example.recipeapp.util.*


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun RecipeListScreen(
    onAddRecipe: () -> Unit,
    onRecipeClick: (DummyRecipe) -> Unit,
    onLuckyClick: () -> Unit, // <-- im Feeling Lucky
    isLuckyLoading: Boolean = false,
    luckyError: String? = null,
    onHomeClick: () -> Unit = {},
    onPantryClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onSearchClick: () -> Unit = {} // <-- onSearchClick handler
) {
    val context = LocalContext.current
    var recipes by remember { mutableStateOf<List<DummyRecipe>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLuckyLoadingState by remember { mutableStateOf(false) }
    var luckyErrorState by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Fetch recipes from DummyJSON API
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

    Scaffold(
        bottomBar = {
            BottomNavBar(
                onHomeClick = onHomeClick,
                onSearchClick = onSearchClick,
                onAddRecipe = onAddRecipe,
                onPantryClick = onPantryClick,
                onProfileClick = onProfileClick
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (errorMessage != null) {
                Text(errorMessage!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(recipes) { recipe ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .clickable { onRecipeClick(recipe) },
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Row(modifier = Modifier.padding(8.dp)) {
                                    recipe.image?.let { img ->
                                        Image(
                                            painter = rememberAsyncImagePainter(img),
                                            contentDescription = recipe.name,
                                            modifier = Modifier.size(64.dp).clip(MaterialTheme.shapes.medium),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(recipe.name, style = MaterialTheme.typography.titleMedium)
                                        recipe.cuisine?.let { Text("Cuisine: $it", style = MaterialTheme.typography.bodySmall) }
                                        recipe.difficulty?.let { Text("Difficulty: $it", style = MaterialTheme.typography.bodySmall) }
                                        recipe.tags?.let { Text("Tags: ${it.joinToString(", ")}", style = MaterialTheme.typography.bodySmall) }
                                    }
                                }
                            }
                        }
                    }
                    // IM feeeling lucky button
                    FloatingActionButton(
                        onClick = onLuckyClick,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 80.dp, end = 24.dp)
                    ) {
                        if (isLuckyLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            Text("I'm Feeling Lucky")
                        }
                    }
                    luckyError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 140.dp))
                    }
                }
            }
        }
    }
}
