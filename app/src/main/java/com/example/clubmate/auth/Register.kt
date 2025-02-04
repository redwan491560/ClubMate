package com.example.clubmate.auth

import android.widget.Toast
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
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.clubmate.R
import com.example.clubmate.db.Routes
import com.example.clubmate.db.Status
import com.example.clubmate.ui.theme.roboto
import com.example.clubmate.viewmodel.AuthViewModel
import kotlinx.coroutines.launch


@Composable
fun RegisterScreen(authViewmodel: AuthViewModel, navController: NavHostController) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val authState = authViewmodel.authState.observeAsState()
    LaunchedEffect(authState.value) {
        when (authState.value) {
            is Status.Error -> {
                Toast.makeText(
                    context,
                    (authState.value as Status.Error).message,
                    Toast.LENGTH_SHORT
                ).show()
                isLoading = false
            }

            else -> Unit
        }
    }


    var visibility by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xff98C1D9))
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .systemBarsPadding()
            ) {
                Spacer(modifier = Modifier.height(10.dp))
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
                    .padding(top = 15.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextField(
                    value = userName,
                    onValueChange = {
                        userName = it
                    },
                    placeholder = {
                        Text(text = "Username", fontFamily = roboto)
                    }, modifier = Modifier.width(310.dp),
                    maxLines = 1,
                    textStyle = TextStyle(fontFamily = roboto),
                    shape = RoundedCornerShape(8.dp),
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
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text, imeAction = ImeAction.Next
                    )
                )

                TextField(value = email, onValueChange = {
                    email = it
                }, maxLines = 1, modifier = Modifier.width(310.dp), placeholder = {
                    Text(text = "Email", fontFamily = roboto)
                }, shape = RoundedCornerShape(8.dp), colors = TextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedPlaceholderColor = Color.Black,
                    unfocusedPlaceholderColor = Color.Black,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ), keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text, imeAction = ImeAction.Next
                ), textStyle = TextStyle(fontFamily = roboto)
                )
                TextField(
                    value = phone,
                    onValueChange = {
                        phone = it
                    },
                    placeholder = {
                        Text(text = "Contact No", fontFamily = roboto)
                    },
                    maxLines = 1,
                    textStyle = TextStyle(fontFamily = roboto),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number, imeAction = ImeAction.Next
                    ),
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
                    shape = RoundedCornerShape(8.dp), modifier = Modifier.width(310.dp)
                )
                TextField(
                    value = password,
                    onValueChange = {
                        password = it
                    },
                    placeholder = {
                        Text(text = "Password", fontFamily = roboto)
                    },
                    textStyle = TextStyle(fontFamily = roboto),
                    maxLines = 1,
                    visualTransformation = if (visibility) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    keyboardActions = KeyboardActions(onDone = {
                        isLoading = true
                        authViewmodel.register(email, password, userName, phone) {
                            isLoading = false
                            navController.navigate(Routes.Login)
                        }
                    }),
                    trailingIcon = {
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
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text, imeAction = ImeAction.Done
                    ), modifier = Modifier.width(310.dp),
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
                    shape = RoundedCornerShape(8.dp)
                )

                Row(
                    modifier = Modifier
                        .width(300.dp)
                        .height(10.dp)
                        .padding(top = 5.dp)
                ) {
                    if (isLoading) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
                OutlinedButton(
                    onClick = {
                        isLoading = true
                        scope.launch {
                            authViewmodel.register(
                                email = email,
                                password = password,
                                userName = userName,
                                phone = phone
                            ) { state ->
                                if (!state) isLoading = false
                                else{
                                    isLoading = false
                                    navController.navigate(Routes.Login)
                                }
                            }
                        }
                    }, shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9DD2F0),
                        disabledContainerColor = Color(0xFFE7EEF1)
                    ), modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "Register",
                        fontFamily = roboto,
                        color = Color.Black,
                        fontSize = 18.sp
                    )
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
                .background(Color(0xFFD3F7D7))
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
                text = "Already have an account?", fontSize = 16.sp, fontFamily = roboto
            )
            Text(text = "Login",
                fontFamily = roboto,
                fontSize = 21.sp,
                textDecoration = TextDecoration.Underline,
                color = Color.Blue,
                modifier = Modifier.clickable {
                    navController.navigate(Routes.Login)
                })
            Spacer(modifier = Modifier.height(25.dp))
        }
    }
}

@Preview(showSystemUi = true)
@Composable
private fun Sheet() {
    val context = LocalContext.current
    RegisterScreen(
        authViewmodel = AuthViewModel(), navController = rememberNavController()
    )
}