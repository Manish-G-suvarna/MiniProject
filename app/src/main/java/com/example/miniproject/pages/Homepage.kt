package com.example.miniproject.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.miniproject.AuthViewModel

@Composable
fun HomePage(navController: NavController, authViewModel: AuthViewModel) {


    val user = authViewModel.currentUser.value

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F7FB)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Welcome, ${user?.email ?: "User"}!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {

                    authViewModel.signOut()
                    navController.navigate("login") {
                        popUpTo("homepage") { inclusive = true }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0072FF))
            ) {
                Text(text = "Logout", color = Color.White)
            }
        }
    }
}