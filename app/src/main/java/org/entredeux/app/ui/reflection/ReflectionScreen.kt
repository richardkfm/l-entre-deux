package org.entredeux.app.ui.reflection

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.entredeux.app.R
import org.entredeux.app.domain.model.AppPauseCount
import org.entredeux.app.domain.model.Intention
import org.entredeux.app.domain.model.IntentionCount
import org.entredeux.app.domain.model.ReflectionStats
import org.entredeux.app.domain.model.TimeOfDay
import org.entredeux.app.domain.model.TimeOfDayCount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReflectionScreen(viewModel: ReflectionViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.reflection_title)) })
        },
    ) { innerPadding ->
        when (val state = uiState) {
            ReflectionUiState.Loading -> Unit
            ReflectionUiState.Empty -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(32.dp),
                ) {
                    Text(
                        text = stringResource(R.string.reflection_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            is ReflectionUiState.Ready -> {
                ReflectionContent(
                    stats = state.stats,
                    appLabels = state.appLabels,
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}

@Composable
private fun ReflectionContent(
    stats: ReflectionStats,
    appLabels: Map<String, String>,
    modifier: Modifier = Modifier,
) {
    val resources = LocalContext.current.resources
    LazyColumn(modifier = modifier.fillMaxSize()) {
        item(key = "overview_header") {
            SectionHeader(stringResource(R.string.reflection_section_overview))
        }
        item(key = "total_pauses") {
            ListItem(headlineContent = {
                Text(
                    resources.getQuantityString(
                        R.plurals.reflection_total_pauses,
                        stats.totalPauses,
                        stats.totalPauses,
                    )
                )
            })
        }
        item(key = "backed_out") {
            ListItem(headlineContent = {
                Text(
                    resources.getQuantityString(
                        R.plurals.reflection_backed_out,
                        stats.backedOutCount,
                        stats.backedOutCount,
                    )
                )
            })
        }

        item(key = "apps_divider") { HorizontalDivider() }
        item(key = "apps_header") { SectionHeader(stringResource(R.string.reflection_section_apps)) }
        items(stats.perApp, key = { "app_${it.packageName}" }) { appCount ->
            AppCountRow(appCount, appLabels)
        }

        item(key = "intentions_divider") { HorizontalDivider() }
        item(key = "intentions_header") { SectionHeader(stringResource(R.string.reflection_section_intentions)) }
        items(stats.intentionMix, key = { "intention_${it.intention.name}" }) { intentionCount ->
            IntentionCountRow(intentionCount)
        }

        item(key = "time_divider") { HorizontalDivider() }
        item(key = "time_header") { SectionHeader(stringResource(R.string.reflection_section_time_of_day)) }
        items(stats.timeOfDay, key = { "tod_${it.period.name}" }) { todCount ->
            TimeOfDayRow(todCount)
        }

        item(key = "budget_divider") { HorizontalDivider() }
        item(key = "budget_header") { SectionHeader(stringResource(R.string.reflection_section_budget)) }
        item(key = "budget_set") {
            ListItem(
                headlineContent = { Text(stringResource(R.string.reflection_budget_set)) },
                trailingContent = { Text("${stats.withBudgetCount}") },
            )
        }
        item(key = "budget_none") {
            ListItem(
                headlineContent = { Text(stringResource(R.string.reflection_budget_none)) },
                trailingContent = { Text("${stats.totalPauses - stats.withBudgetCount}") },
            )
        }
    }
}

@Composable
private fun AppCountRow(appCount: AppPauseCount, appLabels: Map<String, String>) {
    ListItem(
        headlineContent = { Text(appLabels[appCount.packageName] ?: appCount.packageName) },
        trailingContent = { Text("${appCount.count}") },
    )
}

@Composable
private fun IntentionCountRow(intentionCount: IntentionCount) {
    val label = when (intentionCount.intention) {
        Intention.SPECIFIC_TASK -> stringResource(R.string.pause_intention_specific_task)
        Intention.BRIEF_CHECK -> stringResource(R.string.pause_intention_brief_check)
        Intention.AUTOPILOT -> stringResource(R.string.pause_intention_autopilot)
    }
    ListItem(
        headlineContent = { Text(label) },
        trailingContent = { Text("${intentionCount.count}") },
    )
}

@Composable
private fun TimeOfDayRow(todCount: TimeOfDayCount) {
    val label = when (todCount.period) {
        TimeOfDay.MORNING -> stringResource(R.string.reflection_time_morning)
        TimeOfDay.AFTERNOON -> stringResource(R.string.reflection_time_afternoon)
        TimeOfDay.EVENING -> stringResource(R.string.reflection_time_evening)
        TimeOfDay.NIGHT -> stringResource(R.string.reflection_time_night)
    }
    ListItem(
        headlineContent = { Text(label) },
        trailingContent = { Text("${todCount.count}") },
    )
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp),
    )
}
