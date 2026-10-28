package com.barangay.scholarship.test;

import com.barangay.scholarship.algorithms.AlgorithmUtils;
import com.barangay.scholarship.datastructures.ActionStack;
import com.barangay.scholarship.datastructures.ScholarBST;
import com.barangay.scholarship.datastructures.ScholarshipQueue;
import com.barangay.scholarship.model.*;
import com.barangay.scholarship.service.ScholarshipManager;
import com.barangay.scholarship.ui.components.StatCard;
import com.barangay.scholarship.ui.student.StudentApplicationView;
import com.barangay.scholarship.ui.student.StudentGuidelinesView;
import com.barangay.scholarship.ui.views.DashboardView;
import com.barangay.scholarship.util.FileUploadManager;
import com.barangay.scholarship.util.SampleDataSeeder;
import com.barangay.scholarship.util.ValidationUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

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
        runTest("Document Checklist Digital File Tracking", TestRunner::testDocumentChecklistWithFiles);
        runTest("File Upload Manager & Storage Persistence", TestRunner::testFileUploadManager);
        runTest("Applicant Registration with Document Attachments", TestRunner::testRegisterApplicantWithFileUploads);
        runTest("Applicant Form Validation & Anti-Gibberish Rules", TestRunner::testFormValidationRules);
        runTest("Student Portal formatted inputs and registration", TestRunner::testStudentPortalRequestedRegistration);
        runTest("Student Portal guidelines render formatted content", TestRunner::testStudentGuidelinesFormatting);
        runTest("Seeded startup dashboard initial state", TestRunner::testInitialDashboardState);
        runTest("TC-07, TC-08, TC-09: Kent Assigned QA Test Cases (Ranking & Payout)",
                () -> KentAssignedCasesTest.main(new String[0]));

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

    private static void testInitialDashboardState() {
        ScholarshipManager manager = new ScholarshipManager(150000.0, 3000.0);
        SampleDataSeeder.seedDemoData(manager);

        assertEquals(20, manager.getMasterList().size(), "Startup should preload 20 benchmark applicants");
        assertEquals(150000.0, manager.getTotalBudget(), "Initial total budget");
        assertEquals(0.0, manager.getDisbursedBudget(), "Initial disbursed budget");
        assertEquals(150000.0, manager.getRemainingBudget(), "Initial remaining budget");
        assertEquals(3000.0, manager.getGrantPerScholar(), "Default grant");
        assertEquals(10, manager.getCountByStatus(ApplicationStatus.APPROVED), "Approved scholars");
        assertEquals(17, manager.getVerifiedApplicantCount(), "Screened / verified applicants");
        assertEquals(4, manager.getWaitlistQueue().size(), "Waitlisted applicants");

        AtomicInteger changeNotifications = new AtomicInteger();
        manager.addChangeListener(changeNotifications::incrementAndGet);
        manager.setTotalBudget(manager.getTotalBudget());
        assertEquals(1, changeNotifications.get(), "Manager change listeners should receive updates");

        AtomicReference<DashboardView> dashboardRef = new AtomicReference<>();
        try {
            SwingUtilities.invokeAndWait(() -> dashboardRef.set(new DashboardView(manager, ignored -> { })));
        } catch (Exception e) {
            throw new AssertionError("Dashboard should initialize on the EDT", e);
        }

        DashboardView dashboard = dashboardRef.get();
        assertDashboardCard(dashboard, "Remaining Budget", String.format("₱%,.2f", 150000.0));
        assertDashboardCardSubtitle(dashboard, "Remaining Budget",
                String.format("Total: ₱%,.2f | Disbursed: ₱%,.2f", 150000.0, 0.0));
        assertDashboardCard(dashboard, "Approved Scholars", "10");
        assertDashboardCard(dashboard, "Waitlist Queue", "4 Students");
        assertDashboardCard(dashboard, "Screening Line", "5 In Line");
        assertDashboardText(dashboard, String.format(
                "Default Grant: ₱%,.2f | Available Slots by Budget: %d scholars", 3000.0, 50));
        assertEquals(4, countDashboardCards(dashboard), "Dashboard should retain its original four-card layout");
    }

    private static void assertDashboardCard(Container container, String title, String expectedValue) {
        StatCard card = findDashboardCard(container, title);
        assertTrue(card != null, "Missing dashboard card: " + title);

        boolean hasExpectedValue = false;
        for (Component child : card.getComponents()) {
            if (child instanceof JLabel && ((JLabel) child).getText().equals(expectedValue)) {
                hasExpectedValue = true;
                break;
            }
        }
        assertTrue(hasExpectedValue,
                title + " dashboard card should display " + expectedValue);
    }

    private static void assertDashboardCardSubtitle(Container container, String title, String expectedSubtitle) {
        StatCard card = findDashboardCard(container, title);
        assertTrue(card != null, "Missing dashboard card: " + title);
        assertDashboardText(card, expectedSubtitle);
    }

    private static void assertDashboardText(Container container, String expectedText) {
        assertTrue(findDashboardText(container, expectedText),
                "Dashboard should display: " + expectedText);
    }

    private static boolean findDashboardText(Container container, String text) {
        for (Component component : container.getComponents()) {
            if (component instanceof JLabel && ((JLabel) component).getText().equals(text)) {
                return true;
            }
            if (component instanceof Container && findDashboardText((Container) component, text)) {
                return true;
            }
        }
        return false;
    }

    private static int countDashboardCards(Container container) {
        int count = 0;
        for (Component component : container.getComponents()) {
            if (component instanceof StatCard) {
                count++;
            }
            if (component instanceof Container) {
                count += countDashboardCards((Container) component);
            }
        }
        return count;
    }

    private static StatCard findDashboardCard(Container container, String title) {
        for (Component component : container.getComponents()) {
            if (component instanceof StatCard) {
                for (Component child : ((StatCard) component).getComponents()) {
                    if (child instanceof JLabel
                            && ((JLabel) child).getText().equals(title.toUpperCase())) {
                        return (StatCard) component;
                    }
                }
            }
            if (component instanceof Container) {
                StatCard card = findDashboardCard((Container) component, title);
                if (card != null) {
                    return card;
                }
            }
        }
        return null;
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

    // 14. Document Checklist Digital File Tracking Test
    private static void testDocumentChecklistWithFiles() {
        DocumentChecklist docs = new DocumentChecklist();
        assertTrue(!docs.hasAnyUploadedFiles(), "Empty checklist should have 0 uploaded files");
        assertEquals(0, docs.getUploadedFilesCount(), "Count should be 0");

        docs.setIndigencyFilePath("data/uploads/APP-2026-001/indigency_cert.pdf");
        assertTrue(docs.isCertificateOfIndigency(), "Setting file path should auto-mark requirement complete");
        assertTrue(docs.hasIndigencyFile(), "Should report hasIndigencyFile true");
        assertEquals("indigency_cert.pdf", docs.getIndigencyFileName(), "File name extraction");
        assertEquals(1, docs.getUploadedFilesCount(), "Uploaded files count should be 1");

        docs.setGradesFilePath("data/uploads/APP-2026-001/grades_tor.jpg");
        docs.setEnrollmentForm(true);
        docs.setValidBarangayOrSchoolId(true);

        assertTrue(docs.isComplete(), "All 4 requirements met (2 digital, 2 hardcopy)");
        assertEquals(2, docs.getUploadedFilesCount(), "Uploaded files count should be 2");
        assertEquals("4/4 Complete", docs.getStatusSummary(), "Status summary check");
    }

    // 15. File Upload Manager & Storage Persistence Test
    private static void testFileUploadManager() {
        // Size formatting
        assertEquals("500 B", FileUploadManager.formatFileSize(500), "Bytes formatting");
        assertEquals("1.5 KB", FileUploadManager.formatFileSize(1536), "Kilobytes formatting");
        assertEquals("2.00 MB", FileUploadManager.formatFileSize(2 * 1024 * 1024), "Megabytes formatting");

        // Temporary file creation and saving
        File tempFile = new File("test_doc.txt");
        try {
            try (FileWriter fw = new FileWriter(tempFile)) {
                fw.write("BARANGAY SCHOLARSHIP TEST ATTACHMENT CONTENT");
            }
            assertTrue(FileUploadManager.isValidDocumentFile(tempFile), "Test document should be valid");

            String savedPath = FileUploadManager.saveUploadedFile("APP-TEST-999", "Indigency", tempFile);
            File savedFile = new File(savedPath);

            assertTrue(savedFile.exists(), "Saved file must physically exist in storage directory");
            assertTrue(savedFile.length() > 0, "Saved file length must be > 0");
            assertTrue(savedPath.contains("APP-TEST-999"), "Path must contain applicant folder");

            // Clean up test file
            savedFile.delete();
            File dir = savedFile.getParentFile();
            if (dir != null && dir.exists()) dir.delete();
        } catch (IOException e) {
            throw new RuntimeException("FileUploadManager test failed: " + e.getMessage(), e);
        } finally {
            if (tempFile.exists()) tempFile.delete();
        }
    }

    // 16. Applicant Registration with Document Attachments Test
    private static void testRegisterApplicantWithFileUploads() {
        ScholarshipManager mgr = new ScholarshipManager(150000.0, 3000.0);

        File dummyFile = new File("dummy_cor.pdf");
        try {
            try (FileWriter fw = new FileWriter(dummyFile)) {
                fw.write("%PDF-1.4 DUMMY OFFICIAL ENROLLMENT RECEIPT");
            }

            DocumentChecklist docs = new DocumentChecklist(true, true, false, true);

            Applicant app = mgr.registerApplicant(
                    "Clarisse Santos", "clarisse@univ.ph", "09181234567",
                    "University of the Philippines", "2nd Year College",
                    1.40, 14000.0, "Purok 1 Maligaya",
                    docs,
                    null, null, dummyFile, null
            );

            assertTrue(app != null, "Registered applicant should not be null");
            assertTrue(app.getDocuments().hasEnrollmentFile(), "Applicant should have enrollment file attached");
            assertTrue(app.getDocuments().isEnrollmentForm(), "Enrollment requirement should be satisfied");
            assertTrue(app.getDocuments().isComplete(), "All 4 requirements should now be complete");
            assertEquals(ApplicationStatus.VERIFIED, app.getStatus(), "Status should be VERIFIED since docs are complete");

            String savedPath = app.getDocuments().getEnrollmentFilePath();
            assertTrue(savedPath != null && new File(savedPath).exists(), "Saved enrollment document must exist on disk");

            // Clean up
            new File(savedPath).delete();
            new File(savedPath).getParentFile().delete();
        } catch (IOException e) {
            throw new RuntimeException("Registration with files failed: " + e.getMessage(), e);
        } finally {
            if (dummyFile.exists()) dummyFile.delete();
        }
    }

    // 17. Applicant Form Validation & Anti-Gibberish Rules Test
    private static void testFormValidationRules() {
        // Full Name Validation
        assertTrue(ValidationUtils.validateFullName("fewfewf") != null, "Gibberish name should be rejected");
        assertTrue(ValidationUtils.validateFullName("aaaaaaa") != null, "Repeated character name should be rejected");
        assertTrue(ValidationUtils.validateFullName("x") != null, "Too short name should be rejected");
        assertTrue(ValidationUtils.validateFullName("JustOneWord") != null, "Single word name without surname should be rejected");
        assertTrue(ValidationUtils.validateFullName("A".repeat(80)) != null, "Excessively long name should be rejected");
        assertTrue(ValidationUtils.validateFullName("Dela Cruz, Juan M.") == null, "Valid Filipino name with comma should pass");
        assertTrue(ValidationUtils.validateFullName("Maria Santos") == null, "Valid Filipino name with space should pass");

        // Email Validation
        assertTrue(ValidationUtils.validateEmail("fewfew") != null, "Nonsense email without domain should be rejected");
        assertTrue(ValidationUtils.validateEmail("test@") != null, "Incomplete email should be rejected");
        assertTrue(ValidationUtils.validateEmail("a".repeat(80) + "@school.edu") != null, "Excessively long email should be rejected");
        assertTrue(ValidationUtils.validateEmail("student@school.edu.ph") == null, "Valid institutional email should pass");
        assertTrue(ValidationUtils.validateEmail("maria.delacruz@gmail.com") == null, "Valid standard email should pass");

        // Contact Number (Mobile) Validation
        assertTrue(ValidationUtils.validatePhone("ewfewfew") != null, "Nonsense letters in phone should be rejected");
        assertTrue(ValidationUtils.validatePhone("12345") != null, "Short number should be rejected");
        assertTrue(ValidationUtils.validatePhone("08123456789") != null, "Non-09 starting number should be rejected");
        assertTrue(ValidationUtils.validatePhone("09171234567") == null, "Valid 0917 standard 11-digit mobile should pass");
        assertTrue(ValidationUtils.validatePhone("0918-123-4567") == null, "Formatted mobile with dashes should pass");
        assertTrue(ValidationUtils.validatePhone("+639171234567") == null, "International format +639 should pass");

        // School / University Validation
        assertTrue(ValidationUtils.validateSchool("fewfwef") != null, "Gibberish school name should be rejected");
        assertTrue(ValidationUtils.validateSchool("zzzzzzz") != null, "Repeated character school name should be rejected");
        assertTrue(ValidationUtils.validateSchool("ab") != null, "Too short school name should be rejected");
        assertTrue(ValidationUtils.validateSchool("A".repeat(100)) != null, "Excessively long school name should be rejected");
        assertTrue(ValidationUtils.validateSchool("University of the Philippines Diliman") == null, "Valid university name should pass");
        assertTrue(ValidationUtils.validateSchool("Batangas State University - Alangilan (TNEU)") == null, "Valid university with symbols should pass");
    }

    private static void testStudentPortalRequestedRegistration() {
        assertEquals(18000.0, ValidationUtils.parseMonthlyIncome("₱18,000"),
                "Peso-formatted monthly income should parse");
        assertEquals(18000.0, ValidationUtils.parseMonthlyIncome("18,000"),
                "Grouped monthly income should parse");
        assertTrue(rejectsMonthlyIncome("18,00"), "Incorrectly grouped monthly income should be rejected");
        assertTrue(rejectsMonthlyIncome("₱18,000 pesos"), "Non-numeric monthly income should be rejected");

        ScholarshipManager manager = new ScholarshipManager();
        SampleDataSeeder.seedDemoData(manager);
        int initialApplicantCount = manager.getMasterList().size();
        int initialQueueSize = manager.getScreeningQueue().size();

        AtomicReference<StudentApplicationView> viewRef = new AtomicReference<>();
        try {
            SwingUtilities.invokeAndWait(() -> viewRef.set(new StudentApplicationView(manager, ignored -> { })));
        } catch (Exception e) {
            throw new AssertionError("Student application view should initialize on the EDT", e);
        }
        StudentApplicationView view = viewRef.get();

        try {
            Field yearField = StudentApplicationView.class.getDeclaredField("cmbYear");
            Field purokField = StudentApplicationView.class.getDeclaredField("cmbPurok");
            yearField.setAccessible(true);
            purokField.setAccessible(true);
            JComboBox<?> year = (JComboBox<?>) yearField.get(view);
            JComboBox<?> purok = (JComboBox<?>) purokField.get(view);
            assertTrue(year.isEditable(), "Year level should allow the requested free-text value");
            assertTrue(purok.isEditable(), "Purok should allow the requested free-text value");
            year.setSelectedItem("College 2nd Year");
            purok.setSelectedItem("Purok 3");
            assertEquals("College 2nd Year", year.getSelectedItem(), "Requested year level should be retained");
            assertEquals("Purok 3", purok.getSelectedItem(), "Requested purok should be retained");

            Field gwaField = StudentApplicationView.class.getDeclaredField("txtGwa");
            Field incomeField = StudentApplicationView.class.getDeclaredField("txtIncome");
            Method updatePreview = StudentApplicationView.class.getDeclaredMethod("updateEligibilityPreview");
            gwaField.setAccessible(true);
            incomeField.setAccessible(true);
            updatePreview.setAccessible(true);
            ((JTextField) gwaField.get(view)).setText("1.75");
            ((JTextField) incomeField.get(view)).setText("₱18,000");
            updatePreview.invoke(view);
            Field eligibilityText = StudentApplicationView.class.getDeclaredField("lblEligibilityText");
            eligibilityText.setAccessible(true);
            assertTrue(((JLabel) eligibilityText.get(view)).getText().contains("ELIGIBLE"),
                    "GWA 1.75 with ₱18,000 income should pass the Student Portal eligibility preview");
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Could not verify student portal input controls", e);
        }

        assertTrue(ValidationUtils.validateFullName("Juan Dela Cruz") == null, "Requested name should pass");
        assertTrue(ValidationUtils.validateEmail("juan@dlsl.edu.ph") == null, "Requested email should pass");
        assertTrue(ValidationUtils.validatePhone("09123456789") == null, "Requested phone should pass");
        assertTrue(ValidationUtils.validateSchool("Batangas State University") == null, "Requested school should pass");
        assertEquals(1.75, ValidationUtils.parseGwa("1.75"), "Decimal collegiate GWA should parse");
        assertEquals(94.5, ValidationUtils.parseGwa("94.5"), "Percentage grade should parse");
        assertTrue(rejectsGwa("1.75x"), "Nonnumeric GWA should be rejected");
        assertTrue(rejectsGwa("70"), "Out-of-range percentage grade should be rejected");

        Applicant applicant = manager.registerApplicant(
                "Juan Dela Cruz", "juan@dlsl.edu.ph", "09123456789",
                "Batangas State University", "College 2nd Year", ValidationUtils.parseGwa("1.75"),
                ValidationUtils.parseMonthlyIncome("₱18,000"), "Purok 3", new DocumentChecklist()
        );

        assertEquals("APP-2026-021", applicant.getApplicantId(), "Seeded application ID");
        assertEquals(ApplicationStatus.PENDING_VERIFICATION, applicant.getStatus(),
                "Applicant should await document verification");
        assertEquals(initialApplicantCount + 1, manager.getMasterList().size(),
                "Applicant count should increment immediately");
        assertEquals(initialQueueSize + 1, manager.getScreeningQueue().size(),
                "Applicant should be added to the screening queue");
        assertEquals(initialQueueSize + 1, manager.getQueuePosition(applicant.getApplicantId()),
                "New applicant should appear in screening queue");
        assertEquals("College 2nd Year", applicant.getYearLevel(), "Requested year level should be stored");
        assertEquals("Purok 3", applicant.getPurok(), "Requested purok should be stored");
    }

    private static boolean rejectsMonthlyIncome(String income) {
        try {
            ValidationUtils.parseMonthlyIncome(income);
            return false;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    private static boolean rejectsGwa(String gwa) {
        try {
            ValidationUtils.parseGwa(gwa);
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    private static void testStudentGuidelinesFormatting() {
        AtomicReference<StudentGuidelinesView> viewRef = new AtomicReference<>();
        try {
            SwingUtilities.invokeAndWait(() -> viewRef.set(new StudentGuidelinesView()));
        } catch (Exception e) {
            throw new AssertionError("Student guidelines view should initialize on the EDT", e);
        }

        List<JTextArea> descriptions = new ArrayList<>();
        collectTextAreas(viewRef.get(), descriptions);
        JTextArea incomeGuideline = descriptions.stream()
                .filter(area -> area.getText().contains("₱25,000.00"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Income eligibility guideline should be present"));
        assertTrue(!incomeGuideline.getText().contains("<b>"),
                "Income guideline should not show HTML markup");

        JTextArea academicGuideline = descriptions.stream()
                .filter(area -> area.getText().contains("Collegiate Scale"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Academic standing guideline should be present"));
        assertTrue(academicGuideline.getText().contains("\nPercentage Scale:"),
                "Academic scales should render as separate lines");
        assertTrue(academicGuideline.getRows() >= 2,
                "Academic guideline should reserve space for both scales");
    }

    private static void collectTextAreas(Container container, List<JTextArea> textAreas) {
        for (Component component : container.getComponents()) {
            if (component instanceof JTextArea) {
                textAreas.add((JTextArea) component);
            } else if (component instanceof Container) {
                collectTextAreas((Container) component, textAreas);
            }
        }
    }
}
