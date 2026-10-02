package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.JobConfirmationEntity
import com.example.data.repository.ConfirmationRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DuplicateConfirmationTest {

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
    fun submittingDuplicateConfirmation_withSameClientId_isHandledIdempotently() = runBlocking {
        val fixedClientId = "IDEMPOTENT-TEST-${UUID.randomUUID()}"

        val confirmation1 = JobConfirmationEntity(
            clientConfirmationId = fixedClientId,
            workOrder = "WO 155704",
            operation = "0010",
            equipmentId = "121SC008",
            workStart = "2026-10-02T14:32:00",
            workFinish = "2026-10-02T16:05:00",
            elapsedMinutes = 93,
            actualWork = 1.5,
            workNote = "First attempt"
        )

        val result1 = repository.submitConfirmation(confirmation1)
        assertTrue(result1.isSuccess)
        val conf1 = result1.getOrThrow()

        // Resubmit with the same clientConfirmationId (e.g. repeated tap or network retry)
        val confirmation2 = confirmation1.copy(workNote = "Second attempt retry")
        val result2 = repository.submitConfirmation(confirmation2)
        assertTrue(result2.isSuccess)
        val conf2 = result2.getOrThrow()

        // Same remote confirmation ID returned
        assertEquals(conf1.remoteConfirmationId, conf2.remoteConfirmationId)

        // Verify only 1 record exists in Room for this clientConfirmationId
        val records = db.jobConfirmationDao().getConfirmationsForWorkOrder("WO 155704").first()
        val matchCount = records.count { it.clientConfirmationId == fixedClientId }
        assertEquals(1, matchCount)
    }
}
