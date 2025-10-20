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
fun ExploreScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE0F2F1)), // A light teal background
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Explore Content",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00796B)
            )
            Text(
                text = "Discover new things!",
                fontSize = 18.sp,
                color = Color(0xFF4DB6AC)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewExploreScreen() {
    ExploreScreen()
}