package com.example.clubmate.screens.grp

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.clubmate.R
import com.example.clubmate.db.Routes
import com.example.clubmate.screens.launchToast
import com.example.clubmate.ui.theme.Composables.Companion.TextDesign
import com.example.clubmate.ui.theme.roboto
import com.example.clubmate.viewmodel.Category
import com.example.clubmate.viewmodel.GroupViewmodel
import com.example.clubmate.viewmodel.UserJoinDetails

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewGroupUserDetailScreen(
    info: Routes.GroupUserDetails, grpViewmodel: GroupViewmodel, navController: NavHostController
) {

    var currentUserIsAdmin by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    var selectedCategory by remember { mutableStateOf(Category.General) }
    val categoryList = listOf(
        Category.General,
        Category.Admin,
        Category.President,
        Category.VicePresident,
        Category.Treasurer
    )

    var alertState by remember { mutableStateOf(false) }

    var user by remember {
        mutableStateOf(UserJoinDetails())
    }
    var grpDetails by remember {
        mutableStateOf(Routes.GrpDetails())
    }

    LaunchedEffect(Unit) {
        grpViewmodel.getParticipantDetails(grpId = info.grpId, userId = info.userId) {
            it?.let { user = it }
        }
    }

    LaunchedEffect(Unit) {
        grpViewmodel.checkAdmin(grpId = info.grpId, userId = info.currentUserId) {
            currentUserIsAdmin = it
        }
    }

    LaunchedEffect(Unit) {
        grpViewmodel.loadGroupInfo(info.grpId) { details ->
            details?.let {
                grpDetails = details
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(start = 15.dp, end = 10.dp, top = 30.dp), horizontalAlignment = Alignment.Start
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 25.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Image(imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = null,
                    Modifier
                        .size(25.dp)
                        .clickable {
                            navController.popBackStack()
                        })
                TextDesign(text = "Participants details", size = 20)
            }
            TextDesign(
                text = if (currentUserIsAdmin) "Admin" else "General",
                modifier = Modifier.padding(end = 10.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            TextDesign(text = info.grpId, size = 14)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextDesign(text = grpDetails.grpName, size = 22)
                TextDesign(text = grpDetails.description, size = 17)
            }


        }
        Spacer(modifier = Modifier.height(20.dp))
        if (alertState) {
            AlertDialog(shape = RoundedCornerShape(15.dp), onDismissRequest = {
                alertState = false
            }, confirmButton = {

            }, title = {
                Box(
                    modifier = Modifier.height(250.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextDesign(text = "Change roles", size = 17)
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                TextDesign(text = user.email, size = 17)
                                TextDesign(
                                    text = "category: " + user.userType.toString(), size = 20
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))

                        ExposedDropdownMenuBox(
                            expanded = expanded, onExpandedChange = {
                                expanded = !expanded
                            }, modifier = Modifier.fillMaxWidth()
                        ) {
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                elevation = CardDefaults.cardElevation(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(15.dp, 10.dp)
                                        .menuAnchor(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = selectedCategory.toString(),
                                        fontSize = 18.sp,
                                        fontFamily = roboto,
                                        modifier = Modifier.weight(8f), maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Icon(
                                        imageVector = Icons.Outlined.ArrowDropDown,
                                        contentDescription = null
                                    )
                                }
                            }
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.padding(3.dp),
                                scrollState = rememberScrollState()
                            ) {
                                categoryList.forEachIndexed { index, cat ->
                                    DropdownMenuItem(text = {
                                        Text(
                                            text = cat.toString(),
                                            fontFamily = roboto,
                                            fontSize = 16.sp
                                        )
                                    },
                                        onClick = {
                                            selectedCategory = categoryList[index]
                                            expanded = false // Close dropdown after selection
                                        },
                                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                    )

                                }
                            }

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
                            onClick = { alertState = false },
                            colors = ButtonDefaults.textButtonColors(Color.Red),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.width(120.dp)
                        ) {
                            TextDesign(text = "Cancel")
                        }
                        TextButton(
                            border = BorderStroke(1.dp, Color.Blue), onClick = {
                                // change roles
                            }, shape = RoundedCornerShape(8.dp), modifier = Modifier.width(120.dp)
                        ) {
                            TextDesign(text = "Change")
                        }
                    }
                }
            })
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .systemBarsPadding(), shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(15.dp, 8.dp)) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextDesign(text = user.email, size = 19)
                    TextDesign(text = "name: " + user.username, size = 17)

                }
                Spacer(modifier = Modifier.height(5.dp))
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextDesign(text = "category: " + user.userType.toString(), size = 18)

                        Image(painter = painterResource(id = R.drawable.change_role),
                            contentDescription = null,
                            Modifier
                                .size(30.dp)
                                .clickable {
                                    if (currentUserIsAdmin) {
                                        alertState = true
                                    } else {
                                        launchToast(
                                            context = context, "Only admin can change role of  user"
                                        )
                                    }
                                })
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextDesign(text = "joined: " + grpViewmodel.convertTimestampToDate(user.joinData))
                        Image(painter = painterResource(id = R.drawable.delete_msg),
                            contentDescription = null,
                            Modifier
                                .size(30.dp)
                                .clickable {
                                    if (currentUserIsAdmin) {

                                    } else {
                                        launchToast(context = context, "Only admin remove user")
                                    }
                                })
                    }
                }
            }
        }
    }
}
