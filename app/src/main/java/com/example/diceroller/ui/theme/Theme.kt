package com.example.diceroller.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

@Composable
fun DiceRollerTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = lightColorScheme(), content = content)
}
