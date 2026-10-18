package tachiyomi.presentation.core.components.material

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.coerceAtLeast

/**
 * @param refreshing Whether the layout is currently refreshing
 * @param onRefresh Lambda which is invoked when a swipe to refresh gesture is completed.
 * @param enabled Whether the the layout should react to swipe gestures or not.
 * @param indicatorPadding Content padding for the indicator, to inset the indicator in if required.
 * @param content The content containing a vertically scrollable composable.
 */
@Composable
fun PullRefresh(
    refreshing: Boolean,
    enabled: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    indicatorPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable () -> Unit,
) {
    val state = rememberPullToRefreshState()
    val isGlass = LocalLiquidGlassConfig.current.enabled

    Box(
        modifier = modifier
            .pullToRefresh(
                isRefreshing = refreshing,
                state = state,
                enabled = enabled,
                onRefresh = onRefresh,
            ),
    ) {
        content()

        val progress = state.distanceFraction.coerceIn(0f, 1f)
        if (refreshing || progress > 0f) {
            val topOffset = if (refreshing) 24.dp else (progress * 24f).coerceAtLeast(8f).dp
            val isDark = isSystemInDarkTheme()
            val baseModifier = Modifier
                .align(Alignment.TopCenter)
                .padding(indicatorPadding)
                .padding(top = topOffset)
                .size(38.dp)

            val indicatorModifier = if (isGlass) {
                baseModifier.liquidGlassSurface(
                    shape = CircleShape,
                    blurRadius = 14.dp,
                    refractionHeight = 16.dp,
                    refractionAmount = 24.dp,
                )
            } else {
                baseModifier
                    .shadow(elevation = 2.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(if (isDark) Color(0xFF2C2C2E) else Color.White)
            }

            Box(
                modifier = indicatorModifier,
                contentAlignment = Alignment.Center,
            ) {
                CupertinoActivityIndicator(
                    isSpinning = refreshing,
                    progress = progress,
                    radius = 11.dp,
                )
            }
        }
    }
}
