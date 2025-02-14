package com.example.clubmate.screens.grp

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.example.clubmate.R
import com.example.clubmate.db.Routes
import com.example.clubmate.ui.theme.Composables.Companion.TextDesign
import com.example.clubmate.ui.theme.ItemDesignAlert
import com.example.clubmate.ui.theme.roboto
import com.example.clubmate.util.group.MemberDesign
import com.example.clubmate.viewmodel.AuthViewModel
import com.example.clubmate.viewmodel.GroupViewmodel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "StateFlowValueCalledInComposition")
@Composable
fun ViewAllParticipants(
    grpInfo: Routes.ViewAllUser,
    grpViewmodel: GroupViewmodel,
    authViewModel: AuthViewModel,
    navController: NavHostController
) {


    val currentUser = authViewModel.currentUser
    // returns the searched user
    val membersList = grpViewmodel.participantsList.collectAsState()
    val scrollState = rememberLazyListState()
    var query by remember { mutableStateOf("") }

    var grpDetails by remember { mutableStateOf(Routes.GrpDetails()) }

    LaunchedEffect(Unit) {
        grpViewmodel.emptyUser()
        grpViewmodel.loadGroupInfo(grpId = grpInfo.grpId) {
            it?.let {
                grpDetails = it
            }
        }
    }

    val filteredMembers = remember(query, membersList.value) {
        membersList.value.filter {
            it.username.contains(query, ignoreCase = true) || it.email.contains(
                query,
                ignoreCase = true
            ) || it.phone.contains(query, ignoreCase = true) || it.userType.name.contains(
                query,
                ignoreCase = true
            )
        }
    }


    LaunchedEffect(Unit) {
        grpViewmodel.getAllParticipants(grpInfo.grpId)
    }


    Scaffold {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xCCEAEEAC))
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
            ) {

                AsyncImage(
                    model = grpDetails.photoUrl,
                    contentDescription = "Sent Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(40.dp))
                        .size(80.dp),
                    error = painterResource(id = R.drawable.logo_primary), // Error Image
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            TextDesign(text = grpDetails.grpName, size = 17)
            TextDesign(text = grpDetails.description, size = 17)
            Spacer(modifier = Modifier.height(10.dp))

        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 180.dp, start = 15.dp, end = 15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            TextDesign(text = "All members of ${grpDetails.grpName}", size = 18)
            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xD5C0D0F7)), contentAlignment = Alignment.CenterStart
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
                    value = query, onValueChange = {
                        query = it
                    }, modifier = Modifier
                        .padding(15.dp)
                        .fillMaxWidth(), textStyle = TextStyle(
                        fontFamily = roboto, fontSize = 16.sp, color = Color.Black
                    ), maxLines = 1
                )
                Image(painter = painterResource(id = R.drawable.search),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 15.dp)
                        .size(22.dp)
                        .align(Alignment.CenterEnd)
                        .rotate(270f)
                        .clickable {

                        })
            }

            Column {
                ItemDesignAlert(userState = grpViewmodel.userState)
            }
            Spacer(modifier = Modifier.height(20.dp))


            if (query.isNotEmpty()) {
                if (filteredMembers.isEmpty()) {
                    Text(
                        text = "User not found",
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.Gray, fontFamily = roboto,
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp
                    )
                } else {
                    LazyColumn(state = scrollState) {
                        items(filteredMembers) { item ->
                            currentUser.value?.uid?.let { it1 ->
                                MemberDesign(item, grpViewmodel.convertTimestamp(item.joinData)) {
                                    navController.navigate(
                                        Routes.GroupUserDetails(
                                            grpId = grpInfo.grpId,
                                            userId = item.uid,
                                            currentUserId = it1
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(5.dp))
                        }
                    }
                }
            } else {
                LazyColumn(state = scrollState) {
                    items(membersList.value) { item ->
                        currentUser.value?.uid?.let { it1 ->
                            MemberDesign(item, grpViewmodel.convertTimestamp(item.joinData)) {
                                navController.navigate(
                                    Routes.GroupUserDetails(
                                        grpId = grpInfo.grpId,
                                        userId = item.uid,
                                        currentUserId = it1
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(5.dp))
                    }
                }
            }
        }


    }
}

