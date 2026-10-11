package com.example.deviname.data

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.example.deviname.model.Category
import com.example.deviname.model.GameItem
import org.json.JSONArray

object GameRepository {

    fun loadItems(context: Context): List<GameItem> {
        val jsonText = context.assets.open("data.json")
            .bufferedReader()
            .use { it.readText() }

        val array = JSONArray(jsonText)
        val items = mutableListOf<GameItem>()

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val categoryStr = obj.getString("category").uppercase()
            val category = runCatching { Category.valueOf(categoryStr) }
                .getOrElse { Category.FILM }

            items.add(
                GameItem(
                    fileName = obj.getString("file"),
                    title = obj.getString("title"),
                    category = category,
                )
            )
        }
        return items
    }

    fun loadImage(context: Context, fileName: String): ImageBitmap? {
        return runCatching {
            context.assets.open("images/$fileName").use { stream ->
                BitmapFactory.decodeStream(stream).asImageBitmap()
            }
        }.getOrNull()
    }
}