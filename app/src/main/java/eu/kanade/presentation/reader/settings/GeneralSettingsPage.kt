package eu.kanade.presentation.reader.settings

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import eu.kanade.tachiyomi.ui.reader.setting.ReaderPreferences
import eu.kanade.tachiyomi.ui.reader.setting.ReaderSettingsScreenModel
import eu.kanade.tachiyomi.util.system.hasDisplayCutout
import tachiyomi.i18n.MR
import tachiyomi.i18n.sy.SYMR
import tachiyomi.presentation.core.components.CheckboxItem
import tachiyomi.presentation.core.components.SliderItem
import tachiyomi.presentation.core.components.material.CupertinoGroupedSegmentedControl
import tachiyomi.presentation.core.components.material.CupertinoInsetGroup
import tachiyomi.presentation.core.components.material.CupertinoPickerRow
import tachiyomi.presentation.core.i18n.pluralStringResource
import tachiyomi.presentation.core.i18n.stringResource
import tachiyomi.presentation.core.util.collectAsState

private val themes = listOf(
    MR.strings.black_background to 1,
    MR.strings.gray_background to 2,
    MR.strings.white_background to 0,
    MR.strings.automatic_background to 3,
)

private val flashColors = listOf(
    MR.strings.pref_flash_style_black to ReaderPreferences.FlashColor.BLACK,
    MR.strings.pref_flash_style_white to ReaderPreferences.FlashColor.WHITE,
    MR.strings.pref_flash_style_white_black to ReaderPreferences.FlashColor.WHITE_BLACK,
)

@Composable
internal fun GeneralPage(screenModel: ReaderSettingsScreenModel) {
    val readerTheme by screenModel.preferences.readerTheme().collectAsState()
    val flashPageState by screenModel.preferences.flashOnPageChange().collectAsState()
    val flashMillisPref = screenModel.preferences.flashDurationMillis()
    val flashMillis by flashMillisPref.collectAsState()
    val flashIntervalPref = screenModel.preferences.flashPageInterval()
    val flashInterval by flashIntervalPref.collectAsState()
    val flashColorPref = screenModel.preferences.flashColor()
    val flashColor by flashColorPref.collectAsState()
    val isDark = isSystemInDarkTheme()

    val currentThemeEntry = themes.firstOrNull { it.second == readerTheme } ?: themes[3]
    CupertinoInsetGroup(title = stringResource(MR.strings.pref_reader_theme)) {
        CupertinoPickerRow(
            title = stringResource(MR.strings.pref_reader_theme),
            selectedValueTitle = stringResource(currentThemeEntry.first),
            options = themes,
            selectedOption = currentThemeEntry,
            onOptionSelected = { screenModel.preferences.readerTheme().set(it.second) },
            optionLabelProvider = { stringResource(it.first) },
        )
    }

    CupertinoInsetGroup(title = stringResource(MR.strings.pref_category_general)) {
        CheckboxItem(
            label = stringResource(MR.strings.pref_show_page_number),
            pref = screenModel.preferences.showPageNumber(),
        )

        CheckboxItem(
            label = stringResource(MR.strings.pref_fullscreen),
            pref = screenModel.preferences.fullscreen(),
        )

        val isFullscreen by screenModel.preferences.fullscreen().collectAsState()
        if (LocalActivity.current?.hasDisplayCutout() == true && isFullscreen) {
            CheckboxItem(
                label = stringResource(MR.strings.pref_cutout_short),
                pref = screenModel.preferences.drawUnderCutout(),
            )
        }

        CheckboxItem(
            label = stringResource(MR.strings.pref_keep_screen_on),
            pref = screenModel.preferences.keepScreenOn(),
        )

        CheckboxItem(
            label = stringResource(MR.strings.pref_read_with_long_tap),
            pref = screenModel.preferences.readWithLongTap(),
        )

        CheckboxItem(
            label = stringResource(MR.strings.pref_always_show_chapter_transition),
            pref = screenModel.preferences.alwaysShowChapterTransition(),
        )

        // SY -->
        CheckboxItem(
            label = stringResource(SYMR.strings.auto_webtoon_mode),
            pref = screenModel.preferences.useAutoWebtoon(),
        )
        // SY <--
    }

    // SY -->
    val forceHorizontalSeekbar by screenModel.preferences.forceHorizontalSeekbar().collectAsState()
    CupertinoInsetGroup(title = stringResource(SYMR.strings.pref_force_horz_seekbar)) {
        CheckboxItem(
            label = stringResource(SYMR.strings.pref_force_horz_seekbar),
            pref = screenModel.preferences.forceHorizontalSeekbar(),
        )

        if (!forceHorizontalSeekbar) {
            CheckboxItem(
                label = stringResource(SYMR.strings.pref_show_vert_seekbar_landscape),
                pref = screenModel.preferences.landscapeVerticalSeekbar(),
            )

            CheckboxItem(
                label = stringResource(SYMR.strings.pref_left_handed_vertical_seekbar),
                pref = screenModel.preferences.leftVerticalSeekbar(),
            )
        }
    }
    // SY <--

    CupertinoInsetGroup(title = stringResource(MR.strings.pref_flash_page)) {
        CheckboxItem(
            label = stringResource(MR.strings.pref_flash_page),
            pref = screenModel.preferences.flashOnPageChange(),
        )

        if (flashPageState) {
            SliderItem(
                value = flashMillis / ReaderPreferences.MILLI_CONVERSION,
                valueRange = 1..15,
                label = stringResource(MR.strings.pref_flash_duration),
                valueString = stringResource(MR.strings.pref_flash_duration_summary, flashMillis),
                onChange = { flashMillisPref.set(it * ReaderPreferences.MILLI_CONVERSION) },
                pillColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            )
            SliderItem(
                value = flashInterval,
                valueRange = 1..10,
                label = stringResource(MR.strings.pref_flash_page_interval),
                valueString = pluralStringResource(MR.plurals.pref_pages, flashInterval, flashInterval),
                onChange = {
                    flashIntervalPref.set(it)
                },
                pillColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            )

            val currentFlashEntry = flashColors.firstOrNull { it.second == flashColor } ?: flashColors[0]
            HorizontalDivider(thickness = 0.5.dp, color = (if (isDark) Color.White else Color.Black).copy(alpha = 0.10f))
            CupertinoPickerRow(
                title = stringResource(MR.strings.pref_flash_with),
                selectedValueTitle = stringResource(currentFlashEntry.first),
                options = flashColors,
                selectedOption = currentFlashEntry,
                onOptionSelected = { flashColorPref.set(it.second) },
                optionLabelProvider = { stringResource(it.first) },
            )
        }
    }
}
