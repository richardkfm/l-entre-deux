package org.entredeux.app.data.local

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BudgetAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: return
        val budgetMinutes = intent.getIntExtra(EXTRA_BUDGET_MINUTES, 0)
        NotificationHelper.postBudgetReminder(context, packageName, budgetMinutes)
    }

    companion object {
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
        const val EXTRA_BUDGET_MINUTES = "extra_budget_minutes"
    }
}
