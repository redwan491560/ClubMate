package com.example.clubmate.navbarScreens

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.clubmate.viewmodel.AuthViewModel


@Composable
fun SecurityScreen(authViewmodel: AuthViewModel, navController: NavController) {

}

@Preview(showSystemUi = true)
@Composable
private fun Arra() {
    SecurityScreen(viewModel(), rememberNavController())
}

