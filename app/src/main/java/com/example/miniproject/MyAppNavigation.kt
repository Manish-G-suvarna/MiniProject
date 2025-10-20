package com.example.miniproject


import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.miniproject.pages.LoginPage
import com.example.miniproject.pages.SignupPage
import com.example.miniproject.pages.HomePage


@Composable
fun MyAppNavigation(navController: NavHostController) {


    val authViewModel: AuthViewModel = viewModel()


    val startDestination = if (authViewModel.currentUser.value != null) {
        "homepage"
    } else {
        "login"
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") {
            LoginPage(navController, authViewModel)
        }

        composable("signup") {

            SignupPage(navController, authViewModel)
        }

        composable("homepage") {

            HomePage(navController, authViewModel)
        }
    }
}