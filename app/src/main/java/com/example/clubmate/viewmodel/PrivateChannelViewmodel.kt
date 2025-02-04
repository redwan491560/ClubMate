package com.example.clubmate.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.UUID

class PrivateChannelViewModel : ViewModel() {

    private val _channelRef = FirebaseDatabase.getInstance().getReference("private_channels")
    private val _privateMessageList = MutableStateFlow<List<MessageMap>>(emptyList())
    val privateMessageList = _privateMessageList


    fun createChatroom(chatId: String, passWord: String, onResult: (ChannelMap?) -> Unit) {
        if (chatId.isEmpty() || passWord.isEmpty()) {
            onResult(null)
            return
        }

        val chatData = ChannelMap(
            createdAt = System.currentTimeMillis(),
            setPassword = passWord
        )

        _channelRef.child(chatId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    onResult(null)
                } else {
                    _channelRef.child(chatId).setValue(chatData)
                        .addOnSuccessListener {
                            onResult(chatData)
                            Log.d("Chatroom", "Chatroom created: $chatId")
                        }
                        .addOnFailureListener {
                            onResult(null)
                            Log.e("Chatroom", "Failed to create chatroom", it)
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                onResult(null)
            }

        })

    }

    fun joinChatroom(chatId: String, passWord: String, onClick: (Boolean) -> Unit) {
        _channelRef.child(chatId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val chatData = snapshot.getValue(ChannelMap::class.java)
                    if (chatData != null) {
                        if (passWord == chatData.setPassword) onClick(true)
                        else onClick(false)
                    } else {
                        onClick(false)
                    }
                } else {
                    onClick(false)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                onClick(false)
                Log.e("Chatroom", "Database error: ${error.message}")
            }
        })
    }

    // so far good
    fun sendVanishingMessage(chatId: String, messageText: String, imageUrl: String = "") {
        val messageId = _channelRef.child(chatId).child("messages").push().key ?: return

        val message = VanishingMessage(
            messageId = messageId,
            messageText = messageText,
            imageUrl = imageUrl,
            timestampSent = System.currentTimeMillis(),
            isSeen = false,
        )

        _channelRef.child(chatId).child("messages").child(messageId).setValue(message)
            .addOnSuccessListener {
                Log.d("Message", "Vanishing message sent: $messageId")
            }
            .addOnFailureListener {
                Log.e("Message", "Failed to send vanishing message", it)
            }
    }

    fun listenForVanishingMessages(chatId: String, onMessageReceived: (VanishingMessage) -> Unit) {
        val messagesRef = _channelRef.child(chatId).child("messages")

        messagesRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val message = snapshot.getValue(VanishingMessage::class.java)
                if (message != null && !message.isSeen) {
                    // Notify UI of new message
                    onMessageReceived(message)

                    // Mark the message as read
                    markMessageAsRead(chatId, message.messageId)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                Log.e("Message", "Failed to listen for messages", error.toException())
            }
        })
    }

    fun markMessageAsRead(chatId: String, messageId: String) {
        val messageRef = _channelRef.child(chatId).child("messages").child(messageId)

        messageRef.child("isRead").setValue(true)
        messageRef.child("timestampRead").setValue(System.currentTimeMillis())
            .addOnSuccessListener {
                Log.d("Message", "Message marked as read: $messageId")
                deleteMessageAfterRead(chatId, messageId) // Trigger deletion
            }
            .addOnFailureListener {
                Log.e("Message", "Failed to mark message as read", it)
            }
    }

    private fun deleteMessageAfterRead(chatId: String, messageId: String) {
        _channelRef.child(chatId).child("messages").child(messageId).removeValue()
            .addOnSuccessListener {
                Log.d("Message", "Message deleted after reading: $messageId")
            }
            .addOnFailureListener {
                Log.e("Message", "Failed to delete message after reading", it)
            }
    }
    //

    fun requestId(onResult: (String) -> Unit) {
        val channelId = generateUniqueId()

        _channelRef.child(channelId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                // if id is found regenerate using recursion
                requestId(onResult)
            } else {
                // return the id
                onResult(channelId)
            }
        }.addOnFailureListener {
            Log.d("RequestIdError", "Failed to check group ID existence: ${it.message}")
        }
    }

    private fun generateUniqueId(): String {
        return UUID.randomUUID().toString().replace("-", "").take(10)
    }


}

data class MessageMap(
    val messageId: String = "",
    val message: String = "",
    val imageUrl: String = "",
    val timeStamp: Long = 0L,
    val seenBySender: Boolean = false,
    val seenByReceiver: Boolean = false
)

data class ChannelMap(
    val createdAt: Long = 0L, // Store as Long
    val setPassword: String = ""
)

data class VanishingMessage(
    val messageId: String = "", // Unique ID for the message
    val messageText: String = "", // Text content of the message
    val imageUrl: String = "", // URL for media (if any)
    val timestampSent: Long = System.currentTimeMillis(), // When the message was sent
    var senderId: String = "", // Whether the message has been read
    var isSeen: Boolean = false // Whether the message has been deleted
)