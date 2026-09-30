package org.entredeux.app.ui.reflection

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.entredeux.app.R
import org.entredeux.app.domain.model.Intention
import org.entredeux.app.domain.model.TimeOfDay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

// A weekly carnet: a few plain sentences, a dial of the day with one dot per
// pause (all drawn the same, proceeded or not), then the counts as rows of
// dots with the number beside them. No scores, no trends, no comparisons.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReflectionScreen(viewModel: ReflectionViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.reflection_title)) })
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            item(key = "period") {
                PeriodPicker(uiState.period, viewModel::choose)
            }
            when (val state = uiState) {
                is ReflectionUiState.Loading -> Unit
                is ReflectionUiState.Empty -> item(key = "empty") {
                    Text(
                        text = stringResource(
                            if (state.hasHistory) R.string.reflection_empty_period else R.string.reflection_empty,
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                    )
                }
                is ReflectionUiState.Ready -> carnet(state)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeriodPicker(selected: ReflectionPeriod, onChoose: (ReflectionPeriod) -> Unit) {
    val options = listOf(
        ReflectionPeriod.WEEK to R.string.reflection_period_week,
        ReflectionPeriod.ALL to R.string.reflection_period_all,
    )
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        options.forEachIndexed { index, (period, label) ->
            SegmentedButton(
                selected = selected == period,
                onClick = { onChoose(period) },
                shape = SegmentedButtonDefaults.itemShape(index, options.size),
            ) {
                Text(stringResource(label))
            }
        }
    }
}

private fun LazyListScope.carnet(state: ReflectionUiState.Ready) {
    item(key = "range") { RangeLine(state) }
    item(key = "summary") { Summary(state) }
    item(key = "dial") { DayDial(state.stats.hourly) }

    item(key = "intentions_header") { SectionHeader(stringResource(R.string.reflection_section_intentions)) }
    items(state.stats.intentionMix, key = { "intention_${it.intention.name}" }) {
        CountRow(label = intentionLabel(it.intention), count = it.count)
    }
    item(key = "not_now") {
        CountRow(label = stringResource(R.string.pause_back_out), count = state.stats.backedOutCount)
    }

    item(key = "apps_header") { SectionHeader(stringResource(R.string.reflection_section_apps)) }
    items(state.stats.perApp, key = { "app_${it.packageName}" }) {
        CountRow(label = state.appLabels[it.packageName] ?: it.packageName, count = it.count)
    }

    item(key = "time_header") { SectionHeader(stringResource(R.string.reflection_section_time_of_day)) }
    items(state.stats.timeOfDay, key = { "tod_${it.period.name}" }) {
        CountRow(label = periodLabel(it.period), count = it.count)
    }
}

@Composable
private fun RangeLine(state: ReflectionUiState.Ready) {
    val locale = LocalConfiguration.current.locales[0]
    val text = if (state.period == ReflectionPeriod.WEEK) {
        dateRange(state.from, state.to, locale)
    } else {
        stringResource(R.string.reflection_since, DateTimeFormatter.ofPattern("d MMMM yyyy", locale).format(state.from))
    }
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp),
    )
}

private fun dateRange(from: LocalDate, to: LocalDate, locale: Locale): String {
    val full = DateTimeFormatter.ofPattern("d MMMM", locale)
    val start = if (from.month == to.month) DateTimeFormatter.ofPattern("d", locale).format(from) else full.format(from)
    return "$start – ${full.format(to)}"
}

@Composable
private fun Summary(state: ReflectionUiState.Ready) {
    val resources = LocalContext.current.resources
    val summary = state.summary
    val sentences = buildList {
        add(resources.getQuantityString(R.plurals.reflection_sentence_total, summary.totalPauses, summary.totalPauses))
        summary.busiestPeriod?.let { add(stringResource(R.string.reflection_sentence_period, periodPhrase(it))) }
        summary.mostOftenApp?.let { pkg ->
            add(stringResource(R.string.reflection_sentence_app, state.appLabels[pkg] ?: pkg))
        }
        if (summary.notNowCount > 0) {
            add(resources.getQuantityString(R.plurals.reflection_backed_out, summary.notNowCount, summary.notNowCount))
        }
    }
    Text(
        text = sentences.joinToString(" "),
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
    )
}

// One dot per pause at its hour around a 24-hour ring, midnight at the top;
// several pauses in one hour stack inward. The numbers are in the rows
// below for screen readers; here the dial only has a summary label.
@Composable
private fun DayDial(hourly: List<Int>) {
    val colors = MaterialTheme.colorScheme
    val measurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelSmall.copy(color = colors.onSurfaceVariant)
    val description = stringResource(R.string.reflection_dial_cd)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        Canvas(
            Modifier
                .size(220.dp)
                .semantics { contentDescription = description },
        ) {
            val inset = 20.dp.toPx()
            val ring = size.minDimension / 2f - inset
            val step = 9.dp.toPx()
            val dot = 3.4.dp.toPx()
            drawCircle(colors.outlineVariant, radius = ring, style = Stroke(1.dp.toPx()))
            listOf(0, 6, 12, 18).forEach { hour ->
                val a = hour / 24f * 2f * PI.toFloat() - PI.toFloat() / 2f
                val label = measurer.measure(hour.toString(), labelStyle)
                val r = ring + inset / 2f + 2.dp.toPx()
                drawText(
                    label,
                    topLeft = Offset(
                        center.x + r * cos(a) - label.size.width / 2f,
                        center.y + r * sin(a) - label.size.height / 2f,
                    ),
                )
            }
            hourly.forEachIndexed { hour, count ->
                val a = (hour + 0.5f) / 24f * 2f * PI.toFloat() - PI.toFloat() / 2f
                repeat(min(count, MAX_STACK)) { k ->
                    val r = ring - k * step
                    drawCircle(colors.primary, radius = dot, center = Offset(center.x + r * cos(a), center.y + r * sin(a)))
                }
            }
        }
    }
}

@Composable
private fun CountRow(label: String, count: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .padding(horizontal = 16.dp, vertical = 6.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Dots(count)
        Spacer(Modifier.width(12.dp))
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum"),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            modifier = Modifier.width(32.dp),
        )
    }
}

@Composable
private fun Dots(count: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(min(count, MAX_DOTS)) {
            Box(
                Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 4.dp)
            .semantics { heading() },
    )
}

@Composable
private fun intentionLabel(intention: Intention): String = when (intention) {
    Intention.SPECIFIC_TASK -> stringResource(R.string.pause_intention_specific_task)
    Intention.BRIEF_CHECK -> stringResource(R.string.pause_intention_brief_check)
    Intention.AUTOPILOT -> stringResource(R.string.pause_intention_autopilot)
}

@Composable
private fun periodLabel(period: TimeOfDay): String = when (period) {
    TimeOfDay.MORNING -> stringResource(R.string.reflection_time_morning)
    TimeOfDay.AFTERNOON -> stringResource(R.string.reflection_time_afternoon)
    TimeOfDay.EVENING -> stringResource(R.string.reflection_time_evening)
    TimeOfDay.NIGHT -> stringResource(R.string.reflection_time_night)
}

@Composable
private fun periodPhrase(period: TimeOfDay): String = when (period) {
    TimeOfDay.MORNING -> stringResource(R.string.reflection_when_morning)
    TimeOfDay.AFTERNOON -> stringResource(R.string.reflection_when_afternoon)
    TimeOfDay.EVENING -> stringResource(R.string.reflection_when_evening)
    TimeOfDay.NIGHT -> stringResource(R.string.reflection_when_night)
}

// Past these the dots stop adding information; the number beside them
// carries the rest.
private const val MAX_DOTS = 12
private const val MAX_STACK = 6
