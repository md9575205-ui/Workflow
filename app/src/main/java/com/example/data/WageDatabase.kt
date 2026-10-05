package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface WageDao {
    @Query("SELECT * FROM work_shifts ORDER BY dateIso ASC")
    fun getAllShifts(): Flow<List<WorkShiftEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertShift(shift: WorkShiftEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertShifts(shifts: List<WorkShiftEntity>)

    @Query("DELETE FROM work_shifts WHERE dateIso = :dateIso")
    suspend fun deleteShiftByDate(dateIso: String)

    @Query("DELETE FROM work_shifts WHERE dateIso IN (:dateIsos)")
    suspend fun deleteShiftsByDates(dateIsos: List<String>)

    @Query("SELECT * FROM pay_settings WHERE id = 1 LIMIT 1")
    fun getPaySettings(): Flow<PaySettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePaySettings(settings: PaySettingsEntity)

    @Query("SELECT * FROM advances_deductions ORDER BY dateIso ASC")
    fun getAllAdvancesDeductions(): Flow<List<AdvanceDeductionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAdvanceDeduction(item: AdvanceDeductionEntity)

    @Query("DELETE FROM advances_deductions WHERE id = :id")
    suspend fun deleteAdvanceDeduction(id: Long)

    @Query("DELETE FROM advances_deductions WHERE dateIso IN (:dateIsos)")
    suspend fun deleteAdvancesDeductionsByDates(dateIsos: List<String>)
}

@Database(
    entities = [WorkShiftEntity::class, PaySettingsEntity::class, AdvanceDeductionEntity::class],
    version = 3,
    exportSchema = false
)
abstract class WageDatabase : RoomDatabase() {
    abstract fun wageDao(): WageDao

    companion object {
        @Volatile
        private var INSTANCE: WageDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE pay_settings ADD COLUMN themeMode TEXT NOT NULL DEFAULT 'SYSTEM'")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `advances_deductions` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `dateIso` TEXT NOT NULL,
                        `amountRs` REAL NOT NULL,
                        `isDeduction` INTEGER NOT NULL,
                        `note` TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("ALTER TABLE pay_settings ADD COLUMN monthlyTargetGoalRs REAL NOT NULL DEFAULT 10000.0")
            }
        }

        fun getDatabase(context: Context): WageDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WageDatabase::class.java,
                    "wageflow_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class WageRepository(private val wageDao: WageDao) {
    val allShifts: Flow<List<WorkShiftEntity>> = wageDao.getAllShifts()
    val paySettings: Flow<PaySettingsEntity?> = wageDao.getPaySettings()
    val allAdvancesDeductions: Flow<List<AdvanceDeductionEntity>> = wageDao.getAllAdvancesDeductions()

    suspend fun upsertShift(shift: WorkShiftEntity) {
        wageDao.upsertShift(shift)
    }

    suspend fun upsertShifts(shifts: List<WorkShiftEntity>) {
        wageDao.upsertShifts(shifts)
    }

    suspend fun deleteShift(dateIso: String) {
        wageDao.deleteShiftByDate(dateIso)
    }

    suspend fun deleteShifts(dateIsos: List<String>) {
        wageDao.deleteShiftsByDates(dateIsos)
    }

    suspend fun savePaySettings(settings: PaySettingsEntity) {
        wageDao.savePaySettings(settings)
    }

    suspend fun upsertAdvanceDeduction(item: AdvanceDeductionEntity) {
        wageDao.upsertAdvanceDeduction(item)
    }

    suspend fun deleteAdvanceDeduction(id: Long) {
        wageDao.deleteAdvanceDeduction(id)
    }

    suspend fun deleteAdvancesDeductions(dateIsos: List<String>) {
        wageDao.deleteAdvancesDeductionsByDates(dateIsos)
    }
}
