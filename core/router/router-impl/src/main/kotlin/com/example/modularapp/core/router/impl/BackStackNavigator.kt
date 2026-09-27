package com.example.modularapp.core.router.impl

import androidx.navigation3.runtime.NavKey
import com.example.modularapp.core.router.AppNavigator
import com.example.modularapp.core.router.AppRoute

/**
 * [AppNavigator] over a Navigation 3 back stack — which is just a snapshot
 * state list, so every change recomposes `NavDisplay`.
 */
internal class BackStackNavigator(private val stack: MutableList<NavKey>) : AppNavigator {

    override val backStack: List<AppRoute>
        get() = stack.filterIsInstance<AppRoute>()

    override fun push(route: AppRoute) {
        stack.add(route)
    }

    override fun replace(route: AppRoute) {
        if (stack.isEmpty()) stack.add(route) else stack[stack.lastIndex] = route
    }

    override fun goTo(route: AppRoute) {
        // Add first, then trim: the stack is never empty, so NavDisplay never
        // sees a frame with nothing to show.
        stack.add(route)
        while (stack.size > 1) stack.removeAt(0)
    }

    override val canPop: Boolean
        get() = stack.size > 1

    override fun pop() {
        if (canPop) stack.removeAt(stack.lastIndex)
    }

    override fun popUntil(predicate: (AppRoute) -> Boolean) {
        while (stack.size > 1 && !(stack.last() as AppRoute).let(predicate)) {
            stack.removeAt(stack.lastIndex)
        }
    }
}
