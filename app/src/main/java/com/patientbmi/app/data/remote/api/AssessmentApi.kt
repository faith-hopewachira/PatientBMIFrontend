package com.patientbmi.app.data.remote.api

import com.patientbmi.app.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface AssessmentApi {

    @POST("patients/")
    suspend fun registerPatient(@Body patient: PatientRegistrationRequest): Response<Patient>

    @GET("patients/listing/")
    suspend fun getPatientListing(
        @Query("from_date") fromDate: String? = null,
        @Query("to_date") toDate: String? = null
    ): Response<List<PatientListingItem>>

    @POST("vitals/")
    suspend fun submitVitals(@Body vitals: PatientVital): Response<VitalResponse>

    @POST("overweight-assessments/")
    suspend fun submitOverweightAssessment(@Body assessment: OverweightAssessment): Response<OverweightAssessment>

    @POST("general-assessments/")
    suspend fun submitGeneralAssessment(@Body assessment: GeneralAssessment): Response<GeneralAssessment>
}