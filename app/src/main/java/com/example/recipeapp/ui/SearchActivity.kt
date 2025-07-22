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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import com.example.recipeapp.util.*


data class Meal(
    val idMeal: String,
    val strMeal: String,
    val strCategory: String?,
    val strArea: String?,
    val strInstructions: String?,
    val strMealThumb: String?,
    val strTags: String?,
    val strYoutube: String?,
    val ingredients: List<String>,
    val measures: List<String>
)

class SearchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Apply your app's theme here if you have one, e.g., MyAppTheme { ... }
            MaterialTheme { // Using MaterialTheme for basic theming
                SearchScreen()
            }
        }
    }
}

@SuppressLint("ContextCastToActivity")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun SearchScreen() {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var filterSectionExpanded by rememberSaveable { mutableStateOf(false) }
    var selectedMainIngredient by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedCategory by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedDietaryRestriction by rememberSaveable { mutableStateOf<String?>(null) }
    var searchResults by remember { mutableStateOf<List<Meal>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val mainIngredients = listOf("Chicken", "Beef", "Fish", "Pork", "Vegetarian")
    val categories = listOf("Breakfast", "Lunch", "Dinner", "Dessert", "Snack")
    val dietaryRestrictions = listOf("Gluten-Free", "Dairy-Free", "Vegan", "Keto")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.systemBars.asPaddingValues())
            .padding(16.dp)
    ) {
        val activity = (LocalContext.current as? android.app.Activity)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { activity?.finish() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search recipes...") },
                modifier = Modifier
                    .weight(1f),
                singleLine = true,
                shape = MaterialTheme.shapes.extraLarge
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp)
        ) {
            Text("Main Ingredient", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = spacedBy(8.dp), verticalArrangement = spacedBy(8.dp)) {
                mainIngredients.forEach { ingredient ->
                    FilterChip(
                        selected = selectedMainIngredient == ingredient,
                        onClick = { selectedMainIngredient = if (selectedMainIngredient == ingredient) null else ingredient },
                        label = { Text(ingredient) },
                        leadingIcon = if (selectedMainIngredient == ingredient) {
                            { Icon(Icons.Filled.Done, contentDescription = null) }
                        } else null
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("Category", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = spacedBy(8.dp), verticalArrangement = spacedBy(8.dp)) {
                categories.forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = if (selectedCategory == category) null else category },
                        label = { Text(category) },
                        leadingIcon = if (selectedCategory == category) {
                            { Icon(Icons.Filled.Done, contentDescription = null) }
                        } else null
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("Dietary Restriction", style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = spacedBy(8.dp), verticalArrangement = spacedBy(8.dp)) {
                dietaryRestrictions.forEach { restriction ->
                    FilterChip(
                        selected = selectedDietaryRestriction == restriction,
                        onClick = { selectedDietaryRestriction = if (selectedDietaryRestriction == restriction) null else restriction },
                        label = { Text(restriction) },
                        leadingIcon = if (selectedDietaryRestriction == restriction) {
                            { Icon(Icons.Filled.Done, contentDescription = null) }
                        } else null
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = {
                    selectedMainIngredient = null
                    selectedCategory = null
                    selectedDietaryRestriction = null
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Clear All Filters")
            }
        }

        // TODO: Replacez with actual search results list
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (errorMessage != null) {
            Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
        } else if (searchResults.isNotEmpty()) {
            Text("Results:", style = MaterialTheme.typography.titleMedium)
            Column {
                searchResults.forEach { meal ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(modifier = Modifier.padding(8.dp)) {
                            meal.strMealThumb?.let { thumb ->
                                AsyncImage(
                                    model = thumb,
                                    contentDescription = meal.strMeal,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(meal.strMeal, style = MaterialTheme.typography.titleMedium)
                                meal.strCategory?.let { Text("Category: $it", style = MaterialTheme.typography.bodySmall) }
                                meal.strArea?.let { Text("Area: $it", style = MaterialTheme.typography.bodySmall) }
                                if (meal.ingredients.isNotEmpty()) {
                                    Text("Ingredients: " + meal.ingredients.joinToString(", "), style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        } else if (searchQuery.isNotBlank()) {
            Text("No results found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }

    // Modified fetchMeals function
    fun fetchMeals(
        context: android.content.Context, // Add Context as a parameter
        query: String,
        mainIngredient: String?,
        category: String?,
        dietaryRestriction: String?,
        onResults: (List<Meal>) -> Unit,
        onError: (String?) -> Unit,
        onLoading: (Boolean) -> Unit
    ) {
        if (query.isBlank() && mainIngredient == null && category == null && dietaryRestriction == null) {
            onResults(emptyList())
            return
        }
        onLoading(true)
        onError(null)

        // Construct the base URL
        var apiUrl = "https://www.themealdb.com/api/json/v1/1/search.php?s=$query"
        isLoading = true
        errorMessage = null
        (context as? ComponentActivity)?.lifecycleScope?.launch {
            try {
                val url = "https://www.themealdb.com/api/json/v1/1/search.php?s=" + query
                val response = withContext(Dispatchers.IO) { URL(url).readText() }
                val json = JSONObject(response)
                val meals = json.optJSONArray("meals")
                val results = mutableListOf<Meal>()
                if (meals != null) {
                    for (i in 0 until meals.length()) {
                        val meal = meals.getJSONObject(i)
                        val ingredients = mutableListOf<String>()
                        val measures = mutableListOf<String>()
                        for (j in 1..20) {
                            val ingredient = meal.optString("strIngredient$j").orEmpty().trim()
                            val measure = meal.optString("strMeasure$j").orEmpty().trim()
                            if (ingredient.isNotEmpty() && ingredient != "null") {
                                ingredients.add(ingredient)
                                measures.add(measure)
                            }
                        }
                        results.add(
                            Meal(
                                idMeal = meal.getString("idMeal"),
                                strMeal = meal.getString("strMeal"),
                                strCategory = meal.optString("strCategory"),
                                strArea = meal.optString("strArea"),
                                strInstructions = meal.optString("strInstructions"),
                                strMealThumb = meal.optString("strMealThumb"),
                                strTags = meal.optString("strTags"),
                                strYoutube = meal.optString("strYoutube"),
                                ingredients = ingredients,
                                measures = measures
                            )
                        )
                    }
                }
                searchResults = results
            } catch (e: Exception) {
                errorMessage = "Error fetching meals."
                searchResults = emptyList()
            } finally {
                isLoading = false
            }
        }
    }
}

@Composable
fun AsyncImage(model: String, contentDescription: String, modifier: Modifier) {
    TODO("Not yet implemented")
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun SearchScreenPreview() {
    MaterialTheme { // Wrap in MaterialTheme for preview
        SearchScreen()
    }
}
