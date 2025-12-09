package com.patientbmi.app.data.model

import com.google.gson.annotations.SerializedName

/**
 * GeneralAssessment model is used to capture and serialize/deserialize general health
 * assessment data during patient visits.
 *
 * @property patientId Unique identifier for the patient
 * @property visitDate Date of the patient's visit in ISO 8601 format (YYYY-MM-DD)
 * @property generalHealth Subjective assessment of patient's overall health status
 * @property currentlyUsingDrugs Flag indicating if patient is currently on medication
 * @property comments Additional notes from the healthcare provider

 */
data class GeneralAssessment(
    @SerializedName("patient_id")
    val patientId: String,

    @SerializedName("visit_date")
    val visitDate: String,

    @SerializedName("general_health")
    val generalHealth: String,

    @SerializedName("currently_using_drugs")
    val currentlyUsingDrugs: Boolean,

    @SerializedName("comments")
    val comments: String
)