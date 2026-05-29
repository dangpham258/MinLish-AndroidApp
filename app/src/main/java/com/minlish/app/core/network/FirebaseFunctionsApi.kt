package com.minlish.app.core.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import javax.inject.Inject
import javax.inject.Singleton

// DTO for Cloud Functions
data class SendResetEmailRequest(
    val email: String
)

data class CloudFunctionResponse(
    val success: Boolean,
    val message: String? = null,
    val result: Map<String, Any>? = null
)

data class VerifyTokenRequest(
    val userId: String,
    val token: String
)

data class VerifyTokenResponse(
    val valid: Boolean,
    val message: String? = null
)

data class ResetPasswordRequest(
    val userId: String,
    val token: String,
    val newPassword: String
)

interface FirebaseFunctionsApi {
    
    @POST("sendResetPasswordEmail")
    suspend fun sendResetPasswordEmail(
        @Body request: SendResetEmailRequest
    ): Response<CloudFunctionResponse>

    @POST("verifyResetToken")
    suspend fun verifyResetToken(
        @Body request: VerifyTokenRequest
    ): Response<VerifyTokenResponse>

    @POST("resetPassword")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest
    ): Response<CloudFunctionResponse>

    companion object {
        // Firebase Functions emulator URL (local dev)
        const val EMULATOR_URL = "http://10.0.2.2:5001/minlish-1e2ec/asia-southeast1/"
        // Production URL (sau khi deploy)
        const val PRODUCTION_URL = "https://asia-southeast1-minlish-1e2ec.cloudfunctions.net/"
    }
}
