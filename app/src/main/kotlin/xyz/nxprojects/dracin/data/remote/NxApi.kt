package xyz.nxprojects.dracin.data.remote

import xyz.nxprojects.dracin.data.model.*
import retrofit2.http.GET
import retrofit2.http.Query

interface MeloloApi {
    @GET("home")
    suspend fun getHome(): GetBooksResponse

    @GET("drama18")
    suspend fun getDrama18(): GetBooksResponse

    @GET("komik")
    suspend fun getKomik(): GetBooksResponse

    @GET("search")
    suspend fun searchBooks(
        @Query("query") query: String
    ): SearchResponse

    @GET("detail")
    suspend fun getDetail(
        @Query("dramaId") dramaId: String
    ): DetailResponse

    @GET("getvideo")
    suspend fun getStream(
        @Query("fileId") fileId: String
    ): StreamResponse
}