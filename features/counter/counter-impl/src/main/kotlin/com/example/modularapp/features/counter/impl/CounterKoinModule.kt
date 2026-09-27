package com.example.modularapp.features.counter.impl

import com.example.modularapp.core.logger.AppLogger
import com.example.modularapp.core.router.ModuleRouter
import com.example.modularapp.features.counter.CounterApi
import com.example.modularapp.features.counter.impl.di.CounterApiImpl
import com.example.modularapp.features.posts.PostsApi
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

/** Needs: [PostsApi], [AppLogger]. Provides: [CounterApi] and a [ModuleRouter]. */
@Module
class CounterKoinModule {
    @Single
    fun counterApi(): CounterApi = CounterApiImpl()

    @Single(binds = [ModuleRouter::class])
    fun moduleRouter(postsApi: PostsApi, logger: AppLogger): CounterModuleRouter =
        CounterModuleRouter(postsApi, logger)
}
