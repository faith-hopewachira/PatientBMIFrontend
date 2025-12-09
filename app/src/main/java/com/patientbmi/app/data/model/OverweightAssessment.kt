package com.patientbmi.app.data.model

import com.google.gson.annotations.SerializedName

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