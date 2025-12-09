package com.patientbmi.app.data.remote.api

import com.patientbmi.app.data.model.*
import retrofit2.Response
import retrofit2.http.*

/**

 * Retrofit interface acts as the contract between the Android application
 * and the backend API for all patient assessment operations
 *
 * All endpoints return Retrofit [Response] objects containing either the
 * successful response body or error information for proper error handling.

 */
interface AssessmentApi {

    /**
     * Registers a new patient in the system.
     *
     * @param patient Patient registration data transfer object
     * @return [Response] containing the created [Patient] entity with server-generated fields
     *
     * HTTP Method: POST
     * Endpoint: /patients/

     * Status Codes:
     * - 201 Created: Patient successfully registered
     * - 400 Bad Request: Invalid input data
     * - 409 Conflict: Patient already exists
     * - 500 Internal Server Error: Server-side failure
     */
    @POST("patients/")
    suspend fun registerPatient(@Body patient: PatientRegistrationRequest): Response<Patient>

    /**
     * Retrieves a filtered list of patients for display purposes.
     *
     * @param fromDate Optional start date filter (ISO 8601 format: YYYY-MM-DD)
     * @param toDate Optional end date filter (ISO 8601 format: YYYY-MM-DD)
     * @return [Response] containing list of [PatientListingItem] for UI display
     *
     * HTTP Method: GET
     * Endpoint: /patients/listing/

     * Status Codes:
     * - 200 OK: Successfully retrieved patient list
     * - 400 Bad Request: Invalid date format
     * - 500 Internal Server Error: Server-side failure
     */
    @GET("patients/listing/")
    suspend fun getPatientListing(
        @Query("from_date") fromDate: String? = null,
        @Query("to_date") toDate: String? = null
    ): Response<List<PatientListingItem>>

    /**
     * @param vitals Patient's height and weight measurements
     * @return [Response] containing [VitalResponse] with BMI results and next steps
     *
     * HTTP Method: POST
     * Endpoint: /vitals/

     * Status Codes:
     * - 201 Created: Vitals successfully recorded
     * - 400 Bad Request: Invalid measurement values
     * - 404 Not Found: Patient does not exist
     * - 500 Internal Server Error: Server-side failure
     */
    @POST("vitals/")
    suspend fun submitVitals(@Body vitals: PatientVital): Response<VitalResponse>

    /**
     * Submits overweight-specific assessment for patients with BMI ≥ 25.
     *
     * @param assessment Overweight assessment data including dietary history
     * @return [Response] containing the created [OverweightAssessment] record
     *
     * HTTP Method: POST
     * Endpoint: /overweight-assessments/
     *
     * Status Codes:
     * - 201 Created: Assessment successfully recorded
     * - 400 Bad Request: Invalid assessment data or patient not overweight
     * - 404 Not Found: Patient or vital record not found
     * - 500 Internal Server Error: Server-side failure
     */
    @POST("overweight-assessments/")
    suspend fun submitOverweightAssessment(@Body assessment: OverweightAssessment): Response<OverweightAssessment>

    /**
     * Submits general health assessment for patients with BMI < 25.
     *
     * @param assessment General health assessment data
     * @return [Response] containing the created [GeneralAssessment] record
     *
     * HTTP Method: POST
     * Endpoint: /general-assessments/
     *
     * Status Codes:
     * - 201 Created: Assessment successfully recorded
     * - 400 Bad Request: Invalid assessment data or patient not eligible
     * - 404 Not Found: Patient or vital record not found
     * - 500 Internal Server Error: Server-side failure
     */
    @POST("general-assessments/")
    suspend fun submitGeneralAssessment(@Body assessment: GeneralAssessment): Response<GeneralAssessment>
}