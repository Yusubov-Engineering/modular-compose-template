package com.example.modularapp.core.network.impl

import com.example.modularapp.core.logger.AppLogger
import com.example.modularapp.core.network.AppResult
import com.example.modularapp.core.network.NetworkError
import com.example.modularapp.core.network.get
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class KtorNetworkClientTest {
    @Serializable
    private data class Item(val id: Int)

    private fun client(handler: MockRequestHandler) = KtorNetworkClient(
        HttpClientFactory.create(
            config = NetworkConfig(baseUrl = "https://example.test/api", enableLogging = false),
            logger = AppLogger.Silent,
            engine = MockEngine(handler),
        ),
    )

    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

    @Test
    fun `resolves paths under the base url and decodes the body`() = runTest {
        var requested = ""
        val result = client { request ->
            requested = request.url.toString()
            respond("""[{"id":1,"extra":"ignored"}]""", HttpStatusCode.OK, jsonHeaders)
        }.get<List<Item>>("items", query = mapOf("page" to "2"))

        assertEquals("https://example.test/api/items?page=2", requested)
        assertEquals(AppResult.Success(listOf(Item(1))), result)
    }

    @Test
    fun `a non-2xx status is an Http failure, not an exception`() = runTest {
        val result = client { respond("", HttpStatusCode.NotFound) }.get<Item>("missing")

        assertEquals(AppResult.Failure(NetworkError.Http(404)), result)
    }

    @Test
    fun `a body of the wrong shape is a Serialization failure`() = runTest {
        val result = client { respond("""{"name":"no id"}""", HttpStatusCode.OK, jsonHeaders) }.get<Item>("item")

        assertTrue((result as AppResult.Failure).error is NetworkError.Serialization)
    }

    @Test
    fun `an IO error is NoConnection`() = runTest {
        val result = client { throw IOException("offline") }.get<Item>("item")

        assertEquals(AppResult.Failure(NetworkError.NoConnection), result)
    }
}
