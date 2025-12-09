package com.patientbmi.app.data.model

import com.google.gson.annotations.SerializedName

/**
 *
 * @property id Unique patient identifier (primary key)
 * @property fullName Patient's complete name for display purposes
 * @property age Current age of the patient (calculated from date of birth)
 * @property lastBmiStatus Most recent BMI classification from assessments
 *                        ("Underweight", "Normal", "Overweight", "Obese")
 * @property lastAssessmentDate Date of the most recent BMI assessment (ISO 8601 format),
 *                              nullable for patients without assessments

 */
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