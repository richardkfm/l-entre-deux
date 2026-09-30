package org.entredeux.app.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.entredeux.app.R
import org.entredeux.app.domain.model.Look
import org.entredeux.app.ui.theme.LocalLook
import org.entredeux.app.ui.theme.Spectral

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToSelection: () -> Unit,
    onNavigateToPause: (String) -> Unit,
    onTryPause: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val look = LocalLook.current

    LifecycleResumeEffect(Unit) {
        viewModel.refreshPinned()
        onPauseOrDispose { }
    }

    val unsupportedMsg = stringResource(R.string.home_shortcut_unsupported)
    val pinnedTemplate = stringResource(R.string.home_shortcut_success)
    LaunchedEffect(uiState.message) {
        val msg = uiState.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(
            when (msg) {
                is HomeMessage.Pinned -> pinnedTemplate.format(msg.label)
                HomeMessage.Unsupported -> unsupportedMsg
            },
        )
        viewModel.clearMessage()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = if (look == Look.PAPIER) {
                            MaterialTheme.typography.titleLarge.copy(fontStyle = FontStyle.Italic)
                        } else {
                            MaterialTheme.typography.titleLarge
                        },
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSelection) {
                        Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.home_add_apps))
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize())
            uiState.rows.isEmpty() -> EmptyApps(
                onChoose = onNavigateToSelection,
                modifier = Modifier.padding(innerPadding),
            )
            else -> LazyColumn(
                contentPadding = PaddingValues(bottom = 24.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                if (uiState.unpinned.isNotEmpty()) {
                    item(key = "unpinned_header") { SectionLabel(stringResource(R.string.apps_section_unpinned)) }
                    items(uiState.unpinned, key = { "u_" + it.app.packageName }) { row ->
                        val pinDesc = stringResource(R.string.home_pin_shortcut_cd, row.app.label)
                        AppRowItem(row, look, onOpen = { onNavigateToPause(row.app.packageName) }) {
                            FilledTonalButton(
                                onClick = { viewModel.requestPinShortcut(row.app) },
                                modifier = Modifier.semantics { contentDescription = pinDesc },
                            ) {
                                Text(stringResource(R.string.apps_pin))
                            }
                        }
                    }
                }
                if (uiState.pinned.isNotEmpty()) {
                    item(key = "pinned_header") { SectionLabel(stringResource(R.string.apps_section_pinned)) }
                    items(uiState.pinned, key = { "p_" + it.app.packageName }) { row ->
                        AppRowItem(row, look, onOpen = { onNavigateToPause(row.app.packageName) }) {
                            PinnedMark()
                        }
                    }
                }
                item(key = "footer") {
                    Column(Modifier.padding(horizontal = 16.dp, vertical = 20.dp)) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.apps_footer),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontStyle = if (look == Look.PAPIER) FontStyle.Italic else FontStyle.Normal,
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(8.dp))
                        TextButton(
                            onClick = { onTryPause(uiState.rows.first().app.packageName) },
                            contentPadding = PaddingValues(horizontal = 0.dp, vertical = 8.dp),
                        ) {
                            Text(stringResource(R.string.apps_try))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppRowItem(
    row: AppRow,
    look: Look,
    onOpen: () -> Unit,
    trailing: @Composable () -> Unit,
) {
    val openDesc = stringResource(R.string.home_open_via_pause, row.app.label)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clickable(role = Role.Button, onClickLabel = openDesc, onClick = onOpen)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        AppIcon(row.icon)
        Spacer(Modifier.width(16.dp))
        Text(
            text = row.app.label,
            style = labelStyle(look),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        trailing()
    }
}

@Composable
private fun AppIcon(icon: ImageBitmap?) {
    if (icon != null) {
        Image(bitmap = icon, contentDescription = null, modifier = Modifier.size(40.dp))
    } else {
        Box(
            Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        )
    }
}

@Composable
private fun PinnedMark() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = stringResource(R.string.apps_pinned),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp)
            .semantics { heading() },
    )
}

@Composable
private fun EmptyApps(onChoose: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.home_empty_hint),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onChoose) {
            Text(stringResource(R.string.home_empty_action))
        }
    }
}

@Composable
private fun labelStyle(look: Look): TextStyle =
    if (look == Look.PAPIER) {
        MaterialTheme.typography.titleMedium.copy(fontFamily = Spectral, fontWeight = FontWeight.Normal, fontSize = 18.sp)
    } else {
        MaterialTheme.typography.bodyLarge
    }
