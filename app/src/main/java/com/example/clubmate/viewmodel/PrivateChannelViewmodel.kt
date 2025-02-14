package com.example.clubmate.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class PrivateChannelViewModel : ViewModel() {

    private val _channelRef = FirebaseDatabase.getInstance().getReference("private_channels")
    private val _privateMessageList = MutableStateFlow<List<VanishingMessage>>(emptyList())
    val privateMessageList = _privateMessageList

    fun createChatroom(
        chatId: String, passWord: String, uid: String, onResult: (ChannelMap?) -> Unit
    ) {
        if (chatId.isEmpty() || passWord.isEmpty() || uid.isEmpty()) {
            onResult(null)
            return
        }

        val chatData = ChannelMap(
            createdAt = System.currentTimeMillis(), setPassword = passWord
        )

        _channelRef.child(chatId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    onResult(null)
                } else {
                    _channelRef.child(chatId).setValue(chatData).addOnSuccessListener {
                        onResult(chatData)
                    }.addOnFailureListener {
                        onResult(null)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                onResult(null)
            }
        })
    }

    fun joinChatroom(chatId: String, uid: String, passWord: String, onClick: (Boolean) -> Unit) {
        _channelRef.child(chatId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val chatData = snapshot.getValue(ChannelMap::class.java)
                    if (chatData != null) {
                        if (passWord == chatData.setPassword) {
                            markMessagesAsSeen(chatId = chatId, uid = uid)
                            onClick(true)
                        } else onClick(false)
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

    fun leaveChatroom(chatId: String) {
        deleteSeenMessages(chatId)
        Log.d("Chatroom", "User left chatroom and seen messages were deleted")
    }


    // so far good
    fun sendVanishingMessage(
        chatId: String, uid: String, messageText: String = "", imageUrl: String = ""
    ) {

        val messageId = _channelRef.child(chatId).child("messages").push().key ?: return

        val message = VanishingMessage(
            messageId = messageId,
            messageText = messageText,
            imageUrl = imageUrl,
            senderId = uid,
            timestampSent = System.currentTimeMillis(),
            isSeen = false,
        )

        _channelRef.child(chatId).child("messages").child(messageId).setValue(message)
            .addOnSuccessListener {
                Log.d("Message", "Vanishing message sent: $messageId")
            }.addOnFailureListener {
                Log.e("Message", "Failed to send vanishing message", it)
            }
    }

    fun listenForMessages(chatId: String) {

        _channelRef.child(chatId).child("messages")
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val message = snapshot.getValue(VanishingMessage::class.java)
                    message?.let {
                        val updatedMessages = _privateMessageList.value.toMutableList()

                        // Ensure no duplicates
                        if (updatedMessages.none { it.messageId == message.messageId }) {
                            updatedMessages.add(0, message) // Add at the top for newest first
                            _privateMessageList.value = updatedMessages
                        }
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    val updatedMessage = snapshot.getValue(VanishingMessage::class.java)
                    updatedMessage?.let { msg ->
                        val messagesList = _privateMessageList.value.toMutableList()
                        val index = messagesList.indexOfFirst { it.messageId == msg.messageId }

                        if (index >= 0) {
                            messagesList[index] = msg
                            _privateMessageList.value = messagesList
                        }

                        if (msg.isSeen) {
                            deleteMessageAfterRead(chatId, msg.messageId)
                        }
                    }
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    val removedMessage = snapshot.getValue(VanishingMessage::class.java)
                    removedMessage?.let { msg ->
                        val messagesList = _privateMessageList.value.toMutableList()
                        messagesList.removeAll { it.messageId == msg.messageId }
                        _privateMessageList.value = messagesList
                    }
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    Log.d("PrivateChannel", "Message moved")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("PrivateChannel", "Failed to listen for messages: ${error.message}")
                }
            })
    }


    private fun deleteMessageAfterRead(chatId: String, messageId: String) {
        _channelRef.child(chatId).child("messages").child(messageId).removeValue()
            .addOnSuccessListener {
                Log.d("Message", "Message deleted after reading: $messageId")
            }.addOnFailureListener {
                Log.e("Message", "Failed to delete message after reading", it)
            }
    }

    fun requestId(onResult: (String) -> Unit) {
        val channelId = generateUniqueId()

        _channelRef.child(channelId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                requestId(onResult)
            } else {
                onResult(channelId)
            }
        }.addOnFailureListener {
            Log.d("RequestIdError", "Failed to check group ID existence: ${it.message}")
        }
    }

    fun markMessagesAsSeen(chatId: String, uid: String) {
        _channelRef.child(chatId).child("messages")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach { child ->
                        val message = child.getValue(VanishingMessage::class.java)
                        message?.let {
                            if (it.senderId != uid && !it.isSeen) {
                                // Update message to seen
                                _channelRef.child(chatId).child("messages")
                                    .child(it.messageId)
                                    .child("isSeen")
                                    .setValue(true)
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Chatroom", "Failed to mark messages as seen: ${error.message}")
                }
            })
    }

    private fun deleteSeenMessages(chatId: String) {
        _channelRef.child(chatId).child("messages")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach { child ->
                        val message = child.getValue(VanishingMessage::class.java)
                        message?.let {
                            if (it.isSeen) {
                                _channelRef.child(chatId).child("messages").child(it.messageId)
                                    .removeValue()
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Chatroom", "Failed to delete seen messages: ${error.message}")
                }
            })
    }


    private fun generateUniqueId(): String {
        return UUID.randomUUID().toString().replace("-", "").take(10)
    }

    fun convertTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("hh:mm a  dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun deleteChannel(channelId: String, callback: () -> Unit) {
        _channelRef.child(channelId).removeValue().addOnSuccessListener {
            callback()
        }
    }
}

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