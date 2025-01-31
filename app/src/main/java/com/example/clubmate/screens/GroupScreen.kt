package com.example.clubmate.screens

import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import coil3.compose.AsyncImage
import com.example.clubmate.R
import com.example.clubmate.db.Routes
import com.example.clubmate.ui.theme.Composables.Companion.TextDesignClickable
import com.example.clubmate.ui.theme.roboto
import com.example.clubmate.viewmodel.GroupActivity
import com.example.clubmate.viewmodel.GroupViewmodel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "MutableCollectionMutableState")
@Composable
fun GroupScreen(
    userId: String, grpId: String, navController: NavHostController, grpViewmodel: GroupViewmodel
) {

    var grpDetails by remember { mutableStateOf(Routes.GrpDetails()) }

    var text by remember { mutableStateOf("") }

    var pendingImages by remember { mutableStateOf(mutableListOf<Pair<String, android.net.Uri>>()) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        grpViewmodel.loadGroupInfo(grpId) { details ->
            details?.let { grpDetails = details }
        }
    }

    val grpActivity = grpViewmodel.grpActivity.collectAsState()
    LaunchedEffect(grpActivity.value) {
        grpViewmodel.loadActivities(grpId)
    }

    val listState = rememberLazyListState()
    LaunchedEffect(Unit) { listState.interactionSource }

    LaunchedEffect(grpActivity.value.size) {
        if (grpActivity.value.isNotEmpty()) {
            listState.animateScrollToItem(grpActivity.value.size - 1)
        }
    }

    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val openGalleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: android.net.Uri? ->
            uri?.let {
                selectedImageUri = it
            }
        }

    Scaffold(modifier = Modifier
        .systemBarsPadding()
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
                        Text(
                            text = grpDetails.grpName,
                            fontSize = 18.sp,
                            fontFamily = roboto,
                        )

                        TextDesignClickable(text = "View Profile", size = 14) {
                            // view group details
                            navController.navigate(
                                Routes.GrpDetails(
                                    grpId = grpDetails.grpId,
                                    grpName = grpDetails.grpName,
                                    description = grpDetails.description,
                                    createdBy = grpDetails.createdBy,
                                    createdAt = grpDetails.createdAt
                                )
                            )
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

                            })
                    Image(painter = painterResource(id = R.drawable.video_call),
                        contentDescription = null,
                        modifier = Modifier
                            .size(35.dp)
                            .clickable {

                            })
                    Image(painter = painterResource(id = R.drawable.incognito),
                        contentDescription = null,
                        modifier = Modifier
                            .size(30.dp)
                            .clickable {

                            })
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
                                    val sentMessage = text.trim()
                                    selectedImageUri?.let { imageUri ->
                                        // If there's an image URL, send the image URI along with an empty messageText
                                        grpViewmodel.addActivity(
                                            grpId = grpId,
                                            senderId = userId,
                                            messageText = "", // Empty message text since we have an image
                                            imageUri = imageUri // Pass the image URI as a string
                                        )
                                    } ?: run {
                                        // If no image, check if there's a text message
                                        if (sentMessage.isNotEmpty()) {
                                            grpViewmodel.addActivity(
                                                grpId = grpId,
                                                senderId = userId,
                                                messageText = sentMessage,
                                                imageUri = null // No image
                                            )
                                            text = "" // Clear the message input
                                        } else {
                                            launchToast(context, "Message cannot be empty")
                                        }
                                    }
                                    selectedImageUri = null
                                }

                        )
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

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {

                items(grpActivity.value.size) { ind ->
                    Spacer(modifier = Modifier.height(5.dp))
                    val activity = grpActivity.value[ind]
                    var sender by remember { mutableStateOf("") }
                    grpViewmodel.fetchUserDetailsByUid(activity.message.senderId) {
                        sender = it?.username.toString()
                    }

                    if (activity.message.messageText.isNotEmpty() || activity.message.imageRef.isNotEmpty()) {
                        GroupActivityDesign(
                            activity = activity,
                            isSent = activity.message.senderId == userId,
                            sender = sender,
                            sentTime = grpViewmodel.convertTimestampToTime(activity.message.timestamp),
                            sentDate = grpViewmodel.convertTimestampToDate(activity.message.timestamp),
                        ) { act ->
                            if (activity.message.senderId == userId) grpViewmodel.removeActivity(
                                activity = act
                            )
                            else launchToast(context, "only admin and sender can delete message")
                        }

                    }
                }
            }

        }
    }
}


@Composable
fun GroupActivityDesign(
    activity: GroupActivity,
    sender: String,
    isSent: Boolean,
    sentTime: String,
    sentDate: String,
    onDeleteMessage: (GroupActivity) -> Unit
) {

    var isSelected by remember { mutableStateOf(false) }

    val bgColor by remember {
        mutableStateOf(
            if (isSent) Color(0xFFD5F1C4)
            else Color(0xBFF1D4D4)
        )
    }

    Box(
        modifier = Modifier.fillMaxWidth(), contentAlignment = if (isSent) Alignment.CenterEnd
        else Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isSent) Arrangement.End else Arrangement.Start
        ) {
            Column(
                horizontalAlignment = if (isSent) Alignment.End else Alignment.Start
            ) {
                Text(
                    text = sender,
                    fontSize = 12.sp,
                    fontFamily = roboto,
                    modifier = Modifier
                        .widthIn(max = 280.dp)
                        .padding(end = if (isSent) 5.dp else 0.dp),
                    color = Color.Black
                )
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(bgColor)
                        .padding(4.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = { if (isSent) isSelected = true },
                                onTap = { if (isSelected) isSelected = false }
                            )
                        },
                ) {
                    if (activity.message.imageRef.isNotEmpty()) {
                        // Display Image with preloading and caching
                        Box(modifier = Modifier.size(300.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(40.dp)
                                    .align(Alignment.Center),
                                color = Color.Gray,
                                strokeWidth = 4.dp
                            )
                            AsyncImage(
                                model = activity.message.imageRef,
                                contentDescription = "Sent Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .size(300.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                error = painterResource(id = R.drawable.add_24px) // Error Image
                            )
                        }
                    } else {
                        // Display Text
                        Text(
                            text = activity.message.messageText,
                            fontSize = 16.sp,
                            fontFamily = roboto,
                            color = Color.Black,
                            modifier = Modifier
                                .padding(horizontal = 15.dp)
                                .widthIn(max = 280.dp)
                        )
                    }
                }
                Row(
                    horizontalArrangement = if (isSent) Arrangement.End else Arrangement.Start,
                    modifier = Modifier.padding(end = if (isSent) 5.dp else 0.dp)
                ) {
                    Text(
                        text = sentTime, fontFamily = roboto, fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = sentDate, fontFamily = roboto, fontSize = 10.sp
                    )
                }
            }
            AnimatedVisibility(isSelected) {
                Icon(painter = painterResource(id = R.drawable.delete_msg),
                    contentDescription = "Delete Item",
                    modifier = Modifier
                        .padding(bottom = 5.dp, start = 5.dp)
                        .size(30.dp)
                        .clickable {
                            onDeleteMessage(activity)
                        }
                )
            }
        }
    }
}
