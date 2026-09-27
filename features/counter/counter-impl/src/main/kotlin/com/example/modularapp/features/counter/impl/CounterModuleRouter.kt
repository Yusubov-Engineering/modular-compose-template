package com.example.modularapp.features.counter.impl

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.example.modularapp.core.logger.AppLogger
import com.example.modularapp.core.router.ModuleRouter
import com.example.modularapp.features.counter.impl.counter.CounterScreen
import com.example.modularapp.features.counter.impl.counter.CounterViewModel
import com.example.modularapp.features.counter.impl.details.CounterDetailsScreen
import com.example.modularapp.features.counter.impl.router.CounterDetailsRoute
import com.example.modularapp.features.counter.impl.router.CounterRoute
import com.example.modularapp.features.posts.PostsApi
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.subclass

/** The counter route table, and where its view models are built. */
class CounterModuleRouter internal constructor(
    private val postsApi: PostsApi,
    private val logger: AppLogger,
) : ModuleRouter {

    override fun PolymorphicModuleBuilder<NavKey>.registerRoutes() {
        subclass(CounterRoute::class)
        subclass(CounterDetailsRoute::class)
    }

    override fun EntryProviderScope<NavKey>.entries() {
        entry<CounterRoute> {
            CounterScreen(
                viewModel = viewModel { CounterViewModel(logger) },
                postsLauncher = postsApi.launcher,
            )
        }
        entry<CounterDetailsRoute> { route ->
            CounterDetailsScreen(count = route.count)
        }
    }
}
