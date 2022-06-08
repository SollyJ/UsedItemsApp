package com.example.useditemsapp.home

data class ArticleModel(
    val sellerID: String,
    val title: String,
    val createdAt: Long,
    val price: String,
    val imageURL: String
    ) {
    constructor(): this("","",0,"","")
}