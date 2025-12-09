//package com.patientbmi.app.data.repository
//
//import com.patientbmi.app.data.model.GeneralAssessment
//import com.patientbmi.app.data.model.OverweightAssessment
//
//class AssessmentRepository {
//
//    private val api = ApiClient.retrofit.create(`AssessmentApi.kt`::class.java)
//
////    // Correct function name
////    suspend fun submitGeneral(a: GeneralAssessment) =
////        api.submitGeneralAssessment(a)
//
//    suspend fun submitGeneral(a: GeneralAssessment) = api.submitGeneralAssessment(
//        mapOf(
//            "patient_id" to a.patient_id,        // ← Correct field name
//            "visit_date" to a.visit_date,
//            "general_health" to a.general_health,
//            "using_drugs" to a.using_drugs,
//            "comments" to a.comments
//        )
//    )
//
//
//    suspend fun submitOverweight(a: OverweightAssessment) =
//        api.submitOverweightAssessment(a)
//}
