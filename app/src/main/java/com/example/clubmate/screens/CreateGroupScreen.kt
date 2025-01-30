package com.example.clubmate.screens

import android.annotation.SuppressLint
import android.widget.Toast
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.clubmate.R
import com.example.clubmate.db.Routes
import com.example.clubmate.ui.theme.Composables.Companion.TextDesign
import com.example.clubmate.ui.theme.roboto
import com.example.clubmate.viewmodel.GroupStatus
import com.example.clubmate.viewmodel.GroupViewmodel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun CreateGroupScreen(
    user: Routes.CreateGroup,
    grpViewModel: GroupViewmodel,
    navController: NavHostController
) {


    var grpId by remember {
        mutableStateOf("")
    }
    var grpname by remember {
        mutableStateOf("")
    }
    var grpDescription by remember {
        mutableStateOf("")
    }

    var admin by remember {
        mutableStateOf(Routes.UserModel())
    }


    var grpCreated = Routes.GrpDetails()

    LaunchedEffect(Unit) {
        admin = Routes.UserModel(
            uid = user.uid, username = user.username, email = user.email
        )
    }


    val context = LocalContext.current

    Scaffold(
        modifier = Modifier
            .systemBarsPadding()
            .fillMaxWidth()
            .padding(top = 20.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.Start
        ) {


            TextDesign(text = "Create group", size = 18)
            Spacer(modifier = Modifier.height(30.dp))
            Card(
                colors = CardDefaults.cardColors(Color(0xD5C0D0F7)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = grpId.ifEmpty { "Request group ID" },
                        fontSize = 16.sp,
                        fontFamily = roboto,
                        modifier = Modifier.padding(start = 15.dp)
                    )

                    Image(painter = painterResource(id = R.drawable.generate),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(end = 15.dp)
                            .size(22.dp)
                            .rotate(270f)
                            .clickable {
                                grpViewModel.requestId {
                                    grpId = it
                                }
                            }
                    )
                }
            }


            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .height(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xD5C0D0F7)),
                contentAlignment = Alignment.CenterStart
            ) {
                if (grpname.isEmpty()) {
                    Text(
                        text = "Group name",
                        fontFamily = roboto,
                        fontSize = 14.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(start = 15.dp)
                    )
                }
                BasicTextField(
                    value = grpname, onValueChange = {
                        grpname = it
                    }, modifier = Modifier
                        .padding(15.dp)
                        .fillMaxWidth(), textStyle = TextStyle(
                        fontFamily = roboto, fontSize = 16.sp, color = Color.Black
                    ), maxLines = 1, keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    )
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .height(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xD5C0D0F7)),
                contentAlignment = Alignment.CenterStart
            ) {
                if (grpDescription.isEmpty()) {
                    Text(
                        text = "Group description",
                        fontFamily = roboto,
                        fontSize = 14.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(start = 15.dp)
                    )
                }
                BasicTextField(
                    value = grpDescription, onValueChange = {
                        grpDescription = it
                    }, modifier = Modifier
                        .padding(15.dp)
                        .fillMaxWidth(), textStyle = TextStyle(
                        fontFamily = roboto, fontSize = 16.sp, color = Color.Black
                    ), maxLines = 1, keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    )
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                TextButton(colors = ButtonDefaults.buttonColors(Color(0xFFF5C0C0)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.width(160.dp),
                    onClick = {
                        if (grpname.isEmpty() || grpDescription.isEmpty()) {
                            Toast.makeText(
                                context,
                                "Group name and description cannot be empty",

                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            grpViewModel.createGroup(
                                grpId = grpId,
                                createdBy = admin.uid,
                                grpName = grpname,
                                description = grpDescription
                            ) { details ->
                                details?.let {
                                    grpCreated = details

                                    when (grpViewModel.grpStatus.value) {
                                        GroupStatus.Success -> {
                                            navController.navigate(
                                                Routes.GroupModel(
                                                    user = admin.uid,
                                                    grpId = grpCreated.grpId
                                                )
                                            )
                                        }

                                        GroupStatus.Loading -> {

                                        }
                                        GroupStatus.Failed -> {

                                        }
                                        null -> {


                                        }
                                    }
                                }


                            }
                        }
                    }) {

                    TextDesign(text = "Create Group")
                }
            }
        }
    }
}

