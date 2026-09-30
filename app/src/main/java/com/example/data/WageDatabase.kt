package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
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
}

@Database(
    entities = [WorkShiftEntity::class, PaySettingsEntity::class],
    version = 1,
    exportSchema = false
)
abstract class WageDatabase : RoomDatabase() {
    abstract fun wageDao(): WageDao

    companion object {
        @Volatile
        private var INSTANCE: WageDatabase? = null

        fun getDatabase(context: Context): WageDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WageDatabase::class.java,
                    "wageflow_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class WageRepository(private val wageDao: WageDao) {
    val allShifts: Flow<List<WorkShiftEntity>> = wageDao.getAllShifts()
    val paySettings: Flow<PaySettingsEntity?> = wageDao.getPaySettings()

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
}
