package com.example.modularapp.features.posts.impl.shared

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.modularapp.features.posts.impl.R
import com.example.modularapp.features.posts.impl.domain.PostsFailure

/** A failure as a sentence — at the layer that has resources. Exhaustive by design. */
@Composable
internal fun PostsFailure.message(): String = stringResource(
    when (this) {
        PostsFailure.NoConnection -> R.string.posts_failure_no_connection
        PostsFailure.NotFound -> R.string.posts_failure_not_found
        PostsFailure.Unavailable -> R.string.posts_failure_unavailable
        PostsFailure.Malformed -> R.string.posts_failure_malformed
    },
)
