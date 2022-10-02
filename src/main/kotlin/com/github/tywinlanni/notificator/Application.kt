package com.github.tywinlanni.notificator

import com.github.tywinlanni.notificator.bot.TelegramBot
import com.github.tywinlanni.notificator.client.NotificatorClient
import com.github.tywinlanni.notificator.configuration.loadConfiguration
import com.github.tywinlanni.notificator.server.sendNotificationRoutes
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.cio.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main(args: Array<String>): Unit = EngineMain.main(args)


fun Application.module() {

    val configuration = loadConfiguration()
    val notificatorClient = NotificatorClient(configuration.clientConfiguration)
    val bot = TelegramBot(configuration.telegramConfiguration, notificatorClient)

    install(Resources)
    install(Authentication) {
        basic("auth-basic") {
            realm = "Access to the '/' path"
            validate { credentials ->
                if (credentials.name == configuration.ktorConfiguration.auth.base.username && credentials.password == configuration.ktorConfiguration.auth.base.password) {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
        }
    }
    install(ContentNegotiation) {
        json()
    }

    routing {
        authenticate("auth-basic") {
            sendNotificationRoutes(bot)
        }
    }
}
