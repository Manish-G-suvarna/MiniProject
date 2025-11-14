package com.example.miniproject.core.navigation


import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.miniproject.core.auth.AuthViewModel
import com.example.miniproject.presentation.login.LoginPage
import com.example.miniproject.presentation.login.SignupPage
import com.example.miniproject.presentation.home.HomePage


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