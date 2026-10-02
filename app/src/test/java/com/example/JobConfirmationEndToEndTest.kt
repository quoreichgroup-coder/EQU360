package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.ConfirmationSyncStatus
import com.example.data.model.JobCompletionType
import com.example.data.repository.ConfirmationRepository
import com.example.data.repository.EquipmentRepository
import com.example.ui.screens.confirmation.ConfirmationViewModel
import com.example.ui.screens.mywork.MyWorkTab
import com.example.ui.screens.mywork.MyWorkViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class JobConfirmationEndToEndTest {

    private lateinit var db: AppDatabase
    private lateinit var equipmentRepo: EquipmentRepository
    private lateinit var confirmationRepo: ConfirmationRepository

    @Before
    fun setup() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        AppDatabase.seedInitialData(db)
        equipmentRepo = EquipmentRepository(db)
        confirmationRepo = ConfirmationRepository(db)
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun completeJobConfirmationCUJ_onlineScenario() = runBlocking {
        // 1. Open PlantCare -> My Work
        val myWorkVm = MyWorkViewModel(confirmationRepo, equipmentRepo)
        val initialOrders = equipmentRepo.getAllWorkOrders().first()
        val targetWo = initialOrders.first { it.orderNumber == "WO 155704" }
        assertEquals("REL", targetWo.status)

        // 2. Start Job
        myWorkVm.startJob(targetWo)
        val activeJob = confirmationRepo.getActiveJobSync()
        assertNotNull(activeJob)
        assertEquals("WO 155704", activeJob?.workOrder)
        assertEquals("121SC008", activeJob?.equipmentId)

        // 3. Confirm Work
        val confirmVm = ConfirmationViewModel("WO 155704", confirmationRepo, equipmentRepo)
        confirmVm.updateStartTime("14:32")
        confirmVm.updateFinishTime("16:05")

        // 4. Verify elapsed time calculation (14:32 to 16:05 is 93 minutes = 1h 33min)
        assertEquals(93, confirmVm.uiState.value.elapsedMinutes)
        assertEquals("1 h 33 min", confirmVm.uiState.value.formattedElapsedTime)

        // 5. Enter Actual Work = 1.5 h (independent from elapsed time)
        confirmVm.updateActualWork("1.5")
        confirmVm.updateActualWorkUnit("H")

        // 6. Add voice note / work performed
        confirmVm.toggleTaskPerformed("Inspection completed")
        confirmVm.toggleTaskPerformed("Lubrication completed")
        confirmVm.updateWorkNote("Inspected head and tail pulley bearings. Lubrication completed.")

        // 7. Work Completed = YES (Final)
        confirmVm.setJobComplete(true)

        // 8. Review
        confirmVm.setReviewMode(true)
        assertTrue(confirmVm.uiState.value.isReviewMode)

        // 9. Confirm & Save -> Sync API
        confirmVm.submitConfirmation()
        val finalState = confirmVm.uiState.value
        assertTrue(finalState.isSuccess)
        assertNotNull(finalState.submittedResult)
        assertEquals(ConfirmationSyncStatus.SYNCED.name, finalState.submittedResult?.syncStatus)

        // 10. Confirmation History updated in Database
        val history = confirmationRepo.getConfirmationsForWorkOrder("WO 155704").first()
        assertTrue(history.isNotEmpty())
        assertEquals(1.5, history.first().actualWork, 0.001)

        // 11. Active job cleared
        assertNull(confirmationRepo.getActiveJobSync())

        // 12. Work Order status updated to Confirmed (CNF)
        val updatedWo = equipmentRepo.getWorkOrders("121SC008").first().find { it.orderNumber == "WO 155704" }
        assertEquals("CNF", updatedWo?.status)
    }

    @Test
    fun completeJobConfirmationCUJ_offlineScenario() = runBlocking {
        // Offline field tunnel scenario
        confirmationRepo.remoteDataSource.isNetworkConnected = false

        val confirmVm = ConfirmationViewModel("WO 155704", confirmationRepo, equipmentRepo)
        confirmVm.updateStartTime("08:00")
        confirmVm.updateFinishTime("12:00")

        // Elapsed = 240m (4h), Actual Work = 3.0h
        assertEquals(240, confirmVm.uiState.value.elapsedMinutes)
        confirmVm.updateActualWork("3.0")

        confirmVm.setJobComplete(true)
        confirmVm.setReviewMode(true)
        confirmVm.submitConfirmation()

        val finalState = confirmVm.uiState.value
        assertTrue(finalState.isSuccess)
        assertEquals(ConfirmationSyncStatus.PENDING_SYNC.name, finalState.submittedResult?.syncStatus)

        // Stored safely in Room
        val fromDb = confirmationRepo.getConfirmationsForWorkOrder("WO 155704").first()
        val latest = fromDb.first()
        assertEquals(3.0, latest.actualWork, 0.001)
        assertEquals(ConfirmationSyncStatus.PENDING_SYNC.name, latest.syncStatus)

        // Reconnect to network and sync
        confirmationRepo.remoteDataSource.isNetworkConnected = true
        val synced = confirmationRepo.syncPendingConfirmations()
        assertTrue(synced >= 1)

        val syncedDb = confirmationRepo.getConfirmationsForWorkOrder("WO 155704").first().first()
        assertEquals(ConfirmationSyncStatus.SYNCED.name, syncedDb.syncStatus)
    }
}
