package com.bangkit.factha.data.remote

import android.util.Log
import com.bangkit.factha.data.network.ApiServiceAuth
import com.bangkit.factha.data.preference.UserPreferences
import com.bangkit.factha.data.response.LoginResponse
import com.bangkit.factha.data.response.RegisterResponse
import com.bangkit.factha.data.helper.Result
import kotlinx.coroutines.flow.Flow

class AuthRepository(
    private val apiService: ApiServiceAuth,
    private val userPreferences: UserPreferences
) {
    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val response = apiService.login(email, password)
            if (response.isSuccessful) {
                val loginResponse = response.body()
                if (loginResponse?.data != null) {
                    loginResponse.data.token?.let { token ->
                        userPreferences.saveToken(token)
                        Result.Success(loginResponse)
                    } ?: Result.Error("Token not found")
                } else {
                    Result.Error("Empty Response Body")
                }
            } else {
                Result.Error("Failed to login: ${response.code()}")
            }
        } catch (e: Exception) {
            Result.Error("Error: ${e.message}")
        }
    }

    suspend fun register(name: String, email: String, password: String): Result<RegisterResponse> {
        return try {
            val response = apiService.register(name, email, password)
            if (response.isSuccessful) {
                val registerResponse = response.body()
                if (registerResponse != null) {
                    Result.Success(registerResponse)
                } else {
                    Result.Error("Empty response body")
                }
            } else {
                Result.Error("Failed to register: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("Register", "Registration error", e)
            Result.Error("Register error: ${e.message}")
        }
    }

    fun getToken(): Flow<String?> {
        return userPreferences.token
    }

    companion object {
        @Volatile
        private var instance: AuthRepository? = null

        fun getInstance(apiService: ApiServiceAuth, userPreferences: UserPreferences): AuthRepository {
            return instance ?: synchronized(this) {
                instance ?: AuthRepository(apiService, userPreferences).also { instance = it }
            }
        }
    }
}