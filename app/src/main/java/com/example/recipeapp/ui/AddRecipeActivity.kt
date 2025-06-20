package com.example.recipeapp.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.example.recipeapp.model.Ingredient
import com.google.gson.Gson
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment

@Composable
fun AddRecipeScreen(
    onBack: (() -> Unit)? = null,
    onSave: ((String, List<Ingredient>, String) -> Unit)? = null,
    initialName: String = "",
    initialDirections: String = "",
    initialIngredients: List<Ingredient> = emptyList()
) {
    var name by remember { mutableStateOf(initialName) }
    var ingredientName by remember { mutableStateOf("") }
    var ingredientQty by remember { mutableStateOf("") }
    var ingredientUnit by remember { mutableStateOf("") }
    var directions by remember { mutableStateOf(initialDirections) }
    val ingredients = remember { mutableStateListOf<Ingredient>().apply { addAll(initialIngredients) } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onBack?.invoke() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Recipe", modifier = Modifier.padding(bottom = 16.dp))
        }
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("Ingredients", modifier = Modifier.padding(vertical = 8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = ingredientName,
                onValueChange = { ingredientName = it },
                label = { Text("Name") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = ingredientQty,
                onValueChange = { ingredientQty = it },
                label = { Text("Qty") },
                modifier = Modifier.width(80.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = ingredientUnit,
                onValueChange = { ingredientUnit = it },
                label = { Text("Unit") },
                modifier = Modifier.width(80.dp)
            )
        }
        Button(
            onClick = {
                if (ingredientName.isNotBlank() && ingredientQty.isNotBlank()) {
                    val ing = Ingredient(ingredientName, ingredientQty, ingredientUnit)
                    ingredients.add(ing)
                    ingredientName = ""
                    ingredientQty = ""
                    ingredientUnit = ""
                }
            },
            enabled = ingredientName.isNotBlank() && ingredientQty.isNotBlank(),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Add Ingredient")
        }
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            ingredients.forEach { ing ->
                Text("${ing.name} - ${ing.quantity} ${ing.unit}")
            }
        }
        OutlinedTextField(
            value = directions,
            onValueChange = { directions = it },
            label = { Text("Directions") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                onSave?.invoke(name, ingredients, directions)
            },
            enabled = name.isNotBlank() && ingredients.isNotEmpty() && directions.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
        }
    }
}

class AddRecipeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            AddRecipeScreen(
                onBack = { finish() },
                onSave = { name, ingredients, directions ->
                    val resultIntent = Intent().apply {
                        putExtra("name", name)
                        putExtra("ingredients_json", Gson().toJson(ingredients))
                        putExtra("directions", directions)
                    }
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddRecipeScreenPreview() {
    AddRecipeScreen()
}
