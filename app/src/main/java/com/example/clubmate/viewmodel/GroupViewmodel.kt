package com.example.clubmate.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.clubmate.db.Routes
import com.example.clubmate.db.UserModel
import com.example.clubmate.db.UserState
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class GroupViewmodel : AuthViewModel() {

    private val _db = FirebaseDatabase.getInstance()
    private val grpRef = _db.getReference("groups")
    private val userRef = _db.getReference("user")


    // searched user
    var userState by mutableStateOf<UserState>(UserState.Success(null))
        private set

    // fetches he searched result
    var user by mutableStateOf<UserModel?>(UserModel())


    private val _groupsList = MutableStateFlow<List<GroupDetails>>(emptyList())
    val groupsList = _groupsList

    private val _grpStatus = MutableLiveData<GroupStatus>()
    val grpStatus = _grpStatus


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


    private fun fetchUserDetailsByEmail(email: String, onResult: (UserModel?) -> Unit) {

        userRef.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val user = snapshot.getValue(UserModel::class.java)
                        onResult(user)
                    } else {
                        Log.d("user not found", "onCancelled: ")
                        onResult(null)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("user not found", "onCancelled: ")
                }
            })
    }


    fun fetchUserDetailsByUid(uid: String, onResult: (UserModel?) -> Unit) {

        userRef.child(uid).addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val user = snapshot.getValue(UserModel::class.java)
                        user?.let {
                            onResult(user)
                        }
                    } else onResult(null)

                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("request canceled", "onCancelled: ")
                }
            }
        )
    }


    fun createGroup(
        grpId: String,
        createdBy: String,
        grpName: String,
        description: String,
        onResult: (Routes.GrpDetails?) -> Unit
    ) {

        val createdAt = System.currentTimeMillis()
        // Group does not exist, create a new one
        val grpDetails = Routes.GrpDetails(
            createdAt = createdAt,
            createdBy = createdBy,
            description = description,
            grpId = grpId,
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
                                    .setValue(groupList)
                                    .addOnSuccessListener {
                                        Log.d("Firebase", "List updated successfully!")
                                    }
                                    .addOnFailureListener { exception ->
                                        Log.e(
                                            "Firebase",
                                            "Error updating list: ${exception.message}"
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


    fun loadGroupInfo(grpId: String, onResult: (Routes.GrpDetails?) -> Unit) {

        grpRef.child(grpId).child("grpInfo").get()
            .addOnSuccessListener {
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


    // participants

    fun addParticipants(
        email: String,
        category: Category = Category.General,
        grpId: String
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
                                    userGroupsRef.setValue(currentGroups)
                                        .addOnSuccessListener {
                                            Log.d(
                                                "User joined Success",
                                                "addParticipants: User added to group successfully"
                                            )
                                        }
                                        .addOnFailureListener { error ->
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
                    }
                    .addOnFailureListener { error ->
                        Log.d("User joined failed", "addParticipants: ${error.message}")
                    }
            }
        }
    }


    fun removeParticipants(email: String, grpId: String) {

        grpRef.child(grpId).child("participants")
            .orderByChild("email")
            .equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (child in snapshot.children) {
                            child.ref.removeValue()
                                .addOnSuccessListener {
                                    Log.d(
                                        "removeParticipants",
                                        "User removed from group participants successfully"
                                    )

                                    // Now, also remove the user from the user's `groups_connected` list
                                    removeUserFromGroupsConnected(child.key, grpId)
                                }
                                .addOnFailureListener { error ->
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
                                }
                                .addOnFailureListener { error ->
                                    Log.e(
                                        "removeParticipants",
                                        "Failed to remove group from user's groups_connected: ${error.message}"
                                    )
                                }
                        } else {
                            Log.d(
                                "removeParticipants",
                                "Group not found in user's groups_connected"
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


    // others


    fun listenForGroups(uid: String) {
        userRef.child(uid).child("groups_connected")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val groupIds =
                            snapshot.getValue(object : GenericTypeIndicator<List<String>>() {})
                                ?: emptyList()

                        val fetchedGroups = mutableListOf<GroupDetails>()

                        groupIds.forEach { grpId ->
                            grpRef.child(grpId).child("grpInfo")
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(grpSnapshot: DataSnapshot) {
                                        if (grpSnapshot.exists()) {
                                            val groupDetails =
                                                grpSnapshot.getValue(GroupDetails::class.java)
                                            groupDetails?.let {
                                                fetchedGroups.add(it)

                                                // Update the groups list when all groups are fetched
                                                if (fetchedGroups.size == groupIds.size) {
                                                    _groupsList.value = fetchedGroups
                                                }
                                            }
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Log.e(
                                            "listenForGroups",
                                            "Error fetching group details: ${error.message}"
                                        )
                                    }
                                })
                        }

                        // Handle case where no group IDs exist
                        if (groupIds.isEmpty()) {
                            _groupsList.value = emptyList()
                        }
                    } else {
                        // If groups_connected is empty or doesn't exist
                        _groupsList.value = emptyList()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("listenForGroups", "Error fetching user groups: ${error.message}")
                }
            })
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
                            userRef.child(uid).child("groups_connected")
                                .setValue(groupIds)
                                .addOnSuccessListener {
                                    Log.d("leaveGroup", "Successfully removed group: $grpId")
                                    onResult(true)
                                }
                                .addOnFailureListener { error ->
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


    fun updateUserRole(email: String, category: Category, grpId: String) {

        val participantsRef = grpRef.child(grpId).child("participants")

        participantsRef.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (userSnapshot in snapshot.children) {
                            userSnapshot.ref.child("userType").setValue(category)
                                .addOnSuccessListener {
                                    Log.d("Update Success", "User role updated to $category")
                                }
                                .addOnFailureListener { exception ->
                                    Log.e(
                                        "Update Failed",
                                        "Error updating role: ${exception.message}"
                                    )
                                }
                        }
                    } else {
                        Log.e("Update Failed", "No user found with email: $email")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Database Error", "Error querying database: ${error.message}")
                }
            })
    }

    // message handling functions

    fun addActivity(grpId: String, senderId: String, activity: GroupActivity) {

        viewModelScope.launch {
            val messageId = grpRef.child(grpId).push().key ?: return@launch

            val timestamp = System.currentTimeMillis()
            val activityData = GroupActivity(
                senderId = senderId, message = GroupMessage(
                    timestamp = timestamp,
                    messageId = messageId,
                    senderId = activity.senderId,
                    messageText = activity.message.messageText
                )
            )
            grpRef.child(grpId).child("activities").child(activity.message.messageId)
                .setValue(activityData).addOnCompleteListener {
                    Log.d("add succ", "addActivity: success")
                }.addOnFailureListener {
                    Log.d("add failed", "addActivity: failed")
                }
        }
    }

    fun loadAllActivity(grpId: String) {

        viewModelScope.launch {
            grpRef.child(grpId).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("TAG", "onCancelled: ")

                }

            })
        }

    }

    fun removeActivity(activity: GroupActivity) {

    }

    private fun generateUniqueId(): String {
        return UUID.randomUUID().toString().replace("-", "").take(20)
    }

    fun convertTimestampToDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("hh:mm a  dd/MM/yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    //
    fun leaveGroup(uid: String, grpId: String) {

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

    fun setUserEmpty(txt: String) {
        user = null
        userState = UserState.Error(txt)
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


data class GroupDetails(
    val grpId: String = "",
    val grpName: String = "",
    val lastActivity: GroupActivity = GroupActivity()
)

enum class Category {
    Admin, General, President, VicePresident, Treasurer,
}


enum class GroupActivityType {
    Post, Poll, Audio, Video, Notice,
}

data class GroupActivity(
    val type: GroupActivityType = GroupActivityType.Post,
    val message: GroupMessage = GroupMessage(),
    val senderId: String = ""
)

data class GroupMessage(
    val messageId: String = "",
    val senderId: String = "",
    val messageText: String = "",
    val timestamp: Long = 0
)

data class UserJoinDetails(
    val email: String = "",
    val phone: String = "",
    val uid: String = "",
    val username: String = "",
    val joinData: Long = 0L,
    var userType: Category = Category.General
)


enum class GroupStatus {
    Loading, Success, Failed
}