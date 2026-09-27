package com.example.modularapp.core.designsystem.theme

import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.invalidateDraw
import kotlinx.coroutines.launch

/**
 * The design system's press feedback: a faint veil while pressed. Installed
 * as `LocalIndication` by [AppTheme], so any `clickable` under the theme gets
 * it — there is no Material ripple here.
 */
internal object AppPressIndication : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode =
        PressVeilNode(interactionSource)

    override fun equals(other: Any?): Boolean = other === this

    override fun hashCode(): Int = javaClass.hashCode()
}

private class PressVeilNode(private val source: InteractionSource) : Modifier.Node(), DrawModifierNode {
    private var pressed = false

    override fun onAttach() {
        coroutineScope.launch {
            var active = 0
            source.interactions.collect { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> active++
                    is PressInteraction.Release, is PressInteraction.Cancel -> active--
                }
                val isPressed = active > 0
                if (isPressed != pressed) {
                    pressed = isPressed
                    invalidateDraw()
                }
            }
        }
    }

    override fun ContentDrawScope.draw() {
        drawContent()
        if (pressed) drawRect(VEIL)
    }

    private companion object {
        val VEIL = Color.Black.copy(alpha = 0.08f)
    }
}
