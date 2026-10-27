package com.barangay.scholarship.service;

import com.barangay.scholarship.algorithms.AlgorithmUtils;
import com.barangay.scholarship.datastructures.ActionStack;
import com.barangay.scholarship.datastructures.ScholarBST;
import com.barangay.scholarship.datastructures.ScholarshipQueue;
import com.barangay.scholarship.model.*;
import com.barangay.scholarship.util.FileUploadManager;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Core controller service orchestrating data structures, algorithms, and scholarship transactions.
 */
public class ScholarshipManager implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum RankingCriteria {
        MERIT_GWA("Academic Merit (GWA)", "Ranks top academic achievers first using Merge Sort"),
        FINANCIAL_NEED("Financial Need (Household Income)", "Prioritizes lowest household incomes first using Quick Sort");

        private final String title;
        private final String description;

        RankingCriteria(String title, String description) {
            this.title = title;
            this.description = description;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public String toString() {
            return title;
        }
    }

    // Core Data Structures
    private final List<Applicant> masterList;
    private final ScholarshipQueue<Applicant> screeningQueue;
    private final ScholarshipQueue<Applicant> waitlistQueue;
    private final ActionStack<ActionRecord> actionHistory;
    private final ScholarBST scholarDirectory;

    // Budget Tracker State
    private double totalBudget;
    private double grantPerScholar;
    private double disbursedBudget;

    // Change listeners for UI reactivity
    private transient List<Runnable> listeners;

    // Sequence counters
    private int applicantCounter = 1;
    private int scholarCounter = 1;
    private int actionCounter = 1;

    public ScholarshipManager() {
        this(150000.0, 3000.0);
    }

    public ScholarshipManager(double totalBudget, double grantPerScholar) {
        this.masterList = new ArrayList<>();
        this.screeningQueue = new ScholarshipQueue<>();
        this.waitlistQueue = new ScholarshipQueue<>();
        this.actionHistory = new ActionStack<>();
        this.scholarDirectory = new ScholarBST();
        this.totalBudget = totalBudget;
        this.grantPerScholar = grantPerScholar;
        this.disbursedBudget = 0.0;
        this.listeners = new ArrayList<>();
    }

    public synchronized void addChangeListener(Runnable listener) {
        if (listeners == null) listeners = new ArrayList<>();
        listeners.add(listener);
    }

    public synchronized void notifyChange() {
        if (listeners != null) {
            for (Runnable listener : listeners) {
                try {
                    listener.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // =========================================================================
    // BUDGET TRACKER
    // =========================================================================

    public synchronized double getTotalBudget() {
        return totalBudget;
    }

    public synchronized void setTotalBudget(double totalBudget) {
        this.totalBudget = totalBudget;
        notifyChange();
    }

    public synchronized double getGrantPerScholar() {
        return grantPerScholar;
    }

    public synchronized void setGrantPerScholar(double grantPerScholar) {
        this.grantPerScholar = grantPerScholar;
        notifyChange();
    }

    public synchronized double getDisbursedBudget() {
        return disbursedBudget;
    }

    public synchronized double getRemainingBudget() {
        return Math.max(0.0, totalBudget - disbursedBudget);
    }

    public synchronized int getMaxPossibleSlots() {
        if (grantPerScholar <= 0) return 0;
        return (int) (totalBudget / grantPerScholar);
    }

    public synchronized int getRemainingSlotsByBudget() {
        if (grantPerScholar <= 0) return 0;
        return (int) (getRemainingBudget() / grantPerScholar);
    }

    // =========================================================================
    // CORE TRANSACTION 1: APPLICANT REGISTRATION & PROFILING
    // =========================================================================

    public synchronized String generateNextApplicantId() {
        while (findApplicantById(String.format("APP-2026-%03d", applicantCounter)) != null) {
            applicantCounter++;
        }
        return String.format("APP-2026-%03d", applicantCounter++);
    }

    public synchronized String generateNextScholarId() {
        while (scholarDirectory.searchById(String.format("SCH-2026-%03d", scholarCounter)) != null) {
            scholarCounter++;
        }
        return String.format("SCH-2026-%03d", scholarCounter++);
    }

    public synchronized void setApplicantCounter(int counter) {
        this.applicantCounter = counter;
    }

    public synchronized void setScholarCounter(int counter) {
        this.scholarCounter = counter;
    }

    private synchronized String generateNextActionId() {
        return String.format("ACT-%04d", actionCounter++);
    }

    /**
     * Registers a new applicant, runs auto-eligibility evaluation, and adds them to screening queue.
     */
    public synchronized Applicant registerApplicant(String name, String email, String phone,
                                                    String school, String year, double gwa,
                                                    double income, String purok,
                                                    DocumentChecklist docs) {
        return registerApplicant(name, email, phone, school, year, gwa, income, purok, docs, null, null, null, null);
    }

    /**
     * Registers a new applicant with optional digital document attachments.
     */
    public synchronized Applicant registerApplicant(String name, String email, String phone,
                                                    String school, String year, double gwa,
                                                    double income, String purok,
                                                    DocumentChecklist docs,
                                                    File indigencyFile, File gradesFile,
                                                    File enrollmentFile, File idFile) {
        String appId = generateNextApplicantId();
        Applicant applicant = new Applicant(appId, name, email, phone, school, year, gwa, income, purok);
        if (docs == null) {
            docs = new DocumentChecklist();
        }

        // Persist any uploaded files into data/uploads/<appId>/
        if (indigencyFile != null && indigencyFile.exists() && indigencyFile.isFile()) {
            try {
                String saved = FileUploadManager.saveUploadedFile(appId, "Indigency", indigencyFile);
                docs.setIndigencyFilePath(saved);
            } catch (Exception e) {
                System.err.println("Failed to save indigency file: " + e.getMessage());
            }
        }
        if (gradesFile != null && gradesFile.exists() && gradesFile.isFile()) {
            try {
                String saved = FileUploadManager.saveUploadedFile(appId, "Grades", gradesFile);
                docs.setGradesFilePath(saved);
            } catch (Exception e) {
                System.err.println("Failed to save grades file: " + e.getMessage());
            }
        }
        if (enrollmentFile != null && enrollmentFile.exists() && enrollmentFile.isFile()) {
            try {
                String saved = FileUploadManager.saveUploadedFile(appId, "Enrollment", enrollmentFile);
                docs.setEnrollmentFilePath(saved);
            } catch (Exception e) {
                System.err.println("Failed to save enrollment file: " + e.getMessage());
            }
        }
        if (idFile != null && idFile.exists() && idFile.isFile()) {
            try {
                String saved = FileUploadManager.saveUploadedFile(appId, "ID", idFile);
                docs.setIdFilePath(saved);
            } catch (Exception e) {
                System.err.println("Failed to save ID file: " + e.getMessage());
            }
        }

        applicant.setDocuments(docs);

        // Auto-Eligibility Check
        if (!applicant.isEligible()) {
            applicant.setStatus(ApplicationStatus.REJECTED);
        } else if (applicant.getDocuments().isComplete()) {
            applicant.setStatus(ApplicationStatus.VERIFIED);
        } else {
            applicant.setStatus(ApplicationStatus.PENDING_VERIFICATION);
        }

        masterList.add(applicant);

        // If eligible, enqueue into screening queue (FIFO)
        if (applicant.getStatus() != ApplicationStatus.REJECTED) {
            screeningQueue.enqueue(applicant);
        }

        // Record in ActionStack
        actionHistory.push(new ActionRecord(
                generateNextActionId(),
                ActionRecord.ActionType.REGISTER_APPLICANT,
                applicant.getApplicantId(),
                applicant.getFullName(),
                null,
                applicant.getStatus(),
                0.0,
                "Registered student applicant " + applicant.getFullName() +
                        (docs.hasAnyUploadedFiles() ? " with " + docs.getUploadedFilesCount() + " attached document(s)" : ""),
                applicant
        ));

        notifyChange();
        return applicant;
    }

    // =========================================================================
    // CORE TRANSACTION 2: DOCUMENT VERIFICATION & SCREENING
    // =========================================================================

    /**
     * Verifies documents for an applicant. If complete, updates status to VERIFIED.
     */
    public synchronized boolean verifyApplicantDocuments(String applicantId, DocumentChecklist newDocs) {
        Applicant applicant = findApplicantById(applicantId);
        if (applicant == null) return false;

        ApplicationStatus oldStatus = applicant.getStatus();
        applicant.setDocuments(newDocs);

        if (newDocs.isComplete()) {
            applicant.setStatus(ApplicationStatus.VERIFIED);
        } else {
            applicant.setStatus(ApplicationStatus.PENDING_VERIFICATION);
        }

        actionHistory.push(new ActionRecord(
                generateNextActionId(),
                ActionRecord.ActionType.VERIFY_DOCUMENTS,
                applicant.getApplicantId(),
                applicant.getFullName(),
                oldStatus,
                applicant.getStatus(),
                0.0,
                "Updated document checklist for " + applicant.getFullName(),
                oldStatus
        ));

        notifyChange();
        return true;
    }

    // =========================================================================
    // CORE TRANSACTION 3: SLOT ALLOCATION & RANKING
    // =========================================================================

    /**
     * Ranks all verified applicants using Merge Sort (Merit) or Quick Sort (Need),
     * approves up to slotLimit, and places remainder into waitlist queue.
     */
    public synchronized void evaluateAndAllocateSlots(RankingCriteria criteria, int slotLimit) {
        // Collect all verified candidates (and existing waitlisted candidates)
        List<Applicant> candidateList = new ArrayList<>();
        for (Applicant a : masterList) {
            if (a.getStatus() == ApplicationStatus.VERIFIED || a.getStatus() == ApplicationStatus.WAITLISTED) {
                candidateList.add(a);
            }
        }

        if (candidateList.isEmpty()) {
            return;
        }

        // Sort using rubric-specified algorithms
        if (criteria == RankingCriteria.MERIT_GWA) {
            // Merge Sort (Merit Ranking)
            AlgorithmUtils.mergeSortByGWA(candidateList, true);
        } else {
            // Quick Sort (Financial Need Ranking)
            AlgorithmUtils.quickSortByIncome(candidateList, true);
        }

        // Respect both slotLimit and budget constraint
        int maxSlotsByBudget = (int) (totalBudget / grantPerScholar);
        int effectiveLimit = Math.min(slotLimit, maxSlotsByBudget);

        waitlistQueue.clear();

        for (int i = 0; i < candidateList.size(); i++) {
            Applicant applicant = candidateList.get(i);
            ApplicationStatus oldStatus = applicant.getStatus();

            if (i < effectiveLimit) {
                // Approved for official slot
                if (!(applicant instanceof Scholar)) {
                    String schId = generateNextScholarId();
                    Scholar scholar = new Scholar(applicant, schId, grantPerScholar, "1st Sem AY 2026-2027");
                    // Replace in master list
                    int idx = masterList.indexOf(applicant);
                    if (idx >= 0) {
                        masterList.set(idx, scholar);
                    }
                    scholarDirectory.insert(scholar);
                } else {
                    applicant.setStatus(ApplicationStatus.APPROVED);
                    scholarDirectory.insert((Scholar) applicant);
                }
            } else {
                // Excess qualified students moved to Waitlist Queue (FIFO)
                applicant.setStatus(ApplicationStatus.WAITLISTED);
                waitlistQueue.enqueue(applicant);
            }
        }

        actionHistory.push(new ActionRecord(
                generateNextActionId(),
                ActionRecord.ActionType.ALLOCATE_SLOTS,
                "ALL",
                "Batch Allocation (" + candidateList.size() + " applicants)",
                ApplicationStatus.VERIFIED,
                ApplicationStatus.APPROVED,
                0.0,
                "Ranked by " + criteria.getTitle() + " & allocated " + effectiveLimit + " slots",
                criteria
        ));

        notifyChange();
    }

    // =========================================================================
    // CORE TRANSACTION 4: PAYOUT & ALLOWANCE DISBURSEMENT
    // =========================================================================

    /**
     * Releases scholarship grant to an approved scholar, logging payout and deducting remaining budget.
     */
    public synchronized boolean releaseStipend(String scholarId) {
        Scholar scholar = scholarDirectory.searchById(scholarId);
        if (scholar == null) {
            // Search in masterList
            Applicant a = findApplicantById(scholarId);
            if (a instanceof Scholar) {
                scholar = (Scholar) a;
            }
        }

        if (scholar == null) return false;
        if (scholar.isStipendDisbursed()) return false; // already claimed

        if (getRemainingBudget() < scholar.getGrantAmount()) {
            throw new IllegalStateException("Insufficient Barangay scholarship funds remaining!");
        }

        scholar.disburseGrant();
        disbursedBudget += scholar.getGrantAmount();

        actionHistory.push(new ActionRecord(
                generateNextActionId(),
                ActionRecord.ActionType.RELEASE_STIPEND,
                scholar.getScholarId(),
                scholar.getFullName(),
                ApplicationStatus.APPROVED,
                ApplicationStatus.DISBURSED,
                scholar.getGrantAmount(),
                "Disbursed stipend of " + scholar.getFormattedGrant() + " to " + scholar.getFullName(),
                scholar
        ));

        notifyChange();
        return true;
    }

    /**
     * Drops or disqualifies a scholar (e.g. failing grades, dropped out, failed to claim grant).
     * Automatically dequeues next student from Waitlist Queue and promotes them!
     */
    public synchronized Scholar dropScholarAndPromoteWaitlist(String scholarId, String reason) {
        Scholar dropped = scholarDirectory.searchById(scholarId);
        if (dropped == null) return null;

        // If stipend was already disbursed, refund budget
        if (dropped.isStipendDisbursed()) {
            disbursedBudget = Math.max(0.0, disbursedBudget - dropped.getGrantAmount());
        }

        dropped.setStatus(ApplicationStatus.REJECTED);
        scholarDirectory.delete(scholarId);

        actionHistory.push(new ActionRecord(
                generateNextActionId(),
                ActionRecord.ActionType.DROP_SCHOLAR,
                dropped.getScholarId(),
                dropped.getFullName(),
                ApplicationStatus.APPROVED,
                ApplicationStatus.REJECTED,
                0.0,
                "Dropped scholar: " + dropped.getFullName() + " (" + reason + ")",
                dropped
        ));

        // Auto-promote next from waitlist queue (FIFO)
        Scholar promotedScholar = null;
        if (!waitlistQueue.isEmpty()) {
            Applicant nextInLine = waitlistQueue.dequeue();
            String newSchId = generateNextScholarId();
            promotedScholar = new Scholar(nextInLine, newSchId, grantPerScholar, "1st Sem AY 2026-2027");

            int idx = masterList.indexOf(nextInLine);
            if (idx >= 0) {
                masterList.set(idx, promotedScholar);
            }
            scholarDirectory.insert(promotedScholar);

            actionHistory.push(new ActionRecord(
                    generateNextActionId(),
                    ActionRecord.ActionType.PROMOTE_WAITLIST,
                    promotedScholar.getScholarId(),
                    promotedScholar.getFullName(),
                    ApplicationStatus.WAITLISTED,
                    ApplicationStatus.APPROVED,
                    0.0,
                    "Promoted " + promotedScholar.getFullName() + " from Waitlist to Slot " + newSchId,
                    promotedScholar
            ));
        }

        notifyChange();
        return promotedScholar;
    }

    // =========================================================================
    // 1-CLICK ACTION UNDO (LIFO STACK)
    // =========================================================================

    /**
     * Undoes the last recorded action using the ActionStack.
     */
    public synchronized boolean undoLastAction() {
        if (actionHistory.isEmpty()) {
            return false;
        }

        ActionRecord record = actionHistory.pop();
        switch (record.getType()) {
            case RELEASE_STIPEND:
                Scholar scholar = scholarDirectory.searchById(record.getTargetId());
                if (scholar != null && scholar.isStipendDisbursed()) {
                    scholar.reverseDisbursement();
                    disbursedBudget = Math.max(0.0, disbursedBudget - record.getAmount());
                }
                break;

            case REGISTER_APPLICANT:
                Applicant registered = findApplicantById(record.getTargetId());
                if (registered != null) {
                    masterList.remove(registered);
                    screeningQueue.remove(registered);
                }
                break;

            case VERIFY_DOCUMENTS:
                Applicant verified = findApplicantById(record.getTargetId());
                if (verified != null && record.getPreviousStatus() != null) {
                    verified.setStatus(record.getPreviousStatus());
                }
                break;

            case DROP_SCHOLAR:
                if (record.getPayload() instanceof Scholar) {
                    Scholar restored = (Scholar) record.getPayload();
                    restored.setStatus(ApplicationStatus.APPROVED);
                    scholarDirectory.insert(restored);
                }
                break;

            case PROMOTE_WAITLIST:
                Scholar promoted = scholarDirectory.searchById(record.getTargetId());
                if (promoted != null) {
                    scholarDirectory.delete(promoted.getScholarId());
                    promoted.setStatus(ApplicationStatus.WAITLISTED);
                    waitlistQueue.enqueue(promoted);
                }
                break;

            default:
                break;
        }

        notifyChange();
        return true;
    }

    // =========================================================================
    // DIRECT SEEDING & ACCESSORS
    // =========================================================================

    public synchronized void recordDirectPending(Applicant a) {
        masterList.add(a);
        screeningQueue.enqueue(a);
    }

    public synchronized void recordDirectVerified(Applicant a) {
        masterList.add(a);
        screeningQueue.enqueue(a);
    }

    public synchronized void recordDirectApproved(Scholar s) {
        masterList.add(s);
        scholarDirectory.insert(s);
    }

    public synchronized void recordDirectDisbursement(Scholar s) {
        masterList.add(s);
        scholarDirectory.insert(s);
        disbursedBudget += s.getGrantAmount();
    }

    public synchronized void recordDirectWaitlisted(Applicant a) {
        masterList.add(a);
        waitlistQueue.enqueue(a);
    }

    public synchronized void recordDirectRejected(Applicant a) {
        masterList.add(a);
    }

    public synchronized void clearAll() {
        masterList.clear();
        screeningQueue.clear();
        waitlistQueue.clear();
        actionHistory.clear();
        scholarDirectory.clear();
        disbursedBudget = 0.0;
        applicantCounter = 1;
        scholarCounter = 1;
        actionCounter = 1;
        notifyChange();
    }

    public synchronized List<Applicant> getMasterList() {
        return Collections.unmodifiableList(masterList);
    }

    public synchronized List<Scholar> getAllScholarsFromBST() {
        return scholarDirectory.inOrderTraversal();
    }

    public synchronized ScholarshipQueue<Applicant> getScreeningQueue() {
        return screeningQueue;
    }

    public synchronized ScholarshipQueue<Applicant> getWaitlistQueue() {
        return waitlistQueue;
    }

    public synchronized ActionStack<ActionRecord> getActionHistory() {
        return actionHistory;
    }

    public synchronized ScholarBST getScholarDirectory() {
        return scholarDirectory;
    }

    public synchronized Applicant findApplicantById(String id) {
        if (id == null) return null;
        for (Applicant a : masterList) {
            if (a.getApplicantId().equalsIgnoreCase(id.trim())) {
                return a;
            }
            if (a instanceof Scholar && ((Scholar) a).getScholarId().equalsIgnoreCase(id.trim())) {
                return a;
            }
        }
        return null;
    }

    /**
     * Searches applicants by ID, Scholar ID, or full name (case-insensitive substring match).
     * Used by the Student Portal Status Tracker.
     */
    public synchronized List<Applicant> findApplicantsByIdOrName(String query) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String q = query.trim().toLowerCase();
        List<Applicant> results = new ArrayList<>();
        for (Applicant a : masterList) {
            boolean matchId = a.getApplicantId().toLowerCase().contains(q);
            boolean matchName = a.getFullName().toLowerCase().contains(q);
            boolean matchSchId = (a instanceof Scholar) && ((Scholar) a).getScholarId().toLowerCase().contains(q);
            if (matchId || matchName || matchSchId) {
                results.add(a);
            }
        }
        return results;
    }

    /**
     * Finds the 1-based FIFO queue position in the screening queue.
     * Returns -1 if the applicant is not currently in the screening queue.
     */
    public synchronized int getQueuePosition(String applicantId) {
        if (applicantId == null) return -1;
        List<Applicant> list = screeningQueue.toList();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getApplicantId().equalsIgnoreCase(applicantId.trim())) {
                return i + 1;
            }
        }
        return -1;
    }

    /**
     * Finds the 1-based FIFO waitlist position in the waitlist queue.
     * Returns -1 if the applicant is not currently in the waitlist queue.
     */
    public synchronized int getWaitlistPosition(String applicantId) {
        if (applicantId == null) return -1;
        List<Applicant> list = waitlistQueue.toList();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getApplicantId().equalsIgnoreCase(applicantId.trim())) {
                return i + 1;
            }
        }
        return -1;
    }

    public synchronized int getCountByStatus(ApplicationStatus status) {
        int count = 0;
        for (Applicant a : masterList) {
            if (a.getStatus() == status) count++;
        }
        return count;
    }
}
