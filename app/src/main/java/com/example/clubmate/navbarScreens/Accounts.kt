package com.example.clubmate.navbarScreens

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.clubmate.R
import com.example.clubmate.db.Routes
import com.example.clubmate.ui.theme.Composables.Companion.AccountsCard
import com.example.clubmate.ui.theme.Composables.Companion.TextDesign
import com.example.clubmate.viewmodel.AuthViewModel


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AccountScreen(authViewmodel: AuthViewModel, navController: NavController) {


    val userData by authViewmodel.userData.collectAsState()

    LaunchedEffect(Unit) {
        authViewmodel.checkAuthenticationStatus()
    }

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
            TextDesign(text = "Account", size = 18)
        }
    }) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 80.dp, bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    userData.run {
                        userData?.let { it1 -> TextDesign(text = it1.username, size = 22) }
                        userData?.let { it1 -> TextDesign(text = it1.email, size = 22) }
                        userData?.let { it1 -> TextDesign(text = it1.uid, size = 14) }
                    }
                }

            }
            Column(
                verticalArrangement = Arrangement.spacedBy(15.dp),
                modifier = Modifier.padding(horizontal = 10.dp)
            ) {
                Spacer(modifier = Modifier.height(10.dp))
                userData?.username?.let { it1 ->
                    AccountsCard(
                        title = "Change Name",
                        icon = painterResource(id = R.drawable.name),
                        description = it1
                    ) {

                    }
                }
                userData?.email?.let { it1 ->
                    AccountsCard(
                        title = "Change email",
                        icon = painterResource(id = R.drawable.email),
                        description = it1
                    ) {

                    }
                }
                userData?.phone?.let { it1 ->
                    AccountsCard(
                        title = "Change phone",
                        icon = painterResource(id = R.drawable.phone),
                        description = it1

                    ) {

                    }
                }
                AccountsCard(
                    title = "Change password",
                    icon = painterResource(id = R.drawable.password),
                    description = "********"
                ) {

                }
            }
        }
    }
}


