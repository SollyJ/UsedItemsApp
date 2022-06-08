package com.example.useditemsapp.chatlist

data class ChatListItem(
    val buyerID: String,
    val sellerID: String,
    val itemTitle: String,
    val imageURL: String,
    //val msg: String,
    //val lastMsgTime: String,
    val key: String
) {
    constructor(): this("","","","","")
}
