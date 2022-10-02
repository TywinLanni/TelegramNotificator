package com.github.tywinlanni.notificator.bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.telegramError
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.*
import com.github.kotlintelegrambot.entities.inputmedia.InputMediaPhoto
import com.github.kotlintelegrambot.entities.inputmedia.InputMediaVideo
import com.github.kotlintelegrambot.entities.inputmedia.MediaGroup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton
import com.github.kotlintelegrambot.logging.LogLevel
import com.github.tywinlanni.notificator.client.NotificatorClient
import com.github.tywinlanni.notificator.configuration.TelegramConfiguration
import com.github.tywinlanni.notificator.server.SearchResult
import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class TelegramBot(private val telegramConfiguration: TelegramConfiguration, private val notificatorClient: NotificatorClient) : CoroutineScope {

    override val coroutineContext = Dispatchers.Default

    private val bot: Bot = runBlocking { buildBot() }
        .apply {
            startPolling()
        }

    private val bufferMutex = Mutex()
    private val selectedChannelMutex = Mutex()

    // Change to redis or some
    private val bufferData: MutableMap<Long, SearchResult.ChannelSearchResult> = mutableMapOf()
    private val selectedChannel: MutableMap<Long, Int> = mutableMapOf()

    suspend fun addBufferData(chatId: Long, searchResult: SearchResult.ChannelSearchResult) = bufferMutex.withLock {
        bufferData[chatId] = searchResult
    }

    suspend fun changeSelectedChannel(chatId: Long, channelIndex: Int) = selectedChannelMutex.withLock {
        selectedChannel[chatId] = channelIndex
    }

    fun sendMessage(message: String, chatId: ChatId) {
        bot.sendMessage(
            chatId = chatId,
            text = message,
        )
    }

    fun sendButton(buttonName: String, callbackType: CallbackType, chatId: ChatId, buttonHeader: String) {
        bot.sendMessage(
            chatId = chatId,
            text = buttonHeader,
            replyMarkup = InlineKeyboardMarkup.create(
                listOf(InlineKeyboardButton.CallbackData(buttonName, callbackType.name))
            )
        )

    }

    fun sendMediaByUrl(photoUrl: String, chatId: ChatId) {
        bot.sendPhoto(
            chatId = chatId,
            photo = TelegramFile.ByUrl(photoUrl),
        )
    }

    private suspend fun buildBot() = bot {
        token = telegramConfiguration.botToken
        logLevel = LogLevel.Network.Body

        runBlocking {
            buildBotBody()
        }
    }

    private suspend fun Bot.Builder.buildBotBody() {
        dispatch {
            command("find") {
                bot.sendMessage(chatId = ChatId.fromId(update.message!!.chat.id),
                    text = "Введите название канала, оповещения о новых видио которого вы хотите получать")

            }
            text {
                if (text[0] == '/')
                    return@text

                runBlocking {
                    val foundChannels = notificatorClient.findChannelByName(text)
                    addBufferData(update.message!!.chat.id, foundChannels)
                    foundChannels
                }.let { foundChannels ->
                    showSearchResult(foundChannels, ChatId.fromId(update.message!!.chat.id))
                }
            }

            callbackQuery(CallbackType.ADD_CHANNEL.name) {
                val chatId = callbackQuery.message?.chat?.id ?: return@callbackQuery
                println("asd")
                runBlocking {
                    notificatorClient.addNewYoutubeChannel(
                        telegramUserId = chatId,
                        channel = bufferMutex.withLock {
                            selectedChannelMutex.withLock {
                                bufferData[chatId]?.items?.get(selectedChannel[chatId] ?: 0)?.snippet ?: error("")
                            }
                        }
                    ).let {
                        sendMessage("Success", ChatId.fromId(chatId))
                    }
                }
            }
            callbackQuery(CallbackType.NEXT_PAGE.name) {
                runBlocking {
                    val chatId = callbackQuery.message?.chat?.id ?: return@runBlocking
                    notificatorClient.nextSearchPage(
                        token = bufferMutex.withLock {
                            bufferData[chatId]?.nextPageToken ?: error("")
                        }
                    ).let { findChannels ->
                        showSearchResult(findChannels, ChatId.fromId(update.message!!.chat.id))
                    }
                }
            }
            telegramError {
                println(error.getErrorMessage())
            }
        }
    }

    private fun showSearchResult(searchResult: SearchResult.ChannelSearchResult, id: ChatId) {
        searchResult.items.forEachIndexed  { index, foundChannel ->
            sendMessage("$index) ${foundChannel.snippet.channelTitle}\n${foundChannel.snippet.description}", id)
            sendMediaByUrl(foundChannel.snippet.thumbnails.default.url, id)
            sendButton("Подписаться", CallbackType.ADD_CHANNEL, id, "Кнопка для подписки:")
        }

        sendMessage("${searchResult.pageInfo.resultsPerPage}/${searchResult.pageInfo.totalResults} результатов показано.", id)
        sendButton("Следующая страница", CallbackType.NEXT_PAGE, id, "Следующая страница:")
    }
}
