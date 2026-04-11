package xyz.nxprojects.dracin.data.model

/**
 * Data class untuk menyimpan detail error dari API
 */
data class ApiError(
    val httpCode: Int = 0,
    val errorType: ErrorType = ErrorType.UNKNOWN,
    val message: String = "",
    val jsonMessage: String? = null,
    val rawResponse: String? = null
)

enum class ErrorType {
    NETWORK,          // Koneksi gagal
    HTTP_403,         // Forbidden
    HTTP_404,         // Not Found
    HTTP_500,         // Server Error
    TIMEOUT,          // Timeout
    PARSE_ERROR,      // JSON parsing error
    UNKNOWN           // Unknown error
}