package com.example.data.service

import com.example.data.model.VisualInspectionContext
import com.example.data.model.VisualInspectionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.Locale

interface IMaintenanceAiService {
    suspend fun analyzeVisualInspection(context: VisualInspectionContext): VisualInspectionResult
}

class MaintenanceAiServiceImpl(
    private val delayMs: Long = 1200L
) : IMaintenanceAiService {

    override suspend fun analyzeVisualInspection(context: VisualInspectionContext): VisualInspectionResult = withContext(Dispatchers.IO) {
        // Natural processing delay to simulate AI inference
        if (delayMs > 0) {
            delay(delayMs)
        }

        val eq = context.equipment
        val observation = context.technicianObservation?.lowercase(Locale.ROOT) ?: ""
        val eqName = eq.name.lowercase(Locale.ROOT)
        val eqId = eq.equipmentId.uppercase(Locale.ROOT)

        // Industrial heuristic engine tailored to plant asset context & technician input
        val (possibleIssue, confidence, observedText, checks, priority, causes, safetyWarning) = when {
            observation.contains("vibration") || observation.contains("hot") || observation.contains("bearing") || eqId == "121SC008" && observation.isBlank() -> {
                HeuristicResult(
                    issue = "Bearing / exciter drive area abnormality",
                    confidence = "Medium",
                    observed = "Possible thermal rise and abnormal vibration signature detected around the drive bearing housing. Visual surface exhibits light slurry ingress near the seal retainer.",
                    checks = listOf(
                        "Check bearing housing temperature with calibrated pyrometer",
                        "Measure vibration velocity RMS (threshold 5.5 mm/s)",
                        "Verify grease level and check for seal purging",
                        "Inspect cardan shaft and V-belt alignment"
                    ),
                    priority = "HIGH",
                    causes = "Slurry contamination ingress or eccentric counterweight imbalance",
                    warning = "⚠ Technician verification required before adjusting rotating components"
                )
            }
            observation.contains("belt") || observation.contains("tear") || observation.contains("tracking") || eqName.contains("conveyor") -> {
                HeuristicResult(
                    issue = "Belt edge fraying & tracking drift",
                    confidence = "High",
                    observed = "Possible lateral tracking misalignment and edge wear along the carry side skirt board. Tungsten scraper blade shows uneven contact pattern.",
                    checks = listOf(
                        "Inspect return idler alignment at snub pulley",
                        "Check belt drift limit switch clearance",
                        "Measure remaining top cover rubber gauge",
                        "Verify belt scraper tensioning spring"
                    ),
                    priority = "HIGH",
                    causes = "Material accumulation on tail pulley or degraded impact cradle rollers",
                    warning = "⚠ Lockout / Tagout (LOTO) required prior to working inside conveyor perimeter"
                )
            }
            observation.contains("crack") || observation.contains("weld") || observation.contains("structure") -> {
                HeuristicResult(
                    issue = "Structural weld fatigue & surface cracking",
                    confidence = "Medium",
                    observed = "Possible micro-cracking observed near cross-beam gusset weld seam. Cyclic stress marks visible adjacent to side plate fastener row.",
                    checks = listOf(
                        "Clean area and perform Dye Penetrant (PT) test",
                        "Torque check adjacent M24 Grade 10.9 structural bolts",
                        "Inspect dampening spring brackets for tilt"
                    ),
                    priority = "VERY HIGH",
                    causes = "Cyclic impact fatigue under oversize rock feed surges",
                    warning = "⚠ Technician verification required: Inspect structural integrity prior to restarting production"
                )
            }
            observation.contains("oil") || observation.contains("leak") || observation.contains("fluid") -> {
                HeuristicResult(
                    issue = "Lubricant seepage around shaft seal",
                    confidence = "High",
                    observed = "Possible oil seepage pooling around bottom lip seal flange. Slight film contamination noted on drive coupling shroud.",
                    checks = listOf(
                        "Verify gearbox oil sight glass level",
                        "Inspect desiccant breather for moisture saturation",
                        "Check lip seal for elastomer hardening"
                    ),
                    priority = "MEDIUM",
                    causes = "Thermal expansion or seal lip degradation",
                    warning = null
                )
            }
            else -> {
                // General asset inspection analysis based on equipment history
                HeuristicResult(
                    issue = "Mechanical wear & physical condition alert",
                    confidence = "Medium",
                    observed = "Possible abnormal surface wear and material accumulation observed on ${eq.name}. Consistent with operational hours since last maintenance walkdown.",
                    checks = listOf(
                        "Perform physical tactile and acoustic inspection",
                        "Verify structural bolt torques and guard clearances",
                        "Inspect lubrication points and grease purge ports"
                    ),
                    priority = "MEDIUM",
                    causes = "Operational friction and continuous plant feed wear",
                    warning = "⚠ Technician verification required"
                )
            }
        }

        VisualInspectionResult(
            possibleIssue = possibleIssue,
            confidence = confidence,
            observations = observedText,
            recommendedChecks = checks,
            suggestedNotificationTitle = "Possible issue on ${eq.equipmentId}: $possibleIssue",
            suggestedPriority = priority,
            possibleCauses = causes,
            safetyWarning = safetyWarning,
            isOfflineProcessed = true
        )
    }

    private data class HeuristicResult(
        val issue: String,
        val confidence: String,
        val observed: String,
        val checks: List<String>,
        val priority: String,
        val causes: String,
        val warning: String?
    )
}
