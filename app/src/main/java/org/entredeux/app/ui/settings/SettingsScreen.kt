package org.entredeux.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.entredeux.app.R

private val budgetOptions: List<Int?> = listOf(null, 3, 5, 10)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateToSelection: () -> Unit,
    onBack: () -> Unit,
) {
    val defaultBudget by viewModel.defaultBudgetMinutes.collectAsStateWithLifecycle()
    var showWipeConfirm by remember { mutableStateOf(false) }
    var wipeSnackMessage by remember { mutableStateOf<String?>(null) }

    if (showWipeConfirm) {
        AlertDialog(
            onDismissRequest = { showWipeConfirm = false },
            title = { Text(stringResource(R.string.settings_wipe_confirm_title)) },
            text = { Text(stringResource(R.string.settings_wipe_confirm_body)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.wipeSessionLog()
                    showWipeConfirm = false
                    wipeSnackMessage = "done"
                }) {
                    Text(
                        stringResource(R.string.settings_wipe_confirm_yes),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showWipeConfirm = false }) {
                    Text(stringResource(R.string.settings_wipe_confirm_no))
                }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_manage_apps)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateToSelection),
            )
            HorizontalDivider()

            Text(
                text = stringResource(R.string.settings_default_budget),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp),
            )

            budgetOptions.forEach { minutes ->
                val label = if (minutes == null) {
                    stringResource(R.string.settings_default_budget_none)
                } else {
                    when (minutes) {
                        3 -> stringResource(R.string.pause_budget_3min)
                        5 -> stringResource(R.string.pause_budget_5min)
                        else -> stringResource(R.string.pause_budget_10min)
                    }
                }
                ListItem(
                    headlineContent = { Text(label) },
                    trailingContent = if (defaultBudget == minutes) {
                        { Text("✓", color = MaterialTheme.colorScheme.primary) }
                    } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.setDefaultBudget(minutes) },
                )
            }

            HorizontalDivider()

            ListItem(
                headlineContent = {
                    Text(
                        stringResource(R.string.settings_wipe_data),
                        color = MaterialTheme.colorScheme.error,
                    )
                },
                supportingContent = {
                    Text(
                        stringResource(R.string.settings_wipe_data_desc),
                        style = MaterialTheme.typography.bodySmall,
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showWipeConfirm = true },
            )

            if (wipeSnackMessage != null) {
                Text(
                    text = stringResource(R.string.settings_wipe_done),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }
    }
}
