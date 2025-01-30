package com.example.clubmate.screens

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.clubmate.R
import com.example.clubmate.db.Routes
import com.example.clubmate.ui.theme.Composables.Companion.TextDesignClickable
import com.example.clubmate.ui.theme.roboto
import com.example.clubmate.viewmodel.GroupViewmodel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun GroupScreen(
    userId: String,
    grpId: String,
    navController: NavHostController,
    grpViewmodel: GroupViewmodel
) {

    var grpDetails by remember { mutableStateOf(Routes.GrpDetails()) }

    var text by remember { mutableStateOf("") }

    val listState = rememberLazyListState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        // load group details
        grpViewmodel.loadGroupInfo(grpId) { details ->
            details?.let {
                grpDetails = details
            }
        }
    }


    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val openGalleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: android.net.Uri? ->
            uri?.let {
                // Set the selected image URI
                selectedImageUri = it
            }
        }

    val requestPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // If permission is granted, open the gallery
                openGalleryLauncher.launch("image/*")
            } else {
                Toast.makeText(
                    context, "Permission denied to read your External storage", Toast.LENGTH_SHORT
                ).show()
            }
        }

    // Check permission status for READ_EXTERNAL_STORAGE
    val permissionStatus = ContextCompat.checkSelfPermission(
        LocalContext.current, READ_EXTERNAL_STORAGE
    )


    Scaffold(modifier = Modifier
        .systemBarsPadding()
        .padding(horizontal = 7.dp, vertical = 5.dp),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp, 10.dp, 0.dp, 0.dp))
                    .background(Color(0xFFF3E5E5))
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .weight(8f)
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = grpDetails.grpName,
                            fontSize = 18.sp,
                            fontFamily = roboto,
                        )

                        TextDesignClickable(text = "View Profile", size = 14) {
                            // view group details
                            navController.navigate(
                                Routes.GrpDetails(
                                    grpId = grpDetails.grpId,
                                    grpName = grpDetails.grpName,
                                    description = grpDetails.description,
                                    createdBy = grpDetails.createdBy,
                                    createdAt = grpDetails.createdAt
                                )
                            )
                        }

                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(painter = painterResource(id = R.drawable.call_icon),
                        contentDescription = null,
                        modifier = Modifier
                            .size(25.dp)
                            .clickable {

                            })
                    Image(painter = painterResource(id = R.drawable.video_call),
                        contentDescription = null,
                        modifier = Modifier
                            .size(35.dp)
                            .clickable {

                            })
                    Image(painter = painterResource(id = R.drawable.incognito),
                        contentDescription = null,
                        modifier = Modifier
                            .size(30.dp)
                            .clickable {

                            })
                }
            }

        },
        bottomBar = {
            Column(
                modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start
            ) {
                selectedImageUri?.let { uri ->
                    Image(
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = "Selected Image",
                        modifier = Modifier
                            .padding(bottom = 3.dp)
                            .clip(RoundedCornerShape(8.dp, 8.dp, 0.dp, 0.dp))
                            .width(120.dp)
                    )
                }
                OutlinedTextField(value = text,
                    onValueChange = { text = it },
                    shape = RoundedCornerShape(0.dp, 0.dp, 6.dp, 6.dp),
                    maxLines = 3,
                    leadingIcon = {
                        Image(painter = painterResource(id = R.drawable.attachment_24px),
                            contentDescription = null,
                            modifier = Modifier
                                .size(30.dp)
                                .clickable {
                                    // Check permission status here
                                    if (permissionStatus == PermissionChecker.PERMISSION_GRANTED) {
                                        // If permission is granted, launch the gallery
                                        openGalleryLauncher.launch("image/*")
                                    } else {
                                        // Request permission if not granted
                                        requestPermissionLauncher.launch(READ_EXTERNAL_STORAGE)
                                    }
                                })
                    },
                    trailingIcon = {
                        Image(painter = painterResource(id = R.drawable.send_24px),
                            contentDescription = null,
                            modifier = Modifier
                                .size(30.dp)
                                .clickable {

                                    val sentMessage = text.trim()
                                    if (sentMessage.isNotEmpty()) {

                                        text = ""
                                    } else {
                                        launchToast(context, "Message cannot be empty")
                                    }

                                })
                    },
                    textStyle = TextStyle(fontFamily = roboto, fontSize = 18.sp),
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "Enter message",
                            fontFamily = roboto,
                            fontSize = 18.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                )

            }

        }) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 60.dp, top = 70.dp)
        ) {

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (userId.isNotEmpty()) {


                }
            }
        }
    }
}


