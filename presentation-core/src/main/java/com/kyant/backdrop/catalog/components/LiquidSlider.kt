package com.kyant.backdrop.catalog.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.fastRoundToInt
import androidx.compose.ui.util.lerp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberBackdrop
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.catalog.utils.DampedDragAnimation
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import com.kyant.shapes.Capsule
import kotlinx.coroutines.flow.collectLatest
import tachiyomi.presentation.core.components.material.LocalLiquidGlassConfig

@Composable
fun LiquidSlider(
    value: () -> Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    visibilityThreshold: Float,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
) {
    val config = LocalLiquidGlassConfig.current
    val isLightTheme = !isSystemInDarkTheme()
    val accentColor =
        if (isLightTheme) {
            Color(0xFF0088FF)
        } else {
            Color(0xFF0091FF)
        }
    val trackColor =
        if (isLightTheme) {
            Color(0xFF787878).copy(0.2f)
        } else {
            Color(0xFF787880).copy(0.36f)
        }

    val trackBackdrop = rememberLayerBackdrop()

    BoxWithConstraints(
        modifier
            .defaultMinSize(minHeight = 44.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.CenterStart,
    ) {
        val trackWidth = constraints.maxWidth

        val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
        val animationScope = rememberCoroutineScope()
        var isDragging by remember { mutableStateOf(false) }
        var currentDragValue by remember { mutableFloatStateOf(value()) }

        val dampedDragAnimation = remember(animationScope) {
            DampedDragAnimation(
                animationScope = animationScope,
                initialValue = value(),
                valueRange = valueRange,
                visibilityThreshold = visibilityThreshold,
                initialScale = 1f,
                pressedScale = 1.05f,
                onDragStarted = {},
                onDragStopped = {},
                onDrag = { _, _ -> },
            )
        }

        LaunchedEffect(dampedDragAnimation) {
            snapshotFlow { value() }
                .collectLatest { valValue ->
                    if (!isDragging && dampedDragAnimation.targetValue != valValue) {
                        currentDragValue = valValue
                        dampedDragAnimation.updateValue(valValue)
                    }
                }
        }

        Box(
            Modifier
                .layerBackdrop(trackBackdrop)
                .fillMaxWidth()
                .pointerInput(trackWidth, isLtr, valueRange) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            dampedDragAnimation.press()
                            val delta = (valueRange.endInclusive - valueRange.start) * (offset.x / trackWidth)
                            val targetValue =
                                (
                                    if (isLtr) {
                                        valueRange.start + delta
                                    } else {
                                        valueRange.endInclusive - delta
                                    }
                                ).coerceIn(valueRange)
                            currentDragValue = targetValue
                            dampedDragAnimation.updateValue(targetValue)
                            onValueChange(targetValue)
                        },
                        onDragEnd = {
                            dampedDragAnimation.release()
                            onValueChange(currentDragValue)
                            isDragging = false
                        },
                        onDragCancel = {
                            dampedDragAnimation.release()
                            isDragging = false
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            val delta = (valueRange.endInclusive - valueRange.start) * (dragAmount / trackWidth)
                            currentDragValue =
                                (
                                    if (isLtr) {
                                        currentDragValue + delta
                                    } else {
                                        currentDragValue - delta
                                    }
                                ).coerceIn(valueRange)
                            dampedDragAnimation.updateValue(currentDragValue)
                            onValueChange(currentDragValue)
                        },
                    )
                }
                .pointerInput(trackWidth, isLtr, valueRange) {
                    detectTapGestures { position ->
                        val delta = (valueRange.endInclusive - valueRange.start) * (position.x / trackWidth)
                        val targetValue =
                            (
                                if (isLtr) {
                                    valueRange.start + delta
                                } else {
                                    valueRange.endInclusive - delta
                                }
                            ).coerceIn(valueRange)
                        currentDragValue = targetValue
                        dampedDragAnimation.animateToValue(targetValue)
                        onValueChange(targetValue)
                    }
                },
        ) {
            Box(
                Modifier
                    .clip(Capsule())
                    .background(trackColor)
                    .height(6f.dp)
                    .fillMaxWidth(),
            )

            Box(
                Modifier
                    .clip(Capsule())
                    .background(accentColor)
                    .height(6f.dp)
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints)
                        val width = (constraints.maxWidth * dampedDragAnimation.progress).fastRoundToInt()
                        layout(width, placeable.height) {
                            placeable.place(0, 0)
                        }
                    },
            )
        }

        Box(
            Modifier
                .graphicsLayer {
                    translationX =
                        (-size.width / 2f + trackWidth * dampedDragAnimation.progress)
                            .fastCoerceIn(-size.width / 4f, trackWidth - size.width * 3f / 4f) * if (isLtr) 1f else -1f
                }
                .drawBackdrop(
                    backdrop = rememberCombinedBackdrop(
                        backdrop,
                        rememberBackdrop(trackBackdrop) { drawBackdrop ->
                            val progress = dampedDragAnimation.pressProgress
                            val scaleX = lerp(2f / 3f, 1f, progress)
                            val scaleY = lerp(0f, 1f, progress)
                            scale(scaleX, scaleY) {
                                drawBackdrop()
                            }
                        },
                    ),
                    shape = { Capsule() },
                    effects = {
                        val progress = dampedDragAnimation.pressProgress
                        val blurPx = (8f.dp.toPx() * (1f - progress) * config.blurMultiplier).coerceAtLeast(0f)
                        if (blurPx > 0.5f) {
                            blur(blurPx)
                        }
                        if (config.isLiquid) {
                            lens(
                                (10f.dp.toPx() * progress * config.lensHeightMultiplier).coerceAtLeast(0.1f),
                                (14f.dp.toPx() * progress * config.refractionMultiplier).coerceAtLeast(0.1f),
                                depthEffect = config.depthEffect,
                                chromaticAberration = config.chromaticAberration,
                            )
                        }
                    },
                    highlight = if (config.isLiquid) {
                        {
                            val progress = dampedDragAnimation.pressProgress
                            Highlight.Ambient.copy(
                                width = Highlight.Ambient.width / 1.5f,
                                blurRadius = Highlight.Ambient.blurRadius / 1.5f,
                                alpha = progress,
                            )
                        }
                    } else {
                        null
                    },
                    shadow = {
                        Shadow(
                            radius = 4f.dp,
                            color = Color.Black.copy(alpha = 0.05f),
                        )
                    },
                    innerShadow = {
                        val progress = dampedDragAnimation.pressProgress
                        InnerShadow(
                            radius = 4f.dp * progress,
                            alpha = progress,
                        )
                    },
                    layerBlock = {
                        scaleX = dampedDragAnimation.scaleX
                        scaleY = dampedDragAnimation.scaleY
                    },
                    onDrawSurface = {
                        val progress = dampedDragAnimation.pressProgress
                        drawRect(Color.White.copy(alpha = 1f - progress))
                    },
                )
                .size(40f.dp, 24f.dp),
        )
    }
}
