package org.entredeux.app.data.local

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

class BudgetNotificationScheduler(private val context: Context) {

    fun schedule(packageName: String, budgetMinutes: Int) {
        val triggerAt = System.currentTimeMillis() + budgetMinutes * 60_000L

        val intent = Intent(context, BudgetAlarmReceiver::class.java).apply {
            putExtra(BudgetAlarmReceiver.EXTRA_PACKAGE_NAME, packageName)
            putExtra(BudgetAlarmReceiver.EXTRA_BUDGET_MINUTES, budgetMinutes)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            packageName.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        // setAndAllowWhileIdle fires even in Doze mode with no exact-alarm permission.
        // For a soft reminder that fires within a few minutes, this is adequate.
        context.getSystemService(AlarmManager::class.java)
            .setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
    }
}
