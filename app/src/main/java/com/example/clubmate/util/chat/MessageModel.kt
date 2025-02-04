package com.example.clubmate.util.chat

import com.example.clubmate.screens.MessageStatus
import com.example.clubmate.util.MessageType


data class Message(
    val messageId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val messageText: String = "",
    val imageRef: String = "",
    val timestamp: Long = 0,
    val seen: Boolean = false,
    val status: MessageStatus = MessageStatus.SENDING,
    val messageType: MessageType = MessageType.Text
)