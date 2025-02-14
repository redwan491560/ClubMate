package com.example.clubmate.auth

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.clubmate.R
import com.example.clubmate.db.Routes
import com.example.clubmate.db.Status
import com.example.clubmate.ui.theme.roboto
import com.example.clubmate.viewmodel.AuthViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LoginScreen(authViewmodel: AuthViewModel, navController: NavHostController) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }


    val context = LocalContext.current

    val authState = authViewmodel.authState.observeAsState()
    LaunchedEffect(authState.value) {
        when (authState.value) {
            is Status.Error -> Toast.makeText(
                context, (authState.value as Status.Error).message, Toast.LENGTH_SHORT
            ).show()

            is Status.Authenticated -> navController.navigate(Routes.Main)

            else -> Unit
        }
    }


    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFD3F7D7))
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .systemBarsPadding()
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                Image(
                    painter = painterResource(id = R.drawable.logo_primary),
                    contentDescription = null,
                    modifier = Modifier.size(150.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.app_name),
                    contentDescription = null,
                    modifier = Modifier.height(40.dp)
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 25.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AuthTextFieldComponent(value = email, onLoginClick = {}) {
                    email = it
                }
                // do like this

                AuthTextFieldComponent(value = password, isPasswordField = true, onLoginClick = {
                    authViewmodel.logIn(email, password)
                }) {
                    password = it
                }

                Row(
                    modifier = Modifier
                        .width(300.dp)
                        .height(10.dp)
                        .padding(top = 5.dp)
                ) {
                    if (authState.value == Status.Loading) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }


                OutlinedButton(
                    onClick = {
                        authViewmodel.logIn(email, password)
                    }, shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9DF0A5),
                        disabledContainerColor = Color(0xFFE7EEF1)
                    ), modifier = Modifier.padding(8.dp)
                ) {
                    Text(text = "Login", color = Color.Black, fontFamily = roboto)
                }
            }
        }

        Spacer(
            modifier = Modifier
                .width(600.dp)
                .height(450.dp)
                .offset(y = 320.dp)
                .scale(2f, 1f)
                .clip(RoundedCornerShape(200.dp))
                .background(Color.White)
                .align(Alignment.BottomCenter)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Don't have an account?", fontSize = 16.sp, fontFamily = roboto
            )
            Text(
                text = "Register",
                fontSize = 21.sp,
                textDecoration = TextDecoration.Underline,
                color = Color.Blue,
                modifier = Modifier.clickable {
                    navController.navigate(Routes.Register)
                },
                fontFamily = roboto
            )
            Spacer(modifier = Modifier.height(25.dp))
        }
    }
}


@Composable
fun AuthTextFieldComponent(
    value: String,
    modifier: Modifier = Modifier,
    isPasswordField: Boolean = false,
    onLoginClick: () -> Unit,
    onValueChange: (String) -> Unit
) {

    var visibility by remember { mutableStateOf(false) }

    TextField(
        value = value,
        onValueChange = { onValueChange(it) },
        textStyle = TextStyle(fontFamily = roboto),
        placeholder = {
            Text(
                text = if (isPasswordField) "Password" else "Email", fontFamily = roboto
            )
        },
        maxLines = 1,
        shape = RoundedCornerShape(8.dp),
        trailingIcon = {
            if (isPasswordField) {
                if (visibility) Image(painter = painterResource(id = R.drawable.visibility_off_24px),
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable {
                            visibility = !visibility
                        })
                else Image(painter = painterResource(id = R.drawable.visibility_24px),
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable {
                            visibility = !visibility
                        })
            }
        },
        visualTransformation = if (isPasswordField) {
            if (visibility) VisualTransformation.None
            else PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        colors = TextFieldDefaults.colors(
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            focusedPlaceholderColor = Color.Black,
            unfocusedPlaceholderColor = Color.Black,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        ),
        keyboardActions = if (isPasswordField) {
            KeyboardActions(onDone = { onLoginClick() })
        } else {
            KeyboardActions()
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = if (isPasswordField) ImeAction.Done else ImeAction.Next
        ),
        modifier = modifier.width(310.dp)
    )
}