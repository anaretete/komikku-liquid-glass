package com.kyant.backdrop.catalog.components

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceAtMost
import androidx.compose.ui.util.lerp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.catalog.utils.InteractiveHighlight
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.shapes.Capsule
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tanh

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.Dp
import com.kyant.backdrop.backdrops.rememberCanvasBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import tachiyomi.presentation.core.components.material.LocalLayerBackdrop

@Composable
fun LiquidIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backdrop: Backdrop? = null,
    enabled: Boolean = true,
    isInteractive: Boolean = true,
    tint: Color = Color.Unspecified,
    surfaceColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    shape: Shape = CircleShape,
    size: Dp = 42.dp,
    content: @Composable () -> Unit,
) {
    val animationScope = rememberCoroutineScope()
    val isDark = isSystemInDarkTheme()
    val activeBackdrop = backdrop ?: LocalLayerBackdrop.current ?: rememberCanvasBackdrop {
        drawRect(if (isDark) Color(0xFF1C1C1E) else Color(0xFFF2F2F6))
    }

    val interactiveHighlight = remember(animationScope) {
        InteractiveHighlight(
            animationScope = animationScope,
        )
    }

    val defaultSurface = if (isDark) Color(0xFF2C2C2E).copy(alpha = 0.55f) else Color(0xFFE5E5EA).copy(alpha = 0.65f)
    val effectiveSurfaceColor = if (surfaceColor.isSpecified) surfaceColor else defaultSurface

    val effectiveContentColor = if (contentColor.isSpecified) {
        contentColor
    } else if (tint.isSpecified && tint != Color.Transparent) {
        Color.White
    } else {
        if (isDark) Color.White else Color.Black
    }

    Box(
        modifier = modifier
            .size(size)
            .alpha(if (enabled) 1f else 0.45f)
            .drawBackdrop(
                backdrop = activeBackdrop,
                shape = { shape },
                effects = {
                    vibrancy()
                    blur(3f.dp.toPx())
                    try {
                        lens(14f.dp.toPx(), 24f.dp.toPx(), chromaticAberration = true)
                    } catch (_: Throwable) {
                    }
                },
                layerBlock = if (isInteractive && enabled) {
                    {
                        try {
                            val h = this.size.height
                            val w = this.size.width
                            val minDim = this.size.minDimension
                            val maxDim = this.size.maxDimension
                            if (h <= 0.001f || w <= 0.001f || minDim <= 0.001f || maxDim <= 0.001f) {
                                translationX = 0f
                                translationY = 0f
                                scaleX = 1f
                                scaleY = 1f
                            } else {
                                val progress = interactiveHighlight.pressProgress
                                val scale = lerp(1f, 1f + 4f.dp.toPx() / h, progress).takeIf { it.isFinite() } ?: 1f

                                val initialDerivative = 0.05f
                                val offset = interactiveHighlight.offset
                                val tx = minDim * tanh(initialDerivative * offset.x / minDim)
                                val ty = minDim * tanh(initialDerivative * offset.y / minDim)
                                translationX = tx.takeIf { it.isFinite() } ?: 0f
                                translationY = ty.takeIf { it.isFinite() } ?: 0f

                                val maxDragScale = (4f.dp.toPx() / h).takeIf { it.isFinite() } ?: 0f
                                val offsetAngle = atan2(offset.y, offset.x)
                                val sx = scale + maxDragScale * abs(cos(offsetAngle) * offset.x / maxDim)
                                val sy = scale + maxDragScale * abs(sin(offsetAngle) * offset.y / maxDim)
                                scaleX = sx.takeIf { it.isFinite() && it > 0.001f } ?: 1f
                                scaleY = sy.takeIf { it.isFinite() && it > 0.001f } ?: 1f
                            }
                        } catch (_: Throwable) {
                            translationX = 0f
                            translationY = 0f
                            scaleX = 1f
                            scaleY = 1f
                        }
                    }
                } else {
                    null
                },
                onDrawSurface = {
                    if (tint.isSpecified) {
                        drawRect(tint, blendMode = BlendMode.Hue)
                        drawRect(tint.copy(alpha = 0.78f))
                    }
                    if (effectiveSurfaceColor.isSpecified) {
                        drawRect(effectiveSurfaceColor)
                    }
                },
            )
            .then(
                if (isInteractive && enabled) {
                    Modifier
                        .then(interactiveHighlight.gestureModifier)
                        .then(interactiveHighlight.modifier)
                } else {
                    Modifier
                },
            )
            .clickable(
                enabled = enabled,
                interactionSource = null,
                indication = if (isInteractive) null else LocalIndication.current,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides effectiveContentColor) {
            content()
        }
    }
}

@Composable
fun LiquidButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backdrop: Backdrop? = null,
    enabled: Boolean = true,
    isInteractive: Boolean = true,
    tint: Color = Color.Unspecified,
    surfaceColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    shape: Shape = Capsule(),
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
    content: @Composable RowScope.() -> Unit,
) {
    val animationScope = rememberCoroutineScope()
    val isDark = isSystemInDarkTheme()
    val activeBackdrop = backdrop ?: LocalLayerBackdrop.current ?: rememberCanvasBackdrop {
        drawRect(if (isDark) Color(0xFF1C1C1E) else Color(0xFFF2F2F6))
    }

    val interactiveHighlight = remember(animationScope) {
        InteractiveHighlight(
            animationScope = animationScope,
        )
    }

    val effectiveContentColor = if (contentColor.isSpecified) {
        contentColor
    } else if (tint.isSpecified && tint != Color.Transparent) {
        Color.White
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val config = tachiyomi.presentation.core.components.material.LocalLiquidGlassConfig.current

    Row(
        modifier
            .defaultMinSize(minHeight = 44.dp)
            .alpha(if (enabled) 1f else 0.45f)
            .drawBackdrop(
                backdrop = activeBackdrop,
                shape = { shape },
                effects = {
                    vibrancy()
                    val effectiveBlur = (3f.dp.toPx() * config.blurMultiplier).coerceAtLeast(0f)
                    if (effectiveBlur > 0.5f) {
                        blur(effectiveBlur)
                    }
                    try {
                        lens(
                            refractionHeight = (14f.dp.toPx() * config.refractionMultiplier * config.lensHeightMultiplier).coerceAtLeast(0.1f),
                            refractionAmount = (24f.dp.toPx() * config.refractionMultiplier).coerceAtLeast(0.1f),
                            depthEffect = config.depthEffect,
                            chromaticAberration = config.chromaticAberration,
                        )
                    } catch (_: Throwable) {
                    }
                },
                layerBlock = if (isInteractive && enabled) {
                    {
                        try {
                            val width = size.width
                            val height = size.height
                            val minDim = size.minDimension
                            val maxDim = size.maxDimension
                            if (height <= 0.001f || width <= 0.001f || minDim <= 0.001f || maxDim <= 0.001f) {
                                translationX = 0f
                                translationY = 0f
                                scaleX = 1f
                                scaleY = 1f
                            } else {
                                val progress = interactiveHighlight.pressProgress
                                val scale = lerp(1f, 1f + 4f.dp.toPx() / height, progress).takeIf { it.isFinite() } ?: 1f

                                val initialDerivative = 0.05f
                                val offset = interactiveHighlight.offset
                                val tx = minDim * tanh(initialDerivative * offset.x / minDim)
                                val ty = minDim * tanh(initialDerivative * offset.y / minDim)
                                translationX = tx.takeIf { it.isFinite() } ?: 0f
                                translationY = ty.takeIf { it.isFinite() } ?: 0f

                                val maxDragScale = (4f.dp.toPx() / height).takeIf { it.isFinite() } ?: 0f
                                val offsetAngle = atan2(offset.y, offset.x)
                                val aspectW = (width / height).fastCoerceAtMost(1f)
                                val aspectH = (height / width).fastCoerceAtMost(1f)
                                val sx = scale + maxDragScale * abs(cos(offsetAngle) * offset.x / maxDim) * aspectW
                                val sy = scale + maxDragScale * abs(sin(offsetAngle) * offset.y / maxDim) * aspectH
                                scaleX = sx.takeIf { it.isFinite() && it > 0.001f } ?: 1f
                                scaleY = sy.takeIf { it.isFinite() && it > 0.001f } ?: 1f
                            }
                        } catch (_: Throwable) {
                            translationX = 0f
                            translationY = 0f
                            scaleX = 1f
                            scaleY = 1f
                        }
                    }
                } else {
                    null
                },
                onDrawSurface = {
                    if (tint.isSpecified) {
                        drawRect(tint, blendMode = BlendMode.Hue)
                        drawRect(tint.copy(alpha = (0.78f * config.tintAlphaMultiplier).coerceIn(0f, 1f)))
                    }
                    if (surfaceColor.isSpecified) {
                        drawRect(surfaceColor)
                    }
                },
            )
            .then(
                if (isInteractive && enabled) {
                    Modifier
                        .then(interactiveHighlight.gestureModifier)
                        .then(interactiveHighlight.modifier)
                } else {
                    Modifier
                },
            )
            .clickable(
                enabled = enabled,
                interactionSource = null,
                indication = if (isInteractive) null else LocalIndication.current,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(8f.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(LocalContentColor provides effectiveContentColor) {
            content()
        }
    }
}
