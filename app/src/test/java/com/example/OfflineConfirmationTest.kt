package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.ConfirmationSyncStatus
import com.example.data.model.JobCompletionType
import com.example.data.model.JobConfirmationEntity
import com.example.data.repository.ConfirmationRepository
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
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class OfflineConfirmationTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: ConfirmationRepository

    @Before
    fun setup() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        AppDatabase.seedInitialData(db)
        repository = ConfirmationRepository(db)
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun submitConfirmation_whileOffline_storesSafelyInRoomWithPendingSync() = runBlocking {
        // Start a job first
        repository.startJob(
            workOrder = "WO 155704",
            equipmentId = "121SC008",
            equipmentName = "Vibrating Grizzly 121SC008",
            functionalLocation = "BI-PLN-CRU/CRS-003",
            workCenter = "MECHANICAL"
        )
        assertNotNull(repository.getActiveJobSync())

        // Simulate device offline in plant tunnel
        repository.remoteDataSource.isNetworkConnected = false

        val confirmation = JobConfirmationEntity(
            clientConfirmationId = UUID.randomUUID().toString(),
            workOrder = "WO 155704",
            operation = "0010",
            equipmentId = "121SC008",
            equipmentName = "Vibrating Grizzly 121SC008",
            functionalLocation = "BI-PLN-CRU/CRS-003",
            workCenter = "MECHANICAL",
            technicianId = "A. Sawadogo",
            workStart = "2026-10-02T14:32:00",
            workFinish = "2026-10-02T16:05:00",
            elapsedMinutes = 93,
            actualWork = 1.5,
            completionType = JobCompletionType.FINAL.name,
            workNote = "Lubrication and bolt torque complete."
        )

        val result = repository.submitConfirmation(confirmation)
        // Submitting while offline still succeeds locally
        assertTrue(result.isSuccess)

        val saved = result.getOrThrow()
        assertEquals(ConfirmationSyncStatus.PENDING_SYNC.name, saved.syncStatus)

        // Verify stored in Room database
        val fromDb = db.jobConfirmationDao().getConfirmationByClientId(saved.clientConfirmationId)
        assertNotNull(fromDb)
        assertEquals(ConfirmationSyncStatus.PENDING_SYNC.name, fromDb?.syncStatus)
        assertEquals(1.5, fromDb?.actualWork ?: 0.0, 0.001)

        // Verify active job was cleared
        assertNull(repository.getActiveJobSync())

        // Verify Work Order status in Room was updated to CNF
        val wo = db.workOrderDao().getWorkOrderById("WO 155704")
        assertEquals("CNF", wo?.status)
    }
}
