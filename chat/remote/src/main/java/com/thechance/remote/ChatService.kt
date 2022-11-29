package com.thechance.remote

import com.thechance.remote.response.BaseResponse
import com.thechance.remote.response.UnreadMessagesResponse
import com.thechance.repositories.models.ConversationDTO
import com.thechance.repositories.models.MessagesDTO
import retrofit2.Response
import retrofit2.http.*

interface ChatService {
    /**
     * message
     * */

    @GET("message_recent")
    suspend fun getRecentMessages(
        @Query("guid")userID:Int
    ): Response<BaseResponse<ConversationDTO>>

    @GET("message_list")
    suspend fun getConversationWithFriend(
        @Query("to") userID: Int,
        @Query("guid") friendID: Int,
        @Query("markallread") markAsRead: Int = 0,
        @Query("offset") page: Int? = null
    ): Response<BaseResponse<ConversationDTO>>


    // 1 to mark as read , 0 if not.
    @GET("message_new")
    suspend fun getUnreadMessages(
        @Query("to") userID: Int,
        @Query("from") friendID: Int,
        @Query("markallread") markAsRead: Int = 0
    ): Response<BaseResponse<UnreadMessagesResponse>>

    @FormUrlEncoded
    @POST("message_add")
    suspend fun sendMessage(
        @Field("from") userID: Int,
        @Field("to") friendID: Int,
        @Field("message") message: String
    ): Response<BaseResponse<MessagesDTO>>

}