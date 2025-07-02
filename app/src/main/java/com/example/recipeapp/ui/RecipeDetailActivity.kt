package com.example.recipeapp.ui

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.lazy.items

class RecipeDetailActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val recipeJson = intent.getStringExtra("recipe_json")
        val recipe = recipeJson?.let { Gson().fromJson(it, Recipe::class.java) }
        setContent {
            MaterialTheme { // Ensure MaterialTheme is applied for colors/typography
                if (recipe != null) {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = { Text("Recipe Details", style = MaterialTheme.typography.titleLarge) },
                                navigationIcon = {
                                    IconButton(onClick = { finish() }) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Back"
                                        )
                                    }
                                }
                            )
                        }
                    ) { innerPadding ->
                        // Apply innerPadding here for the whole screen content
                        RecipeDetailScreen(
                            recipe = recipe,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                } else {
                    Text("No recipe data available.", modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}

@SuppressLint("DiscouragedApi")
@Composable
fun RecipeDetailScreen(recipe: Recipe, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    // Using LazyColumn to make the entire screen scrollable if content overflows
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding(), // Add navigation bars padding for full screen content
        horizontalAlignment = Alignment.Start,
        contentPadding = PaddingValues(bottom = 16.dp) // Add some bottom padding
    ) {
        item {
            // Recipe Image with a slight gradient overlay for modern look
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp) // Make the image taller for a hero section feel
            ) {
                val imagePainter = when {
                    recipe.imageUri == null -> painterResource(id = R.drawable.ic_launcher_foreground)
                    recipe.imageUri.startsWith("android.resource://") -> {
                        val resName = recipe.imageUri.substringAfterLast("/")
                        val resId = context.resources.getIdentifier(resName, "drawable", context.packageName)
                        if (resId != 0) painterResource(id = resId) else painterResource(id = R.drawable.ic_launcher_foreground)
                    }
                    else -> {
                        val cleanUri = recipe.imageUri.trim('"')
                        val parsedUri = try { Uri.parse(cleanUri) } catch (e: Exception) { null }
                        if (parsedUri != null) {
                            rememberAsyncImagePainter(
                                model = parsedUri,
                                error = painterResource(id = R.drawable.ic_launcher_foreground),
                                placeholder = painterResource(id = R.drawable.ic_launcher_foreground)
                            )
                        } else {
                            painterResource(id = R.drawable.ic_launcher_foreground)
                        }
                    }
                }

                Image(
                    painter = imagePainter,
                    contentDescription = "Recipe Image",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)), // Rounded corners at the bottom
                    contentScale = ContentScale.Crop
                )

                // Optional: Gradient Overlay for sleek look
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.1f), // Subtle dark gradient
                                    Color.Black.copy(alpha = 0.3f)
                                ),
                                startY = 0f,
                                endY = Float.POSITIVE_INFINITY
                            )
                        )
                )
            }
        }

        item {
            // Recipe Title
            Text(
                text = recipe.title,
                style = MaterialTheme.typography.displaySmall, // Larger, more prominent title
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp) // Increased vertical padding
            )
        }

        // Ingredients Section
        item {
            Text(
                text = "Ingredients:",
                style = MaterialTheme.typography.titleLarge, // Clear section title
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = MaterialTheme.shapes.medium, // Rounded corners for card
                tonalElevation = 4.dp, // Subtle elevation
                color = MaterialTheme.colorScheme.surfaceVariant // Use surfaceVariant for contrast
            ) {
                Column(modifier = Modifier.padding(16.dp)) { // Increased internal padding
                    recipe.ingredients.forEach { ing ->
                        Text(
                            text = "- ${ing.name}: ${ing.quantity} ${ing.unit}",
                            style = MaterialTheme.typography.bodyLarge, // Larger text for readability
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 4.dp) // Spacing between ingredients
                        )
                    }
                }
            }
        }

        // Directions Section
        item {
            Text(
                text = "Directions:",
                style = MaterialTheme.typography.titleLarge, // Clear section title
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 4.dp,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = recipe.directions,
                    style = MaterialTheme.typography.bodyLarge, // Larger text for readability
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp) // Increased internal padding
                )
            }
        }
    }
}




@Preview(showBackground = true)
@Composable
fun RecipeDetailScreenPreview() {
    val sampleIngredients = listOf(
        Ingredient("Flour", "2", "cups"),
        Ingredient("Sugar", "1", "cup"),
        Ingredient("Milk", "1", "cup"),
        Ingredient("Eggs", "2", ""),
        Ingredient("Baking Powder", "1", "tsp"),
        Ingredient("Salt", "0.5", "tsp"),
        Ingredient("Butter", "2", "tbsp")
    )
    val longDirections = """
        give me sample recipe with long directions for a pankace recipe
        Step 1: In a large bowl, whisk together the dry ingredients: flour, sugar, baking powder, and salt.
        Step 2: In a separate bowl, whisk together the wet ingredients: milk, egg, and melted butter.
        Step 3: Pour the wet ingredients into the dry ingredients and mix until just combined. Be careful not to overmix; a few lumps are okay.
        Step 4: Heat a lightly oiled griddle or frying pan over medium-high heat.
        Step 5: Pour or scoop the batter onto the griddle, using approximately 1/4 cup for each pancake.
        Step 6: Cook for 2 to 3 minutes per side, until golden brown and cooked through.
        Step 7: Serve immediately with your favorite toppings like syrup, fruit, or whipped cream. Enjoy your delicious homemade pancakes!
        Step 8: If you have extra batter, you can store it in the refrigerator for up to 2 days in an airtight container.
    """.trimIndent()

    val sampleRecipe = Recipe(
        id = 1,
        title = "Fluffy Buttermilk Pancakes",
        ingredients = sampleIngredients,
        directions = longDirections,
        imageUri = "android.resource://com.example.recipeapp/drawable/sample_pancake" // Assuming you have this drawable
    )
    MaterialTheme { // Wrap preview in MaterialTheme
        RecipeDetailScreen(recipe = sampleRecipe)
    }
}