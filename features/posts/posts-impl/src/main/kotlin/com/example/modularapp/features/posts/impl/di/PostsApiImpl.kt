package com.example.modularapp.features.posts.impl.di

import com.example.modularapp.core.router.AppRoute
import com.example.modularapp.features.posts.PostsApi
import com.example.modularapp.features.posts.PostsLauncher
import com.example.modularapp.features.posts.impl.router.PostDetailsRoute
import com.example.modularapp.features.posts.impl.router.PostsRoute

internal class PostsApiImpl : PostsApi {
    override val launcher: PostsLauncher = PostsLauncherImpl()
}

internal class PostsLauncherImpl : PostsLauncher {
    override fun posts(): AppRoute = PostsRoute

    override fun postDetails(postId: Int): AppRoute = PostDetailsRoute(postId)
}
