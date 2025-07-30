package com.example.recipeapp.ui

import android.content.Context
import android.widget.Toast
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.HttpClient
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlinx.coroutines.launch

// This file contains functions to handle user login and registration via a REST API.
suspend fun loginUser(context: Context, email: String, password: String): JSONObject? {
    val client = HttpClient(CIO)
    return try {
        val jsonBody = """
        {
            "email": "$email",
            "password": "$password"
        }
        """.trimIndent()
        val response = client.post("http://10.0.2.2/MyRecipeAppRestApi/login.php") {
            contentType(ContentType.Application.Json)
            setBody(jsonBody)
        }
        val stringBody = response.bodyAsText()
        return try {
            JSONObject(stringBody)
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Server error: ${stringBody}", Toast.LENGTH_LONG).show()
            }
            null
        }
    } catch (e: Exception) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
        null
    } finally {
        client.close()
    }
}

suspend fun registerUser(context: Context, name: String, email: String, password: String, role: String = "user"): JSONObject? {
    val client = HttpClient(CIO)
    return try {
        val jsonBody = """
        {
            "name": "$name",
            "email": "$email",
            "password": "$password",
            "role": "$role"
        }
        """.trimIndent()
        val response = client.post("http://10.0.2.2/MyRecipeAppRestApi/register.php") {
            contentType(ContentType.Application.Json)
            setBody(jsonBody)
        }
        val stringBody = response.bodyAsText()
        return try {
            JSONObject(stringBody)
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Server error: ${stringBody}", Toast.LENGTH_LONG).show()
            }
            null
        }
    } catch (e: Exception) {
        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
        null
    } finally {
        client.close()
    }
}
