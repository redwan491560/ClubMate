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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.clubmate.db.Routes
import com.example.clubmate.ui.theme.Composables.Companion.TextDesign
import com.example.clubmate.util.Category
import com.example.clubmate.viewmodel.GroupViewmodel
import com.example.clubmate.viewmodel.UserJoinDetails

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewGroupUserDetailScreen(
    info: Routes.GroupUserDetails, grpViewmodel: GroupViewmodel, navController: NavHostController
) {

    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    var selectedCategory by remember { mutableStateOf(Category.General) }
    val categoryList = listOf(
        Category.General,
        Category.Admin,
        Category.President,
        Category.VicePresident,
        Category.Treasurer
    )

    var alertState by remember { mutableStateOf(false) }

    var user by remember {
        mutableStateOf(UserJoinDetails())
    }
    var grpDetails by remember {
        mutableStateOf(Routes.GrpDetails())
    }

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
            .padding(start = 15.dp, end = 10.dp, top = 30.dp), horizontalAlignment = Alignment.Start
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 25.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Image(imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = null,
                    Modifier
                        .size(25.dp)
                        .clickable {
                            navController.popBackStack()
                        })
                TextDesign(text = "Participants details", size = 20)
            }
            TextDesign(
                text = if (currentUserIsAdmin) "Admin" else "General",
                modifier = Modifier.padding(end = 10.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            TextDesign(text = info.grpId, size = 14)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextDesign(text = grpDetails.grpName, size = 22)
                TextDesign(text = grpDetails.description, size = 17)
            }


        }
        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .systemBarsPadding(), shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(15.dp, 8.dp)) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextDesign(text = "name: " + user.username, size = 19)
                    TextDesign(text = user.email, size = 17)
                }
                Spacer(modifier = Modifier.height(5.dp))
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextDesign(text = "category: " + user.userType.toString(), size = 18)
                    TextDesign(text = "joined: " + grpViewmodel.convertTimestamp(user.joinData))
                }
            }
        }


    }
}
