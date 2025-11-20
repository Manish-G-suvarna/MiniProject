package com.example.miniproject.core.data.repository
import android.util.Log
import com.example.miniproject.core.data.model.Category
import com.example.miniproject.core.data.model.Crop
import com.example.miniproject.core.data.model.Disease
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
class FarmRepository {
    // Change from Firestore to Realtime Database
    private val database = FirebaseDatabase.getInstance()
    private val TAG = "FarmRepository"
    /**
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
        }
}