package com.barangay.scholarship.test;

import com.barangay.scholarship.model.Scholar;
import com.barangay.scholarship.service.ScholarshipManager;
import com.barangay.scholarship.ui.views.DisbursementView;
import com.barangay.scholarship.ui.views.RankingAllocationView;
import com.barangay.scholarship.util.SampleDataSeeder;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Validates the actual Swing UI components and user interaction flows for Kent's tasks:
 * - RankingAllocationView (Criteria dropdown, slot spinner, search filter, preview table, selection)
 * - DisbursementView (Search filter, payout table, stipend release button, budget cards)
 */
public class KentAssignedViewsUITest {

    public static void main(String[] args) throws Exception {
        System.out.println("=======================================================================");
        System.out.println("   SWING UI INTEGRATION TEST: RANKING & DISBURSEMENT VIEWS");
        System.out.println("=======================================================================\n");

        ScholarshipManager manager = new ScholarshipManager(150000.0, 3000.0);
        SampleDataSeeder.seedDemoData(manager);

        // Run UI tests on the AWT Event Dispatch Thread
        SwingUtilities.invokeAndWait(() -> {
            try {
                testRankingAllocationView(manager);
                testDisbursementView(manager);
                System.out.println("\n>>> [ALL UI COMPONENT TESTS PASSED SUCCESSFULLY!]");
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        });
    }

    private static void testRankingAllocationView(ScholarshipManager manager) throws Exception {
        System.out.println("Testing RankingAllocationView UI components...");
        RankingAllocationView view = new RankingAllocationView(manager);

        Field cmbField = RankingAllocationView.class.getDeclaredField("cmbCriteria");
        cmbField.setAccessible(true);
        JComboBox<?> cmb = (JComboBox<?>) cmbField.get(view);

        Field spnField = RankingAllocationView.class.getDeclaredField("spnSlots");
        spnField.setAccessible(true);
        JSpinner spn = (JSpinner) spnField.get(view);

        Field tableModelField = RankingAllocationView.class.getDeclaredField("rankTableModel");
        tableModelField.setAccessible(true);
        DefaultTableModel model = (DefaultTableModel) tableModelField.get(view);

        Field searchField = RankingAllocationView.class.getDeclaredField("txtSearch");
        searchField.setAccessible(true);
        JTextField txtSearch = (JTextField) searchField.get(view);

        // 1. Check initial preview (Merit GWA)
        cmb.setSelectedItem(ScholarshipManager.RankingCriteria.MERIT_GWA);
        spn.setValue(5);
        view.previewRanking();

        int rowCount = model.getRowCount();
        System.out.println("  Preview row count: " + rowCount);
        if (rowCount != 7) {
            throw new AssertionError("Expected 7 candidate rows, got: " + rowCount);
        }

        // Check top 5 are approved, remaining 2 are waitlisted
        for (int r = 0; r < 5; r++) {
            String outcome = (String) model.getValueAt(r, 1);
            if (!outcome.contains("APPROVED")) {
                throw new AssertionError("Row " + r + " expected APPROVED outcome, got: " + outcome);
            }
        }
        for (int r = 5; r < 7; r++) {
            String outcome = (String) model.getValueAt(r, 1);
            if (!outcome.contains("WAITLIST")) {
                throw new AssertionError("Row " + r + " expected WAITLIST outcome, got: " + outcome);
            }
        }

        // 2. Switch criteria to Financial Need (QuickSort)
        cmb.setSelectedItem(ScholarshipManager.RankingCriteria.FINANCIAL_NEED);
        view.previewRanking();
        String rank1Name = (String) model.getValueAt(0, 3);
        System.out.println("  Financial Need Rank 1: " + rank1Name);
        if (!"Hannah Sophia Del Rosario".equals(rank1Name)) {
            throw new AssertionError("Expected Hannah Sophia Del Rosario as Rank 1 in Financial Need, got: " + rank1Name);
        }

        // 3. Test Live Search filter
        txtSearch.setText("Danilo");
        view.previewRanking();
        if (model.getRowCount() != 1) {
            throw new AssertionError("Expected 1 row matching 'Danilo', got: " + model.getRowCount());
        }
        txtSearch.setText("");
        view.previewRanking();
        if (model.getRowCount() != 7) {
            throw new AssertionError("Expected 7 rows after clearing search, got: " + model.getRowCount());
        }

        System.out.println("  [PASS] RankingAllocationView verified!");
    }

    private static void testDisbursementView(ScholarshipManager manager) throws Exception {
        System.out.println("Testing DisbursementView UI components...");
        DisbursementView view = new DisbursementView(manager);

        Field tableModelField = DisbursementView.class.getDeclaredField("payoutTableModel");
        tableModelField.setAccessible(true);
        DefaultTableModel model = (DefaultTableModel) tableModelField.get(view);

        Field searchField = DisbursementView.class.getDeclaredField("txtSearch");
        searchField.setAccessible(true);
        JTextField txtSearch = (JTextField) searchField.get(view);

        // Check initial rows (10 scholars)
        System.out.println("  Initial payout masterlist rows: " + model.getRowCount());
        if (model.getRowCount() != 10) {
            throw new AssertionError("Expected 10 scholars in payout list, got: " + model.getRowCount());
        }

        // All seeded scholars should start with their grant unclaimed.
        txtSearch.setText("Unclaimed");
        Method applyFilterMethod = DisbursementView.class.getDeclaredMethod("applyFilter");
        applyFilterMethod.setAccessible(true);
        applyFilterMethod.invoke(view);

        System.out.println("  Unclaimed filtered rows: " + model.getRowCount());
        if (model.getRowCount() != 10) {
            throw new AssertionError("Expected 10 unclaimed scholars, got: " + model.getRowCount());
        }

        // Clear search
        txtSearch.setText("");
        applyFilterMethod.invoke(view);
        if (model.getRowCount() != 10) {
            throw new AssertionError("Expected 10 scholars after clearing search");
        }

        // Test disburseScholar method
        Scholar unclaimed = null;
        for (Scholar s : manager.getAllScholarsFromBST()) {
            if (!s.isStipendDisbursed()) {
                unclaimed = s;
                break;
            }
        }
        if (unclaimed == null) {
            throw new AssertionError("Expected at least one unclaimed scholar");
        }

        double remBefore = manager.getRemainingBudget();
        double disbBefore = manager.getDisbursedBudget();

        manager.releaseStipend(unclaimed.getScholarId());
        view.refreshData();

        if (manager.getRemainingBudget() != remBefore - 3000.0) {
            throw new AssertionError("Remaining budget failed to deduct 3000");
        }
        if (manager.getDisbursedBudget() != disbBefore + 3000.0) {
            throw new AssertionError("Disbursed budget failed to increment 3000");
        }

        System.out.println("  [PASS] DisbursementView verified!");
    }
}
