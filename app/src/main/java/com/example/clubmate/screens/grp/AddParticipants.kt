package com.example.clubmate.screens.grp

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clubmate.R
import com.example.clubmate.db.Routes
import com.example.clubmate.screens.launchToast
import com.example.clubmate.ui.theme.Composables
import com.example.clubmate.ui.theme.Composables.Companion.TextDesign
import com.example.clubmate.ui.theme.ItemDesignAlert
import com.example.clubmate.ui.theme.roboto
import com.example.clubmate.viewmodel.Category
import com.example.clubmate.viewmodel.GroupViewmodel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AddParticipants(
    grpInfo: Routes.AddUserToGroup, grpViewmodel: GroupViewmodel
) {

    val context = LocalContext.current
    // returns the searched user
    val user = grpViewmodel.user
    var query by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        // load all participants

    }


    // add user to the group


    // of someones uid does not match than add the user

    Scaffold(
        modifier = Modifier
            .systemBarsPadding()
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextDesign(text = "Add Members to grpname", size = 17)
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xD5C0D0F7)), contentAlignment = Alignment.CenterStart
            ) {
                if (query.isEmpty()) {
                    Text(
                        text = "Add member by email",
                        fontFamily = roboto,
                        fontSize = 14.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(start = 15.dp)
                    )
                }
                BasicTextField(
                    value = query, onValueChange = {
                        query = it
                    }, modifier = Modifier
                        .padding(15.dp)
                        .fillMaxWidth(), textStyle = TextStyle(
                        fontFamily = roboto, fontSize = 16.sp, color = Color.Black
                    ), maxLines = 1, keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ), keyboardActions = KeyboardActions(onDone = {
                        grpViewmodel.findUser(query)
                    })
                )
                Image(painter = painterResource(id = R.drawable.search),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 15.dp)
                        .size(22.dp)
                        .align(Alignment.CenterEnd)
                        .rotate(270f)
                        .clickable {
                            grpViewmodel.findUser(query)
                        })
            }

            Column {
                ItemDesignAlert(userState = grpViewmodel.userState)
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding( top = 10.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Button(onClick = {
                    user?.email?.let {
                        grpViewmodel.addParticipants(
                            grpId = grpInfo.grpId,
                            email = user.email, category = Category.General
                        )
                    }
                    launchToast(context = context, "participant added successfully")
                    query = ""
                    grpViewmodel.emptyUser()
                }, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(Color(0xFFBBF7B1))) {
                    TextDesign(text = "Add participant")

                }
            }
        }
    }


}

@Preview(showSystemUi = true)
@Composable
private fun Hdgf() {
    // AddParticipants(grpId = Routes.AddUserToGroup("kekjhjhfdjf"))
}