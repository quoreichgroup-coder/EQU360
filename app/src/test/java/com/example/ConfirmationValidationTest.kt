package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.JobConfirmationEntity
import com.example.data.repository.ConfirmationRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ConfirmationValidationTest {

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
    fun confirmation_withBlankWorkOrder_isRejected() = runBlocking {
        val invalid = JobConfirmationEntity(
            clientConfirmationId = UUID.randomUUID().toString(),
            workOrder = "",
            operation = "0010",
            equipmentId = "121SC008",
            workStart = "2026-10-02T14:32:00",
            workFinish = "2026-10-02T16:05:00",
            elapsedMinutes = 93,
            actualWork = 1.5
        )
        val result = repository.submitConfirmation(invalid)
        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull()?.message?.contains("Work Order") == true)
    }

    @Test
    fun confirmation_withBlankOperation_isRejected() = runBlocking {
        val invalid = JobConfirmationEntity(
            clientConfirmationId = UUID.randomUUID().toString(),
            workOrder = "WO 155704",
            operation = "",
            equipmentId = "121SC008",
            workStart = "2026-10-02T14:32:00",
            workFinish = "2026-10-02T16:05:00",
            elapsedMinutes = 93,
            actualWork = 1.5
        )
        val result = repository.submitConfirmation(invalid)
        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull()?.message?.contains("Operation") == true)
    }

    @Test
    fun confirmation_withNegativeActualWork_isRejected() = runBlocking {
        val invalid = JobConfirmationEntity(
            clientConfirmationId = UUID.randomUUID().toString(),
            workOrder = "WO 155704",
            operation = "0010",
            equipmentId = "121SC008",
            workStart = "2026-10-02T14:32:00",
            workFinish = "2026-10-02T16:05:00",
            elapsedMinutes = 93,
            actualWork = -2.0
        )
        val result = repository.submitConfirmation(invalid)
        assertFalse(result.isSuccess)
        assertTrue(result.exceptionOrNull()?.message?.contains("negative") == true)
    }

    @Test
    fun confirmation_withValidPayload_isAccepted() = runBlocking {
        val valid = JobConfirmationEntity(
            clientConfirmationId = UUID.randomUUID().toString(),
            workOrder = "WO 155704",
            operation = "0010",
            equipmentId = "121SC008",
            equipmentName = "Vibrating Grizzly 121SC008",
            workStart = "2026-10-02T14:32:00",
            workFinish = "2026-10-02T16:05:00",
            elapsedMinutes = 93,
            actualWork = 1.5,
            workNote = "Replaced drive bolts and checked lubrication."
        )
        val result = repository.submitConfirmation(valid)
        assertTrue(result.isSuccess)
    }
}
