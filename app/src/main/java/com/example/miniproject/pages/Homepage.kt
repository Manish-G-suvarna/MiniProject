package com.example.miniproject.pages

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.miniproject.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(mainNavController: NavController, authViewModel: AuthViewModel) {
    val bottomNavController = rememberNavController()
    val user by authViewModel.currentUser
    val profileImageBase64 by authViewModel.profileImage

    val glassGradient = Brush.verticalGradient(
        listOf(
            Color(0x33A6E3E9), // Lightest teal with opacity
            Color(0x331ABC9C)  // Darker teal with opacity
        )
    )

    val selectedGlassGradient = Brush.verticalGradient(
        listOf(
            Color(0x66A6E3E9),
            Color(0x661ABC9C)
        )
    )

    Scaffold(
        bottomBar = {
            BottomAppBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1ABC9C), Color(0xFFA6E3E9))
                        )
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                containerColor = Color.Transparent
            ) {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomNavItem(
                        icon = Icons.Default.Home,
                        label = "Home",
                        route = "home_tab",
                        currentRoute = currentRoute,
                        navController = bottomNavController,
                        gradient = glassGradient,
                        selectedGradient = selectedGlassGradient
                    )
                    BottomNavItem(
                        icon = Icons.Default.Search,
                        label = "Explore",
                        route = "explore_tab",
                        currentRoute = currentRoute,
                        navController = bottomNavController,
                        gradient = glassGradient,
                        selectedGradient = selectedGlassGradient
                    )
                    BottomNavItem(
                        icon = Icons.Default.CameraAlt,
                        label = "Detect",
                        route = "detect_tab",
                        currentRoute = currentRoute,
                        navController = bottomNavController,
                        gradient = glassGradient,
                        selectedGradient = selectedGlassGradient,
                        isCentral = true
                    )
                    BottomNavItem(
                        icon = Icons.Default.ShoppingCart,
                        label = "Shop",
                        route = "shop_tab",
                        currentRoute = currentRoute,
                        navController = bottomNavController,
                        gradient = glassGradient,
                        selectedGradient = selectedGlassGradient
                    )
                    BottomNavItem(
                        icon = Icons.Default.Person,
                        label = "Profile",
                        route = "profile_tab",
                        currentRoute = currentRoute,
                        navController = bottomNavController,
                        gradient = glassGradient,
                        selectedGradient = selectedGlassGradient
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = bottomNavController,
            startDestination = "home_tab",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home_tab") {
                HomeScreenContent(
                    userName = user?.displayName ?: "User",
                    profileImageBase64 = profileImageBase64
                )
            }
            composable("explore_tab") {
//                ExploreScreen()
            }
            composable("detect_tab") {
//                QuickDetectScreen()
            }
            composable("shop_tab") {
//                ShopScreen()
            }
            composable("profile_tab") {
                ProfileScreen(navController = mainNavController, authViewModel = authViewModel)
            }
        }
    }
}

@Composable
fun RowScope.BottomNavItem(
    icon: ImageVector,
    label: String,
    route: String,
    currentRoute: String?,
    navController: NavController,
    gradient: Brush,
    selectedGradient: Brush,
    isCentral: Boolean = false
) {
    val selected = currentRoute == route
    val containerColor = if (selected) selectedGradient else gradient
    val contentColor = if (selected) Color.White else Color.Black.copy(alpha = 0.7f)

    Column(
        modifier = Modifier
            .weight(1f)
            .clickable {
                navController.navigate(route) {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            }
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .clip(CircleShape)
            .background(containerColor),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = contentColor,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(userName: String, profileImageBase64: String?) {

    val topBarGradient = Brush.verticalGradient(
        listOf(Color(0xFF1ABC9C), Color(0xFFA6E3E9))
    )
    
    val imageBitmap = remember(profileImageBase64) {
        if (profileImageBase64 != null) {
            try {
                val imageBytes = Base64.decode(profileImageBase64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F7FB))
    ) {
        // --- Top Bar Area ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(topBarGradient)
                .padding(16.dp)
        ) {
            Column {
                // --- First Row: Profile and Notification ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile Image
                    if (imageBitmap != null) {
                        Image(
                            bitmap = imageBitmap,
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color.White, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile Image",
                            tint = Color.White,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .border(2.dp, Color.White, CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = userName,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(onClick = { /* Handle notification click */ }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- Second Row: Search and Filter ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Search Bar
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        placeholder = { Text("Search", color = Color.Gray) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search Icon",
                                tint = Color.Gray
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .background(Color.White, RoundedCornerShape(25.dp)),
                        shape = RoundedCornerShape(25.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = Color(0xFF1ABC9C)
                        ),
                        singleLine = true,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    IconButton(onClick = {  }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter Options",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewHomePage() {
    val dummyNavController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()

    HomePage(mainNavController = dummyNavController, authViewModel = authViewModel)
}
