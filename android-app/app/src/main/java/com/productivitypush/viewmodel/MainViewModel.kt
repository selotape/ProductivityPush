package com.productivitypush.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    private val _isBlocking = MutableLiveData<Boolean>(false)
    val isBlocking: LiveData<Boolean> = _isBlocking

    private val _dailyTasks = MutableLiveData<Int>(0)
    val dailyTasks: LiveData<Int> = _dailyTasks

    private val _motivationalMessage = MutableLiveData<String>()
    val motivationalMessage: LiveData<String> = _motivationalMessage

    private val motivationalMessages = listOf(
        "Every small step counts towards your big goals! 🎯",
        "You're building better habits one day at a time! 💪",
        "Focus today, succeed tomorrow! ⭐",
        "Your future self will thank you for staying productive! 🚀",
        "Small daily improvements lead to stunning results! 📈",
        "Stay focused, stay strong! 💯"
    )

    init {
        updateMotivationalMessage()
    }

    fun toggleBlocking(enabled: Boolean) {
        _isBlocking.value = enabled
    }

    fun addTask(taskDescription: String) {
        val currentTasks = _dailyTasks.value ?: 0
        _dailyTasks.value = currentTasks + 1
        updateMotivationalMessage()
    }

    fun completeTask() {
        val currentTasks = _dailyTasks.value ?: 0
        _dailyTasks.value = currentTasks + 1
        updateMotivationalMessage()
    }

    private fun updateMotivationalMessage() {
        val randomMessage = motivationalMessages.random()
        _motivationalMessage.value = randomMessage
    }

    fun resetDailyStats() {
        _dailyTasks.value = 0
        updateMotivationalMessage()
    }
}