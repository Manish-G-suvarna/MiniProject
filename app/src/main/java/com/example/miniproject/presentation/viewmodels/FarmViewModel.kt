package com.example.miniproject.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniproject.core.data.model.Category
import com.example.miniproject.core.data.model.Crop
import com.example.miniproject.core.data.repository.FarmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FarmViewModel(
    private val repo: FarmRepository = FarmRepository()
) : ViewModel() {
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> get() = _categories

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _cropDetails = MutableStateFlow<Crop?>(null)
    val cropDetails: StateFlow<Crop?> get() = _cropDetails

    fun loadCategories() = viewModelScope.launch {
        if (_categories.value.isNotEmpty()) return@launch // Already loaded

        _isLoading.value = true
        try {
            _categories.value = repo.getAllCategories()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _isLoading.value = false
        }
    }

    fun loadCropDetails(category: String, crop: String) = viewModelScope.launch {
        _isLoading.value = true
        try {
            _cropDetails.value = repo.getCropDetails(category, crop)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _isLoading.value = false
        }
    }
}