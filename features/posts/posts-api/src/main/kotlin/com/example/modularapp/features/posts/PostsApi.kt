package com.example.modularapp.features.posts

import com.example.modularapp.core.router.AppRoute

/**
 * Everything other modules may use from the posts feature. Other features
 * depend on `posts-api` and receive this through DI; never on `posts-impl`.
 *
 * Use-case interfaces belong here too, as other features come to need them.
 */
interface PostsApi {
    val launcher: PostsLauncher
}

/**
 * The places this feature can be entered at. A launcher *returns* a route
 * rather than navigating, so the caller picks the verb (`push`, `goTo`,
 * `replace`) and the launcher needs no navigator.
 */
interface PostsLauncher {
    fun posts(): AppRoute

    fun postDetails(postId: Int): AppRoute
}
