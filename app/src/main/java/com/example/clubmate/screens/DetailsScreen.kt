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
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.clubmate.R
import com.example.clubmate.db.Routes
import com.example.clubmate.ui.theme.Composables.Companion.TextDesign
import com.example.clubmate.ui.theme.roboto
import com.example.clubmate.viewmodel.ChatViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun UserDetailsScreen(
    userDetails: Routes.UserDetails, navController: NavHostController, chatViewmodel: ChatViewModel
) {


    val clipboardManager = LocalClipboardManager.current

    val context = LocalContext.current


    Scaffold(topBar = {
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
            TextDesign(text = "User details", size = 18)
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
                TextDesign(text = "Block Chat")
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TextButton(colors = ButtonDefaults.buttonColors(Color(0xFFF3DBDB)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.width(160.dp),
                    onClick = {

                    }) {
                    TextDesign(text = "Report Chat")
                }

                TextButton(
                    onClick = {

                    },
                    colors = ButtonDefaults.buttonColors(Color.Red),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.width(160.dp),
                ) {
                    TextDesign(text = "Delete Chat")
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
                TextDesign(text = userDetails.username, size = 18)
                TextDesign(text = userDetails.email, size = 15)
            }
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
            ) {
                ButtonDesignDetailsScreen(
                    src = painterResource(id = R.drawable.call_icon), title = "Audio"
                ) {

                }
                Spacer(modifier = Modifier.width(40.dp))
                ButtonDesignDetailsScreen(
                    src = painterResource(id = R.drawable.video_call), title = "Video"
                ) {

                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DetailsIconDesign("username", userDetails.username) {
                    launchToast(context = context, text = "Username copied")
                    clipboardManager.setText(AnnotatedString(userDetails.username))
                }
                DetailsIconDesign("email", userDetails.email) {
                    launchToast(context = context, text = "User email copied")
                    clipboardManager.setText(AnnotatedString(userDetails.email))
                }
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
            }
        }

    }
}


@Preview(showSystemUi = true)
@Composable
private fun DKsd() {
    UserDetailsScreen(
        userDetails = Routes.UserDetails(
            username = "Redwan", email = "redwan@gmail.com"
        ), navController = rememberNavController(), chatViewmodel = ChatViewModel()
    )
}


@Composable
fun IconDesignDetailScreen(
    src: Painter,
    title: String,
    isCopied: Boolean = false,
    onClick: () -> Unit,
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextDesign(text = title, size = 16)
        Image(painter = src,
            contentDescription = null,
            modifier = Modifier
                .padding(end = 5.dp)
                .size(25.dp)
                .clickable {
                    onClick()
                }
        )
    }
}

@Composable
fun ButtonDesignDetailsScreen(src: Painter, title: String, onClick: () -> Unit) {

    Card(
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier
            .width(80.dp)
            .clickable {
                onClick()
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = src,
                contentDescription = null,
                modifier = Modifier
                    .size(25.dp)
            )
            TextDesign(text = title, size = 14)
        }
    }

}


@Composable
fun DetailsIconDesign(tag: String, value: String, isCopied: Boolean = false, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = tag,
                fontSize = 16.sp,
                color = Color.Black,
                fontFamily = roboto,
                modifier = Modifier.width(100.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value,
                    fontSize = 16.sp,
                    color = Color.Black,
                    modifier = Modifier.width(260.dp),
                    fontFamily = roboto,
                )
                if (isCopied) {
                    Image(
                        painter = painterResource(id = R.drawable.baseline_content_copy_24),
                        contentDescription = null,
                        modifier = Modifier
                            .size(25.dp)
                            .padding(end = 6.dp)
                            .clickable {
                                onClick()
                            }
                    )
                }

            }

        }
    }

}