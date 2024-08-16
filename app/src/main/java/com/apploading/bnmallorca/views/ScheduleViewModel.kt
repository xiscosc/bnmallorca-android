package com.apploading.bnmallorca.views

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apploading.bnmallorca.bncore.BnApi
import com.apploading.bnmallorca.bncore.Day
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor() : ViewModel() {

    private val _schedule = MutableStateFlow<List<Day>>(emptyList())
    val schedule: StateFlow<List<Day>> get() = _schedule

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> get() = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> get() = _errorMessage

    fun loadSchedule() {
        viewModelScope.launch {
            _errorMessage.value = null
            try {
                _isLoading.value = true
                val api = BnApi.build()
                val response = api.getSchedule()
                _schedule.value = response.days
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}