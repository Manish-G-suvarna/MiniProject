package com.example.miniproject.core.data.model

data class Category(
    val name: String = "",
    val crops: List<Crop> = emptyList()
)

data class Crop(
    val name: String = "",
    val picURL: String = "",
    val regions: List<String> = emptyList(),
    val about: String = "",
    val diseases: List<Disease> = emptyList()
)

data class Disease(
    val name: String = "",
    val symptoms: String = "",
    val cause: String = "",
    val solution: List<String> = emptyList()
)
