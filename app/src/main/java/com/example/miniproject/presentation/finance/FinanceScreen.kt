package com.example.miniproject.presentation.finance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.miniproject.core.auth.AuthViewModel
import com.example.miniproject.data.model.ExpenseCategories
import com.example.miniproject.data.model.ExpenseEntry
import com.example.miniproject.data.model.ProfitSummary
import com.example.miniproject.data.model.SaleEntry
import com.example.miniproject.presentation.viewmodels.FinanceViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    financeViewModel: FinanceViewModel = viewModel()
) {
    val selectedTab by financeViewModel.selectedTab.collectAsState()
    val expenses by financeViewModel.expenses.collectAsState()
    val sales by financeViewModel.sales.collectAsState()
    val profitSummaries by financeViewModel.profitSummaries.collectAsState()
    val totalExpenses by financeViewModel.totalExpenses.collectAsState()
    val totalRevenue by financeViewModel.totalRevenue.collectAsState()
    val totalProfit by financeViewModel.totalProfit.collectAsState()
    val isLoading by financeViewModel.isLoading.collectAsState()
    
    val userId = authViewModel.currentUser.value?.uid ?: ""
    
    // Load data on first composition
    LaunchedEffect(Unit) {
        financeViewModel.loadFinanceData(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Finance Tracker", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2196F3),
                    titleContentColor = Color.White
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
            // Summary Cards
            SummaryCards(
                totalExpenses = totalExpenses,
                totalRevenue = totalRevenue,
                totalProfit = totalProfit
            )
            
            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { financeViewModel.setSelectedTab(0) },
                    text = { Text("Add Expense") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { financeViewModel.setSelectedTab(1) },
                    text = { Text("Add Sale") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { financeViewModel.setSelectedTab(2) },
                    text = { Text("History") }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { financeViewModel.setSelectedTab(3) },
                    text = { Text("Profit") }
                )
            }
            
            // Tab Content
            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTab) {
                    0 -> AddExpenseTab(
                        userId = userId,
                        financeViewModel = financeViewModel
                    )
                    1 -> AddSaleTab(
                        userId = userId,
                        financeViewModel = financeViewModel
                    )
                    2 -> HistoryTab(
                        expenses = expenses,
                        sales = sales,
                        onDeleteExpense = { expenseId ->
                            financeViewModel.deleteExpense(userId, expenseId)
                        },
                        onDeleteSale = { saleId ->
                            financeViewModel.deleteSale(userId, saleId)
                        }
                    )
                    3 -> ProfitDashboardTab(profitSummaries = profitSummaries)
                }
                
                // Loading overlay
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCards(
    totalExpenses: Double,
    totalRevenue: Double,
    totalProfit: Double
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Expenses Card
        SummaryCard(
            title = "Expenses",
            amount = totalExpenses,
            color = Color(0xFFFF5722),
            modifier = Modifier.weight(1f)
        )
        
        // Revenue Card
        SummaryCard(
            title = "Revenue",
            amount = totalRevenue,
            color = Color(0xFF4CAF50),
            modifier = Modifier.weight(1f)
        )
        
        // Profit Card
        SummaryCard(
            title = "Profit",
            amount = totalProfit,
            color = if (totalProfit >= 0) Color(0xFF2196F3) else Color(0xFFF44336),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun SummaryCard(
    title: String,
    amount: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "₹${String.format("%.0f", amount)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun AddExpenseTab(
    userId: String,
    financeViewModel: FinanceViewModel
) {
    var cropName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ExpenseCategories.SEEDS) }
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Add New Expense",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Crop Name
                    OutlinedTextField(
                        value = cropName,
                        onValueChange = { cropName = it },
                        label = { Text("Crop Name") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.LocalFlorist, null) }
                    )
                    
                    // Category Selection
                    Text("Category", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ExpenseCategories.all.take(3).forEach { category ->
                            FilterChip(
                                selected = selectedCategory == category,
                                onClick = { selectedCategory = category },
                                label = { Text(category, fontSize = 12.sp) }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExpenseCategories.all.drop(3).forEach { category ->
                            FilterChip(
                                selected = selectedCategory == category,
                                onClick = { selectedCategory = category },
                                label = { Text(category, fontSize = 12.sp) }
                            )
                        }
                    }
                    
                    // Amount
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) amount = it },
                        label = { Text("Amount (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.CurrencyRupee, null) }
                    )
                    
                    // Notes
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4,
                        leadingIcon = { Icon(Icons.Default.Note, null) }
                    )
                    
                    // Save Button
                    Button(
                        onClick = {
                            if (cropName.isNotBlank() && amount.isNotBlank()) {
                                financeViewModel.addExpense(
                                    userId = userId,
                                    cropName = cropName,
                                    category = selectedCategory,
                                    amount = amount.toDoubleOrNull() ?: 0.0,
                                    notes = notes
                                ) {
                                    // Clear form
                                    cropName = ""
                                    amount = ""
                                    notes = ""
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                    ) {
                        Icon(Icons.Default.Save, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Expense")
                    }
                }
            }
        }
    }
}

@Composable
fun AddSaleTab(
    userId: String,
    financeViewModel: FinanceViewModel
) {
    var cropName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var pricePerUnit by remember { mutableStateOf("") }
    var buyer by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    
    val totalAmount = remember(quantity, pricePerUnit) {
        (quantity.toDoubleOrNull() ?: 0.0) * (pricePerUnit.toDoubleOrNull() ?: 0.0)
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Record Sale",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Crop Name
                    OutlinedTextField(
                        value = cropName,
                        onValueChange = { cropName = it },
                        label = { Text("Crop Name") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.LocalFlorist, null) }
                    )
                    
                    // Quantity
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) quantity = it },
                        label = { Text("Quantity (kg)") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Scale, null) }
                    )
                    
                    // Price Per Unit
                    OutlinedTextField(
                        value = pricePerUnit,
                        onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) pricePerUnit = it },
                        label = { Text("Price per Kg (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.CurrencyRupee, null) }
                    )
                    
                    // Total Amount (Calculated)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Total Amount:", fontWeight = FontWeight.Medium)
                            Text(
                                text = "₹${String.format("%.2f", totalAmount)}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2196F3)
                            )
                        }
                    }
                    
                    // Buyer
                    OutlinedTextField(
                        value = buyer,
                        onValueChange = { buyer = it },
                        label = { Text("Buyer (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Person, null) }
                    )
                    
                    // Notes
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4,
                        leadingIcon = { Icon(Icons.Default.Note, null) }
                    )
                    
                    // Save Button
                    Button(
                        onClick = {
                            if (cropName.isNotBlank() && quantity.isNotBlank() && pricePerUnit.isNotBlank()) {
                                financeViewModel.addSale(
                                    userId = userId,
                                    cropName = cropName,
                                    quantity = quantity.toDoubleOrNull() ?: 0.0,
                                    pricePerUnit = pricePerUnit.toDoubleOrNull() ?: 0.0,
                                    buyer = buyer,
                                    notes = notes
                                ) {
                                    // Clear form
                                    cropName = ""
                                    quantity = ""
                                    pricePerUnit = ""
                                    buyer = ""
                                    notes = ""
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Icon(Icons.Default.Save, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Sale")
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryTab(
    expenses: List<ExpenseEntry>,
    sales: List<SaleEntry>,
    onDeleteExpense: (String) -> Unit,
    onDeleteSale: (String) -> Unit
) {
    var showExpenses by remember { mutableStateOf(true) }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Toggle Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = showExpenses,
                onClick = { showExpenses = true },
                label = { Text("Expenses (${expenses.size})") },
                modifier = Modifier.weight(1f)
            )
            FilterChip(
                selected = !showExpenses,
                onClick = { showExpenses = false },
                label = { Text("Sales (${sales.size})") },
                modifier = Modifier.weight(1f)
            )
        }
        
        if (showExpenses) {
            if (expenses.isEmpty()) {
                EmptyHistoryState("No expenses recorded yet")
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(expenses.sortedByDescending { it.date }) { expense ->
                        ExpenseCard(expense = expense, onDelete = { onDeleteExpense(expense.id) })
                    }
                }
            }
        } else {
            if (sales.isEmpty()) {
                EmptyHistoryState("No sales recorded yet")
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sales.sortedByDescending { it.date }) { sale ->
                        SaleCard(sale = sale, onDelete = { onDeleteSale(sale.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun ExpenseCard(
    expense: ExpenseEntry,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon
            Surface(
                color = Color(0xFFFF5722).copy(alpha = 0.1f),
                shape = CircleShape,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.TrendingDown,
                        null,
                        tint = Color(0xFFFF5722),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.cropName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = expense.category,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = dateFormat.format(Date(expense.date)),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                if (expense.notes.isNotBlank()) {
                    Text(
                        text = expense.notes,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${String.format("%.2f", expense.amount)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF5722)
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Delete", tint = Color.Red)
                }
            }
        }
    }
}

@Composable
fun SaleCard(
    sale: SaleEntry,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon
            Surface(
                color = Color(0xFF4CAF50).copy(alpha = 0.1f),
                shape = CircleShape,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.TrendingUp,
                        null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sale.cropName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "${sale.quantity} kg @ ₹${sale.pricePerUnit}/kg",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = dateFormat.format(Date(sale.date)),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                if (sale.buyer.isNotBlank()) {
                    Text(
                        text = "Buyer: ${sale.buyer}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${String.format("%.2f", sale.totalAmount)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Delete", tint = Color.Red)
                }
            }
        }
    }
}

@Composable
fun ProfitDashboardTab(profitSummaries: List<ProfitSummary>) {
    if (profitSummaries.isEmpty()) {
        EmptyHistoryState("No data available. Add expenses and sales to see profit analysis.")
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(profitSummaries.sortedByDescending { it.netProfit }) { summary ->
                ProfitCard(summary = summary)
            }
        }
    }
}

@Composable
fun ProfitCard(summary: ProfitSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = summary.cropName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Surface(
                    color = if (summary.isProfit) Color(0xFF4CAF50) else Color(0xFFF44336),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (summary.isProfit) "PROFIT" else "LOSS",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Expenses", fontSize = 12.sp, color = Color.Gray)
                    Text(
                        text = "₹${String.format("%.2f", summary.totalExpenses)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF5722)
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Revenue", fontSize = 12.sp, color = Color.Gray)
                    Text(
                        text = "₹${String.format("%.2f", summary.totalRevenue)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50)
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text("Net Profit", fontSize = 12.sp, color = Color.Gray)
                    Text(
                        text = "₹${String.format("%.2f", summary.netProfit)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (summary.isProfit) Color(0xFF2196F3) else Color(0xFFF44336)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Progress Bar
            LinearProgressIndicator(
                progress = {
                    if (summary.totalRevenue > 0) {
                        (summary.totalExpenses / summary.totalRevenue).toFloat().coerceIn(0f, 1f)
                    } else 0f
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFFFF5722),
                trackColor = Color(0xFF4CAF50).copy(alpha = 0.3f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Profit Margin: ${String.format("%.1f", summary.profitPercentage)}%",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun EmptyHistoryState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.BarChart,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.Gray.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}
