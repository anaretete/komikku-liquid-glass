package com.kyant.backdrop.catalog.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceAtMost
import androidx.compose.ui.util.lerp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.rememberCanvasBackdrop
import com.kyant.backdrop.catalog.utils.InteractiveHighlight
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import tachiyomi.presentation.core.components.material.LocalLayerBackdrop
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tanh

/**
 * Authentic Apple iOS 26 Circular Liquid Glass Icon Button.
 * Uses Kyant0's GPU SDF lens shaders, interactive highlights, and spring damping.
 */
@Composable
fun LiquidIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: Dp = 40.dp,
    shape: Shape = CircleShape,
    backdrop: Backdrop? = null,
    isInteractive: Boolean = true,
    tint: Color = Color.Unspecified,
    surfaceColor: Color = Color.Unspecified,
    content: @Composable BoxScope.() -> Unit,
) {
    val isDark = isSystemInDarkTheme()
    val effectiveBackdrop = backdrop ?: LocalLayerBackdrop.current ?: rememberCanvasBackdrop {
        drawRect(if (isDark) Color(0xFF141416) else Color(0xFFF2F2F6))
    }
    val animationScope = rememberCoroutineScope()
    val interactiveHighlight = remember(animationScope) {
        InteractiveHighlight(animationScope = animationScope)
    }

    val defaultSurfaceColor = if (surfaceColor.isSpecified) surfaceColor else {
        if (isDark) Color(0xFF141416).copy(alpha = 0.55f) else Color(0xFFFFFFFF).copy(alpha = 0.65f)
    }

    Box(
        modifier = modifier
            .size(size)
            .alpha(if (enabled) 1f else 0.4f)
            .drawBackdrop(
                backdrop = effectiveBackdrop,
                shape = { shape },
                effects = {
                    vibrancy()
                    blur(16.dp.toPx())
                    lens(14.dp.toPx(), 24.dp.toPx(), depthEffect = true, chromaticAberration = true)
                },
                layerBlock = if (isInteractive && enabled) {
                    {
                        val width = this.size.width
                        val height = this.size.height

                        val progress = interactiveHighlight.pressProgress
                        val scale = lerp(1f, 1f + 4.dp.toPx() / this.size.height, progress)

                        val maxOffset = this.size.minDimension
                        val initialDerivative = 0.05f
                        val offset = interactiveHighlight.offset
                        translationX = maxOffset * tanh(initialDerivative * offset.x / maxOffset)
                        translationY = maxOffset * tanh(initialDerivative * offset.y / maxOffset)

                        val maxDragScale = 4.dp.toPx() / this.size.height
                        val offsetAngle = atan2(offset.y, offset.x)
                        scaleX = scale + maxDragScale * abs(cos(offsetAngle) * offset.x / this.size.maxDimension) *
                            (width / height).fastCoerceAtMost(1f)
                        scaleY = scale + maxDragScale * abs(sin(offsetAngle) * offset.y / this.size.maxDimension) *
                            (height / width).fastCoerceAtMost(1f)
                    }
                } else null,
                onDrawSurface = {
                    if (tint.isSpecified) {
                        drawRect(tint, blendMode = BlendMode.Hue)
                        drawRect(tint.copy(alpha = 0.75f))
                    }
                    drawRect(defaultSurfaceColor)
                },
            )
            .clickable(
                interactionSource = null,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .then(
                if (isInteractive && enabled) {
                    Modifier
                        .then(interactiveHighlight.modifier)
                        .then(interactiveHighlight.gestureModifier)
                } else Modifier
            ),
        contentAlignment = Alignment.Center,
        content = content,
    )
}
