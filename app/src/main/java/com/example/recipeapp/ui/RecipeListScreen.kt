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

//this are for the ui components
//this is from gemini kasi di ako marunong mag ui lol sabi lng ni gemini gamitin toh -ryan
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun RecipeListScreen(
    recipes: List<Recipe>,
    onAddRecipe: () -> Unit,
    onRecipeClick: (Recipe) -> Unit,
    onHomeClick: () -> Unit = {},
    onPantryClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    var filterSectionExpanded by remember { mutableStateOf(false) }
    var selectedMainIngredient by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedGeneralIngredient by remember { mutableStateOf<String?>(null) }

    // FILTERSS contents
    val mainIngredients = listOf("Chicken", "Beef", "Fish", "Pork", "Vegetarian")
    val categories = listOf("Seafood", "Fried", "Soup", "Salad", "Dessert")
    val generalIngredients = listOf("Eggs", "Milk", "Flour", "Sugar", "Salt")

    // count the active filters para madisplay kung ilang filters ang active
    val numActiveFilters = listOf(selectedMainIngredient, selectedCategory, selectedGeneralIngredient).count { it != null }

    Scaffold(
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onHomeClick,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Home,
                            contentDescription = "Home",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    IconButton(
                        onClick = onAddRecipe,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add Recipe",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    IconButton(
                        onClick = onPantryClick,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = "Pantry",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    IconButton(
                        onClick = onProfileClick,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "User Profile",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        var searchQuery by remember { mutableStateOf("") }
        val listState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()
        val isAtTop = listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = if (isAtTop) 0.dp else 16.dp, // Only add top padding if search/filter is hidden
                    bottom = 16.dp
                )
            ) {
                item {
                    // Search and Filter Section
                    AnimatedVisibility(
                        visible = isAtTop, // Show this section only when scrolled to top
                        enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                        exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Column {
                            // SearchBar
                            SearchBar(
                                query = searchQuery,
                                onQueryChange = { searchQuery = it },
                                onSearch = {},
                                active = false,
                                onActiveChange = {},
                                placeholder = { Text("Search recipes...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(MaterialTheme.shapes.extraLarge),
                                colors = SearchBarDefaults.colors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                )
                            ) {}

                            Spacer(modifier = Modifier.height(8.dp))

                            // Filter Button and Active Filter Count
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End, // Align to end by default
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Display active filters count on the left side
                                if (numActiveFilters > 0) {
                                    Text(
                                        text = "$numActiveFilters Active Filter${if (numActiveFilters > 1) "s" else ""}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .align(Alignment.CenterVertically)
                                            .padding(end = 8.dp) // Space between text and icon
                                    )
                                }
                                IconButton(
                                    onClick = { filterSectionExpanded = !filterSectionExpanded },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.FilterList,
                                        contentDescription = if (filterSectionExpanded) "Hide Filters" else "Show Filters",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            // Animated Filter Options
                            AnimatedVisibility(
                                visible = filterSectionExpanded,
                                enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                                exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                        .clip(MaterialTheme.shapes.medium)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(16.dp)
                                ) {
                                    Text("Main Ingredient", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = spacedBy(8.dp),
                                        verticalArrangement = spacedBy(8.dp)
                                    ) {
                                        mainIngredients.forEach { ingredient ->
                                            FilterChip(
                                                selected = selectedMainIngredient == ingredient,
                                                onClick = {
                                                    selectedMainIngredient = if (selectedMainIngredient == ingredient) null else ingredient
                                                },
                                                label = { Text(ingredient) },
                                                leadingIcon = if (selectedMainIngredient == ingredient) {
                                                    { Icon(Icons.Filled.Done, contentDescription = "Selected", Modifier.size(FilterChipDefaults.IconSize)) }
                                                } else null
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))
                                    HorizontalDivider()
                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text("Category", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = spacedBy(8.dp),
                                        verticalArrangement = spacedBy(8.dp)
                                    ) {
                                        categories.forEach { category ->
                                            FilterChip(
                                                selected = selectedCategory == category,
                                                onClick = {
                                                    selectedCategory = if (selectedCategory == category) null else category
                                                },
                                                label = { Text(category) },
                                                leadingIcon = if (selectedCategory == category) {
                                                    { Icon(Icons.Filled.Done, contentDescription = "Selected", Modifier.size(FilterChipDefaults.IconSize)) }
                                                } else null
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))
                                    HorizontalDivider()
                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text("General Ingredient", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = spacedBy(8.dp),
                                        verticalArrangement = spacedBy(8.dp)
                                    ) {
                                        generalIngredients.forEach { ingredient ->
                                            FilterChip(
                                                selected = selectedGeneralIngredient == ingredient,
                                                onClick = {
                                                    selectedGeneralIngredient = if (selectedGeneralIngredient == ingredient) null else ingredient
                                                },
                                                label = { Text(ingredient) },
                                                leadingIcon = if (selectedGeneralIngredient == ingredient) {
                                                    { Icon(Icons.Filled.Done, contentDescription = "Selected", Modifier.size(FilterChipDefaults.IconSize)) }
                                                } else null
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))
                                    HorizontalDivider()
                                    Spacer(modifier = Modifier.height(16.dp))

                                    OutlinedButton(
                                        onClick = {
                                            selectedMainIngredient = null
                                            selectedCategory = null
                                            selectedGeneralIngredient = null
                                            filterSectionExpanded = false // Collapse after clearing
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Clear All Filters")
                                    }
                                }
                            }
                        }
                    }
                }
                // Filtered recipes
                items(recipes.filter {
                    (it.title.contains(searchQuery, ignoreCase = true) || searchQuery.isBlank()) &&
                            (selectedMainIngredient == null || it.ingredients.any { ing -> ing.name.equals(selectedMainIngredient, ignoreCase = true) }) &&
                            // Adjusted category filter logic based on title content for demonstration
                            (selectedCategory == null || it.title.contains(selectedCategory!!, ignoreCase = true)) &&
                            (selectedGeneralIngredient == null || it.ingredients.any { ing -> ing.name.equals(selectedGeneralIngredient, ignoreCase = true) })
                }) { recipe ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable { onRecipeClick(recipe) }
                            .shadow(6.dp, shape = MaterialTheme.shapes.medium)
                            .clip(MaterialTheme.shapes.medium),
                        elevation = CardDefaults.cardElevation(0.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = spacedBy(10.dp)
                        ) {
                            if (recipe.imageUri != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(recipe.imageUri),
                                    contentDescription = "Recipe Image",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp)
                                        .clip(MaterialTheme.shapes.medium),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                    contentDescription = "Placeholder Image",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp)
                                        .clip(MaterialTheme.shapes.medium),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Text(
                                text = recipe.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }
                    }
                }
            }
            // Floating Action Button for Search when not at top
            AnimatedVisibility(
                visible = !isAtTop,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 24.dp, end = 24.dp)
            ) {
                FloatingActionButton(
                    onClick = {
                        // This will scroll to the top, showing the search bar and filter section
                        coroutineScope.launch {
                            listState.animateScrollToItem(0)
                        }
                        // If you want to navigate to a dedicated search screen instead,
                        // you would call a navigation function here, e.g., onNavigateToSearchScreen()
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = MaterialTheme.shapes.extraLarge,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(Icons.Filled.Search, contentDescription = "Show Search")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecipeListScreenPreview() {
    val sampleIngredients = listOf(
        Ingredient("Flour", "2", "cups"),
        Ingredient("Sugar", "1", "cup"),
        Ingredient("Chicken", "500", "g"),
        Ingredient("Carrots", "2", "pcs"),
        Ingredient("Eggs", "3", ""),
        Ingredient("Beef", "1", "kg")
    )
    val sampleRecipes = listOf(
        Recipe(
            id = 1,
            title = "Pancakes",
            ingredients = listOf(Ingredient("Flour", "2", "cups"), Ingredient("Eggs", "2", "")),
            directions = "Mix all ingredients and cook on a skillet."
        ),

    )
    MaterialTheme { // Wrap in MaterialTheme for preview
        RecipeListScreen(
            recipes = sampleRecipes,
            onAddRecipe = {},
            onRecipeClick = {}
        )
    }
}