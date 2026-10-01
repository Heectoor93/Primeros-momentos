package com.example.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.BabyRepository
import com.example.data.AuthRepository
import com.example.data.FirestoreRepository

class BabyViewModelFactory(
    private val application: Application,
    private val repository: BabyRepository,
    private val authRepository: AuthRepository,
    private val firestoreRepository: FirestoreRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BabyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BabyViewModel(application, repository, authRepository, firestoreRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
