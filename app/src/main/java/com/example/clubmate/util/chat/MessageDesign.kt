package com.example.clubmate.util.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.clubmate.R
import com.example.clubmate.screens.MessageStatus
import com.example.clubmate.ui.theme.roboto
import com.example.clubmate.viewmodel.IncognitoMessage


@Composable
fun MessageItem(
    message: Message,
    isSent: Boolean,
    time: String = "",
    onSelect: () -> Unit,
    status: MessageStatus = MessageStatus.SENDING,
    onDeleteMessage: (Message) -> Unit
) {


    var isSelected by remember {
        mutableStateOf(false)
    }


    val bgColor by remember {
        mutableStateOf(
            if (isSent) Color(0xFFD5F1C4) else Color(0xBFF1D4D4)
        )
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isSent) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Column(
                horizontalAlignment = if (isSent) Alignment.End else Alignment.Start
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(bgColor)
                        .padding(vertical = 6.dp, horizontal = 4.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    onSelect()
                                },
                                onLongPress = {
                                    if (isSent) isSelected = true
                                }, onTap = {
                                    if (isSelected) isSelected = false
                                })
                        },
                ) {
                    if (message.imageRef.isNotEmpty()) {
                        Box(
                            modifier = Modifier.width(300.dp), contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(40.dp)
                                    .align(Alignment.Center),
                                color = Color.Gray,
                                strokeWidth = 4.dp
                            )
                            AsyncImage(
                                model = message.imageRef,
                                contentDescription = "Sent Image",
                                contentScale = ContentScale.Inside,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp)),
                                error = painterResource(id = R.drawable.add_24px) // Error Image
                            )
                        }
                    } else {
                        Text(
                            text = message.messageText,
                            fontSize = 16.sp,
                            fontFamily = roboto,
                            modifier = Modifier
                                .padding(horizontal = 10.dp)
                                .widthIn(max = 280.dp),
                            color = Color.Black
                        )
                    }

                }
                Text(
                    text = time, fontFamily = roboto, fontSize = 12.sp, modifier = Modifier.padding(
                        end = if (isSent) 5.dp else 0.dp,
                    )
                )
            }
            if (isSent) {
                AnimatedVisibility(isSelected) {
                    Icon(painter = painterResource(id = R.drawable.delete_msg),
                        contentDescription = "Delete Item",
                        modifier = Modifier
                            .padding(bottom = 20.dp, start = 5.dp)
                            .size(30.dp)
                            .clickable {
                                onDeleteMessage(message)
                            })
                }
            }
        }

    }
}


@Composable
fun IncognitoMessageItem(
    message: IncognitoMessage,
    isSent: Boolean,
    time: String = "",
    status: MessageStatus = MessageStatus.SENDING
) {


    val bgColor by remember {
        mutableStateOf(
            if (isSent) Color.White else Color.Gray
        )
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isSent) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Column(
                horizontalAlignment = if (isSent) Alignment.End else Alignment.Start
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(bgColor)
                        .padding(vertical = 6.dp, horizontal = 4.dp)
                ) {

                    Text(
                        text = message.messageText,
                        fontSize = 16.sp,
                        fontFamily = roboto,
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .widthIn(max = 280.dp),
                        color = Color.Black
                    )
                }
                Text(
                    text = time, fontFamily = roboto, color = Color.White, fontSize = 12.sp,
                    modifier = Modifier.padding(
                        end = if (isSent) 5.dp else 0.dp,
                    )
                )
            }
        }
    }
}
