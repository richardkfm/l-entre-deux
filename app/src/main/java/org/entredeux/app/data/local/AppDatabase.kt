package org.entredeux.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [PauseEventEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun pauseEventDao(): PauseEventDao

    companion object {
        // Drops the obsolete budgetMinutes column (time-limit feature removed in
        // 0.7.0). SQLite can't reliably DROP COLUMN on the Android versions we
        // support, so recreate the table and copy the rows we keep. Preserves
        // every recorded pause so the reflection history survives the upgrade.
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `pause_events_new` " +
                        "(`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`timestamp` INTEGER NOT NULL, " +
                        "`packageName` TEXT NOT NULL, " +
                        "`intentionKey` TEXT NOT NULL, " +
                        "`outcome` TEXT NOT NULL)",
                )
                db.execSQL(
                    "INSERT INTO `pause_events_new` " +
                        "(`id`, `timestamp`, `packageName`, `intentionKey`, `outcome`) " +
                        "SELECT `id`, `timestamp`, `packageName`, `intentionKey`, `outcome` " +
                        "FROM `pause_events`",
                )
                db.execSQL("DROP TABLE `pause_events`")
                db.execSQL("ALTER TABLE `pause_events_new` RENAME TO `pause_events`")
            }
        }

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "entredeux.db",
                ).addMigrations(MIGRATION_1_2).build().also { instance = it }
            }
    }
}
