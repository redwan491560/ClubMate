package com.example.clubmate.util.group

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.clubmate.R
import com.example.clubmate.ui.theme.Composables.Companion.TextDesign
import com.example.clubmate.ui.theme.roboto
import com.example.clubmate.viewmodel.UserJoinDetails

@Composable
fun MemberDesign(user: UserJoinDetails, joinDate: String, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.clickable {
            onClick()
        }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 10.dp)
        ) {

            Column(
                modifier = Modifier
                    .weight(8f)
                    .padding(horizontal = 15.dp, vertical = 8.dp)
            ) {
                Text(
                    text = user.email,
                    fontSize = 18.sp,
                    color = Color.Black,
                    fontFamily = roboto, maxLines = 1,
                    modifier = Modifier, overflow = TextOverflow.Ellipsis
                )
                TextDesign(text = "name: " + user.username, size = 14)
                TextDesign(text = "phone: " + user.phone, size = 12)
                TextDesign(text = "type: " + user.userType, size = 16)
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(5.dp))
                        TextDesign(text = "join: $joinDate", size = 14)
                    }

                }

            }

            AsyncImage(
                model = user.photoUrl,
                contentDescription = "group photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .clip(RoundedCornerShape(40.dp))
                    .size(100.dp)
                    .weight(3f)
                    .padding(end = 15.dp),
                error = painterResource(id = R.drawable.logo_primary)
            )
        }

    }

}
