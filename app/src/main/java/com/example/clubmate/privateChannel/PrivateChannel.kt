package com.example.clubmate.privateChannel

import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.waterfall
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clubmate.R
import com.example.clubmate.ui.theme.roboto

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun PrivateChannel(channelId: String) {

    var text by remember {
        mutableStateOf("")
    }

    // make all the messages seen by launched effect

    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val openGalleryLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: android.net.Uri? ->
            uri?.let {
                selectedImageUri = it
            }
        }


    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2A3150))
            .systemBarsPadding()
            .windowInsetsPadding(WindowInsets.ime),
        topBar = {
            Row {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Messages will be deleted upon seen by reciever",
                        color = Color.White,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Text(text = channelId, color = Color.White, fontSize = 18.sp)
                }
                Row {
                    Image(
                        painter = painterResource(id = R.drawable.baseline_content_copy_24),
                        contentDescription = null, modifier = Modifier.size(25.dp)
                    )
                    Image(
                        painter = painterResource(id = R.drawable.delete_msg),
                        contentDescription = null, modifier = Modifier.size(25.dp)
                    )
                }
            }

        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    shape = RoundedCornerShape(10.dp),
                    maxLines = 3,
                    leadingIcon = {
                        Image(colorFilter = ColorFilter.tint(Color.White),
                            painter = painterResource(id = R.drawable.attachment_24px),
                            contentDescription = null,
                            modifier = Modifier
                                .size(30.dp)
                                .clickable {
                                    openGalleryLauncher.launch("image/*")
                                }
                        )
                    },
                    trailingIcon = {
                        Image(colorFilter = ColorFilter.tint(Color.White),
                            painter = painterResource(id = R.drawable.send_24px),
                            contentDescription = null,
                            modifier = Modifier
                                .size(30.dp)
                                .clickable {

                                }
                        )
                    },
                    textStyle = TextStyle(
                        fontFamily = roboto,
                        color = Color.White,
                        fontSize = 18.sp
                    ),
                    modifier = Modifier
                        .fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "Enter message",
                            fontFamily = roboto,
                            fontSize = 18.sp,
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedPlaceholderColor = Color.Transparent,
                        unfocusedPlaceholderColor = Color.Transparent,
                        focusedIndicatorColor = Color.White,
                        unfocusedIndicatorColor = Color.White,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = "end to end encrypted secured channel",
                    color = Color.White,
                    fontSize = 11.sp
                )
            }
        }
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF2A3150))
        ) {














        }

    }
}




