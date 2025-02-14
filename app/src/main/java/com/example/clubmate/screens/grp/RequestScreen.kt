package com.example.clubmate.screens.grp

import android.annotation.SuppressLint
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.example.clubmate.R
import com.example.clubmate.db.Routes
import com.example.clubmate.screens.launchToast
import com.example.clubmate.ui.theme.Composables.Companion.TextDesign
import com.example.clubmate.ui.theme.roboto
import com.example.clubmate.viewmodel.GroupViewmodel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RequestScreen(
    args: Routes.Request,
    groupViewmodel: GroupViewmodel,
    navController: NavHostController
) {


    var grpDetails by remember {
        mutableStateOf(Routes.GrpDetails())
    }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        groupViewmodel.emptyUser()
        groupViewmodel.loadGroupInfo(grpId = args.grpId) {
            it?.let {
                grpDetails = it
            }
        }
    }

    var userDetails by remember { mutableStateOf(Routes.UserModel()) }
    LaunchedEffect(Unit) {
        groupViewmodel.fetchUserDetailsByUid(uid = args.uid) {
            it?.let {
                userDetails = it
            }
        }
    }

    var others by remember { mutableStateOf("") }


    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(
                        imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                        contentDescription = null,
                        Modifier
                            .size(40.dp)
                            .clickable { navController.navigate(Routes.Main) }
                    )
                    TextDesign(text = "Send join Request", size = 18)
                }

                Image(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    Modifier.size(25.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.weight(5f),
                    contentAlignment = Alignment.Center
                ) {

                    AsyncImage(
                        model = userDetails.photoUrl,
                        contentDescription = "group photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .clip(RoundedCornerShape(60.dp))
                            .size(90.dp),
                        error = painterResource(id = R.drawable.logo_primary)

                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row {
                        Image(
                            painter = painterResource(id = R.drawable.arrow),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Image(
                            painter = painterResource(id = R.drawable.arrow),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    TextDesign(text = "Join", size = 12)
                }

                Box(
                    modifier = Modifier.weight(5f),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = grpDetails.photoUrl,
                        contentDescription = "group photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .clip(RoundedCornerShape(60.dp))
                            .size(90.dp),
                        error = painterResource(id = R.drawable.logo_primary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Column(
                modifier = Modifier.padding(start = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xAA99F0F0)),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (args.grpId.isEmpty()) {
                        Text(
                            text = "Group Id",
                            fontFamily = roboto,
                            fontSize = 18.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(start = 15.dp)
                        )
                    }
                    BasicTextField(
                        value = args.grpId, onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .padding(15.dp)
                            .height(50.dp)
                            .fillMaxWidth(), textStyle = TextStyle(
                            fontFamily = roboto, fontSize = 18.sp, color = Color.Black
                        ), maxLines = 1
                    )
                }

                // read only s
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .height(50.dp)
                        .background(Color(0xAA99F0F0)),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (grpDetails.grpName.isEmpty()) {
                        Text(
                            text = "Group name",
                            fontFamily = roboto,
                            fontSize = 18.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(start = 15.dp)
                        )
                    }
                    BasicTextField(
                        value = grpDetails.grpName,
                        onValueChange = {}, readOnly = true,
                        modifier = Modifier
                            .padding(15.dp)
                            .fillMaxWidth(), textStyle = TextStyle(
                            fontFamily = roboto, fontSize = 18.sp, color = Color.Black
                        ), maxLines = 1
                    )

                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xAA99F0F0)),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (grpDetails.description.isEmpty()) {
                        Text(
                            text = "Description",
                            fontFamily = roboto,
                            fontSize = 18.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(start = 15.dp)
                        )
                    }
                    BasicTextField(
                        value = grpDetails.description, onValueChange = { },
                        modifier = Modifier
                            .padding(15.dp)
                            .fillMaxWidth(), readOnly = true,
                        textStyle = TextStyle(
                            fontFamily = roboto, fontSize = 18.sp, color = Color.Black
                        ), maxLines = 1
                    )
                }

                // user info

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xAA99F0F0)),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (userDetails.username.isEmpty()) {
                        Text(
                            text = "Username",
                            fontFamily = roboto,
                            fontSize = 18.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(start = 15.dp)
                        )
                    }
                    BasicTextField(
                        value = userDetails.username, onValueChange = { },
                        modifier = Modifier
                            .padding(15.dp)
                            .fillMaxWidth(), readOnly = true,
                        textStyle = TextStyle(
                            fontFamily = roboto, fontSize = 18.sp, color = Color.Black
                        ), maxLines = 1
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xAA99F0F0)),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (userDetails.email.isEmpty()) {
                        Text(
                            text = "Email",
                            fontFamily = roboto,
                            fontSize = 18.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(start = 15.dp)
                        )
                    }
                    BasicTextField(
                        value = userDetails.email, onValueChange = { },
                        modifier = Modifier
                            .padding(15.dp)
                            .fillMaxWidth(), readOnly = true,
                        textStyle = TextStyle(
                            fontFamily = roboto, fontSize = 18.sp, color = Color.Black
                        ), maxLines = 1
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xAA99F0F0)),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "General Member",
                        fontFamily = roboto,
                        fontSize = 18.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(start = 15.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xAA99F0F0)),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (others.isEmpty()) {
                        Text(
                            text = "Others",
                            fontFamily = roboto,
                            fontSize = 18.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(start = 15.dp)
                        )
                    }
                    BasicTextField(
                        value = others, onValueChange = { others = it },
                        modifier = Modifier
                            .padding(15.dp)
                            .fillMaxWidth(),
                        textStyle = TextStyle(
                            fontFamily = roboto, fontSize = 18.sp, color = Color.Black
                        ), maxLines = 3,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {

                        })
                    )
                }
            }


            OutlinedButton(onClick = {
                // send request navigate to main page
                groupViewmodel.sendJoinRequest(
                    uid = args.uid,
                    grpId = args.grpId,
                    email = userDetails.email,
                    username = userDetails.username
                ) {
                    if (it) {
                        navController.navigate(Routes.Main)
                    } else {
                        launchToast(context = context, "Error occured retry")
                    }
                }
            }, shape = RoundedCornerShape(6.dp)) {
                TextDesign(text = "Submit")
            }
        }

    }
}