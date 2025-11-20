package com.example.miniproject.presentation.explore

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.miniproject.presentation.viewmodels.FarmViewModel

@Composable
fun ExploreScreen(navController: NavController, farmViewModel: FarmViewModel = viewModel()) {
    val categories = farmViewModel.categories.collectAsState().value

    Column(modifier = Modifier.fillMaxSize()) {
        if (categories.isEmpty()) {
            // Show a loading indicator or an empty state
            Text(text = "Loading categories...")
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.padding(8.dp)
            ) {
                items(categories) { category ->
                    Card(
                        modifier = Modifier
                            .padding(8.dp)
                            .clickable { navController.navigate("crop_list/${category.name}") }
                    ) {
                        Text(text = category.name, modifier = Modifier.padding(16.dp))
                    }
                }
            }
        }
    }
}
