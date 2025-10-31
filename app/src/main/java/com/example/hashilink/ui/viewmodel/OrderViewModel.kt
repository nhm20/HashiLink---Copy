package com.example.hashilink.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hashilink.data.model.Order
import com.example.hashilink.data.repository.OrderRepository
import kotlinx.coroutines.launch

class OrderViewModel : ViewModel() {
    private val repository = OrderRepository.getInstance()

    private val _placeOrderResult = MutableLiveData<Result<String>>()
    val placeOrderResult: LiveData<Result<String>> = _placeOrderResult

    private val _buyerOrders = MutableLiveData<Result<List<Order>>>()
    val buyerOrders: LiveData<Result<List<Order>>> = _buyerOrders

    private val _sellerOrders = MutableLiveData<Result<List<Order>>>()
    val sellerOrders: LiveData<Result<List<Order>>> = _sellerOrders

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    fun placeOrder(
        productId: String,
        productName: String,
        productPrice: Double,
        productImageUrl: String,
        quantity: Int,
        sellerId: String
    ) {
        _loading.value = true
        viewModelScope.launch {
            val result = try {
                repository.placeOrder(productId, productName, productPrice, productImageUrl, quantity, sellerId)
            } catch (e: Exception) {
                Result.failure<String>(e)
            }
            _placeOrderResult.postValue(result)
            _loading.postValue(false)
        }
    }

    fun loadBuyerOrders(buyerId: String) {
        _loading.value = true
        viewModelScope.launch {
            val result = try {
                repository.getBuyerOrders(buyerId)
            } catch (e: Exception) {
                Result.failure<List<Order>>(e)
            }
            _buyerOrders.postValue(result)
            _loading.postValue(false)
        }
    }

    fun loadSellerOrders(sellerId: String) {
        _loading.value = true
        viewModelScope.launch {
            val result = try {
                repository.getSellerOrders(sellerId)
            } catch (e: Exception) {
                Result.failure<List<Order>>(e)
            }
            _sellerOrders.postValue(result)
            _loading.postValue(false)
        }
    }

    fun updateOrderStatus(orderId: String, status: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, status)
        }
    }
}
