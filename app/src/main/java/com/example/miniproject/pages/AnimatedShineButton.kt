package com.example.miniproject.pages

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.scale

@Composable
fun AnimatedShineButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scope = rememberCoroutineScope()

    // 1. Bouncy Scale Animation
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )

    // 2. Shine Animation
    var buttonSize by remember { mutableStateOf(IntSize.Zero) }
    val shineProgress = remember { Animatable(0f) }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            shineProgress.snapTo(0f)
            scope.launch {
                shineProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(700, easing = LinearEasing)
                )
            }
        }
    }

    // Gradient matching your Login Page
    val gradient = Brush.horizontalGradient(
        listOf(Color(0xFFB4EFE3), Color(0xFF1ABC9C))
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .onSizeChanged { buttonSize = it }
            .clip(RoundedCornerShape(25.dp))
            .background(gradient, RoundedCornerShape(25.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Disable ripple
                onClick = onClick
            )
            .drawWithContent {
                // Draw the original content (Icon and Text)
                drawContent()

                // Draw the shine effect
                val progress = shineProgress.value
                if (progress > 0f && progress < 1f) {
                    val shineWidth = buttonSize.width * 0.5f
                    val totalWidth = buttonSize.width.toFloat() + shineWidth
                    val x = (totalWidth * progress) - (shineWidth / 2)

                    val brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.5f),
                            Color.Transparent
                        ),
                        start = Offset(x - (shineWidth / 2), 0f),
                        end = Offset(x + (shineWidth / 2), 0f)
                    )

                    // Apply the brush over the content
                    drawRect(
                        brush = brush,
                        blendMode = BlendMode.Plus, // Additive blend for a bright shine
                        size = size
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            content()
        }
    }
}