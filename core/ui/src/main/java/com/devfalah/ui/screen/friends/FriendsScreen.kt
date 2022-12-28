package com.devfalah.ui.screen.friends

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.devfalah.ui.R
import com.devfalah.ui.composable.*
import com.devfalah.ui.screen.profile.navigateToProfile
import com.devfalah.viewmodels.friends.FriendsUIState
import com.devfalah.viewmodels.friends.FriendsViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController

@Composable
fun FriendsScreen(
    navController: NavController,
    viewModel: FriendsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val systemUIController = rememberSystemUiController()

    FriendsContent(
        state = state,
        onBackClick = { navController.popBackStack() },
        onRefresh = viewModel::getUserFriends,
        onClickProfile = { navController.navigateToProfile(it) },
        onRemoveFriend = viewModel::removeFriend,
        onRetry = viewModel::getUserFriends,
    )
    val color = MaterialTheme.colors.background
    LaunchedEffect(true) {
        setStatusBarColor(
            systemUIController = systemUIController,
            color = color,
        )
    }
}

@Composable
fun FriendsContent(
    state: FriendsUIState,
    onBackClick: () -> Unit,
    onRefresh: () -> Unit,
    onClickProfile: (Int) -> Unit,
    onRemoveFriend: (Int) -> Unit,
    onRetry: () -> Unit,
) {

    Column(modifier = Modifier.fillMaxSize()) {
        AppBar(
            title = "${state.totalFriends} " + stringResource(id = R.string.friends),
            onBackButton = onBackClick,
        )
        if (state.error.isNotBlank()) {
            ErrorItem(onClickRetry = onRetry)
        } else if (state.isLoading) {
            LottieItem(LottieResource = R.raw.loading)
        } else if (state.friends.isEmpty()) {
            LottieItem(LottieResource = R.raw.no_data)
        } else {
            ManualPager(
                onRefresh = onRefresh,
                contentPadding = PaddingValues(16.dp),
                isLoading = state.isPagerLoading,
                error = state.minorError,
                isEndOfPager = state.isPagerEnd,
            ) {

                if (state.isMyProfile) {
                    items(state.friends) {
                        FriendRemoveItem(
                            state = it,
                            onClickOpenProfile = onClickProfile,
                            onRemoveFriend = onRemoveFriend
                        )
                    }
                } else {
                    items(state.friends) {
                        FriendItem(
                            state = it,
                            onOpenProfileClick = onClickProfile
                        )
                    }
                }
            }
        }
    }
}
