package com.example

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.local.AppDatabase
import com.example.data.repository.ConfirmationRepository
import com.example.data.repository.EquipmentRepository
import com.example.ui.screens.confirmation.ConfirmWorkScreen
import com.example.ui.screens.confirmation.ConfirmationViewModel
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.equipment360.Equipment360Screen
import com.example.ui.screens.equipment360.Equipment360ViewModel
import com.example.ui.screens.mywork.MyWorkScreen
import com.example.ui.screens.mywork.MyWorkViewModel
import com.example.ui.screens.scanner.ScannerScreen
import com.example.ui.screens.visualinspection.VisualInspectionScreen
import com.example.ui.screens.visualinspection.VisualInspectionViewModel

object Destinations {
    const val DASHBOARD = "dashboard"
    const val SCANNER = "scanner"
    const val EQUIPMENT_360 = "equipment360/{equipmentId}"
    const val VISUAL_INSPECTION = "visualInspection/{equipmentId}"
    const val MY_WORK = "myWork"
    const val CONFIRM_WORK = "confirmWork/{workOrder}"

    fun equipment360(equipmentId: String) = "equipment360/$equipmentId"
    fun visualInspection(equipmentId: String) = "visualInspection/$equipmentId"
    fun confirmWork(workOrder: String) = "confirmWork/$workOrder"
}

@Composable
fun PlantCareApp() {
    val context = LocalContext.current
    val database = AppDatabase.getInstance(context)
    val repository = EquipmentRepository(database)
    val confirmationRepo = ConfirmationRepository(database)
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Destinations.DASHBOARD
    ) {
        composable(Destinations.DASHBOARD) {
            DashboardScreen(
                repository = repository,
                confirmationRepository = confirmationRepo,
                onNavigateToScanner = {
                    navController.navigate(Destinations.SCANNER)
                },
                onNavigateToEquipment = { eqId ->
                    navController.navigate(Destinations.equipment360(eqId))
                },
                onNavigateToVisualInspection = { eqId ->
                    navController.navigate(Destinations.visualInspection(eqId))
                },
                onNavigateToMyWork = {
                    navController.navigate(Destinations.MY_WORK)
                },
                onNavigateToConfirmWork = { wo ->
                    navController.navigate(Destinations.confirmWork(wo))
                }
            )
        }

        composable(Destinations.MY_WORK) {
            val vm: MyWorkViewModel = viewModel(
                factory = MyWorkViewModel.Factory(confirmationRepo, repository)
            )

            MyWorkScreen(
                viewModel = vm,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToConfirmWork = { wo ->
                    navController.navigate(Destinations.confirmWork(wo))
                },
                onNavigateToEquipment = { eqId ->
                    navController.navigate(Destinations.equipment360(eqId))
                },
                onNavigateToScanner = {
                    navController.navigate(Destinations.SCANNER)
                }
            )
        }

        composable(
            route = Destinations.CONFIRM_WORK,
            arguments = listOf(navArgument("workOrder") { type = NavType.StringType })
        ) { backStackEntry ->
            val workOrder = backStackEntry.arguments?.getString("workOrder") ?: "WO 155704"
            val vm: ConfirmationViewModel = viewModel(
                factory = ConfirmationViewModel.Factory(workOrder, confirmationRepo, repository)
            )

            ConfirmWorkScreen(
                viewModel = vm,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onViewWorkOrder = { wo ->
                    // Find equipment for this work order and navigate to Equipment 360 Activities
                    navController.popBackStack()
                },
                onNextJob = {
                    navController.navigate(Destinations.MY_WORK) {
                        popUpTo(Destinations.DASHBOARD) { inclusive = false }
                    }
                }
            )
        }

        composable(Destinations.SCANNER) {
            ScannerScreen(
                repository = repository,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEquipment = { eqId ->
                    navController.navigate(Destinations.equipment360(eqId)) {
                        popUpTo(Destinations.SCANNER) { inclusive = true }
                    }
                },
                onNavigateToSearch = {
                    navController.navigate(Destinations.DASHBOARD) {
                        popUpTo(Destinations.SCANNER) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Destinations.EQUIPMENT_360,
            arguments = listOf(navArgument("equipmentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val equipmentId = backStackEntry.arguments?.getString("equipmentId") ?: "121SC008"
            val vm: Equipment360ViewModel = viewModel(
                factory = Equipment360ViewModel.Factory(equipmentId, repository)
            )

            Equipment360Screen(
                viewModel = vm,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToScanner = {
                    navController.navigate(Destinations.SCANNER)
                },
                onNavigateToVisualInspection = { eqId ->
                    navController.navigate(Destinations.visualInspection(eqId))
                },
                onNavigateToConfirmWork = { wo ->
                    navController.navigate(Destinations.confirmWork(wo))
                },
                onNavigateToHome = {
                    navController.navigate(Destinations.DASHBOARD) {
                        popUpTo(Destinations.DASHBOARD) { inclusive = false }
                    }
                }
            )
        }

        composable(
            route = Destinations.VISUAL_INSPECTION,
            arguments = listOf(navArgument("equipmentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val equipmentId = backStackEntry.arguments?.getString("equipmentId") ?: "121SC008"
            val vm: VisualInspectionViewModel = viewModel(
                factory = VisualInspectionViewModel.Factory(equipmentId, repository)
            )

            VisualInspectionScreen(
                viewModel = vm,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEquipment360 = { eqId ->
                    navController.navigate(Destinations.equipment360(eqId)) {
                        popUpTo(Destinations.equipment360(eqId)) { inclusive = true }
                    }
                }
            )
        }
    }
}

