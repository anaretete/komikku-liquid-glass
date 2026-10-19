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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CollectionsBookmark
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.kyant.backdrop.catalog.components.LiquidBottomTab
import com.kyant.backdrop.catalog.components.LiquidBottomTabs
import com.kyant.backdrop.catalog.components.LiquidButton
import com.kyant.backdrop.catalog.components.LiquidIconButton
import com.kyant.backdrop.catalog.components.LiquidSlider
import com.kyant.backdrop.catalog.components.LiquidToggle
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.shadow.Shadow
import eu.kanade.domain.ui.UiPreferences
import eu.kanade.presentation.more.settings.Preference
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
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
                title = "OPTICAL LIQUID GLASS DEMO",
                preferenceItems = persistentListOf(
                    Preference.PreferenceItem.CustomPreference(
                        title = "Wavy Digital Art Video & Floating Refractive Blob",
                    ) {
                        WavyDigitalArtDemoCard()
                    },
                ),
            ),
            Preference.PreferenceGroup(
                title = "COMPONENT OPTICS PLAYGROUND",
                preferenceItems = persistentListOf(
                    Preference.PreferenceItem.CustomPreference(
                        title = "Live Component Bench",
                    ) {
                        ComponentPlaygroundCard()
                    },
                ),
            ),
            Preference.PreferenceGroup(
                title = "FREE-FLOATING DROPLETS SANDBOX",
                preferenceItems = persistentListOf(
                    Preference.PreferenceItem.CustomPreference(
                        title = "Interactive Glass Droplets",
                    ) {
                        GlassDropletsSandboxCard()
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
     * Generative looping abstract wavy digital art video canvas with an authentic
     * floating refractive glass lens blob powered by real-time GPU SDF shaders.
     * ZERO fake highlights or static border strokes.
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
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF06060D)),
            ) {
                // 1. Background layer: looping generative fluid wave motion
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .layerBackdrop(demoBackdrop),
                ) {
                    LiveWavyArtCanvas(modifier = Modifier.fillMaxSize())
                }

                // 2. Foreground layer: authentic refractive glass blob & satellite droplets
                AuthenticRefractiveGlassBlob(
                    backdrop = demoBackdrop,
                    modifier = Modifier.fillMaxSize(),
                )

                // Subtitle badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.55f))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                ) {
                    Text(
                        text = "Real-time GPU SDF Glass Lens • Zero Fake Highlights",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }

    @Composable
    private fun LiveWavyArtCanvas(modifier: Modifier = Modifier) {
        val infiniteTransition = rememberInfiniteTransition(label = "wavy_art_transition")
        val phase1 by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = (2f * PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 4200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "phase1",
        )
        val phase2 by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = (-2f * PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 6200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "phase2",
        )
        val phase3 by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = (2f * PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 8500, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "phase3",
        )

        Canvas(modifier = modifier) {
            val width = size.width
            val height = size.height

            val ribbons = listOf(
                Triple(
                    listOf(Color(0xFF00F5FF), Color(0xFF007AFF), Color(0xFF5856D6)),
                    36.dp.toPx(),
                    0.24f,
                ),
                Triple(
                    listOf(Color(0xFFFF007F), Color(0xFFAF52DE), Color(0xFF5856D6)),
                    44.dp.toPx(),
                    0.42f,
                ),
                Triple(
                    listOf(Color(0xFFFF375F), Color(0xFFFF9500), Color(0xFFFFCC00)),
                    40.dp.toPx(),
                    0.60f,
                ),
                Triple(
                    listOf(Color(0xFF30D158), Color(0xFF64D2FF), Color(0xFF0A84FF)),
                    34.dp.toPx(),
                    0.78f,
                ),
            )

            ribbons.forEachIndexed { index, (colors, amplitude, baseFraction) ->
                val path = Path()
                val step = 6f
                var x = 0f
                val baseY = height * baseFraction
                val phaseOffset = when (index % 3) {
                    0 -> phase1
                    1 -> phase2
                    else -> phase3
                } + (index * 0.95f)

                path.moveTo(0f, baseY)
                while (x <= width + step) {
                    val normalizedX = x / width
                    val angle1 = normalizedX * 2f * PI.toFloat() * 1.8f + phaseOffset
                    val angle2 = normalizedX * 2f * PI.toFloat() * 3.2f - phaseOffset * 0.7f
                    val y = baseY + (sin(angle1) * amplitude * 0.7f) + (cos(angle2) * amplitude * 0.35f)
                    path.lineTo(x, y)
                    x += step
                }

                drawPath(
                    path = path,
                    brush = Brush.horizontalGradient(
                        colors = colors,
                        startX = 0f,
                        endX = width,
                    ),
                    style = Stroke(width = (5f + index * 1.5f).dp.toPx()),
                )
            }
        }
    }

    /**
     * Pure refractive glass lens blob over animated media canvas.
     * Uses real-time GPU SDF shaders via Modifier.drawBackdrop with lens(), vibrancy(), blur().
     * ZERO fake highlights or static white/grey border strokes.
     */
    @Composable
    private fun AuthenticRefractiveGlassBlob(
        backdrop: Backdrop,
        modifier: Modifier = Modifier,
    ) {
        val config = LocalLiquidGlassConfig.current
        var mainOffset by remember { mutableStateOf(Offset.Zero) }
        var isDraggingMain by remember { mutableStateOf(false) }

        var satelliteOffset by remember { mutableStateOf(Offset.Zero) }
        var isDraggingSatellite by remember { mutableStateOf(false) }

        val mainSize = 84.dp
        val satSize = 46.dp

        BoxWithConstraints(modifier = modifier) {
            val maxW = constraints.maxWidth.toFloat()
            val maxH = constraints.maxHeight.toFloat()
            val density = LocalDensity.current

            val mainSizePx = with(density) { mainSize.toPx() }
            val satSizePx = with(density) { satSize.toPx() }

            // Main Blob positioning
            val centerMain = remember(maxW, maxH) {
                Offset((maxW - mainSizePx) / 2f, (maxH - mainSizePx) / 2f)
            }
            val posMain = centerMain + mainOffset
            val clampedMainX = posMain.x.coerceIn(0f, (maxW - mainSizePx).coerceAtLeast(0f))
            val clampedMainY = posMain.y.coerceIn(0f, (maxH - mainSizePx).coerceAtLeast(0f))

            val dragScaleMain by animateFloatAsState(
                targetValue = if (isDraggingMain) 1.15f else 1f,
                animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMediumLow),
                label = "blobScaleMain",
            )

            // Satellite Droplet positioning
            val centerSat = remember(maxW, maxH) {
                Offset((maxW - satSizePx) * 0.22f, (maxH - satSizePx) * 0.3f)
            }
            val posSat = centerSat + satelliteOffset
            val clampedSatX = posSat.x.coerceIn(0f, (maxW - satSizePx).coerceAtLeast(0f))
            val clampedSatY = posSat.y.coerceIn(0f, (maxH - satSizePx).coerceAtLeast(0f))

            val dragScaleSat by animateFloatAsState(
                targetValue = if (isDraggingSatellite) 1.2f else 1f,
                animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMediumLow),
                label = "blobScaleSat",
            )

            // 1. Satellite Droplet (Refractive Glass Lens)
            Box(
                modifier = Modifier
                    .offset { IntOffset(clampedSatX.roundToInt(), clampedSatY.roundToInt()) }
                    .size(satSize)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { isDraggingSatellite = true },
                            onDragEnd = { isDraggingSatellite = false },
                            onDragCancel = { isDraggingSatellite = false },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                satelliteOffset += dragAmount
                            },
                        )
                    }
                    .graphicsLayer {
                        scaleX = dragScaleSat
                        scaleY = dragScaleSat
                    }
                    .drawBackdrop(
                        backdrop = backdrop,
                        shape = { CircleShape },
                        effects = {
                            vibrancy()
                            val blurPx = (2f.dp.toPx() * config.blurMultiplier).coerceAtLeast(0f)
                            if (blurPx > 0.5f) {
                                blur(blurPx)
                            }
                            lens(
                                refractionHeight = (18f.dp.toPx() * config.lensHeightMultiplier).coerceAtLeast(0.1f),
                                refractionAmount = (28f.dp.toPx() * config.refractionMultiplier).coerceAtLeast(0.1f),
                                depthEffect = config.depthEffect,
                                chromaticAberration = config.chromaticAberration,
                            )
                        },
                        shadow = {
                            Shadow(
                                radius = 10.dp,
                                color = Color.Black.copy(alpha = 0.4f),
                            )
                        },
                    ),
            )

            // 2. Hero Refractive Glass Blob (Zero fake highlights, 100% authentic GPU SDF lens)
            Box(
                modifier = Modifier
                    .offset { IntOffset(clampedMainX.roundToInt(), clampedMainY.roundToInt()) }
                    .size(mainSize)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { isDraggingMain = true },
                            onDragEnd = { isDraggingMain = false },
                            onDragCancel = { isDraggingMain = false },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                mainOffset += dragAmount
                            },
                        )
                    }
                    .graphicsLayer {
                        scaleX = dragScaleMain
                        scaleY = dragScaleMain
                    }
                    .drawBackdrop(
                        backdrop = backdrop,
                        shape = { CircleShape },
                        effects = {
                            vibrancy()
                            val effectiveBlur = (2.5f.dp.toPx() * config.blurMultiplier).coerceAtLeast(0f)
                            if (effectiveBlur > 0.5f) {
                                blur(effectiveBlur)
                            }
                            lens(
                                refractionHeight = (30f.dp.toPx() * config.lensHeightMultiplier).coerceAtLeast(0.1f),
                                refractionAmount = (46f.dp.toPx() * config.refractionMultiplier).coerceAtLeast(0.1f),
                                depthEffect = config.depthEffect,
                                chromaticAberration = config.chromaticAberration,
                            )
                        },
                        shadow = {
                            Shadow(
                                radius = 16.dp,
                                color = Color.Black.copy(alpha = 0.5f),
                            )
                        },
                    ),
            )
        }
    }

    /**
     * Comprehensive Component Bench:
     * - Fully functional LiquidBottomTabs navigation dock with drag physics
     * - Multiple distinct LiquidButton variants & states (Primary, Crystal, Tonal, Destructive, Disabled, Icon)
     * - Live LiquidToggle and LiquidSlider
     */
    @Composable
    private fun ComponentPlaygroundCard() {
        val isDark = isSystemInDarkTheme()
        var selectedDemoTab by remember { mutableIntStateOf(0) }
        var previewToggle by remember { mutableStateOf(true) }
        var previewSliderValue by remember { mutableFloatStateOf(0.7f) }

        val cardBg = if (isDark) Color(0xFF1C1C1E) else Color(0xFFF2F2F7)
        val backdrop = LocalLayerBackdrop.current ?: rememberCanvasBackdrop {
            drawRect(cardBg)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(cardBg)
                .padding(16.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                // Section 1: Navigation Dock Preview
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "LiquidBottomTabs Navigation Dock",
                        color = if (isDark) Color.White else Color.Black,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Drag or tap across tabs with real-time spring physics and refractive pill indicator",
                        color = if (isDark) Color(0xFF8E8E93) else Color(0xFF6C6C70),
                        fontSize = 12.sp,
                    )

                    LiquidBottomTabs(
                        selectedTabIndex = { selectedDemoTab },
                        onTabSelected = { selectedDemoTab = it },
                        backdrop = backdrop,
                        tabsCount = 4,
                        accentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        val tabs = listOf(
                            "Library" to Icons.Outlined.CollectionsBookmark,
                            "Updates" to Icons.Outlined.NewReleases,
                            "History" to Icons.Outlined.History,
                            "Browse" to Icons.Outlined.Explore,
                        )
                        tabs.forEachIndexed { index, (title, icon) ->
                            val isSelected = selectedDemoTab == index
                            LiquidBottomTab(
                                onClick = { selectedDemoTab = index },
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = title,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else (if (isDark) Color.White.copy(0.6f) else Color.Black.copy(0.55f)),
                                    modifier = Modifier.size(20.dp),
                                )
                                Text(
                                    text = title,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else (if (isDark) Color.White.copy(0.6f) else Color.Black.copy(0.55f)),
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                )
                            }
                        }
                    }
                }

                // Section 2: LiquidButton Variants & States
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "LiquidButton Variants & States",
                        color = if (isDark) Color.White else Color.Black,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    )

                    // Row A: Primary Action & Clear Crystal Glass
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        LiquidButton(
                            onClick = {},
                            backdrop = backdrop,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = "Primary Action",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }

                        LiquidButton(
                            onClick = {},
                            backdrop = backdrop,
                            tint = Color.Transparent,
                            surfaceColor = if (isDark) Color(0xFF2C2C2E).copy(0.4f) else Color.White.copy(0.5f),
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = "Crystal Glass",
                                color = if (isDark) Color.White else Color.Black,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }

                    // Row B: Tonal Glass & Destructive Glass
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        LiquidButton(
                            onClick = {},
                            backdrop = backdrop,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = "Tonal Glass",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }

                        LiquidButton(
                            onClick = {},
                            backdrop = backdrop,
                            tint = Color(0xFFFF3B30),
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = "Destructive",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }

                    // Row C: Disabled Glass & Circular Icon Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        LiquidButton(
                            onClick = {},
                            backdrop = backdrop,
                            enabled = false,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = "Disabled State",
                                color = Color.White.copy(0.5f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }

                        LiquidIconButton(
                            onClick = {},
                            backdrop = backdrop,
                            size = 46.dp,
                            tint = MaterialTheme.colorScheme.primary,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Favorite,
                                contentDescription = "Favorite",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }

                // Section 3: Interactive Toggle & Slider
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Viscoelastic Toggle & Slider",
                        color = if (isDark) Color.White else Color.Black,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = "Liquid Toggle Switch",
                                color = if (isDark) Color.White else Color.Black,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                            )
                            Text(
                                text = "Authentic Apple iOS Liquid Glass Toggle",
                                color = if (isDark) Color(0xFF8E8E93) else Color(0xFF6C6C70),
                                fontSize = 11.sp,
                            )
                        }
                        LiquidToggle(
                            selected = { previewToggle },
                            onSelect = { previewToggle = it },
                            backdrop = backdrop,
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        LiquidSlider(
                            value = { previewSliderValue },
                            onValueChange = { previewSliderValue = it },
                            valueRange = 0f..1f,
                            visibilityThreshold = 0.01f,
                            backdrop = backdrop,
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "${(previewSliderValue * 100).roundToInt()}%",
                            color = if (isDark) Color.White else Color.Black,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }

    /**
     * Free-floating interactive glass bubbles & droplets sandbox.
     * Demonstrates multi-touch physics and real-time GPU SDF lens refraction
     * over a rich patterned canvas.
     */
    @Composable
    private fun GlassDropletsSandboxCard() {
        val isDark = isSystemInDarkTheme()
        val sandboxBackdrop = rememberLayerBackdrop()
        val config = LocalLiquidGlassConfig.current

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(if (isDark) Color(0xFF0F0F14) else Color(0xFFE5E5EA)),
            ) {
                // 1. Colorful patterned canvas background
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .layerBackdrop(sandboxBackdrop),
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        // Geometric chromatic gradient background
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFF5856D6), Color(0xFF007AFF), Color(0xFF0A1128)),
                                center = Offset(w * 0.3f, h * 0.4f),
                                radius = w * 0.8f,
                            ),
                        )

                        // Stylized diagonal tessellation lines
                        val lineSpacing = 28.dp.toPx()
                        var currX = -h
                        while (currX < w + h) {
                            drawLine(
                                color = Color.White.copy(alpha = 0.08f),
                                start = Offset(currX, 0f),
                                end = Offset(currX + h, h),
                                strokeWidth = 2.dp.toPx(),
                            )
                            currX += lineSpacing
                        }

                        // Luminous circular nodes
                        val nodes = listOf(
                            Triple(Offset(w * 0.25f, h * 0.35f), 40.dp.toPx(), Color(0xFFFF2D55)),
                            Triple(Offset(w * 0.72f, h * 0.28f), 55.dp.toPx(), Color(0xFF34C759)),
                            Triple(Offset(w * 0.55f, h * 0.75f), 48.dp.toPx(), Color(0xFFFF9500)),
                            Triple(Offset(w * 0.85f, h * 0.72f), 35.dp.toPx(), Color(0xFF00F5FF)),
                        )
                        nodes.forEach { (center, radius, color) ->
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(color.copy(0.7f), color.copy(0f)),
                                    center = center,
                                    radius = radius,
                                ),
                                center = center,
                                radius = radius,
                            )
                        }
                    }
                }

                // 2. Three free-floating interactive glass droplets
                FreeFloatingDroplets(
                    backdrop = sandboxBackdrop,
                    modifier = Modifier.fillMaxSize(),
                )

                // Subtitle badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.55f))
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                ) {
                    Text(
                        text = "Drag free-floating glass droplets across pattern",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
    }

    @Composable
    private fun FreeFloatingDroplets(
        backdrop: Backdrop,
        modifier: Modifier = Modifier,
    ) {
        val config = LocalLiquidGlassConfig.current

        var offset1 by remember { mutableStateOf(Offset.Zero) }
        var isDragging1 by remember { mutableStateOf(false) }

        var offset2 by remember { mutableStateOf(Offset.Zero) }
        var isDragging2 by remember { mutableStateOf(false) }

        var offset3 by remember { mutableStateOf(Offset.Zero) }
        var isDragging3 by remember { mutableStateOf(false) }

        val size1 = 80.dp
        val size2 = 54.dp
        val size3 = 38.dp

        BoxWithConstraints(modifier = modifier) {
            val maxW = constraints.maxWidth.toFloat()
            val maxH = constraints.maxHeight.toFloat()
            val density = LocalDensity.current

            val s1Px = with(density) { size1.toPx() }
            val s2Px = with(density) { size2.toPx() }
            val s3Px = with(density) { size3.toPx() }

            // Droplet 1: Hero Lens
            val center1 = remember(maxW, maxH) { Offset((maxW - s1Px) * 0.45f, (maxH - s1Px) * 0.4f) }
            val pos1 = center1 + offset1
            val cl1X = pos1.x.coerceIn(0f, (maxW - s1Px).coerceAtLeast(0f))
            val cl1Y = pos1.y.coerceIn(0f, (maxH - s1Px).coerceAtLeast(0f))
            val scale1 by animateFloatAsState(
                targetValue = if (isDragging1) 1.14f else 1f,
                animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMediumLow),
                label = "scale1",
            )

            // Droplet 2: Medium Dewdrop
            val center2 = remember(maxW, maxH) { Offset((maxW - s2Px) * 0.15f, (maxH - s2Px) * 0.2f) }
            val pos2 = center2 + offset2
            val cl2X = pos2.x.coerceIn(0f, (maxW - s2Px).coerceAtLeast(0f))
            val cl2Y = pos2.y.coerceIn(0f, (maxH - s2Px).coerceAtLeast(0f))
            val scale2 by animateFloatAsState(
                targetValue = if (isDragging2) 1.18f else 1f,
                animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMediumLow),
                label = "scale2",
            )

            // Droplet 3: Small Dewdrop
            val center3 = remember(maxW, maxH) { Offset((maxW - s3Px) * 0.82f, (maxH - s3Px) * 0.65f) }
            val pos3 = center3 + offset3
            val cl3X = pos3.x.coerceIn(0f, (maxW - s3Px).coerceAtLeast(0f))
            val cl3Y = pos3.y.coerceIn(0f, (maxH - s3Px).coerceAtLeast(0f))
            val scale3 by animateFloatAsState(
                targetValue = if (isDragging3) 1.22f else 1f,
                animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMediumLow),
                label = "scale3",
            )

            // Render Droplet 1 (Large Hero Lens)
            Box(
                modifier = Modifier
                    .offset { IntOffset(cl1X.roundToInt(), cl1Y.roundToInt()) }
                    .size(size1)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { isDragging1 = true },
                            onDragEnd = { isDragging1 = false },
                            onDragCancel = { isDragging1 = false },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                offset1 += dragAmount
                            },
                        )
                    }
                    .graphicsLayer {
                        scaleX = scale1
                        scaleY = scale1
                    }
                    .drawBackdrop(
                        backdrop = backdrop,
                        shape = { CircleShape },
                        effects = {
                            vibrancy()
                            val blurPx = (2f.dp.toPx() * config.blurMultiplier).coerceAtLeast(0f)
                            if (blurPx > 0.5f) {
                                blur(blurPx)
                            }
                            lens(
                                refractionHeight = (28f.dp.toPx() * config.lensHeightMultiplier).coerceAtLeast(0.1f),
                                refractionAmount = (40f.dp.toPx() * config.refractionMultiplier).coerceAtLeast(0.1f),
                                depthEffect = config.depthEffect,
                                chromaticAberration = config.chromaticAberration,
                            )
                        },
                        shadow = {
                            Shadow(
                                radius = 14.dp,
                                color = Color.Black.copy(alpha = 0.45f),
                            )
                        },
                    ),
            )

            // Render Droplet 2 (Medium Dewdrop)
            Box(
                modifier = Modifier
                    .offset { IntOffset(cl2X.roundToInt(), cl2Y.roundToInt()) }
                    .size(size2)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { isDragging2 = true },
                            onDragEnd = { isDragging2 = false },
                            onDragCancel = { isDragging2 = false },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                offset2 += dragAmount
                            },
                        )
                    }
                    .graphicsLayer {
                        scaleX = scale2
                        scaleY = scale2
                    }
                    .drawBackdrop(
                        backdrop = backdrop,
                        shape = { CircleShape },
                        effects = {
                            vibrancy()
                            val blurPx = (2f.dp.toPx() * config.blurMultiplier).coerceAtLeast(0f)
                            if (blurPx > 0.5f) {
                                blur(blurPx)
                            }
                            lens(
                                refractionHeight = (20f.dp.toPx() * config.lensHeightMultiplier).coerceAtLeast(0.1f),
                                refractionAmount = (30f.dp.toPx() * config.refractionMultiplier).coerceAtLeast(0.1f),
                                depthEffect = config.depthEffect,
                                chromaticAberration = config.chromaticAberration,
                            )
                        },
                        shadow = {
                            Shadow(
                                radius = 10.dp,
                                color = Color.Black.copy(alpha = 0.4f),
                            )
                        },
                    ),
            )

            // Render Droplet 3 (Small Dewdrop)
            Box(
                modifier = Modifier
                    .offset { IntOffset(cl3X.roundToInt(), cl3Y.roundToInt()) }
                    .size(size3)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { isDragging3 = true },
                            onDragEnd = { isDragging3 = false },
                            onDragCancel = { isDragging3 = false },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                offset3 += dragAmount
                            },
                        )
                    }
                    .graphicsLayer {
                        scaleX = scale3
                        scaleY = scale3
                    }
                    .drawBackdrop(
                        backdrop = backdrop,
                        shape = { CircleShape },
                        effects = {
                            vibrancy()
                            val blurPx = (1.5f.dp.toPx() * config.blurMultiplier).coerceAtLeast(0f)
                            if (blurPx > 0.5f) {
                                blur(blurPx)
                            }
                            lens(
                                refractionHeight = (15f.dp.toPx() * config.lensHeightMultiplier).coerceAtLeast(0.1f),
                                refractionAmount = (22f.dp.toPx() * config.refractionMultiplier).coerceAtLeast(0.1f),
                                depthEffect = config.depthEffect,
                                chromaticAberration = config.chromaticAberration,
                            )
                        },
                        shadow = {
                            Shadow(
                                radius = 8.dp,
                                color = Color.Black.copy(alpha = 0.35f),
                            )
                        },
                    ),
            )
        }
    }
}
