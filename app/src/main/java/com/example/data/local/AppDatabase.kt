package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.DowntimeEventEntity
import com.example.data.model.EquipmentEntity
import com.example.data.model.InspectionEntity
import com.example.data.model.MaintenanceNotificationEntity
import com.example.data.model.MaintenancePlanEntity
import com.example.data.model.MaterialConsumptionEntity
import com.example.data.model.TechnicalDocumentEntity
import com.example.data.model.WorkOrderEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        EquipmentEntity::class,
        WorkOrderEntity::class,
        MaintenanceNotificationEntity::class,
        MaintenancePlanEntity::class,
        InspectionEntity::class,
        DowntimeEventEntity::class,
        MaterialConsumptionEntity::class,
        TechnicalDocumentEntity::class,
        com.example.data.model.JobConfirmationEntity::class,
        com.example.data.model.ActiveJobEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun equipmentDao(): EquipmentDao
    abstract fun workOrderDao(): WorkOrderDao
    abstract fun maintenanceNotificationDao(): MaintenanceNotificationDao
    abstract fun maintenancePlanDao(): MaintenancePlanDao
    abstract fun inspectionDao(): InspectionDao
    abstract fun downtimeDao(): DowntimeDao
    abstract fun materialConsumptionDao(): MaterialConsumptionDao
    abstract fun technicalDocumentDao(): TechnicalDocumentDao
    abstract fun jobConfirmationDao(): JobConfirmationDao
    abstract fun activeJobDao(): ActiveJobDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "plantcare_maintenance.db"
                ).fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            seedInitialData(getInstance(context))
                        }
                    }

                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            val count = getInstance(context).equipmentDao().getEquipmentByIdSync("121SC008")
                            if (count == null) {
                                seedInitialData(getInstance(context))
                            }
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        suspend fun seedInitialData(db: AppDatabase) {
            db.equipmentDao().insertEquipments(DemoDataSeeder.equipments)
            db.workOrderDao().insertWorkOrders(DemoDataSeeder.workOrders)
            db.maintenanceNotificationDao().insertNotifications(DemoDataSeeder.notifications)
            db.maintenancePlanDao().insertPlans(DemoDataSeeder.maintenancePlans)
            db.inspectionDao().insertInspections(DemoDataSeeder.inspections)
            db.downtimeDao().insertDowntimes(DemoDataSeeder.downtimeEvents)
            db.materialConsumptionDao().insertConsumptions(DemoDataSeeder.spareParts)
            db.technicalDocumentDao().insertDocuments(DemoDataSeeder.documents)
            for (conf in DemoDataSeeder.initialConfirmations) {
                db.jobConfirmationDao().insertConfirmation(conf)
            }
        }
    }
}
