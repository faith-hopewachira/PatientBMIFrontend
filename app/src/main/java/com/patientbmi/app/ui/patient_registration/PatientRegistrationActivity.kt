package com.patientbmi.app.ui.patient_registration

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.patientbmi.app.R
import com.patientbmi.app.data.model.PatientRegistrationRequest
import com.patientbmi.app.data.remote.api.RetrofitInstance
import com.patientbmi.app.ui.vitals.VitalsActivity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * This screen captures essential patient demographic information required for
 * BMI tracking and clinical assessments.
 * Workflow:
 * 1. User enters patient demographic information
 * 2. Form validation ensures data completeness and correctness
 * 3. API call registers patient in backend system
 * 4. On success: Navigates to VitalsActivity for BMI measurement
 * 5. On failure: Displays error with opportunity to correct data
 *
 * Layout: activity_patient_registration.xml
 */
class PatientRegistrationActivity : AppCompatActivity() {

    // UI Component References
    private lateinit var etFirstName: EditText
    private lateinit var etMiddleName: EditText
    private lateinit var etLastName: EditText
    private lateinit var rgGender: RadioGroup
    private lateinit var etDateOfBirth: EditText
    private lateinit var btnSubmit: Button

    /**
     * Initializes the activity layout, sets up UI components, configures
     * default values, and attaches event listeners for form interaction.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_registration)

        // Initialize UI component references
        initViews()

        // Configure button click listener for form submission
        btnSubmit.setOnClickListener {
            validateAndRegister()
        }

        // Configure date picker for date of birth selection
        etDateOfBirth.setOnClickListener {
            showDatePicker()
        }

        // Set sensible default date (30 years ago) for quick registration
        setDefaultDate()
    }

    /**
     * Binds XML layout components to Kotlin properties using findViewById.
     * Called in onCreate() after setContentView().
     */
    private fun initViews() {
        etFirstName = findViewById(R.id.etFirstName)
        etMiddleName = findViewById(R.id.etMiddleName)
        etLastName = findViewById(R.id.etLastName)
        rgGender = findViewById(R.id.rgGender)
        etDateOfBirth = findViewById(R.id.etDateOfBirth)
        btnSubmit = findViewById(R.id.btnSubmit)
    }

    /**
     * Provides a sensible default for typical adult patients to reduce
     * manual input. Date is formatted in ISO 8601 (yyyy-MM-dd) format.
     */
    private fun setDefaultDate() {
        if (etDateOfBirth.text.isEmpty()) {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.YEAR, -30) // Set to 30 years ago
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            etDateOfBirth.setText(dateFormat.format(calendar.time))
        }
    }

    /**
     * Opens a native Android date picker pre-set to 30 years ago (typical adult).
     * Formats the selected date in ISO 8601 format (yyyy-MM-dd) required by API.
     */
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, -30) // Default to 30 years ago

        DatePickerDialog(
            this,
            { _, year, month, day ->
                // Format as yyyy-MM-dd (ISO 8601)
                val formattedDate = String.format("%04d-%02d-%02d", year, month + 1, day)
                etDateOfBirth.setText(formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    /**
     * Validates form inputs and registers the patient via API.
     * API Integration:
     * - Uses Retrofit for network communication
     * - Handles network errors and API error responses
     * - Updates UI state during network operations
     */
    private fun validateAndRegister() {
        // Extract and trim input values
        val firstName = etFirstName.text.toString().trim()
        val middleName = etMiddleName.text.toString().trim()
        val lastName = etLastName.text.toString().trim()
        val dateOfBirth = etDateOfBirth.text.toString().trim()

        // Validate gender selection
        val genderId = rgGender.checkedRadioButtonId
        if (genderId == -1) {
            Toast.makeText(this, "Please select gender", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate required fields
        if (firstName.isEmpty() || lastName.isEmpty() || dateOfBirth.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate date format (ISO 8601: yyyy-MM-dd)
        if (!dateOfBirth.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
            Toast.makeText(this, "Invalid date format. Use YYYY-MM-DD", Toast.LENGTH_SHORT).show()
            return
        }

        // Extract gender selection (M/F encoding)
        val genderBtn = findViewById<RadioButton>(genderId)
        val gender = if (genderBtn.text == "Male") "M" else "F"

        // Create data transfer object
        val patientRequest = PatientRegistrationRequest(
            firstName = firstName,
            middleName = if (middleName.isEmpty()) null else middleName,
            lastName = lastName,
            gender = gender,
            dateOfBirth = dateOfBirth
        )

        // Launch coroutine for asynchronous network operation
        lifecycleScope.launch {
            try {
                // Update UI state for network operation
                btnSubmit.isEnabled = false
                btnSubmit.text = "Registering..."

                // Make API call
                val response = RetrofitInstance.api.registerPatient(patientRequest)

                // Handle API response
                if (response.isSuccessful) {
                    val patient = response.body()!!

                    // Success: Show confirmation
                    Toast.makeText(
                        this@PatientRegistrationActivity,
                        "Patient registered successfully!",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Navigate to vital sign collection for the new patient
                    startActivity(
                        Intent(
                            this@PatientRegistrationActivity,
                            VitalsActivity::class.java
                        ).apply {
                            putExtra("patient_id", patient.id)
                            putExtra("patient_name", patient.fullName)
                        }
                    )
                    finish() // Remove registration activity from back stack
                } else {
                    // API error response
                    val error = response.errorBody()?.string() ?: "Registration failed"
                    Toast.makeText(
                        this@PatientRegistrationActivity,
                        "Error: $error",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                // Network or unexpected error
                Toast.makeText(
                    this@PatientRegistrationActivity,
                    "Network error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                // Restore UI state regardless of outcome
                btnSubmit.isEnabled = true
                btnSubmit.text = "Register Patient"
            }
        }
    }
}
