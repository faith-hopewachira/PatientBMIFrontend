package com.patientbmi.app.ui.assessment_general

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.patientbmi.app.R
import com.patientbmi.app.data.model.GeneralAssessment
import com.patientbmi.app.data.remote.api.RetrofitInstance
import com.patientbmi.app.ui.patient_listing.PatientListingActivity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Activity for submitting general health assessments for patients with normal BMI.
 *
 * This screen allows healthcare providers to record general health assessments
 * for patients who have BMI values in the normal or underweight range (BMI < 25).
 * Flow:
 * 1. Receives patient_id and patient_name from previous screen
 * 2. Pre-fills current date as default visit date
 * 3. Validates all required fields
 * 4. Submits data to backend API
 * 5. Navigates to patient listing on success
 *
 * Layout: activity_general_assessment.xml
 */
class GeneralAssessmentActivity : AppCompatActivity() {

    // UI Components
    private lateinit var tvPatientName: TextView
    private lateinit var etVisitDate: EditText
    private lateinit var rgGeneralHealth: RadioGroup
    private lateinit var rgDrugUse: RadioGroup
    private lateinit var etComments: EditText
    private lateinit var btnSubmit: Button

    // Data passed from previous activity
    private lateinit var patientId: String
    private lateinit var patientName: String

    /**
     * Activity lifecycle onCreate method.
     * Initializes the activity, sets up UI components, and configures event listeners.
     * Retrieves patient data passed via Intent extras and sets default values.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_general_assessment)

        // Retrieve patient data from Intent
        patientId = intent.getStringExtra("patient_id") ?: ""
        patientName = intent.getStringExtra("patient_name") ?: ""

        // Initialize UI components
        initViews()

        // Set default values
        tvPatientName.text = patientName
        etVisitDate.setText(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))

        // Configure button click listeners
        btnSubmit.setOnClickListener { submitAssessment() }
        etVisitDate.setOnClickListener { showDatePicker() }
    }

    /**
     * Initializes all UI view references.
     * Binds XML layout components to Kotlin properties using findViewById.
     * Should be called after setContentView() in onCreate().
     */
    private fun initViews() {
        tvPatientName = findViewById(R.id.tvPatientName)
        etVisitDate = findViewById(R.id.etVisitDate)
        rgGeneralHealth = findViewById(R.id.rgGeneralHealth)
        rgDrugUse = findViewById(R.id.rgDrugUse)
        etComments = findViewById(R.id.etComments)
        btnSubmit = findViewById(R.id.btnSubmit)
    }

    /**
     * Displays a DatePickerDialog for selecting the visit date.
     * Opens a native Android date picker dialog with current date pre-selected.
     * Updates the visit date EditText with selected date in "yyyy-MM-dd" format.
     * Format: ISO 8601 (required by backend API)
     */
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(year, month, day)
                etVisitDate.setText(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    /**
     * Validates and submits the general assessment form.
     *
     * Performs the following steps:
     * 1. Validates all required fields are filled
     * 2. Extracts data from form inputs
     * 3. Creates GeneralAssessment data object
     * 4. Makes network call using Retrofit
     * 5. Handles success/error responses
     * 6. Navigates on successful submission
     *
     * Error Handling:
     * - Shows Toast messages for validation errors
     * - Disables submit button during network call to prevent duplicate submissions
     * - Catches and displays network exceptions
     * - Re-enables submit button after completion (success or error)
     */
    private fun submitAssessment() {
        // Extract and trim input values
        val visitDate = etVisitDate.text.toString().trim()
        val comments = etComments.text.toString().trim()

        // Get selected radio button IDs
        val generalHealthId = rgGeneralHealth.checkedRadioButtonId
        val drugUseId = rgDrugUse.checkedRadioButtonId

        // Validate all required fields
        if (visitDate.isEmpty() || generalHealthId == -1 || drugUseId == -1 || comments.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        // Extract values from selected radio buttons
        val generalHealth = findViewById<RadioButton>(generalHealthId).text.toString()
        val currentlyUsingDrugs = findViewById<RadioButton>(drugUseId).text.toString() == "Yes"

        // Create data transfer object
        val assessment = GeneralAssessment(
            patientId = patientId,
            visitDate = visitDate,
            generalHealth = generalHealth,
            currentlyUsingDrugs = currentlyUsingDrugs,
            comments = comments
        )

        // Launch coroutine for network operation
        lifecycleScope.launch {
            try {
                // Disable button to prevent duplicate submissions
                btnSubmit.isEnabled = false

                // Make network call
                val response = RetrofitInstance.api.submitGeneralAssessment(assessment)

                // Handle response
                if (response.isSuccessful) {
                    // Success: Show confirmation and navigate
                    Toast.makeText(
                        this@GeneralAssessmentActivity,
                        "Assessment submitted!",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Navigate back to patient listing
                    startActivity(
                        android.content.Intent(
                            this@GeneralAssessmentActivity,
                            PatientListingActivity::class.java
                        )
                    )
                    finish() // Remove this activity from back stack
                } else {
                    // API error: Parse and display error message
                    val error = response.errorBody()?.string() ?: "Submission failed"
                    Toast.makeText(
                        this@GeneralAssessmentActivity,
                        "Error: $error",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                // Network or unexpected error
                e.printStackTrace()
                Toast.makeText(
                    this@GeneralAssessmentActivity,
                    "Network error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                // Always re-enable submit button
                btnSubmit.isEnabled = true
            }
        }
    }
}

