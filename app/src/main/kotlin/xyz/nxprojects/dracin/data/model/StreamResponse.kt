package xyz.nxprojects.dracin.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StreamResponse(
    val data: StreamData = StreamData()
)

@Serializable
data class StreamData(
    @SerialName("main_url")
    val mainUrl: String = "",
    @SerialName("backup_url")
    val backupUrl: String = "",
    @SerialName("video_model")
    val videoModel: VideoModel = VideoModel()
)

@Serializable
data class VideoModel(
    @SerialName("video_list")
    val videoList: Map<String, VideoUrlData> = emptyMap()
)

@Serializable
data class VideoUrlData(
    @SerialName("main_url_decoded")
    val mainUrlDecoded: String = "",
    @SerialName("backup_url_1_decoded")
    val backupUrlDecoded: String = ""
)