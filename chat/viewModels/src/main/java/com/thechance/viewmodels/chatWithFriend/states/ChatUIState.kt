package com.thechance.viewmodels.chatWithFriend.states

data class ChatUIState(
    val appBar: AppBarUIState = AppBarUIState(),
    val messages: List<MessageUIState> = emptyList(),
    val message: MessageUIState = MessageUIState(),
    val isLoading: Boolean = false,
    val error: String? = "",

)

