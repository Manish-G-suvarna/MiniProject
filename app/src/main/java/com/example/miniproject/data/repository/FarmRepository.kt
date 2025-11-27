     * Fetches all categories from Firebase Realtime Database
     * Path: /categories/[0,1,2,3,4,5]
     */
    suspend fun getAllCategories(): List<Category> = suspendCancellableCoroutine { continuation ->
        Log.d(TAG, "========== STARTING getAllCategories (Realtime DB) ==========")

        val categoriesRef = database.getReference("categories")
        Log.d(TAG, "Reference path: ${categoriesRef.path}")

        categoriesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    Log.d(TAG, "✅ Data received!")
                    Log.d(TAG, "Snapshot exists: ${snapshot.exists()}")
                    Log.d(TAG, "Children count: ${snapshot.childrenCount}")

                    if (!snapshot.exists()) {
                        Log.e(TAG, "❌ Categories node doesn't exist!")
                        continuation.resume(emptyList())
                        return
                    }

                    val categories = mutableListOf<Category>()

                    // Iterate through each category (0, 1, 2, 3, 4, 5)
                    snapshot.children.forEach { categorySnapshot ->
                        try {
                            Log.d(TAG, "--- Processing category: ${categorySnapshot.key} ---")

                            val name = categorySnapshot.child("name").getValue(String::class.java)
                            Log.d(TAG, "Category name: $name")

                            if (name == null) {
                                Log.e(TAG, "❌ Name is null for ${categorySnapshot.key}")
                                return@forEach
                            }

                            // Get crops array
                            val cropsSnapshot = categorySnapshot.child("crops")
                            val crops = mutableListOf<Crop>()

                            Log.d(TAG, "Crops count: ${cropsSnapshot.childrenCount}")

                            cropsSnapshot.children.forEach { cropSnapshot ->
                                try {
                                    val cropName = cropSnapshot.child("name").getValue(String::class.java) ?: ""
                                    val picURL = cropSnapshot.child("picURL").getValue(String::class.java) ?: ""
                                    val about = cropSnapshot.child("about").getValue(String::class.java) ?: ""

                                    // Get regions array
                                    val regions = mutableListOf<String>()
                                    cropSnapshot.child("regions").children.forEach {
                                        it.getValue(String::class.java)?.let { region -> regions.add(region) }
                                    }

                                    Log.d(TAG, "  Crop: $cropName")

                                    crops.add(
                                        Crop(
                                            name = cropName,
                                            picURL = picURL,
                                            regions = regions,
                                            about = about,
                                            diseases = emptyList() // Load later for performance
                                        )
                                    )
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error parsing crop: ${e.message}")
                                }
                            }

                            categories.add(Category(name = name, crops = crops))
                            Log.d(TAG, "✅ Added category: $name with ${crops.size} crops")

                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing category ${categorySnapshot.key}: ${e.message}")
                        }
                    }

                    Log.d(TAG, "========== COMPLETED ==========")
                    Log.d(TAG, "Total categories: ${categories.size}")
                    continuation.resume(categories)

                } catch (e: Exception) {
                    Log.e(TAG, "❌ Fatal error: ${e.message}")
                    e.printStackTrace()
                    continuation.resumeWithException(e)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "❌ Database error: ${error.message}")
                continuation.resumeWithException(error.toException())
            }
        })
    }

    /**
     * Fetches detailed crop info including diseases
     */
    suspend fun getCropDetails(categoryName: String, cropName: String): Crop? =
        suspendCancellableCoroutine { continuation ->
            Log.d(TAG, "========== getCropDetails (Realtime DB) ==========")
            Log.d(TAG, "Searching: $categoryName -> $cropName")

            val categoriesRef = database.getReference("categories")

            categoriesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        // Find the category
                        var foundCrop: Crop? = null

                        snapshot.children.forEach { categorySnapshot ->
                            val name = categorySnapshot.child("name").getValue(String::class.java)

                            if (name == categoryName) {
                                Log.d(TAG, "✅ Found category: $categoryName")

                                // Find the crop
                                categorySnapshot.child("crops").children.forEach { cropSnapshot ->
                                    val cName = cropSnapshot.child("name").getValue(String::class.java)

                                    if (cName == cropName) {
                                        Log.d(TAG, "✅ Found crop: $cropName")

                                        // Parse full crop with diseases
                                        val picURL = cropSnapshot.child("picURL").getValue(String::class.java) ?: ""
                                        val about = cropSnapshot.child("about").getValue(String::class.java) ?: ""

                                        // Get regions
                                        val regions = mutableListOf<String>()
                                        cropSnapshot.child("regions").children.forEach {
                                            it.getValue(String::class.java)?.let { region -> regions.add(region) }
                                        }

                                        // Get diseases
                                        val diseases = mutableListOf<Disease>()
                                        cropSnapshot.child("diseases").children.forEach { diseaseSnapshot ->
                                            val diseaseName = diseaseSnapshot.child("name").getValue(String::class.java) ?: ""
                                            val symptoms = diseaseSnapshot.child("symptoms").getValue(String::class.java) ?: ""
                                            val cause = diseaseSnapshot.child("cause").getValue(String::class.java) ?: ""

                                            // Get solutions array
                                            val solutions = mutableListOf<String>()
                                            diseaseSnapshot.child("solution").children.forEach {
                                                it.getValue(String::class.java)?.let { sol -> solutions.add(sol) }
                                            }

                                            diseases.add(
                                                Disease(
                                                    name = diseaseName,
                                                    symptoms = symptoms,
                                                    cause = cause,
                                                    solution = solutions
                                                )
                                            )
                                        }

                                        foundCrop = Crop(
                                            name = cName,
                                            picURL = picURL,
                                            regions = regions,
                                            about = about,
                                            diseases = diseases
                                        )

                                        Log.d(TAG, "✅ Loaded crop with ${diseases.size} diseases")
                                        return@forEach
                                    }
                                }
                            }
                        }

                        continuation.resume(foundCrop)

                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Error: ${e.message}")
                        continuation.resumeWithException(e)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "❌ Database error: ${error.message}")
                    continuation.resumeWithException(error.toException())
                }
            })

    // =============== SHOP/PRODUCTS METHODS ===============
    
    /**
     * Fetches all products from /products path
     */
    suspend fun getAllProducts(): List<Product> = suspendCancellableCoroutine { continuation ->
        Log.d(TAG, "========== getAllProducts (Realtime DB) ==========")
        
        val productsRef = database.getReference("products")
        
        productsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val products = mutableListOf<Product>()
                    
                    snapshot.children.forEach { productSnapshot ->
                        try {
                            val product = productSnapshot.getValue(Product::class.java)
                            product?.let { products.add(it) }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing product: ${e.message}")
                        }
                    }
                    
                    Log.d(TAG, "✅ Loaded ${products.size} products")
                    continuation.resume(products)
                    
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error loading products: ${e.message}")
                    continuation.resumeWithException(e)
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "❌ Database error: ${error.message}")
                continuation.resumeWithException(error.toException())
            }
        })
    }
    
    /**
     * Save order to Firebase
     */
    suspend fun saveOrder(order: Order): Boolean = suspendCancellableCoroutine { continuation ->
        val orderRef = database.getReference("users/${order.userId}/orders").push()
        val orderWithId = order.copy(id = orderRef.key ?: "")
        
        orderRef.setValue(orderWithId)
            .addOnSuccessListener {
                Log.d(TAG, "✅ Order saved: ${orderWithId.id}")
                continuation.resume(true)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Error saving order: ${e.message}")
                continuation.resume(false)
            }
    }

    // =============== EXPENSE TRACKING METHODS ===============
    
    /**
     * Add expense entry
     */
    suspend fun addExpense(expense: ExpenseEntry): Boolean = suspendCancellableCoroutine { continuation ->
        val expenseRef = database.getReference("users/${expense.userId}/expenses").push()
        val expenseWithId = expense.copy(id = expenseRef.key ?: "")
        
        expenseRef.setValue(expenseWithId)
            .addOnSuccessListener {
                Log.d(TAG, "✅ Expense saved: ${expenseWithId.id}")
                continuation.resume(true)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Error saving expense: ${e.message}")
                continuation.resume(false)
            }
    }
    
    /**
     * Get all expenses for a user
     */
    suspend fun getExpenses(userId: String): List<ExpenseEntry> = suspendCancellableCoroutine { continuation ->
        val expensesRef = database.getReference("users/$userId/expenses")
        
        expensesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val expenses = mutableListOf<ExpenseEntry>()
                    
                    snapshot.children.forEach { expenseSnapshot ->
                        try {
                            val expense = expenseSnapshot.getValue(ExpenseEntry::class.java)
                            expense?.let { expenses.add(it) }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing expense: ${e.message}")
                        }
                    }
                    
                    Log.d(TAG, "✅ Loaded ${expenses.size} expenses")
                    continuation.resume(expenses)
                    
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error loading expenses: ${e.message}")
                    continuation.resumeWithException(e)
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "❌ Database error: ${error.message}")
                continuation.resumeWithException(error.toException())
            }
        })
    }
    
    /**
     * Delete expense
     */
    suspend fun deleteExpense(userId: String, expenseId: String): Boolean = suspendCancellableCoroutine { continuation ->
        database.getReference("users/$userId/expenses/$expenseId")
            .removeValue()
            .addOnSuccessListener {
                Log.d(TAG, "✅ Expense deleted: $expenseId")
                continuation.resume(true)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Error deleting expense: ${e.message}")
                continuation.resume(false)
            }
    }

    // =============== SALES TRACKING METHODS ===============
    
    /**
     * Add sale entry
     */
    suspend fun addSale(sale: SaleEntry): Boolean = suspendCancellableCoroutine { continuation ->
        val saleRef = database.getReference("users/${sale.userId}/sales").push()
        val saleWithId = sale.copy(id = saleRef.key ?: "")
        
        saleRef.setValue(saleWithId)
            .addOnSuccessListener {
                Log.d(TAG, "✅ Sale saved: ${saleWithId.id}")
                continuation.resume(true)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Error saving sale: ${e.message}")
                continuation.resume(false)
            }
    }
    
    /**
     * Get all sales for a user
     */
    suspend fun getSales(userId: String): List<SaleEntry> = suspendCancellableCoroutine { continuation ->
        val salesRef = database.getReference("users/$userId/sales")
        
        salesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val sales = mutableListOf<SaleEntry>()
                    
                    snapshot.children.forEach { saleSnapshot ->
                        try {
                            val sale = saleSnapshot.getValue(SaleEntry::class.java)
                            sale?.let { sales.add(it) }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing sale: ${e.message}")
                        }
                    }
                    
                    Log.d(TAG, "✅ Loaded ${sales.size} sales")
                    continuation.resume(sales)
                    
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error loading sales: ${e.message}")
                    continuation.resumeWithException(e)
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "❌ Database error: ${error.message}")
                continuation.resumeWithException(error.toException())
            }
        })
    }
    
    /**
     * Delete sale
     */
    suspend fun deleteSale(userId: String, saleId: String): Boolean = suspendCancellableCoroutine { continuation ->
        database.getReference("users/$userId/sales/$saleId")
            .removeValue()
            .addOnSuccessListener {
                Log.d(TAG, "✅ Sale deleted: $saleId")
                continuation.resume(true)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Error deleting sale: ${e.message}")
                continuation.resume(false)
            }
    }

    // =============== PROFIT CALCULATION METHODS ===============
    
    /**
     * Calculate profit summary for all crops
     */
    suspend fun getProfitSummary(userId: String): List<ProfitSummary> = suspendCancellableCoroutine { continuation ->
        // We need both expenses and sales, so we'll make concurrent calls
        val expensesRef = database.getReference("users/$userId/expenses")
        val salesRef = database.getReference("users/$userId/sales")
        
        var expenses: List<ExpenseEntry>? = null
        var sales: List<SaleEntry>? = null
        
        fun checkAndCalculate() {
            if (expenses != null && sales != null) {
                // Get unique crop names
                val cropNames = (expenses!!.map { it.cropName } + sales!!.map { it.cropName }).distinct()
                
                val summaries = cropNames.map { cropName ->
                    ProfitSummary.calculate(cropName, expenses!!, sales!!)
                }.filter { it.totalExpenses > 0 || it.totalRevenue > 0 } // Only show crops with activity
                
                Log.d(TAG, "✅ Calculated profit for ${summaries.size} crops")
                continuation.resume(summaries)
            }
        }
        
        // Load expenses
        expensesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<ExpenseEntry>()
                snapshot.children.forEach { expenseSnapshot ->
                    expenseSnapshot.getValue(ExpenseEntry::class.java)?.let { list.add(it) }
                }
                expenses = list
                checkAndCalculate()
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "❌ Error loading expenses: ${error.message}")
                continuation.resumeWithException(error.toException())
            }
        })
        
        // Load sales
        salesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<SaleEntry>()
                snapshot.children.forEach { saleSnapshot ->
                    saleSnapshot.getValue(SaleEntry::class.java)?.let { list.add(it) }
                }
                sales = list
                checkAndCalculate()
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "❌ Error loading sales: ${error.message}")
                continuation.resumeWithException(error.toException())
            }
        })
    }
        }
}