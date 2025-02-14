package com.example.clubmate.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.clubmate.R
import com.example.clubmate.db.Routes
import com.example.clubmate.ui.theme.Composables.Companion.TextDesign
import com.example.clubmate.viewmodel.GroupViewmodel


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun GroupDetailsScreen(
    uid: String,
    grpDetails: Routes.GrpDetails,
    navController: NavHostController,
    grpViewModel: GroupViewmodel
) {


    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var currentUserIsAdmin by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        grpViewModel.checkAdmin(grpId = grpDetails.grpId, userId = uid) {
            currentUserIsAdmin = it
        }
    }


    var admin by remember { mutableStateOf(Routes.UserModel()) }

    LaunchedEffect(Unit) {
        grpViewModel.fetchUserDetailsByUid(grpDetails.createdBy) { userInfo ->
            if (userInfo != null) {
                admin = userInfo
            }
        }
    }


    Scaffold(

        topBar = {
            Row(
                modifier = Modifier
                    .systemBarsPadding()
                    .padding(start = 15.dp, top = 15.dp, bottom = 8.dp)
            ) {
                Image(imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.clickable {
                        navController.popBackStack()
                    })
                Spacer(modifier = Modifier.width(25.dp))
                TextDesign(text = "Group details", size = 18)
            }
        }, bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 30.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TextButton(colors = ButtonDefaults.buttonColors(Color(0xFFF5C0C0)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.width(160.dp),
                    onClick = {

                    }) {
                    TextDesign(text = "Block Group")
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(colors = ButtonDefaults.buttonColors(Color(0xFFF3DBDB)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.width(160.dp),
                        onClick = {

                        }) {
                        TextDesign(text = "Report Group")
                    }

                    TextButton(
                        onClick = {
                            grpViewModel.leaveGroup(
                                uid = uid,
                                grpId = grpDetails.grpId
                            ) { value ->
                                if (value) navController.navigate(Routes.Main)
                                else launchToast(context, "Leave attempt unsuccessful")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(Color.Red),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.width(160.dp),
                    ) {
                        TextDesign(text = "Leave Group")
                    }

                }
            }

        }) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 60.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_primary),
                    contentDescription = null,
                    modifier = Modifier.size(80.dp)
                )
                TextDesign(text = grpDetails.grpName, size = 18)
                TextDesign(text = grpDetails.description, size = 14)
            }
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(25.dp)
                ) {

                    ButtonDesignDetailsScreen(
                        src = painterResource(id = R.drawable.noticeboard), title = "Notice"
                    ) {
                        navController.navigate(
                            Routes.Timeline(grpId = grpDetails.grpId, uid = uid)
                        )
                    }
                    ButtonDesignDetailsScreen(
                        src = painterResource(id = R.drawable.terminal), title = "Terminal"
                    ) {
                        if (currentUserIsAdmin) {
                            navController.navigate(
                                Routes.Console(
                                    description = grpDetails.description,
                                    grpId = grpDetails.grpId,
                                    grpName = grpDetails.grpName, uid = uid
                                )
                            )
                        } else {
                            launchToast(context, "Only admin can access this")
                        }
                    }
                }

            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                DetailsIconDesign("name", grpDetails.grpName) {}

                DetailsIconDesign("description", grpDetails.description) {}

                DetailsIconDesign(tag = "invite link", isCopied = true, value = grpDetails.grpId) {
                    launchToast(context = context, text = "invite link copied")
                    clipboardManager.setText(AnnotatedString(grpDetails.grpId))
                }

                DetailsIconDesign(tag = "created by", isCopied = true, value = admin.email) {
                    launchToast(context = context, text = "Copied")
                    clipboardManager.setText(AnnotatedString(admin.email))
                }
                DetailsIconDesign(
                    tag = "created at",
                    value = grpViewModel.convertTimestamp(grpDetails.createdAt)
                ) {}
            }

            Spacer(modifier = Modifier.height(60.dp))
            Column(
                modifier = Modifier.padding(horizontal = 15.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconDesignDetailScreen(
                    src = painterResource(id = R.drawable.baseline_favorite_border_24),
                    title = "Add to favourites"
                ) {

                }
                IconDesignDetailScreen(
                    src = painterResource(id = R.drawable.baseline_notifications_off_24),
                    title = "Turn off notification"
                ) {

                }
                IconDesignDetailScreen(
                    src = painterResource(id = R.drawable.archive), title = "Archive group "
                ) {

                }
            }
        }
    }
}

