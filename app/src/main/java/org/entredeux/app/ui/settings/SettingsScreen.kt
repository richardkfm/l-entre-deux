package org.entredeux.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import org.entredeux.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateToSelection: () -> Unit,
) {
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
            TopAppBar(title = { Text(stringResource(R.string.settings_title)) })
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
                    .clickable(role = Role.Button, onClick = onNavigateToSelection),
            )
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
                    .clickable(role = Role.Button) { showWipeConfirm = true },
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
