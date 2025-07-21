package com.example.recipeapp.util

import org.json.JSONArray
import org.json.JSONObject

fun JSONObject.optIntOrNull(key: String): Int? = if (has(key) && !isNull(key)) getInt(key) else null

fun JSONObject.optStringOrNull(key: String): String? = if (has(key) && !isNull(key)) getString(key) else null

fun JSONObject.optDoubleOrNull(key: String): Double? = if (has(key) && !isNull(key)) getDouble(key) else null

fun JSONObject.optJSONArrayOrNull(key: String): JSONArray? = if (has(key) && !isNull(key)) getJSONArray(key) else null

fun JSONArray.toStringList(): List<String> = (0 until length()).map { getString(it) }

