package com.github.tywinlanni.notificator.server

import io.ktor.resources.*
import kotlinx.serialization.Serializable

@Serializable
@Resource(path = "sendNotification")
data class SendNotificationResource(
    val telegramId: Long,
)
