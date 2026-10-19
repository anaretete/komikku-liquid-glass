package com.kyant.backdrop.catalog.utils

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

@Composable
fun BackHandler(
    enabled: Boolean = true,
    onBack: () -> Unit,
) {
    androidx.activity.compose.BackHandler(enabled, onBack)
}
