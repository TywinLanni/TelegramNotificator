package com.github.tywinlanni.notificator.configuration

data class Configuration(
    val ktorConfiguration: KtorConfiguration,
    val telegramConfiguration: TelegramConfiguration,
    val clientConfiguration: ClientConfiguration,
)

data class TelegramConfiguration(
    val botToken: String,
)

data class ClientConfiguration(
    val host: String,
    val port: String,
    val auth: AuthClientConfiguration,
)

data class AuthClientConfiguration(
    val base: BaseAuthClientConfiguration,
)

data class BaseAuthClientConfiguration(
    val username: String,
    val password: String,
)
data class KtorConfiguration(
    val port: Int,
    val auth: AuthClientConfiguration,
)
