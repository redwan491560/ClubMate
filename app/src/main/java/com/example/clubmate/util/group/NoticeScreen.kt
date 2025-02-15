package com.example.clubmate.util.group

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.example.clubmate.R
import com.example.clubmate.db.Routes
import com.example.clubmate.screens.launchToast
import com.example.clubmate.ui.theme.Composables.Companion.TextDesign
import com.example.clubmate.ui.theme.Composables.Companion.TextDesignClickable
import com.example.clubmate.ui.theme.roboto
import com.example.clubmate.util.Category
import com.example.clubmate.viewmodel.GroupViewmodel


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun TimelineScreen(
    args: Routes.Timeline,
    grpViewmodel: GroupViewmodel,
    navHostController: NavHostController
) {

    val context = LocalContext.current  // Get the current context for launching the browser

    val eventsList = grpViewmodel.eventList.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    val event = eventsList.value.filter { it.type == EventCategory.Event }
    val meeting = eventsList.value.filter { it.type == EventCategory.Meeting }
    val notice = eventsList.value.filter { it.type == EventCategory.Notice }

    val chips = listOf("Event", "Meeting", "Notice")
    var chipsState by rememberSaveable { mutableIntStateOf(0) }
    var selectedEventType by remember { mutableStateOf(EventCategory.Event) }

    LaunchedEffect(eventsList.value, selectedEventType) {
        grpViewmodel.receiveEvent(args.grpId, selectedEventType)
    }

    var currentUserIsAdmin by remember { mutableStateOf(false) }
    var grpDetails by remember { mutableStateOf(Routes.GrpDetails()) }
    LaunchedEffect(Unit) {
        grpViewmodel.receiveEvent(args.grpId, selectedEventType)
        grpViewmodel.checkAdmin(grpId = args.grpId, userId = args.uid) {
            currentUserIsAdmin = it
        }
        grpViewmodel.loadGroupInfo(args.grpId) { details ->
            details?.let {
                grpDetails = details
            }
        }
    }


    Scaffold(
        modifier = Modifier
            .fillMaxSize()
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Column(
                modifier = Modifier
                    .background(Color(0xFFD6F1C2))
                    .systemBarsPadding()
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally

            ) {

                AsyncImage(
                    model = grpDetails.photoUrl,
                    contentDescription = null, contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(50.dp)),
                    error = painterResource(id = R.drawable.logo_primary)
                )
                TextDesign(text = grpDetails.grpName, size = 20)
                TextDesign(text = grpDetails.description, size = 14)
            }

            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp),
                horizontalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                chips.forEachIndexed { index, s ->
                    Text(text = s,
                        fontSize = 20.sp,
                        color = if (chipsState == index) Color.Blue else Color.Black,
                        textDecoration = if (chipsState == index) TextDecoration.Underline
                        else TextDecoration.None,
                        fontFamily = roboto,
                        modifier = Modifier.clickable {
                            chipsState = index
                            selectedEventType = when (index) {
                                0 -> EventCategory.Event
                                1 -> EventCategory.Meeting
                                else -> EventCategory.Notice
                            }
                        })
                }
            }

            if (eventsList.value.isEmpty()) {
                Spacer(modifier = Modifier.height(120.dp))
                TextDesign(text = "No event published yet")
            }
            Spacer(modifier = Modifier.height(20.dp))

            when (chipsState) {
                0 -> {
                    LazyColumn(
                        state = listState
                    ) {
                        items(event.size) { ind ->
                            val item = event[ind]
                            EventComponent(
                                isAdmin = currentUserIsAdmin,
                                onDelete = {
                                    grpViewmodel.deleteEvent(args.grpId, item.messageId) {
                                        if (it) launchToast(context, "deletion successful")
                                        else launchToast(context, "try again to delete")
                                    }
                                },
                                visibility = item.visibility,
                                imageRef = "",
                                title = item.title,
                                message = item.description,
                                type = item.type.name,
                                date = grpViewmodel.convertTimestamp(item.timeStamp)
                            )
                            Spacer(modifier = Modifier.height(5.dp))

                        }
                    }
                }

                1 -> {
                    LazyColumn(
                        state = listState
                    ) {
                        items(meeting.size) { ind ->
                            val item = meeting[ind]

                            MeetingComponent(
                                isAdmin = currentUserIsAdmin,
                                onDelete = {
                                    grpViewmodel.deleteEvent(args.grpId, item.messageId) {
                                        if (it) launchToast(context, "deletion successful")
                                        else launchToast(context, "try again to delete")
                                    }
                                },
                                visibility = item.visibility,
                                date = grpViewmodel.convertTimestamp(item.timeStamp),
                                type = item.type.name,
                                link = item.description,
                                title = item.title
                            ) { isValid ->
                                if (isValid) {
                                    val intent =
                                        Intent(Intent.ACTION_VIEW, Uri.parse(item.description))
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: ActivityNotFoundException) {
                                        launchToast(context, "No app to handle this link")
                                    }
                                } else {
                                    launchToast(context, "Invalid link format")
                                }
                            }
                            Spacer(modifier = Modifier.height(5.dp))
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        state = listState
                    ) {
                        items(notice.size) { ind ->
                            val item = notice[ind]
                            NoticeComponent(
                                isAdmin = currentUserIsAdmin,
                                onDelete = {
                                    grpViewmodel.deleteEvent(args.grpId, item.messageId) {
                                        if (it) launchToast(context, "deletion successful")
                                        else launchToast(context, "try again to delete")
                                    }
                                },
                                visibility = item.visibility,
                                notice = item.description,
                                date = grpViewmodel.convertTimestamp(item.timeStamp),
                                type = item.type.name, title = item.title
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                        }
                    }

                }
            }
        }
    }
}

fun isValidUrl(url: String): Boolean {
    return try {
        Uri.parse(url).run {
            scheme.equals("http", true) || scheme.equals("https", true)
        }
    } catch (e: Exception) {
        false
    }
}


@Composable
fun MeetingComponent(
    isAdmin: Boolean = false,
    onDelete: () -> Unit,
    date: String,
    type: String,
    link: String,
    title: String,
    visibility: Category = Category.General,
    onClick: (Boolean) -> Unit
) {
    var showDelete by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xCCC1D8BD)),
        horizontalAlignment = Alignment.End
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp, horizontal = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 5.dp)
                    .pointerInput(Unit) {
                        if (isAdmin) {
                            detectTapGestures(
                                onLongPress = { showDelete = true },
                                onTap = { showDelete = false }
                            )
                        }
                    }, horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextDesign(text = date, size = 12)
                    TextDesignClickable(text = visibility.name, size = 16) {}
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Column {
                        TextDesign(
                            text = title,
                            size = 18,
                            modifier = Modifier
                                .padding(top = 5.dp)
                        )
                        Text(
                            text = link,
                            fontFamily = roboto,
                            color = Color.Blue,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier
                                .padding(top = 5.dp)
                                .clickable {
                                    val processedUrl =
                                        if (!link.startsWith("http")) "http://$link" else link
                                    if (isValidUrl(processedUrl)) {
                                        onClick(true)
                                    } else {
                                        onClick(false)
                                    }
                                }
                        )
                    }
                }
                if (showDelete && isAdmin) {
                    DeleteButton(onDelete = {
                        onDelete()
                        showDelete = false
                    })
                }
            }
        }
    }
}

@Composable
fun EventComponent(
    imageRef: String,
    title: String,
    isAdmin: Boolean = false,
    onDelete: () -> Unit,
    message: String,
    type: String,
    visibility: Category = Category.General,
    date: String
) {
    var showDelete by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xCCB4D1F0)), horizontalAlignment = Alignment.End

    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp, horizontal = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 5.dp)
                    .pointerInput(Unit) {
                        if (isAdmin) {
                            detectTapGestures(
                                onLongPress = { showDelete = true },
                                onTap = { showDelete = false }
                            )
                        }
                    }, horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextDesign(text = date, size = 12)
                    TextDesignClickable(text = visibility.name, size = 16) {}
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        TextDesign(
                            text = title,
                            size = 18,
                            color = Color.Gray,
                            modifier = Modifier
                                .padding(top = 5.dp)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        TextDesign(
                            size = 14,
                            text = message,
                            color = Color.Black
                        )
                    }

                }

                if (showDelete && isAdmin) {
                    DeleteButton(onDelete = {
                        onDelete()
                        showDelete = false
                    })
                }
            }
        }

    }
}


@Composable
fun NoticeComponent(
    isAdmin: Boolean = false,
    onDelete: () -> Unit,
    notice: String,
    title: String,
    visibility: Category = Category.General,
    date: String,
    type: String
) {
    var showDelete by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xCCEE1930)),
        horizontalAlignment = Alignment.End
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp, horizontal = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 5.dp)
                    .pointerInput(Unit) {
                        if (isAdmin) {
                            detectTapGestures(
                                onLongPress = { showDelete = true },
                                onTap = { showDelete = false }
                            )
                        }
                    }, horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextDesign(text = date, size = 12)
                    TextDesignClickable(text = visibility.name, size = 16) {}

                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        TextDesign(
                            text = title,
                            size = 18,
                            modifier = Modifier
                                .padding(top = 5.dp)
                        )
                        TextDesign(
                            size = 14,
                            text = notice,
                            color = Color.Gray
                        )
                    }
                }
                if (showDelete && isAdmin) {
                    DeleteButton(onDelete = {
                        onDelete()
                        showDelete = false
                    })
                }
            }
        }

    }
}

@Composable
private fun DeleteButton(onDelete: () -> Unit) {
    Box(
        modifier = Modifier
            .clickable { onDelete() },
        contentAlignment = Alignment.CenterEnd
    ) {
        Image(

            painter = painterResource(id = R.drawable.delete_msg),
            contentDescription = null,
            Modifier
                .padding(end = 5.dp, bottom = 5.dp)
                .size(30.dp)
        )
    }
}