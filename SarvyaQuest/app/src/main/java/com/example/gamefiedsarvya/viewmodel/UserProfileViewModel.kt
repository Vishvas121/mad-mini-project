package com.example.gamefiedsarvya.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gamefiedsarvya.data.models.*
import com.example.gamefiedsarvya.data.repository.UserProfileRepository
import com.example.gamefiedsarvya.network.GroqService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProfileUiState(
    val profile: UserProfile = UserProfile(),
    val isLoading: Boolean = true,
    val personalisedGreeting: String = "",
    val isGreetingLoading: Boolean = false,
    val saveSuccess: Boolean = false
)

class UserProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UserProfileRepository(application)

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    val profile: StateFlow<UserProfile> = _uiState.map { it.profile }.stateIn(
        viewModelScope, SharingStarted.Eagerly, UserProfile()
    )

    init {
        viewModelScope.launch {
            repository.profileFlow.collect { profile ->
                _uiState.update { it.copy(profile = profile, isLoading = false) }
            }
        }
    }

    fun saveProfile(profile: UserProfile) {
        viewModelScope.launch {
            repository.saveProfile(profile)
            _uiState.update { it.copy(saveSuccess = true) }
        }
    }

    fun updateName(name: String) {
        viewModelScope.launch {
            val updated = _uiState.value.profile.copy(name = name.trim())
            repository.saveProfile(updated)
        }
    }

    fun completeOnboarding(profile: UserProfile) {
        viewModelScope.launch {
            repository.completeOnboarding(profile)
        }
    }

    fun loadPersonalisedGreeting(tier: LearningTier, level: Int, accuracy: Float) {
        val profile = _uiState.value.profile
        if (profile.name.isBlank()) return

        _uiState.update { it.copy(isGreetingLoading = true) }
        viewModelScope.launch {
            val greeting = GroqService.getPersonalisedGreeting(
                playerName = profile.displayName,
                tier       = tier,
                level      = level,
                accuracy   = accuracy
            ) ?: profile.greeting   // fallback to local greeting

            _uiState.update { it.copy(
                personalisedGreeting = greeting,
                isGreetingLoading    = false
            )}
        }
    }

    fun clearSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    val isOnboardingComplete: Boolean
        get() = _uiState.value.profile.isSetup
}
