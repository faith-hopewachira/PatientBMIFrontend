package com.patientbmi.app.data.model

import com.google.gson.annotations.SerializedName

data class PatientListingItem(
    @SerializedName("id")
    val id: String,

    @SerializedName("full_name")
    val fullName: String,

    @SerializedName("age")
    val age: Int,

    @SerializedName("last_bmi_status")
    val lastBmiStatus: String,

    @SerializedName("last_assessment_date")
    val lastAssessmentDate: String? = null
)