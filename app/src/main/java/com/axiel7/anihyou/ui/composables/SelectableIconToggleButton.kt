package com.axiel7.anihyou.ui.composables

import androidx.annotation.DrawableRes
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.PlainTooltipBox
import androidx.compose.material3.Text
import androidx.compose.material3.rememberPlainTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SelectableIconToggleButton(
    @DrawableRes icon: Int,
    tooltipText: String,
    value: T,
    selectedValue: T,
    onClick: (Boolean) -> Unit
) {
    val tooltipState = rememberPlainTooltipState()
    val scope = rememberCoroutineScope()

    PlainTooltipBox(
        tooltip = { Text(tooltipText) },
        focusable = false,
        tooltipState = tooltipState,
    ) {
        FilledIconToggleButton(
            checked = value == selectedValue,
            onCheckedChange = {
                scope.launch { tooltipState.show() }
                onClick(it)
            },
            modifier = Modifier.tooltipTrigger()
        ) {
            Icon(painter = painterResource(icon), contentDescription = tooltipText)
        }
    }
}