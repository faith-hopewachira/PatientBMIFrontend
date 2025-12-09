package com.patientbmi.app.data.model

import com.google.gson.annotations.SerializedName

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