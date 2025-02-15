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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.clubmate.R
import com.example.clubmate.db.Routes
import com.example.clubmate.db.Status
import com.example.clubmate.ui.theme.Composables.Companion.TextDesign
import com.example.clubmate.ui.theme.ItemDesignAlert
import com.example.clubmate.ui.theme.ItemDesignGroupAlert
import com.example.clubmate.ui.theme.roboto
import com.example.clubmate.util.NavBarItems
import com.example.clubmate.util.chat.ChatsDesign
import com.example.clubmate.util.getInternetConnectionStatus
import com.example.clubmate.util.group.GroupDesign
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
    val authState = authViewModel.authState
    val currentUserId = currentUser.value?.uid

    LaunchedEffect(Unit) {
        if (authState.value == Status.NotAuthenticated)
            navController.navigate(Routes.Login)
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var query by remember { mutableStateOf("") }

    // returns the searched user
    val user = chatViewmodel.user
    val group = grpViewmodel.group

    val chips = listOf("Groups", "Contacts")
    var chipsState by rememberSaveable { mutableIntStateOf(0) }

    val animationDuration = 300
    var isSorted by remember { mutableStateOf(false) }

    val chatsList = chatViewmodel.chats.collectAsStateWithLifecycle()
    val groupsList = grpViewmodel.groupsList.collectAsStateWithLifecycle()

    LaunchedEffect(groupsList.value) {
        currentUser.value?.let {
            grpViewmodel.listenForGroups(it.uid)
        }
    }


    LaunchedEffect(chatsList.value) {
        currentUser.value?.let {
            chatViewmodel.listenForChats(currentUser.value!!.uid)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            chatViewmodel.clearChats()
            grpViewmodel.clearGroups()
        }
    }


    var searchText by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    var isOnline by remember { mutableStateOf(true) }

    LaunchedEffect(context) {
        getInternetConnectionStatus(context).collect { isConnected ->
            isOnline = isConnected
        }
    }

    val filteredChats = remember(searchText, chatsList.value) {
        chatsList.value.filter {
            it.participants[0].username.contains(searchText, ignoreCase = true) ||
                    it.participants[1].username.contains(searchText, ignoreCase = true) ||
                    it.participants[0].phone.contains(searchText, ignoreCase = true) ||
                    it.participants[1].phone.contains(searchText, ignoreCase = true)

        }
    }
    val filteredGroups = remember(searchText, chatsList.value) {
        groupsList.value.filter {
            it.grpName.contains(searchText, ignoreCase = true)
        }
    }



    ModalNavigationDrawer(drawerState = drawerState, gesturesEnabled = true, drawerContent = {
        ModalDrawerSheet(

            drawerShape = RoundedCornerShape(6.dp), modifier = Modifier.padding(8.dp)
        ) {
            NavBarItems(onAccount = { navController.navigate(Routes.Accounts) },
                onSecurity = { navController.navigate(Routes.Security) },
                onSettings = { navController.navigate(Routes.Setting) },
                onPersonalize = { navController.navigate(Routes.Personalize) },
                onDevelopers = { navController.navigate(Routes.Developers) },
                onReportBug = { navController.navigate(Routes.ReportBug) }) {
                authViewModel.signOut()
                navController.navigate(Routes.Login) {
                    navController.popBackStack()
                }
            }
        }
    }) {

        Scaffold(modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDF7F4))
            .systemBarsPadding()
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
            }, topBar = {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFDF7F4))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.padding(start = 5.dp),
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
                        Image(
                            painter = painterResource(id = R.drawable.private_channel),
                            contentDescription = null,
                            modifier = Modifier
                                .size(30.dp)
                                .clickable {
                                    navController.navigate(Routes.PrivateAuth)
                                }
                        )

                        Image(
                            painter = painterResource(id = R.drawable.menubar),
                            contentDescription = null,
                            modifier = Modifier
                                .size(28.dp)
                                .clickable {
                                    scope.launch {
                                        drawerState.open()
                                    }
                                }
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .height(45.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFC4F3B3)),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (searchText.isEmpty()) {
                        Text(
                            text = "Search...",
                            fontFamily = roboto,
                            fontSize = 18.sp,
                            color = Color.Black,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 20.dp)
                        )
                    }
                    BasicTextField(
                        value = searchText, onValueChange = {
                            searchText = it
                        }, textStyle = TextStyle(
                            fontFamily = roboto,
                            fontSize = 18.sp,
                            color = Color.Black
                        ), maxLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done
                        )
                    )
                }
            }

        }, floatingActionButton = {
            Row(
                modifier = Modifier.padding(10.dp)
            ) {
                if (chipsState == 1) {
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
                        Image(
                            painter = painterResource(id = R.drawable.manage_grp),
                            contentDescription = null,
                            modifier = Modifier
                                .padding(bottom = 10.dp)
                                .size(45.dp)
                                .clickable {
                                    expanded = !expanded
                                }
                        )
                    }
                }
            }
        },
            floatingActionButtonPosition = FabPosition.End
        ) {

            // content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFFDF7F4))
                    .padding(10.dp)
            ) {

                Column(
                    modifier = Modifier.padding(top = 120.dp)
                ) {
                    if (alertState && chipsState == 1) {

                        AlertStateDesign(
                            chatViewmodel = chatViewmodel,
                            onStateChange = {
                                alertState = false
                            },
                            searchValue = query,
                            onSearchValueChange = { query = it },
                            onAddClick = {
                                currentUser.value?.uid?.let { uid ->
                                    user?.let { user ->
                                        if (user.uid.isNotEmpty()) {
                                            chatViewmodel.initiateChat(
                                                senderId = uid,
                                                receiverId = user.uid,
                                                message = ""
                                            ) {

                                            }
                                            alertState = false
                                            query = ""
                                            chatViewmodel.emptyUser()
                                        }
                                    }
                                }
                            }) {
                            alertState = false
                            query = ""
                            chatViewmodel.emptyUser()
                        }

                    }

                    if (grpState && chipsState == 0) {
                        AlertDialog(
                            shape = RoundedCornerShape(15.dp),
                            onDismissRequest = {
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
                                                    grpViewmodel.findGroup(query)
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
                                                        grpViewmodel.findGroup(query)
                                                    })
                                        }
                                        Row {
                                            ItemDesignGroupAlert(
                                                groupState = grpViewmodel.groupState
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
                                                grpViewmodel.emptyGroup()
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
                                                    grpViewmodel.emptyGroup("Input is empty")
                                                } else {
                                                    grpViewmodel.group?.let {
                                                        currentUser.value?.uid?.let { uid ->
                                                            navController.navigate(
                                                                Routes.Request(
                                                                    grpId = it.grpId,
                                                                    uid = uid
                                                                )
                                                            )
                                                        }
                                                        grpState = false
                                                        query = ""
                                                        grpViewmodel.emptyGroup()
                                                    }
                                                }
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.width(120.dp)
                                        ) {
                                            TextDesign(text = "Send request")
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
                                    fontSize = 18.sp,
                                    color = if (chipsState == index) Color.Blue else Color.Black,
                                    textDecoration = if (chipsState == index) TextDecoration.Underline
                                    else TextDecoration.None,
                                    fontFamily = roboto,
                                    modifier = Modifier.clickable {
                                        chipsState = index
                                        if (index == 0){
                                            currentUser.value?.let {
                                                chatViewmodel.listenForChats(currentUser.value!!.uid)
                                            }
                                        }else{
                                            currentUser.value?.let {
                                                grpViewmodel.listenForGroups(it.uid)
                                            }
                                        }
                                    })
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(15.dp))

                    if (isOnline) {

                        if (chipsState == 1 && searchText.isNotEmpty()) {
                            if (filteredChats.isEmpty()) {
                                Text(
                                    text = "User not found",
                                    modifier = Modifier.fillMaxWidth(),
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center,
                                    fontSize = 16.sp
                                )
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {

                                    items(filteredChats) { item ->
                                        val receiver =
                                            item.participants.find { it.uid != currentUserId }

                                        receiver?.username?.let {
                                            item.lastMessage?.let { it2 ->
                                                ChatsDesign(
                                                    pp = receiver.photoUrl,
                                                    reciever = receiver.username,
                                                    uri = it2.imageRef,
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
                        } else if (chipsState == 0 && searchText.isNotEmpty()) {
                            if (filteredGroups.isEmpty()) {
                                Text(
                                    text = "Group not found",
                                    modifier = Modifier.fillMaxWidth(),
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center,
                                    fontSize = 16.sp
                                )
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {

                                    items(filteredGroups) { item ->
                                        currentUserId?.let {
                                            GroupDesign(
                                                grpName = item.grpName,
                                                lastActivity = item.lastActivity
                                            ) {
                                                navController.navigate(
                                                    Routes.GroupModel(
                                                        user = currentUserId, grpId = item.grpId
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            if (chipsState == 1) {
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

                                            receiver?.username?.let {
                                                item.lastMessage?.let { it2 ->
                                                    ChatsDesign(
                                                        pp = receiver.photoUrl,
                                                        reciever = receiver.username,
                                                        uri = it2.imageRef,
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
                            if (chipsState == 0) {

                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    items(groupsList.value.sortedByDescending {
                                        it.lastActivity.message.timestamp
                                    }) { item ->
                                        currentUserId?.let {
                                            GroupDesign(
                                                grpName = item.grpName,
                                                lastActivity = item.lastActivity
                                            ) {
                                                navController.navigate(
                                                    Routes.GroupModel(
                                                        user = currentUserId, grpId = item.grpId
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                        ) {
                            TextDesign(text = "Connect to internet", size = 18)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxSize(), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Absolute.Center
                ) {
                    TextDesign(text = "alpha version 1.6.11", size = 11)
                }
            }
        }
    }
}


@Composable
fun AlertStateDesign(
    chatViewmodel: ChatViewModel,
    onStateChange: () -> Unit,
    searchValue: String,
    onSearchValueChange: (String) -> Unit,
    onAddClick: () -> Unit,
    onCancelClick: () -> Unit
) {

    AlertDialog(
        shape = RoundedCornerShape(15.dp), onDismissRequest = {
            onStateChange()
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
                            .background(Color(0xFFFBFFE4)),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (searchValue.isEmpty()) {
                            Text(
                                text = "Search by name or email",
                                fontFamily = roboto,
                                fontSize = 14.sp,
                                color = Color.Black,
                                modifier = Modifier.padding(start = 15.dp)
                            )
                        }
                        BasicTextField(
                            value = searchValue,
                            onValueChange = {
                                onSearchValueChange(it)
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
                                chatViewmodel.findUser(searchValue)
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
                                    chatViewmodel.findUser(searchValue)
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
                            onCancelClick()
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
                            if (searchValue.isBlank()) {
                                chatViewmodel.setUserEmpty("Input is empty")
                            } else {
                                onAddClick()
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.width(120.dp)
                    ) {
                        TextDesign(text = "Add Chat")
                    }
                }
            }
        }
    )
}