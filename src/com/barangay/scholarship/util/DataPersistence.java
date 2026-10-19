package com.barangay.scholarship.util;

import com.barangay.scholarship.model.Scholar;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Handles report generation and exporting of official Barangay Scholarship documents.
 */
public class DataPersistence {

    /**
     * Generates an official, printable Barangay Payout Attendance and Disbursement Sheet.
     */
    public static void exportAttendanceSheet(File file, List<Scholar> scholars,
                                            String semester, double totalBudget,
                                            double disbursed) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("==========================================================================================================");
            writer.println("                          REPUBLIC OF THE PHILIPPINES - BARANGAY COUNCIL");
            writer.println("                         SANGGUNIANG KABATAAN (SK) SCHOLARSHIP PROGRAM");
            writer.println("                           OFFICIAL PAYOUT & DISBURSEMENT ATTENDANCE SHEET");
            writer.println("==========================================================================================================");
            writer.println("Period / Semester   : " + semester);
            writer.println("Generated On        : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy - hh:mm a")));
            writer.println(String.format("Total Budget        : ₱%,.2f       Disbursed: ₱%,.2f       Remaining: ₱%,.2f",
                    totalBudget, disbursed, (totalBudget - disbursed)));
            writer.println("Total Listed Scholars: " + scholars.size());
            writer.println("----------------------------------------------------------------------------------------------------------");
            writer.printf("%-4s | %-12s | %-24s | %-16s | %-10s | %-12s | %-20s%n",
                    "No.", "Scholar ID", "Full Name", "Purok/Zone", "Grant", "Status", "Claimant Signature");
            writer.println("----------------------------------------------------------------------------------------------------------");

            int idx = 1;
            for (Scholar s : scholars) {
                String payoutStatus = s.isStipendDisbursed() ? "DISBURSED" : "UNCLAIMED";
                writer.printf("%-4d | %-12s | %-24s | %-16s | %-10s | %-12s | %-20s%n",
                        idx++,
                        s.getScholarId(),
                        truncate(s.getFullName(), 24),
                        truncate(s.getPurok(), 16),
                        s.getFormattedGrant(),
                        payoutStatus,
                        s.isStipendDisbursed() ? "[SIGNED / RECEIVED]" : "____________________");
            }

            writer.println("----------------------------------------------------------------------------------------------------------");
            writer.println();
            writer.println();
            writer.println("CERTIFICATION & APPROVAL:");
            writer.println();
            writer.println("Prepared by:                        Verified by:                        Approved by:");
            writer.println();
            writer.println("_________________________          _________________________          _________________________");
            writer.println("SK Chairperson / Secretary         Barangay Treasurer                 Punong Barangay (Captain)");
            writer.println("==========================================================================================================");
        }
    }

    private static String truncate(String val, int max) {
        if (val == null) return "";
        if (val.length() <= max) return val;
        return val.substring(0, max - 3) + "...";
    }
}
