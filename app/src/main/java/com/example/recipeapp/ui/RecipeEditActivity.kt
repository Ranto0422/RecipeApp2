package com.example.recipeapp.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.example.recipeapp.model.Ingredient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL


@Composable
fun RecipeEditScreen(
    userId: Int,
    recipeId: Int,
    onBack: (() -> Unit)? = null,
    onSave: (() -> Unit)? = null
) {
    var name by remember { mutableStateOf("") }
    var directions by remember { mutableStateOf("") }
    val ingredients = remember { mutableStateListOf<Ingredient>() }
    var servings by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var cookTime by remember { mutableStateOf("") }
    var prepTime by remember { mutableStateOf("") }
    var cuisine by remember { mutableStateOf("") }
    var visibility by remember { mutableStateOf("Only me") }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    var uploadError by remember { mutableStateOf<String?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Custom update function for edit mode
    fun updateRecipe() {
        // Validate required fields before sending
        if (name.isBlank()) {
            uploadError = "Recipe name is required"
            return
        }
        if (ingredients.isEmpty()) {
            uploadError = "At least one ingredient is required"
            return
        }
        if (directions.isBlank()) {
            uploadError = "Instructions are required"
            return
        }
        if (servings.isBlank()) {
            uploadError = "Servings is required"
            return
        }
        if (tags.isBlank()) {
            uploadError = "Tags are required"
            return
        }
        if (cookTime.isBlank()) {
            uploadError = "Cook time is required"
            return
        }

        isUploading = true
        uploadError = null
        scope.launch(Dispatchers.IO) {
            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .build()

                val requestBody = FormBody.Builder()
                    .add("recipeId", recipeId.toString())
                    .add("name", name.trim())
                    .add("ingredients", com.google.gson.Gson().toJson(ingredients))
                    .add("instructions", directions.trim())
                    .add("servings", servings.trim())
                    .add("tags", tags.trim())
                    .add("cookTimeMinutes", cookTime.trim())
                    .add("prepTimeMinutes", prepTime.trim().ifEmpty { "0" })
                    .add("cuisine", cuisine.trim())
                    .add("visibility", visibility.trim())
                    .build()

                println("RecipeEditScreen: Sending update request for recipe $recipeId")
                println("RecipeEditScreen: name='$name', servings='$servings', tags='$tags', cookTime='$cookTime'")

                val request = Request.Builder()
                    .url("http://10.0.2.2/MyRecipeAppRestApi/update_recipe.php")
                    .post(requestBody)
                    .addHeader("Connection", "close")
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                println("RecipeEditScreen: Response code: ${response.code}")
                println("RecipeEditScreen: Response body: $responseBody")

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && responseBody != null) {
                        try {
                            val json = JSONObject(responseBody)
                            if (json.optBoolean("success", false)) {
                                println("RecipeEditScreen: Update successful")
                                onSave?.invoke()
                            } else {
                                val errorMsg = json.optString("error", "Failed to update recipe")
                                println("RecipeEditScreen: Server error: $errorMsg")
                                uploadError = errorMsg
                            }
                        } catch (e: Exception) {
                            println("RecipeEditScreen: JSON parsing error: ${e.message}")
                            uploadError = "Invalid response from server: $responseBody"
                        }
                    } else {
                        println("RecipeEditScreen: HTTP error ${response.code}")
                        uploadError = "Server error: ${response.code} - ${responseBody ?: "Unknown error"}"
                    }
                }
            } catch (e: java.net.SocketTimeoutException) {
                withContext(Dispatchers.Main) {
                    uploadError = "Update timeout. Please check your connection and try again."
                }
            } catch (e: java.io.EOFException) {
                withContext(Dispatchers.Main) {
                    uploadError = "Connection interrupted. Please try again."
                }
            } catch (e: java.net.ConnectException) {
                withContext(Dispatchers.Main) {
                    uploadError = "Cannot connect to server. Please check if XAMPP is running."
                }
            } catch (e: java.net.UnknownHostException) {
                withContext(Dispatchers.Main) {
                    uploadError = "Server not found. Please check your network connection."
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    uploadError = "Update failed: ${e.message}"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isUploading = false
                }
            }
        }
    }

    // Fetch recipe details from backend
    LaunchedEffect(recipeId, userId) {
        println("RecipeEditScreen: Starting to fetch recipe with ID: $recipeId, userId: $userId")
        isLoading = true
        try {
            val response = withContext(Dispatchers.IO) {
                val connection = URL("http://10.0.2.2/MyRecipeAppRestApi/get_user_recipes.php?userId=$userId").openConnection()
                connection.connectTimeout = 10000
                connection.readTimeout = 15000
                connection.getInputStream().bufferedReader().use { it.readText() }
            }
            println("RecipeEditScreen: Raw response: $response")

            if (response.isBlank()) {
                uploadError = "Empty response from server"
                println("RecipeEditScreen: Empty response received")
                return@LaunchedEffect
            }

            val json = JSONObject(response)
            if (json.optBoolean("success", false)) {
                val arr = json.getJSONArray("recipes")
                var found = false
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    if (obj.optInt("id") == recipeId) {
                        println("RecipeEditScreen: Found recipe with ID: $recipeId")
                        name = obj.optString("name", "")
                        println("RecipeEditScreen: Setting name to: $name")

                        // Parse instructions as list of steps if possible
                        directions = when (val inst = obj.opt("instructions")) {
                            is JSONArray -> (0 until inst.length()).joinToString("\n") { inst.getString(it) }
                            is String -> inst
                            else -> obj.optString("instructions", "")
                        }
                        println("RecipeEditScreen: Setting directions to: $directions")

                        // Parse ingredients as list
                        val ingList = when (val ing = obj.opt("ingredients")) {
                            is JSONArray -> List(ing.length()) {
                                val ingObj = ing.getJSONObject(it)
                                Ingredient(
                                    ingObj.optString("name", ""),
                                    ingObj.optString("quantity", ""),
                                    ingObj.optString("unit", "")
                                )
                            }
                            is String -> try {
                                val arrJson = JSONArray(ing)
                                List(arrJson.length()) {
                                    val ingObj = arrJson.getJSONObject(it)
                                    Ingredient(
                                        ingObj.optString("name", ""),
                                        ingObj.optString("quantity", ""),
                                        ingObj.optString("unit", "")
                                    )
                                }
                            } catch (_: Exception) { emptyList() }
                            else -> emptyList()
                        }
                        ingredients.clear(); ingredients.addAll(ingList)
                        println("RecipeEditScreen: Setting ingredients count: ${ingredients.size}")

                        servings = obj.optString("servings", "")
                        println("RecipeEditScreen: Setting servings to: $servings")

                        // Parse tags as comma separated string
                        tags = when (val tagArr = obj.opt("tags")) {
                            is JSONArray -> (0 until tagArr.length()).joinToString(", ") { tagArr.getString(it) }
                            is String -> tagArr
                            else -> obj.optString("tags", "")
                        }
                        println("RecipeEditScreen: Setting tags to: $tags")

                        cookTime = obj.optString("cookTimeMinutes", "")
                        prepTime = obj.optString("prepTimeMinutes", "")
                        cuisine = obj.optString("cuisine", "")
                        visibility = obj.optString("visibility", "Only me")

                        println("RecipeEditScreen: All fields set successfully")
                        found = true
                        break
                    }
                }
                if (!found) {
                    uploadError = "Recipe with ID $recipeId not found in backend."
                    println("RecipeEditScreen: Recipe not found")
                }
            } else {
                uploadError = "Failed to fetch recipe from backend: ${json.optString("error", "Unknown error")}"
                println("RecipeEditScreen: Backend returned error: ${json.optString("error")}")
            }
        } catch (e: java.net.SocketTimeoutException) {
            uploadError = "Connection timeout. Please check your network connection."
            println("RecipeEditScreen: Timeout exception: ${e.message}")
        } catch (e: java.io.EOFException) {
            uploadError = "Connection interrupted. Please try again."
            println("RecipeEditScreen: EOF exception: ${e.message}")
        } catch (e: java.net.ConnectException) {
            uploadError = "Cannot connect to server. Please check if XAMPP is running."
            println("RecipeEditScreen: Connection exception: ${e.message}")
        } catch (e: Exception) {
            uploadError = "Failed to load recipe: ${e.message}"
            println("RecipeEditScreen: Exception occurred: ${e.message}")
            e.printStackTrace()
        } finally {
            isLoading = false
            println("RecipeEditScreen: Loading finished. isLoading = false")
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        EditRecipeContent(
            name = name,
            onNameChange = { name = it },
            directions = directions,
            onDirectionsChange = { directions = it },
            ingredients = ingredients,
            servings = servings,
            onServingsChange = { servings = it },
            tags = tags,
            onTagsChange = { tags = it },
            cookTime = cookTime,
            onCookTimeChange = { cookTime = it },
            prepTime = prepTime,
            onPrepTimeChange = { prepTime = it },
            cuisine = cuisine,
            onCuisineChange = { cuisine = it },
            visibility = visibility,
            onVisibilityChange = { visibility = it },
            isUploading = isUploading,
            uploadError = uploadError,
            onSave = { updateRecipe() },
            onBack = onBack,
            recipeId = recipeId,
            userId = userId,
            setIsUploading = { isUploading = it },
            setUploadError = { uploadError = it },
            scope = scope
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRecipeContent(
    name: String,
    onNameChange: (String) -> Unit,
    directions: String,
    onDirectionsChange: (String) -> Unit,
    ingredients: MutableList<Ingredient>,
    servings: String,
    onServingsChange: (String) -> Unit,
    tags: String,
    onTagsChange: (String) -> Unit,
    cookTime: String,
    onCookTimeChange: (String) -> Unit,
    prepTime: String,
    onPrepTimeChange: (String) -> Unit,
    cuisine: String,
    onCuisineChange: (String) -> Unit,
    visibility: String,
    onVisibilityChange: (String) -> Unit,
    isUploading: Boolean,
    uploadError: String?,
    onSave: () -> Unit,
    onBack: (() -> Unit)? = null,
    recipeId: Int,
    userId: Int,
    setIsUploading: (Boolean) -> Unit,
    setUploadError: (String?) -> Unit,
    scope: CoroutineScope
) {
    var ingredientName by remember { mutableStateOf("") }
    var ingredientQty by remember { mutableStateOf("") }
    var ingredientUnit by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deleteRequested by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
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
                    text = "Edit Recipe",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Recipe Name
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Recipe Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Visibility
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = visibility,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Visibility") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Only me") },
                        onClick = {
                            onVisibilityChange("Only me")
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Public") },
                        onClick = {
                            onVisibilityChange("Public")
                            expanded = false
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Ingredients
            Text("Ingredients", style = MaterialTheme.typography.titleMedium)
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
                        ingredients.add(Ingredient(ingredientName, ingredientQty, ingredientUnit))
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

            // Ingredients List
            ingredients.forEach { ing ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${ing.name} - ${ing.quantity} ${ing.unit}",
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { ingredients.remove(ing) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Remove")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Instructions
            OutlinedTextField(
                value = directions,
                onValueChange = onDirectionsChange,
                label = { Text("Instructions") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Other fields
            OutlinedTextField(
                value = servings,
                onValueChange = onServingsChange,
                label = { Text("Servings") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = tags,
                onValueChange = onTagsChange,
                label = { Text("Tags (comma separated)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = cookTime,
                onValueChange = onCookTimeChange,
                label = { Text("Cook Time (minutes)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = prepTime,
                onValueChange = onPrepTimeChange,
                label = { Text("Prep Time (minutes)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = cuisine,
                onValueChange = onCuisineChange,
                label = { Text("Cuisine") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Save Button
            Button(
                onClick = onSave,
                enabled = !isUploading && name.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isUploading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                } else {
                    Text("Update Recipe")
                }
            }

            // Delete Button (below Update Recipe)
            if (recipeId > 0) {
                Button(
                    onClick = { showDeleteDialog = true },
                    enabled = !isUploading,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text("Delete Recipe", color = MaterialTheme.colorScheme.onError)
                }
            }

            // Error Message
            uploadError?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        // Place this at the top level of EditRecipeContent composable
        if (deleteRequested) {
            LaunchedEffect(deleteRequested) {
                setIsUploading(true)
                setUploadError(null)
                try {
                    val client = OkHttpClient()
                    val requestBody = FormBody.Builder()
                        .add("recipeId", recipeId.toString())
                        .add("userId", userId.toString())
                        .build()
                    val request = Request.Builder()
                        .url("http://10.0.2.2/MyRecipeAppRestApi/delete_recipe.php")
                        .post(requestBody)
                        .build()
                    val response = withContext(Dispatchers.IO) { client.newCall(request).execute() }
                    val responseBody = response.body?.string()
                    if (response.isSuccessful && responseBody != null) {
                        val json = JSONObject(responseBody)
                        if (json.optBoolean("success", false)) {
                            onBack?.invoke()
                        } else {
                            setUploadError(json.optString("error", "Failed to delete recipe"))
                        }
                    } else {
                        setUploadError("Server error: ${response.code} - ${responseBody ?: "Unknown error"}")
                    }
                } catch (e: Exception) {
                    setUploadError("Delete failed: ${e.message}")
                } finally {
                    setIsUploading(false)
                    deleteRequested = false
                }
            }
        }

        // Confirmation dialog for delete
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Recipe") },
                text = { Text("Are you sure you want to delete this recipe? This action cannot be undone.") },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        deleteRequested = true
                    }) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

class RecipeEditActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val recipeId = intent.getIntExtra("recipe_id", -1)
        val userId = intent.getIntExtra("user_id", -1)
        setContent {
            RecipeEditScreen(
                userId = userId,
                recipeId = recipeId,
                onBack = { finish() },
                onSave = { finish() }
            )
        }
    }
}
