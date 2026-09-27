package com.example.modularapp.features.posts.impl

import app.cash.turbine.test
import com.example.modularapp.features.posts.impl.domain.Post
import com.example.modularapp.features.posts.impl.domain.PostsFailure
import com.example.modularapp.features.posts.impl.domain.PostsRepository
import com.example.modularapp.features.posts.impl.list.PostsEffect
import com.example.modularapp.features.posts.impl.list.PostsEvent
import com.example.modularapp.features.posts.impl.list.PostsState
import com.example.modularapp.features.posts.impl.list.PostsViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class PostsViewModelTest {
    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val post = Post(id = 7, title = "Title", body = "Body")

    private class FakeRepository(var result: () -> List<Post>) : PostsRepository {
        override suspend fun posts(): List<Post> = result()

        override suspend fun post(id: Int): Post = result().first { it.id == id }
    }

    @Test
    fun `loads posts on creation`() {
        val viewModel = PostsViewModel(FakeRepository { listOf(post) })

        assertEquals(PostsState(isLoading = false, posts = listOf(post)), viewModel.state.value)
    }

    @Test
    fun `a failure is kept as a value and retry recovers`() {
        val repository = FakeRepository { throw PostsFailure.NoConnection }
        val viewModel = PostsViewModel(repository)
        assertEquals(PostsFailure.NoConnection, viewModel.state.value.failure)

        repository.result = { listOf(post) }
        viewModel.dispatch(PostsEvent.Retry)

        assertEquals(PostsState(isLoading = false, posts = listOf(post)), viewModel.state.value)
    }

    @Test
    fun `tapping a post asks the screen to open it`() = runTest {
        val viewModel = PostsViewModel(FakeRepository { listOf(post) })

        viewModel.dispatch(PostsEvent.PostClicked(7))

        viewModel.effects.test { assertEquals(PostsEffect.OpenDetails(7), awaitItem()) }
    }
}
