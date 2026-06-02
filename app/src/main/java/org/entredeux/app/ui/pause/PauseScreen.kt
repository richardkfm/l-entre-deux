package org.entredeux.app.ui.pause

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.entredeux.app.R
import org.entredeux.app.domain.model.Intention

private data class IntentionOption(val intention: Intention, val labelRes: Int, val hintRes: Int)

private val intentionOptions = listOf(
    IntentionOption(
        Intention.SPECIFIC_TASK,
        R.string.pause_intention_specific_task,
        R.string.pause_intention_specific_task_hint,
    ),
    IntentionOption(
        Intention.BRIEF_CHECK,
        R.string.pause_intention_brief_check,
        R.string.pause_intention_brief_check_hint,
    ),
    IntentionOption(
        Intention.AUTOPILOT,
        R.string.pause_intention_autopilot,
        R.string.pause_intention_autopilot_hint,
    ),
)

@Composable
fun PauseScreen(
    viewModel: PauseViewModel,
    onProceed: () -> Unit,
    onBackOut: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val backdrop = Brush.verticalGradient(
        listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.40f),
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surface,
        ),
    )

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backdrop)
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.pause_kicker).uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 3.sp,
            )

            Spacer(Modifier.weight(0.6f))

            BreathingAura()

            Spacer(Modifier.weight(0.6f))

            Text(
                text = stringResource(R.string.pause_heading, uiState.appLabel),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { heading() },
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = stringResource(R.string.pause_intention_label),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(20.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup(),
            ) {
                intentionOptions.forEach { option ->
                    IntentionCard(
                        title = stringResource(option.labelRes),
                        hint = stringResource(option.hintRes),
                        selected = uiState.selectedIntention == option.intention,
                        onClick = { viewModel.selectIntention(option.intention) },
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            AnimatedVisibility(
                visible = uiState.selectedIntention != null,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut(),
            ) {
                Button(
                    onClick = {
                        viewModel.onProceed()
                        onProceed()
                    },
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                ) {
                    Text(
                        stringResource(R.string.pause_proceed, uiState.appLabel),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Stepping away is the gentle, affirming choice, so it's always a
            // substantial button — not a faint link — and tapping it takes the
            // user out of the app entirely (back to their home screen).
            FilledTonalButton(
                onClick = {
                    viewModel.onBackOut()
                    onBackOut()
                },
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) {
                Text(
                    stringResource(R.string.pause_back_out),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

@Composable
private fun IntentionCard(
    title: String,
    hint: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val container by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant,
        label = "container",
    )
    val titleColor =
        if (selected) MaterialTheme.colorScheme.onPrimaryContainer
        else MaterialTheme.colorScheme.onSurface

    Surface(
        color = container,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
        ) {
            SelectionDot(selected = selected)
            Spacer(Modifier.size(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = titleColor,
                )
                Text(
                    text = hint,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}

@Composable
private fun SelectionDot(selected: Boolean) {
    val ring by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outline,
        label = "ring",
    )
    val fill by animateDpAsState(if (selected) 12.dp else 0.dp, label = "fill")
    val core = MaterialTheme.colorScheme.primary

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(22.dp)) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(color = ring, style = Stroke(width = 2.dp.toPx()), radius = size.minDimension / 2 - 1.dp.toPx())
        }
        Box(
            modifier = Modifier
                .size(fill)
                .clip(CircleShape)
                .background(core),
        )
    }
}

// Layered breathing visualisation: a soft radial glow, expanding ripple
// rings, and a gentle gradient core that swells and settles like a breath.
// Purely decorative — not announced to screen readers.
@Composable
private fun BreathingAura() {
    val transition = rememberInfiniteTransition(label = "aura")
    val breath by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breath",
    )
    val ripple by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "ripple",
    )

    val glow = MaterialTheme.colorScheme.primary
    val coreInner = MaterialTheme.colorScheme.primaryContainer
    val coreOuter = MaterialTheme.colorScheme.primary

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(240.dp)) {
        Canvas(Modifier.fillMaxSize()) {
            val maxR = size.minDimension / 2f

            // Soft outer glow that swells with the breath.
            val glowR = maxR * (0.7f + 0.3f * breath)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glow.copy(alpha = 0.22f + 0.10f * breath), Color.Transparent),
                    center = center,
                    radius = glowR,
                ),
                radius = glowR,
                center = center,
            )

            // Three phase-shifted rings rising outward and fading — a calm ripple.
            repeat(3) { i ->
                val frac = (ripple + i / 3f) % 1f
                val r = maxR * (0.32f + 0.62f * frac)
                val alpha = (1f - frac).coerceIn(0f, 1f) * 0.45f
                drawCircle(
                    color = glow.copy(alpha = alpha),
                    radius = r,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx()),
                )
            }

            // Breathing core orb with an inner-to-outer gradient.
            val coreR = maxR * (0.30f + 0.10f * breath)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(coreInner, coreOuter),
                    center = center,
                    radius = coreR,
                ),
                radius = coreR,
                center = center,
            )
        }
    }
}
