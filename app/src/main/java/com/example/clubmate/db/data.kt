package com.example.clubmate.db

import com.example.clubmate.screens.MessageStatus
import kotlinx.serialization.Serializable

@Serializable
class Routes {


    @Serializable
    object Splash

    @Serializable
    object Login


    @Serializable
    object Register


    // screens
    @Serializable
    object Main


    @Serializable
    data class UserModel(
        val uid: String = "",
        val email: String = "",
        val username: String = "",
        val chatID: String = "",
        val publicKey: String = ""
    )

    @Serializable
    data class UserDetails(
        val email: String = "",
        val username: String = "",
        val chatID: String = "",
        val phone: String = ""
    )

    @Serializable
    object Chat


    // groups

    @Serializable
    data class GroupModel(
        val user: String = "",
        val grpId: String = ""
    )

    @Serializable
    data class CreateGroup(
        val uid: String = "",
        val email: String = "",
        val username: String = "",
    )


    @Serializable
    data class GrpDetails(
        val grpId: String = "",
        val description: String = "",
        val grpName: String = "",
        val createdAt: Long = 0L,
        val createdBy: String = ""
    )


    @Serializable
    data class AddUserToGroup(
        val grpId: String
    )


    @Serializable
    data class RemoveUserFromGroup(
        val grpId: String
    )

    @Serializable
    data class ViewAllUser(
        val grpId: String
    )


    @Serializable
    object ChangeRoles

    @Serializable
    data class GroupUserDetails(
        val grpId: String,
        val grpName: String = "",
        val userId: String,
        val currentUserId: String
    )


    // nav bars
    @Serializable
    object Personalize

    @Serializable
    object Accounts

    @Serializable
    object ReportBug

    @Serializable
    object Security

    @Serializable
    object Setting

    @Serializable
    object Developers

}


data class Message(
    val messageId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val messageText: String = "",
    val timestamp: Long = 0,
    val status: MessageStatus = MessageStatus.SENDING,
    val messageType: MessageType = MessageType.Text
)


enum class MessageType {
    Text, Audio, Video
}


sealed class UserState {
    data object Loading : UserState()
    data class Success(val user: UserModel?) : UserState()
    data class Error(val msg: String) : UserState()
}


sealed class Status {
    data object Authenticated : Status()
    data object NotAuthenticated : Status()
    data object Loading : Status()
    data class Error(val message: String) : Status()
}


// not final
data class UserModel(
    val email: String = "", val phone: String = "",
    val uid: String = "", val username: String = "",
    val publicKey: String = ""
)
