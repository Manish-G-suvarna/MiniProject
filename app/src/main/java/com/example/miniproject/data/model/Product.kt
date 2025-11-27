package com.example.miniproject.data.model

// Shop/E-commerce models
data class Product(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val imageUrl: String = "",
    val pricePerKg: Double = 0.0,
    val unit: String = "kg",
    val stockAvailable: Int = 0,
    val seller: String = "",
    val description: String = "",
    val marketPrice: Double = 0.0,
    val region: String = ""
)

data class CartItem(
    val product: Product,
    val quantity: Int = 1
) {
    val totalPrice: Double
        get() = product.pricePerKg * quantity
}

data class Order(
    val id: String = "",
    val userId: String = "",
    val items: List<CartItem> = emptyList(),
    val totalPrice: Double = 0.0,
    val date: Long = System.currentTimeMillis(),
    val status: String = "pending", // pending, confirmed, delivered
    val deliveryAddress: String = ""
)

// Finance tracking models
data class ExpenseEntry(
    val id: String = "",
    val userId: String = "",
    val cropName: String = "",
    val category: String = "", // seeds, fertilizer, labor, equipment, pesticides, other
    val amount: Double = 0.0,
    val date: Long = System.currentTimeMillis(),
    val notes: String = ""
)

data class SaleEntry(
    val id: String = "",
    val userId: String = "",
    val cropName: String = "",
    val quantity: Double = 0.0,
    val pricePerUnit: Double = 0.0,
    val totalAmount: Double = 0.0,
    val date: Long = System.currentTimeMillis(),
    val buyer: String = "",
    val notes: String = ""
)

data class Transaction(
    val id: String = "",
    val userId: String = "",
    val type: String = "", // "expense" or "sale"
    val cropName: String = "",
    val amount: Double = 0.0,
    val quantity: Double = 0.0,
    val date: Long = System.currentTimeMillis(),
    val category: String = "",
    val notes: String = ""
)

data class ProfitSummary(
    val cropName: String = "",
    val totalExpenses: Double = 0.0,
    val totalRevenue: Double = 0.0,
    val netProfit: Double = 0.0,
    val profitPercentage: Double = 0.0,
    val isProfit: Boolean = true
) {
    companion object {
        fun calculate(cropName: String, expenses: List<ExpenseEntry>, sales: List<SaleEntry>): ProfitSummary {
            val totalExpenses = expenses.filter { it.cropName == cropName }.sumOf { it.amount }
            val totalRevenue = sales.filter { it.cropName == cropName }.sumOf { it.totalAmount }
            val netProfit = totalRevenue - totalExpenses
            val profitPercentage = if (totalExpenses > 0) (netProfit / totalExpenses) * 100 else 0.0
            
            return ProfitSummary(
                cropName = cropName,
                totalExpenses = totalExpenses,
                totalRevenue = totalRevenue,
                netProfit = netProfit,
                profitPercentage = profitPercentage,
                isProfit = netProfit >= 0
            )
        }
    }
}

// Expense categories enum for consistency
object ExpenseCategories {
    const val SEEDS = "Seeds"
    const val FERTILIZER = "Fertilizer"
    const val PESTICIDES = "Pesticides"
    const val LABOR = "Labor"
    const val EQUIPMENT = "Equipment"
    const val IRRIGATION = "Irrigation"
    const val OTHER = "Other"
    
    val all = listOf(SEEDS, FERTILIZER, PESTICIDES, LABOR, EQUIPMENT, IRRIGATION, OTHER)
}
