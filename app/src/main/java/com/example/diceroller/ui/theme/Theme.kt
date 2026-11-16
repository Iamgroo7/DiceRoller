package com.example.diceroller.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppColors = lightColorScheme(
    primary = Color(0xFF00668B),
    onPrimary = Color.White
)

@Composable
fun DiceRollerTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = AppColors, content = content)
}
