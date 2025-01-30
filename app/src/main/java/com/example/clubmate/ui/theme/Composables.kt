package com.example.clubmate.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clubmate.R
import com.example.clubmate.db.UserState
import com.example.clubmate.ui.theme.Composables.Companion.TextDesign
import java.util.Locale

class Composables {
    companion object {
        @Composable
        fun TextDesign(
            text: String,
            modifier: Modifier = Modifier,
            size: Int = 16,
            color: Color = Color.Black,
        ) {
            Text(
                text = text,
                fontSize = size.sp,
                color = color,
                fontFamily = roboto,
                modifier = modifier
            )
        }

        @Composable
        fun TextDesignClickable(
            text: String, size: Int = 16, color: Color = Color.Black, onClick: () -> Unit
        ) {
            Text(text = text,
                fontSize = size.sp,
                color = color,
                fontFamily = roboto,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable {
                    onClick()
                })
        }

        @Composable
        fun NavigationBarIcon(
            title: String,
            image: Painter? = null,
            icon: ImageVector? = null,
            color: Color = Color.White,
            onClick: () -> Unit
        ) {
            Card(shape = RoundedCornerShape(4.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                modifier = Modifier.clickable {
                    onClick()
                }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically

                ) {
                    if (icon == null) {
                        Image(
                            painter = image!!,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(10.dp, 10.dp)
                                .size(20.dp)
                        )
                    } else {
                        Image(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(10.dp, 10.dp)
                                .size(20.dp)
                        )
                    }
                    Text(
                        text = title, fontSize = 14.sp, modifier = Modifier.padding(5.dp)
                    )
                }
            }
        }


        @Composable
        fun AccountsCard(
            title: String,
            icon: Painter,
            description: String,
            onClick: () -> Unit,
        ) {
            Card(
                shape = RoundedCornerShape(8.dp), elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                        Image(
                            painter = icon,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(10.dp, 10.dp)
                                .size(30.dp)
                        )
                        Column {
                            Text(
                                text = title, fontSize = 16.sp, fontFamily = roboto
                            )
                            Text(
                                text = description, fontSize = 14.sp, fontFamily = roboto
                            )
                        }
                    }
                    Image(painter = painterResource(id = R.drawable.edit),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .size(20.dp)
                            .clickable {
                                onClick()
                            })
                }
            }
        }

        @Composable
        fun DevOpsCard(
            name: String,
            email: String,
            post: String,
            image: Painter?,
            modifier: Modifier = Modifier
        ) {
            Box(
                contentAlignment = Alignment.TopCenter,
                modifier = Modifier.padding(horizontal = 50.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 30.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 50.dp, bottom = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TextDesign(text = name, size = 20)
                        TextDesign(
                            text = email, size = 16, color = Color.Gray
                        )
                        TextDesign(text = post, size = 16)
                    }
                }
                Image(
                    painter = image!!, contentDescription = null, modifier = modifier.size(80.dp)
                )
                Text(
                    text = "Visit",
                    fontFamily = roboto,
                    fontSize = 14.sp,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .align(
                            Alignment.TopEnd
                        )
                        .padding(top = 33.dp, end = 8.dp)
                )
            }
        }
    }
}

@Composable
fun ItemDesignAlert(userState: UserState) {

    when (userState) {
        UserState.Loading -> {
            Row(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                LinearProgressIndicator(strokeCap = ProgressIndicatorDefaults.LinearStrokeCap)
            }
        }

        is UserState.Success -> {
            val user = userState.user
            user?.let {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(0.5.dp, Color(0xFF493101)),
                        colors = CardDefaults.cardColors(Color(0xFFF5E3C1))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp, 5.dp)
                        ) {
                            TextDesign(
                                text = "user:  " + it.username.replaceFirstChar {
                                    if (it.isLowerCase()) it.titlecase(
                                        Locale.ROOT
                                    ) else it.toString()
                                }, size = 18
                            )
                            Text(
                                text = it.email,
                                fontSize = 15.sp,
                                fontFamily = roboto,
                                textDecoration = TextDecoration.Underline
                            )
                        }
                    }

                }
            }

        }

        is UserState.Error -> {
            Row(modifier = Modifier.padding(top = 8.dp, start = 8.dp)) {
                TextDesign(text = userState.msg)
            }
        }
    }
}