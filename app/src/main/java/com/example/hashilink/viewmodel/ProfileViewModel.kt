package com.example.hashilink.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hashilink.data.model.User
import com.example.hashilink.data.repository.AuthRepository
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _userProfile = MutableLiveData<User?>()
    val userProfile: LiveData<User?> = _userProfile

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadUserProfile(uid: String) {
        _loading.value = true
        viewModelScope.launch {
            val result = repository.getUserProfile(uid)
            if (result.isSuccess) {
                _userProfile.postValue(result.getOrNull())
                _error.postValue(null)
            } else {
                _error.postValue(result.exceptionOrNull()?.message ?: "Failed to load profile")
            }
            _loading.postValue(false)
        }
    }
}