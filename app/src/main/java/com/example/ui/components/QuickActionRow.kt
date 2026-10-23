package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContactWithCategory
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryBlueContainer
import com.example.ui.theme.TextSecondary

@Composable
fun QuickActionRow(
    contactWithCategory: ContactWithCategory,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val contact = contactWithCategory.contact

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val hasPhone = contact.phoneNumber.isNotBlank()
        val hasEmail = contact.email.isNotBlank()

        ActionButton(
            icon = Icons.Default.Phone,
            label = "Call",
            enabled = hasPhone,
            tag = "action_call",
            onClick = {
                if (hasPhone) {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:${contact.phoneNumber.trim()}")
                    }
                    launchIntentSafely(context, intent, "Unable to make a call")
                }
            }
        )

        ActionButton(
            icon = Icons.Default.Message,
            label = "Message",
            enabled = hasPhone,
            tag = "action_message",
            onClick = {
                if (hasPhone) {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("smsto:${contact.phoneNumber.trim()}")
                    }
                    launchIntentSafely(context, intent, "Unable to send message")
                }
            }
        )

        ActionButton(
            icon = Icons.Default.Email,
            label = "Email",
            enabled = hasEmail,
            tag = "action_email",
            onClick = {
                if (hasEmail) {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:${contact.email.trim()}")
                    }
                    launchIntentSafely(context, intent, "Unable to send email")
                }
            }
        )

        ActionButton(
            icon = Icons.Default.Share,
            label = "Share",
            enabled = true,
            tag = "action_share",
            onClick = {
                val shareBody = buildString {
                    append("Contact: ${contactWithCategory.displayName}\n")
                    if (hasPhone) append("Phone: ${contact.phoneNumber}\n")
                    if (hasEmail) append("Email: ${contact.email}\n")
                    if (contact.notes.isNotBlank()) append("Notes: ${contact.notes}\n")
                }
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, contactWithCategory.displayName)
                    putExtra(Intent.EXTRA_TEXT, shareBody)
                }
                val chooser = Intent.createChooser(intent, "Share contact via")
                context.startActivity(chooser)
            }
        )
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    tag: String,
    onClick: () -> Unit
) {
    val bgColor = if (enabled) PrimaryBlueContainer else Color(0xFFF1F5F9)
    val iconColor = if (enabled) PrimaryBlue else Color(0xFF94A3B8)
    val textColor = if (enabled) TextSecondary else Color(0xFFCBD5E1)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .testTag(tag)
            .clip(CircleShape)
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

private fun launchIntentSafely(context: Context, intent: Intent, errorToast: String) {
    try {
        context.startActivity(intent)
    } catch (_: Exception) {
        Toast.makeText(context, errorToast, Toast.LENGTH_SHORT).show()
    }
}
