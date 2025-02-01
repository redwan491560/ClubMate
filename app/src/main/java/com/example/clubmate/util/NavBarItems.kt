package com.example.clubmate.util

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clubmate.R
import com.example.clubmate.ui.theme.Composables.Companion.NavigationBarIcon
import com.example.clubmate.ui.theme.roboto

@Composable
fun NavBarItems(
    onAccount: () -> Unit,
    onSecurity: () -> Unit,
    onSettings: () -> Unit,
    onPersonalize: () -> Unit,
    onDevelopers: () -> Unit,
    onReportBug: () -> Unit,
    onSignOut: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(250.dp)
            .padding(5.dp, 10.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.padding(10.dp)
        ) {
            NavigationBarIcon(
                title = "Account",
                image = painterResource(id = R.drawable.account_circle_24px)
            ) {
                onAccount()
            }
            NavigationBarIcon(
                title = "Security", image = painterResource(id = R.drawable.security_24px)
            ) {
                onSecurity()
            }
            NavigationBarIcon(
                title = "Settings", image = painterResource(id = R.drawable.settings_24px)
            ) {
                onSettings()
            }
            NavigationBarIcon(
                title = "Personalize", image = painterResource(id = R.drawable.personalize)
            ) {
                onPersonalize()
            }
            NavigationBarIcon(
                title = "Developers", image = painterResource(id = R.drawable.developers)
            ) {
                onDevelopers()
            }
            NavigationBarIcon(
                title = "Report a bug", image = painterResource(id = R.drawable.report_bug)
            ) {
                onReportBug()
            }
            NavigationBarIcon(
                title = "Sign Out",
                image = painterResource(id = R.drawable.logout_24px),
                color = Color(0xFFF80808)
            ) {
                onSignOut()
            }
        }
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp, 8.dp)
                .align(Alignment.BottomCenter), shape = RoundedCornerShape(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.chat_bg),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .alpha(.24f)
                        .padding(6.dp)
                        .background(Color(0xFF2398AC))
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_primary),
                        contentDescription = null,
                        modifier = Modifier.size(55.dp)
                    )
                    Spacer(modifier = Modifier.width(15.dp))
                    Text(
                        text = "ClubMate",
                        fontSize = 22.sp,
                        color = Color(0xFF071CCA),
                        fontFamily = roboto
                    )
                }
            }
        }
    }
}