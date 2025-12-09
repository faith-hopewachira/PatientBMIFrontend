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

class PatientRegistrationActivity : AppCompatActivity() {

    private lateinit var etFirstName: EditText
    private lateinit var etMiddleName: EditText
    private lateinit var etLastName: EditText
    private lateinit var rgGender: RadioGroup
    private lateinit var etDateOfBirth: EditText
    private lateinit var btnSubmit: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_registration)

        initViews()

        btnSubmit.setOnClickListener {
            validateAndRegister()
        }

        etDateOfBirth.setOnClickListener {
            showDatePicker()
        }

        // Set default date (30 years ago)
        setDefaultDate()
    }

    private fun initViews() {
        etFirstName = findViewById(R.id.etFirstName)
        etMiddleName = findViewById(R.id.etMiddleName)
        etLastName = findViewById(R.id.etLastName)
        rgGender = findViewById(R.id.rgGender)
        etDateOfBirth = findViewById(R.id.etDateOfBirth)
        btnSubmit = findViewById(R.id.btnSubmit)
    }

    private fun setDefaultDate() {
        if (etDateOfBirth.text.isEmpty()) {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.YEAR, -30)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            etDateOfBirth.setText(dateFormat.format(calendar.time))
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, -30)

        DatePickerDialog(
            this,
            { _, year, month, day ->
                val formattedDate = String.format("%04d-%02d-%02d", year, month + 1, day)
                etDateOfBirth.setText(formattedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun validateAndRegister() {
        val firstName = etFirstName.text.toString().trim()
        val middleName = etMiddleName.text.toString().trim()
        val lastName = etLastName.text.toString().trim()
        val dateOfBirth = etDateOfBirth.text.toString().trim()

        val genderId = rgGender.checkedRadioButtonId
        if (genderId == -1) {
            Toast.makeText(this, "Please select gender", Toast.LENGTH_SHORT).show()
            return
        }

        if (firstName.isEmpty() || lastName.isEmpty() || dateOfBirth.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate date format
        if (!dateOfBirth.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) {
            Toast.makeText(this, "Invalid date format. Use YYYY-MM-DD", Toast.LENGTH_SHORT).show()
            return
        }

        val genderBtn = findViewById<RadioButton>(genderId)
        val gender = if (genderBtn.text == "Male") "M" else "F"

        val patientRequest = PatientRegistrationRequest(
            firstName = firstName,
            middleName = if (middleName.isEmpty()) null else middleName,
            lastName = lastName,
            gender = gender,
            dateOfBirth = dateOfBirth
        )

        lifecycleScope.launch {
            try {
                btnSubmit.isEnabled = false
                btnSubmit.text = "Registering..."

                val response = RetrofitInstance.api.registerPatient(patientRequest)

                if (response.isSuccessful) {
                    val patient = response.body()!!

                    Toast.makeText(
                        this@PatientRegistrationActivity,
                        "Patient registered successfully!",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Navigate to Vitals
                    startActivity(
                        Intent(
                            this@PatientRegistrationActivity,
                            VitalsActivity::class.java
                        ).apply {
                            putExtra("patient_id", patient.id)
                            putExtra("patient_name", patient.fullName)
                        }
                    )
                    finish()
                } else {
                    val error = response.errorBody()?.string() ?: "Registration failed"
                    Toast.makeText(
                        this@PatientRegistrationActivity,
                        "Error: $error",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@PatientRegistrationActivity,
                    "Network error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                btnSubmit.isEnabled = true
                btnSubmit.text = "Register Patient"
            }
        }
    }
}