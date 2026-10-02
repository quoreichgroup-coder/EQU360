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
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ActualWorkIndependenceTest {

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

    /**
     * Requirement: Work Start = 08:00, Work Finish = 12:00 (Elapsed 4.0h), Actual Work = 3.0h MUST be accepted.
     */
    @Test
    fun confirmation_withDifferentElapsedAndActualWork_isAcceptedAndPreserved() = runBlocking {
        val startStr = "2026-10-02T08:00:00"
        val finishStr = "2026-10-02T12:00:00"
        val elapsedMinutes = repository.calculateElapsedMinutes(startStr, finishStr)

        // Elapsed time is 240 minutes = 4.0 hours
        assertEquals(240, elapsedMinutes)
        val elapsedHours = elapsedMinutes / 60.0
        assertEquals(4.0, elapsedHours, 0.001)

        val enteredActualWork = 3.0 // Distinct from 4.0h elapsed

        val confirmation = JobConfirmationEntity(
            clientConfirmationId = UUID.randomUUID().toString(),
            workOrder = "WO 155704",
            operation = "0010",
            equipmentId = "121SC008",
            workStart = startStr,
            workFinish = finishStr,
            elapsedMinutes = elapsedMinutes,
            actualWork = enteredActualWork,
            actualWorkUnit = "H",
            workNote = "Work performed: 3.0 hours active wrench time out of 4.0 hours plant access window."
        )

        val result = repository.submitConfirmation(confirmation)
        assertTrue(result.isSuccess)

        val saved = result.getOrThrow()
        // Verify Actual Work was preserved and NOT overwritten by elapsed duration
        assertEquals(3.0, saved.actualWork, 0.001)
        assertEquals(240, saved.elapsedMinutes)
        assertNotEquals(saved.elapsedMinutes.toDouble() / 60.0, saved.actualWork)

        // Verify it was correctly stored in the local Room database
        val fromDb = db.jobConfirmationDao().getConfirmationByClientId(saved.clientConfirmationId)
        assertEquals(3.0, fromDb?.actualWork ?: 0.0, 0.001)
        assertEquals(240, fromDb?.elapsedMinutes)
    }
}
