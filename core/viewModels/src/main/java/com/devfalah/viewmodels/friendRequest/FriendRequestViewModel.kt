package com.devfalah.viewmodels.friendRequest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devfalah.usecases.AddFriendRequestUseCase
import com.devfalah.usecases.GetUserFriendRequestsUseCase
import com.devfalah.usecases.RemoveFriendRequestUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendRequestViewModel @Inject constructor(
    private val friendRequestsUseCase: GetUserFriendRequestsUseCase,
    private val addFriendRequestUseCase: AddFriendRequestUseCase,
    private val removeFriendRequestUseCase: RemoveFriendRequestUseCase
) : ViewModel() {

    private val _uiStateFriendRequests = MutableStateFlow(FriendRequestUiState(emptyList()))
    val uiStateFriendRequests = _uiStateFriendRequests.asStateFlow()

    private val userID = 6

    init {
        fetchFriendsRequest()
    }

    private fun fetchFriendsRequest() {
        viewModelScope.launch {
            _uiStateFriendRequests.update { it.copy(isLoading = true, error = "") }
            try {
                _uiStateFriendRequests.update {
                    it.copy(
                        isLoading = false,
                        friendRequests = friendRequestsUseCase(userID = userID).listToUserUiState()
                    )
                }
            } catch (t: Throwable) {
                _uiStateFriendRequests.update {
                    it.copy(isLoading = false, error = t.message.toString())
                }
            }
        }
    }

    fun acceptFriendRequest(friendRequestId: Int) {
        viewModelScope.launch {
            addFriendRequestUseCase(userID = userID, friendRequestId = friendRequestId)
        }
        fetchFriendsRequest()
    }

    fun deniedFriendRequest(friendRequestId: Int) {
        viewModelScope.launch {
            removeFriendRequestUseCase(userID = userID, friendRequestId = friendRequestId)
        }
        fetchFriendsRequest()
    }
}