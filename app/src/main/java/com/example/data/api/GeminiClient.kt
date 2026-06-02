package com.example.data.api

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

// --- Models for Text Request / Response ---

data class Part(
    val text: String? = null,
    val inlineData: InlineData? = null
)

data class InlineData(
    val mimeType: String,
    val data: String
)

data class Content(
    val parts: List<Part>
)

data class ResponseFormatText(
    val mimeType: String,
    val schema: Map<String, String>? = null // Simplified for basic JSON formatting
)

data class ResponseFormat(
    val text: ResponseFormatText? = null
)

data class GenerationConfig(
    val responseMimeType: String? = null,
    val temperature: Float? = null
)

data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

// Since we serialize manually using JSONObject or Gson to keep it extremely flexible and error-resistant,
// let's define simplified classes or parse raw ResponseBody to handle the nested candidate structures.
// That is much more robust against model variations!

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: okhttp3.RequestBody
    ): ResponseBody
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}

class GeminiManager {
    private val apiKey: String
        get() = BuildConfig.GEMINI_API_KEY

    // Helper to convert Bitmap to Base64 JPEG string
    private fun Bitmap.toBase64(): String {
        val outputStream = ByteArrayOutputStream()
        this.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    /**
     * Generates a conversational response or recommendations.
     */
    suspend fun generateText(prompt: String, systemInstruction: String? = null): String = withContext(Dispatchers.IO) {
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "GEMINI_API_KEY") {
            return@withContext "API key not configured. Please add your GEMINI_API_KEY inside the Secrets panel in AI Studio."
        }

        try {
            // Build raw JSON payload to avoid any Kotlin serialization compile mismatch
            val payload = JSONObject()
            val contentsArr = JSONArray()
            val contentObj = JSONObject()
            val partsArr = JSONArray()
            val partObj = JSONObject()
            partObj.put("text", prompt)
            partsArr.put(partObj)
            contentObj.put("parts", partsArr)
            contentsArr.put(contentObj)
            payload.put("contents", contentsArr)

            if (systemInstruction != null) {
                val sysInstrObj = JSONObject()
                val sysPartsArr = JSONArray()
                val sysPartObj = JSONObject()
                sysPartObj.put("text", systemInstruction)
                sysPartsArr.put(sysPartObj)
                sysInstrObj.put("parts", sysPartsArr)
                payload.put("systemInstruction", sysInstrObj)
            }

            val requestBody = okhttp3.RequestBody.create(
                "application/json".toMediaType(),
                payload.toString()
            )

            val rawResponse = RetrofitClient.service.generateContent(apiKey, requestBody)
            val jsonStr = rawResponse.string()
            parseTextResponse(jsonStr)
        } catch (e: Exception) {
            Log.e("GeminiManager", "Error calling Gemini API", e)
            "Error: ${e.localizedMessage ?: "Connection failed"}"
        }
    }

    /**
     * Analyses a scanned receipt image and extracts crucial parameters.
     */
    suspend fun scanReceipt(bitmap: Bitmap): ScannedReceipt? = withContext(Dispatchers.IO) {
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "GEMINI_API_KEY") {
            Log.w("GeminiManager", "API Key not configured for scanner, running mock fallback")
            return@withContext null
        }

        try {
            val base64Img = bitmap.toBase64()

            val systemPrompt = "You are a receipt intelligence OCR parser. Analyze the receipt image and extract these parameters in clean JSON format: storeName (String), amount (Double), date (String format DD-Mon-YYYY), purchasedItems (List of Strings, e.g. [\"Item 1\", \"Item 2\"]) and category (suggested string Food, Shopping, etc.). Response must be ONLY the raw JSON block without markdown wrappers."

            val prompt = "Extract the store name, transaction amount, date, and items list from this receipt."

            val payload = JSONObject()
            val contentsArr = JSONArray()
            val contentObj = JSONObject()
            val partsArr = JSONArray()

            val textPart = JSONObject()
            textPart.put("text", prompt)
            partsArr.put(textPart)

            val imagePart = JSONObject()
            val inlineDataObj = JSONObject()
            inlineDataObj.put("mimeType", "image/jpeg")
            inlineDataObj.put("data", base64Img)
            imagePart.put("inlineData", inlineDataObj)
            partsArr.put(imagePart)

            contentObj.put("parts", partsArr)
            contentsArr.put(contentObj)
            payload.put("contents", contentsArr)

            val systemInstrObj = JSONObject()
            val sysPartsArr = JSONArray()
            val sysPartObj = JSONObject()
            sysPartObj.put("text", systemPrompt)
            sysPartsArr.put(sysPartObj)
            systemInstrObj.put("parts", sysPartsArr)
            payload.put("systemInstruction", systemInstrObj)

            val genConfig = JSONObject()
            genConfig.put("responseMimeType", "application/json")
            payload.put("generationConfig", genConfig)

            val requestBody = okhttp3.RequestBody.create(
                "application/json".toMediaType(),
                payload.toString()
            )

            val rawResponse = RetrofitClient.service.generateContent(apiKey, requestBody)
            val jsonStr = rawResponse.string()
            val extractedText = parseTextResponse(jsonStr)

            // Parse json output
            val cleanJson = extractedText.trim()
                .substringAfter("```json")
                .substringBefore("```")
                .trim()

            val parsedJson = JSONObject(if (cleanJson.isEmpty()) extractedText else cleanJson)
            val storeName = parsedJson.optString("storeName", "Swiggy Restaurant")
            val amount = parsedJson.optDouble("amount", 0.0)
            val dateStr = parsedJson.optString("date", "02-Jun-2026")
            val category = parsedJson.optString("category", "Food")
            
            val itemsArr = parsedJson.optJSONArray("purchasedItems")
            val items = ArrayList<String>()
            if (itemsArr != null) {
                for (i in 0 until itemsArr.length()) {
                    items.add(itemsArr.getString(i))
                }
            }

            ScannedReceipt(storeName, amount, dateStr, items, category)
        } catch (e: Exception) {
            Log.e("GeminiManager", "Error in scanReceipt", e)
            null
        }
    }

    private fun parseTextResponse(jsonResponse: String): String {
        return try {
            val obj = JSONObject(jsonResponse)
            val candidates = obj.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val candidate = candidates.getJSONObject(0)
                val contentObj = candidate.optJSONObject("content")
                if (contentObj != null) {
                    val parts = contentObj.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return parts.getJSONObject(0).optString("text", "")
                    }
                }
            }
            "No output generated."
        } catch (e: Exception) {
            Log.e("GeminiManager", "Parser error on JSON: $jsonResponse", e)
            "Error parsing AI response"
        }
    }
}

data class ScannedReceipt(
    val storeName: String,
    val amount: Double,
    val dateString: String,
    val purchasedItems: List<String>,
    val category: String
)
