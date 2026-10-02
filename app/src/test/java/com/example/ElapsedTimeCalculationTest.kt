package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.repository.ConfirmationRepository
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ElapsedTimeCalculationTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: ConfirmationRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = ConfirmationRepository(db)
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun calculateElapsedMinutes_from1432to1605_returns93Minutes() {
        // 14:32 to 16:05 is 1 hour 33 minutes = 93 minutes
        val elapsed = repository.calculateElapsedMinutes("14:32", "16:05")
        assertEquals(93, elapsed)
    }

    @Test
    fun calculateElapsedMinutes_from0800to1200_returns240Minutes() {
        // 08:00 to 12:00 is 4 hours = 240 minutes
        val elapsed = repository.calculateElapsedMinutes("08:00", "12:00")
        assertEquals(240, elapsed)
    }

    @Test
    fun calculateElapsedMinutes_withIsoTimestamps_calculatesAccurately() {
        val elapsed = repository.calculateElapsedMinutes("2026-10-02T14:32:00", "2026-10-02T16:05:00")
        assertEquals(93, elapsed)
    }

    @Test
    fun calculateElapsedMinutes_whenStartEqualsFinish_returnsZero() {
        val elapsed = repository.calculateElapsedMinutes("14:32", "14:32")
        assertEquals(0, elapsed)
    }
}
