package com.example.modularapp.core.router

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Moves around the back stack. Screens get it from [LocalAppNavigator];
 * view models never see it — navigation is an effect the screen performs.
 *
 * The caller picks the verb; a launcher only says *where*:
 *
 * ```
 * navigator.push(postsApi.launcher.postDetails(id))  // on top
 * navigator.goTo(postsApi.launcher.posts())          // as the only entry
 * ```
 */
interface AppNavigator {
    val backStack: List<AppRoute>

    /** Adds [route] on top. */
    fun push(route: AppRoute)

    /** Replaces the top entry with [route]. */
    fun replace(route: AppRoute)

    /** Clears the stack and makes [route] its only entry. */
    fun goTo(route: AppRoute)

    /** Whether [pop] would do anything — false at the root. */
    val canPop: Boolean

    /** Removes the top entry. Does nothing at the root: the stack is never empty. */
    fun pop()

    /** Pops until the top satisfies [predicate]; the root is never popped. */
    fun popUntil(predicate: (AppRoute) -> Boolean)
}

val LocalAppNavigator = staticCompositionLocalOf<AppNavigator> {
    error("No AppNavigator provided. Screens must be shown inside AppNavHost.")
}
