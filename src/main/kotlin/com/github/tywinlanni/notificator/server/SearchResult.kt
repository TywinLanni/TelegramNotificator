package com.github.tywinlanni.notificator.server

import kotlinx.serialization.Serializable

@Serializable
sealed class SearchResult {
    @Serializable
    data class ChannelSearchResult(
        val nextPageToken: String,
        val items: List<Item.Channel>,
        val pageInfo: PageInfo
    ) : SearchResult()

    @Serializable
    data class VideoSearchResult(
        val items: List<Item.Video>,
        val pageInfo: PageInfo
    ) : SearchResult()
}

@Serializable
sealed class Item {
    @Serializable
    data class Channel(
        val snippet: Snippet.Channel,
    ) : Item()

    @Serializable
    data class Video(
        val id: Id,
        val snippet: Snippet.Video,
    ) : Item()
}

@Serializable
sealed class Snippet {
    @Serializable
    data class Channel(
        val channelId: String,
        val channelTitle: String,
        val description: String,
        val title: String,
        val thumbnails: Thumbnails,
    ) : Snippet()

    @Serializable
    data class Video(
        val channelTitle: String,
        val description: String,
        val publishedAt: String,
        val title: String,
        val thumbnails: Thumbnails,
    ) : Snippet()
}

@Serializable
data class PageInfo(
    val totalResults: Int,
    val resultsPerPage: Int,
)

@Serializable
data class Thumbnails(
    val default: Preview,
)

@Serializable
data class Preview(
    val url: String,
)

@Serializable
data class Id(
    val videoId: String,
)
