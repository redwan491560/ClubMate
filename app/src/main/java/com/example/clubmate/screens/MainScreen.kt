package com.example.clubmate.screens


import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.clubmate.R
import com.example.clubmate.db.Routes
import com.example.clubmate.ui.theme.Composables.Companion.NavigationBarIcon
import com.example.clubmate.ui.theme.Composables.Companion.TextDesign
import com.example.clubmate.ui.theme.ItemDesignAlert
import com.example.clubmate.ui.theme.roboto
import com.example.clubmate.util.chat.ChatsDesign
import com.example.clubmate.util.group.GroupDesign
import com.example.clubmate.util.getInternetConnectionStatus
import com.example.clubmate.viewmodel.AuthViewModel
import com.example.clubmate.viewmodel.ChatViewModel
import com.example.clubmate.viewmodel.GroupViewmodel
import com.example.clubmate.viewmodel.MainViewmodel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "StateFlowValueCalledInComposition")
@Composable
fun MainScreen(
    chatViewmodel: ChatViewModel,
    navController: NavController,
    authViewModel: AuthViewModel,
    mainViewModel: MainViewmodel,
    grpViewmodel: GroupViewmodel
) {


    var alertState by remember { mutableStateOf(false) }
    var grpState by remember { mutableStateOf(false) }


    val currentUser = authViewModel.currentUser
    val currentUserId = currentUser.value?.uid


    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var query by remember { mutableStateOf("") }

    // returns the searched user
    val user = chatViewmodel.user

    val chips = listOf("contacts", "groups")
    var chipsState by remember { mutableIntStateOf(0) }


    var expanded by remember { mutableStateOf(false) }
    val animationDuration = 300


    val chatsList = chatViewmodel.chats.collectAsState()
    val groupsList = grpViewmodel.groupsList.collectAsState()

    LaunchedEffect(groupsList) {
        currentUser.value?.let {
            grpViewmodel.listenForGroups(it.uid)
        }
    }

    LaunchedEffect(chatsList) {
        currentUser.value?.let {
            chatViewmodel.listenForChats(currentUser.value!!.uid)
        }
    }


    var isOnline by remember {
        mutableStateOf(true)
    }

    LaunchedEffect(context) {
        getInternetConnectionStatus(context).collect { isConnected ->
            isOnline = isConnected
        }
    }

    ModalNavigationDrawer(drawerState = drawerState, gesturesEnabled = true, drawerContent = {
        ModalDrawerSheet(
            drawerShape = RoundedCornerShape(6.dp), modifier = Modifier.padding(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(250.dp)
                    .padding(5.dp, 10.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.padding(10.dp)
                ) {
                    NavigationBarIcon(
                        title = "Account",
                        image = painterResource(id = R.drawable.account_circle_24px)
                    ) {
                        navController.navigate(Routes.Accounts)
                    }
                    NavigationBarIcon(
                        title = "Security", image = painterResource(id = R.drawable.security_24px)
                    ) {
                        navController.navigate(Routes.Security)
                    }
                    NavigationBarIcon(
                        title = "Settings", image = painterResource(id = R.drawable.settings_24px)
                    ) {
                        navController.navigate(Routes.Setting)
                    }
                    NavigationBarIcon(
                        title = "Personalize", image = painterResource(id = R.drawable.personalize)
                    ) {
                        navController.navigate(Routes.Personalize)
                    }
                    NavigationBarIcon(
                        title = "Developers", image = painterResource(id = R.drawable.developers)
                    ) {
                        navController.navigate(Routes.Developers)
                    }
                    NavigationBarIcon(
                        title = "Report a bug", image = painterResource(id = R.drawable.report_bug)
                    ) {
                        navController.navigate(Routes.ReportBug)
                    }
                    NavigationBarIcon(
                        title = "Sign Out",
                        image = painterResource(id = R.drawable.logout_24px),
                        color = Color(0xFFF80808)
                    ) {
                        authViewModel.signOut()
                        navController.navigate(Routes.Login) {
                            navController.popBackStack()
                        }
                    }
                }
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp, 8.dp)
                        .align(Alignment.BottomCenter), shape = RoundedCornerShape(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.chat_bg),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .alpha(.24f)
                                .padding(6.dp)
                                .background(Color(0xFF2398AC))
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(5.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.logo_primary),
                                contentDescription = null,
                                modifier = Modifier.size(55.dp)
                            )
                            Spacer(modifier = Modifier.width(15.dp))
                            Text(
                                text = "ClubMate",
                                fontSize = 22.sp,
                                color = Color(0xFF071CCA),
                                fontFamily = roboto
                            )
                        }
                    }
                }
            }
        }
    }) {

        Scaffold(modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                coroutineScope {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.changes.any { it.changedToUp() }) {
                                expanded = false
                            }
                        }
                    }
                }
            }
            .systemBarsPadding(), topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 15.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ClubMate",
                    fontSize = 24.sp,
                    fontFamily = roboto,
                    textAlign = TextAlign.Start
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 8.dp)
                ) {

                    Image(painter = painterResource(id = R.drawable.private_channel),
                        contentDescription = null,
                        modifier = Modifier
                            .size(30.dp)
                            .clickable {
                                // Execute a private unauthenticated channel
                            })
                    Image(painter = painterResource(id = R.drawable.menubar),
                        contentDescription = null,
                        modifier = Modifier
                            .size(28.dp)
                            .clickable {
                                scope.launch {
                                    drawerState.open()
                                }
                            })
                }
            }
        }, floatingActionButton = {

            Row(
                modifier = Modifier.padding(10.dp)
            ) {
                if (chipsState == 0) {
                    Image(painter = painterResource(id = R.drawable.add_chat_fab),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(bottom = 10.dp)
                            .size(35.dp)
                            .clickable {
                                alertState = true
                            })
                } else {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {

                        AnimatedVisibility(visible = expanded,
                            enter = slideInVertically(animationSpec = tween(animationDuration)) { it } + fadeIn(
                                animationSpec = tween(animationDuration)
                            ),
                            exit = slideOutVertically(animationSpec = tween(animationDuration)) { it } + fadeOut(
                                animationSpec = tween(animationDuration)
                            )) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End
                            ) {

                                Icon(painter = painterResource(id = R.drawable.create),
                                    contentDescription = "Option 2",
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clickable {
                                            currentUser.value?.let {
                                                navController.navigate(
                                                    Routes.CreateGroup(
                                                        email = it.email!!, uid = it.uid
                                                    )
                                                )
                                            }
                                        })
                            }
                        }

                        // Option 2
                        AnimatedVisibility(visible = expanded,
                            enter = slideInVertically(animationSpec = tween(animationDuration)) { it } + fadeIn(
                                animationSpec = tween(animationDuration)
                            ),
                            exit = slideOutVertically(animationSpec = tween(animationDuration)) { it } + fadeOut(
                                animationSpec = tween(animationDuration)
                            )) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End
                            ) {
                                Icon(painter = painterResource(id = R.drawable.join),
                                    contentDescription = "Option 2",
                                    modifier = Modifier
                                        .size(50.dp)
                                        .clickable {
                                            grpState = true
                                        })
                            }
                        }
                        Image(painter = painterResource(id = R.drawable.manage_grp),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(bottom = 10.dp)
                                .size(45.dp)
                                .clickable {
                                    expanded = !expanded
                                })
                    }
                }
            }
        }, floatingActionButtonPosition = FabPosition.End
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xfffaf9f6))
                    .padding(10.dp)
            ) {

                Column(
                    modifier = Modifier.padding(top = 60.dp)
                ) {
                    if (alertState && chipsState == 0) {
                        AlertDialog(shape = RoundedCornerShape(15.dp), onDismissRequest = {
                            alertState = false
                            chatViewmodel.emptyUser()
                        }, confirmButton = {

                        }, title = {
                            Box(
                                modifier = Modifier
                                    .height(350.dp)
                                    .width(450.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    TextDesign(text = "Add chat to ClubMate", size = 17)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 10.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xD5C0D0F7)),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        if (query.isEmpty()) {
                                            Text(
                                                text = "Search by name or email",
                                                fontFamily = roboto,
                                                fontSize = 14.sp,
                                                color = Color.Black,
                                                modifier = Modifier.padding(start = 15.dp)
                                            )
                                        }
                                        BasicTextField(
                                            value = query,
                                            onValueChange = {
                                                query = it
                                            },
                                            modifier = Modifier
                                                .padding(15.dp)
                                                .fillMaxWidth(),
                                            textStyle = TextStyle(
                                                fontFamily = roboto,
                                                fontSize = 16.sp,
                                                color = Color.Black
                                            ),
                                            maxLines = 1,
                                            keyboardOptions = KeyboardOptions(
                                                imeAction = ImeAction.Done
                                            ),
                                            keyboardActions = KeyboardActions(onDone = {
                                                chatViewmodel.findUser(query)
                                            })
                                        )
                                        Image(painter = painterResource(id = R.drawable.search),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .padding(end = 15.dp)
                                                .size(22.dp)
                                                .align(Alignment.CenterEnd)
                                                .rotate(270f)
                                                .clickable {
                                                    chatViewmodel.findUser(query)
                                                })
                                    }
                                    Row {
                                        ItemDesignAlert(
                                            userState = chatViewmodel.userState
                                        )
                                    }
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.BottomCenter),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(
                                        onClick = {
                                            alertState = false
                                            query = ""
                                            chatViewmodel.emptyUser()
                                        },
                                        colors = ButtonDefaults.textButtonColors(Color.Red),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.width(120.dp)
                                    ) {
                                        TextDesign(text = "Cancel")
                                    }
                                    TextButton(
                                        border = BorderStroke(1.dp, Color.Blue),
                                        onClick = {
                                            if (query.isBlank()) {
                                                chatViewmodel.setUserEmpty("Input is empty")
                                            } else {
                                                currentUser.value?.uid?.let { uid ->
                                                    user?.let { user ->
                                                        if (user.uid.isNotEmpty()) {
                                                            chatViewmodel.initiateChat(
                                                                senderId = uid,
                                                                receiverId = user.uid,
                                                                message = "hello, there"
                                                            ) {

                                                            }
                                                            // Reset states after initiating chat
                                                            alertState = false
                                                            query = ""
                                                            chatViewmodel.emptyUser()
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.width(120.dp)
                                    ) {
                                        TextDesign(text = "Add Chat")
                                    }
                                }
                            }
                        })
                    }

                    if (grpState && chipsState == 1) {
                        AlertDialog(shape = RoundedCornerShape(15.dp), onDismissRequest = {
                            grpState = false
                            chatViewmodel.emptyUser()
                        }, confirmButton = {

                        }, title = {
                            Box(
                                modifier = Modifier.height(350.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    TextDesign(text = "Add groups to ClubMate", size = 17)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 10.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xD5C0D0F7)),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        if (query.isEmpty()) {
                                            Text(
                                                text = "Join groups by id",
                                                fontFamily = roboto,
                                                fontSize = 14.sp,
                                                color = Color.Black,
                                                modifier = Modifier.padding(start = 15.dp)
                                            )
                                        }
                                        BasicTextField(
                                            value = query,
                                            onValueChange = {
                                                query = it
                                            },
                                            modifier = Modifier
                                                .padding(15.dp)
                                                .fillMaxWidth(),
                                            textStyle = TextStyle(
                                                fontFamily = roboto,
                                                fontSize = 16.sp,
                                                color = Color.Black
                                            ),
                                            maxLines = 1,
                                            keyboardOptions = KeyboardOptions(
                                                imeAction = ImeAction.Done
                                            ),
                                            keyboardActions = KeyboardActions(onDone = {
                                                chatViewmodel.findUser(query)
                                            })
                                        )
                                        Image(painter = painterResource(id = R.drawable.search),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .padding(end = 15.dp)
                                                .size(22.dp)
                                                .align(Alignment.CenterEnd)
                                                .rotate(270f)
                                                .clickable {
                                                    chatViewmodel.findUser(query)
                                                })
                                    }
                                    Row {
                                        ItemDesignAlert(
                                            userState = chatViewmodel.userState
                                        )
                                    }
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.BottomCenter),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(
                                        onClick = {
                                            grpState = false
                                            query = ""
                                            chatViewmodel.emptyUser()
                                        },
                                        colors = ButtonDefaults.textButtonColors(Color.Red),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.width(120.dp)
                                    ) {
                                        TextDesign(text = "Cancel")
                                    }
                                    TextButton(
                                        border = BorderStroke(1.dp, Color.Blue),
                                        onClick = {
                                            if (query.isBlank()) {
                                                chatViewmodel.setUserEmpty("Input is empty")
                                            } else {

                                                currentUser.value?.uid?.let { uid ->
                                                    user?.let { user ->
                                                        if (user.uid.isNotEmpty()) {


                                                            // Reset states after initiating chat
                                                            grpState = false
                                                            query = ""
                                                            chatViewmodel.emptyUser()
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.width(120.dp)
                                    ) {
                                        TextDesign(text = "Join group")
                                    }
                                }
                            }
                        })
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            chips.forEachIndexed { index, s ->
                                Text(text = s,
                                    fontSize = 17.sp,
                                    color = if (chipsState == index) Color.Blue else Color.Black,
                                    textDecoration = if (chipsState == index) TextDecoration.Underline
                                    else TextDecoration.None,
                                    fontFamily = roboto,
                                    modifier = Modifier.clickable {
                                        chipsState = index
                                    })
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(15.dp))

                    if (isOnline) {
                        if (chipsState == 0) {
                            if (chatsList.value.isNotEmpty()) {

                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {

                                    items(chatsList.value.sortedByDescending {
                                        it.lastMessage?.timestamp
                                    }) { item ->
                                        val receiver = item.participants.find {
                                            it.uid != currentUserId
                                        }

                                        receiver?.username?.let { it1 ->
                                            item.lastMessage?.let { it2 ->

                                                ChatsDesign(
                                                    reciever = it1,
                                                    lastMessage = it2.messageText
                                                ) {
                                                    navController.navigate(
                                                        Routes.UserModel(
                                                            uid = receiver.uid,
                                                            username = receiver.username,
                                                            email = receiver.email,
                                                            chatID = item.chatId
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (chipsState == 1) {

                            LazyColumn(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                items(groupsList.value) { item ->
                                    currentUserId?.let {
                                        GroupDesign(
                                            grpName = item.grpName,
                                            grpId = item.grpId,
                                            lastActivity = item.lastActivity
                                        ) {
                                            navController.navigate(
                                                Routes.GroupModel(
                                                    user = currentUserId,
                                                    grpId = item.grpId
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            TextDesign(text = "Connect to internet", size = 18)
                        }
                    }
                }
            }
        }
    }
}


