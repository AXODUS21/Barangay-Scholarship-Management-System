package com.barangay.scholarship.test;

import com.barangay.scholarship.algorithms.AlgorithmUtils;
import com.barangay.scholarship.datastructures.ActionStack;
import com.barangay.scholarship.datastructures.ScholarBST;
import com.barangay.scholarship.datastructures.ScholarshipQueue;
import com.barangay.scholarship.model.*;
import com.barangay.scholarship.service.ScholarshipManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Automated test suite verifying custom Data Structures, Sorting & Searching Algorithms,
 * Budget Tracking, and Undo mechanics without requiring external testing dependencies.
 */
public class TestRunner {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("   BARANGAY SCHOLARSHIP MANAGEMENT SYSTEM - AUTOMATED TEST SUITE  ");
        System.out.println("=================================================================\n");

        runTest("Custom FIFO Queue (ScholarshipQueue)", TestRunner::testQueue);
        runTest("Custom LIFO Stack (ActionStack)", TestRunner::testStack);
        runTest("Custom Binary Search Tree (ScholarBST)", TestRunner::testBST);
        runTest("Merge Sort Algorithm (Academic Merit by GWA)", TestRunner::testMergeSortGWA);
        runTest("Quick Sort Algorithm (Financial Need by Income)", TestRunner::testQuickSortIncome);
        runTest("Quick Sort Algorithm (Alphabetical by Name)", TestRunner::testQuickSortName);
        runTest("Binary Search Algorithm (O(log n) by ID and Name)", TestRunner::testBinarySearch);
        runTest("Linear Search Algorithm (Filter by Purok and School)", TestRunner::testLinearSearch);
        runTest("Eligibility Auto-Checker (Collegiate & Percentage)", TestRunner::testEligibility);
        runTest("Budget Deduction & Stipend Disbursement", TestRunner::testBudgetDeduction);
        runTest("Waitlist Queue Auto-Promotion on Scholar Drop", TestRunner::testWaitlistAutoPromotion);
        runTest("ActionStack 1-Click Undo Rollback", TestRunner::testUndoRollback);
        runTest("Student Inquiry Lookup & Queue Positioning", TestRunner::testStudentInquiryLookup);

        System.out.println("\n-----------------------------------------------------------------");
        System.out.printf("TEST SUMMARY: %d Passed, %d Failed (Total: %d)%n", passed, failed, (passed + failed));
        System.out.println("=================================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void runTest(String name, Runnable test) {
        try {
            test.run();
            System.out.printf("[PASS] %s%n", name);
            passed++;
        } catch (Throwable t) {
            System.out.printf("[FAIL] %s - Reason: %s%n", name, t.getMessage());
            t.printStackTrace(System.out);
            failed++;
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) return;
        if (expected == null || !expected.equals(actual)) {
            throw new AssertionError(message + " (Expected: " + expected + ", Got: " + actual + ")");
        }
    }

    // 1. Custom Queue Test
    private static void testQueue() {
        ScholarshipQueue<String> queue = new ScholarshipQueue<>();
        assertTrue(queue.isEmpty(), "Queue should start empty");
        assertEquals(0, queue.size(), "Queue size should be 0");

        queue.enqueue("Applicant 1");
        queue.enqueue("Applicant 2");
        queue.enqueue("Applicant 3");

        assertEquals(3, queue.size(), "Queue size should be 3");
        assertEquals("Applicant 1", queue.peek(), "Peek should return front element");

        assertEquals("Applicant 1", queue.dequeue(), "First dequeue must be Applicant 1 (FIFO)");
        assertEquals("Applicant 2", queue.dequeue(), "Second dequeue must be Applicant 2 (FIFO)");
        assertEquals("Applicant 3", queue.dequeue(), "Third dequeue must be Applicant 3 (FIFO)");
        assertTrue(queue.isEmpty(), "Queue should be empty after dequeuing all");
    }

    // 2. Custom Stack Test
    private static void testStack() {
        ActionStack<String> stack = new ActionStack<>();
        assertTrue(stack.isEmpty(), "Stack should start empty");

        stack.push("Action 1");
        stack.push("Action 2");
        stack.push("Action 3");

        assertEquals(3, stack.size(), "Stack size should be 3");
        assertEquals("Action 3", stack.peek(), "Peek should return top item");

        assertEquals("Action 3", stack.pop(), "First pop must be Action 3 (LIFO)");
        assertEquals("Action 2", stack.pop(), "Second pop must be Action 2 (LIFO)");
        assertEquals("Action 1", stack.pop(), "Third pop must be Action 1 (LIFO)");
        assertTrue(stack.isEmpty(), "Stack should be empty after popping all");
    }

    // 3. Custom BST Test
    private static void testBST() {
        ScholarBST bst = new ScholarBST();
        assertTrue(bst.isEmpty(), "BST should start empty");

        Applicant a1 = new Applicant("APP-001", "Maria Santos", "m@e.ph", "091", "PLM", "2nd", 1.25, 12000, "Purok 1");
        Applicant a2 = new Applicant("APP-002", "Juan Dizon", "j@e.ph", "092", "PUP", "3rd", 1.50, 9000, "Purok 2");
        Applicant a3 = new Applicant("APP-003", "Angelica Reyes", "a@e.ph", "093", "UP", "1st", 1.30, 15000, "Purok 3");

        Scholar s1 = new Scholar(a1, "SCH-002", 3000, "1st Sem");
        Scholar s2 = new Scholar(a2, "SCH-001", 3000, "1st Sem");
        Scholar s3 = new Scholar(a3, "SCH-003", 3000, "1st Sem");

        bst.insert(s1);
        bst.insert(s2);
        bst.insert(s3);

        assertEquals(3, bst.size(), "BST should have 3 nodes");

        // Search
        Scholar found = bst.searchById("SCH-001");
        assertTrue(found != null, "Should find scholar SCH-001");
        assertEquals("Juan Dizon", found.getFullName(), "Found scholar name should match");

        // In-Order Traversal should return sorted by ID: SCH-001, SCH-002, SCH-003
        List<Scholar> sorted = bst.inOrderTraversal();
        assertEquals(3, sorted.size(), "Traversal size should be 3");
        assertEquals("SCH-001", sorted.get(0).getScholarId(), "First element should be SCH-001");
        assertEquals("SCH-002", sorted.get(1).getScholarId(), "Second element should be SCH-002");
        assertEquals("SCH-003", sorted.get(2).getScholarId(), "Third element should be SCH-003");

        // Deletion
        boolean deleted = bst.delete("SCH-002");
        assertTrue(deleted, "Delete should return true");
        assertEquals(2, bst.size(), "BST size should be 2 after delete");
        assertTrue(bst.searchById("SCH-002") == null, "SCH-002 should no longer exist in tree");
    }

    // 4. Merge Sort Test
    private static void testMergeSortGWA() {
        List<Applicant> list = new ArrayList<>();
        list.add(new Applicant("APP-01", "Student A", "", "", "", "", 2.25, 10000, "Purok 1"));
        list.add(new Applicant("APP-02", "Student B", "", "", "", "", 1.25, 10000, "Purok 1"));
        list.add(new Applicant("APP-03", "Student C", "", "", "", "", 1.75, 10000, "Purok 1"));

        AlgorithmUtils.mergeSortByGWA(list, true);

        // Top honors should be first: 1.25, then 1.75, then 2.25
        assertEquals(1.25, list.get(0).getGwa(), "Rank 1 GWA");
        assertEquals(1.75, list.get(1).getGwa(), "Rank 2 GWA");
        assertEquals(2.25, list.get(2).getGwa(), "Rank 3 GWA");
    }

    // 5. Quick Sort by Income Test
    private static void testQuickSortIncome() {
        List<Applicant> list = new ArrayList<>();
        list.add(new Applicant("APP-01", "Student A", "", "", "", "", 1.5, 18000.0, "Purok 1"));
        list.add(new Applicant("APP-02", "Student B", "", "", "", "", 1.5, 8000.0, "Purok 1"));
        list.add(new Applicant("APP-03", "Student C", "", "", "", "", 1.5, 12000.0, "Purok 1"));

        AlgorithmUtils.quickSortByIncome(list, true);

        // Lowest income first: 8000, 12000, 18000
        assertEquals(8000.0, list.get(0).getMonthlyFamilyIncome(), "Rank 1 Income");
        assertEquals(12000.0, list.get(1).getMonthlyFamilyIncome(), "Rank 2 Income");
        assertEquals(18000.0, list.get(2).getMonthlyFamilyIncome(), "Rank 3 Income");
    }

    // 6. Quick Sort by Name Test
    private static void testQuickSortName() {
        List<Applicant> list = new ArrayList<>();
        list.add(new Applicant("APP-01", "Santos, Maria", "", "", "", "", 1.5, 10000.0, "Purok 1"));
        list.add(new Applicant("APP-02", "Aquino, Benigno", "", "", "", "", 1.5, 10000.0, "Purok 1"));
        list.add(new Applicant("APP-03", "Dela Cruz, Juan", "", "", "", "", 1.5, 10000.0, "Purok 1"));

        AlgorithmUtils.quickSortByName(list, true);

        assertEquals("Aquino, Benigno", list.get(0).getFullName(), "Alphabetical 1");
        assertEquals("Dela Cruz, Juan", list.get(1).getFullName(), "Alphabetical 2");
        assertEquals("Santos, Maria", list.get(2).getFullName(), "Alphabetical 3");
    }

    // 7. Binary Search Test
    private static void testBinarySearch() {
        List<Applicant> list = new ArrayList<>();
        list.add(new Applicant("APP-001", "Aquino, Benigno", "", "", "", "", 1.5, 10000, "P1"));
        list.add(new Applicant("APP-002", "Dela Cruz, Juan", "", "", "", "", 1.5, 10000, "P2"));
        list.add(new Applicant("APP-003", "Santos, Maria", "", "", "", "", 1.5, 10000, "P3"));

        // Binary Search by ID
        int idx = AlgorithmUtils.binarySearchById(list, "APP-002");
        assertEquals(1, idx, "Binary search by ID should return index 1");

        int notFound = AlgorithmUtils.binarySearchById(list, "APP-999");
        assertEquals(-1, notFound, "Binary search for non-existent ID should return -1");

        // Binary Search by Name
        int nameIdx = AlgorithmUtils.binarySearchByName(list, "Santos, Maria");
        assertEquals(2, nameIdx, "Binary search by Name should return index 2");
    }

    // 8. Linear Search Test
    private static void testLinearSearch() {
        List<Applicant> list = new ArrayList<>();
        list.add(new Applicant("APP-01", "A", "", "", "PUP Manila", "", 1.5, 10000, "Purok 1 Maligaya"));
        list.add(new Applicant("APP-02", "B", "", "", "UP Diliman", "", 1.5, 10000, "Purok 2 Pag-asa"));
        list.add(new Applicant("APP-03", "C", "", "", "PUP Manila", "", 1.5, 10000, "Purok 1 Maligaya"));

        List<Applicant> p1 = AlgorithmUtils.linearSearchByPurok(list, "Purok 1");
        assertEquals(2, p1.size(), "Purok 1 linear search should match 2 students");

        List<Applicant> pup = AlgorithmUtils.linearSearchBySchool(list, "PUP");
        assertEquals(2, pup.size(), "PUP school linear search should match 2 students");
    }

    // 9. Eligibility Auto-Checker Test
    private static void testEligibility() {
        // College Scale: 1.0 - 2.50 eligible, income <= 25,000
        Applicant collegeOk = new Applicant("APP-01", "C1", "", "", "PLM", "1st", 1.75, 15000, "P1");
        assertTrue(collegeOk.isEligible(), "College 1.75 & 15k income should be eligible");

        Applicant collegeHighGwa = new Applicant("APP-02", "C2", "", "", "PLM", "1st", 3.25, 15000, "P1");
        assertTrue(!collegeHighGwa.isEligible(), "College 3.25 GWA should be ineligible");

        Applicant highIncome = new Applicant("APP-03", "C3", "", "", "PLM", "1st", 1.25, 45000, "P1");
        assertTrue(!highIncome.isEligible(), "45k income should be ineligible");

        // Percentage Scale: 85 - 100 eligible
        Applicant hsOk = new Applicant("APP-04", "H1", "", "", "NHS", "G12", 92.0, 12000, "P1");
        assertTrue(hsOk.isEligible(), "HS 92% & 12k income should be eligible");

        Applicant hsLow = new Applicant("APP-05", "H2", "", "", "NHS", "G12", 81.0, 12000, "P1");
        assertTrue(!hsLow.isEligible(), "HS 81% should be ineligible");
    }

    // 10. Budget Deduction Test
    private static void testBudgetDeduction() {
        ScholarshipManager mgr = new ScholarshipManager(150000.0, 3000.0);
        Applicant a = new Applicant("APP-01", "Juan", "", "", "PLM", "1st", 1.5, 10000, "P1");
        Scholar s = new Scholar(a, "SCH-001", 3000.0, "1st Sem");
        mgr.recordDirectApproved(s);

        assertEquals(150000.0, mgr.getRemainingBudget(), "Initial budget should be 150,000");

        mgr.releaseStipend("SCH-001");

        assertEquals(147000.0, mgr.getRemainingBudget(), "Budget after 3,000 release should be 147,000");
        assertEquals(3000.0, mgr.getDisbursedBudget(), "Disbursed budget should be 3,000");
        assertTrue(s.isStipendDisbursed(), "Scholar stipendDisbursed flag should be true");
    }

    // 11. Waitlist Auto-Promotion Test
    private static void testWaitlistAutoPromotion() {
        ScholarshipManager mgr = new ScholarshipManager(150000.0, 3000.0);

        Applicant a1 = new Applicant("APP-01", "Scholar One", "", "", "PLM", "1st", 1.5, 10000, "P1");
        Scholar s1 = new Scholar(a1, "SCH-001", 3000.0, "1st Sem");
        mgr.recordDirectApproved(s1);

        Applicant waitlisted = new Applicant("APP-02", "Waitlist Student", "", "", "PUP", "2nd", 1.8, 11000, "P2");
        mgr.recordDirectWaitlisted(waitlisted);

        assertEquals(1, mgr.getWaitlistQueue().size(), "Waitlist should have 1 applicant");

        // Drop scholar -> should auto-dequeue and promote waitlist candidate!
        Scholar promoted = mgr.dropScholarAndPromoteWaitlist("SCH-001", "Dropped out");

        assertTrue(promoted != null, "Promoted scholar should not be null");
        assertEquals("Waitlist Student", promoted.getFullName(), "Promoted scholar should be Waitlist Student");
        assertEquals(0, mgr.getWaitlistQueue().size(), "Waitlist queue should now be empty");
        assertTrue(mgr.getScholarDirectory().searchById(promoted.getScholarId()) != null, "Promoted scholar should be in BST");
    }

    // 12. 1-Click Undo Test
    private static void testUndoRollback() {
        ScholarshipManager mgr = new ScholarshipManager(150000.0, 3000.0);

        Applicant a = new Applicant("APP-01", "Maria", "", "", "UP", "1st", 1.25, 12000, "P1");
        Scholar s = new Scholar(a, "SCH-001", 3000.0, "1st Sem");
        mgr.recordDirectApproved(s);

        // Disburse
        mgr.releaseStipend("SCH-001");
        assertEquals(147000.0, mgr.getRemainingBudget(), "Budget after disbursement");
        assertTrue(s.isStipendDisbursed(), "Stipend claimed");

        // 1-Click Undo
        boolean undone = mgr.undoLastAction();
        assertTrue(undone, "Undo should succeed");
        assertEquals(150000.0, mgr.getRemainingBudget(), "Budget should be rolled back to 150,000");
        assertTrue(!s.isStipendDisbursed(), "Scholar stipend should be reversed to unclaimed");
    }

    // 13. Student Inquiry Lookup & Queue Positioning Test
    private static void testStudentInquiryLookup() {
        ScholarshipManager mgr = new ScholarshipManager(150000.0, 3000.0);

        Applicant a1 = mgr.registerApplicant("Alice Guo", "alice@school.edu", "0911", "PLM", "1st Year College", 1.50, 10000, "Purok 1 Maligaya", null);
        Applicant a2 = mgr.registerApplicant("Bob Ong", "bob@school.edu", "0922", "PUP", "2nd Year College", 1.75, 12000, "Purok 2 Pag-asa", null);

        // Queue positions
        assertEquals(1, mgr.getQueuePosition(a1.getApplicantId()), "Alice should be #1 in screening queue");
        assertEquals(2, mgr.getQueuePosition(a2.getApplicantId()), "Bob should be #2 in screening queue");
        assertEquals(-1, mgr.getQueuePosition("APP-NONEXISTENT"), "Non-existent applicant should return -1");

        // Search by substring name
        List<Applicant> searchAlice = mgr.findApplicantsByIdOrName("Alice");
        assertEquals(1, searchAlice.size(), "Search 'Alice' should return 1 applicant");
        assertEquals(a1.getApplicantId(), searchAlice.get(0).getApplicantId(), "Applicant ID should match");

        // Search by exact ID
        List<Applicant> searchId = mgr.findApplicantsByIdOrName(a2.getApplicantId());
        assertEquals(1, searchId.size(), "Search by ID should return Bob");
        assertEquals("Bob Ong", searchId.get(0).getFullName(), "Applicant name should match");
    }
}
