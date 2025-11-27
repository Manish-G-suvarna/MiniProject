package com.example.miniproject.presentation.shop

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.miniproject.core.auth.AuthViewModel
import com.example.miniproject.data.model.CartItem
import com.example.miniproject.data.model.Product
import com.example.miniproject.presentation.viewmodels.ShopViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    shopViewModel: ShopViewModel = viewModel()
) {
    val products by shopViewModel.products.collectAsState()
    val cartItems by shopViewModel.cartItems.collectAsState()
    val isLoading by shopViewModel.isLoading.collectAsState()
    val selectedCategory by shopViewModel.selectedCategory.collectAsState()
    val searchQuery by shopViewModel.searchQuery.collectAsState()
    val cartItemCount by shopViewModel.cartItemCount.collectAsState()
    val totalPrice by shopViewModel.totalPrice.collectAsState()
    val orderSuccess by shopViewModel.orderSuccess.collectAsState()
    
    var showCart by remember { mutableStateOf(false) }
    var showCheckoutDialog by remember { mutableStateOf(false) }
    
    val categories = remember(products) { shopViewModel.getCategories() }
    
    // Filter products based on search and category
    val filteredProducts = remember(products, searchQuery, selectedCategory) {
        products.filter { product ->
            val matchesSearch = product.name.lowercase().contains(searchQuery.lowercase()) ||
                    product.description.lowercase().contains(searchQuery.lowercase())
            val matchesCategory = selectedCategory == null || product.category == selectedCategory
            matchesSearch && matchesCategory
        }
    }
    
    // Show success message
    LaunchedEffect(orderSuccess) {
        if (orderSuccess) {
            shopViewModel.resetOrderSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shop", fontWeight = FontWeight.Bold) },
                actions = {
                    // Cart button with badge
                    Box {
                        IconButton(onClick = { showCart = true }) {
                            Icon(Icons.Default.ShoppingCart, "Cart")
                        }
                        if (cartItemCount > 0) {
                            Badge(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                            ) {
                                Text(cartItemCount.toString())
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF4CAF50),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            // Search Bar
            SearchBar(
                query = searchQuery,
                onQueryChange = { shopViewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            
            // Category Filter
            if (categories.isNotEmpty()) {
                CategoryFilterRow(
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onCategorySelected = { shopViewModel.setSelectedCategory(it) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            // Products Grid
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF4CAF50))
                }
            } else if (filteredProducts.isEmpty()) {
                EmptyState(message = if (searchQuery.isNotEmpty()) "No products found" else "No products available")
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredProducts) { product ->
                        ProductCard(
                            product = product,
                            isInCart = cartItems.any { it.product.id == product.id },
                            onAddToCart = { shopViewModel.addToCart(product) }
                        )
                    }
                }
            }
        }
    }
    
    // Cart Bottom Sheet
    if (showCart) {
        CartBottomSheet(
            cartItems = cartItems,
            totalPrice = totalPrice,
            onDismiss = { showCart = false },
            onUpdateQuantity = { product, quantity ->
                shopViewModel.updateQuantity(product, quantity)
            },
            onRemoveItem = { product ->
                shopViewModel.removeFromCart(product)
            },
            onCheckout = {
                showCart = false
                showCheckoutDialog = true
            }
        )
    }
    
    // Checkout Dialog
    if (showCheckoutDialog) {
        CheckoutDialog(
            totalPrice = totalPrice,
            onDismiss = { showCheckoutDialog = false },
            onConfirmOrder = { address ->
                val userId = authViewModel.currentUser.value?.uid ?: ""
                shopViewModel.checkout(userId, address)
                showCheckoutDialog = false
            }
        )
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text("Search products...") },
        leadingIcon = { Icon(Icons.Default.Search, "Search") },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, "Clear")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF4CAF50),
            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
        )
    )
}

@Composable
fun CategoryFilterRow(
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // All categories chip
        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text("All") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF4CAF50),
                    selectedLabelColor = Color.White
                )
            )
        }
        
        items(categories) { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = { Text(category) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF4CAF50),
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    isInCart: Boolean,
    onAddToCart: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Product Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                // Market price badge (if different from selling price)
                if (product.marketPrice > 0 && product.marketPrice != product.pricePerKg) {
                    val discount = ((product.marketPrice - product.pricePerKg) / product.marketPrice * 100).toInt()
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                        color = Color(0xFFFF5722),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "$discount% OFF",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Product Name
                Text(
                    text = product. name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Category
                Text(
                    text = product.category,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Price
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "₹${product.pricePerKg}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                    Text(
                        text = "/${product.unit}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    
                    if (product.marketPrice > 0 && product.marketPrice != product.pricePerKg) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "₹${product.marketPrice}",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Add to Cart Button
                Button(
                    onClick = onAddToCart,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isInCart) Color.Gray else Color(0xFF4CAF50)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = if (isInCart) Icons.Default.Check else Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isInCart) "In Cart" else "Add to Cart", fontSize = 12.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartBottomSheet(
    cartItems: List<CartItem>,
    totalPrice: Double,
    onDismiss: () -> Unit,
    onUpdateQuantity: (Product, Int) -> Unit,
    onRemoveItem: (Product) -> Unit,
    onCheckout: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Shopping Cart",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "Close")
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            if (cartItems.isEmpty()) {
                EmptyState(message = "Your cart is empty")
            } else {
                // Cart Items
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                ) {
                    cartItems.forEach { cartItem ->
                        CartItemCard(
                            cartItem = cartItem,
                            onUpdateQuantity = { qty -> onUpdateQuantity(cartItem.product, qty) },
                            onRemove = { onRemoveItem(cartItem.product) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                
                Divider(modifier = Modifier.padding(vertical = 16.dp))
                
                // Total
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "₹${String.format("%.2f", totalPrice)}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Checkout Button
                Button(
                    onClick = onCheckout,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Proceed to Checkout", modifier = Modifier.padding(vertical = 8.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun CartItemCard(
    cartItem: CartItem,
    onUpdateQuantity: (Int) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Image
            AsyncImage(
                model = cartItem.product.imageUrl,
                contentDescription = cartItem.product.name,
                modifier = Modifier
                    .size(60.dp)
                   .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Product Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = cartItem.product.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "₹${cartItem.product.pricePerKg}/${cartItem.product.unit}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Subtotal: ₹${String.format("%.2f", cartItem.totalPrice)}",
                    fontSize = 12.sp,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Quantity Controls
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onUpdateQuantity(cartItem.quantity - 1) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Remove,
                        "Decrease",
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                Text(
                    text = "${cartItem.quantity}",
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .background(Color.White, CircleShape)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(
                    onClick = { onUpdateQuantity(cartItem.quantity + 1) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        "Increase",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            // Remove Button
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Delete,
                    "Remove",
                    tint = Color.Red
                )
            }
        }
    }
}

@Composable
fun CheckoutDialog(
    totalPrice: Double,
    onDismiss: () -> Unit,
    onConfirmOrder: (String) -> Unit
) {
    var deliveryAddress by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Checkout") },
        text = {
            Column {
                Text(
                    text = "Total Amount: ₹${String.format("%.2f", totalPrice)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = deliveryAddress,
                    onValueChange = { deliveryAddress = it },
                    label = { Text("Delivery Address") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (deliveryAddress.isNotBlank()) {
                        onConfirmOrder(deliveryAddress)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Text("Place Order")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EmptyState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.Gray.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                color = Color.Gray,
                fontSize = 16.sp
            )
        }
    }
}
