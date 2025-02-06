package com.example.clubmate.privateChannel

import android.annotation.SuppressLint
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.clubmate.R
import com.example.clubmate.db.Routes
import com.example.clubmate.screens.launchToast
import com.example.clubmate.ui.theme.Composables
import com.example.clubmate.ui.theme.roboto
import com.example.clubmate.util.chat.VanishingMessageDesign
import com.example.clubmate.viewmodel.PrivateChannelViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun PrivateChannel(
    channelId: String,
    uid: String,
    viewModel: PrivateChannelViewModel,
    navController: NavHostController
) {

    var text by remember { mutableStateOf("") }
    val messages = viewModel.privateMessageList.collectAsState()

    val context = LocalContext.current
    val listState = rememberLazyListState()
    LaunchedEffect(messages.value.size) {
        if (messages.value.isNotEmpty()) {
            listState.animateScrollToItem(messages.value.size - 1)
        }
    }
    DisposableEffect(Unit) {
        onDispose { viewModel.leaveChatroom(channelId) }
    }

    LaunchedEffect(Unit) {
        viewModel.listenForMessages(channelId)
    }

    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val openGalleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: android.net.Uri? ->
            uri?.let {
                selectedImageUri = it
            }
        }


    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2A3150))
            .systemBarsPadding()
            .windowInsetsPadding(WindowInsets.ime),
        topBar = {
            Row {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Messages will be deleted upon seen by reciever",
                        color = Color.White,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Text(text = channelId, color = Color.White, fontSize = 18.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Composables.TextDesignClickable(text = "delete", color = Color.White) {
                            viewModel.deleteChannel(channelId) {
                                navController.navigate(Routes.PrivateAuth)
                            }
                        }
                    }
                }
                Row {
                    Image(
                        painter = painterResource(id = R.drawable.baseline_content_copy_24),
                        contentDescription = null, modifier = Modifier.size(25.dp)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.delete_msg),
                        contentDescription = null, modifier = Modifier.size(25.dp)
                    )
                }
            }

        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    shape = RoundedCornerShape(10.dp),
                    maxLines = 3,
                    leadingIcon = {
                        Image(colorFilter = ColorFilter.tint(Color.White),
                            painter = painterResource(id = R.drawable.attachment_24px),
                            contentDescription = null,
                            modifier = Modifier
                                .size(30.dp)
                                .clickable {
                                    openGalleryLauncher.launch("image/*")
                                }
                        )
                    },
                    trailingIcon = {
                        Image(colorFilter = ColorFilter.tint(Color.White),
                            painter = painterResource(id = R.drawable.send_24px),
                            contentDescription = null,
                            modifier = Modifier
                                .size(30.dp)
                                .clickable {
                                    if (text.isEmpty()) {
                                        launchToast(context = context, "text cannot be empty")
                                    } else {
                                        if (uid.isNotEmpty()) {
                                            viewModel.sendVanishingMessage(
                                                chatId = channelId,
                                                uid = uid,
                                                messageText = text.trim(),
                                                imageUrl = ""
                                            )
                                            text = ""
                                        } else {
                                            launchToast(context = context, "reenter the channel")
                                        }
                                    }
                                }
                        )
                    },
                    textStyle = TextStyle(
                        fontFamily = roboto,
                        color = Color.White,
                        fontSize = 18.sp
                    ),
                    modifier = Modifier
                        .fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "Enter message",
                            fontFamily = roboto,
                            fontSize = 18.sp,
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedPlaceholderColor = Color.Transparent,
                        unfocusedPlaceholderColor = Color.Transparent,
                        focusedIndicatorColor = Color.White,
                        unfocusedIndicatorColor = Color.White,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = "end to end encrypted secured channel",
                    color = Color.White,
                    fontSize = 11.sp
                )
            }
        }
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF2A3150))
                .padding(bottom = 110.dp)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, top = 90.dp, end = 8.dp, bottom = 0.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                items(messages.value.sortedBy { it.timestampSent }) {
                    VanishingMessageDesign(
                        message = it,
                        isSent = (it.senderId == uid),
                        time = viewModel.convertTimestamp(it.timestampSent)
                    ) {
                        // delete messages
                    }

                }
            }

        }

    }
}




