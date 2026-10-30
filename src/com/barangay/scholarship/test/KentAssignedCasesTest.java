package com.barangay.scholarship.test;

import com.barangay.scholarship.algorithms.AlgorithmUtils;
import com.barangay.scholarship.datastructures.ActionStack;
import com.barangay.scholarship.datastructures.ScholarBST;
import com.barangay.scholarship.datastructures.ScholarshipQueue;
import com.barangay.scholarship.model.ActionRecord;
import com.barangay.scholarship.model.Applicant;
import com.barangay.scholarship.model.ApplicationStatus;
import com.barangay.scholarship.model.Scholar;
import com.barangay.scholarship.service.ScholarshipManager;
import com.barangay.scholarship.util.SampleDataSeeder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Verification test suite specifically validating Christian Kent Bayani's assigned test cases:
 * - TC-07: Slot Allocation & Ranking Engine — Academic Merit (Merge Sort)
 * - TC-08: Slot Allocation & Ranking Engine — Financial Need (Quick Sort)
 * - TC-09: Stipend Payout Disbursement & Budget Balance Tracking
 */
public class KentAssignedCasesTest {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("=======================================================================");
        System.out.println("   QA VERIFICATION SUITE: ASSIGNED TEST CASES FOR CHRISTIAN KENT BAYANI");
        System.out.println("   Tasks: TC-07 (Merit Ranking), TC-08 (Need Ranking), TC-09 (Payout Desk)");
        System.out.println("=======================================================================\n");

        runVerification("TC-07: Slot Allocation & Ranking Engine — Academic Merit (Merge Sort)",
                KentAssignedCasesTest::verifyTC07_AcademicMeritMergeSort);

        runVerification("TC-08: Slot Allocation & Ranking Engine — Financial Need (Quick Sort)",
                KentAssignedCasesTest::verifyTC08_FinancialNeedQuickSort);

        runVerification("TC-09: Stipend Payout Disbursement & Budget Balance Tracking",
                KentAssignedCasesTest::verifyTC09_PayoutDisbursementAndBudgetTracking);

        System.out.println("\n-----------------------------------------------------------------------");
        System.out.printf("QA SUMMARY: %d Verified, %d Failed (Total: %d)%n",
                testsPassed, testsFailed, (testsPassed + testsFailed));
        System.out.println("=======================================================================");

        if (testsFailed > 0) {
            System.exit(1);
        }
    }

    private static void runVerification(String name, Runnable test) {
        System.out.println(">>> STARTING: " + name);
        try {
            test.run();
            System.out.println(">>> [VERIFIED] " + name + "\n");
            testsPassed++;
        } catch (Throwable t) {
            System.err.println(">>> [FAILED] " + name + " - Reason: " + t.getMessage() + "\n");
            t.printStackTrace(System.err);
            testsFailed++;
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("Assertion Failed: " + message);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) return;
        if (expected == null || !expected.equals(actual)) {
            throw new AssertionError(message + " (Expected: " + expected + ", Got: " + actual + ")");
        }
    }

    /**
     * TC-07: Slot Allocation & Ranking Engine — Academic Merit (Merge Sort)
     * Step 1. Open Slot Allocation & Ranking Engine tab.
     * Step 2. Set Ranking Criteria to Academic Merit (GWA Merge Sort).
     * Step 3. Adjust the Available Slots spinner/slider to 5.
     * Step 4. Click Preview Ranking.
     * Step 5. Review the preview table ordering.
     * Step 6. Click Confirm & Allocate Slots.
     * Acceptance Criteria:
     * - Merge Sort executes in O(n log n), normalizing college GWA (1.00 at top) and high school grades.
     * - Preview table shows top 5 candidates marked Approved and remaining verified students marked Waitlisted.
     * - Confirmation sets top 5 to APPROVED, inserts them into ScholarBST, pushes remaining qualified students into waitlistQueue, and logs transaction.
     */
    private static void verifyTC07_AcademicMeritMergeSort() {
        ScholarshipManager manager = new ScholarshipManager(150000.0, 3000.0);
        SampleDataSeeder.seedDemoData(manager);

        // Initial sanity check on candidate pool
        List<Applicant> initialCandidates = new ArrayList<>();
        for (Applicant a : manager.getMasterList()) {
            if (a.getStatus() == ApplicationStatus.VERIFIED || a.getStatus() == ApplicationStatus.WAITLISTED) {
                initialCandidates.add(a);
            }
        }
        assertEquals(7, initialCandidates.size(), "Should have 7 verified/waitlisted candidates ready for ranking");

        // Step 2 & 4: Set Criteria to Academic Merit (GWA Merge Sort) and preview ranking
        List<Applicant> previewList = new ArrayList<>(initialCandidates);
        AlgorithmUtils.mergeSortByGWA(previewList, true);

        // Verification of normalized Merge Sort ordering:
        // Candidate GWAs (Collegiate scale 1.00 - 5.00):
        // Katrina Denise Morales: 1.70
        // Hannah Sophia Del Rosario: 1.80
        // Danilo Jose Mercado: 1.95
        // Bea Camille Fernandez: 2.00
        // Jerome Patrick Lopez: 2.10
        // Patricia Anne Castro: 2.15
        // Gabriel Luis Soriano: 2.25
        System.out.println("  [TC-07 Preview Ordering by Academic Merit]");
        for (int i = 0; i < previewList.size(); i++) {
            Applicant a = previewList.get(i);
            int rank = i + 1;
            String outcome = (rank <= 5) ? "OFFICIAL SLOT (APPROVED)" : "WAITLIST QUEUE (FIFO)";
            System.out.printf("    #%d: %s | GWA: %s | Income: %s | Outcome: %s%n",
                    rank, a.getFullName(), a.getFormattedGwa(), a.getFormattedIncome(), outcome);
        }

        assertEquals("Katrina Denise Morales", previewList.get(0).getFullName(), "Rank 1 should be Katrina (1.70)");
        assertEquals("Hannah Sophia Del Rosario", previewList.get(1).getFullName(), "Rank 2 should be Hannah (1.80)");
        assertEquals("Danilo Jose Mercado", previewList.get(2).getFullName(), "Rank 3 should be Danilo (1.95)");
        assertEquals("Bea Camille Fernandez", previewList.get(3).getFullName(), "Rank 4 should be Bea (2.00)");
        assertEquals("Jerome Patrick Lopez", previewList.get(4).getFullName(), "Rank 5 should be Jerome (2.10)");
        assertEquals("Patricia Anne Castro", previewList.get(5).getFullName(), "Rank 6 should be Patricia (2.15)");
        assertEquals("Gabriel Luis Soriano", previewList.get(6).getFullName(), "Rank 7 should be Gabriel (2.25)");

        int initialScholarCount = manager.getScholarDirectory().size();
        assertEquals(10, initialScholarCount, "Pre-seeded scholars in BST should be 10");

        // Step 6: Click Confirm & Allocate Slots (5 slots)
        int slotLimit = 5;
        manager.evaluateAndAllocateSlots(ScholarshipManager.RankingCriteria.MERIT_GWA, slotLimit);

        // Verify top 5 are APPROVED and inserted into ScholarBST
        assertEquals(15, manager.getScholarDirectory().size(), "BST should now contain 10 + 5 = 15 scholars");
        for (int i = 0; i < 5; i++) {
            Applicant expectedApproved = previewList.get(i);
            Applicant inMaster = manager.findApplicantById(expectedApproved.getApplicantId());
            assertTrue(inMaster != null, "Applicant must exist in masterList");
            assertEquals(ApplicationStatus.APPROVED, inMaster.getStatus(),
                    expectedApproved.getFullName() + " must have status APPROVED");
            assertTrue(inMaster instanceof Scholar, expectedApproved.getFullName() + " must be instance of Scholar");

            Scholar s = (Scholar) inMaster;
            Scholar fromBST = manager.getScholarDirectory().searchById(s.getScholarId());
            assertTrue(fromBST != null, s.getScholarId() + " must be searchable in ScholarBST");
            assertEquals(expectedApproved.getFullName(), fromBST.getFullName(), "Scholar in BST matches name");
        }

        // Verify remaining 2 are WAITLISTED and pushed into waitlistQueue (FIFO)
        ScholarshipQueue<Applicant> waitlist = manager.getWaitlistQueue();
        assertEquals(2, waitlist.size(), "Waitlist queue should contain the 2 remaining candidates");
        Applicant wl1 = waitlist.dequeue();
        Applicant wl2 = waitlist.dequeue();
        assertEquals("Patricia Anne Castro", wl1.getFullName(), "Waitlist head must be Patricia Anne Castro");
        assertEquals("Gabriel Luis Soriano", wl2.getFullName(), "Second waitlist entry must be Gabriel Luis Soriano");
        assertEquals(ApplicationStatus.WAITLISTED, wl1.getStatus(), "Patricia status is WAITLISTED");
        assertEquals(ApplicationStatus.WAITLISTED, wl2.getStatus(), "Gabriel status is WAITLISTED");

        // Verify action logged in ActionStack
        ActionStack<ActionRecord> actionStack = manager.getActionHistory();
        assertTrue(!actionStack.isEmpty(), "ActionStack must record transaction");
        ActionRecord lastAction = actionStack.peek();
        assertEquals(ActionRecord.ActionType.ALLOCATE_SLOTS, lastAction.getType(),
                "Last action type must be ALLOCATE_SLOTS");
        System.out.println("  [TC-07 Confirmed]: Action logged: " + lastAction.getDescription());
    }

    /**
     * TC-08: Slot Allocation & Ranking Engine — Financial Need (Quick Sort)
     * Step 1. In Slot Allocation Engine, set Ranking Criteria to Financial Need (Household Income Quick Sort).
     * Step 2. Set slot limit to 5 and click Preview Ranking.
     * Step 3. Verify applicant ordering based on household income.
     * Step 4. Compare order against the Merit-based ranking from TC-07.
     * Acceptance Criteria:
     * - Quicksort executes in O(n log n), ordering applicants ascending by household income (lowest income / poorest families ranked first).
     * - Preview cleanly rearranges candidates without altering underlying data until confirmation.
     * - Algorithm switching is responsive and instantaneous.
     */
    private static void verifyTC08_FinancialNeedQuickSort() {
        ScholarshipManager manager = new ScholarshipManager(150000.0, 3000.0);
        SampleDataSeeder.seedDemoData(manager);

        List<Applicant> candidateList = new ArrayList<>();
        for (Applicant a : manager.getMasterList()) {
            if (a.getStatus() == ApplicationStatus.VERIFIED || a.getStatus() == ApplicationStatus.WAITLISTED) {
                candidateList.add(a);
            }
        }

        // Step 1 & 2: Preview QuickSort by Income
        List<Applicant> needRanked = new ArrayList<>(candidateList);
        AlgorithmUtils.quickSortByIncome(needRanked, true);

        System.out.println("  [TC-08 Preview Ordering by Financial Need (QuickSort)]");
        for (int i = 0; i < needRanked.size(); i++) {
            Applicant a = needRanked.get(i);
            int rank = i + 1;
            String outcome = (rank <= 5) ? "OFFICIAL SLOT (APPROVED)" : "WAITLIST QUEUE (FIFO)";
            System.out.printf("    #%d: %s | Income: %s (₱%.2f) | GWA: %s | Outcome: %s%n",
                    rank, a.getFullName(), a.getFormattedIncome(), a.getMonthlyFamilyIncome(),
                    a.getFormattedGwa(), outcome);
        }

        // Step 3: Verify ascending household income ordering (lowest income first)
        for (int i = 0; i < needRanked.size() - 1; i++) {
            double incomeCurr = needRanked.get(i).getMonthlyFamilyIncome();
            double incomeNext = needRanked.get(i + 1).getMonthlyFamilyIncome();
            assertTrue(incomeCurr <= incomeNext,
                    String.format("Applicant #%d (₱%.2f) must have income <= Applicant #%d (₱%.2f)",
                            i + 1, incomeCurr, i + 2, incomeNext));
        }

        assertEquals("Hannah Sophia Del Rosario", needRanked.get(0).getFullName(), "Rank 1 Income should be Hannah (₱9,000)");
        assertEquals("Danilo Jose Mercado", needRanked.get(1).getFullName(), "Rank 2 Income should be Danilo (₱11,500)");
        assertEquals("Katrina Denise Morales", needRanked.get(2).getFullName(), "Rank 3 Income should be Katrina (₱12,500)");
        assertEquals("Jerome Patrick Lopez", needRanked.get(3).getFullName(), "Rank 4 Income should be Jerome (₱14,500)");
        assertEquals("Bea Camille Fernandez", needRanked.get(4).getFullName(), "Rank 5 Income should be Bea (₱17,000)");
        assertEquals("Patricia Anne Castro", needRanked.get(5).getFullName(), "Rank 6 Income should be Patricia (₱18,000)");
        assertEquals("Gabriel Luis Soriano", needRanked.get(6).getFullName(), "Rank 7 Income should be Gabriel (₱19,000)");

        // Step 4: Compare order against Merit-based ranking from TC-07
        List<Applicant> meritRanked = new ArrayList<>(candidateList);
        AlgorithmUtils.mergeSortByGWA(meritRanked, true);

        // In Merit, Katrina was #1 (GWA 1.70) and Hannah was #2.
        // In Financial Need, Hannah is #1 (Income ₱9,000) and Katrina is #3 (Income ₱12,500).
        assertTrue(!meritRanked.get(0).getApplicantId().equals(needRanked.get(0).getApplicantId()),
                "Rank 1 must differ between Merit (Katrina) and Financial Need (Hannah)");
        System.out.println("  [TC-08 Comparison]: Rank 1 Merit is " + meritRanked.get(0).getFullName() +
                " vs Rank 1 Need is " + needRanked.get(0).getFullName());

        // Verify underlying data was NOT modified by the preview
        for (Applicant a : candidateList) {
            assertTrue(a.getStatus() == ApplicationStatus.VERIFIED || a.getStatus() == ApplicationStatus.WAITLISTED,
                    "Applicant " + a.getFullName() + " status must not be changed during preview");
        }
        assertEquals(10, manager.getScholarDirectory().size(), "ScholarBST must remain untouched during preview");
    }

    /**
     * TC-09: Stipend Payout Disbursement & Budget Balance Tracking
     * Step 1. Open Payout & Allowance Disbursement Desk tab.
     * Step 2. Use search bar to find an APPROVED scholar with payout status "Unclaimed".
     * Step 3. Select the scholar and click Release Stipend (₱3,000).
     * Step 4. Confirm the disbursement prompt.
     * Step 5. Verify dashboard budget cards and scholar payout status.
     * Step 6. Try clicking Release Stipend again on the same disbursed scholar.
     * Acceptance Criteria:
     * - Scholar payout status updates to DISBURSED with timestamp recorded.
     * - Disbursed Budget increments by ₱3,000; Remaining Budget decrements by ₱3,000.
     * - Second attempt is blocked or disabled: "Stipend already disbursed for this period."
     * - Action pushed to ActionStack with amount ₱3,000 for undo capability.
     */
    private static void verifyTC09_PayoutDisbursementAndBudgetTracking() {
        ScholarshipManager manager = new ScholarshipManager(150000.0, 3000.0);
        SampleDataSeeder.seedDemoData(manager);

        // Pre-seeded scholars begin unclaimed so the startup budget remains untouched.
        double initialTotal = manager.getTotalBudget();
        double initialDisbursed = manager.getDisbursedBudget();
        double initialRemaining = manager.getRemainingBudget();

        assertEquals(150000.0, initialTotal, "Total budget should be 150,000");
        assertEquals(0.0, initialDisbursed, "Disbursed budget should start at 0");
        assertEquals(150000.0, initialRemaining, "Remaining budget should start at 150,000");

        // Step 2: Find an APPROVED scholar with payout status "Unclaimed"
        Scholar unclaimedScholar = null;
        for (Scholar s : manager.getAllScholarsFromBST()) {
            if (!s.isStipendDisbursed()) {
                unclaimedScholar = s;
                break;
            }
        }
        assertTrue(unclaimedScholar != null, "Must find an unclaimed scholar from the pre-seeded BST");
        System.out.printf("  [TC-09 Selected Scholar]: %s (%s) | Status: %s | Grant: %s%n",
                unclaimedScholar.getFullName(), unclaimedScholar.getScholarId(),
                (unclaimedScholar.isStipendDisbursed() ? "CLAIMED" : "UNCLAIMED"), unclaimedScholar.getFormattedGrant());
        assertTrue(!unclaimedScholar.isStipendDisbursed(), "Initial status must be UNCLAIMED");
        assertTrue(unclaimedScholar.getDisbursedAt() == null, "Initial disbursed timestamp must be null");

        // Step 3 & 4: Release Stipend (₱3,000) and confirm
        String scholarId = unclaimedScholar.getScholarId();
        boolean releaseResult = manager.releaseStipend(scholarId);
        assertTrue(releaseResult, "manager.releaseStipend() must return true for valid unclaimed scholar");

        // Step 5: Verify dashboard budget cards and scholar payout status
        assertEquals(initialDisbursed + 3000.0, manager.getDisbursedBudget(),
                "Disbursed budget must increment by ₱3,000");
        assertEquals(initialRemaining - 3000.0, manager.getRemainingBudget(),
                "Remaining budget must decrement by ₱3,000");
        assertTrue(unclaimedScholar.isStipendDisbursed(), "Scholar isStipendDisbursed() must now be true");
        assertEquals(ApplicationStatus.DISBURSED, unclaimedScholar.getStatus(), "Scholar status must now be DISBURSED");
        assertTrue(unclaimedScholar.getDisbursedAt() != null, "Scholar getDisbursedAt() timestamp must be recorded");

        System.out.printf("  [TC-09 Post-Disbursement Verification]:%n" +
                        "    Scholar: %s (%s)%n" +
                        "    Payout Status: %s on %s%n" +
                        "    Total Disbursed: ₱%,.2f%n" +
                        "    Remaining Budget: ₱%,.2f%n",
                unclaimedScholar.getFullName(), unclaimedScholar.getScholarId(),
                (unclaimedScholar.isStipendDisbursed() ? "CLAIMED" : "UNCLAIMED"), unclaimedScholar.getFormattedDisbursedDate(),
                manager.getDisbursedBudget(), manager.getRemainingBudget());

        // Step 6: Try clicking Release Stipend again on the same disbursed scholar
        boolean secondAttempt = manager.releaseStipend(scholarId);
        assertTrue(!secondAttempt, "Second disbursement attempt must be blocked and return false");
        assertEquals(initialDisbursed + 3000.0, manager.getDisbursedBudget(),
                "Disbursed budget must not increase on blocked second attempt");
        assertEquals(initialRemaining - 3000.0, manager.getRemainingBudget(),
                "Remaining budget must not decrease on blocked second attempt");
        System.out.println("  [TC-09 Blocked Second Attempt]: Correctly blocked duplicate stipend release.");

        // Verify ActionStack record
        ActionRecord lastAction = manager.getActionHistory().peek();
        assertEquals(ActionRecord.ActionType.RELEASE_STIPEND, lastAction.getType(),
                "Action record type must be RELEASE_STIPEND");
        assertEquals(3000.0, lastAction.getAmount(), "Action record amount must be 3,000.0");
        assertEquals(scholarId, lastAction.getTargetId(), "Action record targetId must match scholarId");

        // Verify 1-Click Undo rollback capability (LIFO)
        manager.undoLastAction();
        assertEquals(initialDisbursed, manager.getDisbursedBudget(),
                "After undo, Disbursed budget must return to ₱0");
        assertEquals(initialRemaining, manager.getRemainingBudget(),
                "After undo, Remaining budget must return to ₱150,000");
        assertTrue(!unclaimedScholar.isStipendDisbursed(),
                "After undo, scholar isStipendDisbursed() must be reset to false");
        assertEquals(ApplicationStatus.APPROVED, unclaimedScholar.getStatus(),
                "After undo, scholar status must be reset to APPROVED");
        System.out.println("  [TC-09 Undo Verification]: Budget and scholar status successfully rolled back via ActionStack.");
    }
}
