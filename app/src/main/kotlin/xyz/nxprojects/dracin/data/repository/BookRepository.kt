package xyz.nxprojects.dracin.data.repository

import xyz.nxprojects.dracin.data.model.*
import xyz.nxprojects.dracin.data.remote.MeloloApi
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
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
        var lastException: Exception? = null
        
        repeat(maxRetries) { attempt ->
            try {
                val result = block()
                return Result.success(result)
            } catch (e: HttpException) {
                lastException = e
                val errorBody = e.response()?.errorBody()?.string()
                Log.e(TAG, "HTTP ${e.code()} pada percobaan ${attempt + 1}: ${e.message()}")
                Log.e(TAG, "Error body: $errorBody")
                
                // Jangan retry untuk 4xx errors kecuali 403, 408, 429
                if (e.code() !in listOf(403, 408, 429) && e.code() in 400..499) {
                    break
                }
                
                if (attempt < maxRetries - 1) {
                    delay(delayMillis * (attempt + 1))
                }
            } catch (e: IOException) {
                lastException = e
                Log.e(TAG, "Network error pada percobaan ${attempt + 1}: ${e.message}")
                
                if (attempt < maxRetries - 1) {
                    delay(delayMillis * (attempt + 1))
                }
            } catch (e: Exception) {
                lastException = e
                Log.e(TAG, "Unexpected error pada percobaan ${attempt + 1}: ${e.message}", e)
                break
            }
        }
        
        return Result.failure(lastException ?: Exception("Unknown error"))
    }

    private fun ensureValidUrl(url: String?): String {
        if (url.isNullOrEmpty()) return ""
        return if (url.startsWith("http")) url else "https://$url"
    }
}
