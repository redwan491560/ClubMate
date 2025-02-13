package com.example.clubmate.screens.grp

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.example.clubmate.R
import com.example.clubmate.db.Routes
import com.example.clubmate.screens.launchToast
import com.example.clubmate.ui.theme.Composables.Companion.TextDesign
import com.example.clubmate.ui.theme.roboto
import com.example.clubmate.util.Category
import com.example.clubmate.viewmodel.GroupViewmodel
import com.example.clubmate.viewmodel.UserJoinDetails

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewGroupUserDetailScreen(
    info: Routes.GroupUserDetails, grpViewmodel: GroupViewmodel, navController: NavHostController
) {

    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf(Category.General) }
    val categoryList = listOf(
        Category.General,
        Category.Admin,
        Category.President,
        Category.VicePresident,
        Category.Treasurer
    )
    var expanded by remember { mutableStateOf(false) }

    var user by remember { mutableStateOf(UserJoinDetails()) }
    var grpDetails by remember { mutableStateOf(Routes.GrpDetails()) }

    LaunchedEffect(Unit) {
        grpViewmodel.getParticipantDetails(grpId = info.grpId, userId = info.userId) {
            it?.let { user = it }
        }
    }

    var currentUserIsAdmin by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        grpViewmodel.checkAdmin(grpId = info.grpId, userId = info.currentUserId) {
            currentUserIsAdmin = it
        }
    }

    LaunchedEffect(Unit) {
        grpViewmodel.loadGroupInfo(info.grpId) { details ->
            details?.let {
                grpDetails = details
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(start = 15.dp, end = 10.dp, top = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 25.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Image(imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = null,
                    Modifier
                        .size(25.dp)
                        .clickable { navController.popBackStack() })
                TextDesign(text = "Participants details", size = 20)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 15.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 15.dp, top = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                AsyncImage(
                    model = grpDetails.photoUrl,
                    contentDescription = "group photo",
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.logo_primary),
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(60.dp))
                )
                Column(modifier = Modifier.padding(start = 20.dp)) {
                    TextDesign(text = grpDetails.grpName, size = 22)
                    TextDesign(text = grpDetails.description, size = 17)
                    TextDesign(text = info.grpId, size = 13)
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 15.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                TextDesign(text = user.userType.toString(), size = 18, color = Color.Gray)
                TextDesign(text = user.username, size = 22)
                TextDesign(text = user.email, size = 16)
                Spacer(modifier = Modifier.height(5.dp))
                TextDesign(text = grpViewmodel.convertTimestamp(user.joinData))
            }

            AsyncImage(
                model = user.photoUrl,
                contentDescription = "group photo",
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.logo_primary),
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(60.dp))
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Column(
            modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start
        ) {

            if (currentUserIsAdmin) {
                TextDesign(text = "Change Role", size = 18)
                Spacer(modifier = Modifier.height(15.dp))


                ExposedDropdownMenuBox(expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {

                    Card(
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 15.dp)
                                .menuAnchor()
                        ) {
                            TextDesign(text = selectedCategory.name, size = 18)
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = expanded
                            )
                        }
                    }

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.padding(horizontal = 3.dp)
                    ) {
                        categoryList.forEach { category ->
                            DropdownMenuItem(text = {
                                Text(
                                    text = category.name, fontFamily = roboto, fontSize = 18.sp
                                )
                            }, onClick = {
                                selectedCategory = category
                                expanded = false
                            }, contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }


                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 15.dp, end = 10.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {
                        grpViewmodel.updateUserRole(
                            grpId = info.grpId, category = selectedCategory, uid = user.uid
                        ) {
                            if (it) launchToast(context = context, "update successful")
                            else launchToast(context = context, "update failed try again")

                        }
                    }) {
                        Text(
                            text = "Update",
                            fontSize = 20.sp,
                            color = Color.Blue,
                            fontFamily = roboto,
                            textDecoration = TextDecoration.Underline
                        )
                    }
                }
            }

        }

    }
}