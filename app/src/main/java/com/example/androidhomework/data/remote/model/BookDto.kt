package com.example.androidhomework.data.remote.model

import com.google.gson.annotations.SerializedName

data class SearchResponse(
    @SerializedName("docs") val docs: List<BookDto>
)

data class BookDto(
    @SerializedName("key") val key: String,
    @SerializedName("title") val title: String,
    @SerializedName("author_name") val authorName: List<String>?,
    @SerializedName("first_publish_year") val firstPublishYear: Int?,
    @SerializedName("cover_i") val coverI: Long?,
    @SerializedName("edition_count") val editionCount: Int?,
    @SerializedName("language") val language: List<String>?,
    @SerializedName("subject") val subject: List<String>?
)