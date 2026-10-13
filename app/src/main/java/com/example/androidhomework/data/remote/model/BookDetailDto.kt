package com.example.androidhomework.data.remote.model

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class BookDetailDto(
    @SerializedName("title") val title: String?,
    @SerializedName("description") val descriptionElement: JsonElement?,
    @SerializedName("subjects") val subjects: List<String>?,
    @SerializedName("covers") val covers: List<Long>?,
    @SerializedName("first_publish_date") val firstPublishDate: String?
) {
    val description: String
        get() = when {
            descriptionElement == null || descriptionElement.isJsonNull -> "There is no available description."
            descriptionElement.isJsonPrimitive -> descriptionElement.asString
            descriptionElement.isJsonObject -> descriptionElement.asJsonObject.get("value")?.asString ?: "There is no available description."
            else -> "There is no available description."
        }

    val publishYear: Int
        get() {
            if (firstPublishDate.isNullOrBlank()) return 0
            val regex = Regex("\\d{4}")
            return regex.find(firstPublishDate)?.value?.toIntOrNull() ?: 0
        }
}