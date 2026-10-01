package com.example.data

import kotlinx.coroutines.flow.Flow

class BabyRepository(private val babyDao: BabyDao) {

    val allBabyProfiles: Flow<List<BabyProfile>> = babyDao.getAllBabyProfilesFlow()

    fun getBabyProfileFlow(id: Int): Flow<BabyProfile?> = babyDao.getBabyProfileFlow(id)

    fun getAllActivitiesFlow(babyId: Int): Flow<List<ActivityRecord>> = babyDao.getAllActivitiesFlow(babyId)

    fun getAllHealthRecordsFlow(babyId: Int): Flow<List<HealthRecord>> = babyDao.getAllHealthRecordsFlow(babyId)

    fun getAllMomentsFlow(babyId: Int): Flow<List<MomentRecord>> = babyDao.getAllMomentsFlow(babyId)

    fun getCompletedSpecialMomentsFlow(babyId: Int): Flow<List<MomentRecord>> = babyDao.getCompletedSpecialMomentsFlow(babyId)

    suspend fun getAllBabyProfilesSync(): List<BabyProfile> {
        return babyDao.getAllBabyProfiles()
    }

    suspend fun getBabyProfileByIdSync(id: Int): BabyProfile? {
        return babyDao.getBabyProfileById(id)
    }

    suspend fun saveBabyProfile(profile: BabyProfile): Long {
        val existing = if (profile.id != 0) babyDao.getBabyProfileById(profile.id) else null
        return if (existing == null) {
            babyDao.insertBabyProfile(profile)
        } else {
            babyDao.updateBabyProfile(profile)
            profile.id.toLong()
        }
    }

    suspend fun deleteBabyProfile(id: Int) {
        babyDao.deleteBabyProfileById(id)
        babyDao.deleteActivitiesForBaby(id)
        babyDao.deleteHealthRecordsForBaby(id)
        babyDao.deleteMomentsForBaby(id)
    }

    suspend fun clearDatabase() {
        babyDao.clearBabyProfile()
        babyDao.clearActivities()
        babyDao.clearHealthRecords()
        babyDao.clearMoments()
    }

    suspend fun insertActivity(activity: ActivityRecord) {
        babyDao.insertActivity(activity)
    }

    suspend fun getAllActivitiesSync(babyId: Int): List<ActivityRecord> {
        return babyDao.getAllActivities(babyId)
    }

    suspend fun getAllHealthRecordsSync(babyId: Int): List<HealthRecord> {
        return babyDao.getAllHealthRecords(babyId)
    }

    suspend fun getAllMomentsSync(babyId: Int): List<MomentRecord> {
        return babyDao.getAllMoments(babyId)
    }

    suspend fun deleteActivity(id: Int) {
        babyDao.deleteActivity(id)
    }

    suspend fun insertHealthRecord(record: HealthRecord) {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = record.timestamp
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
        calendar.set(java.util.Calendar.MINUTE, 59)
        calendar.set(java.util.Calendar.SECOND, 59)
        calendar.set(java.util.Calendar.MILLISECOND, 999)
        val endOfDay = calendar.timeInMillis

        babyDao.deleteHealthRecordsForDay(record.babyId, record.type, startOfDay, endOfDay)
        babyDao.insertHealthRecord(record)
    }

    suspend fun updateHealthRecord(record: HealthRecord) {
        babyDao.insertHealthRecord(record)
    }

    suspend fun deleteHealthRecord(id: Int) {
        babyDao.deleteHealthRecord(id)
    }

    suspend fun insertMoment(moment: MomentRecord): Long {
        return babyDao.insertMoment(moment)
    }

    suspend fun updateMoment(moment: MomentRecord) {
        babyDao.updateMoment(moment)
    }

    suspend fun getMomentByIdSync(id: Int): MomentRecord? {
        return babyDao.getMomentById(id)
    }

    suspend fun deleteMomentById(id: Int) {
        babyDao.deleteMomentById(id)
    }

    suspend fun prepopulateDefaultMomentsIfEmpty(babyId: Int) {
        val count = babyDao.getMomentsCount(babyId)
        if (count == 0) {
            val list = mutableListOf<MomentRecord>()
            
            // 0-6 meses (10 moments, all uncompleted)
            list.add(MomentRecord(babyId = babyId, name = "Mi primer baño", ageRange = "0-6 meses", isCompleted = false))
            list.add(MomentRecord(babyId = babyId, name = "Primera noche en casa", ageRange = "0-6 meses", isCompleted = false))
            list.add(MomentRecord(babyId = babyId, name = "Primera sonrisa social", ageRange = "0-6 meses", isCompleted = false))
            list.add(MomentRecord(babyId = babyId, name = "Sostener la cabeza", ageRange = "0-6 meses", isCompleted = false))
            list.add(MomentRecord(babyId = babyId, name = "Primer alimento sólido", ageRange = "0-6 meses", isCompleted = false))
            list.add(MomentRecord(babyId = babyId, name = "Dormir 6 horas seguidas", ageRange = "0-6 meses", isCompleted = false))
            list.add(MomentRecord(babyId = babyId, name = "Agarrar un juguete", ageRange = "0-6 meses", isCompleted = false))
            list.add(MomentRecord(babyId = babyId, name = "Balbucear sonidos lindos", ageRange = "0-6 meses", isCompleted = false))
            list.add(MomentRecord(babyId = babyId, name = "Darse la vuelta solo", ageRange = "0-6 meses", isCompleted = false))
            list.add(MomentRecord(babyId = babyId, name = "Reconocer voces familiares", ageRange = "0-6 meses", isCompleted = false))

            // 6-12 meses (8 moments, 0 completed)
            list.add(MomentRecord(babyId = babyId, name = "Gateo por primera vez", ageRange = "6-12 meses", isCompleted = false))
            list.add(MomentRecord(babyId = babyId, name = "Primer diente", ageRange = "6-12 meses", isCompleted = false))
            list.add(MomentRecord(babyId = babyId, name = "Primera palabra", ageRange = "6-12 meses", isCompleted = false))
            list.add(MomentRecord(babyId = babyId, name = "Sentarse sin apoyo", ageRange = "6-12 meses", isCompleted = false))
            list.add(MomentRecord(babyId = babyId, name = "Ponerse de pie sosteniéndose", ageRange = "6-12 meses", isCompleted = false))
            list.add(MomentRecord(babyId = babyId, name = "Responder a su nombre", ageRange = "6-12 meses", isCompleted = false))
            list.add(MomentRecord(babyId = babyId, name = "Beber de un vaso entrenador", ageRange = "6-12 meses", isCompleted = false))
            list.add(MomentRecord(babyId = babyId, name = "Señalar objetos interesantes", ageRange = "6-12 meses", isCompleted = false))

            // 12-18 meses (6 moments, 0 completed)
            list.add(MomentRecord(babyId = babyId, name = "Primeros pasos solos", ageRange = "12-18 meses", isCompleted = false))
            list.add(MomentRecord(babyId = babyId, name = "Beber de un vaso común", ageRange = "12-18 meses", isCompleted = false))
            list.add(MomentRecord(babyId = babyId, name = "Señalar para pedir algo", ageRange = "12-18 meses", isCompleted = false))
            list.add(MomentRecord(babyId = babyId, name = "Decir de 3 a 5 palabras", ageRange = "12-18 meses", isCompleted = false))
            list.add(MomentRecord(babyId = babyId, name = "Imitar sonidos de animales", ageRange = "12-18 meses", isCompleted = false))
            list.add(MomentRecord(babyId = babyId, name = "Garabatear con crayones", ageRange = "12-18 meses", isCompleted = false))

            // 18-24 meses (6 moments, 0 completed)
            list.add(MomentRecord(babyId = babyId, name = "Correr con seguridad", ageRange = "18-24 meses", isCompleted = false))
            list.add(MomentRecord(babyId = babyId, name = "Construir una torre de bloques", ageRange = "18-24 meses", isCompleted = false))
            list.add(MomentRecord(babyId = babyId, name = "Decir frases de dos palabras", ageRange = "18-24 meses", isCompleted = false))
            list.add(MomentRecord(babyId = babyId, name = "Patear una pelota", ageRange = "18-24 meses", isCompleted = false))
            list.add(MomentRecord(babyId = babyId, name = "Subir escaleras con ayuda", ageRange = "18-24 meses", isCompleted = false))
            list.add(MomentRecord(babyId = babyId, name = "Seguir instrucciones sencillas", ageRange = "18-24 meses", isCompleted = false))

            babyDao.insertMoments(list)
        }
    }
}
