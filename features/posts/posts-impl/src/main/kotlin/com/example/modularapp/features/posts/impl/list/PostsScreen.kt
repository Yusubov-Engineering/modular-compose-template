package com.example.modularapp.features.posts.impl.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.modularapp.core.designsystem.component.AppCard
import com.example.modularapp.core.designsystem.component.AppMessageView
import com.example.modularapp.core.designsystem.component.AppProgressIndicator
import com.example.modularapp.core.designsystem.component.AppScaffold
import com.example.modularapp.core.designsystem.component.AppText
import com.example.modularapp.core.designsystem.component.AppTopBar
import com.example.modularapp.core.designsystem.theme.AppTheme
import com.example.modularapp.core.router.LocalAppNavigator
import com.example.modularapp.core.statemanager.CollectEffects
import com.example.modularapp.features.posts.impl.R
import com.example.modularapp.features.posts.impl.domain.Post
import com.example.modularapp.features.posts.impl.router.PostDetailsRoute
import com.example.modularapp.features.posts.impl.shared.message

@Composable
internal fun PostsScreen(viewModel: PostsViewModel) {
    val navigator = LocalAppNavigator.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Intra-module navigation uses the route directly; no launcher needed.
    CollectEffects(viewModel.effects) { effect ->
        when (effect) {
            is PostsEffect.OpenDetails -> navigator.push(PostDetailsRoute(effect.postId))
        }
    }

    AppScaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.posts_title),
                onBack = if (navigator.canPop) navigator::pop else null,
                backLabel = stringResource(R.string.posts_back),
            )
        },
    ) {
        PostsContent(state = state, onEvent = viewModel::dispatch)
    }
}

@Composable
private fun PostsContent(state: PostsState, onEvent: (PostsEvent) -> Unit) {
    val failure = state.failure
    when {
        state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            AppProgressIndicator(label = stringResource(R.string.posts_loading))
        }

        failure != null -> AppMessageView(
            title = stringResource(R.string.posts_failure_title),
            message = failure.message(),
            actionLabel = stringResource(R.string.posts_retry),
            onAction = { onEvent(PostsEvent.Retry) },
        )

        state.posts.isEmpty() -> AppMessageView(
            title = stringResource(R.string.posts_empty_title),
            message = stringResource(R.string.posts_empty_message),
        )

        else -> PostList(posts = state.posts, onPostClick = { onEvent(PostsEvent.PostClicked(it)) })
    }
}

@Composable
private fun PostList(posts: List<Post>, onPostClick: (Int) -> Unit) {
    val spacing = AppTheme.spacing
    LazyColumn(
        contentPadding = PaddingValues(horizontal = spacing.lg, vertical = spacing.sm),
        verticalArrangement = Arrangement.spacedBy(spacing.md),
    ) {
        items(posts, key = Post::id) { post ->
            AppCard(onClick = { onPostClick(post.id) }) {
                AppText(
                    text = stringResource(R.string.posts_post_number, post.id),
                    style = AppTheme.typography.caption,
                    color = AppTheme.accent.base,
                )
                AppText(text = post.title, style = AppTheme.typography.label, maxLines = 2)
            }
        }
    }
}
