package com.example.hashilink.ai

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FirebaseAIChatHelper {

    companion object {
        private const val TAG = "FirebaseAIChatHelper"
        private const val MODEL_NAME = "gemini-2.5-pro"
    }

    /**
     * Get AI response for user's message using Firebase Vertex AI
     * @param userMessage The message from the user
     * @param productContext Optional product details for context
     * @param chatHistory Optional previous messages for context
     * @return AI-generated response
     */
    suspend fun getChatResponse(
        userMessage: String,
        productContext: String? = null,
        chatHistory: List<String>? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Getting AI response for message: $userMessage")

            // Initialize Firebase Vertex AI backend
            val generativeModel = Firebase.ai(backend = GenerativeBackend.googleAI())
                .generativeModel(MODEL_NAME)

            // Build prompt with context
            val prompt = buildPrompt(userMessage, productContext, chatHistory)
            Log.d(TAG, "Generated prompt: $prompt")

            // Generate content using Firebase AI
            val response = generativeModel.generateContent(prompt)
            val responseText = response.text ?: "Sorry, I couldn't generate a response."

            Log.d(TAG, "AI response received: $responseText")
            Result.success(responseText)

        } catch (e: Exception) {
            Log.e(TAG, "Error getting AI response", e)
            Result.failure(e)
        }
    }

    /**
     * Get product-specific AI assistance
     * @param productName Product name
     * @param productDescription Product description
     * @param productPrice Product price
     * @param userQuestion User's question about the product
     * @return AI-generated answer
     */
    suspend fun getProductAssistance(
        productName: String,
        productDescription: String,
        productPrice: String,
        userQuestion: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                You are a helpful shopping assistant for HashiLink, a marketplace app.
                
                Product Details:
                - Name: $productName
                - Description: $productDescription
                - Price: ₹$productPrice
                
                User Question: $userQuestion
                
                Provide a helpful, concise answer about this product. Be friendly and professional.
                If the question cannot be answered with the given information, politely suggest 
                contacting the seller directly.
            """.trimIndent()

            Log.d(TAG, "Getting product assistance for: $productName")

            // Initialize Firebase Gemini AI
            val generativeModel = Firebase.ai(backend = GenerativeBackend.googleAI())
                .generativeModel(MODEL_NAME)

            val response = generativeModel.generateContent(prompt)
            val responseText = response.text ?: "Sorry, I couldn't help with that. Please contact the seller directly."

            Log.d(TAG, "Product assistance response received")
            Result.success(responseText)

        } catch (e: Exception) {
            Log.e(TAG, "Error getting product assistance", e)
            Result.failure(e)
        }
    }

    /**
     * Get negotiation suggestions for buyers
     */
    suspend fun getNegotiationSuggestion(
        productPrice: String,
        productName: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                You are a negotiation advisor for buyers on HashiLink marketplace.
                
                Product: $productName
                Current Price: ₹$productPrice
                
                Provide 2-3 polite negotiation strategies or phrases the buyer could use 
                when messaging the seller. Be respectful and professional. Keep it brief.
            """.trimIndent()

            // Initialize Firebase Vertex AI
            val generativeModel = Firebase.ai(backend = GenerativeBackend.googleAI())
                .generativeModel(MODEL_NAME)

            val response = generativeModel.generateContent(prompt)
            val responseText = response.text ?: "Consider politely asking if the seller is open to negotiations."

            Result.success(responseText)

        } catch (e: Exception) {
            Log.e(TAG, "Error getting negotiation suggestion", e)
            Result.failure(e)
        }
    }

    private fun buildPrompt(
        userMessage: String,
        productContext: String?,
        chatHistory: List<String>?
    ): String {
        val promptBuilder = StringBuilder()

        promptBuilder.append("You are a helpful AI assistant for HashiLink, a marketplace chat app. ")
        promptBuilder.append("Help users with their questions about products, negotiations, or general marketplace queries. ")
        promptBuilder.append("Be concise, friendly, and professional.\n\n")

        // Add product context if available
        if (!productContext.isNullOrBlank()) {
            promptBuilder.append("Product Context:\n$productContext\n\n")
        }

        // Add chat history for context (last 5 messages)
        if (!chatHistory.isNullOrEmpty()) {
            promptBuilder.append("Recent Chat History:\n")
            chatHistory.takeLast(5).forEach { message ->
                promptBuilder.append("- $message\n")
            }
            promptBuilder.append("\n")
        }

        promptBuilder.append("User Question: $userMessage\n\n")
        promptBuilder.append("Provide a helpful response:")

        return promptBuilder.toString()
    }
}