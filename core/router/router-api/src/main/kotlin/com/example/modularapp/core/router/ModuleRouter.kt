package com.example.modularapp.core.router

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.modules.PolymorphicModuleBuilder

/**
 * One feature's route table. Each -impl module has exactly one, and the app
 * hands the list of them to `AppNavHost`.
 *
 * ```
 * override fun PolymorphicModuleBuilder<NavKey>.registerRoutes() {
 *     subclass(PostsRoute::class)
 *     subclass(PostDetailsRoute::class)
 * }
 *
 * override fun EntryProviderScope<NavKey>.entries() {
 *     entry<PostsRoute> { PostsScreen(...) }
 *     entry<PostDetailsRoute> { route -> PostDetailsScreen(route.id, ...) }
 * }
 * ```
 *
 * A route missing from [registerRoutes] still navigates, and then crashes
 * the first time the back stack is saved — rotate the device, or leave the
 * app in the background. Register every route in both functions.
 */
interface ModuleRouter {
    /** Registers each route's serializer, so the back stack can be saved and restored. */
    fun PolymorphicModuleBuilder<NavKey>.registerRoutes()

    /** Maps each route to the screen it shows. */
    fun EntryProviderScope<NavKey>.entries()
}
