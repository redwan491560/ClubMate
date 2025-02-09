package com.example.clubmate.util.group

import android.annotation.SuppressLint
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.example.clubmate.R
import com.example.clubmate.db.Routes
import com.example.clubmate.ui.theme.Composables.Companion.TextDesign
import com.example.clubmate.ui.theme.roboto
import com.example.clubmate.viewmodel.GroupViewmodel

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Console(
    args: Routes.Console, grpViewmodel: GroupViewmodel, navController: NavHostController
) {

    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var link by remember { mutableStateOf("") }
    var notice by remember { mutableStateOf("") }


    val chips = listOf("Event", "Meetings", "Notice")
    var chipsState by rememberSaveable { mutableIntStateOf(0) }
    var expanded by rememberSaveable { mutableStateOf(false) }

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



    Scaffold(
        modifier = Modifier.systemBarsPadding()
    ) {
        Column(
            Modifier
                .fillMaxSize()
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
                    Image(
                        painter = painterResource(id = R.drawable.camera),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(end = 10.dp, bottom = 10.dp)
                            .size(20.dp)
                            .align(Alignment.BottomEnd)
                    )
                    AsyncImage(
                        model = "message.imageRef",
                        contentDescription = "Sent Image",
                        contentScale = ContentScale.Inside,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .clip(RoundedCornerShape(8.dp))
                            .size(40.dp),
                        error = painterResource(id = R.drawable.loading), // Error Image
                        placeholder = painterResource(R.drawable.loading)
                    )
                }
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextDesign(text = args.description, size = 14)
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
                verticalAlignment = Alignment.Top,
                modifier = Modifier.padding(start = 16.dp, end = 8.dp)
            ) {
                Column(
                    modifier = Modifier.weight(9f)
                ) {
                    TextDesign(text = "Manage timeline", size = 20)
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
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xD5C0D0F7)),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (title.isEmpty()) {
                                        Text(
                                            text = "Add title",
                                            fontFamily = roboto,
                                            fontSize = 14.sp,
                                            color = Color.Black,
                                            modifier = Modifier.padding(start = 15.dp)
                                        )
                                    }
                                    BasicTextField(
                                        value = title, onValueChange = {
                                            title = it
                                        }, modifier = Modifier
                                            .padding(15.dp)
                                            .fillMaxWidth(), textStyle = TextStyle(
                                            fontFamily = roboto,
                                            fontSize = 16.sp,
                                            color = Color.Black
                                        ), maxLines = 1,
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
                                            fontSize = 14.sp,
                                            color = Color.Black,
                                            modifier = Modifier.padding(start = 15.dp)
                                        )
                                    }
                                    BasicTextField(
                                        value = message, onValueChange = {
                                            message = it
                                        }, modifier = Modifier
                                            .padding(15.dp)
                                            .fillMaxWidth(), textStyle = TextStyle(
                                            fontFamily = roboto,
                                            fontSize = 16.sp,
                                            color = Color.Black
                                        ), maxLines = 1,
                                        keyboardOptions = KeyboardOptions(
                                            imeAction = ImeAction.Done
                                        ), keyboardActions = KeyboardActions(onDone = {

                                        })
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp)
                                        .padding(start = 5.dp),
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    ExpandableIconsRow()
                                }
                                OutlinedButton(
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.width(120.dp),
                                    onClick = {

                                    }) {
                                    Text(text = "Upload")
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
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xD5C0D0F7)),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (title.isEmpty()) {
                                        Text(
                                            text = "Add title",
                                            fontFamily = roboto,
                                            fontSize = 14.sp,
                                            color = Color.Black,
                                            modifier = Modifier.padding(start = 15.dp)
                                        )
                                    }
                                    BasicTextField(
                                        value = title, onValueChange = {
                                            title = it
                                        }, modifier = Modifier
                                            .padding(15.dp)
                                            .fillMaxWidth(), textStyle = TextStyle(
                                            fontFamily = roboto,
                                            fontSize = 16.sp,
                                            color = Color.Black
                                        ), maxLines = 1,
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
                                            fontSize = 14.sp,
                                            color = Color.Black,
                                            modifier = Modifier.padding(start = 15.dp)
                                        )
                                    }
                                    BasicTextField(
                                        value = link, onValueChange = {
                                            link = it
                                        }, modifier = Modifier
                                            .padding(15.dp)
                                            .fillMaxWidth(), textStyle = TextStyle(
                                            fontFamily = roboto,
                                            fontSize = 16.sp,
                                            color = Color.Black
                                        ), maxLines = 1,
                                        keyboardOptions = KeyboardOptions(
                                            imeAction = ImeAction.Done
                                        ), keyboardActions = KeyboardActions(onDone = {

                                        })
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedButton(
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.width(120.dp),
                                    onClick = {

                                    }) {
                                    Text(text = "Upload")
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
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xD5C0D0F7)),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (title.isEmpty()) {
                                        Text(
                                            text = "Add title",
                                            fontFamily = roboto,
                                            fontSize = 14.sp,
                                            color = Color.Black,
                                            modifier = Modifier.padding(start = 15.dp)
                                        )
                                    }
                                    BasicTextField(
                                        value = title, onValueChange = {
                                            title = it
                                        }, modifier = Modifier
                                            .padding(15.dp)
                                            .fillMaxWidth(), textStyle = TextStyle(
                                            fontFamily = roboto,
                                            fontSize = 16.sp,
                                            color = Color.Black
                                        ), maxLines = 1,
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
                                            fontSize = 14.sp,
                                            color = Color.Black,
                                            modifier = Modifier.padding(start = 15.dp)
                                        )
                                    }
                                    BasicTextField(
                                        value = notice, onValueChange = {
                                            notice = it
                                        }, modifier = Modifier
                                            .padding(15.dp)
                                            .fillMaxWidth(), textStyle = TextStyle(
                                            fontFamily = roboto,
                                            fontSize = 16.sp,
                                            color = Color.Black
                                        ), maxLines = 1,
                                        keyboardOptions = KeyboardOptions(
                                            imeAction = ImeAction.Done
                                        ), keyboardActions = KeyboardActions(onDone = {

                                        })
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedButton(
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.width(120.dp),
                                    onClick = {

                                    }) {
                                    Text(text = "Upload")
                                }
                            }
                        }
                    }


                }
                Card(shape = RoundedCornerShape(6.dp), modifier = Modifier.clickable {
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
    expanded: Boolean,                     // Determines if the nav bar is expanded
    onItemClicked: (index: Int, item: NavItem) -> Unit,        // Called when an item is clicked
    onToggle: () -> Unit                     // Called when the bar itself is tapped to toggle expanded/collapsed state
) {
    Column(modifier = modifier
        .padding(vertical = 10.dp)
        // Make the entire bar clickable to toggle expansion
        .clickable { onToggle() }
        .animateContentSize(animationSpec = tween(durationMillis = 300)),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.End) {
        items.forEachIndexed { index, item ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                // Allow item-specific click actions if desired.
                .height(35.dp)
                .clickable { onItemClicked(index, item) }
                .padding(horizontal = 8.dp, vertical = 4.dp)) {
                // When expanded, show the title next to the icon.
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
fun ExpandableIconsRow() {
    var expanded by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Primary Icon (Click to Expand)

        Icon(
            painter = painterResource(id = R.drawable.attachment_24px),
            contentDescription = null,
            modifier = Modifier
                .size(30.dp)
                .clickable { expanded = !expanded }
        )
        // Animated Icons
        AnimatedVisibility(
            visible = expanded,
            enter = expandHorizontally(animationSpec = tween(durationMillis = 300)) + fadeIn(),
            exit = shrinkHorizontally(animationSpec = tween(durationMillis = 300)) + fadeOut()
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(
                    painter = painterResource(id = R.drawable.camera),
                    contentDescription = "Image",
                    modifier = Modifier
                        .size(25.dp)
                        .clickable { /* Handle Click */ }
                )
                Icon(
                    painter = painterResource(id = R.drawable.video),
                    contentDescription = "Video",
                    modifier = Modifier
                        .size(25.dp)
                        .clickable { /* Handle Click */ }
                )
                Icon(
                    painter = painterResource(id = R.drawable.file),
                    contentDescription = "File",
                    modifier = Modifier
                        .size(25.dp)
                        .clickable { /* Handle Click */ }
                )
            }
        }

    }
}
