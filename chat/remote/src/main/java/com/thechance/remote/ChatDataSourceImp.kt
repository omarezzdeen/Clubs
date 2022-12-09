package com.thechance.remote

import android.util.Log
import com.devfalah.repository.ChatDataSource
import com.devfalah.repository.models.ChatDTO
import com.devfalah.repository.models.ConversationDTO
import com.devfalah.repository.models.NotificationDto
import com.thechance.remote.response.BaseResponse
import retrofit2.Response
import javax.inject.Inject

class ChatDataSourceImp @Inject constructor(
    private val service: ChatService,
    private val cloudMessagingService: CloudMessagingService,
) : ChatDataSource {

    override suspend fun getChats(userID: Int): ConversationDTO {
        return wrap { service.getRecentMessages(userID) }
    }

    override suspend fun getMessagesWithFriend(userID: Int, friendID: Int): ConversationDTO {
        return wrap { service.getConversationWithFriend(userID, friendID) }
    }

    override suspend fun setSendMessage(from: Int, to: Int, message: String): ChatDTO {
        return wrap { service.sendMessage(from, to, message) }
    }

    override suspend fun postNotification(notification: NotificationDto): Boolean {
        return try {
            cloudMessagingService.postNotification(notification)
            true
        }catch (e:Throwable){
            Log.e("DEVFALAH",e.message.toString())
            false
        }


    }


    private suspend fun <T> wrap(
        function: suspend () -> Response<BaseResponse<T>>,
    ): T {
        val response = function()
        return if (response.isSuccessful) {
            when (response.body()?.code) {
                "100" -> response.body()?.payload
                "103" -> response.body()?.message.toString()
                else -> throw Throwable("One or more component required for this request can not be found on remote server")
            } as T
        } else {
            throw Throwable(" Not Success Request ")
        }
    }
}
