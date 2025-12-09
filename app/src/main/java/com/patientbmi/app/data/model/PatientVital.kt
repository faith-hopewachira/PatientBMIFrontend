package com.patientbmi.app.data.model

import com.google.gson.annotations.SerializedName

//data class PatientVital(
//    @SerializedName("patient_id")
//    val patientId: String,
//
//    @SerializedName("visit_date")
//    val visitDate: String,
//
//    @SerializedName("height_cm")
//    val heightCm: Double,
//
//    @SerializedName("weight_kg")
//    val weightKg: Double
//)


data class PatientVital(
    @SerializedName("patient_id")
    val patientId: String,

    @SerializedName("visit_date")
    val visitDate: String,

    @SerializedName("height_cm")
    val heightCm: Double,

    @SerializedName("weight_kg")
    val weightKg: Double,

    @SerializedName("bmi")
    val bmi: Double? = null,

    @SerializedName("bmi_status")
    val bmiStatus: String? = null
)


data class VitalResponse(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("patient_name")
    val patientName: String? = null,

    @SerializedName("bmi")
    val bmi: Double,

    @SerializedName("bmi_status")
    val bmiStatus: String,

    @SerializedName("next_form")
    val nextForm: String
)