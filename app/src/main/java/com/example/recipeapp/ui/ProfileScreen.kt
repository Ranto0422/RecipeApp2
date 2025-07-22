package com.example.recipeapp.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userName: String = "User",
    userEmail: String = "user@email.com",
    userRole: String = "user",
    userImageUrl: String? = null,
    onLogout: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onPantryClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onAddRecipe: () -> Unit = {},
    onAdminApproval: (() -> Unit)? = null

) {
    Scaffold(
        bottomBar = {
            BottomNavBar(
                onHomeClick = onHomeClick,
                onSearchClick = onSearchClick,
                onAddRecipe = onAddRecipe,
                onPantryClick = onPantryClick,
                onProfileClick = onProfileClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(PaddingValues(
                    start = 32.dp,
                    top = innerPadding.calculateTopPadding() + 32.dp,
                    end = 32.dp,
                    bottom = innerPadding.calculateBottomPadding() + 32.dp
                )),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Profile", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(24.dp))
            userImageUrl?.let {
                // Show user image if available
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = "Profile Image",
                    modifier = Modifier.size(96.dp)
                )
                Spacer(Modifier.height(16.dp))
            }
            Text("Name: $userName", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))
            Text("Email: $userEmail", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))
            Text("Role: $userRole", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(32.dp))
            if (userRole == "admin" && onAdminApproval != null) {
                Button(
                    onClick = onAdminApproval,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Admin Approval")
                }
                Spacer(Modifier.height(16.dp))
            }
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Logout")
            }
        }
    }
}
