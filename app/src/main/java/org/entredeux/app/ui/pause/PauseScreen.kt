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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.entredeux.app.R
import org.entredeux.app.domain.model.Intention
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

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

// The four buttons whose order is shuffled each pause: the three intentions
// plus the get-out action. Everything else on the screen stays put.
private sealed interface PauseAction {
    data class Choose(val option: IntentionOption) : PauseAction
    data object Leave : PauseAction
}

@Composable
fun PauseScreen(
    viewModel: PauseViewModel,
    onProceed: () -> Unit,
    onBackOut: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val phrases = stringArrayResource(R.array.pause_phrases)

    // Chosen once per visit (survive recomposition / rotation, fresh on each
    // new pause). The phrase rotates, and the four action buttons are shuffled
    // so the screen can't be cleared from muscle memory — you have to read it.
    // Both proceeding and leaving stay clearly labelled; only order changes.
    val phraseSeed = rememberSaveable { Random.nextInt() }
    val orderSeed = rememberSaveable { Random.nextInt() }
    val phrase = phrases[phraseSeed.mod(phrases.size)]
    val actions = remember(orderSeed) {
        (intentionOptions.map { PauseAction.Choose(it) } + PauseAction.Leave)
            .shuffled(Random(orderSeed.toLong()))
    }

    val backdrop = Brush.verticalGradient(
        listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.40f),
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surface,
        ),
    )

    val selected = uiState.selectedIntention
    val proceed: () -> Unit = { viewModel.onProceed(); onProceed() }
    val leave: () -> Unit = { viewModel.onBackOut(); onBackOut() }

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
            PhraseLine(phrase)
            Spacer(Modifier.weight(0.6f))
            BreathingAura()
            Spacer(Modifier.weight(0.6f))
            PauseHeader(uiState.appLabel)
            Spacer(Modifier.height(20.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                actions.forEach { action ->
                    when (action) {
                        is PauseAction.Choose -> IntentionCard(
                            title = stringResource(action.option.labelRes),
                            hint = stringResource(action.option.hintRes),
                            selected = selected == action.option.intention,
                            onClick = { viewModel.selectIntention(action.option.intention) },
                        )

                        PauseAction.Leave -> LeaveButton(onClick = leave)
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            ProceedButton(visible = selected != null, appLabel = uiState.appLabel, onClick = proceed)
        }
    }
}

@Composable
private fun PhraseLine(phrase: String) {
    Text(
        text = phrase,
        style = MaterialTheme.typography.titleSmall,
        fontStyle = FontStyle.Italic,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 8.dp),
    )
}

@Composable
private fun PauseHeader(appLabel: String) {
    Text(
        text = stringResource(R.string.pause_heading, appLabel),
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
}

@Composable
private fun ProceedButton(visible: Boolean, appLabel: String, onClick: () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut(),
    ) {
        Button(
            onClick = onClick,
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) {
            Text(
                stringResource(R.string.pause_proceed, appLabel),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun LeaveButton(onClick: () -> Unit) {
    FilledTonalButton(
        onClick = onClick,
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

private class Dot(
    val baseX: Float,
    val baseY: Float,
    val ringR: Float,
    val phase: Float,
    val driftPhaseX: Float,
    val driftPhaseY: Float,
    val driftSpeedX: Float,
    val driftSpeedY: Float,
    val sizeFactor: Float,
)

// A phyllotaxis (sunflower) spread gives an organic, non-grid scatter.
private fun buildDots(count: Int): List<Dot> {
    val golden = (PI * (3.0 - sqrt(5.0))).toFloat()
    return List(count) { i ->
        val rr = sqrt((i + 0.5f) / count)
        val theta = i * golden
        val rnd = ((i * 9301 + 49297) % 233280) / 233280f
        Dot(
            baseX = rr * cos(theta),
            baseY = rr * sin(theta),
            ringR = rr,
            phase = (i * 0.6180340f) % 1f,
            driftPhaseX = rnd,
            driftPhaseY = (i * 0.7548777f) % 1f,
            driftSpeedX = 0.5f + (i % 4) * 0.17f,
            driftSpeedY = 0.6f + (i % 3) * 0.21f,
            sizeFactor = 0.5f + rnd,
        )
    }
}

// A living layer of small dots that breathe outward and back while each
// drifts on its own gentle orbit — organic, never quite the same frame
// twice. Purely decorative; not announced to screen readers.
@Composable
private fun BreathingAura() {
    val dots = remember { buildDots(76) }
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
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "drift",
    )

    val dotColor = MaterialTheme.colorScheme.primary
    val glowColor = MaterialTheme.colorScheme.primary
    val twoPi = (2.0 * PI).toFloat()

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(240.dp)) {
        Canvas(Modifier.fillMaxSize()) {
            val maxR = size.minDimension / 2f
            val field = maxR * 0.9f
            val globalScale = 0.78f + 0.24f * breath
            val driftAmp = maxR * 0.06f
            val baseDot = maxR * 0.02f

            // Soft central glow for depth, breathing with the field.
            val glowR = maxR * (0.65f + 0.30f * breath)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowColor.copy(alpha = 0.14f + 0.08f * breath), Color.Transparent),
                    center = center,
                    radius = glowR,
                ),
                radius = glowR,
                center = center,
            )

            dots.forEach { d ->
                val twinkle = 0.5f + 0.5f * sin((drift * d.driftSpeedX + d.phase) * twoPi)
                val pulse = 0.5f * breath + 0.5f * twinkle
                val dx = cos((drift * d.driftSpeedX + d.driftPhaseX) * twoPi) * driftAmp
                val dy = sin((drift * d.driftSpeedY + d.driftPhaseY) * twoPi) * driftAmp
                val x = center.x + d.baseX * field * globalScale + dx
                val y = center.y + d.baseY * field * globalScale + dy
                val edgeFade = 1f - d.ringR * 0.5f
                val alpha = ((0.16f + 0.60f * pulse) * edgeFade * (0.7f + 0.3f * breath))
                    .coerceIn(0f, 1f)
                val radius = baseDot * (0.6f + 1.1f * d.sizeFactor) * (0.55f + 0.5f * pulse)
                drawCircle(color = dotColor.copy(alpha = alpha), radius = radius, center = Offset(x, y))
            }
        }
    }
}
