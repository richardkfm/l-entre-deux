package org.entredeux.app.ui.pause

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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

private data class BudgetOption(val minutes: Int?, val labelRes: Int)

private val budgetOptions = listOf(
    BudgetOption(3, R.string.pause_budget_3min),
    BudgetOption(5, R.string.pause_budget_5min),
    BudgetOption(10, R.string.pause_budget_10min),
    BudgetOption(null, R.string.pause_budget_none),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PauseScreen(
    viewModel: PauseViewModel,
    onProceed: () -> Unit,
    onBackOut: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Permission launcher for POST_NOTIFICATIONS on Android 13+.
    // We proceed regardless of the grant outcome — the reminder is optional.
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { _ -> onProceed() }

    val handleProceed: () -> Unit = handleProceed@{
        viewModel.onProceed()
        val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            uiState.selectedBudgetMinutes != null &&
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        if (needsPermission) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            return@handleProceed
        }
        onProceed()
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.pause_heading, uiState.appLabel),
                style = MaterialTheme.typography.headlineSmall,
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.pause_intention_label),
                style = MaterialTheme.typography.titleMedium,
            )

            intentionOptions.forEach { option ->
                val label = stringResource(option.labelRes)
                FilterChip(
                    selected = uiState.selectedIntention == option.intention,
                    onClick = { viewModel.selectIntention(option.intention) },
                    label = { Text(label) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = context.getString(
                                R.string.pause_intention_selected, label,
                            )
                        },
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.pause_budget_label),
                style = MaterialTheme.typography.titleMedium,
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                budgetOptions.forEach { option ->
                    val label = stringResource(option.labelRes)
                    FilterChip(
                        selected = uiState.budgetChosen &&
                            uiState.selectedBudgetMinutes == option.minutes,
                        onClick = { viewModel.selectBudget(option.minutes) },
                        label = { Text(label) },
                        modifier = Modifier.semantics {
                            contentDescription = context.getString(
                                R.string.pause_budget_selected, label,
                            )
                        },
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = handleProceed,
                enabled = uiState.selectedIntention != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
            ) {
                Text(stringResource(R.string.pause_proceed))
            }

            OutlinedButton(
                onClick = {
                    viewModel.onBackOut()
                    onBackOut()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
            ) {
                Text(stringResource(R.string.pause_back_out))
            }
        }
    }
}
