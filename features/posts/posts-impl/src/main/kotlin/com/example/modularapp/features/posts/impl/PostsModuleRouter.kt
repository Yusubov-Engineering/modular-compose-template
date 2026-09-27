package com.example.modularapp.features.posts.impl

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.example.modularapp.core.router.ModuleRouter
import com.example.modularapp.features.posts.impl.details.PostDetailsScreen
import com.example.modularapp.features.posts.impl.details.PostDetailsViewModel
import com.example.modularapp.features.posts.impl.domain.PostsRepository
import com.example.modularapp.features.posts.impl.list.PostsScreen
import com.example.modularapp.features.posts.impl.list.PostsViewModel
import com.example.modularapp.features.posts.impl.router.PostDetailsRoute
import com.example.modularapp.features.posts.impl.router.PostsRoute
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.subclass

/**
 * The posts route table — and the one place this feature's view models are
 * built. Koin hands it its dependencies (see PostsKoinModule), and it passes
 * them to each view model's constructor, so no screen
 * or view model ever touches a container.
 */
class PostsModuleRouter internal constructor(
    private val repository: PostsRepository,
) : ModuleRouter {

    override fun PolymorphicModuleBuilder<NavKey>.registerRoutes() {
        subclass(PostsRoute::class)
        subclass(PostDetailsRoute::class)
    }

    override fun EntryProviderScope<NavKey>.entries() {
        entry<PostsRoute> {
            PostsScreen(viewModel = viewModel { PostsViewModel(repository) })
        }
        entry<PostDetailsRoute> { route ->
            PostDetailsScreen(viewModel = viewModel { PostDetailsViewModel(route.postId, repository) })
        }
    }
}
