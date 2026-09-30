package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BabyDao {
    // Baby Profile queries
    @Query("SELECT * FROM baby_profile ORDER BY id ASC")
    fun getAllBabyProfilesFlow(): Flow<List<BabyProfile>>

    @Query("SELECT * FROM baby_profile WHERE id = :id LIMIT 1")
    fun getBabyProfileFlow(id: Int): Flow<BabyProfile?>

    @Query("SELECT * FROM baby_profile WHERE id = :id LIMIT 1")
    suspend fun getBabyProfileById(id: Int): BabyProfile?

    @Query("SELECT * FROM baby_profile ORDER BY id ASC")
    suspend fun getAllBabyProfiles(): List<BabyProfile>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBabyProfile(babyProfile: BabyProfile): Long

    @Update
    suspend fun updateBabyProfile(babyProfile: BabyProfile)

    @Query("DELETE FROM baby_profile WHERE id = :id")
    suspend fun deleteBabyProfileById(id: Int)

    // Clear queries
    @Query("DELETE FROM baby_profile")
    suspend fun clearBabyProfile()

    @Query("DELETE FROM activity_record")
    suspend fun clearActivities()

    @Query("DELETE FROM health_record")
    suspend fun clearHealthRecords()

    @Query("DELETE FROM moment_record")
    suspend fun clearMoments()

    @Query("DELETE FROM activity_record WHERE babyId = :babyId")
    suspend fun deleteActivitiesForBaby(babyId: Int)

    @Query("DELETE FROM health_record WHERE babyId = :babyId")
    suspend fun deleteHealthRecordsForBaby(babyId: Int)

    @Query("DELETE FROM moment_record WHERE babyId = :babyId")
    suspend fun deleteMomentsForBaby(babyId: Int)

    // Activity Record queries
    @Query("SELECT * FROM activity_record WHERE babyId = :babyId ORDER BY timestamp DESC")
    fun getAllActivitiesFlow(babyId: Int): Flow<List<ActivityRecord>>

    @Query("SELECT * FROM activity_record WHERE babyId = :babyId ORDER BY timestamp DESC")
    suspend fun getAllActivities(babyId: Int): List<ActivityRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: ActivityRecord)

    @Query("DELETE FROM activity_record WHERE id = :id")
    suspend fun deleteActivity(id: Int)

    // Health Record queries
    @Query("SELECT * FROM health_record WHERE babyId = :babyId ORDER BY timestamp DESC")
    fun getAllHealthRecordsFlow(babyId: Int): Flow<List<HealthRecord>>

    @Query("SELECT * FROM health_record WHERE babyId = :babyId ORDER BY timestamp DESC")
    suspend fun getAllHealthRecords(babyId: Int): List<HealthRecord>

    @Query("SELECT * FROM health_record WHERE babyId = :babyId AND type = :type AND timestamp >= :startOfDay AND timestamp <= :endOfDay LIMIT 1")
    suspend fun getHealthRecordForDay(babyId: Int, type: String, startOfDay: Long, endOfDay: Long): HealthRecord?

    @Query("DELETE FROM health_record WHERE babyId = :babyId AND type = :type AND timestamp >= :startOfDay AND timestamp <= :endOfDay")
    suspend fun deleteHealthRecordsForDay(babyId: Int, type: String, startOfDay: Long, endOfDay: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthRecord(healthRecord: HealthRecord)

    @Query("DELETE FROM health_record WHERE id = :id")
    suspend fun deleteHealthRecord(id: Int)

    // Moment Record queries
    @Query("SELECT * FROM moment_record WHERE babyId = :babyId ORDER BY isCustom ASC, id ASC")
    fun getAllMomentsFlow(babyId: Int): Flow<List<MomentRecord>>

    @Query("SELECT * FROM moment_record WHERE babyId = :babyId ORDER BY isCustom ASC, id ASC")
    suspend fun getAllMoments(babyId: Int): List<MomentRecord>

    @Query("SELECT * FROM moment_record WHERE babyId = :babyId AND isCompleted = 1 AND isCustom = 0")
    fun getCompletedSpecialMomentsFlow(babyId: Int): Flow<List<MomentRecord>>

    @Query("SELECT COUNT(*) FROM moment_record WHERE babyId = :babyId")
    suspend fun getMomentsCount(babyId: Int): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoment(moment: MomentRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoments(moments: List<MomentRecord>)

    @Update
    suspend fun updateMoment(moment: MomentRecord)

    @Query("SELECT * FROM moment_record WHERE id = :id LIMIT 1")
    suspend fun getMomentById(id: Int): MomentRecord?

    @Query("DELETE FROM moment_record WHERE id = :id")
    suspend fun deleteMomentById(id: Int)
}
