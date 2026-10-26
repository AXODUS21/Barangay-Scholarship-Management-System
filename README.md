# Barangay Scholarship & Educational Assistance Management System

A desktop Java application with a graphical user interface (GUI) designed for Philippine Barangay Halls and Sangguniang Kabataan (SK) councils. It digitizes paper-based scholarship records and manual spreadsheets, implementing all required core transactions, custom non-library data structures (Queue, Stack, and Binary Search Tree), fundamental sorting and searching algorithms, real-time budget tracking, an eligibility auto-checker, and a 1-click transaction undo system.

---

## System Features & Core Transactions

### 1. Applicant Registration & Profiling (Transaction Stage 1)
- Encodes resident student details: Applicant ID, Full Name, School/University, Year Level, GWA, Monthly Household Income, and Barangay Zone/Purok.
- **Dual-Scale Eligibility Auto-Checker**:
  - Automatically detects collegiate grading scale (1.00 – 5.00, where $\le 2.50$ qualifies) or percentage scale (75% – 100%, where $\ge 85.0\%$ qualifies).
  - Evaluates monthly income poverty threshold ($\le \text{₱}25,000$).
  - Provides instant visual feedback badge as data is typed.
- **Digital Document Uploads & Declaration**:
  - Students attach digital copies (PDF, PNG, JPG, JPEG, DOCX) for each of the 4 required documents: Certificate of Indigency, Transcript/Form 138, COR, and Valid ID.
  - Automatically verifies, stores, and archives attachments in `data/uploads/<applicantId>/`.
  - In-app preview and removal of selected files prior to submission.
  - Data privacy and truthfulness certification agreement.

### 2. Document Verification & Screening Queue (Transaction Stage 2)
- Visualizes the custom **FIFO Queue (`ScholarshipQueue<Applicant>`)**: applicants are processed on a First-Come, First-Served basis.
- Interactive Document Inspector checking off physical copies and inspecting digital uploads:
  1. Certificate of Indigency (Barangay-issued)
  2. Certified True Copy of Grades / Transcript of Records / Form 138
  3. Certificate of Registration / Official Enrollment Form
  4. Valid Barangay ID or Student ID
- Direct **"View Document"** button to open and review digital attachments in the OS viewer or built-in image viewer.
- Desk file attachment button to attach walk-in student files during in-person screening.
- Moving complete applications directly to the evaluation queue (`VERIFIED`).

### 3. Slot Allocation & Ranking Engine (Transaction Stage 3)
- Automatically ranks verified candidates using rubric-mandated algorithms:
  - **Academic Merit Ranking**: Divide-and-conquer **Merge Sort** (`mergeSortByGWA`) prioritizes top achievers.
  - **Financial Need Ranking**: Partitioning **Quick Sort** (`quickSortByIncome`) prioritizes lowest household incomes.
- Configurable slot quota and budget limits:
  - Top $N$ applicants are officially granted scholarship slots (`APPROVED`) and indexed into the `ScholarBST`.
  - Excess qualified applicants automatically overflow into the **Waitlist Queue (FIFO)**.

### 4. Payout & Allowance Disbursement Desk (Transaction Stage 4)
- Logs when an approved scholar claims their semester stipend (default: ₱3,000.00).
- **Live Budget Tracker**:
  - Deducts claimed stipends in real-time from the total SK Scholarship Budget (default: ₱150,000.00).
  - Prevents disbursement if remaining barangay funds are depleted.
- **Official Printable Attendance & Payroll Sheet Generator**:
  - Exports a formatted text file (`.txt`) complete with claimant signature columns and certification lines for the SK Chairperson, Barangay Treasurer, and Punong Barangay.

### 5. Automated Waitlist Promotion (Dropout Replacement)
- If an approved scholar drops out, is disqualified, or fails to claim their grant:
  - Dropping the scholar automatically dequeues the next candidate from the head of the **Waitlist Queue**.
  - Promotes the candidate to an official **Scholar** and indexes them into the BST.

### 6. Staff Accident Protection: 1-Click Action Undo (LIFO Stack)
- Every transaction is recorded in a custom **ActionStack (`ActionStack<ActionRecord>`)**.
- A single click on **"1-Click Undo Last Action"** rolls back accidental disbursements (restoring the budget balance), status changes, or registrations.

---

## Data Structure & Algorithm Mapping

| Rubric Requirement | Implementation Class | Time Complexity | Workflow Role |
| :--- | :--- | :--- | :--- |
| **Custom Queue (FIFO)** | `ScholarshipQueue<T>` | $O(1)$ Enqueue / Dequeue | First-come, first-served document screening & waitlist queue |
| **Custom Stack (LIFO)** | `ActionStack<T>` | $O(1)$ Push / Pop | Audit log and 1-click transaction undo |
| **Binary Search Tree** | `ScholarBST` | $O(\log n)$ Search / Insert | Official scholar directory and ordered traversals |
| **Merge Sort** | `AlgorithmUtils.mergeSortByGWA` | $O(n \log n)$ | Ranks applicants by academic merit (honors first) |
| **Quick Sort** | `AlgorithmUtils.quickSortByIncome` | $O(n \log n)$ | Prioritizes applicants by greatest financial need |
| **Quick Sort** | `AlgorithmUtils.quickSortByName` | $O(n \log n)$ | Alphabetical attendance and payroll masterlist |
| **Binary Search** | `AlgorithmUtils.binarySearchById` | $O(\log n)$ | Instant lookup when student walks into Barangay Hall |
| **Binary Search** | `AlgorithmUtils.binarySearchByName` | $O(\log n)$ | Instant student name lookup |
| **Linear Search** | `AlgorithmUtils.linearSearchByPurok` | $O(n)$ | Filter scholars by Purok/Zone |
| **Linear Search** | `AlgorithmUtils.linearSearchBySchool`| $O(n)$ | Filter scholars by School/University |

---

## Project Architecture & OOP Class Diagram

```
com.barangay.scholarship
├── Main.java                        # System entry point (supports portal selection)
├── model/
│   ├── ApplicationStatus.java       # Status lifecycle enum
│   ├── DocumentChecklist.java       # Tracks 4 required verification documents
│   ├── Applicant.java               # Base student profile entity
│   ├── Scholar.java                 # Scholar extends Applicant (OOP Inheritance)
│   └── ActionRecord.java            # Transaction audit record for ActionStack
├── datastructures/
│   ├── ScholarshipQueue.java        # Custom singly-linked generic FIFO Queue
│   ├── ActionStack.java             # Custom singly-linked generic LIFO Stack
│   └── ScholarBST.java              # Custom Binary Search Tree keyed by Scholar ID
├── algorithms/
│   └── AlgorithmUtils.java          # From-scratch MergeSort, QuickSort, BinarySearch
├── service/
│   └── ScholarshipManager.java      # Central transaction controller and budget tracker
├── ui/
│   ├── Theme.java                   # Color palette and modern Swing tokens
│   ├── PortalSelectorFrame.java     # Gateway hub separating Student & Staff Portals
│   ├── MainFrame.java               # Barangay Staff & Admin Management Suite
│   ├── student/                     # Dedicated Student Self-Service Portal (No Admin Controls)
│   │   ├── StudentPortalFrame.java  # Isolated student application & tracking window
│   │   ├── StudentApplicationView.java # Dedicated student registration & auto-checker
│   │   ├── StudentStatusTrackerView.java # Self-service status & queue position inquiry
│   │   └── StudentGuidelinesView.java # Criteria, documents checklist, and FAQs
│   ├── components/
│   │   ├── SidebarPanel.java        # Left navigation sidebar with portal switcher
│   │   ├── StatCard.java            # KPI metric cards with accent borders
│   │   ├── ModernTable.java         # Alternating row table with custom headers
│   │   └── StatusBadgeRenderer.java # Colored status badge pill renderer
│   └── views/
│       ├── DashboardView.java       # Overview stats, budget meter, recent actions
│       ├── RegistrationView.java    # Staff walk-in intake & master registry
│       ├── ScreeningQueueView.java  # FIFO queue visualizer and document screening
│       ├── RankingAllocationView.java # Merge / Quick Sort ranking & slot allocator
│       ├── DisbursementView.java    # Payout tracker, budget deduction, payroll export
│       ├── ScholarDirectoryView.java # BST directory, binary search, tree traversals
│       ├── AuditLogView.java        # ActionStack viewer and 1-click undo button
│       └── SettingsView.java        # Budget configuration & demo data reloader
├── util/
│   ├── SampleDataSeeder.java        # Preloads 20 realistic Filipino student records
│   ├── DataPersistence.java         # Official printable payroll sheet generator
│   └── FileUploadManager.java       # Document file uploads, validation, storage, and viewer
└── test/
    └── TestRunner.java              # Standalone automated unit test suite (16 tests)
```

---

## Running the Project

### Prerequisites
- **Java SE JDK**: Version 17, 21, or 26.
- **Zero Third-Party Dependencies**: No Maven, Gradle, or external libraries required. The application runs natively using standard Java SE, Swing, and AWT.
- Java sources are compiled explicitly as UTF-8. The interface uses text-only labels instead of emoji icons so controls remain legible on systems without emoji fonts.

---

### Quick Start: Easy Compilation & Run Guide

To compile and launch the entire application in a single step, simply run the root **`run.sh`** script:

```bash
./run.sh
```

#### What `run.sh` Does Automatically:
1. **JDK Detection**: Checks `$JAVA_HOME`, standard system paths, and `/Library/Java/JavaVirtualMachines/jdk-26.jdk/Contents/Home/bin` to locate your Java compiler (`javac`) and runtime (`java`).
2. **Directory Setup**: Creates the `bin/` output directory if it doesn't already exist.
3. **Fresh Source Compilation**: Compiles all `.java` files from `src/` directly into `bin/`.
4. **GUI Launch**: Launches the graphical interface (`com.barangay.scholarship.Main`) with macOS/Windows anti-aliasing and high-DPI scaling enabled.

#### Expected Terminal Output:
```text
===================================================================
   BARANGAY SCHOLARSHIP & EDUCATIONAL ASSISTANCE MANAGEMENT SYSTEM 
===================================================================
[INFO] Using Java Compiler: /Library/Java/JavaVirtualMachines/jdk-26.jdk/Contents/Home/bin/javac
java version "26.0.2" 2026-07-21

[1/2] Compiling all source code from src/...
[OK] Compilation successful! Classes compiled into bin/

[2/2] Launching Graphical User Interface (GUI)...
-------------------------------------------------------------------
```

---

### Running the Automated Test Suite

To verify all custom data structures, sorting algorithms, searching algorithms, budget deduction logic, and undo mechanics:

```bash
./build.sh test
```

### Sample Automated Test Output:
```
=================================================================
   BARANGAY SCHOLARSHIP MANAGEMENT SYSTEM - AUTOMATED TEST SUITE  
=================================================================

[PASS] Custom FIFO Queue (ScholarshipQueue)
[PASS] Custom LIFO Stack (ActionStack)
[PASS] Custom Binary Search Tree (ScholarBST)
[PASS] Merge Sort Algorithm (Academic Merit by GWA)
[PASS] Quick Sort Algorithm (Financial Need by Income)
[PASS] Quick Sort Algorithm (Alphabetical by Name)
[PASS] Binary Search Algorithm (O(log n) by ID and Name)
[PASS] Linear Search Algorithm (Filter by Purok and School)
[PASS] Eligibility Auto-Checker (Collegiate & Percentage)
[PASS] Budget Deduction & Stipend Disbursement
[PASS] Waitlist Queue Auto-Promotion on Scholar Drop
[PASS] ActionStack 1-Click Undo Rollback

-----------------------------------------------------------------
TEST SUMMARY: 12 Passed, 0 Failed (Total: 12)
=================================================================
```

---

## Presentation & Classroom Demonstration Guide
1. **Launch the App**: Run `./run.sh`. The app opens with **20 realistic Filipino student applicants** pre-loaded.
2. **Dashboard**: View the **Remaining SK Budget meter (₱105,000 / ₱150,000)**, active scholars, and recent transactions.
3. **Screening Queue (FIFO)**: Click the **Screening Queue** tab. Demonstrate inspecting requirements for the student at the front of the line and clicking **"Save & Mark Verified"**.
4. **Ranking & Slot Allocation**:
   - Switch between **Merge Sort (Merit by GWA)** and **Quick Sort (Need by Income)**.
   - Adjust slot quota and click **"Confirm & Allocate Slots"**. Watch the cutoff preview separate official scholars from the waitlist!
5. **Payout & Disbursement**: Click **Disburse Selected Stipend**. The budget instantly deducts by ₱3,000. Click **"Export Payout Attendance Sheet"** to generate the official printable payroll.
6. **1-Click Undo**: Click **"1-Click Undo Last Action"**. The stipend disbursement is rolled back, and ₱3,000 is refunded to the remaining budget!
7. **Scholar Directory & BST**:
   - Search by Scholar ID or Name using **Binary Search**.
   - Toggle **In-Order**, **Pre-Order**, and **Post-Order** tree traversals.
   - Click **"Drop & Promote Waitlist"** on any scholar to demonstrate the automatic promotion of the next student in the Waitlist Queue!