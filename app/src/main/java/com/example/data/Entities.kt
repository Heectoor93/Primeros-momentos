package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "baby_profile")
data class BabyProfile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val dob: Long, // Date of birth as timestamp
    val gender: String, // "Niño" or "Niña"
    val skinTone: String = "Claro", // "Claro", "Moreno", "Oscuro"
    val hairColor: String = "Castaño", // "Castaño", "Rubio", "Pelirrojo", "Sin pelo"
    val preferredPacifierColor: String? = null // User selected color: "Blanco", "Azul", "Verde", "Púrpura", "Rojo", "Dorado"
)

@Entity(tableName = "activity_record")
data class ActivityRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val babyId: Int,
    val type: String, // "comida", "sueno", "panal"
    val timestamp: Long, // date/time of record
    val startTime: String, // e.g., "08:30"
    val endTime: String? = null, // e.g., "11:00"
    val durationMinutes: Int? = null, // dynamic calculations
    val extraType: String? = null, // meal name, diaper condition ("Seco", "Mojado", etc.)
    val notes: String
)

@Entity(tableName = "health_record")
data class HealthRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val babyId: Int,
    val type: String, // "peso", "altura", "vacuna", "cita"
    val timestamp: Long,
    val value: Double, // e.g. weight (kg), height (cm)
    val title: String, // e.g., vaccine name, appointment title
    val notes: String,
    val hasReminder: Boolean = false,
    val status: String? = null, // "Falta por vacunar", "Pendiente más tomas", "Vacunado completamente"
    val dateString: String? = null // optional formatted date, e.g., "05/09/2026"
)

@Entity(tableName = "moment_record")
data class MomentRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val babyId: Int,
    val name: String,
    val ageRange: String, // "0-6 meses", "6-12 meses", "12-18 meses", "18-24 meses"
    val dateHappened: String? = null, // e.g., "15/05/2024"
    val location: String? = null,
    val details: String? = null,
    val isCustom: Boolean = false, // True if custom moments (doesn't count for achievements)
    val photoPath: String? = null, // local photo cache path
    val driveFileId: String? = null, // Google Drive file ID for cloud deletion
    val isCompleted: Boolean = false
)

data class AppBackupData(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val babies: List<BabyBackupItem> = emptyList()
)

data class BabyBackupItem(
    val profile: BabyProfile,
    val activities: List<ActivityRecord> = emptyList(),
    val healthRecords: List<HealthRecord> = emptyList(),
    val moments: List<MomentRecord> = emptyList()
)

