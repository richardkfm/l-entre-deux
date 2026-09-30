package org.entredeux.app.ui.selection

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.entredeux.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSelectionScreen(
    viewModel: AppSelectionViewModel,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.selection_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back),
                        )
                    }
                },
                actions = {
                    // "Done" lives in the top bar so it stays reachable while
                    // the keyboard is open for the search field. Selections
                    // persist as they're toggled, so it's simply a clear way
                    // forward — the user shouldn't have to reach for Back.
                    TextButton(onClick = onBack) {
                        Text(stringResource(R.string.selection_done))
                    }
                },
            )
        },
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding(),
        ) {
            item {
                OutlinedTextField(
                    value = uiState.query,
                    onValueChange = viewModel::onQueryChange,
                    label = { Text(stringResource(R.string.selection_search_hint)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }

            val searching = uiState.query.isNotBlank()
            val nothing = if (searching) {
                uiState.results.isEmpty()
            } else {
                uiState.chosen.isEmpty() && uiState.often.isEmpty() && uiState.others.isEmpty()
            }
            if (nothing) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.selection_empty),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            } else if (searching) {
                appRows("results", uiState.results, viewModel)
            } else {
                appSection("chosen", R.string.selection_section_chosen, uiState.chosen, viewModel)
                appSection("often", R.string.selection_section_often, uiState.often, viewModel)
                appSection("others", R.string.selection_section_all, uiState.others, viewModel)
            }
        }
    }
}

private fun LazyListScope.appSection(
    key: String,
    titleRes: Int,
    rows: List<SelectableApp>,
    viewModel: AppSelectionViewModel,
) {
    if (rows.isEmpty()) return
    item(key = "header_$key") {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp)
                .semantics { heading() },
        )
    }
    appRows(key, rows, viewModel)
}

private fun LazyListScope.appRows(key: String, rows: List<SelectableApp>, viewModel: AppSelectionViewModel) {
    items(rows, key = { "${key}_${it.app.packageName}" }) { selectable ->
        AppRow(
            selectable = selectable,
            loadIcon = viewModel::icon,
            onToggle = { viewModel.onToggle(selectable.app.packageName) },
        )
    }
}

@Composable
private fun AppRow(
    selectable: SelectableApp,
    loadIcon: suspend (String) -> ImageBitmap?,
    onToggle: () -> Unit,
) {
    val pkg = selectable.app.packageName
    var icon by remember(pkg) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(pkg) { icon = loadIcon(pkg) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .toggleable(
                value = selectable.isSelected,
                role = Role.Checkbox,
                onValueChange = { onToggle() },
            )
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(36.dp)) {
            icon?.let { Image(bitmap = it, contentDescription = null, modifier = Modifier.size(36.dp)) }
        }
        Spacer(Modifier.width(16.dp))
        Text(
            text = selectable.app.label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Checkbox(
            checked = selectable.isSelected,
            onCheckedChange = null,
        )
    }
}
