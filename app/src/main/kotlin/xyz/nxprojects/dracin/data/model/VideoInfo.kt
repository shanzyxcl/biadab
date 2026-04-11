package xyz.nxprojects.dracin.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VideoInfo(
    val vid: String,
    @SerialName("vid_index")
    val vidIndex: Int,
    val title: String = "",
    val duration: Int = 0,
    val cover: String = ""
)

@Serializable
data class DetailResponse(
    val data: DetailData = DetailData()
)

@Serializable
data class DetailData(
    @SerialName("video_data")
    val videoData: VideoData = VideoData()
)

@Serializable
data class VideoData(
    @SerialName("series_title")
    val seriesTitle: String = "",
    @SerialName("series_intro")
    val seriesIntro: String = "",
    @SerialName("series_cover")
    val seriesCover: String = "",
    @SerialName("episode_cnt")
    val episodeCount: Int = 0,
    @SerialName("category_schema")
    val categorySchema: String = "[]",
    @SerialName("video_list")
    val videoList: List<VideoInfo> = emptyList()
)

@Serializable
data class Category(
    @SerialName("category_id")
    val categoryId: Int,
    val name: String
)