package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.VisualInspectionContext
import com.example.data.repository.EquipmentRepository
import com.example.data.service.MaintenanceAiServiceImpl
import com.example.ui.screens.visualinspection.VisualInspectionStep
import com.example.ui.screens.visualinspection.VisualInspectionViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class VisualInspectionTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: EquipmentRepository
    private val aiService = MaintenanceAiServiceImpl(delayMs = 0L)

    @Before
    fun setup() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        AppDatabase.seedInitialData(db)
        repository = EquipmentRepository(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun aiService_analyzesBearingVibration_returnsStructuredResultWithCautiousLanguage() = runBlocking {
        val eq = repository.getEquipmentSync("121SC008")!!
        val context = VisualInspectionContext(
            equipment = eq,
            imageUri = "content://photos/bearing_test.jpg",
            technicianObservation = "Abnormal vibration on the head pulley bearing. The bearing also feels hotter than usual."
        )

        val result = aiService.analyzeVisualInspection(context)

        assertTrue(result.possibleIssue.contains("Bearing", ignoreCase = true))
        assertTrue(result.observations.startsWith("Possible", ignoreCase = true))
        assertTrue(result.recommendedChecks.isNotEmpty())
        assertEquals("HIGH", result.suggestedPriority)
        assertNotNull(result.safetyWarning)
        assertTrue(result.safetyWarning!!.contains("Technician verification required"))
    }

    @Test
    fun visualInspectionViewModel_completeFieldWorkflow_createsNotificationAndInspection() = runBlocking {
        val vm = VisualInspectionViewModel("121SC008", repository, aiService)

        // Wait for equipment context to load
        var state = vm.uiState.first { !it.isLoading }
        assertEquals("121SC008", state.equipment?.equipmentId)
        assertEquals(VisualInspectionStep.CAPTURE_PHOTO, state.currentStep)

        // 1. Take Photo
        vm.onPhotoCaptured("content://photos/bearing_hot.jpg")
        state = vm.uiState.value
        assertEquals(VisualInspectionStep.PHOTO_PREVIEW_AND_OBSERVATION, state.currentStep)
        assertEquals("content://photos/bearing_hot.jpg", state.capturedImageUri)

        // 2. Add Observation
        vm.onObservationChanged("Abnormal vibration around drive bearing")
        state = vm.uiState.value
        assertEquals("Abnormal vibration around drive bearing", state.technicianObservation)

        // 3. Start AI Analysis (skip animation delay for instant test execution)
        vm.startAnalysis(skipAnimation = true)
        // Wait for analysis result
        state = vm.uiState.first { it.currentStep == VisualInspectionStep.RESULT_SUMMARY }
        assertNotNull(state.inspectionResult)
        assertTrue(state.draftTitle.isNotBlank())
        assertTrue(state.draftChecks.isNotEmpty())

        // 4. Proceed to Review & Confirm Notification
        vm.proceedToConfirmNotification()
        state = vm.uiState.value
        assertEquals(VisualInspectionStep.CONFIRM_NOTIFICATION, state.currentStep)

        // 5. Confirm & Create
        vm.confirmAndCreateNotification {}
        state = vm.uiState.first { it.currentStep == VisualInspectionStep.SUCCESS }
        assertNotNull(state.createdNotificationNumber)

        // Verify persisted to Room database
        val notifications = repository.getNotifications("121SC008").first()
        assertTrue(notifications.any { it.notificationNumber == state.createdNotificationNumber })

        val inspections = repository.getInspections("121SC008").first()
        assertTrue(inspections.any { it.inspectionType == "AI Visual Inspection" })
    }
}
