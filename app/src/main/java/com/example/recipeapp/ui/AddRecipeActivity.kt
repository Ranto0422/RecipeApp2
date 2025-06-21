package com.example.recipeapp.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
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
import coil.compose.rememberAsyncImagePainter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.net.toUri

// grants permission for the image to be displayed in the detail activity pare
class GetContentWithPersistablePermission : ActivityResultContract<String, Uri?>() {
    override fun createIntent(context: android.content.Context, input: String): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = input
            addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        if (resultCode != Activity.RESULT_OK || intent == null) return null
        return intent.data
    }
}

@Composable
fun AddRecipeScreen(
    onBack: (() -> Unit)? = null,
    onSave: ((String, List<Ingredient>, String, String?) -> Unit)? = null,
    initialName: String = "",
    initialDirections: String = "",
    initialIngredients: List<Ingredient> = emptyList(),
    initialImageUri: String? = null
) {
    var name by remember { mutableStateOf(initialName) }
    var ingredientName by remember { mutableStateOf("") }
    var ingredientQty by remember { mutableStateOf("") }
    var ingredientUnit by remember { mutableStateOf("") }
    var directions by remember { mutableStateOf(initialDirections) }
    var imageUri by remember { mutableStateOf<Uri?>(initialImageUri?.toUri()) }
    val ingredients = remember { mutableStateListOf<Ingredient>().apply { addAll(initialIngredients) } }
    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(GetContentWithPersistablePermission()) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {}
        }
    }

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
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { imagePickerLauncher.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
            Text("Pick Image")
        }
        imageUri?.let {
            Image(
                painter = rememberAsyncImagePainter(it),
                contentDescription = "Recipe Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                onSave?.invoke(name, ingredients, directions, imageUri?.toString())
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
                onSave = { name, ingredients, directions, imageUri ->
                    val resultIntent = Intent().apply {
                        putExtra("name", name)
                        putExtra("ingredients_json", Gson().toJson(ingredients))
                        putExtra("directions", directions)
                        putExtra("imageUri", imageUri)
                    }
                    setResult(RESULT_OK, resultIntent)
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
