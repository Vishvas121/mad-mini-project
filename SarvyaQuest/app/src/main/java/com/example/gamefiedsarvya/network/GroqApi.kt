package com.example.gamefiedsarvya.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

// ── Request / Response models ─────────────────────────────────────────────────

data class GroqMessage(
    val role: String,       // "system" | "user" | "assistant"
    val content: String
)

data class GroqRequest(
    val model: String = "llama3-8b-8192",
    val messages: List<GroqMessage>,
    @SerializedName("max_tokens") val maxTokens: Int = 512,
    val temperature: Float = 0.7f,
    val stream: Boolean = false
)

data class GroqChoice(
    val message: GroqMessage,
    @SerializedName("finish_reason") val finishReason: String?
)

data class GroqUsage(
    @SerializedName("prompt_tokens")     val promptTokens: Int,
    @SerializedName("completion_tokens") val completionTokens: Int,
    @SerializedName("total_tokens")      val totalTokens: Int
)

data class GroqResponse(
    val id: String,
    val choices: List<GroqChoice>,
    val usage: GroqUsage?
) {
    val text: String get() = choices.firstOrNull()?.message?.content?.trim() ?: ""
}

// ── Retrofit interface ────────────────────────────────────────────────────────

interface GroqApi {
    @POST("openai/v1/chat/completions")
    suspend fun chat(
        @Header("Authorization") bearer: String,
        @Header("Content-Type")  contentType: String = "application/json",
        @Body request: GroqRequest
    ): GroqResponse
}
