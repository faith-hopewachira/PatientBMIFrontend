//package com.patientbmi.app.ui.patient_listing
//
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//import com.patientbmi.app.R
//import com.patientbmi.app.data.model.PatientListingItem
//
//class PatientListingAdapter(
//    private var patients: List<PatientListingItem>,
//    private val onPatientClick: (PatientListingItem) -> Unit
//) : RecyclerView.Adapter<PatientListingAdapter.PatientViewHolder>() {
//
//    // Update the adapter's list
//    fun updateList(newList: List<PatientListingItem>) {
//        patients = newList
//        notifyDataSetChanged()
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.item_patient_row, parent, false)
//        return PatientViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
//        val patient = patients[position]
//        holder.tvName.text = patient.name
//        holder.tvAge.text = patient.age.toString()
//        holder.tvBmi.text = patient.bmi_status
//        holder.tvAssessment.text = patient.last_assessment_date ?: "N/A"
//
//        holder.itemView.setOnClickListener {
//            onPatientClick(patient)
//        }
//    }
//
//    override fun getItemCount(): Int = patients.size
//
//    class PatientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val tvName: TextView = itemView.findViewById(R.id.tvPatientName)
//        val tvAge: TextView = itemView.findViewById(R.id.tvAge)
//        val tvBmi: TextView = itemView.findViewById(R.id.tvLastBMI)
//        val tvAssessment: TextView = itemView.findViewById(R.id.tvLastAssessmentDate)
//    }
//}
