package com.example.clubmate.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.utils.ObjectUtils
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
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
        channelId: String, uid: String, messageText: String = "", imageUri: Uri? = null
    ) {
        viewModelScope.launch {
            val messageId =
                _channelRef.child(channelId).child("messages").push().key ?: return@launch
            val timestampSent = System.currentTimeMillis()

            if (imageUri != null) {
                uploadImageToStorage(imageUri, channelId) { imageUrl ->
                    val message = VanishingMessage(
                        messageId = messageId,
                        messageText = "",
                        imageUrl = imageUrl,
                        senderId = uid,
                        timestampSent = timestampSent,
                        isSeen = false
                    )
                    saveVanishingMessage(channelId, messageId, message)
                }
            } else {
                val message = VanishingMessage(
                    messageId = messageId,
                    messageText = messageText,
                    imageUrl = "",
                    senderId = uid,
                    timestampSent = timestampSent,
                    isSeen = false
                )
                saveVanishingMessage(channelId, messageId, message)
            }
        }
    }

    // Save message to the database
    private fun saveVanishingMessage(chatId: String, messageId: String, message: VanishingMessage) {
        _channelRef.child(chatId).child("messages").child(messageId).setValue(message)
            .addOnSuccessListener {
                Log.d("Message", "Vanishing message sent: $messageId")
            }.addOnFailureListener {
                Log.e("Message", "Failed to send vanishing message", it)
            }
    }

    // Upload image to Cloudinary and return the URL
    private fun uploadImageToStorage(imageUri: Uri, chatId: String, onComplete: (String) -> Unit) {
        MediaManager.get().upload(imageUri).option(
            "folder", "vanishing_messages/$chatId"
        ).callback(object : com.cloudinary.android.callback.UploadCallback {
            override fun onStart(requestId: String?) {
                Log.d("Cloudinary", "Upload started")
            }

            override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                Log.d("Cloudinary", "Uploading: $bytes/$totalBytes")
            }

            override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                val imageUrl = resultData?.get("secure_url") as? String
                if (imageUrl != null) {
                    onComplete(imageUrl)
                }
            }

            override fun onError(requestId: String?, error: ErrorInfo?) {
                Log.e("Cloudinary", "Upload failed: ${error?.description}")
            }

            override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                Log.e("Cloudinary", "Upload rescheduled")
            }
        }).dispatch()
    }


    private fun deleteImageFromCloudinary(imageUri: String, onComplete: (Boolean) -> Unit) {
        try {
            // Extract the public ID from the Cloudinary URL
            val publicId = extractPublicIdFromUrl(imageUri)

            if (publicId.isNullOrEmpty()) {
                Log.e("Cloudinary", "Invalid image URI: $imageUri")
                onComplete(false)
                return
            }

            // Perform the deletion
            MediaManager.get().cloudinary.uploader().destroy(
                publicId,
                ObjectUtils.emptyMap(),
            )
        } catch (e: Exception) {
            Log.e("Cloudinary", "Error deleting image: ${e.message}")
            onComplete(false)
        }
    }

    // Helper function to extract the public ID from the Cloudinary image URL
    private fun extractPublicIdFromUrl(imageUrl: String): String? {
        val regex = Regex(".*/upload/(?:v\\d+/)?([^/.]+)")
        val matchResult = regex.find(imageUrl)
        return matchResult?.groupValues?.get(1)
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
                                _channelRef.child(chatId).child("messages").child(it.messageId)
                                    .child("isSeen").setValue(true)
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

    fun deleteIndividualMessage(
        channelId: String, message: VanishingMessage, onComplete: (Boolean) -> Unit
    ) {
        if (message.imageUrl.isNotEmpty()) {
            deleteImageFromCloudinary(message.imageUrl) { imageDeleted ->
                if (imageDeleted) {
                    _channelRef.child(channelId).child("messages").child(message.messageId)
                        .removeValue().addOnSuccessListener { onComplete(true) }
                        .addOnFailureListener { onComplete(false) }
                } else {
                    Log.e("DeleteMessage", "Image deletion failed, message not deleted")
                    onComplete(false)
                }
            }
        } else {
            _channelRef.child(channelId).child("messages").child(message.messageId).removeValue()
                .addOnSuccessListener { onComplete(true) }
                .addOnFailureListener { onComplete(false) }
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