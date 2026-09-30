package org.entredeux.app.ui.pause

import android.os.Build
import android.provider.Settings
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import org.entredeux.app.R
import org.entredeux.app.domain.model.Intention
import org.entredeux.app.domain.model.Look
import org.entredeux.app.ui.theme.LocalLook
import org.entredeux.app.ui.theme.Spectral
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
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

private data class Epigraph(val text: String, val author: String)

// How long the answer is acknowledged before the app opens (ink fill, the
// aura exhales), and how long "Not now" takes to disperse the aura.
private const val ACKNOWLEDGE_MS = 240
private const val DISPERSE_MS = 380

@Composable
fun PauseScreen(
    viewModel: PauseViewModel,
    onProceed: () -> Unit,
    onBackOut: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val look = LocalLook.current
    val motion = rememberMotionEnabled()
    val view = LocalView.current

    val texts = stringArrayResource(R.array.pause_epigraph_text)
    val authors = stringArrayResource(R.array.pause_epigraph_author)

    // Chosen once per visit (survive recomposition / rotation, fresh on each
    // new pause): a random epigraph, and a shuffled order for the four
    // buttons so the screen can't be cleared from muscle memory. Both opening
    // and leaving stay clearly labelled — only the order changes.
    val epigraphSeed = rememberSaveable { Random.nextInt() }
    val orderSeed = rememberSaveable { Random.nextInt() }
    val index = epigraphSeed.mod(texts.size)
    val epigraph = Epigraph(texts[index], authors[index])
    val actions = remember(orderSeed) {
        (intentionOptions.map { PauseAction.Choose(it) } + PauseAction.Leave)
            .shuffled(Random(orderSeed.toLong()))
    }

    var chosen by remember { mutableStateOf<PauseAction?>(null) }
    val release by animateFloatAsState(
        targetValue = if (chosen != null) 1f else 0f,
        animationSpec = tween(
            durationMillis = when {
                !motion -> 0
                chosen == PauseAction.Leave -> DISPERSE_MS
                else -> ACKNOWLEDGE_MS
            },
            easing = FastOutSlowInEasing,
        ),
        label = "release",
    )
    // Logged at the tap so nothing is lost if the process dies mid-animation;
    // the callback follows once the acknowledgement has been seen.
    LaunchedEffect(chosen) {
        val action = chosen ?: return@LaunchedEffect
        if (motion) delay(if (action == PauseAction.Leave) DISPERSE_MS.toLong() else ACKNOWLEDGE_MS.toLong())
        if (action == PauseAction.Leave) onBackOut() else onProceed()
    }
    val pick: (PauseAction) -> Unit = { action ->
        if (chosen == null) {
            when (action) {
                is PauseAction.Choose -> {
                    // One tick when an intention is named. Nothing on "Not
                    // now", which should feel like setting something down.
                    view.performHapticFeedback(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            HapticFeedbackConstants.CONFIRM
                        } else {
                            HapticFeedbackConstants.VIRTUAL_KEY
                        },
                    )
                    viewModel.proceed(action.option.intention)
                }
                PauseAction.Leave -> viewModel.backOut()
            }
            chosen = action
        }
    }

    val aura: @Composable ColumnScope.() -> Unit = {
        BreathingAura(
            seed = viewModel.packageName.hashCode(),
            release = release,
            dispersing = chosen == PauseAction.Leave,
            motion = motion,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        )
    }
    val answers: @Composable () -> Unit = {
        Column(
            verticalArrangement = Arrangement.spacedBy(if (look == Look.PAPIER) 8.dp else 10.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            actions.forEach { action ->
                PauseAnswer(
                    title = when (action) {
                        is PauseAction.Choose -> stringResource(action.option.labelRes)
                        PauseAction.Leave -> stringResource(R.string.pause_back_out)
                    },
                    look = look,
                    inked = action is PauseAction.Choose && chosen == action,
                    motion = motion,
                    onClick = { pick(action) },
                )
            }
        }
    }

    val backdrop = if (look == Look.MATERIAL) {
        Brush.verticalGradient(
            listOf(
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.40f),
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.surface,
            ),
        )
    } else {
        Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface))
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.surface) { innerPadding ->
        // The breathing aura takes whatever vertical space is left after the
        // fixed elements, so the screen fits one view on any normal device.
        // The column is still scrollable with a minimum height of the
        // viewport: under infinite height a weighted child only gets the
        // leftover of that minimum, so at very large font sizes the aura
        // shrinks to nothing and the answers scroll instead of being clipped.
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(backdrop)
                .padding(innerPadding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = maxHeight)
                    .padding(horizontal = 24.dp, vertical = if (look == Look.PAPIER) 16.dp else 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                when (look) {
                    Look.PAPIER -> PapierLayout(uiState.appLabel, uiState.appIcon, epigraph, aura, answers)
                    Look.MATERIAL -> MaterialLayout(uiState.appLabel, epigraph, aura, answers)
                }
            }
        }
    }
}

// Paper and ink: a signed epigraph like a book's, the aura, the target app
// named plainly under its greyed-out icon, and four hairline answers.
@Composable
private fun ColumnScope.PapierLayout(
    appLabel: String,
    appIcon: ImageBitmap?,
    epigraph: Epigraph,
    aura: @Composable ColumnScope.() -> Unit,
    answers: @Composable () -> Unit,
) {
    Column(horizontalAlignment = Alignment.End, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = epigraph.text,
            style = MaterialTheme.typography.bodyLarge.copy(fontFamily = Spectral, fontStyle = FontStyle.Italic),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth(0.84f),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = epigraph.author.lowercase(),
            style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = Spectral,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.2.sp,
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    Spacer(Modifier.height(16.dp))
    aura()
    if (appIcon != null) {
        Image(
            bitmap = appIcon,
            contentDescription = null,
            // Greyed out on purpose: the colourful icon is itself a cue to
            // reach; here it only says which app is waiting.
            colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }),
            modifier = Modifier
                .size(40.dp)
                .alpha(0.85f),
        )
        Spacer(Modifier.height(10.dp))
    }
    Text(
        text = appLabel,
        style = MaterialTheme.typography.headlineLarge,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        modifier = Modifier.semantics { heading() },
    )
    Spacer(Modifier.height(2.dp))
    Text(
        text = stringResource(R.string.pause_intention_label),
        style = MaterialTheme.typography.titleMedium.copy(fontFamily = Spectral, fontStyle = FontStyle.Italic),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(20.dp))
    answers()
}

// The 1.0.x look, kept as the Material option in Settings.
@Composable
private fun ColumnScope.MaterialLayout(
    appLabel: String,
    epigraph: Epigraph,
    aura: @Composable ColumnScope.() -> Unit,
    answers: @Composable () -> Unit,
) {
    Spacer(Modifier.height(8.dp))
    Text(
        text = stringResource(R.string.pause_epigraph_line, epigraph.text, epigraph.author),
        style = MaterialTheme.typography.titleSmall,
        fontStyle = FontStyle.Italic,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 8.dp),
    )
    Spacer(Modifier.height(28.dp))
    aura()
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
    Spacer(Modifier.height(20.dp))
    answers()
}

// Every choice on the pause screen — the three intentions and the get-out
// action — uses this one identical button so leaving can't be told apart
// by shape or colour and has to be read like any other option. A chosen
// intention fills with ink from where the finger touched.
@Composable
private fun PauseAnswer(
    title: String,
    look: Look,
    inked: Boolean,
    motion: Boolean,
    onClick: () -> Unit,
) {
    var press by remember { mutableStateOf<Offset?>(null) }
    val ink by animateFloatAsState(
        targetValue = if (inked) 1f else 0f,
        animationSpec = tween(if (motion) ACKNOWLEDGE_MS else 0, easing = FastOutSlowInEasing),
        label = "ink",
    )
    val colors = MaterialTheme.colorScheme
    val inkColor = colors.primary
    val papier = look == Look.PAPIER
    val shape = RoundedCornerShape(percent = 50)
    val surface = if (papier) {
        Modifier.border(1.dp, lerp(colors.outline, inkColor, ink), shape)
    } else {
        Modifier.background(colors.surfaceVariant)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = if (papier) 52.dp else 56.dp)
            .clip(shape)
            .then(surface)
            .drawBehind {
                if (ink > 0f) {
                    val origin = press ?: center
                    val reach = hypot(max(origin.x, size.width - origin.x), max(origin.y, size.height - origin.y))
                    drawCircle(inkColor, radius = reach * ink, center = origin)
                }
            }
            .pointerInput(Unit) {
                awaitEachGesture { press = awaitFirstDown(requireUnconsumed = false).position }
            }
            .clickable(role = Role.Button, onClick = onClick),
    ) {
        Text(
            text = title,
            style = if (papier) {
                MaterialTheme.typography.titleMedium.copy(fontFamily = Spectral, fontSize = 18.sp, fontWeight = FontWeight.Normal)
            } else {
                MaterialTheme.typography.titleMedium
            },
            color = lerp(colors.onSurface, colors.onPrimary, ink),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }
}

// Honour the system's "remove animations" setting: a still aura and no
// acknowledgement delay.
@Composable
private fun rememberMotionEnabled(): Boolean {
    val resolver = LocalContext.current.contentResolver
    return remember(resolver) {
        Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f
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

// A phyllotaxis (sunflower) spread gives an organic, non-grid scatter. The
// seed (the app's package name) sets the count, the turn and every dot's
// phases, so each app has its own recognisable constellation.
private fun buildDots(seed: Int): List<Dot> {
    val rnd = Random(seed)
    val count = 72 + rnd.nextInt(25)
    val golden = (PI * (3.0 - sqrt(5.0))).toFloat()
    val turn = rnd.nextFloat() * (2.0 * PI).toFloat()
    return List(count) { i ->
        val rnd1 = rnd.nextFloat()
        val rnd2 = rnd.nextFloat()
        Dot(
            rr = sqrt((i + 0.5f) / count),
            theta0 = i * golden + turn,
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
// whole-number cycles so the loop is seamless (no jump back). When an answer
// is chosen the field exhales (contracts and dims); on "Not now" it
// disperses outward and fades. Purely decorative; not read by screen readers.
@Composable
private fun BreathingAura(
    seed: Int,
    release: Float,
    dispersing: Boolean,
    motion: Boolean,
    modifier: Modifier = Modifier,
) {
    val dots = remember(seed) { buildDots(seed) }
    var breathPhase = 0.3f
    var spin = 0f
    var orbit = 0f
    if (motion) {
        val transition = rememberInfiniteTransition(label = "aura")
        // Raw 0..1 phase (not a reversing tween) so each dot can sample the
        // breath waveform at its own radial lag. It starts mid-inhale so the
        // field is visibly breathing within the first second.
        val breath by transition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 10000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "breath",
        )
        val turn by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 42000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "spin",
        )
        val drift by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 17000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
            label = "orbit",
        )
        breathPhase = breath
        spin = turn
        orbit = drift
    }

    val dotColor = MaterialTheme.colorScheme.primary
    val twoPi = (2.0 * PI).toFloat()
    val waveCycles = 2f
    val waveLength = 2.2f
    val breathLag = 0.18f
    val scale = if (dispersing) 1f + 0.18f * release else 1f - 0.14f * release
    val fade = if (dispersing) 1f - release else 1f - 0.35f * release

    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        Canvas(Modifier.fillMaxSize()) {
            // Below this the field is a speck, not an aura; draw nothing.
            if (size.minDimension < 72.dp.toPx()) return@Canvas
            val maxR = size.minDimension / 2f
            val field = maxR * 0.9f * scale
            val spinAngle = spin * twoPi
            val baseDot = maxR * 0.013f

            // The whole field wanders a little around its anchor, like
            // something floating rather than something mounted.
            val cx = center.x + maxR * 0.020f * sin(orbit * twoPi)
            val cy = center.y + maxR * 0.016f * cos(orbit * 2f * twoPi + 1f)

            // Soft central glow for depth, breathing with the centre of the
            // field (lag zero — the breath starts here and ripples outward).
            val glowBreath = breathWave(breathPhase)
            val glowR = maxR * (0.6f + 0.32f * glowBreath) * scale
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(dotColor.copy(alpha = (0.12f + 0.08f * glowBreath) * fade), Color.Transparent),
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
                val alpha = ((0.12f + 0.55f * bright) * edgeFade * fade).coerceIn(0f, 1f)
                val radius = baseDot * (0.5f + 0.8f * d.sizeFactor) * (0.6f + 0.5f * bright)
                drawCircle(color = dotColor.copy(alpha = alpha), radius = radius, center = Offset(x, y))
            }
        }
    }
}
