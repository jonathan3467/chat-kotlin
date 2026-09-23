package Modelo

import kotlinx.serialization.Serializable

@Serializable
data class NewRoom(
    val type: String = "NEW_ROOM",
    val roomname: String
)

@Serializable
data class Invite(
    val type: String = "INVITE",
    val roomname: String,
    val usernames: List<String>
)

@Serializable
data class Invitation(
    val type: String = "INVITATION",
    val username: String,
    val roomname: String
)

@Serializable
data class JoinRoom(
    val type: String = "JOIN_ROOM",
    val roomname: String
)

@Serializable
data class JoinedRoom(
    val type: String = "JOINED_ROOM",
    val roomname: String,
    val username: String
)

@Serializable
data class RoomUsers(
    val type: String = "ROOM_USERS",
    val roomname: String
)

@Serializable
data class RoomUserList(
    val type: String = "ROOM_USER_LIST",
    val roomname: String,
    val users: Map<String, String>
)

@Serializable
data class RoomText(
    val type: String = "ROOM_TEXT",
    val roomname: String,
    val text: String
)

@Serializable
data class RoomTextFrom(
    val type: String = "ROOM_TEXT_FROM",
    val roomname: String,
    val username: String,
    val text: String
)

@Serializable
data class LeaveRoom(
    val type: String = "LEAVE_ROOM",
    val roomname: String,
)

@Serializable
data class LeftRoom(
    val type: String = "LEFT_ROOM",
    val roomname: String,
    val username: String
)
