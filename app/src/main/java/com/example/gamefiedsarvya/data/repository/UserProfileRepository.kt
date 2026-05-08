package com.example.gamefiedsarvya.data.repository

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.gamefiedsarvya.data.models.*
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.profileDataStore by preferencesDataStore(name = "sarvya_user_profile")

class UserProfileRepository(private val context: Context) {

    private val gson = Gson()
    private val PROFILE_KEY = stringPreferencesKey("user_profile")

    val profileFlow: Flow<UserProfile> = context.profileDataStore.data.map { prefs ->
        prefs[PROFILE_KEY]?.let {
            try { gson.fromJson(it, UserProfile::class.java) }
            catch (e: Exception) { UserProfile() }
        } ?: UserProfile()
    }

    suspend fun saveProfile(profile: UserProfile) {
        context.profileDataStore.edit { prefs ->
            prefs[PROFILE_KEY] = gson.toJson(profile)
        }
    }

    suspend fun updateName(name: String) {
        val current = getProfile()
        saveProfile(current.copy(name = name.trim()))
    }

    suspend fun completeOnboarding(profile: UserProfile) {
        saveProfile(profile.copy(onboardingComplete = true))
    }

    private suspend fun getProfile(): UserProfile =
        context.profileDataStore.data.first()[PROFILE_KEY]?.let {
            try { gson.fromJson(it, UserProfile::class.java) }
            catch (e: Exception) { UserProfile() }
        } ?: UserProfile()
}
