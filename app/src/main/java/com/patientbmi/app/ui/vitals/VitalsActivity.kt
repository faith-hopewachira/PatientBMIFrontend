package com.patientbmi.app.ui.vitals

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * This screen is a critical component in the patient assessment workflow where
 * healthcare providers input height and weight measurements.
 * Workflow Logic:
 * 1. Receives patient data from registration or patient listing
 * 2. User enters height (cm) and weight (kg)
 * 3. BMI is calculated and displayed in real-time
 * 4. User submits data to backend API
 * 5. Based on BMI category, navigates to appropriate assessment form
 *
 * Layout: activity_vitals.xml
 */
class VitalsActivity : AppCompatActivity() {

    // UI Component References
    private lateinit var tvPatientName: TextView
    private lateinit var etHeight: EditText
    private lateinit var etWeight: EditText
    private lateinit var tvBmiValue: TextView
    private lateinit var tvBmiCategory: TextView
    private lateinit var btnSubmit: Button

    // Patient data passed from previous activity
    private lateinit var patientId: String
    private lateinit var patientName: String

    // Logging and async operation management
    private val TAG = "VitalsActivity"
    private var submitJob: Job? = null
    private val handler = Handler(Looper.getMainLooper())

    /**
     * Initializes the activity, validates incoming patient data,
     * sets up UI components, and configures event listeners.
     * Data Validation:
     * - Ensures patient_id and patient_name are provided
     * - Logs diagnostic information for debugging
     * - Finishes activity if patient data is missing
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vitals)

        Log.d(TAG, "=== VitalsActivity onCreate ===")

        // Extract patient data from Intent
        patientId = intent.getStringExtra("patient_id").orEmpty()
        patientName = intent.getStringExtra("patient_name").orEmpty()

        Log.d(TAG, "Patient ID: $patientId, Name: $patientName")

        // Validate essential patient data
        if (patientId.isEmpty()) {
            Log.e(TAG, "No patient ID provided!")
            showToast("Error: No patient data")
            finish()
            return
        }

        // Initialize UI and event handlers
        initViews()
        setupListeners()
    }

    /**
     * Cleans up resources to prevent memory leaks:
     * - Cancels any ongoing API calls
     * - Removes pending handler callbacks
     */
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "=== VitalsActivity onDestroy ===")
        // Cancel any ongoing API call
        submitJob?.cancel()
        // Remove any pending callbacks
        handler.removeCallbacksAndMessages(null)
    }

    /**
     * Handles back button press with logging.
     */
    override fun onBackPressed() {
        Log.d(TAG, "onBackPressed called")
        super.onBackPressed()
    }

    /**
     * Binds XML layout components to Kotlin properties and sets
     * initial display values for BMI fields.
     */
    private fun initViews() {
        tvPatientName = findViewById(R.id.tvPatientName)
        etHeight = findViewById(R.id.etHeight)
        etWeight = findViewById(R.id.etWeight)
        tvBmiValue = findViewById(R.id.tvBmiValue)
        tvBmiCategory = findViewById(R.id.tvBmiCategory)
        btnSubmit = findViewById(R.id.btnSubmit)

        // Set patient name for context
        tvPatientName.text = "Patient: $patientName"

        // Initialize BMI display with placeholder values
        updateBmiDisplay(0.0, "Enter height & weight")
    }

    private fun setupListeners() {
        // Calculate BMI whenever height or weight changes
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
            Log.d(TAG, "Submit button clicked")
            submitVitals()
        }
    }

    /**
     * Calculates and displays BMI based on current input values.
     */
    private fun calculateAndDisplayBmi() {
        val heightText = etHeight.text.toString().trim()
        val weightText = etWeight.text.toString().trim()

        // Handle empty inputs
        if (heightText.isEmpty() || weightText.isEmpty()) {
            updateBmiDisplay(0.0, "Enter height & weight")
            return
        }

        try {
            val height = heightText.toDouble()
            val weight = weightText.toDouble()

            // Validate positive values
            if (height <= 0 || weight <= 0) {
                updateBmiDisplay(0.0, "Invalid values")
                return
            }

            // Calculate BMI and category
            val bmi = calculateBmi(height, weight)
            val category = getBmiCategory(bmi)

            updateBmiDisplay(bmi, category)

        } catch (e: NumberFormatException) {
            updateBmiDisplay(0.0, "Invalid number")
        }
    }

    /**
     * Calculates BMI from height (cm) and weight (kg).
     * Formula: BMI = weight(kg) / (height(m)²)
     */
    private fun calculateBmi(heightCm: Double, weightKg: Double): Double {
        val heightM = heightCm / 100.0
        val bmi = weightKg / (heightM * heightM)
        // Round to 1 decimal place
        return String.format("%.1f", bmi).toDouble()
    }

    /**
     * Determines WHO BMI category based on BMI value.
     */
    private fun getBmiCategory(bmi: Double): String {
        return when {
            bmi < 18.5 -> "Underweight"
            bmi < 25 -> "Normal"
            bmi < 30 -> "Overweight"
            else -> "Obese"
        }
    }

    /**
     * Updates the BMI display with calculated values and color coding.
     */
    private fun updateBmiDisplay(bmi: Double, category: String) {
        runOnUiThread {
            if (bmi > 0) {
                tvBmiValue.text = String.format("BMI: %.1f", bmi)
                tvBmiCategory.text = category

                // Apply color coding based on category
                val color = when (category) {
                    "Underweight" -> resources.getColor(android.R.color.holo_orange_light, null)
                    "Normal" -> resources.getColor(android.R.color.holo_green_light, null)
                    "Overweight" -> resources.getColor(android.R.color.holo_orange_dark, null)
                    "Obese" -> resources.getColor(android.R.color.holo_red_light, null)
                    else -> resources.getColor(android.R.color.black, null)
                }
                tvBmiCategory.setTextColor(color)

            } else {
                // Show placeholder values
                tvBmiValue.text = "BMI: --"
                tvBmiCategory.text = category
                tvBmiCategory.setTextColor(resources.getColor(android.R.color.darker_gray, null))
            }
        }
    }

    /**
     * Validates and submits vital measurements to the backend API.
     * Performs comprehensive validation and makes an asynchronous API call.
     */
    private fun submitVitals() {
        Log.d(TAG, "=== submitVitals called ===")

        val heightText = etHeight.text.toString().trim()
        val weightText = etWeight.text.toString().trim()

        // Basic validation
        if (heightText.isEmpty() || weightText.isEmpty()) {
            showToast("Please enter height and weight")
            return
        }

        try {
            val height = heightText.toDouble()
            val weight = weightText.toDouble()

            // Range validation
            if (height <= 0 || weight <= 0) {
                showToast("Height and weight must be positive numbers")
                return
            }

            // Calculate BMI and category
            val bmi = calculateBmi(height, weight)
            val bmiCategory = getBmiCategory(bmi)

            // Format current date for visit
            val visitDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            Log.d(TAG, "Creating PatientVital: height=$height, weight=$weight, bmi=$bmi, category=$bmiCategory")

            // Create data transfer object
            val patientVital = PatientVital(
                patientId = patientId,
                visitDate = visitDate,
                heightCm = height,
                weightKg = weight,
                bmi = bmi,
                bmiStatus = bmiCategory
            )

            // Update UI state to prevent multiple submissions
            btnSubmit.isEnabled = false
            btnSubmit.text = "Submitting..."

            // Launch coroutine for asynchronous API call
            submitJob = lifecycleScope.launch {
                try {
                    Log.d(TAG, "Starting API call...")

                    val response = RetrofitInstance.api.submitVitals(patientVital)

                    Log.d(TAG, "API response received. Success: ${response.isSuccessful}")
                    Log.d(TAG, "Response code: ${response.code()}")

                    if (response.isSuccessful) {
                        Log.d(TAG, "Vitals submitted successfully!")

                        runOnUiThread {
                            showToast("Vitals recorded successfully!")

                            // Determine next assessment form based on BMI
                            val nextForm = if (bmi >= 25) "overweight" else "general"
                            Log.d(TAG, "BMI: $bmi, Next form: $nextForm")

                            // Add delay for better UX before navigation
                            handler.postDelayed({
                                navigateToNextForm(nextForm, bmi)
                            }, 500)
                        }

                    } else {
                        // Handle API error response
                        val errorBody = response.errorBody()?.string()
                        Log.e(TAG, "API Error: $errorBody")
                        Log.e(TAG, "Error code: ${response.code()}")

                        runOnUiThread {
                            btnSubmit.isEnabled = true
                            btnSubmit.text = "Submit Vitals"

                            // User-friendly error messages
                            val errorMessage = if (errorBody?.contains("bmi") == true) {
                                "BMI value error. Please check your values."
                            } else if (errorBody?.contains("5 digits") == true) {
                                "Value too precise. Please use whole numbers or 1 decimal place."
                            } else {
                                "Failed to submit vitals. Please try again."
                            }

                            showToast(errorMessage)
                        }
                    }
                } catch (e: Exception) {
                    // Handle network or unexpected errors
                    Log.e(TAG, "Network/Exception error: ${e.message}", e)

                    runOnUiThread {
                        btnSubmit.isEnabled = true
                        btnSubmit.text = "Submit Vitals"
                        showToast("Network error: ${e.message ?: "Unknown error"}")
                    }
                }
            }

        } catch (e: NumberFormatException) {
            Log.e(TAG, "Number format error: ${e.message}")
            showToast("Please enter valid numbers")
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error in submitVitals: ${e.message}", e)
            showToast("An unexpected error occurred")
            btnSubmit.isEnabled = true
            btnSubmit.text = "Submit Vitals"
        }
    }

    /**
     * Navigates to the appropriate assessment form based on BMI category.
     * Navigation Logic:
     * - BMI < 25: GeneralAssessmentActivity
     * - BMI ≥ 25: OverweightAssessmentActivity
     */
    private fun navigateToNextForm(nextForm: String, bmi: Double) {
        Log.d(TAG, "navigateToNextForm: $nextForm, BMI: $bmi")

        try {
            // Safety check to prevent navigation on destroyed activity
            if (isFinishing || isDestroyed) {
                Log.w(TAG, "Activity is finishing/destroyed, not navigating")
                return
            }

            when (nextForm) {
                "overweight" -> {
                    Log.d(TAG, "Starting OverweightAssessmentActivity")
                    val intent = Intent(this, OverweightAssessmentActivity::class.java)
                    intent.putExtra("patient_id", patientId)
                    intent.putExtra("patient_name", patientName)
                    intent.putExtra("bmi", bmi)
                    startActivity(intent)
                }
                "general" -> {
                    Log.d(TAG, "Starting GeneralAssessmentActivity")
                    val intent = Intent(this, GeneralAssessmentActivity::class.java)
                    intent.putExtra("patient_id", patientId)
                    intent.putExtra("patient_name", patientName)
                    intent.putExtra("bmi", bmi)
                    startActivity(intent)
                }
            }

            // Finish current activity after successful navigation
            Log.d(TAG, "Finishing VitalsActivity")
            finish()

        } catch (e: Exception) {
            Log.e(TAG, "Error in navigateToNextForm: ${e.message}", e)
            // Safe finish even if navigation fails
            if (!isFinishing && !isDestroyed) {
                finish()
            }
        }
    }


    private fun showToast(message: String) {
        runOnUiThread {
            if (!isFinishing && !isDestroyed) {
                Toast.makeText(this@VitalsActivity, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun testWithoutApi() {
        Log.d(TAG, "=== Testing without API call ===")

        btnSubmit.isEnabled = false
        btnSubmit.text = "Testing..."

        // Simulate API delay
        handler.postDelayed({
            runOnUiThread {
                showToast("Test successful (no API call)")
                btnSubmit.isEnabled = true
                btnSubmit.text = "Submit Vitals"

                // Try to finish after delay
                handler.postDelayed({
                    Log.d(TAG, "Finishing after test")
                    if (!isFinishing && !isDestroyed) {
                        finish()
                    }
                }, 1000)
            }
        }, 2000)
    }
}
