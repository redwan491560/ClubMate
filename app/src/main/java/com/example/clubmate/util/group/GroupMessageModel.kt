package com.example.clubmate.util.group

data class GroupMessage(
    val messageId: String = "",
    val senderId: String = "",
    val messageText: String = "",
    val imageRef: String = "",
    val timestamp: Long = 0
)