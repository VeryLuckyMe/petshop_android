package com.example.zootopia.core.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.zootopia.core.model.Product
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object RecentViewedManager {
    private const val PREFS_NAME = "zootopia_prefs"
    private const val KEY_RECENTLY_VIEWED = "recently_viewed"
    private const val MAX_ITEMS = 10

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getRecentlyViewed(context: Context): List<Product> {
        val prefs = getPrefs(context)
        val jsonString = prefs.getString(KEY_RECENTLY_VIEWED, null) ?: return emptyList()
        return try {
            Json.decodeFromString<List<Product>>(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addProduct(context: Context, product: Product) {
        val currentList = getRecentlyViewed(context).toMutableList()
        currentList.removeAll { it.id == product.id }
        currentList.add(0, product)
        
        val trimmedList = if (currentList.size > MAX_ITEMS) {
            currentList.subList(0, MAX_ITEMS)
        } else {
            currentList
        }

        val jsonString = Json.encodeToString(trimmedList)
        getPrefs(context).edit().putString(KEY_RECENTLY_VIEWED, jsonString).apply()
    }

    fun clear(context: Context) {
        getPrefs(context).edit().remove(KEY_RECENTLY_VIEWED).apply()
    }
}
