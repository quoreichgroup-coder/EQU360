package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DowntimeEventEntity
import com.example.data.model.EquipmentEntity
import com.example.data.model.InspectionEntity
import com.example.data.model.MaintenanceNotificationEntity
import com.example.data.model.MaintenancePlanEntity
import com.example.data.model.MaterialConsumptionEntity
import com.example.data.model.TechnicalDocumentEntity
import com.example.data.model.WorkOrderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EquipmentDao {
    @Query("SELECT * FROM equipments ORDER BY equipmentId ASC")
    fun getAllEquipments(): Flow<List<EquipmentEntity>>

    @Query("SELECT * FROM equipments WHERE UPPER(equipmentId) = UPPER(:equipmentId) LIMIT 1")
    fun getEquipmentById(equipmentId: String): Flow<EquipmentEntity?>

    @Query("SELECT * FROM equipments WHERE UPPER(equipmentId) = UPPER(:equipmentId) LIMIT 1")
    suspend fun getEquipmentByIdSync(equipmentId: String): EquipmentEntity?

    @Query("""
        SELECT * FROM equipments 
        WHERE equipmentId LIKE '%' || :query || '%' 
           OR name LIKE '%' || :query || '%' 
           OR functionalLocation LIKE '%' || :query || '%'
           OR area LIKE '%' || :query || '%'
        ORDER BY equipmentId ASC
    """)
    fun searchEquipments(query: String): Flow<List<EquipmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEquipments(equipments: List<EquipmentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEquipment(equipment: EquipmentEntity)
}

@Dao
interface WorkOrderDao {
    @Query("SELECT * FROM work_orders WHERE UPPER(equipmentId) = UPPER(:equipmentId) ORDER BY plannedDate DESC")
    fun getWorkOrdersForEquipment(equipmentId: String): Flow<List<WorkOrderEntity>>

    @Query("SELECT * FROM work_orders ORDER BY plannedDate DESC")
    fun getAllWorkOrders(): Flow<List<WorkOrderEntity>>

    @Query("SELECT * FROM work_orders WHERE orderNumber = :orderNumber LIMIT 1")
    fun getWorkOrder(orderNumber: String): Flow<WorkOrderEntity?>

    @Query("SELECT * FROM work_orders WHERE orderNumber = :orderNumber LIMIT 1")
    suspend fun getWorkOrderById(orderNumber: String): WorkOrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkOrders(orders: List<WorkOrderEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkOrder(order: WorkOrderEntity)

    @Update
    suspend fun updateWorkOrder(order: WorkOrderEntity)
}

@Dao
interface MaintenanceNotificationDao {
    @Query("SELECT * FROM maintenance_notifications WHERE UPPER(equipmentId) = UPPER(:equipmentId) ORDER BY creationDate DESC")
    fun getNotificationsForEquipment(equipmentId: String): Flow<List<MaintenanceNotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<MaintenanceNotificationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: MaintenanceNotificationEntity)
}

@Dao
interface MaintenancePlanDao {
    @Query("SELECT * FROM maintenance_plans WHERE UPPER(equipmentId) = UPPER(:equipmentId) ORDER BY nextPlannedDate ASC")
    fun getPlansForEquipment(equipmentId: String): Flow<List<MaintenancePlanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlans(plans: List<MaintenancePlanEntity>)

    @Update
    suspend fun updatePlan(plan: MaintenancePlanEntity)
}

@Dao
interface InspectionDao {
    @Query("SELECT * FROM inspections WHERE UPPER(equipmentId) = UPPER(:equipmentId) ORDER BY inspectionDate DESC")
    fun getInspectionsForEquipment(equipmentId: String): Flow<List<InspectionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInspections(inspections: List<InspectionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInspection(inspection: InspectionEntity)
}

@Dao
interface DowntimeDao {
    @Query("SELECT * FROM downtime_events WHERE UPPER(equipmentId) = UPPER(:equipmentId) ORDER BY startDateTime DESC")
    fun getDowntimesForEquipment(equipmentId: String): Flow<List<DowntimeEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDowntimes(events: List<DowntimeEventEntity>)
}

@Dao
interface MaterialConsumptionDao {
    @Query("SELECT * FROM material_consumptions WHERE UPPER(equipmentId) = UPPER(:equipmentId) ORDER BY consumptionDate DESC")
    fun getConsumptionsForEquipment(equipmentId: String): Flow<List<MaterialConsumptionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConsumptions(consumptions: List<MaterialConsumptionEntity>)
}

@Dao
interface TechnicalDocumentDao {
    @Query("SELECT * FROM technical_documents WHERE UPPER(equipmentId) = UPPER(:equipmentId) ORDER BY date DESC")
    fun getDocumentsForEquipment(equipmentId: String): Flow<List<TechnicalDocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocuments(documents: List<TechnicalDocumentEntity>)
}

@Dao
interface JobConfirmationDao {
    @Query("SELECT * FROM job_confirmations WHERE workOrder = :workOrder ORDER BY createdAt DESC")
    fun getConfirmationsForWorkOrder(workOrder: String): Flow<List<com.example.data.model.JobConfirmationEntity>>

    @Query("SELECT * FROM job_confirmations WHERE UPPER(equipmentId) = UPPER(:equipmentId) ORDER BY createdAt DESC")
    fun getConfirmationsForEquipment(equipmentId: String): Flow<List<com.example.data.model.JobConfirmationEntity>>

    @Query("SELECT * FROM job_confirmations ORDER BY createdAt DESC")
    fun getAllConfirmations(): Flow<List<com.example.data.model.JobConfirmationEntity>>

    @Query("SELECT * FROM job_confirmations WHERE syncStatus IN ('PENDING_SYNC', 'SYNC_FAILED') ORDER BY createdAt ASC")
    suspend fun getPendingSyncConfirmations(): List<com.example.data.model.JobConfirmationEntity>

    @Query("SELECT * FROM job_confirmations WHERE clientConfirmationId = :clientConfirmationId LIMIT 1")
    suspend fun getConfirmationByClientId(clientConfirmationId: String): com.example.data.model.JobConfirmationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfirmation(confirmation: com.example.data.model.JobConfirmationEntity)

    @Update
    suspend fun updateConfirmation(confirmation: com.example.data.model.JobConfirmationEntity)

    @Query("UPDATE job_confirmations SET syncStatus = :status, remoteConfirmationId = :remoteId, syncErrorMessage = :error WHERE clientConfirmationId = :clientConfirmationId")
    suspend fun updateSyncStatus(clientConfirmationId: String, status: String, remoteId: String? = null, error: String? = null)
}

@Dao
interface ActiveJobDao {
    @Query("SELECT * FROM active_jobs LIMIT 1")
    fun getActiveJob(): Flow<com.example.data.model.ActiveJobEntity?>

    @Query("SELECT * FROM active_jobs LIMIT 1")
    suspend fun getActiveJobSync(): com.example.data.model.ActiveJobEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setActiveJob(activeJob: com.example.data.model.ActiveJobEntity)

    @Query("DELETE FROM active_jobs")
    suspend fun clearActiveJob()
}
