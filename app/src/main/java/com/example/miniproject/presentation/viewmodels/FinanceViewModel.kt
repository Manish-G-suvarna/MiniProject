package com.example.miniproject.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniproject.core.data.repository.FarmRepository
import com.example.miniproject.data.model.ExpenseEntry
import com.example.miniproject.data.model.ProfitSummary
import com.example.miniproject.data.model.SaleEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FinanceViewModel(
    private val repo: FarmRepository = FarmRepository()
) : ViewModel() {

    private val _expenses = MutableStateFlow<List<ExpenseEntry>>(emptyList())
    val expenses: StateFlow<List<ExpenseEntry>> = _expenses.asStateFlow()

    private val _sales = MutableStateFlow<List<SaleEntry>>(emptyList())
    val sales: StateFlow<List<SaleEntry>> = _sales.asStateFlow()

    private val _profitSummaries = MutableStateFlow<List<ProfitSummary>>(emptyList())
    val profitSummaries: StateFlow<List<ProfitSummary>> = _profitSummaries.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // Computed values
    val totalExpenses: StateFlow<Double> = MutableStateFlow(0.0).apply {
        viewModelScope.launch {
            expenses.collect { list ->
                value = list.sumOf { it.amount }
            }
        }
    }

    val totalRevenue: StateFlow<Double> = MutableStateFlow(0.0).apply {
        viewModelScope.launch {
            sales.collect { list ->
                value = list.sumOf { it.totalAmount }
            }
        }
    }

    val totalProfit: StateFlow<Double> = MutableStateFlow(0.0).apply {
        viewModelScope.launch {
            profitSummaries.collect { list ->
                value = list.sumOf { it.netProfit }
            }
        }
    }

    fun setSelectedTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun loadFinanceData(userId: String) = viewModelScope.launch {
        _isLoading.value = true
        try {
            // Load expenses
            _expenses.value = repo.getExpenses(userId)
            
            // Load sales
            _sales.value = repo.getSales(userId)
            
            // Calculate profit summaries
            _profitSummaries.value = repo.getProfitSummary(userId)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _isLoading.value = false
        }
    }

    fun addExpense(
        userId: String,
        cropName: String,
        category: String,
        amount: Double,
        notes: String,
        onSuccess: () -> Unit = {}
    ) = viewModelScope.launch {
        _isLoading.value = true
        try {
            val expense = ExpenseEntry(
                userId = userId,
                cropName = cropName,
                category = category,
                amount = amount,
                notes = notes,
                date = System.currentTimeMillis()
            )

            val success = repo.addExpense(expense)
            if (success) {
                loadFinanceData(userId)
                onSuccess()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _isLoading.value = false
        }
    }

    fun deleteExpense(userId: String, expenseId: String) = viewModelScope.launch {
        _isLoading.value = true
        try {
            val success = repo.deleteExpense(userId, expenseId)
            if (success) {
                loadFinanceData(userId)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _isLoading.value = false
        }
    }

    fun addSale(
        userId: String,
        cropName: String,
        quantity: Double,
        pricePerUnit: Double,
        buyer: String,
        notes: String,
        onSuccess: () -> Unit = {}
    ) = viewModelScope.launch {
        _isLoading.value = true
        try {
            val sale = SaleEntry(
                userId = userId,
                cropName = cropName,
                quantity = quantity,
                pricePerUnit = pricePerUnit,
                totalAmount = quantity * pricePerUnit,
                buyer = buyer,
                notes = notes,
                date = System.currentTimeMillis()
            )

            val success = repo.addSale(sale)
            if (success) {
                loadFinanceData(userId)
                onSuccess()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _isLoading.value = false
        }
    }

    fun deleteSale(userId: String, saleId: String) = viewModelScope.launch {
        _isLoading.value = true
        try {
            val success = repo.deleteSale(userId, saleId)
            if (success) {
                loadFinanceData(userId)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _isLoading.value = false
        }
    }

    // Get expenses grouped by crop
    fun getExpensesByCrop(): Map<String, List<ExpenseEntry>> {
        return _expenses.value.groupBy { it.cropName }
    }

    // Get sales grouped by crop
    fun getSalesByCrop(): Map<String, List<SaleEntry>> {
        return _sales.value.groupBy { it.cropName }
    }

    // Get unique crop names from expenses and sales
    fun getAllCropNames(): List<String> {
        val crops = (_expenses.value.map { it.cropName } + _sales.value.map { it.cropName }).distinct()
        return crops.sorted()
    }
}
