package com.example.clubmate.screens

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.clubmate.R
import com.example.clubmate.db.Routes
import com.example.clubmate.ui.theme.Composables.Companion.TextDesignClickable
import com.example.clubmate.ui.theme.roboto
import com.example.clubmate.util.chat.MessageItem
import com.example.clubmate.viewmodel.ChatViewModel
import com.example.clubmate.viewmodel.MainViewmodel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "StateFlowValueCalledInComposition")
@Composable
fun ChatScreen(
    userModel: Routes.UserModel,
    chatViewmodel: ChatViewModel,
    navController: NavController,
    mainViewmodel: MainViewmodel
) {

    val messages = chatViewmodel.messages.collectAsState()

    val listState = rememberLazyListState()
    var text by remember { mutableStateOf("") }
    val context = LocalContext.current


    val receiverId = userModel.uid
    val currentUser by chatViewmodel.currentUser.collectAsState()


    LaunchedEffect(messages.value) {
        if (messages.value.isNotEmpty()) {
            listState.animateScrollToItem(messages.value.size - 1)
        }
    }


    // notification part
    LaunchedEffect(Unit) {
        listState.interactionSource
    }

    LaunchedEffect(userModel.uid, userModel.chatID) {
        currentUser?.uid?.let {
            chatViewmodel.receiveMessage(
                chatId = userModel.chatID
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            chatViewmodel.clearMessage()
        }
    }


    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val openGalleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: android.net.Uri? ->
            uri?.let {
                selectedImageUri = it
            }
        }


    BackHandler {
        chatViewmodel.emptyUser()
        navController.navigateUp()
    }

    Scaffold(modifier = Modifier
        .systemBarsPadding()
        .windowInsetsPadding(WindowInsets.ime)
        .padding(horizontal = 7.dp, vertical = 5.dp),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp, 10.dp, 0.dp, 0.dp))
                    .background(Color(0xFFF3E5E5))
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .weight(8f)
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        userModel.run {
                            Text(
                                text = username,
                                fontSize = 18.sp,
                                fontFamily = roboto,
                            )

                            TextDesignClickable(text = "View Profile", size = 14) {
                                chatViewmodel.fetchUserByUid(receiverId) { user ->
                                    user?.let {
                                        navController.navigate(
                                            Routes.UserDetails(
                                                username = username,
                                                email = email,
                                                chatID = chatID,
                                                phone = it.phone
                                            )
                                        )
                                    }
                                }
                            }

                        }
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(painter = painterResource(id = R.drawable.call_icon),
                        contentDescription = null,
                        modifier = Modifier
                            .size(25.dp)
                            .clickable {

                            }
                    )
                    Image(painter = painterResource(id = R.drawable.video_call),
                        contentDescription = null,
                        modifier = Modifier
                            .size(35.dp)
                            .clickable {

                            }
                    )
                    Image(painter = painterResource(id = R.drawable.incognito),
                        contentDescription = null,
                        modifier = Modifier
                            .size(30.dp)
                            .clickable {

                            }
                    )

                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start
            ) {

                selectedImageUri?.let { uri ->

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 0.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(8.dp, 8.dp, 0.dp, 0.dp))
                            .background(Color(0xFF3A4233)),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Row(
                            modifier = Modifier.weight(8f),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(uri),
                                contentDescription = "Selected Image",
                                modifier = Modifier
                                    .padding(8.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .width(250.dp)
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(top = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.edit_button),
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(top = 5.dp, end = 5.dp, bottom = 5.dp)
                                    .size(23.dp),
                                colorFilter = ColorFilter.tint(Color(0xFFF0F0D3))
                            )
                            Image(
                                painter = painterResource(id = R.drawable.delete_msg),
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(top = 5.dp, end = 5.dp)
                                    .size(40.dp)
                                    .clickable {
                                        selectedImageUri = null
                                    },
                                colorFilter = ColorFilter.tint(
                                    Color(0xFFF0F0D3)
                                )
                            )
                        }
                    }

                }

                OutlinedTextField(value = text,
                    onValueChange = { text = it },
                    shape = RoundedCornerShape(0.dp, 0.dp, 6.dp, 6.dp),
                    maxLines = 3,
                    leadingIcon = {
                        Image(painter = painterResource(id = R.drawable.attachment_24px),
                            contentDescription = null,
                            modifier = Modifier
                                .size(30.dp)
                                .clickable {
                                    openGalleryLauncher.launch("image/*")
                                }
                        )
                    },
                    trailingIcon = {
                        Image(painter = painterResource(id = R.drawable.send_24px),
                            contentDescription = null,
                            modifier = Modifier
                                .size(30.dp)
                                .clickable {
                                    val uid = currentUser?.uid
                                    if (uid != null) {
                                        val sentMessage = text.trim()
                                        selectedImageUri?.let { imageUri ->
                                            chatViewmodel.sendMessage(
                                                chatId = userModel.chatID,
                                                messageText = "",
                                                imageUri = imageUri,
                                                senderId = uid,
                                                receiverId = receiverId
                                            )
                                            selectedImageUri = null
                                        } ?: run {
                                            if (sentMessage.isNotEmpty()) {
                                                chatViewmodel.sendMessage(
                                                    chatId = userModel.chatID,
                                                    messageText = sentMessage,
                                                    senderId = uid,
                                                    receiverId = receiverId
                                                )
                                                text = ""
                                            } else {
                                                launchToast(context, "Message cannot be empty")
                                            }
                                        }

                                    }
                                })
                    },
                    textStyle = TextStyle(fontFamily = roboto, fontSize = 18.sp),
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "Enter message",
                            fontFamily = roboto,
                            fontSize = 18.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    })
            }
        }) {


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 60.dp, top = 70.dp)
        ) {

            currentUser?.let { user ->
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 40.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(messages.value.size) { ind ->
                        Spacer(modifier = Modifier.height(5.dp))
                        val message = messages.value[ind]

                        if (message.messageText.isNotEmpty() || message.imageRef.isNotEmpty()) {
                            MessageItem(
                                message = message,
                                isSent = messages.value[ind].senderId == user.uid,
                                time = chatViewmodel.convertTimestampToDate(messages.value[ind].timestamp),
                                onDeleteMessage = { msg ->
                                    chatViewmodel.deleteIndividualMessage(
                                        chatId = userModel.chatID, messageId = msg.messageId
                                    )
                                })
                        }
                    }
                }
            }

        }
    }
}


fun launchToast(context: Context, text: String) {
    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
}


enum class MessageStatus {
    SEEN, DELIVERED, FAILED, SENDING
}