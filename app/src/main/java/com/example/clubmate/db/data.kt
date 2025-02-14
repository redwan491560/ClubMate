package com.example.clubmate.db

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
        val phone: String = "",
        val chatID: String = "",
        val publicKey: String = "",
        val photoUrl: String = "",
        val encryptedPrivateKey: String = ""
    )

    @Serializable
    data class UserDetails(
        val email: String = "",
        val uid: String = "",
        val username: String = "",
        val chatID: String = "",
        val phone: String = ""
    )

    @Serializable
    object Chat

    @Serializable
    object PrivateAuth


    @Serializable
    data class PrivateChat(
        val channelId: String,
        val uid: String,
        val password: String
    )

    @Serializable
    object CreateChannel


    // groups

    @Serializable
    data class GroupModel(
        val user: String = "", val grpId: String = ""
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
        val photoUrl: String = "",
        val grpName: String = "",
        val createdAt: Long = 0L,
        val createdBy: String = ""
    )


    @Serializable
    data class AddUserToGroup(
        val grpId: String
    )

    @Serializable
    data class Request(
        val uid: String,
        var grpId: String
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
    data class Timeline(
        val grpId: String,
        val uid: String
    )

    @Serializable
    data class Console(
        val grpId: String,
        val grpName: String,
        val image: String = "",
        val description: String = "",
        val uid: String
    )


    @Serializable
    data class ChangeRoles(
        val grpId: String, val uid: String
    )

    @Serializable
    data class Block(
        val uid: String, val grpId: String
    )

    @Serializable
    data class GroupUserDetails(
        val grpId: String, val grpName: String = "",
        val userId: String, val currentUserId: String
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


sealed class UserState {
    data object Loading : UserState()
    data class Success(val user: Routes.UserModel?) : UserState()
    data class Error(val msg: String) : UserState()
}

sealed class GroupState {
    data object Loading : GroupState()
    data class Success(val group: Routes.GrpDetails?) : GroupState()
    data class Error(val msg: String) : GroupState()
}


sealed class Status {
    data object Authenticated : Status()
    data object NotAuthenticated : Status()
    data object Loading : Status()
    data class Error(val message: String) : Status()
}


// not final
//data class UserModel(
//    val email: String = "",
//    val phone: String = "",
//    val uid: String = "",
//    val username: String = "",
//    val publicKey: String = "",
//    val encryptedPrivateKey: String = ""
//)
