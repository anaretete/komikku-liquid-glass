package eu.kanade.presentation.reader.appbars

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import com.kyant.shapes.Capsule
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import tachiyomi.presentation.core.components.material.MoreHoriz
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import com.kyant.backdrop.catalog.components.LiquidIconButton
import tachiyomi.presentation.core.components.material.CupertinoFrostedPopup
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.components.AppBarActions
import kotlinx.collections.immutable.persistentListOf
import tachiyomi.i18n.MR
import tachiyomi.presentation.core.components.material.IconButton
import tachiyomi.presentation.core.components.material.LocalLiquidGlassConfig
import tachiyomi.presentation.core.components.material.liquidGlassSurface
import tachiyomi.presentation.core.i18n.stringResource

import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import tachiyomi.presentation.core.components.material.LocalLayerBackdrop
import com.kyant.backdrop.backdrops.rememberCanvasBackdrop

@Composable
fun ReaderTopBar(
    mangaTitle: String?,
    chapterTitle: String?,
    navigateUp: () -> Unit,
    bookmarked: Boolean,
    onToggleBookmarked: () -> Unit,
    onOpenInWebView: (() -> Unit)?,
    onOpenInBrowser: (() -> Unit)?,
    onShare: (() -> Unit)?,
    modifier: Modifier = Modifier,
    onClickTopAppBar: (() -> Unit)? = null,
) {
    val isGlass = LocalLiquidGlassConfig.current.enabled
    val isDark = isSystemInDarkTheme()

    if (isGlass) {
        var showOverflow by remember { mutableStateOf(false) }
        val hasOverflow = onOpenInWebView != null || onOpenInBrowser != null || onShare != null
        val effectiveBackdrop = LocalLayerBackdrop.current ?: rememberCanvasBackdrop {
            drawRect(if (isDark) Color(0xFF141416) else Color(0xFFF2F2F6))
        }

        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // Standalone 44dp Circular Liquid Glass Back Button
            LiquidIconButton(
                onClick = navigateUp,
                size = 44.dp,
                backdrop = effectiveBackdrop,
                surfaceColor = Color(0xFF141416).copy(alpha = 0.55f),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                    contentDescription = stringResource(MR.strings.action_bar_up_description),
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }

            // Compact Centered Floating Liquid Glass Capsule Pill for Title / Subtitle
            Box(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .padding(horizontal = 12.dp)
                    .drawBackdrop(
                        backdrop = effectiveBackdrop,
                        shape = { Capsule() },
                        effects = {
                            vibrancy()
                            blur(18.dp.toPx())
                            lens(16.dp.toPx(), 28.dp.toPx(), depthEffect = true, chromaticAberration = true)
                        },
                        onDrawSurface = { drawRect(Color(0xFF141416).copy(alpha = 0.55f)) },
                    )
                    .padding(horizontal = 16.dp, vertical = 7.dp)
                    .then(if (onClickTopAppBar != null) Modifier.clickable(onClick = onClickTopAppBar) else Modifier),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (!mangaTitle.isNullOrEmpty()) {
                        Text(
                            text = mangaTitle,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = Color.White,
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (!chapterTitle.isNullOrEmpty()) {
                        Text(
                            text = chapterTitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                color = Color(0xFFEBEBF5),
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            // Right Actions: Standalone 44dp Circular Liquid Glass Bookmark & Overflow
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LiquidIconButton(
                    onClick = onToggleBookmarked,
                    size = 44.dp,
                    backdrop = effectiveBackdrop,
                    surfaceColor = Color(0xFF141416).copy(alpha = 0.55f),
                ) {
                    Icon(
                        imageVector = if (bookmarked) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = stringResource(
                            if (bookmarked) MR.strings.action_remove_bookmark else MR.strings.action_bookmark,
                        ),
                        tint = if (bookmarked) Color(0xFFFF9F0A) else Color.White,
                        modifier = Modifier.size(22.dp),
                    )
                }

                if (hasOverflow) {
                    Box {
                        LiquidIconButton(
                            onClick = { showOverflow = true },
                            size = 44.dp,
                            backdrop = effectiveBackdrop,
                            surfaceColor = Color(0xFF141416).copy(alpha = 0.55f),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.MoreHoriz,
                                contentDescription = stringResource(MR.strings.label_more),
                                tint = Color.White,
                                modifier = Modifier.size(22.dp),
                            )
                        }

                        CupertinoFrostedPopup(
                            expanded = showOverflow,
                            onDismissRequest = { showOverflow = false },
                        ) {
                            onOpenInWebView?.let {
                                DropdownMenuItem(
                                    text = { Text(stringResource(MR.strings.action_open_in_web_view)) },
                                    onClick = {
                                        showOverflow = false
                                        it()
                                    },
                                )
                            }
                            onOpenInBrowser?.let {
                                DropdownMenuItem(
                                    text = { Text(stringResource(MR.strings.action_open_in_browser)) },
                                    onClick = {
                                        showOverflow = false
                                        it()
                                    },
                                )
                            }
                            onShare?.let {
                                DropdownMenuItem(
                                    text = { Text(stringResource(MR.strings.action_share)) },
                                    onClick = {
                                        showOverflow = false
                                        it()
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        AppBar(
            modifier = modifier,
            backgroundColor = Color.Transparent,
            title = mangaTitle,
            subtitle = chapterTitle,
            navigateUp = navigateUp,
            actions = {
                AppBarActions(
                    actions = persistentListOf<AppBar.AppBarAction>().builder()
                        .apply {
                            add(
                                AppBar.Action(
                                    title = stringResource(
                                        if (bookmarked) {
                                            MR.strings.action_remove_bookmark
                                        } else {
                                            MR.strings.action_bookmark
                                        },
                                    ),
                                    icon = if (bookmarked) {
                                        Icons.Outlined.Bookmark
                                    } else {
                                        Icons.Outlined.BookmarkBorder
                                    },
                                    onClick = onToggleBookmarked,
                                ),
                            )
                            onOpenInWebView?.let {
                                add(
                                    AppBar.OverflowAction(
                                        title = stringResource(MR.strings.action_open_in_web_view),
                                        onClick = it,
                                    ),
                                )
                            }
                            onOpenInBrowser?.let {
                                add(
                                    AppBar.OverflowAction(
                                        title = stringResource(MR.strings.action_open_in_browser),
                                        onClick = it,
                                    ),
                                )
                            }
                            onShare?.let {
                                add(
                                    AppBar.OverflowAction(
                                        title = stringResource(MR.strings.action_share),
                                        onClick = it,
                                    ),
                                )
                            }
                        }
                        .build(),
                )
            },
        )
    }
}
