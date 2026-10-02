package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.MaintenanceNotificationEntity
import com.example.data.repository.EquipmentRepository
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
class EquipmentLookupTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: EquipmentRepository

    @Before
    fun createDb() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        AppDatabase.seedInitialData(db)
        repository = EquipmentRepository(db)
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun getEquipment_121SC008_returnsCorrectDetails() = runBlocking {
        val eq = repository.getEquipmentSync("121SC008")
        assertNotNull(eq)
        assertEquals("121SC008", eq?.equipmentId)
        assertEquals("VIBRATING GRIZZLY 121SC008", eq?.name)
        assertEquals("BI-PLN-CRU/CRS-003", eq?.functionalLocation)
        assertEquals("MECHANICAL", eq?.workCenter)
        assertEquals("HIGH", eq?.criticality)
        assertEquals("ACTIVE", eq?.status)
    }

    @Test
    fun getEquipment360_aggregatesAllDataOffline() = runBlocking {
        val data = repository.getEquipment360("121SC008").first()
        assertNotNull(data.equipment)
        assertEquals("121SC008", data.equipment?.equipmentId)
        assertTrue(data.isOfflineAvailable)
        assertTrue(data.workOrders.isNotEmpty())
        assertTrue(data.notifications.isNotEmpty())
        assertTrue(data.plans.isNotEmpty())
        assertTrue(data.inspections.isNotEmpty())
        assertTrue(data.downtimes.isNotEmpty())
        assertTrue(data.spareParts.isNotEmpty())
        assertTrue(data.documents.isNotEmpty())
        assertTrue(data.summary.openWorkOrdersCount > 0)
    }

    @Test
    fun getEquipment_unknownId_returnsNull() = runBlocking {
        val eq = repository.getEquipmentSync("NON_EXISTENT_ID")
        assertNull(eq)
    }

    @Test
    fun getWorkOrders_121SC008_containsSapStatuses() = runBlocking {
        val workOrders = repository.getWorkOrders("121SC008").first()
        assertTrue(workOrders.isNotEmpty())
        val statuses = workOrders.map { it.status }.toSet()
        assertTrue(statuses.contains("REL"))
        assertTrue(statuses.contains("CRTD"))
    }

    @Test
    fun searchEquipments_byFunctionalLocation_returnsMatch() = runBlocking {
        val results = repository.searchEquipments("BI-PLN-CRU").first()
        assertTrue(results.isNotEmpty())
        assertTrue(results.any { it.equipmentId == "121SC008" })
    }

    @Test
    fun createNotification_persistsInLocalDatabase() = runBlocking {
        val newNotif = MaintenanceNotificationEntity(
            notificationNumber = "NOTIF-TEST-999",
            equipmentId = "121SC008",
            description = "Test oil leakage detected on test bench",
            priority = "HIGH",
            status = "OPEN",
            creationDate = "2026-10-02 07:15",
            reportedBy = "Test Lead",
            damage = "Shaft seal leak",
            cause = "Thermal expansion",
            observation = "Oil dripping at 2 drops per min"
        )
        repository.createNotification(newNotif)

        val notifications = repository.getNotifications("121SC008").first()
        assertTrue(notifications.any { it.notificationNumber == "NOTIF-TEST-999" })
    }

    @Test
    fun updateWorkOrderStatus_updatesDatabaseAndFlow() = runBlocking {
        val firstWo = repository.getWorkOrders("121SC008").first().first()
        repository.updateWorkOrderStatus(firstWo, "TECO")

        val updatedWo = repository.getWorkOrders("121SC008").first().first { it.orderNumber == firstWo.orderNumber }
        assertEquals("TECO", updatedWo.status)
    }
}
