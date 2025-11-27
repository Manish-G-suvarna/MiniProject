package com.example.miniproject.presentation.home

import com.example.miniproject.R
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.miniproject.presentation.viewmodels.FarmViewModel
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material3.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.miniproject.core.auth.AuthViewModel
import com.example.miniproject.core.data.model.Crop
import com.example.miniproject.presentation.profile.ProfileScreen
import com.example.miniproject.presentation.shop.ShopScreen
import com.example.miniproject.presentation.finance.FinanceScreen
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import kotlinx.coroutines.launch


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
data class dataPlants(
    val plants: List<PlantNames>
)
data class PlantNames(
    val diffName: String
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
    val farmViewModel: FarmViewModel = viewModel()
    val bottomNav = rememberNavController()
    val homeCategories = dataPlants(
        plants = listOf(
            PlantNames("Cereal"),
            PlantNames("Pulse"),
            PlantNames("Oilseed"),
            PlantNames("Fiber")
        )
    )
    val fullCategories = dataPlants(
        plants = listOf(
            PlantNames("Cereal"),
            PlantNames("Pulse"),
            PlantNames("Oilseed"),
            PlantNames("Fiber"),
            PlantNames("Sugar"),
            PlantNames("Cash")
        )
    )


    Scaffold(
        bottomBar = { BottomNavBar(bottomNav) }
    ) { padding ->
        NavHost(
            navController = bottomNav,
            startDestination = "home_tab",
            modifier = Modifier.padding(padding)
        ) {
            composable("home_tab") {
                val refresh = it.savedStateHandle.getStateFlow("home_refresh", 0).collectAsState().value
                HomeScreenContent(
                    bottomNav,
                    authViewModel,
                    categoryData = homeCategories
                )
            }

            composable("TravelExplore_tab") {
                ExploreScreenContent(bottomNav, farmViewModel)
            }
            composable("shop_tab") {
                ShopScreen(bottomNav, authViewModel)
            }
            composable("finance_tab") {
                FinanceScreen(bottomNav, authViewModel)
            }
            composable("profile_tab") {
                ProfileScreen(mainNavController, authViewModel)
            }
            composable("category_screen") {
                CategoryScreen(
                    navController = bottomNav,
                    category = fullCategories
                )
            }
            composable("crop_list/{category}") { backStackEntry ->
                val category = backStackEntry.arguments?.getString("category") ?: ""
                CropListScreen(
                    navController = bottomNav,
                    viewModel = farmViewModel,
                    categoryName = category
                )
            }

            composable("crop_details/{category}/{crop}") { backStackEntry ->
                val category = backStackEntry.arguments?.getString("category") ?: ""
                val crop = backStackEntry.arguments?.getString("crop") ?: ""
                CropDetailsScreen(
                    navController = bottomNav,
                    category = category,
                    crop = crop,
                    viewModel = farmViewModel
                )
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

            BottomNavItem(
                selectedIcon = Icons.Filled.Home,
                unselectedIcon = Icons.Outlined.Home,
                label = "Home",
                route = "home_tab",
                current = current,
                navController = navController,
                onReselect = {
                    navController.navigate("home_tab") {
                        popUpTo("home_tab") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )

            BottomNavItem(
                selectedIcon = Icons.Filled.TravelExplore,
                unselectedIcon = Icons.Outlined.TravelExplore,
                label = "Explore",
                route = "TravelExplore_tab",
                current = current,
                navController = navController,
                onReselect = {
                    navController.navigate("TravelExplore_tab") {
                        popUpTo("TravelExplore_tab") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )

            BottomNavItem(
                selectedIcon = Icons.Filled.ShoppingBag,
                unselectedIcon = Icons.Outlined.ShoppingBag,
                label = "Shop",
                route = "shop_tab",
                current = current,
                navController = navController,
                onReselect = {
                    navController.navigate("shop_tab") {
                        popUpTo("shop_tab") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )

            BottomNavItem(
                selectedIcon = Icons.Filled.AccountBalance,
                unselectedIcon = Icons.Outlined.AccountBalance,
                label = "Finance",
                route = "finance_tab",
                current = current,
                navController = navController,
                onReselect = {
                    navController.navigate("finance_tab") {
                        popUpTo("finance_tab") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )

            BottomNavItem(
                selectedIcon = Icons.Filled.Person,
                unselectedIcon = Icons.Outlined.Person,
                label = "Profile",
                route = "profile_tab",
                current = current,
                navController = navController,
                onReselect = {
                    navController.navigate("profile_tab") {
                        popUpTo("profile_tab") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
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
    navController: NavController,
    onReselect: () -> Unit
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
                if (selected) {
                    // RELOAD the screen
                    onReselect()
                } else {
                    // ALWAYS return to home from deep screens
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(40.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale)
                .background(
                    if (selected) Color(0xFFFADADD)
                    else Color.Transparent,
                    CircleShape
                ),
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
    authViewModel: AuthViewModel,
    categoryData: dataPlants,
) {
    val farmViewModel: FarmViewModel = viewModel()
    val user = authViewModel.currentUser.value
    val localImg = authViewModel.localProfileImageUri.value
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

    val categories by farmViewModel.categories.collectAsState()
    val isLoading by farmViewModel.isLoading.collectAsState()

// Load categories when screen opens
    LaunchedEffect(Unit) {
        farmViewModel.loadCategories()
    }

// Search across all crops
    val searchResults = remember(searchQuery, categories) {
        if (searchQuery.isEmpty()) {
            emptyList()
        } else {
            categories.flatMap { category ->
                category.crops
                    .filter { crop ->
                        crop.name.contains(searchQuery, ignoreCase = true) ||
                                category.name.contains(searchQuery, ignoreCase = true)
                    }
                    .map { crop -> SearchResult(category.name, crop) }
            }
        }
    }

// Fake weather data (unchanged)
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

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .verticalScroll(rememberScrollState())
        ) {
            // Top Section with Gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF427EB3), Color(0xFF5B9BD5))
                        )
                    )
            ) {
                Column(Modifier.padding(16.dp)) {
                    // Profile Row
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { navController.navigate("profile_tab") }
                        ) {
                            AsyncImage(
                                model = localImg ?: user?.photoUrl ?: "https://www.gravatar.com/avatar/",
                                contentDescription = "Profile Image",
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, Color.White, CircleShape),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = "Hello,",
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                                Text(
                                    text = user?.displayName ?: "Guest",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        // Notification Icon
                        Card(
                            shape = CircleShape,
                            colors = CardDefaults.cardColors(Color.White.copy(alpha = 0.2f)),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    AnimatedVisibility(visible = isLoading) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = Color.White,
                            trackColor = Color.White.copy(alpha = 0.2f)
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // Animated Search Bar
                    AnimatedSearchBar(
                        query = searchQuery,
                        isSearching = isSearching,
                        isLoading = isLoading,
                        onQueryChange = {
                            searchQuery = it
                            isSearching = it.isNotEmpty()
                        },
                        onClear = {
                            searchQuery = ""
                            isSearching = false
                        }
                    )

                    Spacer(Modifier.height(16.dp))
                }
            }

            // Search Results or Regular Content
            if (isSearching) {
                SearchResultsSection(
                    results = searchResults,
                    isLoading = isLoading,
                    onCropClick = { categoryName, cropName ->
                        navController.navigate("crop_details/$categoryName/$cropName")
                    }
                )
            } else {
                // Weather Section
                Column(modifier = Modifier.padding(16.dp)) {
                    WeatherCard(weather = fakeWeather)
                    Spacer(modifier = Modifier.height(16.dp))
                    SevenDayForecast(dailyForecast = fakeWeather.daily)
                }

                // Categories Section
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Browse Categories",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2C3E50)
                        )

                        TextButton(onClick = { navController.navigate("category_screen") }) {
                            Text("See All", color = Color(0xFF427EB3))
                            Icon(
                                Icons.Default.KeyboardArrowRight,
                                contentDescription = null,
                                tint = Color(0xFF427EB3)
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    PlantGrid(
                        plants = categoryData.plants.map { it.diffName },
                        navController = navController,
                        isLoading = isLoading
                    )
                }
            }
        }
    }
}
@Composable
fun AnimatedSearchBar(
    query: String,
    isSearching: Boolean,
    isLoading: Boolean,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }
    val backgroundColor by animateColorAsState(
        targetValue = if (isFocused) Color.White else Color.White.copy(alpha = 0.95f),
        label = "search-background"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) Color.White else Color.White.copy(alpha = 0.4f),
        label = "search-border"
    )
    val elevation by animateDpAsState(
        targetValue = if (isFocused) 12.dp else 4.dp,
        label = "search-elevation"
    )
    val placeholderAlpha by animateFloatAsState(
        targetValue = if (isFocused) 0.9f else 0.6f,
        label = "placeholder-alpha"
    )
    val quickSuggestions = remember {
        listOf("Organic seeds", "Smart irrigation", "Soil testing", "Weather today")
    }

    Column {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation, RoundedCornerShape(20.dp), clip = false)
                .border(1.dp, borderColor, RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(backgroundColor),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color(0xFF2C6AE3),
                    modifier = Modifier.size(26.dp)
                )

                Spacer(Modifier.width(12.dp))

                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester)
                        .onFocusChanged { isFocused = it.isFocused },
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        color = Color(0xFF1E1E1E)
                    ),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (query.isEmpty()) {
                                Text(
                                    text = "Search crops, categories...",
                                    color = Color(0xFF6D7B8C).copy(alpha = placeholderAlpha),
                                    fontSize = 16.sp,
                                    modifier = Modifier.alpha(placeholderAlpha)
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                Spacer(Modifier.width(8.dp))

                SearchLoadingDots(
                    visible = isSearching && isLoading
                )

                AnimatedVisibility(visible = query.isNotEmpty()) {
                    IconButton(onClick = onClear) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search",
                            tint = Color(0xFF6D7B8C)
                        )
                    }
                }

                AnimatedVisibility(visible = query.isEmpty()) {
                    IconButton(onClick = { /* TODO: voice search */ }) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice search",
                            tint = Color(0xFF2C6AE3)
                        )
                    }
                }

                IconButton(onClick = { /* TODO: open filters */ }) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Filters",
                        tint = Color(0xFF2C6AE3)
                    )
                }
            }
        }

        AnimatedVisibility(visible = query.isEmpty()) {
            QuickSearchSuggestions(
                suggestions = quickSuggestions,
                onSuggestionClick = { suggestion ->
                    onQueryChange(suggestion)
                }
            )
        }
    }
}

@Composable
fun SearchLoadingDots(visible: Boolean) {
    AnimatedVisibility(visible = visible) {
        val transition = rememberInfiniteTransition(label = "search-dots")
        val alpha1 by transition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 600, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dot1"
        )
        val alpha2 by transition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 600, delayMillis = 120, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dot2"
        )
        val alpha3 by transition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 600, delayMillis = 240, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dot3"
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf(alpha1, alpha2, alpha3).forEach { alpha ->
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2C6AE3).copy(alpha = alpha))
                )
            }
        }
    }
}

@Composable
fun QuickSearchSuggestions(
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(suggestions) { suggestion ->
            SuggestionChip(
                text = suggestion,
                onClick = { onSuggestionClick(suggestion) }
            )
        }
    }
}

@Composable
fun SuggestionChip(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier,
        shape = RoundedCornerShape(50),
        shadowElevation = 4.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                color = Color(0xFF1F3A5C),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
data class SearchResult(
    val categoryName: String,
    val crop: Crop
)
@Composable
fun SearchResultsSection(
    results: List<SearchResult>,
    isLoading: Boolean,
    onCropClick: (String, String) -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Search Results",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2C3E50)
        )
        Spacer(Modifier.height(8.dp))

        Text(
            text = "${results.size} crops found",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(Modifier.height(16.dp))

        if (isLoading) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF427EB3))
            }
        } else if (results.isEmpty()) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.SearchOff,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "No crops found",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.height((results.size / 2 + 1) * 260.dp)
            ) {
                items(results) { result ->
                    SearchResultCard(
                        result = result,
                        onClick = { onCropClick(result.categoryName, result.crop.name) }
                    )
                }
            }
        }
    }
}

@Composable
fun SearchResultCard(
    result: SearchResult,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(Color.White)
    ) {
        Column {
// Crop Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color(0xFFF5F5F5))
            ) {
                AsyncImage(
                    model = result.crop.picURL,
                    contentDescription = result.crop.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = painterResource(R.drawable.cereal),
                    placeholder = painterResource(R.drawable.cereal)
                )
                // Category Badge
                Card(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopEnd),
                    colors = CardDefaults.cardColors(
                        getCategoryColor(result.categoryName)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = result.categoryName,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Crop Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = result.crop.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black,
                    maxLines = 1
                )

                Spacer(Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color(0xFF427EB3)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "View Details",
                        color = Color(0xFF427EB3),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
fun getCategoryColor(categoryName: String): Color {
    return when (categoryName.lowercase()) {
        "cereal" -> Color(0xFFFFB74D)
        "pulse" -> Color(0xFFCDDC39)
        "oilseed" -> Color(0xFFFFCA28)
        "fiber" -> Color(0xFFBA68C8)
        "sugar" -> Color(0xFFFF8A65)
        "cash" -> Color(0xFF64B5F6)
        else -> Color.Gray
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

@Composable
fun CategoryCard(category: dataPlants,navController: NavController) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {

        Column(modifier = Modifier.padding(16.dp)) {

            // Category title
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(16.dp)
                    .clickable {
                        navController.navigate("category_screen")
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically

            ) {
                Text(
                    text = "Category",
                    color = Color.Black,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            PlantGrid(
                plants = category.plants.map { it.diffName },
                navController = navController
            )

        }
    }
}



@Composable
fun PlantGrid(
    plants: List<String>,
    navController: NavController,
    isLoading: Boolean = false
) {
    if (isLoading) {
        PlantGridSkeleton()
        return
    }

    val subtitles = mapOf(
        "Cereal" to "Wheat, Rice",
        "Pulse"  to "Lentils, Beans",
        "Oilseed" to "Groundnut, Mustard",
        "Fiber"  to "Cotton, Jute",
        "Sugar"  to "Cane, Beet",
        "Cash"   to "High-value crops"
    )

    val icons = mapOf(
        "Cereal" to R.drawable.cereal,
        "Pulse"  to R.drawable.pulse,
        "Oilseed" to R.drawable.oilseed,
        "Fiber"  to R.drawable.fiber,
        "Sugar"  to R.drawable.sugar,
        "Cash"   to R.drawable.cash
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        plants.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                FancyCategoryCard(
                    name = row[0],
                    subtitle = subtitles[row[0]] ?: "",
                    imageRes = icons[row[0]]!!,
                    navController = navController,
                    isLoading = isLoading
                )

                if (row.size > 1) {
                    FancyCategoryCard(
                        name = row[1],
                        subtitle = subtitles[row[1]] ?: "",
                        imageRes = icons[row[1]]!!,
                        navController = navController,
                        isLoading = isLoading
                    )
                } else {
                    Spacer(modifier = Modifier.width(170.dp))
                }
            }
        }
    }
}

@Composable
fun PlantGridSkeleton(rows: Int = 2) {
    val shimmerBrush = rememberShimmerBrush()
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        repeat(rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(2) {
                    SkeletonCategoryCard(brush = shimmerBrush)
                }
            }
        }
    }
}

@Composable
fun SkeletonCategoryCard(brush: Brush) {
    Card(
        modifier = Modifier
            .width(170.dp)
            .height(200.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(brush)
            )
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .height(18.dp)
                    .fillMaxWidth(0.9f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(brush)
            )
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .height(14.dp)
                    .fillMaxWidth(0.6f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(brush)
            )
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .height(36.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(brush)
            )
        }
    }
}

@Composable
fun rememberShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val offset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer-shift"
    )
    return Brush.linearGradient(
        colors = listOf(
            Color(0xFFE5E5E5),
            Color(0xFFF5F5F5),
            Color(0xFFE5E5E5)
        ),
        start = Offset(offset - 200f, 0f),
        end = Offset(offset, 200f)
    )
}

@Composable
fun FancyCategoryCard(
    name: String,
    subtitle: String,
    imageRes: Int,
    navController: NavController,
    isLoading: Boolean = false
) {
    val gradient = getCategoryGradient(name)
    val bounceScale = remember { Animatable(1f) }
    var isAnimating by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(
            durationMillis = 180,
            easing = LinearOutSlowInEasing
        )
    )
    val tilt by animateFloatAsState(
        targetValue = if (isPressed) -4f else 0f,
        animationSpec = tween(
            durationMillis = 220,
            easing = FastOutSlowInEasing
        )
    )
    val scope = rememberCoroutineScope()
    val cardScale = bounceScale.value * pressScale
    Card(
        modifier = Modifier
            .width(170.dp)
            .padding(8.dp)
            .graphicsLayer(
                scaleX = cardScale,
                scaleY = cardScale,
                rotationY = tilt
            )
            .clickable(
                interactionSource = interactionSource,
                enabled = !isLoading && !isAnimating
            ) {
                if (isLoading) return@clickable
                scope.launch {
                    if (isAnimating) return@launch
                    isAnimating = true
                    try {
                        bounceScale.animateTo(
                            targetValue = 0.92f,
                            animationSpec = tween(
                                durationMillis = 110,
                                easing = LinearOutSlowInEasing
                            )
                        )
                        bounceScale.animateTo(
                            targetValue = 1.05f,
                            animationSpec = tween(
                                durationMillis = 170,
                                easing = FastOutSlowInEasing
                            )
                        )
                        bounceScale.animateTo(
                            targetValue = 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMedium
                            )
                        )
                        navController.navigate("crop_list/$name")
                    } finally {
                        if (bounceScale.isRunning.not()) {
                            bounceScale.snapTo(1f)
                        }
                        isAnimating = false
                    }
                }
            },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(
            modifier = Modifier
                .height(200.dp)
                .background(
                    Brush.verticalGradient(
                        colors = gradient + listOf(gradient.last().copy(alpha = 0.8f))
                    )
                )
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(20.dp))

                // Floating Image with better shadow
                Card(
                    shape = CircleShape,
                    modifier = Modifier.size(100.dp),
                    elevation = CardDefaults.cardElevation(12.dp)
                ) {
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = name,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(Modifier.weight(1f))

                // Text Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color.White.copy(alpha = 0.95f),
                            RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                        )
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = name,
                        color = Color(0xFF2C3E50),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        color = Color.Gray,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryScreen(
    navController: NavController,
    category: dataPlants
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())   // <- IMPORTANT
            .background(Color.White)
            .padding(20.dp)
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Card(
                shape = CircleShape,
                modifier = Modifier.size(48.dp).clickable { navController.popBackStack() },
                colors = CardDefaults.cardColors(Color(0xFFEFEFEF)),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Back", tint = Color.Black)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Category",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        PlantGrid(
            plants = category.plants.map { it.diffName },
            navController = navController
        )
    }
}

@Composable
fun ExploreScreenContent(
    navController: NavController,
    farmViewModel: FarmViewModel
) {
    val categories by farmViewModel.categories.collectAsState()
    val isLoading by farmViewModel.isLoading.collectAsState()
    
    LaunchedEffect(Unit) {
        farmViewModel.loadCategories()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // Header with Gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF427EB3), Color(0xFF5B9BD5))
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Explore",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        
        // Content Section
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF427EB3))
            }
        } else if (categories.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.SearchOff,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray.copy(alpha = 0.5f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "No categories found",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    text = "Browse All Categories",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C3E50)
                )
                
                Spacer(Modifier.height(16.dp))
                
                // Categories Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.height((categories.size / 2 + 1) * 230.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    userScrollEnabled = false
                ) {
                    items(categories) { category ->
                        ExploreCategoryCard(
                            categoryName = category.name,
                            cropCount = category.crops.size,
                            onClick = { navController.navigate("crop_list/${category.name}") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExploreCategoryCard(
    categoryName: String,
    cropCount: Int,
    onClick: () -> Unit
) {
    val gradient = getCategoryGradient(categoryName)
    val icons = mapOf(
        "Cereal" to R.drawable.cereal,
        "Pulse" to R.drawable.pulse,
        "Oilseed" to R.drawable.oilseed,
        "Fiber" to R.drawable.fiber,
        "Sugar" to R.drawable.sugar,
        "Cash" to R.drawable.cash
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(gradient)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                icons[categoryName]?.let { iconRes ->
                    Image(
                        painter = painterResource(id = iconRes),
                        contentDescription = categoryName,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.3f))
                            .padding(12.dp)
                    )
                }
                
                Spacer(Modifier.height(12.dp))
                
                Text(
                    text = categoryName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Text(
                    text = "$cropCount crops",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
            
            // Decorative corner icon
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .size(24.dp)
            )
        }
    }
}




@Composable
fun CropListScreen(
    navController: NavController,
    categoryName: String,
    viewModel: FarmViewModel = viewModel()
) {
    val categories by viewModel.categories.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.loadCategories()
    }

    val category = categories.find { it.name == categoryName }
    val crops = category?.crops ?: emptyList()

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // Back button with category name
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        ) {
            Card(
                shape = CircleShape,
                modifier = Modifier
                    .size(48.dp)
                    .clickable { navController.popBackStack() },
                colors = CardDefaults.cardColors(Color(0xFFEFEFEF)),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Text(
                text = categoryName,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        // Loading state
        if (isLoading) {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF427EB3))
            }
            return@Column
        }

        // Empty state
        if (crops.isEmpty()) {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "No crops found in $categoryName",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }
            return@Column
        }

        // Crops Grid - Flipkart style (2 columns)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(crops) { crop ->
                CropCardItem(
                    crop = crop,
                    onClick = {
                        navController.navigate("crop_details/$categoryName/${crop.name}")
                    }
                )
            }
        }
    }
}


@Composable
fun CropCardItem(
    crop: Crop,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(Color.White)
    ) {
        Column {
// Crop Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(Color(0xFFF5F5F5))
            ) {
                AsyncImage(
                    model = crop.picURL,
                    contentDescription = crop.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = painterResource(R.drawable.cereal), // fallback
                    placeholder = painterResource(R.drawable.cereal)
                )
            }
            // Crop Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = crop.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black,
                    maxLines = 1
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "View Details →",
                    color = Color(0xFF427EB3),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun CropDetailsScreen(
    navController: NavController,
    category: String,
    crop: String,
    viewModel: FarmViewModel
) {
    val data by viewModel.cropDetails.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    
    // fetch when crop changes
    LaunchedEffect(crop) {
        viewModel.loadCropDetails(category, crop)
    }
    
    // --- LOADING SCREEN ---
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFF427EB3))
        }
        return
    }
    
    // --- FAILED FETCH ---
    if (data == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.Red.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(16.dp))
                Text("Failed to load crop details", color = Color.Red, fontSize = 16.sp)
            }
        }
        return
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // Hero Section with Image and Gradient Overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        ) {
            AsyncImage(
                model = data!!.picURL,
                contentDescription = data!!.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.6f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.8f)
                            )
                        )
                    )
            )
            
            // Back Button
            Card(
                shape = CircleShape,
                modifier = Modifier
                    .padding(16.dp)
                    .size(48.dp)
                    .align(Alignment.TopStart)
                    .clickable { navController.popBackStack() },
                colors = CardDefaults.cardColors(Color.White.copy(alpha = 0.9f)),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.KeyboardArrowLeft,
                        contentDescription = "Back",
                        tint = Color.Black,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            // Crop Name and Category
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(20.dp)
            ) {
                Text(
                    text = data!!.name,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = category,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 14.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = Color(0xFF427EB3)
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("About") },
                icon = { Icon(Icons.Default.Info, null, modifier = Modifier.size(20.dp)) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Diseases") },
                icon = { Icon(Icons.Default.BugReport, null, modifier = Modifier.size(20.dp)) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Care") },
                icon = { Icon(Icons.Default.Eco, null, modifier = Modifier.size(20.dp)) }
            )
        }
        
        // Tab Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            when (selectedTab) {
                0 -> AboutTabContent(data!!)
                1 -> DiseasesTabContent(data!!.diseases)
                2 -> CareTabContent()
            }
        }
    }
}

@Composable
fun AboutTabContent(crop: Crop) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "About ${crop.name}",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50)
            )
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                text = crop.about,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                color = Color(0xFF5A5A5A)
            )
            
            if (crop.regions.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                
                Text(
                    text = "Growing Regions",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C3E50)
                )
                
                Spacer(Modifier.height(12.dp))
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(40.dp),
                    userScrollEnabled = false
                ) {
                    items(crop.regions.size) { index ->
                        Surface(
                            color = Color(0xFF427EB3).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Color(0xFF427EB3).copy(alpha = 0.3f)
                            )
                        ) {
                            Text(
                                text = crop.regions[index],
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                fontSize = 14.sp,
                                color = Color(0xFF427EB3),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DiseasesTabContent(diseases: List<com.example.miniproject.core.data.model.Disease>) {
    if (diseases.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 40.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFF4CAF50).copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "No common diseases recorded",
                    color = Color.Gray,
                    fontSize = 16.sp
                )
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            diseases.forEach { disease ->
                DiseaseCard(disease)
            }
        }
    }
}

@Composable
fun DiseaseCard(disease: com.example.miniproject.core.data.model.Disease) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFFFF5722).copy(alpha = 0.1f),
                        shape = CircleShape,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.BugReport,
                                null,
                                tint = Color(0xFFFF5722),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    Spacer(Modifier.width(12.dp))
                    
                    Text(
                        text = disease.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C3E50)
                    )
                }
                
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = Color.Gray
                )
            }
            
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    // Symptoms
                    InfoSection(
                        title = "Symptoms",
                        content = disease.symptoms,
                        icon = Icons.Default.Visibility
                    )
                    
                    Spacer(Modifier.height(12.dp))
                    
                    // Cause
                    InfoSection(
                        title = "Cause",
                        content = disease.cause,
                        icon = Icons.Default.Science
                    )
                    
                    Spacer(Modifier.height(12.dp))
                    
                    // Solutions
                    Text(
                        text = "Solutions",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C3E50)
                    )
                    Spacer(Modifier.height(8.dp))
                    disease.solution.forEach { solution ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(20.dp).padding(top = 2.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = solution,
                                fontSize = 14.sp,
                                color = Color(0xFF5A5A5A),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InfoSection(title: String, content: String, icon: ImageVector) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                null,
                tint = Color(0xFF427EB3),
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50)
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = content,
            fontSize = 14.sp,
            color = Color(0xFF5A5A5A),
            lineHeight = 20.sp
        )
    }
}

@Composable
fun CareTabContent() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Eco,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color(0xFF4CAF50).copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Care Tips",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Detailed care instructions coming soon!",
                color = Color.Gray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}



fun getCategoryGradient(name: String): List<Color> {
    return when (name.lowercase()) {
        "cereal" -> listOf(Color(0xFFFFCC80), Color(0xFFFFB74D))
        "pulse" -> listOf(Color(0xFFDCE775), Color(0xFFCDDC39))
        "oilseed" -> listOf(Color(0xFFFFE082), Color(0xFFFFCA28))
        "fiber" -> listOf(Color(0xFFCE93D8), Color(0xFFBA68C8))
        "sugar" -> listOf(Color(0xFFFFAB91), Color(0xFFFF8A65))
        "cash" -> listOf(Color(0xFF90CAF9), Color(0xFF64B5F6))
        else -> listOf(Color(0xFFE0E0E0), Color(0xFFBDBDBD))
    }
}
@Composable
fun Modifier.shimmerEffect(): Modifier {
    val transition = rememberInfiniteTransition()
    val xShimmer by transition.animateFloat(
        initialValue = -300f,
        targetValue = 300f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing)
        )
    )

    return this.drawBehind {
        val shimmerBrush = Brush.linearGradient(
            colors = listOf(
                Color.LightGray.copy(alpha = 0.3f),
                Color.White.copy(alpha = 0.6f),
                Color.LightGray.copy(alpha = 0.3f)
            ),
            start = Offset(xShimmer, 0f),
            end = Offset(xShimmer + 300f, size.height)
        )
        drawRect(shimmerBrush)
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewHomePage() {
    val dummy = rememberNavController()
    HomePage(dummy, viewModel())
}
