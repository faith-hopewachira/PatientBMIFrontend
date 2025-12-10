package com.patientbmi.app.ui.vitals

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.patientbmi.app.R
import com.patientbmi.app.data.model.PatientVital
import com.patientbmi.app.data.remote.api.RetrofitInstance
import com.patientbmi.app.ui.assessment_general.GeneralAssessmentActivity
import com.patientbmi.app.ui.assessment_overweight.OverweightAssessmentActivity
import com.patientbmi.app.ui.patient_listing.PatientListingActivity
import com.patientbmi.app.utils.BaseToastHelper
import kotlinx.coroutines.*
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.*

class VitalsActivity : AppCompatActivity() {

    private lateinit var tvPatientName: TextView
    private lateinit var etHeight: EditText
    private lateinit var etWeight: EditText
    private lateinit var tvBmiValue: TextView
    private lateinit var tvBmiCategory: TextView
    private lateinit var btnCancel: Button
    private lateinit var btnSubmit: Button

    private lateinit var patientId: String
    private lateinit var patientName: String

    private val TAG = "VitalsActivity"
    private var submitJob: Job? = null
    private var activityDestroyed = false
    private val activityId = UUID.randomUUID().toString().substring(0, 8)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vitals)

        Log.d(TAG, "=== VitalsActivity [$activityId] onCreate ===")

        patientId = intent.getStringExtra("patient_id").orEmpty()
        patientName = intent.getStringExtra("patient_name").orEmpty()

        Log.d(TAG, "Patient ID: $patientId, Name: $patientName")

        if (patientId.isEmpty()) {
            Log.e(TAG, "No patient ID provided!")
            showToastSafe("Error: No patient data")
            finish()
            return
        }

        initViews()
        setupListeners()
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "=== VitalsActivity [$activityId] onStart ===")
        activityDestroyed = false
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "=== VitalsActivity [$activityId] onResume ===")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "=== VitalsActivity [$activityId] onPause ===")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "=== VitalsActivity [$activityId] onStop ===")
    }

    override fun onDestroy() {
        super.onDestroy()
        activityDestroyed = true
        submitJob?.cancel("Activity destroyed")
        BaseToastHelper.cancelCurrentToast()  // Add this line
    }

    override fun onBackPressed() {
        Log.d(TAG, "=== VitalsActivity [$activityId] onBackPressed ===")
        if (submitJob?.isActive == true) {
            Log.w(TAG, "Back pressed while submitting")
            showToastSafe("Please wait for submission to complete")
            return
        }
        super.onBackPressed()
    }

    private fun initViews() {
        tvPatientName = findViewById(R.id.tvPatientName)
        etHeight = findViewById(R.id.etHeight)
        etWeight = findViewById(R.id.etWeight)
        tvBmiValue = findViewById(R.id.tvBmiValue)
        tvBmiCategory = findViewById(R.id.tvBmiCategory)
        btnCancel = findViewById(R.id.btnCancel)
        btnSubmit = findViewById(R.id.btnSubmit)

        tvPatientName.text = "Patient: $patientName"
        updateBmiDisplay(0.0, "Enter height & weight")
    }

    private fun setupListeners() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                calculateAndDisplayBmi()
            }
        }

        etHeight.addTextChangedListener(textWatcher)
        etWeight.addTextChangedListener(textWatcher)

        btnSubmit.setOnClickListener {
            Log.d(TAG, "=== Submit button clicked [$activityId] ===")
            if (submitJob?.isActive == true) {
                Log.w(TAG, "Submission already in progress")
                showToastSafe("Submission in progress...")
                return@setOnClickListener
            }
            submitVitals()
        }

        btnCancel.setOnClickListener {
            Log.d(TAG, "=== Cancel button clicked [$activityId] ===")
            if (submitJob?.isActive == true) {
                Log.w(TAG, "Cancelling submission...")
                submitJob?.cancel("User cancelled")
                resetUIState()
                showToastSafe("Submission cancelled")
            }
            navigateToPatientListing()
        }
    }

    private fun navigateToPatientListing() {
        Log.d(TAG, "=== Navigating to patient listing [$activityId] ===")
        val intent = Intent(this, PatientListingActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun calculateAndDisplayBmi() {
        val heightText = etHeight.text.toString().trim()
        val weightText = etWeight.text.toString().trim()

        if (heightText.isEmpty() || weightText.isEmpty()) {
            updateBmiDisplay(0.0, "Enter height & weight")
            return
        }

        try {
            val height = heightText.toDouble()
            val weight = weightText.toDouble()

            if (height <= 0 || weight <= 0) {
                updateBmiDisplay(0.0, "Invalid values")
                return
            }

            val bmi = calculateBmi(height, weight)
            val category = getBmiCategory(bmi)
            updateBmiDisplay(bmi, category)

        } catch (e: NumberFormatException) {
            updateBmiDisplay(0.0, "Invalid number")
        }
    }

    private fun calculateBmi(heightCm: Double, weightKg: Double): Double {
        val heightM = heightCm / 100.0
        val bmi = weightKg / (heightM * heightM)
        return String.format("%.1f", bmi).toDouble()
    }

    private fun getBmiCategory(bmi: Double): String {
        return when {
            bmi < 18.5 -> "Underweight"
            bmi < 25 -> "Normal"
            bmi < 30 -> "Overweight"
            else -> "Obese"
        }
    }

    private fun updateBmiDisplay(bmi: Double, category: String) {
        runOnUiThread {
            if (activityDestroyed || isFinishing) {
                Log.w(TAG, "updateBmiDisplay called on destroyed activity")
                return@runOnUiThread
            }

            try {
                if (bmi > 0) {
                    tvBmiValue.text = String.format("BMI: %.1f", bmi)
                    tvBmiCategory.text = category

                    val color = when (category) {
                        "Underweight" -> getColor(android.R.color.holo_orange_light)
                        "Normal" -> getColor(android.R.color.holo_green_light)
                        "Overweight" -> getColor(android.R.color.holo_orange_dark)
                        "Obese" -> getColor(android.R.color.holo_red_light)
                        else -> getColor(android.R.color.black)
                    }
                    tvBmiCategory.setTextColor(color)
                } else {
                    tvBmiValue.text = "BMI: --"
                    tvBmiCategory.text = category
                    tvBmiCategory.setTextColor(getColor(android.R.color.darker_gray))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating BMI display: ${e.message}")
            }
        }
    }

    private fun submitVitals() {
        Log.d(TAG, "=== submitVitals called [$activityId] ===")

        val heightText = etHeight.text.toString().trim()
        val weightText = etWeight.text.toString().trim()

        if (heightText.isEmpty() || weightText.isEmpty()) {
            showToastSafe("Please enter height and weight")
            return
        }

        try {
            val height = heightText.toDouble()
            val weight = weightText.toDouble()

            if (height <= 0 || weight <= 0) {
                showToastSafe("Height and weight must be positive numbers")
                return
            }

            val bmi = calculateBmi(height, weight)
            val bmiCategory = getBmiCategory(bmi)
            val visitDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            Log.d(TAG, "Creating PatientVital: height=$height, weight=$weight, bmi=$bmi, category=$bmiCategory")

            val patientVital = PatientVital(
                patientId = patientId,
                visitDate = visitDate,
                heightCm = height,
                weightKg = weight,
                bmi = bmi,
                bmiStatus = bmiCategory
            )

            // Update UI state
            updateUIForSubmission(true)

            // Launch coroutine with timeout
            submitJob = lifecycleScope.launch {
                try {
                    Log.d(TAG, "=== Starting API call [$activityId] ===")

                    val response = withTimeout(15000) {
                        withContext(Dispatchers.IO) {
                            RetrofitInstance.api.submitVitals(patientVital)
                        }
                    }

                    Log.d(TAG, "=== API response received [$activityId] ===")
                    Log.d(TAG, "Success: ${response.isSuccessful}, Code: ${response.code()}")

                    if (response.isSuccessful) {
                        Log.d(TAG, "=== Vitals submitted successfully! [$activityId] ===")
                        showToastSafe("Vitals recorded successfully!")

                        val nextForm = if (bmi >= 25) "overweight" else "general"
                        Log.d(TAG, "BMI: $bmi, Next form: $nextForm")

                        // Navigate immediately without delay
                        navigateToNextForm(nextForm, bmi)

                    } else {
                        handleApiError(response)
                    }
                } catch (e: TimeoutCancellationException) {
                    handleNetworkError("Request timed out. Please try again.")
                } catch (e: SocketTimeoutException) {
                    handleNetworkError("Connection timeout. Please try again.")
                } catch (e: ConnectException) {
                    handleNetworkError("Cannot connect to server. Check your internet.")
                } catch (e: IOException) {
                    handleNetworkError("Network error. Please check connection.")
                } catch (e: CancellationException) {
                    Log.w(TAG, "=== Submission cancelled [$activityId]: ${e.message} ===")
                    // Don't show toast for cancellation
                } catch (e: Exception) {
                    Log.e(TAG, "=== Unexpected error [$activityId]: ${e.message}", e)
                    handleNetworkError("An unexpected error occurred")
                }
            }

        } catch (e: NumberFormatException) {
            Log.e(TAG, "Number format error: ${e.message}")
            showToastSafe("Please enter valid numbers")
            updateUIForSubmission(false)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in submitVitals: ${e.message}", e)
            showToastSafe("An unexpected error occurred")
            updateUIForSubmission(false)
        }
    }

    private suspend fun handleApiError(response: retrofit2.Response<*>) {
        withContext(Dispatchers.Main) {
            if (activityDestroyed || isFinishing) return@withContext

            val errorBody = response.errorBody()?.string()?.take(200)
            Log.e(TAG, "=== API Error [$activityId]: $errorBody ===")

            val errorMessage = when {
                response.code() == 401 -> "Session expired. Please login again."
                response.code() == 403 -> "Access denied."
                response.code() == 404 -> "Service not found."
                response.code() == 500 -> "Server error. Please try again later."
                response.code() == 503 -> "Service unavailable."
                errorBody?.contains("bmi", ignoreCase = true) == true ->
                    "BMI value error. Please check your values."
                errorBody?.contains("5 digits", ignoreCase = true) == true ->
                    "Value too precise. Please use whole numbers or 1 decimal place."
                else -> "Failed to submit vitals (Error ${response.code()})."
            }

            showToastSafe(errorMessage)
            updateUIForSubmission(false)
        }
    }

    private suspend fun handleNetworkError(message: String) {
        withContext(Dispatchers.Main) {
            if (activityDestroyed || isFinishing) return@withContext
            showToastSafe(message)
            updateUIForSubmission(false)
        }
    }

    private fun updateUIForSubmission(submitting: Boolean) {
        runOnUiThread {
            if (activityDestroyed || isFinishing) {
                Log.w(TAG, "updateUIForSubmission called on destroyed activity")
                return@runOnUiThread
            }

            try {
                if (submitting) {
                    btnSubmit.isEnabled = false
                    btnSubmit.text = "Submitting..."
                    btnCancel.isEnabled = false
                    btnCancel.text = "Cancel"
                } else {
                    btnSubmit.isEnabled = true
                    btnSubmit.text = "Submit Vitals"
                    btnCancel.isEnabled = true
                    btnCancel.text = "Cancel"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating UI state: ${e.message}")
            }
        }
    }

    private fun resetUIState() {
        updateUIForSubmission(false)
    }

    private fun navigateToNextForm(nextForm: String, bmi: Double) {
        Log.d(TAG, "=== navigateToNextForm [$activityId]: $nextForm, BMI: $bmi ===")

        runOnUiThread {
            if (activityDestroyed || isFinishing) {
                Log.w(TAG, "Activity already destroyed, skipping navigation")
                return@runOnUiThread
            }

            try {
                // Cancel any current toast before navigation
                BaseToastHelper.cancelCurrentToast()

                // Create intent based on next form
                val intent = when (nextForm) {
                    "overweight" -> {
                        Intent(this@VitalsActivity, OverweightAssessmentActivity::class.java).apply {
                            putExtra("patient_id", patientId)
                            putExtra("patient_name", patientName)
                            putExtra("bmi", bmi)
                        }
                    }
                    else -> {
                        Intent(this@VitalsActivity, GeneralAssessmentActivity::class.java).apply {
                            putExtra("patient_id", patientId)
                            putExtra("patient_name", patientName)
                            putExtra("bmi", bmi)
                        }
                    }
                }

                // Clear back stack
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)

                // Start activity
                startActivity(intent)
                finish()

            } catch (e: Exception) {
                Log.e(TAG, "=== Error navigating to next form [$activityId]: ${e.message}", e)
                showToastSafe("Error navigating to next screen")
                resetUIState()
            }
        }
    }

    private fun showToastSafe(message: String) {
        BaseToastHelper.showToastFromActivity(this, message)
    }
}