package com.devfalah.ui.screen.profile

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.devfalah.ui.composable.ManualPager
import com.devfalah.ui.composable.PostItem
import com.devfalah.ui.composable.setStatusBarColor
import com.devfalah.ui.modifiers.nonRippleEffect
import com.devfalah.ui.screen.friends.navigateToFriends
import com.devfalah.ui.screen.home.openBrowser
import com.devfalah.ui.screen.postCreation.navigateToPostCreation
import com.devfalah.ui.screen.postDetails.navigateToPostDetails
import com.devfalah.ui.screen.profile.composable.*
import com.devfalah.ui.screen.userInformation.navigateToEditUserInformation
import com.devfalah.ui.theme.LightPrimaryBrandColor
import com.devfalah.ui.util.createFileFromContentUri
import com.devfalah.viewmodels.util.Constants.PROFILE_CLUB_ID
import com.devfalah.viewmodels.userProfile.PostUIState
import com.devfalah.viewmodels.userProfile.ProfileViewModel
import com.devfalah.viewmodels.userProfile.UserUIState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val systemUIController = rememberSystemUiController()
    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let { viewModel.onClickChangeImage(createFileFromContentUri(it, context)) }
        }
    )
    ProfileContent(
        state,
        onClickLike = viewModel::onClickLike,
        onClickComment = {
            navController.navigateToPostDetails(id = it.postId, publisherId = it.publisherId,)
        },
        onClickSave = viewModel::onClickSave,
        onClickAddFriend = viewModel::onClickAddFriend,
        onClickPostSetting = viewModel::onClickPostSetting,
        onClickSendMessage = {
            Toast.makeText(context, "not done yet.. ", Toast.LENGTH_LONG).show()
        },
        onChangeProfileImage = {
            singlePhotoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
        onRefresh = viewModel::swipeToRefresh,
        onCreatePost = { navController.navigateToPostCreation(PROFILE_CLUB_ID) },
        onClickProfile = {
            if (!state.isMyProfile) {
                navController.navigateToProfile(it)
            }
        },
        onRetry = viewModel::getData,
        onClickFriends = { navController.navigateToFriends(it) },
        onOpenLinkClick = { openBrowser(context, it) },
        onEditUserInformation = {
            navController.navigateToEditUserInformation()
        }
    )

    LaunchedEffect(key1 = state.minorError) {
        if (state.minorError.isNotEmpty()) {
            Toast.makeText(context, state.minorError, Toast.LENGTH_LONG).show()
        }
    }
    LaunchedEffect(true) {
        setStatusBarColor(
            systemUIController = systemUIController,
            color = LightPrimaryBrandColor,
            darkIcons = false
        )
    }
}

@Composable
fun ProfileContent(
    state: UserUIState,
    onClickLike: (PostUIState) -> Unit,
    onClickComment: (PostUIState) -> Unit,
    onClickSave: (PostUIState) -> Unit,
    onClickAddFriend: () -> Unit,
    onClickSendMessage: () -> Unit,
    onChangeProfileImage: () -> Unit,
    onRefresh: () -> Unit,
    onClickPostSetting: (PostUIState) -> Unit,
    onCreatePost: () -> Unit,
    onClickProfile: (Int) -> Unit,
    onRetry: () -> Unit,
    onClickFriends: (Int) -> Unit,
    onOpenLinkClick: (String) -> Unit,
    onEditUserInformation: () -> Unit
) {

    ManualPager(
        onRefresh = onRefresh,
        contentPadding = PaddingValues(bottom = 16.dp),
        isLoading = state.loading,
        error = state.minorError,
        isEndOfPager = state.isEndOfPager
    ) {

        item(key = state.userDetails.userID) {
            ProfileDetailsSection(
                state.userDetails,
                modifier = Modifier.nonRippleEffect { onEditUserInformation() },
                onChangeProfileImage = onChangeProfileImage,
                onSendRequestClick = onClickAddFriend
            )
        }
        item(key = state.friends) {
            FriendsSection(
                state.friends,
                totalFriends = state.totalFriends,
                modifier = Modifier
                    .nonRippleEffect { onClickFriends(state.userDetails.userID) }
                    .padding(horizontal = 16.dp)
            )
        }
        if (state.isMyProfile || state.userDetails.areFriends) {
            item {
                PostCreatingSection(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    onCreatePost = if (state.isMyProfile) {
                        onCreatePost
                    } else {
                        onClickSendMessage
                    },
                    isMyProfile = state.isMyProfile
                )
            }
        }
        items(state.posts) {
            PostItem(
                state = it,
                isMyPost = true,
                isContentExpandable = true,
                isClubPost = false,
                showGroupName = false,
                onClickLike = onClickLike,
                onClickComment = onClickComment,
                onClickSave = onClickSave,
                onClickPostSetting = onClickPostSetting,
                onClickProfile = onClickProfile,
                onOpenLinkClick = onOpenLinkClick,
            )
        }
    }
}
