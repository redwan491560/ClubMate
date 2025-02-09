package com.example.clubmate.navbarScreens

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.clubmate.R
import com.example.clubmate.db.Routes
import com.example.clubmate.ui.theme.Composables.Companion.DevOpsCard
import com.example.clubmate.ui.theme.Composables.Companion.TextDesign


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun DevelopersScreen(navController: NavController) {


    Scaffold(modifier = Modifier
        .systemBarsPadding()
        .fillMaxSize(), topBar = {
        Row(
            modifier = Modifier.padding(15.dp, 20.dp)
        ) {
            Image(imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = null,
                modifier = Modifier.clickable { navController.navigate(Routes.Main) })
            Spacer(modifier = Modifier.width(25.dp))
            TextDesign(text = "Meet the Team", size = 18)
        }
    }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 60.dp)
        ) {


            Column(
                modifier = Modifier
                    .padding(10.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                // Text(text = "", fontFamily = roboto, fontSize = 20.sp)
                DevOpsCard(
                    name = "Redwan Hussain",
                    email = "redwan491560@gmail.com",
                    post = "Developer",
                    image = painterResource(id = R.drawable.redwan),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .scale(3.3f)
                )
                DevOpsCard(
                    name = "Mizanur Rahman",
                    email = "mizan21331@gmail.com",
                    post = "Developer",
                    image = painterResource(id = R.drawable.mizan),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .scale(1.1f)
                )
                DevOpsCard(
                    name = "Tonmoy Chanda",
                    email = "tonmoychanda07@gmail.com",
                    post = "Developer",
                    image = painterResource(id = R.drawable.tonmoy),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .scale(1.1f)
                )
                DevOpsCard(
                    name = "Abu Adnan Shad",
                    email = "adnanshad1035@gmail.com",
                    post = "Developer",
                    image = painterResource(id = R.drawable.adnan),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .scale(1.1f)
                )

            }
        }

    }

}

@Preview(showSystemUi = true)
@Composable
private fun Arra() {
    DevelopersScreen(rememberNavController())
}