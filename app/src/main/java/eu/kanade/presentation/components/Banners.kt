package eu.kanade.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.kyant.shapes.Capsule
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastMaxBy
import dev.icerock.moko.resources.StringResource
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.i18n.stringResource
import java.math.RoundingMode
import java.text.NumberFormat

val DownloadedOnlyBannerBackgroundColor
    @Composable get() = MaterialTheme.colorScheme.tertiary
val IncognitoModeBannerBackgroundColor
    @Composable get() = MaterialTheme.colorScheme.primary
val IndexingBannerBackgroundColor
    @Composable get() = MaterialTheme.colorScheme.secondary

// KMK -->
val RestoringBannerBackgroundColor
    @Composable get() = MaterialTheme.colorScheme.error
val SyncingBannerBackgroundColor
    @Composable get() = MaterialTheme.colorScheme.secondary
val UpdatingBannerBackgroundColor
    @Composable get() = MaterialTheme.colorScheme.tertiary
// KMK <--

@Composable
fun WarningBanner(
    textRes: StringResource,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(textRes),
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.error)
            .padding(16.dp),
        color = MaterialTheme.colorScheme.onError,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
    )
}

// KMK -->
private val percentFormatter = NumberFormat.getPercentInstance().apply {
    roundingMode = RoundingMode.DOWN
    maximumFractionDigits = 0
}
// KMK <--

@Composable
fun AppStateBanners(
    downloadedOnlyMode: Boolean,
    incognitoMode: Boolean,
    indexing: Boolean,
    // KMK -->
    restoring: Boolean,
    syncing: Boolean,
    updating: Boolean,
    // KMK <--
    modifier: Modifier = Modifier,
    // KMK -->
    progress: Float? = null,
    // KMK <--
) {
    val density = LocalDensity.current
    val mainInsets = WindowInsets.statusBars
    val mainInsetsTop = mainInsets.getTop(density)
    SubcomposeLayout(modifier = modifier) { constraints ->
        val indexingPlaceable = subcompose(0) {
            AnimatedVisibility(
                // KMK -->
                visible = indexing || restoring || syncing || updating,
                // KMK <--
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                IndexingDownloadBanner(
                    modifier = Modifier.windowInsetsPadding(mainInsets),
                    // KMK -->
                    text = when {
                        updating -> progress?.let {
                            stringResource(
                                MR.strings.notification_updating_progress,
                                percentFormatter.format(it),
                            )
                        } ?: stringResource(MR.strings.updating_library)
                        syncing -> progress?.let {
                            stringResource(MR.strings.syncing_library) + " (${percentFormatter.format(it)})"
                        } ?: stringResource(MR.strings.syncing_library)
                        restoring -> progress?.let {
                            stringResource(MR.strings.restoring_backup) + " (${percentFormatter.format(it)})"
                        } ?: stringResource(MR.strings.restoring_backup)
                        else -> stringResource(MR.strings.download_notifier_cache_renewal)
                    },
                    // KMK <--
                )
            }
        }.fastMap { it.measure(constraints) }
        val indexingHeight = indexingPlaceable.fastMaxBy { it.height }?.height ?: 0

        val downloadedOnlyPlaceable = subcompose(1) {
            AnimatedVisibility(
                visible = downloadedOnlyMode,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                val top = (mainInsetsTop - indexingHeight).coerceAtLeast(0)
                DownloadedOnlyModeBanner(
                    modifier = Modifier.windowInsetsPadding(WindowInsets(top = top)),
                )
            }
        }.fastMap { it.measure(constraints) }
        val downloadedOnlyHeight = downloadedOnlyPlaceable.fastMaxBy { it.height }?.height ?: 0

        val incognitoPlaceable = subcompose(2) {
            AnimatedVisibility(
                visible = incognitoMode,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                val top = (mainInsetsTop - indexingHeight - downloadedOnlyHeight).coerceAtLeast(0)
                IncognitoModeBanner(
                    modifier = Modifier.windowInsetsPadding(WindowInsets(top = top)),
                )
            }
        }.fastMap { it.measure(constraints) }
        val incognitoHeight = incognitoPlaceable.fastMaxBy { it.height }?.height ?: 0

        layout(constraints.maxWidth, indexingHeight + downloadedOnlyHeight + incognitoHeight) {
            indexingPlaceable.fastForEach {
                it.place(0, 0)
            }
            downloadedOnlyPlaceable.fastForEach {
                it.place(0, indexingHeight)
            }
            incognitoPlaceable.fastForEach {
                it.place(0, indexingHeight + downloadedOnlyHeight)
            }
        }
    }
}

@Composable
private fun DownloadedOnlyModeBanner(modifier: Modifier = Modifier) {
    val isDark = isSystemInDarkTheme()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 4.dp)
            .then(modifier),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .shadow(
                    elevation = 4.dp,
                    shape = Capsule(),
                    ambientColor = Color.Black.copy(0.2f),
                    spotColor = Color.Black.copy(0.25f),
                )
                .clip(Capsule())
                .background(
                    if (isDark) Color(0xFF1C1C1E).copy(alpha = 0.88f)
                    else Color(0xFFFFFFFF).copy(alpha = 0.92f),
                )
                .border(
                    BorderStroke(
                        0.5.dp,
                        if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.08f),
                    ),
                    shape = Capsule(),
                )
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.CloudDownload,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(15.dp),
            )
            Text(
                text = stringResource(MR.strings.label_downloaded_only),
                color = if (isDark) Color.White else Color(0xFF1C1C1E),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.1).sp,
                ),
            )
        }
    }
}

@Composable
private fun IncognitoModeBanner(modifier: Modifier = Modifier) {
    val isDark = isSystemInDarkTheme()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 4.dp)
            .then(modifier),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .shadow(
                    elevation = 4.dp,
                    shape = Capsule(),
                    ambientColor = Color.Black.copy(0.2f),
                    spotColor = Color.Black.copy(0.25f),
                )
                .clip(Capsule())
                .background(
                    if (isDark) Color(0xFF1C1C1E).copy(alpha = 0.88f)
                    else Color(0xFFFFFFFF).copy(alpha = 0.92f),
                )
                .border(
                    BorderStroke(
                        0.5.dp,
                        if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.08f),
                    ),
                    shape = Capsule(),
                )
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.VisibilityOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(15.dp),
            )
            Text(
                text = stringResource(MR.strings.pref_incognito_mode),
                color = if (isDark) Color.White else Color(0xFF1C1C1E),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.1).sp,
                ),
            )
        }
    }
}

@Composable
private fun IndexingDownloadBanner(
    modifier: Modifier = Modifier,
    // KMK -->
    text: String = stringResource(MR.strings.download_notifier_cache_renewal),
    // KMK <--
) {
    val isDark = isSystemInDarkTheme()
    val density = LocalDensity.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 4.dp)
            .then(modifier),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .shadow(
                    elevation = 4.dp,
                    shape = Capsule(),
                    ambientColor = Color.Black.copy(0.2f),
                    spotColor = Color.Black.copy(0.25f),
                )
                .clip(Capsule())
                .background(
                    if (isDark) Color(0xFF1C1C1E).copy(alpha = 0.88f)
                    else Color(0xFFFFFFFF).copy(alpha = 0.92f),
                )
                .border(
                    BorderStroke(
                        0.5.dp,
                        if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.08f),
                    ),
                    shape = Capsule(),
                )
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            var textHeight by remember { mutableStateOf(0.dp) }
            CircularProgressIndicator(
                modifier = Modifier.requiredSize(14.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 2.dp,
            )
            Text(
                text = text,
                color = if (isDark) Color.White else Color(0xFF1C1C1E),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-0.1).sp,
                ),
                onTextLayout = {
                    with(density) {
                        textHeight = it.size.height.toDp()
                    }
                },
            )
        }
    }
}
