package com.example.modularapp.core.router.impl

import androidx.navigation3.runtime.NavKey
import com.example.modularapp.core.router.AppRoute
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BackStackNavigatorTest {
    private data class Route(val name: String) : AppRoute

    private val home = Route("home")
    private val list = Route("list")
    private val details = Route("details")

    private fun navigator(vararg routes: AppRoute) =
        BackStackNavigator(mutableListOf<NavKey>(*routes))

    @Test
    fun `push adds on top and pop removes it`() {
        val navigator = navigator(home)

        navigator.push(list)
        assertEquals(listOf(home, list), navigator.backStack)

        assertTrue(navigator.canPop)
        navigator.pop()
        assertEquals(listOf(home), navigator.backStack)
    }

    @Test
    fun `pop never empties the stack`() {
        val navigator = navigator(home)

        assertFalse(navigator.canPop)
        navigator.pop()
        assertEquals(listOf(home), navigator.backStack)
    }

    @Test
    fun `goTo leaves the route as the only entry`() {
        val navigator = navigator(home, list)

        navigator.goTo(details)

        assertEquals(listOf(details), navigator.backStack)
    }

    @Test
    fun `replace swaps the top entry`() {
        val navigator = navigator(home, list)

        navigator.replace(details)

        assertEquals(listOf(home, details), navigator.backStack)
    }

    @Test
    fun `popUntil stops at the match and never pops the root`() {
        val navigator = navigator(home, list, details)

        navigator.popUntil { it == list }
        assertEquals(listOf(home, list), navigator.backStack)

        navigator.popUntil { false }
        assertEquals(listOf(home), navigator.backStack)
    }
}
