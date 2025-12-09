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

/**
 * Main activity for displaying and filtering the patient listing.
 *
 * This screen serves as the primary dashboard for healthcare providers to
 * view all registered patients, filter them by date, and see their current
 * BMI status at a glance.
 * Key Features:
 * - Date-based filtering for patient registration dates
 * - Real-time patient data loading from backend API
 * - Color-coded BMI status visualization
 * - Responsive RecyclerView with patient cards
 * - Loading states and error handling
 * Architecture:
 * - Activity: UI controller and lifecycle management
 * - RecyclerView: Efficient list rendering with ViewHolder pattern
 * - Coroutines: Asynchronous network operations
 * - Retrofit: API communication
 *
 * Layout: activity_patient_listing.xml
 */
class PatientListingActivity : AppCompatActivity() {

    // UI Component References
    private lateinit var etFilterDate: EditText
    private lateinit var btnFilter: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar

    // RecyclerView Adapter
    private lateinit var adapter: PatientListAdapter

    /**
     * Initializes the activity, sets up UI components, configures RecyclerView,
     * and loads initial patient data. Sets today's date as default filter.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_listing)

        // Initialize UI components
        initViews()

        // Set today's date as default filter (ISO 8601 format)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        etFilterDate.setText(today)

        // Configure event listeners
        btnFilter.setOnClickListener { loadPatients() }
        etFilterDate.setOnClickListener { showDatePicker() }

        // Initialize RecyclerView with adapter and layout manager
        adapter = PatientListAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Load initial patient data
        loadPatients()
    }

    /**
     * Binds XML layout components to Kotlin properties using findViewById.
     * Should be called after setContentView() in onCreate().
     */
    private fun initViews() {
        etFilterDate = findViewById(R.id.etFilterDate)
        btnFilter = findViewById(R.id.btnFilter)
        recyclerView = findViewById(R.id.recyclerView)
        progressBar = findViewById(R.id.progressBar)
    }

    /**
     * Opens a native Android date picker dialog with current date pre-selected.
     * Updates the filter date EditText with selected date in "yyyy-MM-dd" format.
     */
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

    /**
     * Loads patients from the backend API with optional date filtering.
     * Makes an asynchronous network call to fetch patient listing data.
     * Handles loading states, success responses, and error conditions.
     * Updates the RecyclerView adapter with new data on successful response.
     */
    private fun loadPatients() {
        val filterDate = etFilterDate.text.toString().trim()

        lifecycleScope.launch {
            try {
                // Show loading indicator
                progressBar.visibility = View.VISIBLE

                // Make API call with optional date filtering
                val response = if (filterDate.isNotEmpty()) {
                    RetrofitInstance.api.getPatientListing(fromDate = filterDate, toDate = filterDate)
                } else {
                    RetrofitInstance.api.getPatientListing()
                }

                // Handle API response
                if (response.isSuccessful) {
                    val patients = response.body() ?: emptyList()
                    adapter.submitList(patients)

                    // Provide feedback for empty results
                    if (patients.isEmpty()) {
                        Toast.makeText(
                            this@PatientListingActivity,
                            "No patients found",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    // Handle API error response
                    val error = response.errorBody()?.string() ?: "Error loading patients"
                    Toast.makeText(
                        this@PatientListingActivity,
                        "Error: $error",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                // Handle network or unexpected errors
                e.printStackTrace()
                Toast.makeText(
                    this@PatientListingActivity,
                    "Network error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                // Hide loading indicator
                progressBar.visibility = View.GONE
            }
        }
    }
}

/**
 * Manages the creation and binding of ViewHolder instances for patient data.
 * Implements efficient view recycling and data binding with color-coded
 * BMI status indicators.
 */
class PatientListAdapter : RecyclerView.Adapter<PatientListAdapter.ViewHolder>() {

    // Data source for the adapter
    private var patients: List<com.patientbmi.app.data.model.PatientListingItem> = emptyList()

    /**
     * Holds references to individual view components within a patient list item.
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // View references
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvAge: TextView = view.findViewById(R.id.tvAge)
        val tvBmiStatus: TextView = view.findViewById(R.id.tvBmiStatus)
        val tvLastAssessment: TextView = view.findViewById(R.id.tvLastAssessment)
    }

    /**
     * Inflates the patient list item layout and returns a new ViewHolder.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_patient_list, parent, false)
        return ViewHolder(view)
    }

    /**
     * Populates the view holder's views with data from the patients list.
     * Applies conditional formatting to BMI status with color coding.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val patient = patients[position]

        // Set patient name
        holder.tvName.text = patient.fullName

        // Set age (display "N/A" for invalid or zero age)
        holder.tvAge.text = if (patient.age > 0) patient.age.toString() else "N/A"

        // Set BMI status with color coding
        val bmiStatus = patient.lastBmiStatus
        holder.tvBmiStatus.text = bmiStatus

        // Apply color coding based on BMI category
        val bgColor = when (bmiStatus.lowercase(Locale.getDefault())) {
            "underweight" -> android.graphics.Color.parseColor("#FF9800")  // Orange
            "normal" -> android.graphics.Color.parseColor("#4CAF50")       // Green
            "overweight" -> android.graphics.Color.parseColor("#FF5722")   // Deep Orange
            "obese" -> android.graphics.Color.parseColor("#F44336")        // Red
            else -> android.graphics.Color.parseColor("#9E9E9E")           // Gray (default/unknown)
        }

        holder.tvBmiStatus.setBackgroundColor(bgColor)

        // Set last assessment date (or placeholder if none exists)
        val assessmentDate = patient.lastAssessmentDate ?: "No assessment"
        holder.tvLastAssessment.text = assessmentDate
    }

    /**
     * Returns the total number of items in the data set.
     */
    override fun getItemCount(): Int = patients.size

    /**
     * Updates the adapter's data set and notifies the RecyclerView.
     */
    fun submitList(newList: List<com.patientbmi.app.data.model.PatientListingItem>) {
        patients = newList
        notifyDataSetChanged()
    }
}
