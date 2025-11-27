package com.example.miniproject.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniproject.data.repository.FarmRepository
import com.example.miniproject.data.model.CartItem
import com.example.miniproject.data.model.Order
import com.example.miniproject.data.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ShopViewModel(
    private val repo: FarmRepository = FarmRepository()
) : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _orderSuccess = MutableStateFlow(false)
    val orderSuccess: StateFlow<Boolean> = _orderSuccess.asStateFlow()

    // Computed properties
    val filteredProducts: StateFlow<List<Product>> = MutableStateFlow<List<Product>>(emptyList()).apply {
        viewModelScope.launch {
            products.collect { allProducts ->
                val query = searchQuery.value.lowercase()
                val category = selectedCategory.value

                value = allProducts.filter { product ->
                    val matchesSearch = product.name.lowercase().contains(query) ||
                            product.description.lowercase().contains(query)
                    val matchesCategory = category == null || product.category == category

                    matchesSearch && matchesCategory
                }
            }
        }
    }

    val totalPrice: StateFlow<Double> = MutableStateFlow(0.0).apply {
        viewModelScope.launch {
            cartItems.collect { items ->
                value = items.sumOf { it.totalPrice }
            }
        }
    }

    val cartItemCount: StateFlow<Int> = MutableStateFlow(0).apply {
        viewModelScope.launch {
            cartItems.collect { items ->
                value = items.sumOf { it.quantity }
            }
        }
    }

    init {
        loadProducts()
    }

    fun loadProducts() = viewModelScope.launch {
        _isLoading.value = true
        try {
            _products.value = repo.getAllProducts()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _isLoading.value = false
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun addToCart(product: Product) {
        val currentCart = _cartItems.value.toMutableList()
        val existingItem = currentCart.find { it.product.id == product.id }

        if (existingItem != null) {
            // Increase quantity
            val index = currentCart.indexOf(existingItem)
            currentCart[index] = existingItem.copy(quantity = existingItem.quantity + 1)
        } else {
            // Add new item
            currentCart.add(CartItem(product, 1))
        }

        _cartItems.value = currentCart
    }

    fun removeFromCart(product: Product) {
        val currentCart = _cartItems.value.toMutableList()
        currentCart.removeAll { it.product.id == product.id }
        _cartItems.value = currentCart
    }

    fun updateQuantity(product: Product, quantity: Int) {
        if (quantity <= 0) {
            removeFromCart(product)
            return
        }

        val currentCart = _cartItems.value.toMutableList()
        val existingItem = currentCart.find { it.product.id == product.id }

        if (existingItem != null) {
            val index = currentCart.indexOf(existingItem)
            currentCart[index] = existingItem.copy(quantity = quantity)
            _cartItems.value = currentCart
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    fun checkout(userId: String, deliveryAddress: String) = viewModelScope.launch {
        _isLoading.value = true
        try {
            val order = Order(
                userId = userId,
                items = _cartItems.value,
                totalPrice = totalPrice.value,
                deliveryAddress = deliveryAddress
            )

            val success = repo.saveOrder(order)
            if (success) {
                clearCart()
                _orderSuccess.value = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _isLoading.value = false
        }
    }

    fun resetOrderSuccess() {
        _orderSuccess.value = false
    }

    // Get unique categories from products
    fun getCategories(): List<String> {
        return _products.value.map { it.category }.distinct().sorted()
    }
}
