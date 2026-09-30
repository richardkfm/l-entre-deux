package org.entredeux.app.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.entredeux.app.R
import org.entredeux.app.domain.model.Look
import org.entredeux.app.ui.theme.LocalLook
import org.entredeux.app.ui.theme.Spectral

// One screen, set like a dictionary entry: the name, what it means here,
// the promise, and a single way forward into choosing apps.
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onDone: () -> Unit,
) {
    val type = MaterialTheme.typography
    val colors = MaterialTheme.colorScheme
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 24.dp),
        ) {
            Text(
                text = stringResource(R.string.app_name).lowercase(),
                style = type.labelLarge.copy(letterSpacing = 1.5.sp),
                color = colors.onSurfaceVariant,
            )
            Spacer(Modifier.weight(1f).heightIn(min = 48.dp))
            Text(
                text = stringResource(R.string.onboarding_word),
                style = type.displayMedium.copy(fontWeight = FontWeight.Light),
                color = colors.onSurface,
                modifier = Modifier.semantics { heading() },
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.onboarding_grammar),
                style = type.titleMedium.copy(
                    fontStyle = FontStyle.Italic,
                    fontFamily = if (LocalLook.current == Look.PAPIER) Spectral else null,
                ),
                color = colors.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.onboarding_definition_1), style = type.bodyLarge, color = colors.onSurface)
                Text(stringResource(R.string.onboarding_definition_2), style = type.bodyLarge, color = colors.onSurface)
            }
            Spacer(Modifier.weight(1f).heightIn(min = 48.dp))
            Text(
                text = stringResource(R.string.onboarding_promise),
                style = type.bodyMedium,
                color = colors.onSurfaceVariant,
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    viewModel.completeOnboarding()
                    onDone()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
            ) {
                Text(stringResource(R.string.onboarding_cta))
            }
        }
    }
}
