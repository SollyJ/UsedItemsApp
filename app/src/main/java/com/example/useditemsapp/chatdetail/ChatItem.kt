package com.example.useditemsapp.chatdetail

data class ChatItem(
    val sendID: String,
    val message: String
) {
    constructor(): this("", "")
}
