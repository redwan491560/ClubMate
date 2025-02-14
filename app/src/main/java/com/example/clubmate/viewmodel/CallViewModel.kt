package com.example.clubmate.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch

class CallViewModel : ViewModel() {

    private val callRef = FirebaseDatabase.getInstance().getReference("call")

    private fun generateCallID(senderId: String, receiverId: String): String {
        val sortedIds = listOf(senderId, receiverId).sorted()
        val callId = "${sortedIds[0]}+${sortedIds[1]}"
        return callId
    }


    fun fetchChatId(recieverId: String, senderId: String): String {
        var chatId: String = generateCallID(recieverId, senderId)

        viewModelScope.launch {
            callRef.child(chatId).get().addOnCompleteListener { task ->
                chatId = if (task.isSuccessful) {
                    task.result.toString()
                } else {
                    generateCallID(senderId, recieverId)
                }
            }
        }
        return chatId
    }
}