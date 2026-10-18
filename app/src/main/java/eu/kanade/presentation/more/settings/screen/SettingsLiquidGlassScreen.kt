package eu.kanade.presentation.more.settings.screen

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberCanvasBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.catalog.components.LiquidButton
import com.kyant.backdrop.catalog.components.LiquidSlider
import com.kyant.backdrop.catalog.components.LiquidToggle
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import eu.kanade.domain.ui.UiPreferences
import eu.kanade.presentation.more.settings.Preference
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.border
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.material.LocalLayerBackdrop
import tachiyomi.presentation.core.components.material.LocalLiquidGlassConfig
import tachiyomi.presentation.core.util.collectAsState
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

object SettingsLiquidGlassScreen : SearchableSettings {
    @Suppress("unused")
    private fun readResolve(): Any = SettingsLiquidGlassScreen

    @ReadOnlyComposable
    @Composable
    override fun getTitleRes() = MR.strings.pref_category_appearance

    @Composable
    override fun getPreferences(): List<Preference> {
        val uiPreferences = remember { Injekt.get<UiPreferences>() }
        val scope = rememberCoroutineScope()
        return getLiquidGlassPreferences(uiPreferences, scope)
    }

    @Composable
    fun getLiquidGlassPreferences(
        uiPreferences: UiPreferences,
        scope: CoroutineScope = rememberCoroutineScope(),
    ): List<Preference> {
        val isGlassEnabled by uiPreferences.liquidGlassEnabled().collectAsState()
        val refractionStrength by uiPreferences.liquidGlassRefractionStrength().collectAsState()
        val blurRadius by uiPreferences.liquidGlassBlurRadius().collectAsState()
        val lensHeight by uiPreferences.liquidGlassLensHeight().collectAsState()
        val tintAlpha by uiPreferences.liquidGlassTintAlpha().collectAsState()

        return listOf(
            Preference.PreferenceGroup(
                title = "LIQUID GLASS ENGINE",
                preferenceItems = persistentListOf(
                    Preference.PreferenceItem.SwitchPreference(
                        preference = uiPreferences.liquidGlassEnabled(),
                        title = "Enable Liquid Glass",
                        subtitle = "Enable real-time AGSL refraction, caustic dispersion, and spring physics across the entire interface",
                    ),
                ),
            ),
            Preference.PreferenceGroup(
                title = "OPTICAL LIQUID GLASS DEMO",
                preferenceItems = persistentListOf(
                    Preference.PreferenceItem.CustomPreference(
                        title = "Wavy Digital Art Video & Floating Bubble",
                    ) {
                        WavyDigitalArtDemoCard()
                    },
                ),
            ),
            Preference.PreferenceGroup(
                title = "COMPONENT OPTICS",
                preferenceItems = persistentListOf(
                    Preference.PreferenceItem.CustomPreference(
                        title = "Live Component Controls",
                    ) {
                        LiveGlassPreviewCard()
                    },
                ),
            ),
            Preference.PreferenceGroup(
                title = "OPTICAL REFRACTION & BLUR",
                preferenceItems = persistentListOf(
                    Preference.PreferenceItem.SliderPreference(
                        value = (refractionStrength * 100).roundToInt(),
                        valueRange = 25..200,
                        title = "Refraction Strength",
                        subtitle = "Signed Distance Field lens refraction depth and caustic distortion",
                        valueString = "${(refractionStrength * 100).roundToInt()}%",
                        enabled = isGlassEnabled,
                        onValueChanged = {
                            uiPreferences.liquidGlassRefractionStrength().set(it / 100f)
                        },
                    ),
                    Preference.PreferenceItem.SliderPreference(
                        value = (blurRadius * 100).roundToInt(),
                        valueRange = 25..200,
                        title = "Optical Blur Radius",
                        subtitle = "Gaussian vibrancy blur behind translucent glass lenses",
                        valueString = "${(blurRadius * 100).roundToInt()}%",
                        enabled = isGlassEnabled,
                        onValueChanged = {
                            uiPreferences.liquidGlassBlurRadius().set(it / 100f)
                        },
                    ),
                    Preference.PreferenceItem.SliderPreference(
                        value = (lensHeight * 100).roundToInt(),
                        valueRange = 25..200,
                        title = "Lens Dome Curvature",
                        subtitle = "Optical 3D surface convex curvature profile",
                        valueString = "${(lensHeight * 100).roundToInt()}%",
                        enabled = isGlassEnabled,
                        onValueChanged = {
                            uiPreferences.liquidGlassLensHeight().set(it / 100f)
                        },
                    ),
                    Preference.PreferenceItem.SliderPreference(
                        value = (tintAlpha * 100).roundToInt(),
                        valueRange = 20..200,
                        title = "Tint Opacity",
                        subtitle = "Translucent background tint saturation level",
                        valueString = "${(tintAlpha * 100).roundToInt()}%",
                        enabled = isGlassEnabled,
                        onValueChanged = {
                            uiPreferences.liquidGlassTintAlpha().set(it / 100f)
                        },
                    ),
                ),
            ),
            Preference.PreferenceGroup(
                title = "PHYSICS & EFFECTS",
                preferenceItems = persistentListOf(
                    Preference.PreferenceItem.SwitchPreference(
                        preference = uiPreferences.liquidGlassDepthEffect(),
                        title = "3D Depth Lens Effect",
                        subtitle = "Enable 3D depth distortion and edge illumination",
                        enabled = isGlassEnabled,
                    ),
                    Preference.PreferenceItem.SwitchPreference(
                        preference = uiPreferences.liquidGlassChromaticAberration(),
                        title = "Chromatic Aberration",
                        subtitle = "Prismatic color dispersion along curved glass borders",
                        enabled = isGlassEnabled,
                    ),
                    Preference.PreferenceItem.SwitchPreference(
                        preference = uiPreferences.liquidGlassSpringPhysics(),
                        title = "Spring Physics Animation",
                        subtitle = "Viscoelastic rebound on press, drag, and release",
                        enabled = isGlassEnabled,
                    ),
                ),
            ),
            Preference.PreferenceGroup(
                title = "RESET",
                preferenceItems = persistentListOf(
                    Preference.PreferenceItem.TextPreference(
                        title = "Reset to Apple Defaults",
                        subtitle = "Restore all liquid glass optical and physics parameters to default values",
                        onClick = {
                            scope.launch {
                                uiPreferences.liquidGlassEnabled().set(true)
                                uiPreferences.liquidGlassChromaticAberration().set(true)
                                uiPreferences.liquidGlassRefractionStrength().set(1.0f)
                                uiPreferences.liquidGlassBlurRadius().set(1.0f)
                                uiPreferences.liquidGlassLensHeight().set(1.0f)
                                uiPreferences.liquidGlassTintAlpha().set(1.0f)
                                uiPreferences.liquidGlassSpringPhysics().set(true)
                                uiPreferences.liquidGlassDepthEffect().set(true)
                            }
                        },
                    ),
                ),
            ),
        )
    }

    /**
     * Pinterest Reference Demo (https://pin.it/2gdxhG43k):
     * Generative looping abstract wavy digital art video canvas with an interactive
     * draggable Liquid Glass bubble floating and optically refracting the flowing wave ribbons.
     */
    @Composable
    private fun WavyDigitalArtDemoCard() {
        val demoBackdrop = rememberLayerBackdrop()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black),
            ) {
                // 1. Background layer: looping abstract wavy line art video
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .layerBackdrop(demoBackdrop),
                ) {
                    WavyDigitalArtVideo(modifier = Modifier.fillMaxSize())
                }

                // 2. Foreground layer: interactive draggable liquid glass bubble
                LiquidGlassBubble(
                    backdrop = demoBackdrop,
                    modifier = Modifier.fillMaxSize(),
                )

                // Subtitle badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.65f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = "Drag liquid glass bubble over looping video",
                        color = Color.White.copy(alpha = 0.90f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }

    @Composable
    private fun WavyDigitalArtVideo(modifier: Modifier = Modifier) {
        val context = LocalContext.current
        AsyncImage(
            model = remember(context) {
                ImageRequest.Builder(context)
                    .data(eu.kanade.tachiyomi.R.raw.wavy_art_loop)
                    .build()
            },
            contentDescription = "Looping abstract geometric wavy line art",
            contentScale = ContentScale.Crop,
            modifier = modifier,
        )
    }

    @Composable
    private fun LiquidGlassBubble(
        backdrop: Backdrop,
        modifier: Modifier = Modifier,
    ) {
        val config = LocalLiquidGlassConfig.current
        var offset by remember { mutableStateOf(Offset.Zero) }
        var isDragging by remember { mutableStateOf(false) }
        val bubbleSize = 82.dp

        BoxWithConstraints(modifier = modifier) {
            val maxW = constraints.maxWidth.toFloat()
            val maxH = constraints.maxHeight.toFloat()
            val density = LocalDensity.current
            val bubbleSizePx = with(density) { bubbleSize.toPx() }

            val centerOffset = remember(maxW, maxH) {
                Offset((maxW - bubbleSizePx) / 2f, (maxH - bubbleSizePx) / 2f)
            }

            val currentPos = centerOffset + offset
            val clampedX = currentPos.x.coerceIn(0f, (maxW - bubbleSizePx).coerceAtLeast(0f))
            val clampedY = currentPos.y.coerceIn(0f, (maxH - bubbleSizePx).coerceAtLeast(0f))

            val dragScale by animateFloatAsState(
                targetValue = if (isDragging && config.springPhysicsEnabled) 1.18f else 1f,
                animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMediumLow),
                label = "bubbleScale",
            )

            val bubbleModifier = if (config.enabled) {
                Modifier
                    .offset { IntOffset(clampedX.roundToInt(), clampedY.roundToInt()) }
                    .size(bubbleSize)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { isDragging = true },
                            onDragEnd = { isDragging = false },
                            onDragCancel = { isDragging = false },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                offset += dragAmount
                            },
                        )
                    }
                    .graphicsLayer {
                        scaleX = dragScale
                        scaleY = dragScale
                    }
                    .drawBackdrop(
                        backdrop = backdrop,
                        shape = { CircleShape },
                        effects = {
                            vibrancy()
                            val blurVal = (2.dp.toPx() * config.blurMultiplier).coerceAtLeast(0f)
                            if (blurVal > 0.5f) {
                                blur(blurVal)
                            }
                            try {
                                lens(
                                    refractionHeight = (28.dp.toPx() * config.refractionMultiplier * config.lensHeightMultiplier).coerceAtLeast(0.1f),
                                    refractionAmount = (42.dp.toPx() * config.refractionMultiplier).coerceAtLeast(0.1f),
                                    depthEffect = config.depthEffect,
                                    chromaticAberration = config.chromaticAberration,
                                )
                            } catch (_: Throwable) {
                            }
                        },
                        highlight = { Highlight.Default },
                        shadow = {
                            Shadow(
                                radius = 12.dp,
                                color = Color.Black.copy(alpha = 0.5f),
                            )
                        },
                        innerShadow = {
                            InnerShadow(
                                radius = 6.dp,
                                color = Color.White.copy(alpha = (0.55f * config.tintAlphaMultiplier).coerceIn(0f, 1f)),
                            )
                        },
                        onDrawSurface = {
                            drawRect(Color.White.copy(alpha = (0.08f * config.tintAlphaMultiplier).coerceIn(0f, 1f)))
                        },
                    )
            } else {
                Modifier
                    .offset { IntOffset(clampedX.roundToInt(), clampedY.roundToInt()) }
                    .size(bubbleSize)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { isDragging = true },
                            onDragEnd = { isDragging = false },
                            onDragCancel = { isDragging = false },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                offset += dragAmount
                            },
                        )
                    }
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f))
                    .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
            }

            Box(
                modifier = bubbleModifier,
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "DRAG",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                )
            }
        }
    }

    @Composable
    private fun LiveGlassPreviewCard() {
        val isDark = isSystemInDarkTheme()
        val config = LocalLiquidGlassConfig.current
        var previewToggle by remember { mutableStateOf(true) }
        var previewSliderValue by remember { mutableFloatStateOf(0.65f) }
        val cardBg = if (isDark) Color(0xFF1C1C1E) else Color(0xFFF2F2F7)
        val backdrop = LocalLayerBackdrop.current ?: rememberCanvasBackdrop {
            drawRect(cardBg)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(cardBg)
                .padding(16.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = "Interactive Optics",
                            color = if (isDark) Color.White else Color.Black,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = if (config.enabled) "Live refraction & spring preview" else "Standard preview (Glass disabled)",
                            color = if (isDark) Color(0xFF8E8E93) else Color(0xFF6C6C70),
                            fontSize = 12.sp,
                        )
                    }
                    LiquidToggle(
                        selected = { previewToggle },
                        onSelect = { previewToggle = it },
                        backdrop = backdrop,
                        accentColor = MaterialTheme.colorScheme.primary,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LiquidSlider(
                        value = { previewSliderValue },
                        onValueChange = { previewSliderValue = it },
                        backdrop = backdrop,
                        accentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "${(previewSliderValue * 100).roundToInt()}%",
                        color = if (isDark) Color.White else Color.Black,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LiquidButton(
                        onClick = {},
                        backdrop = backdrop,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = "Liquid Button",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}
