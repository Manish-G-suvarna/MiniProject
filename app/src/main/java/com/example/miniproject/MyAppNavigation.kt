package com.example.miniproject


import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.miniproject.pages.LoginPage
import com.example.miniproject.pages.SignupPage
import com.example.miniproject.pages.HomePage


@Composable
fun MyAppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginPage(navController)
        }

        composable("signup") {
            SignupPage(navController)
        }

        composable("homepage") {
            HomePage(navController)
        }
    }
}