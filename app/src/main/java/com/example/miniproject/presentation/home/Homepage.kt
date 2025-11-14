package com.example.miniproject.presentation.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import coil.compose.rememberAsyncImagePainter
import com.example.miniproject.core.auth.AuthViewModel
import com.example.miniproject.data.remote.WeatherApiResponse
import com.example.miniproject.presentation.profile.ProfileScreen
import com.example.miniproject.domain.location.ReverseGeocodingHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(mainNavController: NavController, authViewModel: AuthViewModel) {
    val bottomNav = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavBar(bottomNav) }
    ) { padding ->
        NavHost(
            navController = bottomNav,
            startDestination = "home_tab",
            modifier = Modifier.padding(padding)
        ) {
            composable("home_tab") {
                val weatherViewModel: WeatherViewModel = viewModel()
                HomeScreenContent(
                    navController = bottomNav,
                    authViewModel = authViewModel,
                    weatherViewModel = weatherViewModel
                )
            }
            composable("detect_tab") { /* TODO */ }
            composable("profile_tab") {
                ProfileScreen(mainNavController, authViewModel)
            }
        }
    }
}

@Composable
fun BottomNavBar(navController: NavController) {
    val current = navController.currentBackStackEntryAsState().value?.destination?.route

    BottomAppBar(
        modifier = Modifier.height(70.dp).shadow(12.dp),
        containerColor = Color.White
    ) {
        Row(
            Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(Icons.Filled.Home, Icons.Outlined.Home, "Home", "home_tab", current, navController)
            BottomNavItem(Icons.Filled.CameraAlt, Icons.Outlined.CameraAlt, "Detect", "detect_tab", current, navController)
            BottomNavItem(Icons.Filled.Person, Icons.Outlined.Person, "Profile", "profile_tab", current, navController)
        }
    }
}

@Composable
fun RowScope.BottomNavItem(
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    label: String,
    route: String,
    current: String?,
    navController: NavController
) {
    val selected = current == route
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.1f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium)
    )

    Column(
        modifier = Modifier
            .weight(1f)
            .clickable {
                navController.navigate(route) {
                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(40.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale)
                .background(if (selected) Color(0xFFFADADD) else Color.Transparent, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (selected) selectedIcon else unselectedIcon,
                contentDescription = label,
                tint = Color.Black
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreenContent(
    navController: NavController,
    authViewModel: AuthViewModel,
    weatherViewModel: WeatherViewModel
) {
    val user = authViewModel.currentUser.value
    val localImg = authViewModel.localProfileImageUri.value
    var searchText by remember { mutableStateOf("") }

    val context = LocalContext.current

    val gpsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.any { it }
        if (granted) {
            weatherViewModel.loadWeatherUsingGPS(context)
        }
    }

    // Ask permission & load GPS weather only once
    LaunchedEffect(Unit) {
        gpsPermissionLauncher.launch(
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    val weatherState = weatherViewModel.weatherState.value

    Column(
        Modifier.fillMaxSize()
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(bottom = 16.dp)
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable { navController.navigate("profile_tab") },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = localImg ?: user?.photoUrl ?: "https://www.gravatar.com/avatar/"
                    ),
                    contentDescription = "Profile Image",
                    modifier = Modifier
                        .size(45.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(Modifier.width(16.dp))

                Text(
                    text = user?.displayName ?: "Guest",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = { Text("Search anything...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when (weatherState) {
                is WeatherUiState.Loading -> LoadingState()
                is WeatherUiState.Error -> ErrorState(weatherState.message)
                is WeatherUiState.Success -> WeatherContent(weatherState.weather)
            }
        }

    }
}

@Composable
fun LoadingState() {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorState(msg: String) {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Text(msg, color = Color.Red, fontSize = 18.sp)
    }
}

// WEATHER UI ------------------------------------------------------------

@Composable
fun WeatherContent(weather: WeatherApiResponse) {
    val isDay = weather.current.isDay == 1

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // LOCATION NAME
        Text(
            text = weather.location.name,
            color = Color.Black,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // WEATHER ICON (static placeholder – optional to upgrade later)
        Icon(
            imageVector = Icons.Default.WbSunny,
            contentDescription = null,
            tint = if (isDay) Color(0xFFFFD700) else Color(0xFF87CEFA),
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // TEMPERATURE
        Text(
            text = "${weather.current.tempC.toInt()}°C",
            color = Color.Black,
            fontSize = 60.sp,
            fontWeight = FontWeight.Bold
        )

        // CONDITION TEXT
        Text(
            text = weather.current.condition.text,
            color = Color.DarkGray,
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // EXTRA SUMMARY CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(Color(0xFFF1F1F1))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                WeatherRowItem("Region", weather.location.region)
                WeatherRowItem("Country", weather.location.country)
                WeatherRowItem("Latitude", weather.location.lat.toString())
                WeatherRowItem("Longitude", weather.location.lon.toString())
            }
        }
    }
}

@Composable
fun WeatherRowItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray)
        Text(value, fontWeight = FontWeight.Bold)
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewHomePage() {
    val dummy = rememberNavController()
    HomePage(dummy, viewModel())
}
