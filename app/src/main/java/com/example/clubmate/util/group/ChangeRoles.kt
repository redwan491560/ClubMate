package com.example.clubmate.util.group

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.clubmate.R
import com.example.clubmate.db.Routes
import com.example.clubmate.ui.theme.Composables.Companion.TextDesign
import com.example.clubmate.ui.theme.roboto
import com.example.clubmate.viewmodel.GroupViewmodel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ChangeRoles(
    args: Routes.ChangeRoles,
    grpViewmodel: GroupViewmodel,
    navController: NavHostController
) {

    val currentUser = args.uid
    val membersList = grpViewmodel.participantsList.collectAsState()
    val scrollState = rememberLazyListState()
    var query by remember { mutableStateOf("") }

    var grpDetails by remember { mutableStateOf(Routes.GrpDetails()) }

    LaunchedEffect(Unit) {
        grpViewmodel.emptyUser()
        grpViewmodel.loadGroupInfo(grpId = args.grpId) {
            it?.let { grpDetails = it }
        }
        grpViewmodel.getAllParticipants(args.grpId)
    }

    val filteredMembers = remember(query, membersList.value) {
        membersList.value.filter {
            it.username.contains(query, ignoreCase = true) ||
                    it.email.contains(query, ignoreCase = true) ||
                    it.phone.contains(query, ignoreCase = true) ||
                    it.userType.name.contains(query, ignoreCase = true)
        }
    }

    Scaffold(
        modifier = Modifier
            .systemBarsPadding()
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextDesign(text = "Members of ${grpDetails.grpName}", size = 17)
            Spacer(modifier = Modifier.height(20.dp))

            // Search Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xD5C0D0F7)),
                contentAlignment = Alignment.CenterStart
            ) {
                if (query.isEmpty()) {
                    Text(
                        text = "Search member",
                        fontFamily = roboto,
                        fontSize = 14.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(start = 15.dp)
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier
                        .padding(15.dp)
                        .fillMaxWidth(),
                    textStyle = TextStyle(
                        fontFamily = roboto,
                        fontSize = 16.sp,
                        color = Color.Black
                    ),
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {})
                )
                Image(
                    painter = painterResource(id = R.drawable.search),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 15.dp)
                        .size(22.dp)
                        .align(Alignment.CenterEnd)
                        .rotate(270f)
                        .clickable { }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Show results only when user is searching
            if (query.isNotEmpty()) {
                if (filteredMembers.isEmpty()) {
                    Text(
                        text = "User not found",
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp
                    )
                } else {
                    LazyColumn(state = scrollState) {
                        items(filteredMembers) { item ->
                            MemberDesign(item, grpViewmodel.convertTimestamp(item.joinData)) {
                                navController.navigate(
                                    Routes.GroupUserDetails(
                                        grpId = args.grpId,
                                        userId = item.uid,
                                        currentUserId = currentUser
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(5.dp))
                        }
                    }
                }
            } else {
                LazyColumn(state = scrollState) {
                    items(membersList.value) { item ->
                        MemberDesign(item, grpViewmodel.convertTimestamp(item.joinData)) {
                            navController.navigate(
                                Routes.GroupUserDetails(
                                    grpId = args.grpId,
                                    userId = item.uid,
                                    currentUserId = currentUser
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(5.dp))
                    }
                }
            }
        }
    }
}