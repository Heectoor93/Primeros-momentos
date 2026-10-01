package com.example.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirestoreRepository {

    private val db = FirebaseFirestore.getInstance()

    // ── Upload all local data to Firestore ──────────────────────────────────
    suspend fun syncToCloud(userId: String, repository: BabyRepository) {
        try {
            val babies = repository.getAllBabyProfilesSync()
            for (baby in babies) {
                val babyRef = db.collection("users").document(userId)
                    .collection("babies").document(baby.id.toString())

                babyRef.set(mapOf(
                    "id" to baby.id,
                    "name" to baby.name,
                    "dob" to baby.dob,
                    "gender" to baby.gender,
                    "skinTone" to baby.skinTone,
                    "hairColor" to baby.hairColor
                ), SetOptions.merge()).await()

                // Activities
                val activities = repository.getAllActivitiesSync(baby.id)
                for (a in activities) {
                    babyRef.collection("activities").document(a.id.toString()).set(mapOf(
                        "id" to a.id, "babyId" to a.babyId, "type" to a.type,
                        "timestamp" to a.timestamp, "startTime" to a.startTime,
                        "endTime" to (a.endTime ?: ""), "durationMinutes" to (a.durationMinutes ?: 0),
                        "extraType" to (a.extraType ?: ""), "notes" to a.notes
                    ), SetOptions.merge()).await()
                }

                // Health Records
                val healthRecords = repository.getAllHealthRecordsSync(baby.id)
                for (h in healthRecords) {
                    babyRef.collection("healthRecords").document(h.id.toString()).set(mapOf(
                        "id" to h.id, "babyId" to h.babyId, "type" to h.type,
                        "timestamp" to h.timestamp, "value" to h.value,
                        "title" to h.title, "notes" to h.notes,
                        "hasReminder" to h.hasReminder, "status" to (h.status ?: ""),
                        "dateString" to (h.dateString ?: "")
                    ), SetOptions.merge()).await()
                }

                // Moments
                val moments = repository.getAllMomentsSync(baby.id)
                for (m in moments) {
                    babyRef.collection("moments").document(m.id.toString()).set(mapOf(
                        "id" to m.id, "babyId" to m.babyId, "name" to m.name,
                        "ageRange" to m.ageRange, "dateHappened" to (m.dateHappened ?: ""),
                        "location" to (m.location ?: ""), "details" to (m.details ?: ""),
                        "isCustom" to m.isCustom, "photoPath" to (m.photoPath ?: ""),
                        "isCompleted" to m.isCompleted
                    ), SetOptions.merge()).await()
                }
            }
            Log.i("FirestoreRepository", "Sync to cloud complete for user $userId")
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "syncToCloud error", e)
            throw e
        }
    }

    // ── Download data from Firestore and restore to Room ───────────────────
    suspend fun restoreFromCloud(userId: String, repository: BabyRepository) {
        try {
            val babiesSnapshot = db.collection("users").document(userId)
                .collection("babies").get().await()

            if (babiesSnapshot.isEmpty) {
                Log.i("FirestoreRepository", "No data in cloud for user $userId")
                return
            }

            repository.clearDatabase()

            for (babyDoc in babiesSnapshot.documents) {
                val babyData = babyDoc.data ?: continue
                val baby = BabyProfile(
                    id = (babyData["id"] as? Long)?.toInt() ?: 0,
                    name = babyData["name"] as? String ?: "",
                    dob = babyData["dob"] as? Long ?: 0L,
                    gender = babyData["gender"] as? String ?: "",
                    skinTone = babyData["skinTone"] as? String ?: "Claro",
                    hairColor = babyData["hairColor"] as? String ?: "Castaño"
                )
                val savedId = repository.saveBabyProfile(baby)
                val babyId = savedId.toInt()

                // Restore Activities
                val activitiesSnap = babyDoc.reference.collection("activities").get().await()
                for (aDoc in activitiesSnap.documents) {
                    val d = aDoc.data ?: continue
                    val activity = ActivityRecord(
                        id = (d["id"] as? Long)?.toInt() ?: 0,
                        babyId = babyId,
                        type = d["type"] as? String ?: "",
                        timestamp = d["timestamp"] as? Long ?: 0L,
                        startTime = d["startTime"] as? String ?: "",
                        endTime = (d["endTime"] as? String)?.ifBlank { null },
                        durationMinutes = (d["durationMinutes"] as? Long)?.toInt()?.takeIf { it > 0 },
                        extraType = (d["extraType"] as? String)?.ifBlank { null },
                        notes = d["notes"] as? String ?: ""
                    )
                    repository.insertActivity(activity)
                }

                // Restore Health Records
                val healthSnap = babyDoc.reference.collection("healthRecords").get().await()
                for (hDoc in healthSnap.documents) {
                    val d = hDoc.data ?: continue
                    val health = HealthRecord(
                        id = (d["id"] as? Long)?.toInt() ?: 0,
                        babyId = babyId,
                        type = d["type"] as? String ?: "",
                        timestamp = d["timestamp"] as? Long ?: 0L,
                        value = d["value"] as? Double ?: 0.0,
                        title = d["title"] as? String ?: "",
                        notes = d["notes"] as? String ?: "",
                        hasReminder = d["hasReminder"] as? Boolean ?: false,
                        status = (d["status"] as? String)?.ifBlank { null },
                        dateString = (d["dateString"] as? String)?.ifBlank { null }
                    )
                    repository.insertHealthRecord(health)
                }

                // Restore Moments
                val momentsSnap = babyDoc.reference.collection("moments").get().await()
                for (mDoc in momentsSnap.documents) {
                    val d = mDoc.data ?: continue
                    val moment = MomentRecord(
                        id = (d["id"] as? Long)?.toInt() ?: 0,
                        babyId = babyId,
                        name = d["name"] as? String ?: "",
                        ageRange = d["ageRange"] as? String ?: "",
                        dateHappened = (d["dateHappened"] as? String)?.ifBlank { null },
                        location = (d["location"] as? String)?.ifBlank { null },
                        details = (d["details"] as? String)?.ifBlank { null },
                        isCustom = d["isCustom"] as? Boolean ?: false,
                        photoPath = (d["photoPath"] as? String)?.ifBlank { null },
                        isCompleted = d["isCompleted"] as? Boolean ?: false
                    )
                    repository.insertMoment(moment)
                }
            }
            Log.i("FirestoreRepository", "Restore from cloud complete for user $userId")
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "restoreFromCloud error", e)
            throw e
        }
    }

    suspend fun deleteBabyFromCloud(userId: String, babyId: Int) {
        try {
            val babyRef = db.collection("users").document(userId)
                .collection("babies").document(babyId.toString())

            // Delete subcollections (activities, healthRecords, moments)
            val activities = babyRef.collection("activities").get().await()
            for (doc in activities.documents) { doc.reference.delete().await() }

            val health = babyRef.collection("healthRecords").get().await()
            for (doc in health.documents) { doc.reference.delete().await() }

            val moments = babyRef.collection("moments").get().await()
            for (doc in moments.documents) { doc.reference.delete().await() }

            // Delete baby document
            babyRef.delete().await()
            Log.i("FirestoreRepository", "Deleted baby $babyId from cloud for user $userId")
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "deleteBabyFromCloud error", e)
        }
    }

    suspend fun deleteUserData(userId: String) {
        try {
            val babiesSnapshot = db.collection("users").document(userId)
                .collection("babies").get().await()
            for (babyDoc in babiesSnapshot.documents) {
                babyDoc.reference.delete().await()
            }
            db.collection("users").document(userId).delete().await()
        } catch (e: Exception) {
            Log.e("FirestoreRepository", "deleteUserData error", e)
        }
    }
}
