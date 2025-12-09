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

class GeneralAssessmentActivity : AppCompatActivity() {

    private lateinit var tvPatientName: TextView
    private lateinit var etVisitDate: EditText
    private lateinit var rgGeneralHealth: RadioGroup
    private lateinit var rgDrugUse: RadioGroup
    private lateinit var etComments: EditText
    private lateinit var btnSubmit: Button

    private lateinit var patientId: String
    private lateinit var patientName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_general_assessment)

        patientId = intent.getStringExtra("patient_id") ?: ""
        patientName = intent.getStringExtra("patient_name") ?: ""

        initViews()

        tvPatientName.text = patientName
        etVisitDate.setText(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))

        btnSubmit.setOnClickListener { submitAssessment() }

        etVisitDate.setOnClickListener { showDatePicker() }
    }

    private fun initViews() {
        tvPatientName = findViewById(R.id.tvPatientName)
        etVisitDate = findViewById(R.id.etVisitDate)
        rgGeneralHealth = findViewById(R.id.rgGeneralHealth)
        rgDrugUse = findViewById(R.id.rgDrugUse)
        etComments = findViewById(R.id.etComments)
        btnSubmit = findViewById(R.id.btnSubmit)
    }

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

    private fun submitAssessment() {
        val visitDate = etVisitDate.text.toString().trim()
        val comments = etComments.text.toString().trim()

        val generalHealthId = rgGeneralHealth.checkedRadioButtonId
        val drugUseId = rgDrugUse.checkedRadioButtonId

        if (visitDate.isEmpty() || generalHealthId == -1 || drugUseId == -1 || comments.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        val generalHealth = findViewById<RadioButton>(generalHealthId).text.toString()
        val currentlyUsingDrugs = findViewById<RadioButton>(drugUseId).text.toString() == "Yes"

        val assessment = GeneralAssessment(
            patientId = patientId,
            visitDate = visitDate,
            generalHealth = generalHealth,
            currentlyUsingDrugs = currentlyUsingDrugs,
            comments = comments
        )

        lifecycleScope.launch {
            try {
                btnSubmit.isEnabled = false
                val response = RetrofitInstance.api.submitGeneralAssessment(assessment)

                if (response.isSuccessful) {
                    Toast.makeText(
                        this@GeneralAssessmentActivity,
                        "Assessment submitted!",
                        Toast.LENGTH_SHORT
                    ).show()

                    startActivity(
                        android.content.Intent(
                            this@GeneralAssessmentActivity,
                            PatientListingActivity::class.java
                        )
                    )
                    finish()
                } else {
                    val error = response.errorBody()?.string() ?: "Submission failed"
                    Toast.makeText(
                        this@GeneralAssessmentActivity,
                        "Error: $error",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@GeneralAssessmentActivity,
                    "Network error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                btnSubmit.isEnabled = true
            }
        }
    }
}