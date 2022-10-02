package com.github.tywinlanni.notificator.configuration

import io.ktor.server.application.*

fun Application.loadConfiguration(): Configuration =
    Configuration(
        ktorConfiguration = KtorConfiguration(
            port = environment.config.propertyOrNull("ktor.deployment.port")?.getString()?.toIntOrNull() ?: error("Application port not found"),
            auth = AuthClientConfiguration(
                base = BaseAuthClientConfiguration(
                    username = environment.config.propertyOrNull("ktor.auth.base.username")?.getString() ?: error("Base auth username not found"),
                    password = environment.config.propertyOrNull("ktor.auth.base.password")?.getString() ?: error("Base auth password not found"),
                )
            ),
        ),
        telegramConfiguration = TelegramConfiguration(
            botToken = environment.config.propertyOrNull("telegram.botToken")?.getString() ?: error("Telegram bot token not found")
        ),
        clientConfiguration = ClientConfiguration(
            host = environment.config.propertyOrNull("client.host")?.getString() ?: error("Client host not found"),
            port = environment.config.propertyOrNull("client.port")?.getString() ?: error("Client port not found"),
            auth = AuthClientConfiguration(
                base = BaseAuthClientConfiguration(
                    username = environment.config.propertyOrNull("client.auth.base.username")?.getString() ?: error("Client username not found"),
                    password = environment.config.propertyOrNull("client.auth.base.password")?.getString() ?: error("Client password not found"),
                )
            )
        )
    )
