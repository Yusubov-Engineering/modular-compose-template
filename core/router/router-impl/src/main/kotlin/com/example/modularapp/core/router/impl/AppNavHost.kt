package com.example.modularapp.core.router.impl

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.example.modularapp.core.designsystem.theme.AppMotion
import com.example.modularapp.core.designsystem.theme.AppTheme
import com.example.modularapp.core.router.AppRoute
import com.example.modularapp.core.router.LocalAppNavigator
import com.example.modularapp.core.router.ModuleRouter
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

/**
 * The app's navigation root: one back stack, every feature's routes.
 *
 * - The back stack survives configuration changes and process death. That is
 *   why routes are `@Serializable` and every [ModuleRouter] registers them.
 * - Each entry gets its own `ViewModelStore`, so a screen's view model lives
 *   exactly as long as the screen is on the stack.
 * - Page transitions are set here, once, and honour reduced motion.
 */
@Composable
fun AppNavHost(
    initialRoute: AppRoute,
    moduleRouters: List<ModuleRouter>,
    modifier: Modifier = Modifier,
) {
    val configuration = remember(moduleRouters) {
        SavedStateConfiguration {
            serializersModule = SerializersModule {
                polymorphic(NavKey::class) {
                    moduleRouters.forEach { router -> with(router) { registerRoutes() } }
                }
            }
        }
    }
    val backStack = rememberNavBackStack(configuration, initialRoute)
    val navigator = remember(backStack) { BackStackNavigator(backStack) }
    val provider = remember(moduleRouters) {
        entryProvider<NavKey> {
            moduleRouters.forEach { router -> with(router) { entries() } }
        }
    }
    val motion = AppTheme.motion

    CompositionLocalProvider(LocalAppNavigator provides navigator) {
        NavDisplay(
            backStack = backStack,
            modifier = modifier,
            // NavDisplay only intercepts back while there is something to pop;
            // at the root, back falls through to the activity and closes it.
            onBack = navigator::pop,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            transitionSpec = { fadeThrough(motion) },
            popTransitionSpec = { fadeThrough(motion) },
            predictivePopTransitionSpec = { fadeThrough(motion) },
            entryProvider = provider,
        )
    }
}

private fun fadeThrough(motion: AppMotion): ContentTransform =
    fadeIn(tween(motion.medium, delayMillis = motion.short, easing = motion.standard)) togetherWith
        fadeOut(tween(motion.short, easing = motion.standard))
