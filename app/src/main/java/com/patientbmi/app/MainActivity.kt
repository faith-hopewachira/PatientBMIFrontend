package com.patientbmi.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.patientbmi.app.ui.patient_registration.PatientRegistrationActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Immediately go to Patient Registration (first screen)
        val intent = Intent(this, PatientRegistrationActivity::class.java)
        startActivity(intent)
        finish()
    }
}
