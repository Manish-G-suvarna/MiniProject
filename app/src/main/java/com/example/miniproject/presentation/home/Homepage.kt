package com.example.miniproject.presentation.home

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.miniproject.core.auth.AuthViewModel
import com.example.miniproject.presentation.profile.ProfileScreen

// --- SIMPLE, BULLETPROOF DATA CLASSES (NO SERIALIZATION) ---
data class SimpleWeatherData(
    val locationName: String,
    val currentTemp: Int,
    val currentCondition: String,
    val isDay: Boolean,
    val todayHigh: Int,
    val todayLow: Int,
    val hourly: List<SimpleHour>,
    val daily: List<SimpleDay>
)

data class SimpleHour(
    val time: String,
    val temp: Int,
    val condition: String
)

data class SimpleDay(
    val dayName: String,
    val high: Int,
    val low: Int,
    val condition: String
)

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
                HomeScreenContent(
                    navController = bottomNav,
                    authViewModel = authViewModel
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

@Composable
fun HomeScreenContent(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val user = authViewModel.currentUser.value
    val localImg = authViewModel.localProfileImageUri.value
    var searchText by remember { mutableStateOf("Dakshina Kannada") }

    // --- HARDCODED FAKE DATA ---
    val fakeWeather = remember {
        SimpleWeatherData(
            locationName = "Dakshina Kannada",
            currentTemp = 28,
            currentCondition = "Partly Cloudy",
            isDay = true,
            todayHigh = 32,
            todayLow = 24,
            hourly = listOf(
                SimpleHour("10 AM", 29, "Partly cloudy"),
                SimpleHour("11 AM", 31, "Partly cloudy"),
                SimpleHour("12 PM", 32, "Partly cloudy"),
                SimpleHour("1 PM", 32, "Partly cloudy"),
                SimpleHour("2 PM", 32, "Partly cloudy"),
                SimpleHour("3 PM", 31, "Partly cloudy")
            ),
            daily = listOf(
                SimpleDay("Today", 32, 24, "Partly cloudy"),
                SimpleDay("Tue", 33, 25, "Sunny"),
                SimpleDay("Wed", 31, 24, "Patchy rain"),
                SimpleDay("Thu", 30, 23, "Cloudy"),
                SimpleDay("Fri", 34, 26, "Sunny"),
                SimpleDay("Sat", 32, 25, "Partly cloudy"),
                SimpleDay("Sun", 31, 24, "Patchy rain")
            )
        )
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // --- Profile and Search Section (UNCHANGED) ---
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
                AsyncImage(
                    model = localImg ?: user?.photoUrl ?: "https://www.gravatar.com/avatar/",
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
                placeholder = { Text("Search Location...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp)
            )
        }

        // --- Weather Section ---
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                WeatherCard(weather = fakeWeather)
            }

            Spacer(modifier = Modifier.height(16.dp))

            SevenDayForecast(dailyForecast = fakeWeather.daily)
        }
    }
}


@Composable
fun WeatherCard(weather: SimpleWeatherData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF427EB3)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = weather.locationName,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${weather.currentTemp}°",
                        color = Color.White,
                        fontSize = 60.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Icon(
                        imageVector = getWeatherIcon(weather.currentCondition, weather.isDay),
                        contentDescription = weather.currentCondition,
                        modifier = Modifier.size(50.dp),
                        tint = Color.White
                    )
                    Text(
                        text = weather.currentCondition,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 16.sp
                    )
                    Text(
                        text = "H:${weather.todayHigh}° L:${weather.todayLow}°",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(weather.hourly) { hour ->
                    HourlyForecastItem(hour)
                }
            }
        }
    }
}

@Composable
fun HourlyForecastItem(hour: SimpleHour) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = hour.time, color = Color.White, fontSize = 14.sp)
        Icon(
            imageVector = getWeatherIcon(hour.condition, true), // Assuming hourly is always for day
            contentDescription = hour.condition,
            modifier = Modifier.size(32.dp),
            tint = Color.White
        )
        Text(text = "${hour.temp}°", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SevenDayForecast(dailyForecast: List<SimpleDay>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF427EB3).copy(alpha = 0.8f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "7-DAY FORECAST",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            dailyForecast.forEach {
                DailyForecastRow(it)
                Divider(color = Color.White.copy(alpha = 0.3f))
            }
        }
    }
}

@Composable
fun DailyForecastRow(dayForecast: SimpleDay) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(dayForecast.dayName, color = Color.White, fontWeight = FontWeight.Medium, modifier = Modifier.width(50.dp))
        Icon(
            imageVector = getWeatherIcon(dayForecast.condition, true), // Assume day for forecast icon
            contentDescription = dayForecast.condition,
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )
        Text("L: ${dayForecast.low}°", color = Color.White.copy(alpha = 0.7f))
        Text("H: ${dayForecast.high}°", color = Color.White)
    }
}

fun getWeatherIcon(condition: String, isDay: Boolean): ImageVector {
    return when {
        condition.contains("Sunny", ignoreCase = true) || condition.contains("Clear", ignoreCase = true) -> if (isDay) Icons.Default.WbSunny else Icons.Default.NightsStay
        condition.contains("cloudy", ignoreCase = true) -> Icons.Default.Cloud
        condition.contains("rain", ignoreCase = true) -> Icons.Default.WaterDrop
        condition.contains("snow", ignoreCase = true) -> Icons.Default.AcUnit
        else -> if (isDay) Icons.Default.WbSunny else Icons.Default.NightsStay
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHomePage() {
    val dummy = rememberNavController()
    HomePage(dummy, viewModel())
}
