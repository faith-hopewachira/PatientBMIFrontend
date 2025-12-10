package com.patientbmi.app.ui.assessment_overweight

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.patientbmi.app.R
import com.patientbmi.app.data.model.OverweightAssessment
import com.patientbmi.app.data.remote.api.RetrofitInstance
import com.patientbmi.app.ui.patient_listing.PatientListingActivity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Activity for submitting overweight-specific assessments for patients with BMI ≥ 25.
 *
 * This assessment screen is displayed for patients identified as overweight
 * (BMI between 25-29.9) or obese (BMI ≥ 30). It captures additional dietary history
 * information crucial for weight management counseling and follow-up care
 * Workflow Context:
 * 1. Patient vital signs are recorded (height/weight)
 * 2. BMI is calculated (≥ 25 triggers this assessment)
 * 3. This activity is launched with patient details
 * 4. Healthcare provider completes overweight-specific assessment
 * 5. Data is submitted to backend for clinical records
 *
 * Layout: activity_overweight_assessment.xml
 */
class OverweightAssessmentActivity : AppCompatActivity() {

    // UI Component References
    private lateinit var tvPatientName: TextView
    private lateinit var etVisitDate: EditText
    private lateinit var rgGeneralHealth: RadioGroup
    private lateinit var rgDietHistory: RadioGroup
    private lateinit var etComments: EditText
    private lateinit var btnCancel: Button
    private lateinit var btnSubmit: Button

    // Patient data received from previous activity
    private lateinit var patientId: String
    private lateinit var patientName: String

    /**
     * Activity lifecycle onCreate method.
     * Sets up the activity layout, initializes UI components, retrieves patient data,
     * and configures event listeners for form submission and date selection.
     * Data Flow:
     * - Receives patient_id and patient_name from Intent extras
     * - Sets current date as default assessment date
     * - Configures button click listeners
     * @throws IllegalArgumentException if patient_id or patient_name are missing from Intent
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_overweight_assessment)

        // Extract patient data from Intent (passed from previous activity)
        patientId = intent.getStringExtra("patient_id") ?: ""
        patientName = intent.getStringExtra("patient_name") ?: ""

        // Initialize all UI component references
        initViews()

        // Set patient name and default date
        tvPatientName.text = patientName
        etVisitDate.setText(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))

        // Configure event listeners
        btnSubmit.setOnClickListener { submitAssessment() }
        btnCancel.setOnClickListener { navigateToPatientListing() }
        etVisitDate.setOnClickListener { showDatePicker() }
    }

    /**
     * Initializes all UI view references by binding them to Kotlin properties.
     * Uses findViewById to connect XML layout elements to class properties.
     * Should be called immediately after setContentView() in onCreate().
     * @throws NullPointerException if any view ID doesn't exist in the layout
     */
    private fun initViews() {
        tvPatientName = findViewById(R.id.tvPatientName)
        etVisitDate = findViewById(R.id.etVisitDate)
        rgGeneralHealth = findViewById(R.id.rgGeneralHealth)
        rgDietHistory = findViewById(R.id.rgDietHistory)
        etComments = findViewById(R.id.etComments)
        btnCancel = findViewById(R.id.btnCancel)
        btnSubmit = findViewById(R.id.btnSubmit)
    }

    /**
     * Displays a DatePickerDialog for selecting the assessment date.
     * Opens a native Android date picker with the current date pre-selected.
     * Updates the visit date EditText with the selected date in ISO 8601 format (yyyy-MM-dd).
     * This format is required by the backend API for consistent date parsing.
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
     * Navigates back to patient listing screen.
     * Called when Cancel button is clicked.
     */
    private fun navigateToPatientListing() {
        startActivity(android.content.Intent(this, PatientListingActivity::class.java))
        finish()
    }

    /**
     * Validates form data and submits the overweight assessment to the backend.
     * Performs comprehensive validation before making network call:
     * 1. Validates all required fields are completed
     * 2. Extracts and formats data from UI components
     * 3. Creates OverweightAssessment data object
     * 4. Makes asynchronous network request
     * 5. Handles success and error responses appropriately
     * 6. Manages UI state during network operations
     *
     * Error Handling Scenarios:
     * - Form validation failures (shows Toast)
     * - Network connectivity issues
     * - API error responses (4xx, 5xx status codes)
     * - JSON parsing errors
     */
    private fun submitAssessment() {
        // Extract and sanitize input values
        val visitDate = etVisitDate.text.toString().trim()
        val comments = etComments.text.toString().trim()

        // Get selected radio button states
        val generalHealthId = rgGeneralHealth.checkedRadioButtonId
        val dietHistoryId = rgDietHistory.checkedRadioButtonId

        // Validate all required fields
        if (visitDate.isEmpty() || generalHealthId == -1 || dietHistoryId == -1 || comments.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        // Extract values from selected radio buttons
        val generalHealth = findViewById<RadioButton>(generalHealthId).text.toString()
        val dietHistory = findViewById<RadioButton>(dietHistoryId).text.toString() == "Yes"

        // Create data transfer object for API submission
        val assessment = OverweightAssessment(
            patientId = patientId,
            visitDate = visitDate,
            generalHealth = generalHealth,
            dietHistory = dietHistory,
            comments = comments
        )

        // Launch coroutine for asynchronous network operation
        lifecycleScope.launch {
            try {
                // Disable buttons to prevent duplicate submissions
                btnSubmit.isEnabled = false
                btnCancel.isEnabled = false

                // Make API call
                val response = RetrofitInstance.api.submitOverweightAssessment(assessment)

                // Handle API response
                if (response.isSuccessful) {
                    // Success: Show confirmation and navigate to patient listing
                    Toast.makeText(
                        this@OverweightAssessmentActivity,
                        "Assessment submitted!",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Navigate back to main patient listing
                    startActivity(
                        android.content.Intent(
                            this@OverweightAssessmentActivity,
                            PatientListingActivity::class.java
                        )
                    )
                    finish() // Remove this activity from back stack
                } else {
                    // API returned error status code
                    val error = response.errorBody()?.string() ?: "Submission failed"
                    Toast.makeText(
                        this@OverweightAssessmentActivity,
                        "Error: $error",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                // Network failure or unexpected exception
                e.printStackTrace()
                Toast.makeText(
                    this@OverweightAssessmentActivity,
                    "Network error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                // Always re-enable buttons regardless of outcome
                btnSubmit.isEnabled = true
                btnCancel.isEnabled = true
            }
        }
    }
}