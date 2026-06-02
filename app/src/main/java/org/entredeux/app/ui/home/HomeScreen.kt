package org.entredeux.app.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.entredeux.app.R
import org.entredeux.app.domain.model.SelectedApp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToSelection: () -> Unit,
    onNavigateToPause: (String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val successMsg = stringResource(R.string.home_shortcut_success)
    val unsupportedMsg = stringResource(R.string.home_shortcut_unsupported)
    LaunchedEffect(uiState.shortcutResult) {
        val result = uiState.shortcutResult ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(
            when (result) {
                ShortcutResult.SUCCESS -> successMsg
                ShortcutResult.UNSUPPORTED -> unsupportedMsg
            },
        )
        viewModel.clearShortcutResult()
    }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.app_name)) },
                    actions = {
                        val addDesc = stringResource(R.string.home_add_apps)
                        Box(contentAlignment = Alignment.Center) {
                            if (uiState.coachStep == CoachStep.ADD_APP) {
                                PulsingHalo(
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.matchParentSize(),
                                )
                            }
                            IconButton(
                                onClick = onNavigateToSelection,
                                modifier = Modifier.semantics { contentDescription = addDesc },
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null)
                            }
                        }
                    },
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { innerPadding ->
            if (uiState.selectedApps.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.home_empty_hint),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 100.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                ) {
                    itemsIndexed(
                        uiState.selectedApps,
                        key = { _, app -> app.packageName },
                    ) { index, app ->
                        AppTile(
                            app = app,
                            highlightPin = uiState.coachStep == CoachStep.PIN && index == 0,
                            onClick = { onNavigateToPause(app.packageName) },
                            onPinShortcut = { viewModel.requestPinShortcut(app) },
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = uiState.coachStep != CoachStep.NONE,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            CoachCard(step = uiState.coachStep, onDismiss = viewModel::dismissCoach)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun AppTile(
    app: SelectedApp,
    highlightPin: Boolean,
    onClick: () -> Unit,
    onPinShortcut: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val openDesc = stringResource(R.string.home_open_via_pause, app.label)
    val longPressLabel = stringResource(R.string.home_tile_options, app.label)
    val pinDesc = stringResource(R.string.home_pin_shortcut_cd, app.label)

    Box {
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 2.dp,
            modifier = Modifier
                .size(100.dp)
                .semantics(mergeDescendants = true) { contentDescription = openDesc }
                .combinedClickable(
                    role = Role.Button,
                    onClick = onClick,
                    onLongClick = { menuExpanded = true },
                    onLongClickLabel = longPressLabel,
                ),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(top = 40.dp, start = 8.dp, end = 8.dp, bottom = 8.dp),
            ) {
                Text(
                    text = app.label,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                )
            }
        }
        // The pin action is the app's killer feature, so it sits visibly on
        // every tile rather than hiding behind the long-press menu (kept below
        // as a secondary affordance). During the guided start it also pulses.
        Box(modifier = Modifier.align(Alignment.TopEnd), contentAlignment = Alignment.Center) {
            if (highlightPin) {
                PulsingHalo(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.matchParentSize(),
                )
            }
            IconButton(
                onClick = onPinShortcut,
                modifier = Modifier.semantics { contentDescription = pinDesc },
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_pin),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.home_pin_shortcut)) },
                onClick = {
                    menuExpanded = false
                    onPinShortcut()
                },
            )
        }
    }
}

@Composable
private fun CoachCard(step: CoachStep, onDismiss: () -> Unit) {
    val stepNumber = if (step == CoachStep.ADD_APP) 1 else 2
    val titleRes = if (step == CoachStep.ADD_APP) R.string.coach_add_title else R.string.coach_pin_title
    val bodyRes = if (step == CoachStep.ADD_APP) R.string.coach_add_body else R.string.coach_pin_body

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 3.dp,
        shadowElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.coach_step, stepNumber, 2),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(bodyRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                if (step == CoachStep.ADD_APP) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.coach_skip))
                    }
                } else {
                    Button(onClick = onDismiss) {
                        Text(stringResource(R.string.coach_done))
                    }
                }
            }
        }
    }
}

// A soft pinging halo placed behind the control the guide is pointing at.
@Composable
private fun PulsingHalo(color: Color, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "halo")
    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "pulse",
    )
    Canvas(modifier) {
        val maxR = size.minDimension / 2f
        drawCircle(color = color.copy(alpha = 0.22f), radius = maxR * 0.92f, center = center)
        drawCircle(
            color = color.copy(alpha = (1f - pulse) * 0.5f),
            radius = maxR * (0.85f + 0.85f * pulse),
            center = center,
            style = Stroke(width = 2.dp.toPx()),
        )
    }
}
