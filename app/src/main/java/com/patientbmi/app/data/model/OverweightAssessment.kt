package com.patientbmi.app.data.model

import com.google.gson.annotations.SerializedName

/**
 * Overweight Assessment model captures specific assessment data for patients identified as overweight,
 * including dietary history tracking.
 *
 * @property patientId Unique identifier for the patient (foreign key to Patient)
 * @property visitDate Date of the assessment in ISO 8601 format (YYYY-MM-DD)
 * @property generalHealth Overall health status assessment ("Good", "Fair", "Poor", etc.)
 * @property dietHistory Boolean flag indicating if detailed dietary history was recorded
 * @property comments Clinical notes or specific recommendations for weight management

 */
data class OverweightAssessment(
    @SerializedName("patient_id")
    val patientId: String,

    @SerializedName("visit_date")
    val visitDate: String,

    @SerializedName("general_health")
    val generalHealth: String,

    @SerializedName("diet_history")
    val dietHistory: Boolean,

    @SerializedName("comments")
    val comments: String
)