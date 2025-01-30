package com.example.clubmate.util

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clubmate.R
import com.example.clubmate.db.Message
import com.example.clubmate.screens.MessageStatus
import com.example.clubmate.ui.theme.roboto


@Composable
fun MessageItem(
    message: Message,
    isSent: Boolean,
    time: String = "",
    status: MessageStatus = MessageStatus.SENDING,
    onDeleteMessage: (Message) -> Unit
) {
    val src = if (status == MessageStatus.FAILED) R.drawable.failed
    else R.drawable.status

    var isSelected by remember {
        mutableStateOf(false)
    }


    val context = LocalContext.current

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
            if (isSent) {
                Text(
                    text = time, fontFamily = roboto, fontSize = 10.sp, modifier = Modifier.padding(
                        end = 5.dp,
                    )
                )
            }
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(bgColor)
                    .padding(vertical = 6.dp, horizontal = 4.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = {
                                if (isSent) isSelected = true
                            },
                            onTap = {
                                if (isSelected) isSelected = false
                            }
                        )
                    },
            ) {
                if (!isSent) {
                    Image(
                        painter = painterResource(src),
                        contentDescription = null,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(start = 5.dp),
                        colorFilter = ColorFilter.tint(checkFilterStatus(status))
                    )
                }

                Text(
                    text = message.messageText,
                    fontSize = 16.sp,
                    fontFamily = roboto,
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                        .widthIn(max = 280.dp),
                    color = Color.Black
                )
                if (isSent) {
                    Image(
                        painter = painterResource(id = src),
                        contentDescription = null,
                        modifier = Modifier
                            .size(18.dp)
                            .padding(end = 5.dp),
                        colorFilter = ColorFilter.tint(checkFilterStatus(status))
                    )
                }
            }
            if (!isSent) {
                Text(
                    text = time, fontFamily = roboto, fontSize = 10.sp,
                    modifier = Modifier.padding(
                        start = 5.dp,
                    )
                )
            }
            if (isSent) {
                AnimatedVisibility(isSelected) {
                    Icon(
                        painter = painterResource(id = R.drawable.delete_msg),
                        contentDescription = "Delete Item",
                        modifier = Modifier
                            .padding(bottom = 5.dp, start = 5.dp)
                            .size(30.dp)
                            .clickable {
                                onDeleteMessage(message)
                            })
                }
            }
        }
    }
}


fun checkFilterStatus(status: MessageStatus): Color {
    return when (status) {
        MessageStatus.SEEN -> {
            Color.Blue
        }

        MessageStatus.DELIVERED -> {
            Color.Black
        }

        MessageStatus.SENDING -> {
            Color(0xD3928888)
        }

        else -> {
            Color(0xBF6F4D4D)
        }
    }
}