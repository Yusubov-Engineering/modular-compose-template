package com.example.modularapp.features.posts.impl

import com.example.modularapp.core.network.NetworkClient
import com.example.modularapp.core.router.ModuleRouter
import com.example.modularapp.features.posts.PostsApi
import com.example.modularapp.features.posts.impl.data.PostsRepositoryImpl
import com.example.modularapp.features.posts.impl.di.PostsApiImpl
import com.example.modularapp.features.posts.impl.domain.PostsRepository
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

/**
 * Needs: [NetworkClient]. Provides: [PostsApi], and a [ModuleRouter] the app
 * collects with `getAll()`.
 */
@Module
class PostsKoinModule {
    @Single
    fun postsApi(): PostsApi = PostsApiImpl()

    @Single
    internal fun repository(client: NetworkClient): PostsRepository = PostsRepositoryImpl(client)

    @Single(binds = [ModuleRouter::class])
    internal fun moduleRouter(repository: PostsRepository): PostsModuleRouter = PostsModuleRouter(repository)
}
