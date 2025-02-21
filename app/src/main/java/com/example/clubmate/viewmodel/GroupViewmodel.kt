package com.example.clubmate.viewmodel

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.example.clubmate.db.GroupState
import com.example.clubmate.db.Routes
import com.example.clubmate.db.UserState
import com.example.clubmate.util.Category
import com.example.clubmate.util.group.EventCategory
import com.example.clubmate.util.group.GroupMessage
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.coroutines.resume

class GroupViewmodel : ViewModel() {

    private val _db = FirebaseDatabase.getInstance()
    private val grpRef = _db.getReference("groups")
    private val userRef = _db.getReference("user")


    // searched user or group
    var userState by mutableStateOf<UserState>(UserState.Success(null))
        private set
    var groupState by mutableStateOf<GroupState>(GroupState.Success(null))
        private set


    // fetches he searched result
    var user by mutableStateOf<Routes.UserModel?>(Routes.UserModel())
    var group by mutableStateOf<Routes.GrpDetails?>(Routes.GrpDetails())


    private val _groupsList = MutableStateFlow<List<GroupDetails>>(emptyList())
    val groupsList = _groupsList

    private val _grpStatus = MutableLiveData<GroupStatus>()
    val grpStatus = _grpStatus

    private val _eventList = MutableStateFlow<List<EventData>>(emptyList()) // This holds the state
    val eventList: StateFlow<List<EventData>> = _eventList // Exposed as read-only

    private val _grpActivity = MutableStateFlow<List<GroupActivity>>(emptyList())
    val grpActivity = _grpActivity

    private val _participantsList = MutableStateFlow<List<UserJoinDetails>>(emptyList())
    val participantsList = _participantsList


    fun requestId(onResult: (String) -> Unit) {
        val grpId = generateUniqueId()

        grpRef.child(grpId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                // if id is found regenerate using recursion
                requestId(onResult)
            } else {
                // return the id
                onResult(grpId)
            }
        }.addOnFailureListener {
            Log.d("RequestIdError", "Failed to check group ID existence: ${it.message}")
        }
    }


    fun fetchUserDetailsByUid(uid: String, onResult: (Routes.UserModel?) -> Unit) {

        userRef.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(Routes.UserModel::class.java)
                    user?.let {
                        onResult(user)
                    }
                } else onResult(null)

            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("request canceled", "onCancelled: ")
            }
        })
    }


    // group fun s


    fun createGroup(
        grpId: String,
        createdBy: String,
        grpName: String,
        description: String,
        onResult: (Routes.GrpDetails?) -> Unit
    ) {

        val createdAt = System.currentTimeMillis()
        val grpDetails = Routes.GrpDetails(
            createdAt = createdAt,
            createdBy = createdBy,
            description = description,
            grpId = grpId,
            photoUrl = "",
            grpName = grpName,
        )

        grpStatus.value = GroupStatus.Loading
        grpRef.child(grpId).child("grpInfo").setValue(grpDetails).addOnSuccessListener {

            grpStatus.value = GroupStatus.Success
            fetchUserDetailsByUid(createdBy) { admin ->
                admin?.let {
                    addParticipants(grpId = grpId, email = admin.email, category = Category.Admin)
                }

                // save info to the user
                userRef.child(createdBy).child("groups_connected")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val groupList =
                                snapshot.getValue(object : GenericTypeIndicator<List<String>>() {})
                                    ?.toMutableList() ?: mutableListOf()

                            if (!groupList.contains(grpId)) {
                                groupList.add(grpId)

                                // Update the list back to Firebase
                                userRef.child(createdBy).child("groups_connected")
                                    .setValue(groupList).addOnSuccessListener {
                                        Log.d("Firebase", "List updated successfully!")
                                    }.addOnFailureListener { exception ->
                                        Log.e(
                                            "Firebase", "Error updating list: ${exception.message}"
                                        )
                                    }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("createGroup", "Error updating user groups: ${error.message}")
                        }
                    })
            }
            onResult(grpDetails)

        }.addOnFailureListener { exception ->
            grpStatus.value = GroupStatus.Failed
            Log.e("createGroup", "Error creating group: ${exception.message}")
            onResult(null)
        }
    }


    fun updateGroupProfilePicture(grpId: String, uri: Uri, onComplete: (Boolean) -> Unit) {

        uploadPPtoDB(imageUri = uri, grpId = grpId) { path ->
            grpRef.child(grpId).child("grpInfo").child("photoUrl").setValue(path)
                .addOnSuccessListener { onComplete(true) }
                .addOnFailureListener { onComplete(false) }
        }

    }

    private fun uploadPPtoDB(imageUri: Uri, grpId: String, onComplete: (String) -> Unit) {
        val requestId = MediaManager.get().upload(imageUri)
            .option("folder", "group_images/$grpId")
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
            }).dispatch()
    }

    fun loadGroupInfo(grpId: String, onResult: (Routes.GrpDetails?) -> Unit) {

        grpRef.child(grpId).child("grpInfo").get().addOnSuccessListener {
            if (it.exists()) {
                val grpInfo = it.getValue(Routes.GrpDetails::class.java)
                onResult(grpInfo)
            } else {
                onResult(null)
            }
        }.addOnFailureListener {
            onResult(null)
        }
    }

    // groups
    fun listenForGroups(uid: String) {
        userRef.child(uid).child("groups_connected")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        _groupsList.value = emptyList()
                        return
                    }

                    val groupIds =
                        snapshot.getValue(object : GenericTypeIndicator<List<String>>() {})
                            ?: emptyList()

                    if (groupIds.isEmpty()) {
                        _groupsList.value = emptyList() // No groups found
                        return
                    }

                    val deferredList = mutableListOf<Deferred<GroupDetails?>>()

                    viewModelScope.launch {
                        groupIds.forEach { grpId ->
                            val deferred = async {
                                try {
                                    val groupDetails = fetchGroupDetails(grpId)
                                    groupDetails
                                } catch (e: Exception) {
                                    Log.e(
                                        "listenForGroups",
                                        "Error fetching group details: ${e.message}"
                                    )
                                    null
                                }
                            }
                            deferredList.add(deferred)
                        }

                        val results = deferredList.awaitAll().filterNotNull()
                        _groupsList.value = results // Only update once all groups have been fetched
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("listenForGroups", "Error fetching user groups: ${error.message}")
                }
            })
    }

    private suspend fun fetchGroupDetails(grpId: String): GroupDetails? {
        return suspendCancellableCoroutine { continuation ->
            grpRef.child(grpId).child("grpInfo")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(grpSnapshot: DataSnapshot) {
                        if (!grpSnapshot.exists()) {
                            if (continuation.isActive) {
                                continuation.resume(null)
                            }
                            return
                        }

                        val groupDetails = grpSnapshot.getValue(GroupDetails::class.java)
                        if (groupDetails == null) {
                            if (continuation.isActive) {
                                continuation.resume(null)
                            }
                            return
                        }

                        // Get last activity and then resume once
                        getLastActivity(groupDetails.grpId) { lastActivity ->
                            // Check if the continuation is still active before resuming
                            if (continuation.isActive) {
                                groupDetails.lastActivity =
                                    lastActivity ?: GroupActivity() // Default if null
                                continuation.resume(groupDetails)
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("fetchGroupDetails", "Error fetching group info: ${error.message}")
                        if (continuation.isActive) {
                            continuation.resume(null)
                        }
                    }
                })
        }
    }

    fun leaveGroup(uid: String, grpId: String, onResult: (Boolean) -> Unit) {

        userRef.child(uid).child("groups_connected")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val groupIds = snapshot.getValue(object :
                            GenericTypeIndicator<MutableList<String>>() {})
                        if (groupIds != null && groupIds.contains(grpId)) {
                            groupIds.remove(grpId) // Remove the group ID from the list

                            // Update the modified list back to the database
                            userRef.child(uid).child("groups_connected").setValue(groupIds)
                                .addOnSuccessListener {
                                    Log.d("leaveGroup", "Successfully removed group: $grpId")
                                    onResult(true)
                                }.addOnFailureListener { error ->
                                    Log.e(
                                        "leaveGroup",
                                        "Failed to update groups_connected: ${error.message}"
                                    )
                                    onResult(false)
                                }
                        } else {
                            Log.d("leaveGroup", "Group ID not found in groups_connected")
                            onResult(false)
                        }
                    } else {
                        Log.d("leaveGroup", "No groups_connected found for user")
                        onResult(false)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("leaveGroup", "Database error: ${error.message}")
                    onResult(false)
                }
            })
    }

    private fun joinGroup(uid: String, grpId: String, onComplete: (Boolean) -> Unit) {

        grpRef.child(grpId).get().addOnSuccessListener { groupSnapshot ->
            if (groupSnapshot.exists()) {
                fetchUserDetailsByUid(uid) { userModel ->
                    if (userModel != null) {
                        addParticipants(
                            grpId = grpId, email = userModel.email, category = Category.General
                        )

                        // Fetch updated group details
                        grpRef.child(grpId).child("grpInfo")
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {
                                        val groupDetails =
                                            snapshot.getValue(GroupDetails::class.java)
                                        groupDetails?.let { newGroup ->
                                            updateGroupList(newGroup)
                                        }
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.e(
                                        "joinGroup",
                                        "Error fetching group details: ${error.message}"
                                    )
                                }
                            })
                        onComplete(true)
                    } else {
                        onComplete(false)
                    }
                }
            } else {
                onComplete(false)
            }
        }.addOnFailureListener {
            onComplete(false)
        }
    }


    private fun updateGroupList(newGroup: GroupDetails) {

        val currentList = _groupsList.value.toMutableList()

        val index = currentList.indexOfFirst { it.grpId == newGroup.grpId }
        if (index != -1) {
            currentList[index] = newGroup // Update existing entry
        } else {
            currentList.add(newGroup) // Add new group
        }

        _groupsList.value = currentList
    }


    // participants

    fun addParticipants(
        email: String, category: Category = Category.General, grpId: String
    ) {
        val joiningTime = System.currentTimeMillis()

        // Fetch user details by email
        find(email) { user ->
            user?.let {
                val userData = UserJoinDetails(
                    email = user.email,
                    username = user.username,
                    phone = user.phone,
                    uid = user.uid,
                    joinData = joiningTime,
                    userType = category
                )

                // Add user to group participants
                grpRef.child(grpId).child("participants").child(user.uid).setValue(userData)
                    .addOnSuccessListener {
                        // Successfully added to group, now update the user's groups_connected field
                        val userGroupsRef = userRef.child(user.uid).child("groups_connected")
                        userGroupsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val currentGroups = snapshot.getValue(object :
                                    GenericTypeIndicator<List<String>>() {})?.toMutableList()
                                    ?: mutableListOf()

                                if (!currentGroups.contains(grpId)) {
                                    currentGroups.add(grpId)  // Corrected this line to add the group ID

                                    // Update the user's groups_connected field with the new list
                                    userGroupsRef.setValue(currentGroups).addOnSuccessListener {
                                        Log.d(
                                            "User joined Success",
                                            "addParticipants: User added to group successfully"
                                        )
                                    }.addOnFailureListener { error ->
                                        Log.d(
                                            "User joined failed",
                                            "addParticipants (user update failed): ${error.message}"
                                        )
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.d(
                                    "User joined failed",
                                    "addParticipants (user update cancelled): ${error.message}"
                                )
                            }
                        })
                    }.addOnFailureListener { error ->
                        Log.d("User joined failed", "addParticipants: ${error.message}")
                    }
            }
        }
    }


    fun removeParticipants(email: String, grpId: String) {

        grpRef.child(grpId).child("participants").orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (child in snapshot.children) {
                            child.ref.removeValue().addOnSuccessListener {
                                Log.d(
                                    "removeParticipants",
                                    "User removed from group participants successfully"
                                )

                                // Now, also remove the user from the user's `groups_connected` list
                                removeUserFromGroupsConnected(child.key, grpId)
                            }.addOnFailureListener { error ->
                                Log.e(
                                    "removeParticipants",
                                    "Failed to remove user from group participants: ${error.message}"
                                )
                            }
                        }
                    } else {
                        Log.d("removeParticipants", "User not found in participants")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("removeParticipants", "Error fetching participants: ${error.message}")
                }
            })
    }

    private fun removeUserFromGroupsConnected(userUid: String?, grpId: String) {
        // Get the user's groups_connected reference

        if (userUid != null) {
            userRef.child(userUid).child("groups_connected")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val currentGroups =
                            snapshot.getValue(object : GenericTypeIndicator<List<String>>() {})
                                ?.toMutableList() ?: mutableListOf()

                        // Remove the group ID from the list if it exists
                        if (currentGroups.contains(grpId)) {
                            currentGroups.remove(grpId)
                            userRef.child(userUid).child("groups_connected").setValue(currentGroups)
                                .addOnSuccessListener {
                                    Log.d(
                                        "removeParticipants",
                                        "Group removed from user's groups_connected successfully"
                                    )
                                }.addOnFailureListener { error ->
                                    Log.e(
                                        "removeParticipants",
                                        "Failed to remove group from user's groups_connected: ${error.message}"
                                    )
                                }
                        } else {
                            Log.d(
                                "removeParticipants", "Group not found in user's groups_connected"
                            )
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e(
                            "removeParticipants",
                            "Error fetching user's groups_connected: ${error.message}"
                        )
                    }
                })
        }
    }


    fun getAllParticipants(grpId: String) {

        grpRef.child(grpId).child("participants") // Access only participants, excluding `admin`
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val participants = mutableListOf<UserJoinDetails>()

                        for (child in snapshot.children) {
                            val prt = child.getValue(UserJoinDetails::class.java)
                            prt?.let {
                                participants.add(it)
                            }
                        }

                        _participantsList.value = participants // Update the list
                    } else {
                        _participantsList.value = emptyList() // No participants found
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("getAllParticipants", "Error fetching participants: ${error.message}")
                }
            })
    }


    fun getParticipantDetails(grpId: String, userId: String, onResult: (UserJoinDetails?) -> Unit) {

        grpRef.child(grpId).child("participants").child(userId).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val userDetails = snapshot.getValue(UserJoinDetails::class.java)
                    onResult(userDetails) // Return user details
                } else {
                    onResult(null) // User not found
                }
            }.addOnFailureListener {
                onResult(null) // User not found
            } // Get user's category
    }


    fun checkAdmin(grpId: String, userId: String, onResult: (Boolean) -> Unit) {
        grpRef.child(grpId).child("participants").child(userId) // Get user's category
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val userDetails = snapshot.getValue(UserJoinDetails::class.java)
                        onResult(userDetails?.userType == Category.Admin) // Check if Admin
                    } else {
                        onResult(false) // User not found
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("checkAdmin", "Error checking admin status: ${error.message}")
                    onResult(false) // Default to false on error
                }
            })
    }

    private val _requestList = MutableStateFlow<List<RequestMap>>(emptyList())
    val requestList = _requestList

    fun listenToRequest(grpId: String) {
        grpRef.child(grpId).child("request")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val requestList = mutableListOf<RequestMap>()

                    // Loop through all children inside "request"
                    for (childSnapshot in snapshot.children) {
                        val request = childSnapshot.getValue(RequestMap::class.java)
                        request?.let { requestList.add(it) } // Add to list if not null
                    }

                    _requestList.value = requestList // Update StateFlow
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Error fetching requests: ${error.message}")
                }
            })
    }


    fun acceptRequest(grpId: String, requestMap: RequestMap) {
        joinGroup(uid = requestMap.uid, grpId = grpId) {
            if (it) {
                grpRef.child(grpId).child("request").child(requestMap.uid).removeValue()
                    .addOnSuccessListener {
                        _requestList.value = _requestList.value.filter { request ->
                            request.uid != requestMap.uid
                        }
                    }
                    .addOnFailureListener { error ->
                        println("Failed to remove request: ${error.message}")
                    }
            }
        }
    }

    fun declineRequest(grpId: String, item: RequestMap) {
        grpRef.child(grpId).child("request").child(item.uid).removeValue()
            .addOnSuccessListener {
                _requestList.value = _requestList.value.filter { request ->
                    request.uid != item.uid
                }
            }
            .addOnFailureListener { error ->
                println("Failed to remove request: ${error.message}")
            }
    }

    fun sendJoinRequest(
        uid: String,
        email: String,
        username: String,
        grpId: String,
        onComplete: (Boolean) -> Unit
    ) {

        val requestTime = System.currentTimeMillis()
        val requestData = RequestMap(
            uid = uid, username = username, email = email, sentTime = requestTime
        )

        grpRef.child(grpId).child("request").child(uid).setValue(requestData)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }

    }


    fun updateUserRole(
        uid: String,
        category: Category,
        grpId: String,
        onComplete: (Boolean) -> Unit
    ) {

        val participantsRef = grpRef.child(grpId).child("participants")

        participantsRef.child(uid).child("userType").setValue(category)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }

    }


    // activities

    fun addActivity(
        grpId: String, senderId: String, messageText: String = "", imageUri: Uri? = null
    ) {

        viewModelScope.launch {
            val messageId = grpRef.child(grpId).push().key ?: return@launch
            val timestamp = System.currentTimeMillis()

            if (imageUri != null) {
                // If an image is present, upload and then proceed
                uploadImageToStorage(imageUri = imageUri, grpId = grpId) { imageUrl ->
                    val activityData = GroupActivity(
                        message = GroupMessage(
                            timestamp = timestamp,
                            messageId = messageId,
                            imageRef = imageUrl,  // Store the uploaded image URL
                            senderId = senderId,
                            messageText = "" // No text since it's an image message
                        ), type = GroupActivityType.Image
                    )
                    saveActivityToDatabase(grpId, messageId, activityData)
                    updateLastActivity(grpId, activityData)
                }
            } else {
                // Text message scenario
                val activityData = GroupActivity(
                    message = GroupMessage(
                        timestamp = timestamp, messageId = messageId, imageRef = "",  // No image
                        senderId = senderId, messageText = messageText
                    ), type = GroupActivityType.Text
                )
                saveActivityToDatabase(grpId, messageId, activityData)
                updateLastActivity(grpId, activityData)
            }
        }
    }


    private fun uploadImageToStorage(
        imageUri: Uri,
        grpId: String,
        onComplete: (String) -> Unit
    ) {

        val requestId = MediaManager.get().upload(imageUri)
            .option("folder", "group_images/$grpId")
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
            }).dispatch()
    }

    private fun updateLastActivity(grpId: String, activityData: GroupActivity) {
        grpRef.child(grpId).child("activities").child("lastAct").setValue(activityData)
            .addOnSuccessListener {
                Log.d("Success", "Activity added successfully")
            }.addOnFailureListener { e ->
                Log.e("Failure", "Failed to add activity: ${e.message}")
            }
    }

    private fun saveActivityToDatabase(
        grpId: String, messageId: String, activityData: GroupActivity
    ) {

        grpRef.child(grpId).child("activities").child(messageId).setValue(activityData)
            .addOnSuccessListener {
                Log.d("Success", "Activity added successfully")
            }.addOnFailureListener { e ->
                Log.e("Failure", "Failed to add activity: ${e.message}")
            }
    }

    fun getLastActivity(grpId: String, onResult: (GroupActivity?) -> Unit) {

        grpRef.child(grpId).child("activities").child("lastAct")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val lastMsg = snapshot.getValue(GroupActivity::class.java)
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


    fun loadActivities(grpId: String) {

        grpRef.child(grpId).child("activities").addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val activity = snapshot.getValue(GroupActivity::class.java)
                activity?.let {
                    val updatedActivities = _grpActivity.value.toMutableList()
                    if (updatedActivities.none { existingActivity ->
                            existingActivity.message.timestamp == it.message.timestamp
                        }) {
                        updatedActivities.add(it)
                        _grpActivity.value = updatedActivities
                    }
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val updatedActivity = snapshot.getValue(GroupActivity::class.java)
                updatedActivity?.let {
                    val activitiesList = _grpActivity.value.toMutableList()
                    val index = activitiesList.indexOfFirst { existingActivity ->
                        existingActivity.message.timestamp == it.message.timestamp
                    }
                    if (index >= 0) {
                        activitiesList[index] = it
                        _grpActivity.value = activitiesList
                    }
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val removedActivity = snapshot.getValue(GroupActivity::class.java)
                removedActivity?.let {
                    val activitiesList = _grpActivity.value.toMutableList()
                    activitiesList.removeIf { activity ->
                        activity.message.timestamp == removedActivity.message.timestamp
                    }
                    _grpActivity.value = activitiesList
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                Log.d("TAG", "onChildMoved: Activity moved")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("TAG", "Error loading activities: ${error.message}")
            }
        })
    }


    fun removeActivity(activity: GroupActivity, grpId: String) {
        grpRef.child(grpId).child("activities").child(activity.message.messageId).removeValue()

    }


    private fun generateUniqueId(): String {
        return UUID.randomUUID().toString().replace("-", "").take(20)
    }

    fun convertTimestampToDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun convertTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("hh:mm a  dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun convertTimestampToTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }


    // find group

    fun findGroup(search: String) {
        if (search.isEmpty()) {
            groupState = GroupState.Error("Query cannot be empty")
            return
        }

        groupState = GroupState.Loading
        viewModelScope.launch {
            findGrp(search) { result ->
                if (result != null) {
                    group = result
                    groupState = GroupState.Success(result)
                } else {
                    groupState = GroupState.Error("User not found")
                }
            }
        }
    }

    // find group result
    private fun findGrp(search: String, onResult: (Routes.GrpDetails?) -> Unit) {

        grpRef.child(search).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    grpRef.child(search).child("grpInfo").get().addOnSuccessListener {
                        if (it.exists()) {
                            val info = it.getValue(Routes.GrpDetails::class.java)
                            onResult(info)
                        } else {
                            groupState = GroupState.Error("group details not available")
                            onResult(null)
                        }
                    }

                } else {
                    groupState = GroupState.Error("user not found")
                    onResult(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                groupState = GroupState.Error(error.message)
                onResult(null)
            }
        })
    }


    fun emptyGroup(text: String = "") {
        group = null
        groupState = GroupState.Error(text)
    }

    // find user


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


    fun clearMessage() {
        _grpActivity.value = emptyList()
    }

    private fun find(search: String, onResult: (Routes.UserModel?) -> Unit) {

        userRef.orderByChild("email").equalTo(search)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (childSnapshot in snapshot.children) {
                            val usr = childSnapshot.getValue(Routes.UserModel::class.java)
                            onResult(usr)
                            return
                        }
                    } else {
                        userRef.orderByChild("username").equalTo(search)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {
                                        for (childSnapshot in snapshot.children) {
                                            val usr =
                                                childSnapshot.getValue(Routes.UserModel::class.java)
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

    fun clearGroups() {
        _groupsList.value = emptyList()
    }

    fun getParticipantsDetails(
        grpId: String,
        senderId: String,
        function: (UserJoinDetails?) -> Unit
    ) {

        grpRef.child(grpId).child("participants").child(senderId).addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val user = snapshot.getValue(UserJoinDetails::class.java)
                        user?.let(function)
                    } else {
                        function(null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    function(null)
                }

            }
        )
    }

    // notice section

    fun uploadEvent(
        type: EventCategory,
        title: String,
        message: String,
        visibility: Category,
        grpId: String,
        onComplete: (Boolean) -> Unit
    ) {
        val messageId = grpRef.child(grpId).push().key ?: ""
        val timestamp = System.currentTimeMillis()
        val eventData = EventData(
            type = type,
            messageId = messageId,
            timeStamp = timestamp,
            title = title,
            description = message,
            visibility = visibility
        )

        grpRef.child(grpId).child("events").child(messageId).setValue(eventData)
            .addOnSuccessListener {
                onComplete(true)
            }.addOnFailureListener {
                onComplete(false)
            }

    }


    // Change receiveEvent() to use continuous listener
    fun receiveEvent(grpId: String, type: EventCategory) {
        grpRef.child(grpId).child("events")
            .addValueEventListener(object :
                ValueEventListener { // Changed from addListenerForSingleValueEvent
                override fun onDataChange(snapshot: DataSnapshot) {
                    val eventList = mutableListOf<EventData>()
                    for (eventSnapshot in snapshot.children) {
                        val event = eventSnapshot.getValue(EventData::class.java)?.apply {
                            messageId = eventSnapshot.key ?: ""
                        }
                        event?.let { eventList.add(it) }
                    }
                    _eventList.value = eventList
                }

                override fun onCancelled(error: DatabaseError) {
                    _eventList.value = emptyList()
                }
            })
    }

    fun deleteEvent(grpId: String, messageId: String, callback: (Boolean) -> Unit) {
        if (messageId.isBlank()) {
            callback(false)
            return
        }

        grpRef.child(grpId).child("events").child(messageId)
            .removeValue()
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener { e ->
                callback(false)
            }
    }
}


data class EventData(
    val type: EventCategory = EventCategory.Event,
    val title: String = "",
    val description: String = "",
    var messageId: String = "",
    val visibility: Category = Category.General,
    val timeStamp: Long = 0L
)

data class GroupDetails(
    val grpId: String = "",
    val grpName: String = "",
    val photoUrl: String = "",
    var lastActivity: GroupActivity = GroupActivity()
)


enum class GroupActivityType {
    Image, Text
}

data class GroupActivity(
    val type: GroupActivityType = GroupActivityType.Text, val message: GroupMessage = GroupMessage()
)

data class RequestMap(
    val email: String = "",
    val uid: String = "",
    val username: String = "",
    val sentTime: Long = 0L,
)

data class UserJoinDetails(
    val email: String = "",
    val phone: String = "",
    val photoUrl: String = "",
    val uid: String = "",
    val username: String = "",
    val joinData: Long = 0L,
    var userType: Category = Category.General
)


enum class GroupStatus {
    Loading, Success, Failed
}