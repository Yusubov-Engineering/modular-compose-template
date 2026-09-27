package com.example.modularapp.features.posts.impl.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.modularapp.core.designsystem.component.AppMessageView
import com.example.modularapp.core.designsystem.component.AppProgressIndicator
import com.example.modularapp.core.designsystem.component.AppScaffold
import com.example.modularapp.core.designsystem.component.AppText
import com.example.modularapp.core.designsystem.component.AppTopBar
import com.example.modularapp.core.designsystem.theme.AppTheme
import com.example.modularapp.core.router.LocalAppNavigator
import com.example.modularapp.features.posts.impl.R
import com.example.modularapp.features.posts.impl.domain.Post
import com.example.modularapp.features.posts.impl.shared.message

@Composable
internal fun PostDetailsScreen(viewModel: PostDetailsViewModel) {
    val navigator = LocalAppNavigator.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val post = state.post
    val failure = state.failure

    AppScaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.posts_details_title),
                onBack = navigator::pop,
                backLabel = stringResource(R.string.posts_back),
            )
        },
    ) {
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                AppProgressIndicator(label = stringResource(R.string.posts_loading))
            }

            failure != null -> AppMessageView(
                title = stringResource(R.string.posts_failure_title),
                message = failure.message(),
                actionLabel = stringResource(R.string.posts_retry),
                onAction = { viewModel.dispatch(PostDetailsEvent.Retry) },
            )

            post != null -> PostBody(post)
        }
    }
}

@Composable
private fun PostBody(post: Post) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(AppTheme.spacing.xl),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.lg),
    ) {
        AppText(
            text = stringResource(R.string.posts_post_number, post.id),
            style = AppTheme.typography.caption,
            color = AppTheme.accent.base,
        )
        AppText(text = post.title, style = AppTheme.typography.title)
        AppText(text = post.body, color = AppTheme.colors.textSecondary)
    }
}
