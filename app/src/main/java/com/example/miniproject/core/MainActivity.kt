package com.example.miniproject.core

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.miniproject.core.data.repository.FarmRepository
import com.example.miniproject.core.navigation.MyAppNavigation
import com.example.miniproject.ui.theme.MiniProjectTheme
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        class TestFirebaseActivity : ComponentActivity() {
            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                // Test Firebase connection
                lifecycleScope.launch {
                    Log.d("TEST", "Starting Firebase test...")

                    val repo = FarmRepository()
                    val categories = repo.getAllCategories()

                    Log.d("TEST", "Result: ${categories.size} categories")
                    categories.forEach {
                        Log.d("TEST", "  - ${it.name}: ${it.crops.size} crops")
                    }
                }
            }
        }
        FirebaseApp.initializeApp(this)

        setContent {
            MiniProjectTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    MyAppNavigation(navController)
                }
            }
        }
    }
}