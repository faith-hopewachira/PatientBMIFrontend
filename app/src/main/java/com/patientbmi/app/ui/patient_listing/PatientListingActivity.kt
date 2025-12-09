package com.patientbmi.app.ui.patient_listing

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.patientbmi.app.R
import com.patientbmi.app.data.remote.api.RetrofitInstance
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class PatientListingActivity : AppCompatActivity() {

    private lateinit var etFilterDate: EditText
    private lateinit var btnFilter: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar

    private lateinit var adapter: PatientListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_listing)

        initViews()

        // Set today's date as default filter
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        etFilterDate.setText(today)

        btnFilter.setOnClickListener { loadPatients() }
        etFilterDate.setOnClickListener { showDatePicker() }

        // Initialize RecyclerView
        adapter = PatientListAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        loadPatients()
    }

    private fun initViews() {
        etFilterDate = findViewById(R.id.etFilterDate)
        btnFilter = findViewById(R.id.btnFilter)
        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(year, month, day)
                etFilterDate.setText(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun loadPatients() {
        val filterDate = etFilterDate.text.toString().trim()

        lifecycleScope.launch {
            try {
                progressBar.visibility = View.VISIBLE

                val response = if (filterDate.isNotEmpty()) {
                    RetrofitInstance.api.getPatientListing(fromDate = filterDate, toDate = filterDate)
                } else {
                    RetrofitInstance.api.getPatientListing()
                }

                if (response.isSuccessful) {
                    val patients = response.body() ?: emptyList()
                    adapter.submitList(patients)

                    if (patients.isEmpty()) {
                        Toast.makeText(
                            this@PatientListingActivity,
                            "No patients found",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    val error = response.errorBody()?.string() ?: "Error loading patients"
                    Toast.makeText(
                        this@PatientListingActivity,
                        "Error: $error",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@PatientListingActivity,
                    "Network error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                progressBar.visibility = View.GONE
            }
        }
    }
}

class PatientListAdapter : RecyclerView.Adapter<PatientListAdapter.ViewHolder>() {

    private var patients: List<com.patientbmi.app.data.model.PatientListingItem> = emptyList()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvAge: TextView = view.findViewById(R.id.tvAge)
        val tvBmiStatus: TextView = view.findViewById(R.id.tvBmiStatus)
        val tvLastAssessment: TextView = view.findViewById(R.id.tvLastAssessment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_patient_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val patient = patients[position]

        // Set patient name
        holder.tvName.text = patient.fullName

        // Set age with label (label is in XML)
        holder.tvAge.text = if (patient.age > 0) patient.age.toString() else "N/A"

        // Set BMI status (label is in XML)
        val bmiStatus = patient.lastBmiStatus
        holder.tvBmiStatus.text = bmiStatus

        // Color code the BMI status
        val bgColor = when (bmiStatus.lowercase(Locale.getDefault())) {
            "underweight" -> android.graphics.Color.parseColor("#FF9800")  // Orange
            "normal" -> android.graphics.Color.parseColor("#4CAF50")       // Green
            "overweight" -> android.graphics.Color.parseColor("#FF5722")   // Deep Orange
            "obese" -> android.graphics.Color.parseColor("#F44336")        // Red
            else -> android.graphics.Color.parseColor("#9E9E9E")           // Gray
        }

        holder.tvBmiStatus.setBackgroundColor(bgColor)

        // Set last assessment date (label is in XML)
        val assessmentDate = patient.lastAssessmentDate ?: "No assessment"
        holder.tvLastAssessment.text = assessmentDate
    }

    override fun getItemCount(): Int = patients.size

    fun submitList(newList: List<com.patientbmi.app.data.model.PatientListingItem>) {
        patients = newList
        notifyDataSetChanged()
    }
}