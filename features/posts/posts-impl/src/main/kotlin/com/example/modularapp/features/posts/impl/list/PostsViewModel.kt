package com.example.modularapp.features.posts.impl.list

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
internal data class PostsState(
    val isLoading: Boolean = true,
    val posts: List<Post> = emptyList(),
    val failure: PostsFailure? = null,
)

internal sealed interface PostsEvent {
    data object Retry : PostsEvent

    data class PostClicked(val postId: Int) : PostsEvent
}

internal sealed interface PostsEffect {
    data class OpenDetails(val postId: Int) : PostsEffect
}

internal class PostsViewModel(
    private val repository: PostsRepository,
) : AppStateViewModel<PostsState, PostsEvent, PostsEffect>(PostsState()) {

    init {
        load()
    }

    override fun onEvent(event: PostsEvent) {
        when (event) {
            PostsEvent.Retry -> load()
            is PostsEvent.PostClicked -> emitEffect(PostsEffect.OpenDetails(event.postId))
        }
    }

    private fun load() {
        emit(currentState.copy(isLoading = true, failure = null))
        viewModelScope.launch {
            try {
                emit(PostsState(isLoading = false, posts = repository.posts()))
            } catch (failure: PostsFailure) {
                emit(currentState.copy(isLoading = false, failure = failure))
            }
        }
    }
}
