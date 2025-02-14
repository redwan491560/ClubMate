package com.example.clubmate.util.group

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.example.clubmate.R
import com.example.clubmate.db.Routes
import com.example.clubmate.ui.theme.Composables.Companion.TextDesign
import com.example.clubmate.ui.theme.Composables.Companion.TextDesignClickable
import com.example.clubmate.viewmodel.GroupViewmodel

@Composable
fun TimelineScreen(
    args: Routes.Timeline,
    grpViewmodel: GroupViewmodel,
    navHostController: NavHostController
) {

}

@Composable
fun MeetingComponent(date: String, type: String, link: String, text: String) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Card(
            shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 10.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextDesign(text = "date", size = 13)
                        TextDesignClickable(text = "type", size = 16) {}
                    }

                    TextDesign(
                        text = "text",
                        modifier = Modifier
                            .padding(top = 10.dp, bottom = 5.dp)
                    )

                    TextDesignClickable(
                        text = "link"
                    ) {

                    }
                }
            }
        }
    }
}

@Composable
fun ReminderComponent() {


}

@Composable
fun EventComponent(imageRef: String, event: String, message: String, type: String, date: String) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Card(
            shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextDesign(text = "date", size = 13)
                    TextDesignClickable(text = "type", size = 16) {}
                }
                if (imageRef.isEmpty()) {
                    TextDesign(
                        text = "notice",
                        modifier = Modifier
                            .padding(top = 28.dp)
                            .align(Alignment.TopStart)
                    )
                } else {
                    Column {
                        TextDesign(
                            text = "message", modifier = Modifier.padding(top = 28.dp)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {

                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(40.dp)
                                    .align(Alignment.Center),
                                color = Color.Gray,
                                strokeWidth = 4.dp
                            )
                            AsyncImage(
                                model = imageRef,
                                contentDescription = "Sent Image",
                                contentScale = ContentScale.Inside,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp)),
                                error = painterResource(id = R.drawable.add_24px) // Error Image
                            )

                        }
                    }

                }
            }
        }
    }
}


@Composable
fun NoticeComponent(notice: String, date: String, type: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Card(
            shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextDesign(text = date, size = 13)
                    TextDesignClickable(text = type, size = 16) {}
                }
                TextDesign(
                    text = notice,
                    modifier = Modifier
                        .padding(top = 28.dp)
                        .align(Alignment.TopStart)
                )
            }

        }
    }
}