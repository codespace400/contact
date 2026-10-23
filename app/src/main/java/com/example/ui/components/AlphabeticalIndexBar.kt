package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextMuted

val ALPHABET_INDEX = ('A'..'Z').toList() + listOf('#')

@Composable
fun AlphabeticalIndexBar(
    availableLetters: Set<Char>,
    selectedLetter: Char?,
    onLetterSelected: (Char) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(22.dp)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        ALPHABET_INDEX.forEach { char ->
            val isAvailable = availableLetters.contains(char)
            val isSelected = selectedLetter == char
            val textColor = when {
                isSelected -> PrimaryBlue
                isAvailable -> Color(0xFF334155)
                else -> TextMuted.copy(alpha = 0.5f)
            }

            Text(
                text = char.toString(),
                fontSize = 10.sp,
                fontWeight = if (isSelected || isAvailable) FontWeight.Bold else FontWeight.Normal,
                color = textColor,
                modifier = Modifier
                    .testTag("alpha_index_$char")
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        onLetterSelected(char)
                    }
                    .padding(vertical = 1.dp)
            )
        }
    }
}
