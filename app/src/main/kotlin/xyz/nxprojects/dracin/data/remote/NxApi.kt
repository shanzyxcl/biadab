package xyz.nxprojects.dracin.data.remote

import xyz.nxprojects.dracin.data.model.*
import retrofit2.http.GET
import retrofit2.http.Query

interface MeloloApi {
    @GET("latest")
    suspend fun getLatest(): GetBooksResponse

    @GET("trending")
    suspend fun getTrending(): GetBooksResponse

    @GET("search")
    suspend fun searchBooks(
        @Query("query") query: String,
        @Query("limit") limit: Int = 10,
        @Query("offset") offset: Int = 0
    ): SearchResponse

    @GET("detail")
    suspend fun getDetail(
        @Query("bookId") bookId: String
    ): DetailResponse

    @GET("stream")
    suspend fun getStream(
        @Query("videoId") videoId: String
    ): StreamResponse
}
