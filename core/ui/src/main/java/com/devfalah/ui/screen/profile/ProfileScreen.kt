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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.devfalah.ui.screen.profile.composable.*
import com.devfalah.ui.theme.LightCardBackgroundColor
import com.devfalah.ui.theme.LightCardColor
import com.devfalah.ui.theme.LightPrimaryBrandColor
import com.devfalah.viewmodels.Constants
import com.devfalah.viewmodels.userProfile.PostUIState
import com.devfalah.viewmodels.userProfile.ProfileViewModel
import com.devfalah.viewmodels.userProfile.UserUIState
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.SwipeRefreshState
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
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

    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let { viewModel.onClickChangeImage(createFileFromContentUri(it, context)) }
        }
    )

    ProfileContent(
        state,
        swipeRefreshState = rememberSwipeRefreshState(isRefreshing = state.loading),
        onClickLike = viewModel::onClickLike,
        onClickComment = viewModel::onClickComment,
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
    )
}

@Composable
fun ProfileContent(
    state: UserUIState,
    swipeRefreshState: SwipeRefreshState,
    onClickLike: (PostUIState) -> Unit,
    onClickComment: (PostUIState) -> Unit,
    onClickSave: (PostUIState) -> Unit,
    onClickAddFriend: () -> Unit,
    onClickSendMessage: () -> Unit,
    onChangeProfileImage: () -> Unit,
    onRefresh: (Int) -> Unit,
    onClickPostSetting: (PostUIState) -> Unit
) {
    val scrollState = rememberLazyListState()
    loadMore(scrollState, onRefresh = onRefresh, items = state.posts)
    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = { onRefresh(Constants.SWIPE_UP) },
        indicatorAlignment = Alignment.BottomCenter,
        indicator = { state, refreshTrigger ->
            SwipeRefreshIndicator(
                state = state,
                refreshTriggerDistance = refreshTrigger,
                backgroundColor = Color.Transparent,
                contentColor = LightPrimaryBrandColor
            )
        },
    ) {
        LazyColumn(
            modifier = Modifier
                .background(LightCardBackgroundColor)
                .fillMaxSize(),
            state = scrollState,
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(key = state.userDetails.userID) {
                ProfileDetailsSection(
                    state.userDetails,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    onChangeProfileImage = onChangeProfileImage
                )
            }
            if (!state.isMyProfile) {
                item(key = state.userDetails.areFriends) {
                    FriendOptionsSection(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        areFriends = state.userDetails.areFriends,
                        onClickAddFriend = onClickAddFriend,
                        onClickSendMessage = onClickSendMessage
                    )
                }
            }
            item { FriendsSection(state.friends, modifier = Modifier.padding(horizontal = 16.dp)) }
            item {
                PostCreatingSection(
                    state.userDetails,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            items(state.posts) {
                PostItem(
                    state = it,
                    onClickLike = { onClickLike(it) },
                    onClickComment = { onClickComment(it) },
                    onClickSave = { onClickSave(it) },
                    onClickPostSetting = { onClickPostSetting(it) }
                )
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
private fun createFileFromContentUri(fileUri: Uri, context: Context): File {
    var fileName = ""
    fileUri.let { returnUri ->
        context.contentResolver.query(returnUri, null, null, null)
    }?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        cursor.moveToFirst()
        fileName = cursor.getString(nameIndex)
    }

    val iStream = context.contentResolver.openInputStream(fileUri)!!
    val outputDir = context.cacheDir!!

    val outputFile = File(outputDir, fileName)
    copyStreamToFile(iStream, outputFile)
    iStream.close()
    return outputFile
}

private fun copyStreamToFile(inputStream: InputStream, outputFile: File) {
    inputStream.use { input ->
        val outputStream = FileOutputStream(outputFile)
        outputStream.use { output ->
            val buffer = ByteArray(4 * 1024)
            while (true) {
                val byteCount = input.read(buffer)
                if (byteCount < 0) break
                output.write(buffer, 0, byteCount)
            }
            output.flush()
        }
    }
}

@Composable
private fun LazyListState.isScrollingUp(): Boolean {
    var previousIndex by remember(this) { mutableStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember(this) { mutableStateOf(firstVisibleItemScrollOffset) }
    return remember(this) {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value
}

@Composable
private fun loadMore(
    scrollState: LazyListState,
    items: List<PostUIState>,
    onRefresh: (Int) -> Unit
) {
    val comparedItemIndex = if (items.size > 5) {
        items.size.minus(5)
    } else {
        items.lastIndex
    }

    if (!scrollState.isScrollingUp() && !scrollState.isScrollInProgress
        && comparedItemIndex > 0 && scrollState.firstVisibleItemIndex < items.lastIndex
        && (items[scrollState.firstVisibleItemIndex].postId != items[comparedItemIndex].postId)
    ) {
        onRefresh(Constants.SWIPE_DOWN)
    }
}