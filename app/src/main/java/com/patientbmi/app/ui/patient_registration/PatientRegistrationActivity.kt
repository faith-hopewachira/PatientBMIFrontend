package com.patientbmi.app.ui.patient_registration

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.patientbmi.app.R
import com.patientbmi.app.data.model.PatientRegistrationRequest
import com.patientbmi.app.data.remote.api.RetrofitInstance
import com.patientbmi.app.ui.patient_listing.PatientListingActivity
import com.patientbmi.app.ui.vitals.VitalsActivity
import com.patientbmi.app.utils.BaseToastHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class PatientRegistrationActivity : AppCompatActivity() {

    private lateinit var etPatientNo: EditText
    private lateinit var etFirstName: EditText
    private lateinit var etMiddleName: EditText
    private lateinit var etLastName: EditText
    private lateinit var rgGender: RadioGroup
    private lateinit var etDateOfBirth: EditText
    private lateinit var etRegistrationDate: EditText
    private lateinit var btnCancel: Button
    private lateinit var btnSubmit: Button

    private val TAG = "PatientRegistration"
    private var isDestroyed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_registration)

        Log.d(TAG, "=== PatientRegistrationActivity onCreate ===")

        initViews()

        // Set default dates
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        etRegistrationDate.setText(today)

        if (etDateOfBirth.text.isEmpty()) {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.YEAR, -30)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            etDateOfBirth.setText(dateFormat.format(calendar.time))
        }

        btnSubmit.setOnClickListener {
            validateAndRegister()
        }

        btnCancel.setOnClickListener {
            navigateToPatientListing()
        }

        etDateOfBirth.setOnClickListener {
            showDatePicker(etDateOfBirth)
        }

        etRegistrationDate.setOnClickListener {
            showDatePicker(etRegistrationDate)
        }
    }

    override fun onStart() {
        super.onStart()
        isDestroyed = false
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "=== PatientRegistrationActivity onDestroy ===")
        isDestroyed = true
        BaseToastHelper.cancelCurrentToast()
    }

    private fun initViews() {
        etPatientNo = findViewById(R.id.etPatientNo)
        etFirstName = findViewById(R.id.etFirstName)
        etMiddleName = findViewById(R.id.etMiddleName)
        etLastName = findViewById(R.id.etLastName)
        rgGender = findViewById(R.id.rgGender)
        etDateOfBirth = findViewById(R.id.etDateOfBirth)
        etRegistrationDate = findViewById(R.id.etRegistrationDate)
        btnCancel = findViewById(R.id.btnCancel)
        btnSubmit = findViewById(R.id.btnSubmit)
    }

    private fun showDatePicker(editText: EditText) {
        if (isDestroyed) return

        val calendar = Calendar.getInstance()

        // If editing date of birth, default to 30 years ago
        if (editText == etDateOfBirth) {
            calendar.add(Calendar.YEAR, -30)
        }

        // Create DatePickerDialog with proper styling
        val datePicker = DatePickerDialog(
            this,
            R.style.DatePickerTheme, // Custom theme for better visibility
            { _, year, month, day ->
                val formattedDate = String.format("%04d-%02d-%02d", year, month + 1, day)
                editText.setText(formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // Set custom colors for better visibility
        datePicker.datePicker.setBackgroundColor(Color.WHITE)

        // Set the dialog title
        datePicker.setTitle("Select Date")

        // Set button colors
        datePicker.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK)
        datePicker.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK)

        datePicker.show()
    }

    private fun navigateToPatientListing() {
        Log.d(TAG, "=== Navigating to PatientListing ===")
        BaseToastHelper.cancelCurrentToast()
        val intent = Intent(this, PatientListingActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun navigateToVitals(patientId: String, patientName: String) {
        Log.d(TAG, "=== Navigating to Vitals ===")
        BaseToastHelper.cancelCurrentToast()
        val intent = Intent(this, VitalsActivity::class.java)
            .apply {
                putExtra("patient_id", patientId)
                putExtra("patient_name", patientName)
                // Clear back stack
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        startActivity(intent)
        finish()
    }

    private fun validateAndRegister() {
        Log.d(TAG, "=== Starting registration ===")

        if (isDestroyed) return

        // Get values
        val patientNo = etPatientNo.text.toString().trim()
        val firstName = etFirstName.text.toString().trim()
        val middleName = etMiddleName.text.toString().trim()
        val lastName = etLastName.text.toString().trim()
        val dateOfBirth = etDateOfBirth.text.toString().trim()
        val registrationDate = etRegistrationDate.text.toString().trim()

        // Validate gender
        val genderId = rgGender.checkedRadioButtonId
        if (genderId == -1) {
            showToastSafe("Please select gender")
            return
        }

        // Validate required fields
        if (patientNo.isEmpty() || firstName.isEmpty() || lastName.isEmpty() ||
            dateOfBirth.isEmpty() || registrationDate.isEmpty()) {
            showToastSafe("Please fill all required fields")
            return
        }

        // Get gender value - convert to single letter
        val gender = when (genderId) {
            R.id.rbMale -> "M"
            R.id.rbFemale -> "F"
            else -> "M"
        }

        Log.d(TAG, "Gender code: $gender")

        // Create request
        val patientRequest = PatientRegistrationRequest(
            patientId = patientNo,
            firstName = firstName,
            middleName = if (middleName.isEmpty()) null else middleName,
            lastName = lastName,
            gender = gender,
            dateOfBirth = dateOfBirth,
            registrationDate = registrationDate
        )

        Log.d(TAG, "Sending: $patientRequest")

        // Disable button immediately
        btnSubmit.isEnabled = false
        btnSubmit.text = "Registering..."
        btnCancel.isEnabled = false

        lifecycleScope.launch {
            try {
                Log.d(TAG, "=== Making API call ===")
                val response = RetrofitInstance.api.registerPatient(patientRequest)

                if (response.isSuccessful) {
                    val patient = response.body()!!
                    Log.d(TAG, "Success: $patient")

                    runOnUiThread {
                        if (isDestroyed || isFinishing) {
                            Log.w(TAG, "Activity finishing, skipping navigation")
                            return@runOnUiThread
                        }

                        try {
                            showToastSafe("Patient registered successfully!")
                            // Navigate immediately without delay
                            navigateToVitals(patient.id, "${patient.firstName} ${patient.lastName}")
                        } catch (e: Exception) {
                            Log.e(TAG, "Error navigating: ${e.message}")
                            resetSubmitButton()
                        }
                    }

                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e(TAG, "API Error: $errorBody")

                    runOnUiThread {
                        if (isDestroyed || isFinishing) return@runOnUiThread
                        resetSubmitButton()

                        val errorMessage = when {
                            response.code() == 400 -> "Bad request. Check your data."
                            response.code() == 409 -> "Patient ID already exists."
                            response.code() == 500 -> "Server error. Please try again."
                            else -> "Registration failed: ${errorBody ?: "Unknown error"}"
                        }

                        showToastSafe(errorMessage)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception: ${e.message}", e)

                runOnUiThread {
                    if (isDestroyed || isFinishing) return@runOnUiThread
                    resetSubmitButton()
                    showToastSafe("Network error: ${e.message}")
                }
            }
        }
    }

    private fun resetSubmitButton() {
        if (isDestroyed || isFinishing) return
        btnSubmit.isEnabled = true
        btnSubmit.text = "Register Patient"
        btnCancel.isEnabled = true
    }

    private fun showToastSafe(message: String) {
        BaseToastHelper.showToastFromActivity(this, message)
    }
}