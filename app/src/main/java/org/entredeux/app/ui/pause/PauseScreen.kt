package org.entredeux.app.ui.pause

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.entredeux.app.R
import org.entredeux.app.domain.model.Intention

private data class IntentionOption(val intention: Intention, val labelRes: Int)

private val intentionOptions = listOf(
    IntentionOption(Intention.SPECIFIC_TASK, R.string.pause_intention_specific_task),
    IntentionOption(Intention.BRIEF_CHECK, R.string.pause_intention_brief_check),
    IntentionOption(Intention.AUTOPILOT, R.string.pause_intention_autopilot),
)

@Composable
fun PauseScreen(
    viewModel: PauseViewModel,
    onProceed: () -> Unit,
    onBackOut: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(1f))

            BreathingCircle()

            Spacer(Modifier.height(40.dp))

            Text(
                text = stringResource(R.string.pause_heading, uiState.appLabel),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { heading() },
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.pause_intention_label),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(12.dp))

            intentionOptions.forEach { option ->
                val label = stringResource(option.labelRes)
                FilterChip(
                    selected = uiState.selectedIntention == option.intention,
                    onClick = { viewModel.selectIntention(option.intention) },
                    label = { Text(label) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .semantics {
                            contentDescription = context.getString(
                                R.string.pause_intention_selected, label,
                            )
                        },
                )
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.onProceed()
                    onProceed()
                },
                enabled = uiState.selectedIntention != null,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.pause_proceed))
            }

            TextButton(
                onClick = {
                    viewModel.onBackOut()
                    onBackOut()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.pause_back_out))
            }
        }
    }
}

// A slow expand/contract circle that invites a single breath. Purely
// decorative — the screen is fully usable if motion is reduced or ignored.
@Composable
private fun BreathingCircle() {
    val transition = rememberInfiniteTransition(label = "breathing")
    val scale by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breathing-scale",
    )

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
        )
        Box(
            modifier = Modifier
                .size(56.dp)
                .scale(scale)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
        )
    }
}
