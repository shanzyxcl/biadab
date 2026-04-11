package xyz.nxprojects.dracin.data.repository

import xyz.nxprojects.dracin.data.model.*
import xyz.nxprojects.dracin.data.remote.MeloloApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BookRepository @Inject constructor(
    private val api: MeloloApi
) {
    suspend fun getLatest(): Result<List<Book>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getLatest()
            Result.success(response.books.map { it.copy(thumbUrl = ensureValidUrl(it.thumbUrl)) })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTrending(): Result<List<Book>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getTrending()
            Result.success(response.books.map { it.copy(thumbUrl = ensureValidUrl(it.thumbUrl)) })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchBooks(
        query: String,
        limit: Int = 10,
        offset: Int = 0
    ): Result<List<Book>> = withContext(Dispatchers.IO) {
        try {
            val response = api.searchBooks(query, limit, offset)
            val books = response.data.searchData
                .flatMap { it.books }
                .filter { it.bookName.isNotEmpty() }
                .map { it.copy(thumbUrl = ensureValidUrl(it.thumbUrl)) }
            Result.success(books)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDetail(bookId: String): Result<VideoData> = withContext(Dispatchers.IO) {
        try {
            val response = api.getDetail(bookId)
            val videoData = response.data.videoData.copy(
                seriesCover = ensureValidUrl(response.data.videoData.seriesCover),
                videoList = response.data.videoData.videoList.map {
                    it.copy(cover = ensureValidUrl(it.cover))
                }
            )
            Result.success(videoData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getStream(videoId: String): Result<StreamData> = withContext(Dispatchers.IO) {
        try {
            val response = api.getStream(videoId)
            Result.success(response.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun ensureValidUrl(url: String?): String {
        if (url.isNullOrEmpty()) return ""
        return if (url.startsWith("http")) url else "https://$url"
    }
}