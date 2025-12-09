package com.patientbmi.app.data.model

import com.google.gson.annotations.SerializedName

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
    val fullName: String,

    @SerializedName("age")
    val age: Int
)

data class PatientRegistrationRequest(
    @SerializedName("first_name")
    val firstName: String,

    @SerializedName("middle_name")
    val middleName: String? = null,

    @SerializedName("last_name")
    val lastName: String,

    @SerializedName("gender")
    val gender: String,

    @SerializedName("date_of_birth")
    val dateOfBirth: String
)