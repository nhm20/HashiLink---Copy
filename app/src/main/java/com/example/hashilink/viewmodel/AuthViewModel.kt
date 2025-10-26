package com.example.hashilink.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hashilink.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _authResult = MutableLiveData<Pair<Boolean, String>>()
    val authResult: LiveData<Pair<Boolean, String>> = _authResult

    private val _userRole = MutableLiveData<String?>()
    val userRole: LiveData<String?> = _userRole

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading
    fun login(email: String, password: String) {
        _loading.value = true
        viewModelScope.launch {
            val result = repository.login(email, password)
            _authResult.postValue(Pair(result.isSuccess, result.exceptionOrNull()?.message ?: "Login completed"))
            result.getOrNull()?.role?.let { _userRole.postValue(it) }
            _loading.postValue(false)
        }
    }

    fun signup(email: String, password: String, name: String, mobile: String, role: String) {
        _loading.value = true
        viewModelScope.launch {
            val result = repository.signup(email, password, name, mobile, role)
            _authResult.postValue(Pair(result.isSuccess, result.exceptionOrNull()?.message ?: "Signup completed"))
            if (result.isSuccess) _userRole.postValue(role)
            _loading.postValue(false)
        }
    }

    fun logout() {
        repository.logout()
        _userRole.value = null
    }
}