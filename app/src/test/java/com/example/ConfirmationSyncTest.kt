package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.ConfirmationSyncStatus
import com.example.data.model.JobConfirmationEntity
import com.example.data.repository.ConfirmationRepository
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
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ConfirmationSyncTest {

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
    fun syncPendingConfirmations_synchronizesOfflineQueuedRecordsWhenNetworkRestored() = runBlocking {
        // Step 1: Submit while offline
        repository.remoteDataSource.isNetworkConnected = false

        val confirmationId = UUID.randomUUID().toString()
        val offlineConf = JobConfirmationEntity(
            clientConfirmationId = confirmationId,
            workOrder = "WO 155704",
            operation = "0010",
            equipmentId = "121SC008",
            workStart = "2026-10-02T14:32:00",
            workFinish = "2026-10-02T16:05:00",
            elapsedMinutes = 93,
            actualWork = 1.5,
            workNote = "Offline completed task."
        )

        val initialResult = repository.submitConfirmation(offlineConf)
        assertTrue(initialResult.isSuccess)

        val beforeSync = db.jobConfirmationDao().getConfirmationByClientId(confirmationId)
        assertEquals(ConfirmationSyncStatus.PENDING_SYNC.name, beforeSync?.syncStatus)

        // Step 2: Device reconnects to network
        repository.remoteDataSource.isNetworkConnected = true

        // Step 3: Trigger synchronization
        val syncedCount = repository.syncPendingConfirmations()
        assertTrue(syncedCount >= 1)

        // Step 4: Verify record was updated in database to SYNCED with remote ID
        val afterSync = db.jobConfirmationDao().getConfirmationByClientId(confirmationId)
        assertNotNull(afterSync)
        assertEquals(ConfirmationSyncStatus.SYNCED.name, afterSync?.syncStatus)
        assertNotNull(afterSync?.remoteConfirmationId)
        assertTrue(afterSync?.remoteConfirmationId?.startsWith("SAP-CONF-") == true)
    }
}
