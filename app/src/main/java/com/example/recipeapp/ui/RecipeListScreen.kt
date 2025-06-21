package com.example.recipeapp.ui

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.recipeapp.R

@Composable
fun RecipeListScreen(
    recipes: List<Recipe>,
    onAddRecipe: () -> Unit,
    onRecipeClick: (Recipe) -> Unit
) {
    val expandedStates = remember { mutableStateMapOf<Int, Boolean>() }
    Column(modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .padding(16.dp)) {
        Button(
            onClick = onAddRecipe,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Recipe")
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(recipes) { recipe ->
                val expanded = expandedStates[recipe.id] ?: false
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { expandedStates[recipe.id] = !expanded },
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        if (recipe.imageUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(recipe.imageUri),
                                contentDescription = "Recipe Image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                contentDescription = "Placeholder Image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Text(text = recipe.title)
                        OutlinedButton(
                            onClick = {
                                expandedStates[recipe.id] = !expanded
                            },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text(if (expanded) "Hide Details" else "Show Details")
                        }
                        if (expanded) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Ingredients:")
                            recipe.ingredients.forEach { ing ->
                                Text(text = "- ${ing.name} : ${ing.quantity} ${ing.unit}")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Directions:")
                            Text(text = recipe.directions)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { onRecipeClick(recipe) }) {
                                Text("View Details")
                            }
                        }
                    }
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
        Ingredient("Sugar", "1", "cup")
    )
    val sampleRecipes = listOf(
        Recipe(
            id = 1,
            title = "Pancakes",
            ingredients = sampleIngredients,
            directions = "Mix all ingredients and cook on a skillet."
        ),
        Recipe(
            id = 2,
            title = "Waffles",
            ingredients = sampleIngredients,
            directions = "Mix all ingredients and cook in a waffle iron."
        )
    )
    RecipeListScreen(
        recipes = sampleRecipes,
        onAddRecipe = {},
        onRecipeClick = {}
    )
}
