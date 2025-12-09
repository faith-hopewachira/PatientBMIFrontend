package com.patientbmi.app.data.model

import com.google.gson.annotations.SerializedName

/**

 * This model captures essential measurements during patient visits
 * and includes computed BMI values.
 *
 * @property patientId Foreign key reference to the associated patient
 * @property visitDate Date of measurement in ISO 8601 format (YYYY-MM-DD)
 * @property heightCm Patient height in centimeters
 * @property weightKg Patient weight in kilograms
 * @property bmi Computed Body Mass Index
 *               nullable for records before calculation
 * @property bmiStatus Classification based on WHO BMI categories:
 *                    - "Underweight" (BMI < 18.5)
 *                    - "Normal" (BMI 18.5-24.9)
 *                    - "Overweight" (BMI 25-29.9)
 *                    - "Obese" (BMI ≥ 30)
 *                    Nullable for uncalculated records
 */

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

/**
 * Patient Vital model represents the server response after successfully submitting
 * patient vital measurements.
 *
 * @property id Unique identifier of the created vital record
 * @property patientName Full name of the patient
 * @property bmi Calculated Body Mass Index with decimal precision
 * @property bmiStatus Classification of the calculated BMI
 * @property nextForm Indicates which assessment form should be displayed next:
 *                    - "general" for Normal/Underweight patients
 *                    - "overweight" for Overweight patients (BMI 25-29.9)
 *                    - "obese" for Obese patients (BMI ≥ 30)
 *
 */
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