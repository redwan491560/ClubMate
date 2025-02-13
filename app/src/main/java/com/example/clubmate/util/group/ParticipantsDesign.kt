package com.example.clubmate.util.group

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.clubmate.R
import com.example.clubmate.ui.theme.Composables.Companion.TextDesign
import com.example.clubmate.util.Category
import com.example.clubmate.viewmodel.UserJoinDetails

@Composable
fun MemberDesign(user: UserJoinDetails, joinDate: String, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.clickable {
            onClick()
        }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 10.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {

                AsyncImage(
                    model = user.photoUrl,
                    contentDescription = "group photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(70.dp)
                        .clip(RoundedCornerShape(40.dp)),
                    error = painterResource(id = R.drawable.logo_primary)
                )
            }
            Column(
                modifier = Modifier.padding(horizontal = 15.dp, vertical = 8.dp)
            ) {
                TextDesign(text = user.email, size = 20)
                TextDesign(text = "name: " + user.username, size = 18)
                TextDesign(text = "phone: " + user.phone, size = 14)
                TextDesign(text = "type: " + user.userType, size = 20)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(5.dp))
                        TextDesign(text = "join: $joinDate", size = 14)
                    }

                }

            }
        }

    }

}
