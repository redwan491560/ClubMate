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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.clubmate.R
import com.example.clubmate.db.Routes
import com.example.clubmate.screens.launchToast
import com.example.clubmate.ui.theme.Composables.Companion.TextDesign
import com.example.clubmate.ui.theme.ItemDesignAlertGroup
import com.example.clubmate.ui.theme.roboto
import com.example.clubmate.util.Category
import com.example.clubmate.viewmodel.GroupViewmodel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AddParticipants(
    grpInfo: Routes.AddUserToGroup, grpViewmodel: GroupViewmodel
) {

    val context = LocalContext.current
    // returns the searched user
    val user = grpViewmodel.user
    var query by remember { mutableStateOf("") }

    var grpDetails by remember {
        mutableStateOf(Routes.GrpDetails())
    }

    LaunchedEffect(Unit) {
        grpViewmodel.emptyUser()
        grpViewmodel.loadGroupInfo(grpId = grpInfo.grpId) {
            it?.let {
                grpDetails = it
            }
        }
    }

    // add user to the group


    // of someones uid does not match than add the user

    Scaffold(
        modifier = Modifier
            .systemBarsPadding()
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
            ) {


                AsyncImage(
                    model = grpDetails.photoUrl,
                    contentDescription = "group photo",
                    contentScale = ContentScale.Inside,
                    error = painterResource(id = R.drawable.logo_primary),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .size(80.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            TextDesign(text = grpDetails.grpName, size = 17)
            TextDesign(text = grpDetails.description, size = 17)


            Spacer(modifier = Modifier.height(60.dp))
            TextDesign(text = "Add Members", size = 17)
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xD5C0D0F7)), contentAlignment = Alignment.CenterStart
            ) {
                if (query.isEmpty()) {
                    Text(
                        text = "Add member by email",
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
                    ), maxLines = 1, keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ), keyboardActions = KeyboardActions(onDone = {
                        grpViewmodel.findUser(query)
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
                            grpViewmodel.findUser(query)
                        })
            }

            Column(
                modifier = Modifier.height(80.dp)
            ) {
                ItemDesignAlertGroup(userState = grpViewmodel.userState)
            }
            Row(
                Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = {
                        if (query.isNotEmpty()) {
                            user?.email?.let {
                                grpViewmodel.addParticipants(
                                    grpId = grpInfo.grpId,
                                    email = user.email,
                                    category = Category.General
                                )
                                launchToast(context = context, "participant added successfully")
                                query = ""
                                grpViewmodel.emptyUser()
                            } ?: run {
                                launchToast(context = context, "participant not found")

                            }
                        } else {
                            launchToast(context = context, "query is empty")
                        }


                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(Color(0xFFBBF7B1))
                ) {
                    TextDesign(text = "Add participant")
                }
            }
        }
    }
}