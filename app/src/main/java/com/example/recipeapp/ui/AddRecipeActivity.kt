package com.example.recipeapp.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import coil.compose.rememberAsyncImagePainter
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.net.toUri
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID


// grants permission for the image to be displayed in the detail activity pare idk yet how this works
//i copy pasted this from some android documentation
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecipeScreen(
    userId: Int,
    onBack: (() -> Unit)? = null,
    onSave: (() -> Unit)? = null,
    initialName: String = "",
    initialDirections: String = "",
    initialIngredients: List<Ingredient> = emptyList(),
    initialImageUri: String? = null,
    initialServings: String = "",
    initialTags: String = "",
    initialCookTime: String = "",
    initialPrepTime: String = "",
    initialCuisine: String = "",
    initialDifficulty: String = "",
    initialVisibility: String = "Only me",
    onRequestAdminApproval: (() -> Unit)? = null,
    headerText: String = "Add Recipe"
) {
    var name by remember { mutableStateOf(initialName) }
    var ingredientName by remember { mutableStateOf("") }
    var ingredientQty by remember { mutableStateOf("") }
    var ingredientUnit by remember { mutableStateOf("") }
    var directions by remember { mutableStateOf(initialDirections) }
    var imageUri by remember { mutableStateOf<Uri?>(initialImageUri?.toUri()) }
    val ingredients = remember { mutableStateListOf<Ingredient>().apply { addAll(initialIngredients) } }
    var servings by remember { mutableStateOf(initialServings) }
    var tags by remember { mutableStateOf(initialTags) }
    var cookTime by remember { mutableStateOf(initialCookTime) }
    var prepTime by remember { mutableStateOf(initialPrepTime) }
    var cuisine by remember { mutableStateOf(initialCuisine) }
    var difficulty by remember { mutableStateOf(initialDifficulty) }
    var visibility by remember { mutableStateOf(initialVisibility) }
    var expanded by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
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

    fun getImageFileName(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val safeName = name.replace("[^A-Za-z0-9]".toRegex(), "_")
        return "recipe_${safeName}_$timestamp.jpg"
    }
    fun uploadImageAndRecipe() {
        if (imageUri == null) {
            uploadError = "Image is required."
            return
        }
        isUploading = true
        uploadError = null
        scope.launch(Dispatchers.IO) {
            try {
                // Upload image with proper name
                val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri!!)
                val tempFile = File(context.cacheDir, getImageFileName())
                inputStream?.use { input ->
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("image", tempFile.name, tempFile.asRequestBody())
                    .build()
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("http://10.0.2.2/MyRecipeAppRestApi/upload_image.php")
                    .post(requestBody)
                    .build()
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()
                if (!response.isSuccessful || responseBody == null) {
                    uploadError = "Image upload failed."
                    isUploading = false
                    return@launch
                }
                val imageUrl = org.json.JSONObject(responseBody).optString("url", "")
                if (imageUrl.isBlank()) {
                    uploadError = "Image upload failed."
                    isUploading = false
                    return@launch
                }
                // Upload recipe
                val recipeRequestBody = FormBody.Builder()
                    .add("name", name)
                    .add("ingredients", Gson().toJson(ingredients))
                    .add("instructions", directions)
                    .add("servings", servings)
                    .add("tags", tags)
                    .add("image", imageUrl)
                    .add("cookTimeMinutes", cookTime)
                    .add("prepTimeMinutes", prepTime)
                    .add("cuisine", cuisine)
                    .add("difficulty", difficulty)
                    .add("visibility", visibility)
                    .add("userId", userId.toString()) // <-- Always include userId
                    .build()
                val recipeRequest = Request.Builder()
                    .url("http://10.0.2.2/MyRecipeAppRestApi/add_recipe.php")
                    .post(recipeRequestBody)
                    .build()
                val recipeResponse = client.newCall(recipeRequest).execute()
                val recipeResponseBody = recipeResponse.body?.string()
                if (!recipeResponse.isSuccessful || recipeResponseBody == null) {
                    withContext(Dispatchers.Main) {
                        uploadError = "Recipe upload failed."
                    }
                    isUploading = false
                    return@launch
                }
                isUploading = false
                withContext(Dispatchers.Main) {
                    if (recipeResponse.isSuccessful && recipeResponseBody != null) {
                        try {
                            val json = org.json.JSONObject(recipeResponseBody)
                            if (json.optBoolean("success", false)) {
                                // Recipe uploaded successfully
                                if (visibility == "Public") {
                                    android.widget.Toast.makeText(context, "Upload successful! Your recipe is waiting for admin approval.", android.widget.Toast.LENGTH_LONG).show()
                                    // Auto-back after showing toast
                                    kotlinx.coroutines.delay(1000)
                                    onRequestAdminApproval?.invoke()
                                } else {
                                    android.widget.Toast.makeText(context, "Recipe saved successfully!", android.widget.Toast.LENGTH_SHORT).show()
                                    // Auto-back after showing toast
                                    kotlinx.coroutines.delay(1000)
                                    onSave?.invoke()
                                }
                            } else {
                                uploadError = json.optString("error", "Recipe upload failed.")
                            }
                        } catch (e: Exception) {
                            // If we can't parse the response but got a 200 status, assume success
                            // This handles cases where the response is cut off but the recipe was saved
                            if (recipeResponse.code == 200) {
                                uploadError = "Recipe may have been uploaded successfully, but response was incomplete. Please check your recipes."
                                // Still call success callback and show toast after a short delay
                                kotlinx.coroutines.delay(2000)
                                if (visibility == "Public") {
                                    android.widget.Toast.makeText(context, "Upload may be successful! Check admin approval status.", android.widget.Toast.LENGTH_LONG).show()
                                    onRequestAdminApproval?.invoke()
                                } else {
                                    android.widget.Toast.makeText(context, "Recipe may have been saved!", android.widget.Toast.LENGTH_SHORT).show()
                                    onSave?.invoke()
                                }
                            } else {
                                uploadError = "Recipe upload failed: Invalid response"
                            }
                        }
                    } else {
                        uploadError = "Recipe upload failed: Server error ${recipeResponse.code}"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    uploadError = "Upload failed: ${e.message}"
                }
                isUploading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(0.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        Surface(
            tonalElevation = 4.dp,
            shadowElevation = 4.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                IconButton(onClick = { onBack?.invoke() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = headerText,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier.weight(1f, fill = true)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .shadow(8.dp, MaterialTheme.shapes.large),
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Button(onClick = { imagePickerLauncher.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                            Text("Pick Image (Required)")
                        }
                        imageUri?.let {
                            Image(
                                painter = rememberAsyncImagePainter(it),
                                contentDescription = "Recipe Image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(MaterialTheme.shapes.medium),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Recipe Name (Required)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = visibility,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Visibility (Required)") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                                },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Only me") },
                                    onClick = {
                                        visibility = "Only me"
                                        expanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Public") },
                                    onClick = {
                                        visibility = "Public"
                                        expanded = false
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Ingredients (Required)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = ingredientName,
                                onValueChange = { ingredientName = it },
                                label = { Text("Name (Required)") },
                                modifier = Modifier.weight(1f),
                                shape = MaterialTheme.shapes.medium
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = ingredientQty,
                                onValueChange = { ingredientQty = it },
                                label = { Text("Qty (Required)") },
                                modifier = Modifier.width(80.dp),
                                shape = MaterialTheme.shapes.medium
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = ingredientUnit,
                                onValueChange = { ingredientUnit = it },
                                label = { Text("Unit (Optional)") },
                                modifier = Modifier.width(80.dp),
                                shape = MaterialTheme.shapes.medium
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
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${ing.name} - ${ing.quantity} ${ing.unit}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(onClick = { ingredients.remove(ing) }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Remove Ingredient")
                                    }
                                }
                                HorizontalDivider()
                            }
                        }
                        OutlinedTextField(
                            value = directions,
                            onValueChange = { directions = it },
                            label = { Text("Instructions (Required)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            minLines = 3
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = servings,
                            onValueChange = { servings = it },
                            label = { Text("Servings (Required)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = tags,
                            onValueChange = { tags = it },
                            label = { Text("Tags (Required, comma separated)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = cookTime,
                            onValueChange = { cookTime = it },
                            label = { Text("Cook Time Minutes (Required)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = prepTime,
                            onValueChange = { prepTime = it },
                            label = { Text("Prep Time Minutes (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = cuisine,
                            onValueChange = { cuisine = it },
                            label = {
                                Text("Cuisine (Optional)")
                            })
                        Button(
                            onClick = { uploadImageAndRecipe() },
                            enabled = !isUploading && name.isNotBlank() && ingredients.isNotEmpty() && directions.isNotBlank() && servings.isNotBlank() && tags.isNotBlank() && cookTime.isNotBlank() && imageUri != null,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (isUploading) "Uploading..." else "Save Recipe")
                        }
                        uploadError?.let { error ->
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
class AddRecipeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val editMode = intent.getBooleanExtra("edit_mode", false)
        val recipeId = intent.getIntExtra("recipe_id", -1)
        val userId = intent.getIntExtra("userId", -1) // Changed from "user_id" to "userId"
        setContent {
            var name by remember { mutableStateOf("") }
            var directions by remember { mutableStateOf("") }
            val ingredients = remember { mutableStateListOf<Ingredient>() }
            var imageUri by remember { mutableStateOf<android.net.Uri?>(null) }
            var servings by remember { mutableStateOf("") }
            var tags by remember { mutableStateOf("") }
            var cookTime by remember { mutableStateOf("") }
            var prepTime by remember { mutableStateOf("") }
            var cuisine by remember { mutableStateOf("") }
            var difficulty by remember { mutableStateOf("") }
            var visibility by remember { mutableStateOf("Only me") }
            var isApproved by remember { mutableStateOf(0) }
            var isLoading by remember { mutableStateOf(editMode) }
            var uploadError by remember { mutableStateOf<String?>(null) }
            val context = LocalContext.current
            val scope = rememberCoroutineScope()

            // Fetch recipe details from backend if editing
            LaunchedEffect(editMode, recipeId, userId) {
                if (editMode && recipeId != -1 && userId != -1) {
                    isLoading = true
                    try {
                        val response = withContext(Dispatchers.IO) {
                            java.net.URL("http://10.0.2.2/MyRecipeAppRestApi/get_user_recipes.php?userId=$userId").readText()
                        }
                        val json = org.json.JSONObject(response)
                        if (json.optBoolean("success", false)) {
                            val arr = json.getJSONArray("recipes")
                            for (i in 0 until arr.length()) {
                                val obj = arr.getJSONObject(i)
                                if (obj.optInt("id") == recipeId) {
                                    name = obj.optString("name", "")
                                    directions = try {
                                        val inst = obj.optString("instructions", "")
                                        val arrInst = org.json.JSONArray(inst)
                                        (0 until arrInst.length()).joinToString("\n") { arrInst.getString(it) }
                                    } catch (_: Exception) { obj.optString("instructions", "") }
                                    val ingList = try {
                                        val arrIng = obj.optString("ingredients", "[]")
                                        val arrJson = org.json.JSONArray(arrIng)
                                        List(arrJson.length()) {
                                            val ing = arrJson.getJSONObject(it)
                                            Ingredient(
                                                ing.optString("name", ""),
                                                ing.optString("quantity", ""),
                                                ing.optString("unit", "")
                                            )
                                        }
                                    } catch (_: Exception) { emptyList() }
                                    ingredients.clear(); ingredients.addAll(ingList)
                                    val imgUrl = obj.optString("image", "")
                                    imageUri = if (imgUrl.isNotBlank()) imgUrl.toUri() else null
                                    servings = obj.optString("servings", "")
                                    tags = try {
                                        val tagArr = obj.optString("tags", "[]")
                                        val arrTag = org.json.JSONArray(tagArr)
                                        (0 until arrTag.length()).joinToString(", ") { arrTag.getString(it) }
                                    } catch (_: Exception) { obj.optString("tags", "") }
                                    cookTime = obj.optString("cookTimeMinutes", "")
                                    prepTime = obj.optString("prepTimeMinutes", "")
                                    cuisine = obj.optString("cuisine", "")
                                    difficulty = obj.optString("difficulty", "")
                                    visibility = obj.optString("visibility", "Only me")
                                    isApproved = obj.optInt("isApproved", 0)
                                    break
                                }
                            }
                        }
                    } catch (e: Exception) {
                        uploadError = "Failed to load recipe: ${e.message}"
                    } finally {
                        isLoading = false
                    }
                }
            }

            AddRecipeScreen(
                userId = userId,
                initialName = name,
                initialDirections = directions,
                initialIngredients = ingredients,
                initialImageUri = imageUri?.toString(),
                initialServings = servings,
                initialTags = tags,
                initialCookTime = cookTime,
                initialPrepTime = prepTime,
                initialCuisine = cuisine,
                initialDifficulty = difficulty,
                initialVisibility = visibility,
                onBack = { finish() },
                onSave = {
                    val changed = false // You can implement change detection as before
                    if (editMode) {
                        // ...existing update logic...
                    } else {

                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddRecipeScreenPreview() {
    MaterialTheme {
        AddRecipeScreen(userId = 1, onBack = {}, onSave = {})
    }
}
