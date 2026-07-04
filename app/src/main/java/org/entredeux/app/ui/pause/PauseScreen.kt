package org.entredeux.app.ui.pause

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
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

private data class IntentionOption(val intention: Intention, val labelRes: Int)

private val intentionOptions = listOf(
    IntentionOption(Intention.SPECIFIC_TASK, R.string.pause_intention_specific_task),
    IntentionOption(Intention.BRIEF_CHECK, R.string.pause_intention_brief_check),
    IntentionOption(Intention.AUTOPILOT, R.string.pause_intention_autopilot),
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
    // new pause): a random reflective line, and a shuffled order for the four
    // buttons so the screen can't be cleared from muscle memory. Both opening
    // and leaving stay clearly labelled — only the order changes.
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

    Scaffold { innerPadding ->
        // No scrolling — the breathing aura takes whatever vertical space is
        // left after the fixed elements, so the whole screen always fits in
        // one view on any device without ever clipping a button.
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
            Spacer(Modifier.height(28.dp))
            BreathingAura(
                Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )
            PauseHeader(uiState.appLabel)
            Spacer(Modifier.height(20.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                actions.forEach { action ->
                    when (action) {
                        is PauseAction.Choose -> PauseButton(
                            title = stringResource(action.option.labelRes),
                            onClick = {
                                viewModel.proceed(action.option.intention)
                                onProceed()
                            },
                        )

                        PauseAction.Leave -> PauseButton(
                            title = stringResource(R.string.pause_back_out),
                            onClick = {
                                viewModel.backOut()
                                onBackOut()
                            },
                        )
                    }
                }
            }
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

// Every choice on the pause screen — the three intentions and the get-out
// action — uses this one identical button so leaving can't be told apart
// by shape or colour and has to be read like any other option.
@Composable
private fun PauseButton(title: String, onClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(role = Role.Button, onClick = onClick),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }
}

private class Dot(
    val rr: Float,        // base distance from centre, 0..1
    val theta0: Float,    // base angle
    // Two superimposed epicycles (the second counter-rotating) trace an
    // organic, non-circular path. Whole-number cycles keep the loop seamless.
    val cyc1: Float,
    val phase1: Float,
    val r1: Float,
    val cyc2: Float,
    val phase2: Float,
    val r2: Float,
    // A slow angular sway so the spin reads as fluid, not rigid-body.
    val swayCyc: Float,
    val swayPhase: Float,
    val sizeFactor: Float,
)

// A phyllotaxis (sunflower) spread gives an organic, non-grid scatter.
private fun buildDots(count: Int): List<Dot> {
    val golden = (PI * (3.0 - sqrt(5.0))).toFloat()
    return List(count) { i ->
        val rnd1 = ((i * 9301 + 49297) % 233280) / 233280f
        val rnd2 = ((i * 4801 + 9973) % 134456) / 134456f
        Dot(
            rr = sqrt((i + 0.5f) / count),
            theta0 = i * golden,
            cyc1 = (1 + (i % 2)).toFloat(),
            phase1 = rnd1,
            r1 = 0.018f + rnd1 * 0.032f,
            cyc2 = -(2 + (i % 2)).toFloat(),
            phase2 = rnd2,
            r2 = 0.010f + rnd2 * 0.022f,
            swayCyc = (1 + (i % 3)).toFloat(),
            swayPhase = rnd1,
            sizeFactor = 0.5f + rnd2,
        )
    }
}

// Skewed breath waveform: warping the phase with a sine makes the rise
// (inhale) quicker and the fall (exhale) longer, like real breathing.
// Still strictly 2π-periodic, so the loop stays seamless.
private fun breathWave(phase: Float): Float {
    val theta = phase * (2.0 * PI).toFloat()
    return 0.5f - 0.5f * cos(theta + 0.45f * sin(theta))
}

// A living layer of dots that together form one slowly turning whole: the
// field rotates like a galaxy (with a gentle per-dot sway so the turn feels
// fluid rather than rigid), each dot rides its own small epicycle, the
// breath ripples outward through the field instead of scaling it in
// lockstep, and the whole aura drifts slightly around its anchor — all on
// whole-number cycles so the loop is seamless (no jump back). Purely
// decorative; not read by screen readers.
@Composable
private fun BreathingAura(modifier: Modifier = Modifier) {
    val dots = remember { buildDots(84) }
    val transition = rememberInfiniteTransition(label = "aura")
    // Raw 0..1 phase (not a reversing tween) so each dot can sample the
    // breath waveform at its own radial lag.
    val breathPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "breath",
    )
    val spin by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 42000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "spin",
    )
    val orbit by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 17000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "orbit",
    )

    val dotColor = MaterialTheme.colorScheme.primary
    val glowColor = MaterialTheme.colorScheme.primary
    val twoPi = (2.0 * PI).toFloat()
    val waveCycles = 2f
    val waveLength = 2.2f
    val breathLag = 0.18f

    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        Canvas(Modifier.fillMaxSize()) {
            val maxR = size.minDimension / 2f
            val field = maxR * 0.9f
            val spinAngle = spin * twoPi
            val baseDot = maxR * 0.013f

            // The whole field wanders a little around its anchor, like
            // something floating rather than something mounted.
            val cx = center.x + maxR * 0.020f * sin(orbit * twoPi)
            val cy = center.y + maxR * 0.016f * cos(orbit * 2f * twoPi + 1f)

            // Soft central glow for depth, breathing with the centre of the
            // field (lag zero — the breath starts here and ripples outward).
            val glowBreath = breathWave(breathPhase)
            val glowR = maxR * (0.6f + 0.32f * glowBreath)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowColor.copy(alpha = 0.12f + 0.08f * glowBreath), Color.Transparent),
                    center = Offset(cx, cy),
                    radius = glowR,
                ),
                radius = glowR,
                center = Offset(cx, cy),
            )

            dots.forEach { d ->
                // Each dot breathes slightly after the one inside it, so the
                // inhale travels outward through the field.
                val breath = breathWave(breathPhase - d.rr * breathLag)
                val breathScale = 0.82f + 0.18f * breath

                val sway = 0.09f * (1.2f - d.rr) *
                    sin((orbit * d.swayCyc + d.swayPhase) * twoPi)
                val angle = d.theta0 + spinAngle + sway
                val baseR = d.rr * field * breathScale
                val e1 = (orbit * d.cyc1 + d.phase1) * twoPi
                val e2 = (orbit * d.cyc2 + d.phase2) * twoPi
                val ox = (d.r1 * cos(e1) + d.r2 * cos(e2)) * maxR
                val oy = (d.r1 * sin(e1) + d.r2 * sin(e2)) * maxR
                val x = cx + baseR * cos(angle) + ox
                val y = cy + baseR * sin(angle) + oy

                // A brightness wave travelling outward through the structure;
                // a touch of per-dot jitter keeps it from reading as perfect
                // concentric rings.
                val wave = 0.5f + 0.5f *
                    sin((orbit * waveCycles - d.rr * waveLength + (d.phase2 - 0.5f) * 0.14f) * twoPi)
                val bright = 0.45f * breath + 0.55f * wave
                val edgeFade = 1f - d.rr * 0.45f
                val alpha = ((0.12f + 0.55f * bright) * edgeFade).coerceIn(0f, 1f)
                val radius = baseDot * (0.5f + 0.8f * d.sizeFactor) * (0.6f + 0.5f * bright)
                drawCircle(color = dotColor.copy(alpha = alpha), radius = radius, center = Offset(x, y))
            }
        }
    }
}
