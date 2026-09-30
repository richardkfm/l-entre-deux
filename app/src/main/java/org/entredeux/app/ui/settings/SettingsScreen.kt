package org.entredeux.app.ui.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.entredeux.app.R
import org.entredeux.app.domain.model.Look

private const val SOURCE_URL = "https://github.com/richardkfm/l-entre-deux"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateToSelection: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val version = remember(context) {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()
    }
    var showWipeConfirm by remember { mutableStateOf(false) }
    var wiped by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val wipedMessage = stringResource(R.string.settings_wipe_done)
    LaunchedEffect(wiped) {
        if (wiped) {
            snackbarHostState.showSnackbar(wipedMessage)
            wiped = false
        }
    }

    if (showWipeConfirm) {
        AlertDialog(
            onDismissRequest = { showWipeConfirm = false },
            title = { Text(stringResource(R.string.settings_wipe_confirm_title)) },
            text = { Text(stringResource(R.string.settings_wipe_confirm_body)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.wipeSessionLog()
                    showWipeConfirm = false
                    wiped = true
                }) {
                    Text(stringResource(R.string.settings_wipe_confirm_yes))
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            SectionHeader(stringResource(R.string.settings_section_apps))
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_manage_apps)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.Button, onClick = onNavigateToSelection),
            )

            HorizontalDivider()
            SectionHeader(stringResource(R.string.settings_section_look))
            LookOption(
                title = stringResource(R.string.settings_look_papier),
                description = stringResource(R.string.settings_look_papier_desc),
                selected = uiState.look == Look.PAPIER,
                onSelect = { viewModel.setLook(Look.PAPIER) },
            )
            LookOption(
                title = stringResource(R.string.settings_look_material),
                description = stringResource(R.string.settings_look_material_desc),
                selected = uiState.look == Look.MATERIAL,
                onSelect = { viewModel.setLook(Look.MATERIAL) },
            )
            if (uiState.look == Look.PAPIER) {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.settings_tint)) },
                    supportingContent = { Text(stringResource(R.string.settings_tint_desc)) },
                    trailingContent = { Switch(checked = uiState.tintByTime, onCheckedChange = null) },
                    modifier = Modifier.toggleable(
                        value = uiState.tintByTime,
                        role = Role.Switch,
                        onValueChange = viewModel::setTintByTime,
                    ),
                )
            }

            HorizontalDivider()
            SectionHeader(stringResource(R.string.settings_section_pause))
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_grace)) },
                supportingContent = { Text(stringResource(R.string.settings_grace_desc)) },
                trailingContent = { Switch(checked = uiState.graceWindow, onCheckedChange = null) },
                modifier = Modifier.toggleable(
                    value = uiState.graceWindow,
                    role = Role.Switch,
                    onValueChange = viewModel::setGraceWindow,
                ),
            )

            HorizontalDivider()
            SectionHeader(stringResource(R.string.settings_section_data))
            Text(
                text = stringResource(R.string.settings_data_promise),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_wipe_data)) },
                supportingContent = { Text(stringResource(R.string.settings_wipe_data_desc)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.Button) { showWipeConfirm = true },
            )

            HorizontalDivider()
            SectionHeader(stringResource(R.string.settings_section_about))
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_version)) },
                trailingContent = { Text(version) },
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_source)) },
                supportingContent = { Text(stringResource(R.string.settings_source_desc)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.Button) {
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(SOURCE_URL)))
                        } catch (_: ActivityNotFoundException) {
                            // No browser installed; nothing sensible to do.
                        }
                    },
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_typeface)) },
                supportingContent = { Text(stringResource(R.string.settings_typeface_desc)) },
            )
        }
    }
}

@Composable
private fun LookOption(title: String, description: String, selected: Boolean, onSelect: () -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(description) },
        leadingContent = { RadioButton(selected = selected, onClick = null) },
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, role = Role.RadioButton, onClick = onSelect),
    )
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
