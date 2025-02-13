package com.example.clubmate.navbarScreens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.clubmate.R
import com.example.clubmate.viewmodel.AuthViewModel


@Composable
fun SettingsScreen(authViewmodel: AuthViewModel, navController: NavController) {

    Box(
        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.underdev),
                contentDescription = null,
                modifier =Modifier.size(150.dp)
            )
            Text(text = "Under construction")
        }

    }
}