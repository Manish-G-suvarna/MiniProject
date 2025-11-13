package com.example.miniproject.pages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
import com.example.miniproject.AuthViewModel
import com.example.miniproject.pages.ProfileScreen

// --- Weather Data and ViewModel ---
data class HourlyWeather(val time: String, val icon: ImageVector, val temperature: Int, val isSunset: Boolean = false)
data class DailyWeather(val day: String, val icon: ImageVector, val precipChance: Int, val lowTemp: Int, val highTemp: Int)

class WeatherViewModel : ViewModel() {
    val location = mutableStateOf("Dakshina Kannada")
    val currentTemperature = mutableStateOf(30)
    val weatherCondition = mutableStateOf("Partly Cloudy")
    val isDay = mutableStateOf(true)
    val highTemp = mutableStateOf(32)
    val lowTemp = mutableStateOf(24)

    private fun getWeatherIcon(condition: String, isDayTime: Boolean): ImageVector {
        return when {
            condition.contains("Cloudy") -> Icons.Default.Cloud
            isDayTime -> Icons.Default.WbSunny
            else -> Icons.Default.NightsStay
        }
    }

    val hourlyForecasts = mutableStateListOf(
        HourlyWeather("Now", getWeatherIcon("Partly Cloudy", true), 30),
        HourlyWeather("4PM", Icons.Default.WbSunny, 29),
        HourlyWeather("5PM", Icons.Default.Cloud, 28),
        HourlyWeather("6PM", Icons.Default.WbSunny, 27),
        HourlyWeather("6:01PM", Icons.Default.WbSunny, 0, isSunset = true),
        HourlyWeather("7PM", Icons.Default.NightsStay, 27),
        HourlyWeather("8PM", Icons.Default.NightsStay, 26)
    )

    val dailyForecasts = listOf(
        DailyWeather("Today", Icons.Default.Cloud, 45, 24, 32),
        DailyWeather("Fri", Icons.Default.Cloud, 60, 23, 32),
        DailyWeather("Sat", Icons.Default.WbSunny, 0, 23, 32),
        DailyWeather("Sun", Icons.Default.WbSunny, 0, 21, 32),
        DailyWeather("Mon", Icons.Default.Cloud, 0, 22, 32)
    )

    init {
        viewModelScope.launch {
            while (true) {
                delay(10000) // Update every 10 seconds

                // Toggle day/night
                isDay.value = !isDay.value

                // Toggle weather condition
                val newCondition = if (Random.nextBoolean()) "Partly Cloudy" else "Sunny"
                weatherCondition.value = newCondition

                // Update temperature
                val tempChange = Random.nextInt(-1, 2)
                currentTemperature.value += tempChange

                // Update the "Now" forecast in the hourly list
                hourlyForecasts[0] = hourlyForecasts[0].copy(
                    temperature = currentTemperature.value,
                    icon = getWeatherIcon(newCondition, isDay.value)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(mainNavController: NavController, authViewModel: AuthViewModel) {
    val bottomNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomAppBar(
                modifier = Modifier
                    .height(70.dp)
                    .shadow(12.dp),
                containerColor = Color.White,
                contentColor = Color.Black,
                tonalElevation = 0.dp
            ) {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomNavItem(Icons.Filled.Home, Icons.Outlined.Home, "Home", "home_tab", currentRoute, bottomNavController)
                    BottomNavItem(Icons.Filled.CameraAlt, Icons.Outlined.CameraAlt, "Detect", "detect_tab",  currentRoute, bottomNavController)
                    BottomNavItem(Icons.Filled.Person, Icons.Outlined.Person, "Profile", "profile_tab", currentRoute, bottomNavController)
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = bottomNavController,
            startDestination = "home_tab",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home_tab") { HomeScreenContent(weatherViewModel = viewModel()) }
            composable("detect_tab") { /* Detect Screen */ }
            composable("profile_tab") {
                ProfileScreen(navController = mainNavController, authViewModel = authViewModel)
            }
        }
    }
}

@Composable
fun RowScope.BottomNavItem(
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    label: String,
    route: String,
    currentRoute: String?,
    navController: NavController
) {
    val selected = currentRoute == route
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.1f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )

    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                navController.navigate(route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .background(
                    color = if (selected) Color(0xFFFADADD) else Color.Transparent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (selected) selectedIcon else unselectedIcon,
                contentDescription = label,
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreenContent(weatherViewModel: WeatherViewModel = viewModel()) {
    val isDay = weatherViewModel.isDay.value
    val weatherCondition = weatherViewModel.weatherCondition.value

    val mainWeatherIcon = when {
        weatherCondition.contains("Cloudy") -> Icons.Default.Cloud
        isDay -> Icons.Default.WbSunny
        else -> Icons.Default.NightsStay
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = if (isDay) listOf(Color(0xFF6EACE0), Color(0xFF8EC5F0)) else listOf(Color(0xFF001428), Color(0xFF003366))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 60.dp, bottom = 80.dp), // Padding for status bar and navigation bar
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main Weather Info
            Text(weatherViewModel.location.value, color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Light)
            Spacer(modifier = Modifier.height(16.dp))
            Icon(mainWeatherIcon, contentDescription = null, tint = Color.White, modifier = Modifier.size(80.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("${weatherViewModel.currentTemperature.value}°", color = Color.White, fontSize = 100.sp, fontWeight = FontWeight.Thin)
            Text(weatherCondition, color = Color.White, fontSize = 20.sp)
            Text("H:${weatherViewModel.highTemp.value}° L:${weatherViewModel.lowTemp.value}°", color = Color.White, fontSize = 20.sp)

            Spacer(modifier = Modifier.height(32.dp))

            // Hourly Forecast Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Cloudy conditions expected around 5PM. Wind gusts are up to 23 kph.",
                        color = Color.White
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.5f))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        items(weatherViewModel.hourlyForecasts) { forecast ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(forecast.time, color = Color.White, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                if (forecast.isSunset) {
                                    Icon(forecast.icon, contentDescription = "Sunset", tint = Color.White)
                                    Text("Sunset", color = Color.White)
                                } else {
                                    Icon(forecast.icon, contentDescription = "Weather Icon", tint = Color.White, modifier = Modifier.size(28.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("${forecast.temperature}°", color = Color.White, fontSize = 20.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 10-Day Forecast Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("10-DAY FORECAST", color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    weatherViewModel.dailyForecasts.forEach { forecast ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(forecast.day, color = Color.White, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                            Icon(forecast.icon, contentDescription = null, tint = Color.White, modifier = Modifier.weight(1f))
                            Text("${forecast.lowTemp}°", color = Color.White.copy(alpha = 0.7f))
                            TemperatureRangeBar(low = forecast.lowTemp, high = forecast.highTemp, modifier = Modifier.weight(2f).padding(horizontal = 8.dp))
                            Text("${forecast.highTemp}°")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TemperatureRangeBar(low: Int, high: Int, modifier: Modifier = Modifier) {
    val totalRange = 40 // Assume a fixed total temperature range for visualization
    val lowPercentage = low / totalRange.toFloat()
    val highPercentage = (high - low) / totalRange.toFloat()

    BoxWithConstraints(modifier = modifier.height(4.dp)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Gray.copy(alpha = 0.5f), shape = CircleShape)
        ) {
            Box(
                modifier = Modifier
                    .height(4.dp)
                    .width(maxWidth * highPercentage)
                    .padding(start = maxWidth * lowPercentage)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFFF39C12), Color(0xFFF1C40F))
                        ),
                        shape = CircleShape
                    )
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewHomePage() {
    val dummyNavController = rememberNavController()
    HomePage(mainNavController = dummyNavController, authViewModel = viewModel())
}
