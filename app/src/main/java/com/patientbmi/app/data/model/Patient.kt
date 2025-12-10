package com.patientbmi.app.data.model

import com.google.gson.annotations.SerializedName

/**
 * Data class representing a Patient entity in the system.
 *
 * This is the core domain model for patient information, containing both
 * demographic data and computed properties. The model supports both local
 * database storage and API communication through GSON serialization.
 *
 * @property id Unique database identifier (primary key, typically auto-generated)
 * @property patientId Optional alternative patient identifier (e.g., hospital ID number)
 * @property firstName Patient's first name (required)
 * @property middleName Patient's middle name (optional)
 * @property lastName Patient's last name (required)
 * @property gender Patient's gender ("Male", "Female", "Other")
 * @property dateOfBirth Date of birth in ISO 8601 format (YYYY-MM-DD), nullable for historical data
 * @property registrationDate Date when patient was registered in the system (ISO 8601)
 * @property fullName Computed full name combining first, middle, and last names
 * @property age Computed age based on date of birth (calculated at registration)
 *
 * @see com.patientbmi.app.data.model.PatientRegistrationRequest for input DTO
 * @see com.patientbmi.app.data.repository.PatientRepository for business logic
 *
 * @note The `patientId` field is nullable to support legacy data migration
 * @warning `dateOfBirth` is nullable but should be provided for accurate BMI calculations
 */
data class Patient(
    @SerializedName("id")
    val id: String,

    @SerializedName("patient_id")
    val patientId: String? = null,

    @SerializedName("first_name")
    val firstName: String,

    @SerializedName("middle_name")
    val middleName: String? = null,

    @SerializedName("last_name")
    val lastName: String,

    @SerializedName("gender")
    val gender: String,

    @SerializedName("date_of_birth")
    val dateOfBirth: String?,

    @SerializedName("registration_date")
    val registrationDate: String,

    @SerializedName("full_name")
    val fullName: String? = null,

    @SerializedName("age")
    val age: Int? = null
)
/**

 * Patient model represents the input data when registering a new patient,
 * containing only the fields required for initial registration. It excludes
 * computed fields like `id`, `registrationDate`, `fullName`, and `age` which
 * are generated server-side.
 *
 * @property firstName Patient's first name
 * @property middleName Patient's middle name
 * @property lastName Patient's last name
 * @property gender Patient's gender
 * @property dateOfBirth Date of birth in ISO 8601 format (YYYY-MM-DD)

 */

data class PatientRegistrationRequest(
    @SerializedName("patient_id")
    val patientId: String? = null,  // Make sure this matches your EditText

    @SerializedName("first_name")
    val firstName: String,

    @SerializedName("middle_name")
    val middleName: String? = null,

    @SerializedName("last_name")
    val lastName: String,

    @SerializedName("gender")
    val gender: String,

    @SerializedName("date_of_birth")
    val dateOfBirth: String,

    @SerializedName("registration_date")
    val registrationDate: String? = null
)