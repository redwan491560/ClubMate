package com.example.clubmate.viewmodel

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.example.clubmate.db.UserModel
import com.example.clubmate.db.UserState
import com.example.clubmate.screens.MessageStatus
import com.example.clubmate.util.MessageType
import com.example.clubmate.util.chat.Message
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

open class ChatViewModel : AuthViewModel() {

    // db reference


    private val _db = FirebaseDatabase.getInstance()
    private val chatRef = _db.getReference("chat")
    private val userRef = _db.getReference("user")


    // message list
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    // chats lists
    private val _chats = MutableStateFlow<List<Chats>>(emptyList())
    val chats: StateFlow<List<Chats>> = _chats


    // userdata
    var userState by mutableStateOf<UserState>(UserState.Success(null))
        private set

    // fetches he searched result
    var user by mutableStateOf<UserModel?>(UserModel())


    init {
        super.checkAuthenticationStatus()

    }

    private suspend fun fetchUsers(senderId: String, receiverId: String): List<UserModel> {
        val users = mutableListOf<UserModel>()
        suspend fun fetchUser(uid: String): UserModel? = suspendCoroutine { continuation ->
            fetchUserByUid(uid) { user ->
                continuation.resume(user)
            }
        }

        val user1 = fetchUser(senderId)
        val user2 = fetchUser(receiverId)

        user1?.let { users.add(it) }
        user2?.let { users.add(it) }

        return users
    }


    fun initiateChat(
        senderId: String, receiverId: String, message: String = "", onClick: (String) -> Unit
    ) {
        val chatId = getChatId(senderId, receiverId)

        viewModelScope.launch {
            val users = fetchUsers(senderId, receiverId)

            if (users.size == 2) {
                onClick(chatId)
                chatRef.child(chatId).child("participants").setValue(users).addOnCompleteListener {
                    sendMessage(
                        chatId = chatId,
                        messageText = message,
                        receiverId = receiverId,
                        senderId = senderId
                    )
                }.addOnFailureListener {
                    Log.d("failed to initiate chat", "initiateChat: ")
                }
            } else {
                Log.d("failed to initiate chat", "initiateChat: ")
            }
        }
    }

    private fun saveMessageToDatabase(
        chatId: String,
        messageId: String,
        messageData: Message
    ) {

        chatRef.child(chatId).child("messages").child(messageId)
            .setValue(messageData)
            .addOnSuccessListener {
                Log.d("Success", "Activity added successfully")
            }
            .addOnFailureListener { e ->
                Log.e("Failure", "Failed to add activity: ${e.message}")
            }
    }


    fun sendMessage(
        chatId: String,
        senderId: String,
        receiverId: String,
        messageText: String = "",
        imageUri: Uri? = null,
    ) {

        viewModelScope.launch {
            val messageId = chatRef.child(chatId).push().key ?: return@launch
            val timestamp = System.currentTimeMillis()

            if (imageUri != null) {
                // If an image is present, upload and then proceed
                uploadImageToStorage(imageUri, chatId) { imageUrl ->
                    val messageData = Message(
                        messageId = messageId,
                        senderId = senderId,
                        receiverId = receiverId,
                        messageText = "",
                        imageRef = imageUrl,
                        timestamp = timestamp,
                        messageType = MessageType.Text,
                        status = MessageStatus.SENDING
                    )
                    saveMessageToDatabase(chatId, messageId, messageData)
                    updateLastMessage(chatId, messageData)
                }
            } else {
                val messageData = Message(
                    messageId = messageId,
                    senderId = senderId,
                    receiverId = receiverId,
                    messageText = messageText,
                    imageRef = "",
                    timestamp = timestamp,
                    messageType = MessageType.Text,
                    status = MessageStatus.SENDING
                )
                saveMessageToDatabase(chatId, messageId, messageData)
                updateLastMessage(chatId, messageData)

            }

        }
    }

    private fun uploadImageToStorage(imageUri: Uri, chatId: String, onComplete: (String) -> Unit) {

        val requestId = MediaManager.get().upload(imageUri)
            .option("folder", "group_images/$chatId") // Store images inside "group_images/{grpId}"
            .callback(object : com.cloudinary.android.callback.UploadCallback {
                override fun onStart(requestId: String?) {
                    Log.d("Cloudinary", "Upload started")
                }

                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                    Log.d("Cloudinary", "Uploading: $bytes/$totalBytes")
                }

                override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
                    val imageUrl = resultData?.get("secure_url") as? String
                    if (imageUrl != null) {
                        onComplete(imageUrl) // Pass the Cloudinary image URL to save in the database
                    }
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    Log.e("Cloudinary", "Upload rescheduled")

                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                    Log.e("Cloudinary", "Upload rescheduled")

                }
            })
            .dispatch()
    }

    private fun updateLastMessage(chatId: String, messageData: Message) {
        chatRef.child(chatId).child("msg").child("last").setValue(messageData)
            .addOnFailureListener {
                Log.e("Message", "Error updating last message")
            }
    }


    fun receiveMessage(chatId: String) {

        viewModelScope.launch {
            chatRef.child(chatId).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    chatRef.child(chatId).child("messages")
                        .addChildEventListener(object : ChildEventListener {
                            override fun onChildAdded(
                                snapshot: DataSnapshot, previousChildName: String?
                            ) {
                                val message = snapshot.getValue(Message::class.java)
                                message?.let {
                                    val updMsg = it.copy(messageText = it.messageText)
                                    val updatedMessages = _messages.value.toMutableList()
                                    if (updatedMessages.none { existingMessage ->
                                            existingMessage.timestamp == updMsg.timestamp
                                        }) {
                                        updatedMessages.add(updMsg)
                                        _messages.value = updatedMessages
                                    }
                                }
                            }

                            override fun onChildChanged(
                                snapshot: DataSnapshot, previousChildName: String?
                            ) {
                                val updatedMessage = snapshot.getValue(Message::class.java)
                                updatedMessage?.let {
                                    //    val decryptedMessageText = decryptMessage(it.messageText)
                                    val newMessage = it.copy(messageText = it.messageText)

                                    val messagesList = _messages.value.toMutableList()
                                    val index = messagesList.indexOfFirst { ind ->
                                        ind.timestamp == newMessage.timestamp
                                    }
                                    if (index >= 0) {
                                        messagesList[index] = newMessage
                                        _messages.value = messagesList
                                    }
                                }
                            }

                            override fun onChildRemoved(snapshot: DataSnapshot) {
                                val removedMessage = snapshot.getValue(Message::class.java)
                                removedMessage?.let {
                                    val messagesList = _messages.value.toMutableList()

                                    messagesList.removeIf { message ->
                                        message.timestamp == removedMessage.timestamp
                                    }
                                    _messages.value = messagesList
                                }
                            }

                            override fun onChildMoved(
                                snapshot: DataSnapshot, previousChildName: String?
                            ) {
                                Log.d("TAG", "onChildMoved: ")
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.d("TAG", "onChildMoved: ")
                            }

                        })
                } else {
                    //  receiveMessageReq(receiverId)
                }
            }.addOnFailureListener {
                Log.e(
                    "Message", "Error receiving  message"
                )
            }
        }
    }


    fun convertTimestampToDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("hh:mm a  dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun clearMessage() {
        _messages.value = emptyList()
    }

    fun getParticipants(
        chatId: String, onResult: (List<UserModel>) -> Unit
    ) {

        chatRef.child(chatId).child("participants")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val participants = mutableListOf<UserModel>()
                    if (snapshot.exists()) {
                        for (child in snapshot.children) {
                            val participant = child.getValue(UserModel::class.java)
                            if (participant != null) {
                                participants.add(participant)
                            }
                        }
                    }
                    onResult(participants)
                }

                override fun onCancelled(error: DatabaseError) {
                    onResult(emptyList())
                }
            })
    }


    // message edit

    fun deleteIndividualMessage(chatId: String, messageId: String) {

        chatRef.child(chatId).child("messages").child(messageId).removeValue()
            .addOnCompleteListener {
                // onResult()
                Log.d("TAG", "deleteIndividualMessage: succ")
            }.addOnFailureListener {
                Log.d("TAG", "deleteIndividualMessage: failed")
            }
    }


    // chat section
    fun listenForChats(receiverId: String) {

        viewModelScope.launch {
            chatRef.addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val chatId = snapshot.key
                    if (chatId != null && chatId.contains(receiverId)) {
                        val participants = chatId.split("+")

                        if (participants.contains(receiverId)) {
                            val newChat = Chats(chatId = chatId)

                            getLastMessage(chatId) { lstMsg ->
                                lstMsg?.let {
                                    newChat.lastMessage = it
                                }
                            }

                            getParticipants(chatId) { prt ->
                                newChat.participants = prt
                            }

                            // Update the _contact list
                            val updatedChats = _chats.value.toMutableList()
                            val index = updatedChats.indexOfFirst {
                                it.chatId == chatId
                            }
                            if (index >= 0) {
                                updatedChats[index] = newChat
                            } else {
                                updatedChats.add(newChat)
                            }
                            _chats.value = updatedChats
                        }
                    }
                }


                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    val chatId = snapshot.key
                    if (chatId != null && chatId.contains(receiverId)) {
                        val participants = chatId.split("+")

                        if (participants.size == 2 && participants.contains(receiverId)) {
                            val updChat = Chats(chatId = chatId)

                            // Correct approach
                            getLastMessage(chatId) { lstMsg ->
                                lstMsg?.let {
                                    updChat.lastMessage =
                                        it // This updates the value of the StateFlow
                                }
                            }

                            getParticipants(chatId) { prt ->
                                updChat.participants = prt
                            }

                            val updatedChats = _chats.value.toMutableList()
                            val index = updatedChats.indexOfFirst { it.chatId == chatId }
                            if (index >= 0) {
                                updatedChats[index] = updChat
                                _chats.value = updatedChats
                            }
                        }
                    }
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    val chatId = snapshot.key
                    if (chatId != null) {
                        // Remove the chat from the list
                        val updatedChats = _chats.value.toMutableList()
                        val index = updatedChats.indexOfFirst { it.chatId == chatId }
                        if (index >= 0) {
                            updatedChats.removeAt(index)
                            _chats.value = updatedChats
                        }
                    }
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    // Not needed for this use case
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ChatListener", "Error listening for chats: ${error.message}")
                }
            })
        }
    }

    private fun getLastMessage(chatId: String, onResult: (Message?) -> Unit) {

        chatRef.child(chatId).child("msg").child("last")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val lastMsg = snapshot.getValue(Message::class.java)
                        onResult(lastMsg)
                    } else {
                        onResult(null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("getLastMessage", "Error fetching last message: ${error.message}")
                    onResult(null)
                }
            })
    }


    private fun generateChatID(senderId: String, receiverId: String): String {
        val sortedIds = listOf(senderId, receiverId).sorted()
        val chatID = "${sortedIds[0]}+${sortedIds[1]}"

        return chatID
    }

    private fun getChatId(receiverId: String, senderId: String): String {
        var chatId = generateChatID(receiverId = receiverId, senderId = senderId)

        chatRef.child(chatId).get().addOnSuccessListener { snapshot ->
            chatId = if (snapshot.exists()) {
                // Return existing chat ID
                snapshot.key.toString()
            } else {
                // Chat ID does not exist, return the generated one
                snapshot.key.toString()
            }
        }.addOnFailureListener {
            Log.e("Firebase", "Error fetching chats: ${it.message}")
        }
        return chatId
    }


// find user section

    fun findUser(search: String) {
        if (search.isEmpty()) {
            userState = UserState.Error("Query cannot be empty")
            return
        }

        userState = UserState.Loading
        viewModelScope.launch {
            find(search) { result ->
                if (result != null) {
                    user = result
                    userState = UserState.Success(result)
                } else {
                    userState = UserState.Error("User not found")
                }
            }
        }
    }


    fun emptyUser() {
        user = null
        userState = UserState.Success(null)
    }

    fun setUserEmpty(txt: String) {
        user = null
        userState = UserState.Error(txt)
    }

    fun fetchUserByUid(uid: String, callback: (UserModel?) -> Unit) {

        userRef.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val userInfo = snapshot.getValue(UserModel::class.java)
                    callback(userInfo) // Return user information via the callback
                } else {
                    Log.e("fetchUserInfoByUid", "No user found with UID: $uid")
                    callback(null) // No user found
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("fetchUserInfoByUid", "Error fetching user info: ${error.message}")
                callback(null) // On error, return null
            }
        })
    }


    private fun find(search: String, onResult: (UserModel?) -> Unit) {

        userRef.orderByChild("email").equalTo(search)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (childSnapshot in snapshot.children) {
                            val usr = childSnapshot.getValue(UserModel::class.java)
                            onResult(usr)
                            return
                        }
                    } else {
                        userRef.orderByChild("username").equalTo(search)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {
                                        for (childSnapshot in snapshot.children) {
                                            val usr = childSnapshot.getValue(UserModel::class.java)
                                            onResult(usr)
                                            return
                                        }
                                    } else {
                                        userState = UserState.Error("User doesn't exist")
                                        onResult(null)
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    userState = UserState.Error(error.message)
                                    onResult(null)
                                }
                            })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    userState = UserState.Error(error.message)
                    onResult(null)
                }
            })
    }

}

data class Chats(
    val chatId: String = "",
    var lastMessage: Message? = Message(),
    var participants: List<UserModel> = listOf(UserModel())
)

