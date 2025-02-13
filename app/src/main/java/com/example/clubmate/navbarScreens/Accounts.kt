package com.example.clubmate.navbarScreens

import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil3.compose.AsyncImage
import com.example.clubmate.R
import com.example.clubmate.db.Routes
import com.example.clubmate.screens.launchToast
import com.example.clubmate.ui.theme.Composables.Companion.AccountsCard
import com.example.clubmate.ui.theme.Composables.Companion.TextDesign
import com.example.clubmate.viewmodel.AuthViewModel


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AccountScreen(authViewmodel: AuthViewModel, navController: NavController) {


    val userData by authViewmodel.userData.collectAsState()

    LaunchedEffect(Unit) {
        authViewmodel.checkAuthenticationStatus()
    }

    val context = LocalContext.current
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


    Scaffold(modifier = Modifier
        .systemBarsPadding()
        .fillMaxSize(),
        topBar = {
            Row(
                modifier = Modifier.padding(15.dp, 20.dp)
            ) {
                Image(imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.clickable { navController.navigate(Routes.Main) })
                Spacer(modifier = Modifier.width(25.dp))
                TextDesign(text = "Account", size = 18)
            }
        }) {


        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(70.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {

                    AsyncImage(
                        model = userData?.photoUrl,
                        contentDescription = "group photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(80.dp)),
                        error = painterResource(id = R.drawable.logo_primary)
                    )

                }
                Image(
                    painter = painterResource(id = R.drawable.camera),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .size(20.dp)
                        .clickable {
                            openGalleryLauncher.launch("image/*")
                        }
                )
            }


            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 15.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    userData.run {
                        userData?.let { it1 -> TextDesign(text = it1.username, size = 22) }
                        userData?.let { it1 -> TextDesign(text = it1.email, size = 16) }
                    }
                }

            }
            Column(
                verticalArrangement = Arrangement.spacedBy(15.dp),
                modifier = Modifier.padding(horizontal = 10.dp)
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                userData?.username?.let { it1 ->
                    AccountsCard(
                        title = "Change Name",
                        icon = painterResource(id = R.drawable.name),
                        description = it1
                    ) {

                    }
                }
                userData?.email?.let { it1 ->
                    AccountsCard(
                        title = "Change email",
                        icon = painterResource(id = R.drawable.email),
                        description = it1
                    ) {

                    }
                }
                userData?.phone?.let { it1 ->
                    AccountsCard(
                        title = "Change phone",
                        icon = painterResource(id = R.drawable.phone),
                        description = it1

                    ) {

                    }
                }
                AccountsCard(
                    title = "Change password",
                    icon = painterResource(id = R.drawable.lock_logo),
                    description = "********"
                ) {

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
                        modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center,
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

                            Image(
                                colorFilter = ColorFilter.tint(Color.White),
                                painter = painterResource(id = R.drawable.delete_msg),
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(top = 5.dp, end = 15.dp)
                                    .size(40.dp)
                                    .clickable {
                                        selectedImageUri = null
                                    }
                            )

                            Image(
                                colorFilter = ColorFilter.tint(Color.White),
                                imageVector = Icons.Outlined.Done, contentDescription = null,
                                modifier = Modifier
                                    .padding(top = 5.dp, end = 5.dp, bottom = 5.dp)
                                    .size(40.dp)
                                    .clickable {
                                        selectedImageUri?.let {
                                            userData?.uid.let { uid ->
                                                isLoading = true
                                                authViewmodel.uploadImage(
                                                    uid!!,
                                                    selectedImageUri!!
                                                ) {
                                                    if (it) {
                                                        authViewmodel.checkAuthenticationStatus()
                                                        selectedImageUri = null
                                                        isLoading = false
                                                    }
                                                }
                                            }
                                        } ?: run {
                                            launchToast(
                                                context = context,
                                                "Error occured try again"
                                            )
                                        }

                                    }
                            )
                        }
                    }
                }

            }
        }
    }

}


