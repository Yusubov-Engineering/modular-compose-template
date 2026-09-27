package com.example.modularapp.features.posts.impl.details

import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import com.example.modularapp.core.statemanager.AppStateViewModel
import com.example.modularapp.features.posts.impl.domain.Post
import com.example.modularapp.features.posts.impl.domain.PostsFailure
import com.example.modularapp.features.posts.impl.domain.PostsRepository
import kotlinx.coroutines.launch

// @Immutable: PostsFailure extends Exception, which Compose cannot prove
// immutable. It never changes after it is thrown, and state is replaced rather
// than edited — so this promise holds, and screens can skip by equality.
@Immutable
internal data class PostDetailsState(
    val isLoading: Boolean = true,
    val post: Post? = null,
    val failure: PostsFailure? = null,
)

internal sealed interface PostDetailsEvent {
    data object Retry : PostDetailsEvent
}

/** No effects: the screen only goes back, which needs no view model. */
internal class PostDetailsViewModel(
    private val postId: Int,
    private val repository: PostsRepository,
) : AppStateViewModel<PostDetailsState, PostDetailsEvent, Nothing>(PostDetailsState()) {

    init {
        load()
    }

    override fun onEvent(event: PostDetailsEvent) {
        when (event) {
            PostDetailsEvent.Retry -> load()
        }
    }

    private fun load() {
        emit(PostDetailsState(isLoading = true))
        viewModelScope.launch {
            try {
                emit(PostDetailsState(isLoading = false, post = repository.post(postId)))
            } catch (failure: PostsFailure) {
                emit(PostDetailsState(isLoading = false, failure = failure))
            }
        }
    }
}
