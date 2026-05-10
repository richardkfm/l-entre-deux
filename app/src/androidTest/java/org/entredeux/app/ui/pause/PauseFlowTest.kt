package org.entredeux.app.ui.pause

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.entredeux.app.data.apps.InstalledAppsRepository
import org.entredeux.app.data.local.AppDatabase
import org.entredeux.app.data.local.BudgetNotificationScheduler
import org.entredeux.app.data.local.PauseEventRepository
import org.entredeux.app.data.prefs.AppSelectionRepository
import org.entredeux.app.ui.theme.EntreDeuxTheme
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PauseFlowTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val installedAppsRepository = InstalledAppsRepository(context)
    private val appSelectionRepository = AppSelectionRepository(context)
    private val budgetScheduler = BudgetNotificationScheduler(context)
    private val testScope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)

    private val testPackage = context.packageName

    private lateinit var db: AppDatabase
    private lateinit var pauseEventRepository: PauseEventRepository

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        pauseEventRepository = PauseEventRepository(db.pauseEventDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun newViewModel() = PauseViewModel(
        installedAppsRepository = installedAppsRepository,
        pauseEventRepository = pauseEventRepository,
        budgetScheduler = budgetScheduler,
        appSelectionRepository = appSelectionRepository,
        appScope = testScope,
        packageName = testPackage,
    )

    @Test
    fun proceedButton_disabledUntilIntentionSelected() {
        val viewModel = newViewModel()

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
        val viewModel = newViewModel()
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
        val viewModel = newViewModel()
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
