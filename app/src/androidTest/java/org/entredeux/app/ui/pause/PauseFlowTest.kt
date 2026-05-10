package org.entredeux.app.ui.pause

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import org.entredeux.app.data.apps.InstalledAppsRepository
import org.entredeux.app.ui.theme.EntreDeuxTheme
import org.junit.Rule
import org.junit.Test

class PauseFlowTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val repository = InstalledAppsRepository(context)

    // Uses our own package so the label resolves; pause flow works the same regardless.
    private val testPackage = context.packageName

    @Test
    fun proceedButton_disabledUntilIntentionSelected() {
        val viewModel = PauseViewModel(repository, testPackage)

        composeRule.setContent {
            EntreDeuxTheme {
                PauseScreen(
                    viewModel = viewModel,
                    onProceed = {},
                    onBackOut = {},
                )
            }
        }

        composeRule.onNodeWithText("Open the app").assertIsNotEnabled()

        composeRule.onNodeWithText("I need this for one specific task").performClick()

        composeRule.onNodeWithText("Open the app").assertIsEnabled()
    }

    @Test
    fun backOut_callsCallback() {
        val viewModel = PauseViewModel(repository, testPackage)
        var backedOut = false

        composeRule.setContent {
            EntreDeuxTheme {
                PauseScreen(
                    viewModel = viewModel,
                    onProceed = {},
                    onBackOut = { backedOut = true },
                )
            }
        }

        composeRule.onNodeWithText("Not now").performClick()

        assert(backedOut) { "onBackOut was not called" }
    }

    @Test
    fun happyPath_selectIntentionAndProceed() {
        val viewModel = PauseViewModel(repository, testPackage)
        var proceeded = false

        composeRule.setContent {
            EntreDeuxTheme {
                PauseScreen(
                    viewModel = viewModel,
                    onProceed = { proceeded = true },
                    onBackOut = {},
                )
            }
        }

        composeRule.onNodeWithText("I am checking something briefly").performClick()
        composeRule.onNodeWithText("Open the app").performClick()

        assert(proceeded) { "onProceed was not called" }
    }
}
