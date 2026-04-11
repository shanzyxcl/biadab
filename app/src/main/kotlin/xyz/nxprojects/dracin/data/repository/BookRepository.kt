package xyz.nxprojects.dracin.data.repository

import xyz.nxprojects.dracin.data.model.*
import xyz.nxprojects.dracin.data.remote.MeloloApi
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

class BookRepository @Inject constructor(
    private val api: MeloloApi
) {
    private val TAG = "BookRepository"
    
    suspend fun getLatest(): Result<List<Book>> = withContext(Dispatchers.IO) {
        executeWithRetry {
            val response = api.getLatest()
            response.books.map { it.copy(thumbUrl = ensureValidUrl(it.thumbUrl)) }
        }
    }

    suspend fun getTrending(): Result<List<Book>> = withContext(Dispatchers.IO) {
        executeWithRetry {
            val response = api.getTrending()
            response.books.map { it.copy(thumbUrl = ensureValidUrl(it.thumbUrl)) }
        }
    }

    suspend fun searchBooks(
        query: String,
        limit: Int = 10,
        offset: Int = 0
    ): Result<List<Book>> = withContext(Dispatchers.IO) {
        executeWithRetry {
            val response = api.searchBooks(query, limit, offset)
            response.data.searchData
                .flatMap { it.books }
                .filter { it.bookName.isNotEmpty() }
                .map { it.copy(thumbUrl = ensureValidUrl(it.thumbUrl)) }
        }
    }

    suspend fun getDetail(bookId: String): Result<VideoData> = withContext(Dispatchers.IO) {
        executeWithRetry {
            val response = api.getDetail(bookId)
            response.data.videoData.copy(
                seriesCover = ensureValidUrl(response.data.videoData.seriesCover),
                videoList = response.data.videoData.videoList.map {
                    it.copy(cover = ensureValidUrl(it.cover))
                }
            )
        }
    }

    suspend fun getStream(videoId: String): Result<StreamData> = withContext(Dispatchers.IO) {
        executeWithRetry {
            val response = api.getStream(videoId)
            response.data
        }
    }

    private suspend fun <T> executeWithRetry(
        maxRetries: Int = 3,
        delayMillis: Long = 1000,
        block: suspend () -> T
    ): Result<T> {
        var lastError: ApiError? = null
        
        repeat(maxRetries) { attempt ->
            try {
                val result = block()
                return Result.success(result)
            } catch (e: HttpException) {
                val httpCode = e.code()
                val errorBody = e.response()?.errorBody()?.string()
                
                // Parse JSON message dari error response
                val jsonMessage = try {
                    errorBody?.let { body ->
                        val json = JSONObject(body)
                        json.optString("message") 
                            ?: json.optString("error")
                            ?: json.optString("msg")
                            ?: body
                    }
                } catch (_: Exception) {
                    errorBody
                }
                
                lastError = ApiError(
                    httpCode = httpCode,
                    errorType = when (httpCode) {
                        403 -> ErrorType.HTTP_403
                        404 -> ErrorType.HTTP_404
                        in 500..599 -> ErrorType.HTTP_500
                        else -> ErrorType.UNKNOWN
                    },
                    message = "HTTP $httpCode: ${e.message()}",
                    jsonMessage = jsonMessage,
                    rawResponse = errorBody
                )
                
                Log.e(TAG, "═══════════════════════════════════════")
                Log.e(TAG, "HTTP Error pada percobaan ${attempt + 1}/$maxRetries")
                Log.e(TAG, "HTTP Code: $httpCode")
                Log.e(TAG, "Error Type: ${lastError?.errorType}")
                Log.e(TAG, "Message: ${e.message()}")
                Log.e(TAG, "JSON Response: $jsonMessage")
                Log.e(TAG, "Raw Response: $errorBody")
                Log.e(TAG, "═══════════════════════════════════════")
                
                // Jangan retry untuk 4xx errors kecuali 403, 408, 429
                if (httpCode !in listOf(403, 408, 429) && httpCode in 400..499) {
                    break
                }
                
                if (attempt < maxRetries - 1) {
                    delay(delayMillis * (attempt + 1))
                }
            } catch (e: UnknownHostException) {
                lastError = ApiError(
                    errorType = ErrorType.NETWORK,
                    message = "Tidak dapat terhubung ke server. Periksa koneksi internet Anda.",
                    jsonMessage = "DNS resolution failed: ${e.message}"
                )
                
                Log.e(TAG, "Network error: Host tidak ditemukan - ${e.message}")
                
                if (attempt < maxRetries - 1) {
                    delay(delayMillis * (attempt + 1))
                }
            } catch (e: SocketTimeoutException) {
                lastError = ApiError(
                    errorType = ErrorType.TIMEOUT,
                    message = "Koneksi timeout. Server terlalu lama merespon.",
                    jsonMessage = "Timeout: ${e.message}"
                )
                
                Log.e(TAG, "Timeout error: ${e.message}")
                
                if (attempt < maxRetries - 1) {
                    delay(delayMillis * (attempt + 1))
                }
            } catch (e: IOException) {
                lastError = ApiError(
                    errorType = ErrorType.NETWORK,
                    message = "Gagal terhubung ke server. Periksa koneksi internet Anda.",
                    jsonMessage = "IO Error: ${e.message}"
                )
                
                Log.e(TAG, "IO error: ${e.message}")
                
                if (attempt < maxRetries - 1) {
                    delay(delayMillis * (attempt + 1))
                }
            } catch (e: Exception) {
                lastError = ApiError(
                    errorType = ErrorType.UNKNOWN,
                    message = "Terjadi kesalahan: ${e.message}",
                    jsonMessage = e.stackTraceToString()
                )
                
                Log.e(TAG, "Unexpected error: ${e.message}", e)
                break
            }
        }
        
        return Result.failure(ApiErrorException(lastError ?: ApiError()))
    }

    private fun ensureValidUrl(url: String?): String {
        if (url.isNullOrEmpty()) return ""
        return if (url.startsWith("http")) url else "https://$url"
    }
}

/**
 * Custom exception untuk membawa ApiError detail
 */
class ApiErrorException(val apiError: ApiError) : Exception(apiError.message)