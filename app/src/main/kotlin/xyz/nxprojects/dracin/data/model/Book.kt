package xyz.nxprojects.dracin.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Book(
    @SerialName("book_id")
    val bookId: String,
    @SerialName("book_name")
    val bookName: String,
    val abstract: String = "",
    val author: String = "",
    @SerialName("thumb_url")
    val thumbUrl: String = "",
    @SerialName("read_count")
    val readCount: String = "0",
    @SerialName("serial_count")
    val serialCount: String = "0",
    @SerialName("show_creation_status")
    val showCreationStatus: String = "",
    @SerialName("stat_infos")
    val statInfos: List<String> = emptyList()
)

@Serializable
data class GetBooksResponse(
    val books: List<Book> = emptyList()
)

@Serializable
data class SearchResponse(
    val data: SearchData = SearchData()
)

@Serializable
data class SearchData(
    @SerialName("search_data")
    val searchData: List<SearchItem> = emptyList()
)

@Serializable
data class SearchItem(
    val books: List<Book> = emptyList()
)