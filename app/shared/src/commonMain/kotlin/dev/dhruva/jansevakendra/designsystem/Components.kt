package dev.dhruva.jansevakendra.designsystem

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.dhruva.jansevakendra.core.model.ApplicationStage

/**
 * High-performance, tactile Jan Seva Kendra Primary Action Button.
 */
@Composable
fun JskPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .shadow(if (enabled) 6.dp else 0.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (enabled) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )
            .bouncyClickable(enabled = enabled && !isLoading, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.5.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (leadingIcon != null) {
                    leadingIcon()
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = if (enabled) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                )
            }
        }
    }
}

/**
 * Secondary outlined/tonal button.
 */
@Composable
fun JskSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .bouncyClickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

/**
 * Elevation-aware civic Card component.
 */
@Composable
fun JskCard(
    modifier: Modifier = Modifier,
    elevation: Dp = 2.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val clickModifier = if (onClick != null) {
        Modifier.bouncyClickable(onClick = onClick)
    } else Modifier

    Column(
        modifier = modifier
            .shadow(elevation, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
            .then(clickModifier)
            .padding(16.dp),
        content = content
    )
}

/**
 * Floating styled Input Field with label, validation support, and leading/trailing elements.
 */
@Composable
fun JskTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                .border(
                    width = if (isError) 1.5.dp else 1.dp,
                    color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(Modifier.width(10.dp))
            }

            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty() && placeholder.isNotEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    )
                }

                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    singleLine = true
                )
            }

            if (trailingIcon != null) {
                Spacer(Modifier.width(8.dp))
                trailingIcon()
            }
        }

        AnimatedVisibility(visible = isError && errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.error),
                modifier = Modifier.padding(start = 6.dp, top = 4.dp)
            )
        }
    }
}

/**
 * Status Chip with adaptive colors for government application verification steps.
 */
@Composable
fun JskStatusChip(stage: ApplicationStage) {
    val (label, bg, fg) = when (stage) {
        ApplicationStage.SUBMITTED -> Triple("Submitted", Color(0xFFE0F2FE), Color(0xFF0369A1))
        ApplicationStage.DOCUMENTS_VERIFYING -> Triple("Verifying Docs", Color(0xFFFEF3C7), Color(0xFFB45309))
        ApplicationStage.FIELD_VERIFICATION -> Triple("Field Verification", Color(0xFFEDE9FE), Color(0xFF6D28D9))
        ApplicationStage.APPROVED -> Triple("Approved", Color(0xFFDCFCE7), Color(0xFF15803D))
        ApplicationStage.REJECTED -> Triple("Rejected", Color(0xFFFEE2E2), Color(0xFFB91C1C))
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(fg)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = fg
            )
        )
    }
}

/**
 * Custom Vector Canvas drawing for Jan Seva Kendra Emblem / Ashok Chakra emblem.
 */
@Composable
fun JskEmblem(
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    tint: Color = JskColors.SaffronPrimary
) {
    Canvas(modifier = modifier.size(size)) {
        val radius = this.size.minDimension / 2f
        val center = Offset(this.size.width / 2f, this.size.height / 2f)

        // Outer wheel circle
        drawCircle(
            color = tint,
            radius = radius * 0.92f,
            center = center,
            style = Stroke(width = radius * 0.08f)
        )

        // Inner core circle
        drawCircle(
            color = tint,
            radius = radius * 0.22f,
            center = center
        )

        // 24 Radial spokes (Chakra)
        val spokeCount = 24
        val innerR = radius * 0.22f
        val outerR = radius * 0.88f
        for (i in 0 until spokeCount) {
            val angle = (i * 360f / spokeCount) * (kotlin.math.PI / 180f).toFloat()
            val start = Offset(
                x = center.x + innerR * kotlin.math.cos(angle),
                y = center.y + innerR * kotlin.math.sin(angle)
            )
            val end = Offset(
                x = center.x + outerR * kotlin.math.cos(angle),
                y = center.y + outerR * kotlin.math.sin(angle)
            )
            drawLine(
                color = tint.copy(alpha = 0.85f),
                start = start,
                end = end,
                strokeWidth = radius * 0.035f
            )
        }
    }
}

/**
 * Shimmer skeleton placeholder item.
 */
@Composable
fun JskShimmerCard(
    modifier: Modifier = Modifier,
    height: Dp = 100.dp
) {
    val brush = rememberShimmerBrush()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(20.dp))
            .background(brush)
    )
}
