package com.example.clubmate.util.group

import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import coil3.compose.AsyncImage
import com.example.clubmate.R
import com.example.clubmate.db.Routes
import com.example.clubmate.screens.launchToast
import com.example.clubmate.ui.theme.Composables.Companion.ConsoleDetailsIcon
import com.example.clubmate.ui.theme.Composables.Companion.TextDesign
import com.example.clubmate.ui.theme.Composables.Companion.TextDesignClickable
import com.example.clubmate.ui.theme.roboto
import com.example.clubmate.util.Category
import com.example.clubmate.viewmodel.GroupViewmodel
import com.example.clubmate.viewmodel.RequestMap

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Console(
    args: Routes.Console, grpViewmodel: GroupViewmodel, navController: NavHostController
) {


    val requests = grpViewmodel.requestList.collectAsState()
    LaunchedEffect(requests.value) { grpViewmodel.listenToRequest(args.grpId) }

    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf(Category.General) }
    val categoryList = listOf(
        Category.General,
        Category.Admin,
        Category.President,
        Category.VicePresident,
        Category.Treasurer
    )
    var expandedDropDown by remember { mutableStateOf(false) }

    val allParticipants = grpViewmodel.participantsList.collectAsState()
    LaunchedEffect(Unit) {
        grpViewmodel.getAllParticipants(args.grpId)
    }

    val admins = remember(allParticipants) {
        allParticipants.value.filter { it.userType == Category.Admin }.size // Change "Admin" to desired role
    }

    val treasurer = remember(allParticipants) {
        allParticipants.value.filter { it.userType == Category.Treasurer }.size // Change "Admin" to desired role
    }

    val general = remember(allParticipants) {
        allParticipants.value.filter { it.userType == Category.General }.size // Change "Admin" to desired role
    }

    val president = remember(allParticipants) {
        allParticipants.value.filter { it.userType == Category.President }.size // Change "Admin" to desired role
    }

    val vp = remember(allParticipants) {
        allParticipants.value.filter { it.userType == Category.VicePresident }.size// Change "Admin" to desired role
    }


    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var link by remember { mutableStateOf("") }
    var notice by remember { mutableStateOf("") }

    val chips = listOf("Event", "Meetings", "Notice")
    var chipsState by rememberSaveable { mutableIntStateOf(0) }
    var expanded by rememberSaveable { mutableStateOf(false) }

    val chipsText = listOf("Event", "Request")
    var state by rememberSaveable { mutableIntStateOf(0) }


    val navList = listOf(
        NavItem(
            title = "Add members", image = R.drawable.add_24px
        ), NavItem(
            title = "Remove members", image = R.drawable.remove_24px
        ), NavItem(
            title = "Accept request", image = R.drawable.request
        ), NavItem(
            title = "View members", image = R.drawable.visibility_24px
        ), NavItem(
            title = "Change roles", image = R.drawable.change_role
        ), NavItem(
            title = "BLock members", image = R.drawable.block
        )
    )

    var grpDetails by remember { mutableStateOf(Routes.GrpDetails()) }
    LaunchedEffect(Unit) {
        grpViewmodel.loadGroupInfo(args.grpId) { details ->
            details?.let {
                grpDetails = details
            }
        }
    }


    var isLoading by remember {
        mutableStateOf(false)
    }
    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val openGalleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: android.net.Uri? ->
            uri?.let {
                selectedImageUri = it
            }
        }


    Scaffold(
        modifier = Modifier.systemBarsPadding()
    ) {

        Column(
            Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .height(120.dp)
                        .width(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(painter = painterResource(id = R.drawable.camera),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(end = 10.dp, bottom = 10.dp)
                            .size(20.dp)
                            .align(Alignment.BottomEnd)
                            .clickable {
                                openGalleryLauncher.launch("image/*")
                            })
                    AsyncImage(
                        model = grpDetails.photoUrl,
                        contentDescription = "Sent Image",
                        contentScale = ContentScale.Inside,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .clip(RoundedCornerShape(8.dp))
                            .size(80.dp),
                        error = painterResource(id = R.drawable.logo_primary), // Error Image
                    )
                }
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextDesign(text = args.description, size = 16)
                        Image(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = null,
                            Modifier.size(20.dp)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextDesign(text = args.grpName, size = 22)
                        Image(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = null,
                            Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 0.dp, end = 5.dp)
            ) {

                Column(
                    Modifier
                        .weight(9f)
                        .padding(start = 15.dp), Arrangement.spacedBy(10.dp)
                ) {

                    ConsoleDetailsIcon(
                        image = painterResource(id = R.drawable.person),
                        title = "Participants",
                        number = allParticipants.value.size.toString()
                    ) {

                    }
                    ConsoleDetailsIcon(
                        image = painterResource(id = R.drawable.admin),
                        title = "Admins",
                        number = admins.toString()
                    ) {

                    }

                    ConsoleDetailsIcon(
                        image = painterResource(id = R.drawable.president),
                        title = "President",
                        number = president.toString()
                    ) {

                    }

                    ConsoleDetailsIcon(
                        image = painterResource(id = R.drawable.vp),
                        title = "Vice President",
                        number = vp.toString()
                    ) {

                    }

                    ConsoleDetailsIcon(
                        image = painterResource(id = R.drawable.treasure),
                        title = "Treasurer",
                        number = treasurer.toString()
                    ) {

                    }
                    ConsoleDetailsIcon(
                        image = painterResource(id = R.drawable.person),
                        title = "General",
                        number = general.toString()
                    ) {

                    }
                }

                Card(shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.clickable {
                        expanded = !expanded
                    }) {
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Icon(imageVector = if (!expanded) Icons.AutoMirrored.Outlined.KeyboardArrowLeft
                        else Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(top = 10.dp, end = 13.dp)
                                .size(30.dp)
                                .clickable {
                                    expanded = !expanded
                                })
                        Box(modifier = Modifier
                            .padding(8.dp)
                            .clickable {
                                expanded = !expanded
                            }) {
                            VerticalNavigationBar(modifier = Modifier.align(Alignment.CenterEnd),
                                items = navList,
                                expanded = expanded,
                                onItemClicked = { index, _ ->
                                    when (index) {
                                        0 -> navController.navigate(
                                            Routes.AddUserToGroup(grpId = args.grpId)
                                        )

                                        1 -> navController.navigate(
                                            Routes.RemoveUserFromGroup(grpId = args.grpId)
                                        )

                                        2 -> navController.navigate(
                                            Routes.Request(uid = args.uid, grpId = args.grpId)
                                        )

                                        3 -> navController.navigate(
                                            Routes.ViewAllUser(grpId = args.grpId)
                                        )

                                        4 -> navController.navigate(
                                            Routes.ChangeRoles(grpId = args.grpId, uid = args.uid)
                                        )

                                        5 -> navController.navigate(
                                            Routes.Block(
                                                uid = args.uid, grpId = args.grpId
                                            )
                                        )

                                    }
                                },
                                onToggle = { expanded = !expanded } // Toggle the entire bar.
                            )
                        }
                    }
                }
            }
            Column(
                modifier = Modifier.padding(top = 15.dp, start = 10.dp, end = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    chipsText.forEachIndexed { ind, str ->
                        Text(
                            text = str,
                            fontSize = 20.sp,
                            textDecoration = if (state == ind) TextDecoration.Underline else TextDecoration.None,
                            color = if (state == ind) Color.Blue else Color.Black,
                            fontFamily = roboto,
                            modifier = Modifier.clickable {
                                state = ind
                            }
                        )
                        Spacer(modifier = Modifier.width(20.dp))
                    }
                    TextDesign(
                        text = if (requests.value.size.toString() == "0") "" else requests.value.size.toString(),
                        size = 20,
                        color = Color.Red
                    )
                }

                if (state == 0) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        Column(
                            modifier = Modifier.padding(start = 10.dp, end = 5.dp)
                        ) {
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                                chips.forEachIndexed { index, s ->
                                    Text(text = s,
                                        fontSize = 16.sp,
                                        color = if (chipsState == index) Color.Blue else Color.Black,
                                        textDecoration = if (chipsState == index) TextDecoration.Underline
                                        else TextDecoration.None,
                                        fontFamily = roboto,
                                        modifier = Modifier.clickable {
                                            chipsState = index
                                        })
                                }
                            }

                            // text field

                            Spacer(modifier = Modifier.heightIn(15.dp))
                            Row(
                                modifier = Modifier.padding(end = 10.dp)
                            ) {
                                ExposedDropdownMenuBox(expanded = expandedDropDown,
                                    onExpandedChange = { expandedDropDown = !expandedDropDown }) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 15.dp)
                                                .menuAnchor()
                                        ) {
                                            TextDesign(
                                                text = "visibility: " + selectedCategory.name,
                                                size = 16
                                            )
                                            TrailingIcon(
                                                expanded = expandedDropDown
                                            )
                                        }
                                    }

                                    ExposedDropdownMenu(
                                        expanded = expandedDropDown,
                                        onDismissRequest = { expandedDropDown = false },
                                        modifier = Modifier.padding(horizontal = 3.dp)
                                    ) {
                                        categoryList.forEach { category ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = category.name,
                                                        fontFamily = roboto,
                                                        fontSize = 16.sp
                                                    )
                                                },
                                                onClick = {
                                                    selectedCategory = category
                                                    expandedDropDown = false
                                                },
                                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                            )
                                        }
                                    }
                                }

                            }


                            when (chipsState) {
                                0 -> {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(end = 10.dp)
                                    ) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(50.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xD5C0D0F7)),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            if (title.isEmpty()) {
                                                Text(
                                                    text = "Add title",
                                                    fontFamily = roboto,
                                                    fontSize = 16.sp,
                                                    color = Color.Black,
                                                    modifier = Modifier.padding(start = 15.dp)
                                                )
                                            }
                                            BasicTextField(
                                                value = title,
                                                onValueChange = {
                                                    title = it
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
                                                    imeAction = ImeAction.Next
                                                )
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xD5C0D0F7)),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            if (message.isEmpty()) {
                                                Text(
                                                    text = "Add message",
                                                    fontFamily = roboto,
                                                    fontSize = 16.sp,
                                                    color = Color.Black,
                                                    modifier = Modifier.padding(start = 15.dp)
                                                )
                                            }
                                            BasicTextField(
                                                value = message,
                                                onValueChange = {
                                                    message = it
                                                },
                                                modifier = Modifier
                                                    .padding(15.dp)
                                                    .fillMaxWidth(),
                                                textStyle = TextStyle(
                                                    fontFamily = roboto,
                                                    fontSize = 16.sp,
                                                    color = Color.Black
                                                ),
                                                maxLines = 3,
                                                keyboardOptions = KeyboardOptions(
                                                    imeAction = ImeAction.Done
                                                ),
                                                keyboardActions = KeyboardActions(onDone = {

                                                })
                                            )
                                        }
                                    }
                                }

                                1 -> {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(end = 10.dp)
                                    ) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(50.dp)

                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xD5C0D0F7)),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            if (title.isEmpty()) {
                                                Text(
                                                    text = "Add title",
                                                    fontFamily = roboto,
                                                    fontSize = 16.sp,
                                                    color = Color.Black,
                                                    modifier = Modifier.padding(start = 15.dp)
                                                )
                                            }
                                            BasicTextField(
                                                value = title,
                                                onValueChange = {
                                                    title = it
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
                                                    imeAction = ImeAction.Next
                                                )
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xD5C0D0F7)),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            if (link.isEmpty()) {
                                                Text(
                                                    text = "Add link",
                                                    fontFamily = roboto,
                                                    fontSize = 16.sp,
                                                    color = Color.Black,
                                                    modifier = Modifier.padding(start = 15.dp)
                                                )
                                            }
                                            BasicTextField(
                                                value = link,
                                                onValueChange = {
                                                    link = it
                                                },
                                                modifier = Modifier
                                                    .padding(15.dp)
                                                    .fillMaxWidth(),
                                                textStyle = TextStyle(
                                                    fontFamily = roboto,
                                                    fontSize = 16.sp,
                                                    color = Color.Black
                                                ),
                                                maxLines = 2,
                                                keyboardOptions = KeyboardOptions(
                                                    imeAction = ImeAction.Done
                                                ),
                                                keyboardActions = KeyboardActions(onDone = {

                                                })
                                            )
                                        }
                                    }
                                }

                                2 -> {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(end = 10.dp)
                                    ) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(50.dp)

                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xD5C0D0F7)),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            if (title.isEmpty()) {
                                                Text(
                                                    text = "Add title",
                                                    fontFamily = roboto,
                                                    fontSize = 16.sp,
                                                    color = Color.Black,
                                                    modifier = Modifier.padding(start = 15.dp)
                                                )
                                            }
                                            BasicTextField(
                                                value = title,
                                                onValueChange = {
                                                    title = it
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
                                                    imeAction = ImeAction.Next
                                                )
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xD5C0D0F7)),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            if (notice.isEmpty()) {
                                                Text(
                                                    text = "Add notice",
                                                    fontFamily = roboto,
                                                    fontSize = 16.sp,
                                                    color = Color.Black,
                                                    modifier = Modifier.padding(start = 15.dp)
                                                )
                                            }
                                            BasicTextField(
                                                value = notice,
                                                onValueChange = {
                                                    notice = it
                                                },
                                                modifier = Modifier
                                                    .padding(15.dp)
                                                    .fillMaxWidth(),
                                                textStyle = TextStyle(
                                                    fontFamily = roboto,
                                                    fontSize = 16.sp,
                                                    color = Color.Black
                                                ),
                                                maxLines = 3,
                                                keyboardOptions = KeyboardOptions(
                                                    imeAction = ImeAction.Done
                                                ),
                                                keyboardActions = KeyboardActions(onDone = {

                                                })
                                            )
                                        }

                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(end = 15.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                OutlinedButton(shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.width(100.dp),
                                    onClick = {

                                    }) {
                                    Text(text = "Upload")
                                }
                                ExpandableIconsRow()
                            }


                        }


                    }
                } else {

                    Spacer(modifier = Modifier.height(10.dp))
                    LazyColumn {
                        items(requests.value.sortedBy {
                            it.sentTime
                        }) { item ->
                            RequestDesign(
                                requestMap = item,
                                time = grpViewmodel.convertTimestamp(item.sentTime),
                                onClick = {
                                    grpViewmodel.acceptRequest(grpId = args.grpId, item)
                                }
                            ) {
                                grpViewmodel.declineRequest(grpId = args.grpId, item)
                            }
                        }
                    }

                }


            }
        }



        if (selectedImageUri != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 40.dp, vertical = 160.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                selectedImageUri?.let { uri ->

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TextDesign(text = "Select profile picture", size = 18, color = Color.White)
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = "Selected Image",
                            contentScale = ContentScale.Inside,
                            modifier = Modifier
                                .padding(8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .width(200.dp)
                                .height(300.dp)
                        )

                        if (isLoading) {
                            LinearProgressIndicator()
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {

                            Image(colorFilter = ColorFilter.tint(Color.White),
                                painter = painterResource(id = R.drawable.delete_msg),
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(top = 5.dp, end = 15.dp)
                                    .size(40.dp)
                                    .clickable {
                                        selectedImageUri = null
                                    })

                            Image(colorFilter = ColorFilter.tint(Color.White),
                                imageVector = Icons.Outlined.Done,
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(top = 5.dp, end = 5.dp, bottom = 5.dp)
                                    .size(40.dp)
                                    .clickable {
                                        selectedImageUri?.let {
                                            selectedImageUri?.let {
                                                grpViewmodel.updateGroupProfilePicture(
                                                    grpId = args.grpId, uri = selectedImageUri!!
                                                ) {
                                                    selectedImageUri = null
                                                    isLoading = false
                                                    if (it) {
                                                        launchToast(
                                                            context = context, "Success"
                                                        )
                                                    } else {
                                                        launchToast(
                                                            context = context, "try again"
                                                        )
                                                    }
                                                }
                                            }
                                        } ?: run {
                                            launchToast(
                                                context = context, "Error occured try again"
                                            )
                                        }

                                    })
                        }
                    }
                }

            }
        }
    }
}

enum class EventCategory {
    Meeting, Notice, Event
}


data class NavItem(
    val title: String, val image: Int
)

@Composable
fun VerticalNavigationBar(
    modifier: Modifier = Modifier,
    items: List<NavItem>,
    expanded: Boolean,
    onItemClicked: (index: Int, item: NavItem) -> Unit,
    onToggle: () -> Unit
) {
    Column(modifier = modifier
        .padding(vertical = 10.dp)
        .clickable { onToggle() }
        .animateContentSize(animationSpec = tween(durationMillis = 300)),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.End) {
        items.forEachIndexed { index, item ->
            Row(verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .height(35.dp)
                    .clickable { onItemClicked(index, item) }
                    .padding(horizontal = 8.dp, vertical = 4.dp)) {
                if (expanded) {
                    TextDesign(text = item.title, size = 17)
                    Spacer(modifier = Modifier.width(12.dp))
                }
                Icon(
                    painter = painterResource(id = item.image),
                    contentDescription = item.title,
                    tint = Color.Black,
                    modifier = Modifier.size(25.dp)
                )

            }
        }
    }

}

@Composable
fun RequestDesign(
    requestMap: RequestMap,
    time: String,
    onClick: (RequestMap) -> Unit,
    onDecline: (RequestMap) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xCCDCF0D1))
            .padding(vertical = 5.dp, horizontal = 15.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            TextDesign(text = requestMap.username, size = 19)
            TextDesign(text = requestMap.email, size = 14)
            TextDesign(text = time, size = 12)
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextDesignClickable(text = "Decline", color = Color.Blue) {
                onDecline(requestMap)
            }
            TextDesignClickable(text = "Approve", color = Color.Blue) {
                onClick(requestMap)
            }
        }

    }
}


@Composable
fun ExpandableIconsRow() {
    var expanded by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = expandHorizontally(animationSpec = tween(durationMillis = 300)) + fadeIn(),
            exit = shrinkHorizontally(animationSpec = tween(durationMillis = 300)) + fadeOut()
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(painter = painterResource(id = R.drawable.camera),
                    contentDescription = "Image",
                    modifier = Modifier
                        .size(25.dp)
                        .clickable { /* Handle Click */ })
                Icon(painter = painterResource(id = R.drawable.video),
                    contentDescription = "Video",
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { /* Handle Click */ })
                Icon(painter = painterResource(id = R.drawable.file),
                    contentDescription = "File",
                    modifier = Modifier
                        .size(23.dp)
                        .clickable { /* Handle Click */ })
            }
        }

        Icon(painter = painterResource(id = R.drawable.attachment_24px),
            contentDescription = null,
            modifier = Modifier
                .size(30.dp)
                .clickable { expanded = !expanded })
    }
}