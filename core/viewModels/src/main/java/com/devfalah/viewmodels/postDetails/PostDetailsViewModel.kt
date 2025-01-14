package com.devfalah.viewmodels.postDetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devfalah.usecases.posts.*
import com.devfalah.usecases.user.GetUserAccountDetailsUseCase
import com.devfalah.viewmodels.postDetails.mapper.toUIState
import com.devfalah.viewmodels.userProfile.PostUIState
import com.devfalah.viewmodels.userProfile.mapper.toEntity
import com.devfalah.viewmodels.userProfile.mapper.toUIState
import com.devfalah.viewmodels.util.Constants.MAX_PAGE_ITEM
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostDetailsViewModel @Inject constructor(
    val getPostCommentsUseCase: GetPostCommentsUseCase,
    val getPostDetailsUseCase: GetPostDetailsUseCase,
    val mangeComment: ManageCommentUseCase,
    val commentLike: SetCommentLikeUseCase,
    val favoritePostUseCase: SetFavoritePostUseCase,
    val postLike: SetPostLikeUseCase,
    val deletePostUseCase: DeletePostUseCase,
    val publisherDetails: GetUserAccountDetailsUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(PostDetailsUIState())
    val uiState = _uiState.asStateFlow()
    private val args = PostDetailsArgs(savedStateHandle)

    init {
        getData()
    }

    fun getData() {
        getPostDetails(args.postId)
    }

    //region Post
    private fun getPublisherDetails(publisherId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(minorError = "") }
            try {
                val user = publisherDetails(publisherId)
                _uiState.update {
                    it.copy(
                        post = it.post.copy(
                            publisherId = publisherId,
                            publisherName = user.name,
                            publisherImage = user.profileUrl,
                        )
                    )
                }
            } catch (t: Throwable) {
                _uiState.update { it.copy(minorError = t.message.toString()) }
            }
        }
    }

    private fun getPostDetails(postId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = "") }
            try {
                val post = getPostDetailsUseCase(postId)
                _uiState.update { it.copy(post = post.toUIState(), isLoading = false) }
                getPublisherDetails(args.publisherId)
                getPostComments()
            } catch (t: Throwable) {
                _uiState.update { it.copy(isLoading = false, error = t.message.toString()) }
            }
        }
    }

    fun onClickLikePost(post: PostUIState) {
        viewModelScope.launch {
            _uiState.update { it.copy(error = "") }
            try {
                val totalLikes = postLike(args.postId, post.isLikedByUser)
                _uiState.update {
                    it.copy(
                        post = post.copy(
                            isLikedByUser = !post.isLikedByUser, totalLikes = totalLikes
                        )
                    )
                }
            } catch (t: Throwable) {
                _uiState.update { it.copy(error = t.message.toString()) }
            }
        }
    }

    fun onClickSavePost(post: PostUIState) {
        viewModelScope.launch {
            try {
                favoritePostUseCase(post.toEntity())
                _uiState.update { it.copy(post = it.post.copy(isSaved = !post.isSaved)) }
            } catch (t: Throwable) {
                t.message.toString()
            }
        }
    }

    fun onDeletePost(post: PostUIState) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                if (deletePostUseCase(post.postId)) {
                    _uiState.update { it.copy(isPostDeleted = true) }
                }
            } catch (t: Throwable) {
                _uiState.update { it.copy(error = t.message.toString()) }
            }
        }
    }
    //endregion

    //region Comment
    fun onCommentValueChanged(comment: String) {
        _uiState.update { it.copy(commentText = comment) }
    }

    fun onClickLikeComment(comment: CommentUIState) {
        viewModelScope.launch {
            _uiState.update { it.copy(minorError = "") }
            try {
                val totalLike = commentLike(
                    commentId = comment.id, isLiked = comment.isLikedByUser
                )
                val comments = uiState.value.comments.map {
                    if (it.id == comment.id) {
                        comment.copy(totalLikes = totalLike, isLikedByUser = !it.isLikedByUser)
                    } else {
                        it
                    }
                }
                _uiState.update {
                    it.copy(comments = comments)
                }
            } catch (t: Throwable) {
                _uiState.update { it.copy(minorError = t.message.toString()) }
            }
        }
    }

    //need add loading on button like editProfile...
    fun onClickSendComment() {
        val commentText = _uiState.value.commentText
        _uiState.update { it.copy(commentText = "", minorError = "") }
        viewModelScope.launch {
            try {
                val comment = mangeComment.addComment(args.postId, commentText)
                _uiState.update {
                    it.copy(
                        commentText = "",
                        post = it.post.copy(totalComments = it.post.totalComments + 1),
                        comments = it.comments + comment.toUIState()
                    )
                }
            } catch (t: Throwable) {
                _uiState.update { it.copy(minorError = t.message.toString()) }
            }
        }
    }

    fun getPostComments() {
        _uiState.update { it.copy(isPagerLoading = true, minorError = "", error = "") }
        viewModelScope.launch {
            try {
                if (!uiState.value.isEndOfPager) {
                    val comments = getPostCommentsUseCase(args.postId)
                    _uiState.update {
                        it.copy(
                            comments = (it.comments + comments.toUIState().distinctBy { it.id }),
                            isEndOfPager = (comments.isEmpty() || comments.size < MAX_PAGE_ITEM),
                            isPagerLoading = false,
                            isLoading = false
                        )
                    }
                }
            } catch (t: Throwable) {
                _uiState.update {
                    it.copy(
                        isPagerLoading = false, isLoading = false, minorError = t.message.toString()
                    )
                }
            }
        }
    }

    fun onClickDeleteComment(comment: CommentUIState) {
        viewModelScope.launch {
            _uiState.update { it.copy(minorError = "") }
            try {
                val isDeleted = mangeComment.deleteComment(commentId = comment.id)
                if (isDeleted) {
                    _uiState.update {
                        it.copy(
                            comments = it.comments.filterNot { it.id == comment.id },
                            post = it.post.copy(totalComments = (it.post.totalComments - 1))
                        )
                    }
                }
            } catch (t: Throwable) {
                _uiState.update { it.copy(minorError = t.message.toString()) }
            }
        }
    }
    //endregion

}