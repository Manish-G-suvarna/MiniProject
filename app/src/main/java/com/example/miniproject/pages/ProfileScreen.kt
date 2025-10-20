package com.example.miniproject.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp

@Composable
fun ProfileScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3E5F5)), // A light purple background
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "User Profile",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8E24AA)
            )
            Text(
                text = "Manage your account.",
                fontSize = 18.sp,
                color = Color(0xFFBA68C8)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewProfileScreen() {
    ProfileScreen()
}