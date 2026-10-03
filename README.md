# PlantCare AI — Industrial Maintenance Intelligence & Field Operations

[![Android Build](https://img.shields.io/badge/Android-APK%20Ready-success.svg)](app/build/outputs/apk/debug/app-debug.apk)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-blue.svg)]()
[![Room Database](https://img.shields.io/badge/Storage-Room%20(SQLite)%20Offline--First-orange.svg)]()
[![Architecture](https://img.shields.io/badge/Architecture-Repository%20Pattern%20%2B%20Clean%20MVVM-brightgreen.svg)]()

**PlantCare AI** est une application mobile d'intelligence de maintenance industrielle conçue pour les techniciens et ingénieurs de maintenance sur site dans les environnements industriels lourds (installations de concassage, criblage, broyage et manutention de minerais).

L'application combine une architecture **100% Hors-Ligne (Offline-First)** avec persistance locale **Room Database**, le scan ultra-rapide de tags d'équipements (QR/codes-barres), une vue globale **Equipment 360°**, le module d'activités **"My Work"**, la **confirmation d'intervention terrain (Job Confirmation)** en 30 à 60 secondes, et une assistance de diagnostic visuel par IA.

---

## Sommaire / Table of Contents

1. [Génération & Téléchargement de l'APK (Build Instructions)](#1-génération--téléchargement-de-lapk-build-instructions)
2. [Architecture Système & Pattern Repository](#2-architecture-système--pattern-repository)
3. [Module « My Work » (Activités & Ordres de Travail)](#3-module--my-work--activités--ordres-de-travail)
4. [Module « Job Confirmation » (Confirmation d'Intervention Terrain)](#4-module--job-confirmation--confirmation-dintervention-terrain)
5. [Equipment 360° & Scan Industriel](#5-equipment-360--scan-industriel)
6. [Inspection Visuelle & Diagnostic IA](#6-inspection-visuelle--diagnostic-ia)
7. [Équipements & Données Pré-chargées](#7-équipements--données-pré-chargées)
8. [Validation & Tests Unitaires (Robolectric)](#8-validation--tests-unitaires-robolectric)

---

## 1. Génération & Téléchargement de l'APK (Build Instructions)

### Emplacement de l'APK généré
L'APK debug est compilé et prêt à l'emploi aux chemins suivants :
- **Chemin Gradle standard** : `app/build/outputs/apk/debug/app-debug.apk`
- **Artefact AI Studio** : `.build-outputs/app-debug.apk`
- **Taille** : ~26 Mo
- **Compatibilité** : Android 8.0+ (API level 26 et supérieur)

### Commandes de compilation Gradle
Pour compiler ou re-générer l'APK dans votre environnement :

```bash
# Compilation complète de l'APK Debug
gradle assembleDebug

# Compilation et exécution de tous les tests unitaires
gradle :app:testDebugUnitTest

# Vérification rapide de compilation
gradle compileDebugSources
```

### Installation sur appareil / émulateur
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 2. Architecture Système & Pattern Repository

L'application respecte rigoureusement les principes de **Clean Architecture** et de **Repository Pattern**. L'interface utilisateur Jetpack Compose et les ViewModels n'interagissent jamais directement avec les tables Room ou les requêtes SQLite.

### Diagramme de flux

```text
┌────────────────────────────────────────────────────────────────────────┐
│                        Jetpack Compose UI Layer                        │
│   DashboardScreen  •  MyWorkScreen  •  ConfirmWorkScreen  •  Eq360°    │
└───────────────────────────────────▲────────────────────────────────────┘
                                    │ (StateFlow & UI Events)
┌───────────────────────────────────┴────────────────────────────────────┐
│                            ViewModel Layer                             │
│   ConfirmationViewModel  •  MyWorkViewModel  •  Equipment360ViewModel   │
└───────────────────────────────────▲────────────────────────────────────┘
                                    │ (Domain Flows & Coroutines)
┌───────────────────────────────────┴────────────────────────────────────┐
│                    DataRepository Abstraction Layer                    │
│   • IEquipmentRepository   ──► EquipmentRepository (Equipment, WOs)   │
│   • IConfirmationRepository ──► ConfirmationRepository (Sync & Jobs)   │
└───────────────────────────────────▲────────────────────────────────────┘
                                    │ (Dispatched on Dispatchers.IO)
┌───────────────────────────────────┴────────────────────────────────────┐
│                      Room Local Database (SQLite)                      │
│   AppDatabase: WorkOrderDao, JobConfirmationDao, ActiveJobDao,        │
│   EquipmentDao, MaintenanceNotificationDao, DowntimeDao, InspectionDao │
└───────────────────────────────────▲────────────────────────────────────┘
                                    │ (Sync Queue / Background)
                                    ▼
                     PlantCare API / SAP PM & EAM
```

### Principes Clés
1. **Découplage absolu** : Toute modification de données (changement de statut d'OT, horodatage, confirmation de travail) transite par l'interface de repository correspondante (`IEquipmentRepository` ou `IConfirmationRepository`).
2. **Offline-First Garanti** : Toutes les informations (OTs, confirmations, historique, pièces de rechange) sont persistées localement dans `plantcare_maintenance.db`. L'application fonctionne sans aucune perte en zone blanche (galeries souterraines, fosses de concassage).
3. **Protection contre les doublons (Idempotence)** : Chaque confirmation génère un UUID `clientConfirmationId` persistant. Une tentative de re-synchronisation ultérieure ne génère aucun doublon côté serveur/SAP.

---

## 3. Module « My Work » (Activités & Ordres de Travail)

Le nouvel écran d'activités **"My Work"** remplace les listings denses de bureau par une interface mobile native optimisée pour le terrain.

### Fonctionnalités Clés
- **Filtrage par onglets d'activités** :
  - **Today** : Ordres de travail planifiés pour le jour même.
  - **In Progress** : Ordres actuellement entamés ou partiellement confirmés (`REL`, `PCNF`).
  - **Upcoming** : Interventions planifiées à venir.
  - **Waiting** : Ordres en attente de pièces ou d'arrêt machine.
  - **Completed** : Ordres confirmés techniquement (`CNF`, `TECO`).
- **Cartes d'OTs Terrain Complètes** :
  - Numéro d'ordre (ex: `WO 155704`, `WO-4001928`).
  - Badge de statut SAP (`REL`, `PCNF`, `CNF`, `TECO`).
  - Description claire du travail (*Vibrating Grizzly Inspection*).
  - Identifiant équipement (`121SC008`) et localisation fonctionnelle (`BI-PLN-CRU/CRS-003`).
  - Métier et temps alloué (`Mechanical • 6.0 h`).
- **Actions Directes « Un-Tap »** :
  - Bouton `[ START JOB ]` : Démarre le chronométrage actif et enregistre l'heure de début réelle.
  - Bouton `[ CONFIRM WORK ]` : Ouvre le formulaire de confirmation d'intervention pré-rempli.
- **Bannière « CURRENT JOB » Persistante** :
  - Carte active omniprésente avec chronomètre temps réel (`CURRENT JOB: WO 155704 • Elapsed 01:24`).
  - Accès direct depuis le Dashboard principal ou l'écran My Work.

---

## 4. Module « Job Confirmation » (Confirmation d'Intervention Terrain)

Conçu pour une saisie ultra-rapide en **30 à 60 secondes** directement au pied de la machine.

```text
SÉLECTIONNER OT ──► REVOIR CONTEXTE ──► HEURES & TRAVAIL ──► STATUT ──► CONFIRMER
```

### Les 5 Sections du Formulaire de Confirmation
1. **1. Contexte OT (Lecture seule)** :
   - Numéro d'ordre, opération (`0010`), équipement, localisation fonctionnelle et centre de travail.
2. **2. Horodatage & Durée Écoulée** :
   - Heure de début réelle (ex: `14:32`) & Heure de fin réelle (ex: `16:05`).
   - **Calcul automatique du temps écoulé** (`Elapsed: 1 h 33 min`).
3. **3. Temps de Travail Effectif (Actual Work)** :
   - Grand pavé numérique tactile (`[ 1.5 ] [ H ]`) avec bascule instantanée Heures / Minutes.
   - Suggestion intelligente calculée depuis le temps écoulé (`1.55 h`).
   - **Indépendance stricte du temps de clé (Wrench Time)** : Le temps de travail réel n'est jamais écrasé automatiquement s'il diffère de la durée de présence.
   - **Prise en charge Multi-Techniciens (Team Labor)** : Possibilité d'allouer les heures d'équipiers supplémentaires.
4. **4. Travaux Réalisés & Preuves Terrain** :
   - Checkboxes rapides : *Inspection completed, Lubrication completed, Adjustment performed, Component replaced, Cleaning performed*.
   - Notes de travail avec saisie vocale simulée (🎤 Voice note dictation).
   - Mesures physiques : Température de palier (`68 °C`), Vibrations (`4.2 mm/s`), Jeu de fonctionnement (`0.08 mm`).
   - Pièces consommées avec ajout rapide de références de stock.
   - Preuves photographiques : Avant, Pendant et Après intervention.
5. **5. Statut d'Achèvement** :
   - `OUI — Travail Terminé` : Confirmation finale SAP, statut de l'OT mis à jour automatiquement en `CNF`.
   - `NON — Travaux Supplémentaires Requis` : Confirmation partielle (`PCNF`), sélection en un clic du motif (*Attente pièces, Outillage, Accès machine, Relève de poste*) et proposition immédiate de créer un avis de maintenance ou une observation avec héritage du contexte.

### File de synchronisation (Sync Queue)
- Statuts de confirmation : `DRAFT`, `PENDING_SYNC`, `SYNCING`, `SYNCED`, `SYNC_FAILED`.
- Indicateurs visuels clairs informant le technicien que ses données sont sécurisées sur l'appareil.

---

## 5. Equipment 360° & Scan Industriel

- **Scanner Polyvalent** : Reconnaissance automatique des codes QR, DataMatrix, codes-barres 1D et tags industriels structurés :
  - `PLANTCARE:EQUIPMENT:121SC008`
  - `SAP:EAM:121SC008`
  - URL Web d'actif (`https://plantcare.ai/eq/121SC008`)
  - Format JSON (`{"equipmentId": "121SC008"}`)
- **Fiche 360° Complète** :
  - Spécifications techniques complètes, criticité, fabricant et modèle.
  - Compteurs cliquables : Ordres ouverts, Avis en cours, Prochaine maintenance préventive, Taux d'arrêt annuel (Downtime YTD).
  - Historique des consommations de pièces détachées et documentation technique constructeur.

---

## 6. Inspection Visuelle & Diagnostic IA

- Workflow rapide en 4 étapes : *Scan → Photo → Analyse IA → Confirmation*.
- Inférence contextuelle alimentée par modèle d'IA prenant en compte l'historique complet de l'équipement.
- Détection des anomalies critiques (fuite hydraulique, usure de garniture, surchauffe palier) avec recommandations d'actions immédiates.
- Génération automatique d'avis de maintenance pré-remplis avec photos et observations.

---

## 7. Équipements & Données Pré-chargées

L'application embarque un jeu complet de données industrielles réalistes :
- **`121SC008`** — Vibrating Grizzly Feeder Screen (Scalping) — Inclut l'OT **`WO 155704`** (REL, Mechanical • 6.0 h).
- **`CRU01`** — Superior MKIII Gyratory Primary Crusher.
- **`AF01`** — Heavy Duty Apron Feeder.
- **`CV04`** — Overland Belt Conveyor 1200mm.
- **`CV05C`** — Reclaim Conveyor EP-800.
- **`CV06A`** — Mill Feed Conveyor A with Tripper.
- **`CV06B`** — Mill Feed Conveyor B.

---

## 8. Validation & Tests Unitaires (Robolectric)

La suite de tests automatisée valide la totalité des parcours critiques (Critical User Journeys) :

| Classe de Test | Périmètre Validé |
| :--- | :--- |
| `ConfirmationValidationTest` | Validation des champs obligatoires, formats d'opérations et valeurs positives. |
| `ElapsedTimeCalculationTest` | Calcul précis des durées écoulées (`14:32 ➔ 16:05 = 93 min`). |
| `ActualWorkIndependenceTest` | Non-écrasement du temps de clé (`08:00-12:00 = 4h`, réel = `3h` préservé). |
| `OfflineConfirmationTest` | Enregistrement local hors-ligne, mise à jour OT en `CNF`, archivage du job actif. |
| `ConfirmationSyncTest` | Vidage de file d'attente à la reconnexion et affectation du remote ID SAP. |
| `DuplicateConfirmationTest` | Déduplication idempotente via `clientConfirmationId` (UUID). |
| `JobConfirmationEndToEndTest` | Parcours complet de confirmation de bout en bout en local. |
| `QrCodeParserTest` | Décodage et tolérance de tous les formats de tags d'actifs. |
| `EquipmentLookupTest` | Requêtes Room in-memory et agrégation unifiée `getEquipment360`. |
| `VisualInspectionTest` | Pipeline d'inspection visuelle et création d'avis. |

Exécuter la suite :
```bash
gradle :app:testDebugUnitTest
```
