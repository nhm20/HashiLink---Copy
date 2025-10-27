package com.example.hashilink.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hashilink.data.model.Product
import com.example.hashilink.data.repository.ProductRepository
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {
    private val repository = ProductRepository.getInstance()

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // New: LiveData for add product result
    private val _addResult = MutableLiveData<Result<String>?>()
    val addResult: LiveData<Result<String>?> = _addResult

    fun loadSellerProducts(sellerId: String) {
        _loading.value = true
        viewModelScope.launch {
            val result = repository.getSellerProducts(sellerId)
            if (result.isSuccess) {
                _products.postValue(result.getOrNull() ?: emptyList())
                _error.postValue(null)
            } else {
                _error.postValue(result.exceptionOrNull()?.message ?: "Failed to load products")
            }
            _loading.postValue(false)
        }
    }

    // New: addProduct uses the same repository and viewModelScope
    fun addProduct(name: String, description: String, price: Double, quantity: Int) {
        _loading.value = true
        viewModelScope.launch {
            val res = try {
                repository.addProduct(name, description, price, quantity)
            } catch (e: Exception) {
                Result.failure<String>(e)
            }
            _addResult.postValue(res)
            _loading.postValue(false)
        }
    }

    // New: Method to load all products
    fun loadAllProducts() {
        _loading.value = true
        viewModelScope.launch {
            val result = repository.getAllProducts()
            if (result.isSuccess) {
                _products.postValue(result.getOrNull() ?: emptyList())
                _error.postValue(null)
            } else {
                _error.postValue(result.exceptionOrNull()?.message ?: "Failed to load products")
            }
            _loading.postValue(false)
        }
    }

    // Helper for UI to clear the add result after handling
    fun clearAddResult() {
        _addResult.value = null
    }
}