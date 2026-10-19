package eu.kanade.presentation.reader.settings

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import eu.kanade.domain.manga.model.readerOrientation
import eu.kanade.domain.manga.model.readingMode
import eu.kanade.tachiyomi.ui.reader.setting.ReaderOrientation
import eu.kanade.tachiyomi.ui.reader.setting.ReaderPreferences
import eu.kanade.tachiyomi.ui.reader.setting.ReaderSettingsScreenModel
import eu.kanade.tachiyomi.ui.reader.setting.ReadingMode
import eu.kanade.tachiyomi.ui.reader.viewer.webtoon.WebtoonViewer
import tachiyomi.i18n.MR
import tachiyomi.i18n.kmk.KMR
import tachiyomi.i18n.sy.SYMR
import tachiyomi.presentation.core.components.CheckboxItem
import tachiyomi.presentation.core.components.SliderItem
import tachiyomi.presentation.core.components.material.CupertinoInsetGroup
import tachiyomi.presentation.core.components.material.CupertinoPickerRow
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.collectAsState
import java.text.NumberFormat

@Composable
internal fun ReadingModePage(screenModel: ReaderSettingsScreenModel) {
    val manga by screenModel.mangaFlow.collectAsState()
    val readingMode = remember(manga) { ReadingMode.fromPreference(manga?.readingMode?.toInt()) }
    val orientation = remember(manga) { ReaderOrientation.fromPreference(manga?.readerOrientation?.toInt()) }
    val isDark = isSystemInDarkTheme()

    CupertinoInsetGroup(title = stringResource(MR.strings.pref_category_for_this_series)) {
        CupertinoPickerRow(
            title = stringResource(MR.strings.pref_category_reading_mode),
            selectedValueTitle = stringResource(readingMode.stringRes),
            options = ReadingMode.entries,
            selectedOption = readingMode,
            onOptionSelected = { screenModel.onChangeReadingMode(it) },
            optionLabelProvider = { stringResource(it.stringRes) },
        )
        HorizontalDivider(thickness = 0.5.dp, color = (if (isDark) Color.White else Color.Black).copy(alpha = 0.10f))
        CupertinoPickerRow(
            title = stringResource(MR.strings.rotation_type),
            selectedValueTitle = stringResource(orientation.stringRes),
            options = ReaderOrientation.entries,
            selectedOption = orientation,
            onOptionSelected = { screenModel.onChangeOrientation(it) },
            optionLabelProvider = { stringResource(it.stringRes) },
        )
    }

    val viewer by screenModel.viewerFlow.collectAsState()
    if (viewer is WebtoonViewer) {
        WebtoonViewerSettings(
            screenModel = screenModel,
            readingMode = readingMode,
        )
        WebtoonWithGapsViewerSettings(screenModel)
    } else {
        PagerViewerSettings(screenModel)
    }
}

@Composable
private fun PagerViewerSettings(screenModel: ReaderSettingsScreenModel) {
    val navigationModePager by screenModel.preferences.navigationModePager().collectAsState()
    val pagerNavInverted by screenModel.preferences.pagerNavInverted().collectAsState()
    val imageScaleType by screenModel.preferences.imageScaleType().collectAsState()
    val zoomStart by screenModel.preferences.zoomStart().collectAsState()
    val pageLayout by screenModel.preferences.pageLayout().collectAsState()
    val centerMarginType by screenModel.preferences.centerMarginType().collectAsState()
    val isDark = isSystemInDarkTheme()

    CupertinoInsetGroup(title = stringResource(MR.strings.pager_viewer)) {
        TapZonesItems(
            selected = navigationModePager,
            onSelect = screenModel.preferences.navigationModePager()::set,
            invertMode = pagerNavInverted,
            onSelectInvertMode = screenModel.preferences.pagerNavInverted()::set,
        )

        HorizontalDivider(thickness = 0.5.dp, color = (if (isDark) Color.White else Color.Black).copy(alpha = 0.10f))
        val scaleOptions = (1..ReaderPreferences.ImageScaleType.size).toList()
        CupertinoPickerRow(
            title = stringResource(MR.strings.pref_image_scale_type),
            selectedValueTitle = stringResource(ReaderPreferences.ImageScaleType[(imageScaleType - 1).coerceIn(0, ReaderPreferences.ImageScaleType.lastIndex)]),
            options = scaleOptions,
            selectedOption = imageScaleType,
            onOptionSelected = { screenModel.preferences.imageScaleType().set(it) },
            optionLabelProvider = { stringResource(ReaderPreferences.ImageScaleType[it - 1]) },
        )

        HorizontalDivider(thickness = 0.5.dp, color = (if (isDark) Color.White else Color.Black).copy(alpha = 0.10f))
        val zoomOptions = (1..ReaderPreferences.ZoomStart.size).toList()
        CupertinoPickerRow(
            title = stringResource(MR.strings.pref_zoom_start),
            selectedValueTitle = stringResource(ReaderPreferences.ZoomStart[(zoomStart - 1).coerceIn(0, ReaderPreferences.ZoomStart.lastIndex)]),
            options = zoomOptions,
            selectedOption = zoomStart,
            onOptionSelected = { screenModel.preferences.zoomStart().set(it) },
            optionLabelProvider = { stringResource(ReaderPreferences.ZoomStart[it - 1]) },
        )

        HorizontalDivider(thickness = 0.5.dp, color = (if (isDark) Color.White else Color.Black).copy(alpha = 0.10f))
        val layoutOptions = ReaderPreferences.PageLayouts.indices.toList()
        CupertinoPickerRow(
            title = stringResource(SYMR.strings.page_layout),
            selectedValueTitle = stringResource(ReaderPreferences.PageLayouts[pageLayout.coerceIn(0, ReaderPreferences.PageLayouts.lastIndex)]),
            options = layoutOptions,
            selectedOption = pageLayout,
            onOptionSelected = { screenModel.preferences.pageLayout().set(it) },
            optionLabelProvider = { stringResource(ReaderPreferences.PageLayouts[it]) },
        )

        HorizontalDivider(thickness = 0.5.dp, color = (if (isDark) Color.White else Color.Black).copy(alpha = 0.10f))
        val centerMarginOptions = ReaderPreferences.CenterMarginTypes.indices.toList()
        CupertinoPickerRow(
            title = stringResource(SYMR.strings.pref_center_margin),
            selectedValueTitle = stringResource(ReaderPreferences.CenterMarginTypes[centerMarginType.coerceIn(0, ReaderPreferences.CenterMarginTypes.lastIndex)]),
            options = centerMarginOptions,
            selectedOption = centerMarginType,
            onOptionSelected = { screenModel.preferences.centerMarginType().set(it) },
            optionLabelProvider = { stringResource(ReaderPreferences.CenterMarginTypes[it]) },
        )
    }

    CupertinoInsetGroup(title = stringResource(MR.strings.pref_category_reading_mode)) {
        CheckboxItem(
            label = stringResource(KMR.strings.pref_viewer_nav_smaller_tap_zone),
            pref = screenModel.preferences.smallerTapZone(),
        )
        CheckboxItem(
            label = stringResource(MR.strings.pref_crop_borders),
            pref = screenModel.preferences.cropBorders(),
        )
        if (imageScaleType in ReaderPreferences.zoomWideImagesAllowedList) {
            CheckboxItem(
                label = stringResource(MR.strings.pref_landscape_zoom),
                pref = screenModel.preferences.landscapeZoom(),
            )
        }
        CheckboxItem(
            label = stringResource(MR.strings.pref_navigate_pan),
            pref = screenModel.preferences.navigateToPan(),
        )
        val dualPageSplitPaged by screenModel.preferences.dualPageSplitPaged().collectAsState()
        CheckboxItem(
            label = stringResource(MR.strings.pref_dual_page_split),
            pref = screenModel.preferences.dualPageSplitPaged(),
        )
        if (dualPageSplitPaged) {
            CheckboxItem(
                label = stringResource(MR.strings.pref_dual_page_invert),
                pref = screenModel.preferences.dualPageInvertPaged(),
            )
        }
        val dualPageRotateToFit by screenModel.preferences.dualPageRotateToFit().collectAsState()
        CheckboxItem(
            label = stringResource(MR.strings.pref_page_rotate),
            pref = screenModel.preferences.dualPageRotateToFit(),
        )
        if (dualPageRotateToFit) {
            CheckboxItem(
                label = stringResource(MR.strings.pref_page_rotate_invert),
                pref = screenModel.preferences.dualPageRotateToFitInvert(),
            )
        }
        CheckboxItem(
            label = stringResource(MR.strings.pref_page_transitions),
            pref = screenModel.preferences.pageTransitionsPager(),
        )
        CheckboxItem(
            label = stringResource(SYMR.strings.invert_double_pages),
            pref = screenModel.preferences.invertDoublePages(),
        )
        CheckboxItem(
            label = stringResource(KMR.strings.pref_paged_disable_zoom_in),
            pref = screenModel.preferences.pagedDisableZoomIn(),
        )
        val pagedDisableZoomIn by screenModel.preferences.pagedDisableZoomIn().collectAsState()
        if (!pagedDisableZoomIn) {
            CheckboxItem(
                label = stringResource(MR.strings.pref_double_tap_zoom),
                pref = screenModel.preferences.pagedDoubleTapZoomEnabled(),
            )
        }
    }
}

@Composable
private fun WebtoonViewerSettings(
    screenModel: ReaderSettingsScreenModel,
    readingMode: ReadingMode,
) {
    val numberFormat = remember { NumberFormat.getPercentInstance() }
    val navigationModeWebtoon by screenModel.preferences.navigationModeWebtoon().collectAsState()
    val webtoonNavInverted by screenModel.preferences.webtoonNavInverted().collectAsState()
    val webtoonScaleTypePref = screenModel.preferences.webtoonScaleType()
    val webtoonScaleType by webtoonScaleTypePref.collectAsState()
    val webtoonSmartScaleLongStripGap = screenModel.preferences.longStripGapSmartScale().get()
    val isDark = isSystemInDarkTheme()

    CupertinoInsetGroup(title = stringResource(MR.strings.webtoon_viewer)) {
        TapZonesItems(
            selected = navigationModeWebtoon,
            onSelect = screenModel.preferences.navigationModeWebtoon()::set,
            invertMode = webtoonNavInverted,
            onSelectInvertMode = screenModel.preferences.webtoonNavInverted()::set,
        )

        if (readingMode != ReadingMode.CONTINUOUS_VERTICAL || webtoonSmartScaleLongStripGap) {
            HorizontalDivider(thickness = 0.5.dp, color = (if (isDark) Color.White else Color.Black).copy(alpha = 0.10f))
            CupertinoPickerRow(
                title = stringResource(KMR.strings.pref_webtoon_scale_type),
                selectedValueTitle = stringResource(webtoonScaleType.titleRes),
                options = ReaderPreferences.WebtoonScaleType.entries,
                selectedOption = webtoonScaleType,
                onOptionSelected = { webtoonScaleTypePref.set(it) },
                optionLabelProvider = { stringResource(it.titleRes) },
            )
        }
    }

    val webtoonSidePadding by screenModel.preferences.webtoonSidePadding().collectAsState()
    SliderItem(
        value = webtoonSidePadding,
        valueRange = ReaderPreferences.let { it.WEBTOON_PADDING_MIN..it.WEBTOON_PADDING_MAX },
        label = stringResource(MR.strings.pref_webtoon_side_padding),
        valueString = numberFormat.format(webtoonSidePadding / 100f),
        onChange = {
            screenModel.preferences.webtoonSidePadding().set(it)
        },
        pillColor = MaterialTheme.colorScheme.surfaceContainerHighest,
    )

    CupertinoInsetGroup(title = stringResource(MR.strings.pref_category_reading_mode)) {
        CheckboxItem(
            label = stringResource(KMR.strings.pref_viewer_nav_smaller_tap_zone),
            pref = screenModel.preferences.smallerTapZone(),
        )
        CheckboxItem(
            label = stringResource(MR.strings.pref_crop_borders),
            pref = screenModel.preferences.cropBordersWebtoon(),
        )
        CheckboxItem(
            label = stringResource(SYMR.strings.pref_smooth_scroll),
            pref = screenModel.preferences.smoothAutoScroll(),
        )
        CheckboxItem(
            label = stringResource(MR.strings.pref_page_transitions),
            pref = screenModel.preferences.pageTransitionsWebtoon(),
        )
        val dualPageSplitWebtoon by screenModel.preferences.dualPageSplitWebtoon().collectAsState()
        CheckboxItem(
            label = stringResource(MR.strings.pref_dual_page_split),
            pref = screenModel.preferences.dualPageSplitWebtoon(),
        )
        if (dualPageSplitWebtoon) {
            CheckboxItem(
                label = stringResource(MR.strings.pref_dual_page_invert),
                pref = screenModel.preferences.dualPageInvertWebtoon(),
            )
        }
        val dualPageRotateToFitWebtoon by screenModel.preferences.dualPageRotateToFitWebtoon().collectAsState()
        CheckboxItem(
            label = stringResource(MR.strings.pref_page_rotate),
            pref = screenModel.preferences.dualPageRotateToFitWebtoon(),
        )
        if (dualPageRotateToFitWebtoon) {
            CheckboxItem(
                label = stringResource(MR.strings.pref_page_rotate_invert),
                pref = screenModel.preferences.dualPageRotateToFitInvertWebtoon(),
            )
        }
        CheckboxItem(
            label = stringResource(MR.strings.pref_double_tap_zoom),
            pref = screenModel.preferences.webtoonDoubleTapZoomEnabled(),
        )
        CheckboxItem(
            label = stringResource(KMR.strings.pref_pinch_to_zoom),
            pref = screenModel.preferences.webtoonPinchToZoomEnabled(),
        )
        CheckboxItem(
            label = stringResource(MR.strings.pref_webtoon_disable_zoom_out),
            pref = screenModel.preferences.webtoonDisableZoomOut(),
        )
    }
}

@Composable
private fun WebtoonWithGapsViewerSettings(screenModel: ReaderSettingsScreenModel) {
    CupertinoInsetGroup(title = stringResource(MR.strings.vertical_plus_viewer)) {
        CheckboxItem(
            label = stringResource(MR.strings.pref_crop_borders),
            pref = screenModel.preferences.cropBordersContinuousVertical(),
        )
    }
}

@Composable
private fun TapZonesItems(
    selected: Int,
    onSelect: (Int) -> Unit,
    invertMode: ReaderPreferences.TappingInvertMode,
    onSelectInvertMode: (ReaderPreferences.TappingInvertMode) -> Unit,
) {
    val tapZoneOptions = ReaderPreferences.TapZones.indices.toList()
    val invertOptions = ReaderPreferences.TappingInvertMode.entries
    val isDark = isSystemInDarkTheme()

    CupertinoPickerRow(
        title = stringResource(MR.strings.pref_viewer_nav),
        selectedValueTitle = stringResource(ReaderPreferences.TapZones[selected.coerceIn(0, ReaderPreferences.TapZones.lastIndex)]),
        options = tapZoneOptions,
        selectedOption = selected,
        onOptionSelected = onSelect,
        optionLabelProvider = { stringResource(ReaderPreferences.TapZones[it]) },
    )

    if (selected != 5) {
        HorizontalDivider(thickness = 0.5.dp, color = (if (isDark) Color.White else Color.Black).copy(alpha = 0.10f))
        CupertinoPickerRow(
            title = stringResource(MR.strings.pref_read_with_tapping_inverted),
            selectedValueTitle = stringResource(invertMode.titleRes),
            options = invertOptions,
            selectedOption = invertMode,
            onOptionSelected = onSelectInvertMode,
            optionLabelProvider = { stringResource(it.titleRes) },
        )
    }
}
