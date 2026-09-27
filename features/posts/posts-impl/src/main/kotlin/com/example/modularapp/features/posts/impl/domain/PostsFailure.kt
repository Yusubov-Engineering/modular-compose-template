package com.example.modularapp.features.posts.impl.domain

/**
 * Why loading posts failed, in this feature's terms. Sealed: adding a kind
 * fails to compile everywhere it is turned into text, rather than quietly
 * becoming "something went wrong".
 *
 * A value, not a string — a view model has no `Context` to localise with;
 * the screen turns it into a sentence (see `shared/PostsFailureText.kt`).
 */
internal sealed class PostsFailure : Exception() {
    data object NoConnection : PostsFailure()

    data object NotFound : PostsFailure()

    data object Unavailable : PostsFailure()

    data object Malformed : PostsFailure()
}
