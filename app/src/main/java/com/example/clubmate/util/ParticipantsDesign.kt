package com.example.clubmate.util

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.clubmate.ui.theme.Composables.Companion.TextDesign
import com.example.clubmate.viewmodel.Category
import com.example.clubmate.viewmodel.UserJoinDetails

@Composable
fun MemberDesign(user: UserJoinDetails, joinDate: String, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.clickable {
            onClick()
        }
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 8.dp)
        ) {
            TextDesign(text = user.email, size = 19)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Spacer(modifier = Modifier.height(5.dp))
                    TextDesign(text = "name: " + user.username, size = 15)
                    TextDesign(text = "type: " + user.userType, size = 15)
                }
                Column {
                    TextDesign(text = "join: $joinDate", size = 14)
                }
            }

        }
    }

}

@Preview(showSystemUi = true)
@Composable
private fun JNDJFd() {
    MemberDesign(
        user = UserJoinDetails(
            email = "ehfbhfrf",
            userType = Category.General,
            joinData = 121212122,
            uid = "dnjfdfd",
            phone = "01774",
            username = "redwan"
        ), joinDate = "121212122"
    ){

    }
}