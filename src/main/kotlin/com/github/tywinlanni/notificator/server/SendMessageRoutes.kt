package com.github.tywinlanni.notificator.server

import com.github.kotlintelegrambot.entities.ChatId
import com.github.tywinlanni.notificator.bot.CallbackType
import com.github.tywinlanni.notificator.bot.TelegramBot
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.routing.*


fun Route.sendNotificationRoutes(bot: TelegramBot) {
    put<SendNotificationResource> { (telegramId: Long) ->
        val releaseVideo = call.receive<Item.Video>()

        bot.sendMessage("https://www.youtube.com/watch?v=${releaseVideo.id.videoId}", ChatId.fromId(telegramId))
    }
}
