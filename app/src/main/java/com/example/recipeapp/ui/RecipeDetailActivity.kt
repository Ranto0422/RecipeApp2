package com.example.recipeapp.ui

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.recipeapp.R
import com.example.recipeapp.model.Ingredient
import com.example.recipeapp.model.Recipe
import com.google.gson.Gson

class RecipeDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val recipeJson = intent.getStringExtra("recipe_json")
        val recipe = recipeJson?.let { Gson().fromJson(it, Recipe::class.java) }
        setContent {
            Box(modifier = Modifier.fillMaxSize()) {
                if (recipe != null) {
                    RecipeDetailScreen(
                        recipe = recipe,
                        contentTopPadding = 56.dp
                    )
                } else {
                    Text("No recipe data available.", modifier = Modifier.padding(top = 56.dp))
                }
                androidx.compose.material3.IconButton(
                    onClick = { finish() },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .systemBarsPadding()
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        }
    }
}

@SuppressLint("DiscouragedApi")
@Composable
fun RecipeDetailScreen(recipe: Recipe, contentTopPadding: Dp = 0.dp) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = contentTopPadding, bottom = 16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        val imageUri = recipe.imageUri
        when {
            imageUri == null -> {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "Placeholder Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentScale = ContentScale.Crop
                )
            }
            imageUri.startsWith("android.resource://") -> {
                val resName = imageUri.substringAfterLast("/")
                val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
                if (resId != 0) {
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = "Recipe Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "Placeholder Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            else -> {
                val cleanUri = imageUri.trim('"')
                val parsedUri = try { Uri.parse(cleanUri) } catch (e: Exception) { null }
                if (parsedUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = parsedUri,
                            error = painterResource(id = R.drawable.ic_launcher_foreground),
                            placeholder = painterResource(id = R.drawable.ic_launcher_foreground)
                        ),
                        contentDescription = "Recipe Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "Placeholder Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = recipe.title, style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Ingredients:", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(8.dp)) {
                recipe.ingredients.forEach { ing ->
                    Text(text = "- ${ing.name}: ${ing.quantity} ${ing.unit}")
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Directions:", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = recipe.directions)
    }
}

@Preview(showBackground = true)
@Composable
fun RecipeDetailScreenPreview() {
    val sampleIngredients = listOf(
        Ingredient("Flour", "2", "cups"),
        Ingredient("Sugar", "1", "cup")
    )
    val sampleRecipe = Recipe(
        id = 1,
        title = "Pancakes",
        ingredients = sampleIngredients,
        directions = "Mix all ingredients and cook on a skillet."
        , imageUri = "android.resource://com.example.recipeapp/drawable/sample_pancake"
    )
    RecipeDetailScreen(recipe = sampleRecipe)
}