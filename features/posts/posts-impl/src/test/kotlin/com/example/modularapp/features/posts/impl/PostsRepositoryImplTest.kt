package com.example.modularapp.features.posts.impl

import com.example.modularapp.core.network.AppResult
import com.example.modularapp.core.network.NetworkError
import com.example.modularapp.features.posts.impl.data.PostsRepositoryImpl
import com.example.modularapp.features.posts.impl.domain.Post
import com.example.modularapp.features.posts.impl.domain.PostsFailure
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class PostsRepositoryImplTest {
    @Test
    fun `maps DTOs to trimmed domain models`() = runTest {
        val client = FakeNetworkClient { AppResult.Success("""[{"id":1,"title":" Hi ","body":"there\n"}]""") }

        val posts = PostsRepositoryImpl(client).posts()

        assertEquals(listOf("posts"), client.requestedPaths)
        assertEquals(listOf(Post(id = 1, title = "Hi", body = "there")), posts)
    }

    @Test
    fun `every network error becomes a PostsFailure`() = runTest {
        val cases = mapOf(
            NetworkError.NoConnection to PostsFailure.NoConnection,
            NetworkError.Http(404) to PostsFailure.NotFound,
            NetworkError.Http(500) to PostsFailure.Unavailable,
            NetworkError.Timeout to PostsFailure.Unavailable,
            NetworkError.Serialization(IllegalStateException()) to PostsFailure.Malformed,
        )

        cases.forEach { (error, expected) ->
            val repository = PostsRepositoryImpl(FakeNetworkClient { AppResult.Failure(error) })
            val thrown = runCatching { repository.post(1) }.exceptionOrNull()
            assertEquals("for $error", expected, thrown)
        }
    }
}
