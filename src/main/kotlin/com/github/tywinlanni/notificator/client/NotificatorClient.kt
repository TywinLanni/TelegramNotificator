package com.github.tywinlanni.notificator.client

import com.github.tywinlanni.notificator.configuration.ClientConfiguration
import com.github.tywinlanni.notificator.server.SearchResult
import com.github.tywinlanni.notificator.server.Snippet
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class NotificatorClient(private val clientConfiguration: ClientConfiguration) {

    private val client = HttpClient(CIO) {
        /*defaultRequest {
            host = "http://${clientConfiguration.host}:${clientConfiguration.port}"
        }*/
        install(ContentNegotiation) {
            json(
                Json {
                    prettyPrint = true
                    isLenient = true
                }
            )
        }
        install(HttpTimeout) {
            connectTimeoutMillis = 30_000
        }
        install(Auth) {
            basic {
                credentials {
                    BasicAuthCredentials(
                        username = clientConfiguration.auth.base.username,
                        password = clientConfiguration.auth.base.password,
                    )
                }
                realm = "Access to the '/' path"
            }
        }
    }

    suspend fun addNewYoutubeChannel(telegramUserId: Long, channel: Snippet.Channel) = client.post("http://${clientConfiguration.host}:${clientConfiguration.port}/youtube/addMonitoredChannel") {
        parameter(key = "userTelegramId", value = telegramUserId)
        contentType(ContentType.Application.Json)
        setBody(channel)
    }

    // Переделать на аналог локаций из ктора 2.0
    suspend fun removeYoutubeChannelSubscriptions(youtubeChannelLink: String, telegramUserId: Long) = client.delete("/youtubeChannel") {
        parameter(key = "youtubeChannel", value = youtubeChannelLink)
        parameter(key = "telegramUserId", value = telegramUserId)
    }

    suspend fun findChannelByName(channelName: String) = client.get("http://${clientConfiguration.host}:${clientConfiguration.port}/youtube/findChannelByName") {
        parameter(key = "channelName", value = channelName)
    }.body<SearchResult.ChannelSearchResult>()

    suspend fun nextSearchPage(token: String) = client.get("http://${clientConfiguration.host}:${clientConfiguration.port}/youtube/nextPage") {
        parameter(key = "nextPageToken", value = token)
    }.body<SearchResult.ChannelSearchResult>()

    suspend fun currentSubscriptions(telegramUserId: Long) = client.get("/list") {

    }
}
