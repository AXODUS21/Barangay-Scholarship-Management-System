package com.barangay.scholarship.util;

import com.barangay.scholarship.model.Applicant;
import com.barangay.scholarship.model.ApplicationStatus;
import com.barangay.scholarship.model.DocumentChecklist;
import com.barangay.scholarship.model.Scholar;
import com.barangay.scholarship.service.ScholarshipManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Pre-populates realistic Filipino student records for testing and classroom demonstration.
 */
public class SampleDataSeeder {

    public static void seedDemoData(ScholarshipManager manager) {
        manager.clearAll();

        // 20 realistic student records across various Puroks and Universities
        Object[][] demoApplicants = new Object[][]{
            // Full Name, School, Year Level, GWA, Monthly Income, Purok, DocsComplete, InitialStatus
            {"Maria Santos", "Pamantasan ng Lungsod", "2nd Year College", 1.25, 12000.0, "Purok 1 Maligaya", true, ApplicationStatus.APPROVED},
            {"Juan Carlos Dizon", "Polytechnic University of the Philippines", "3rd Year College", 1.35, 9500.0, "Purok 2 Pag-asa", true, ApplicationStatus.APPROVED},
            {"Angelica Reyes", "University of the Philippines", "1st Year College", 1.40, 15000.0, "Purok 3 Riverside", true, ApplicationStatus.APPROVED},
            {"Joshua Miguel Bautista", "Technological University of the Philippines", "4th Year College", 1.50, 8000.0, "Purok 1 Maligaya", true, ApplicationStatus.APPROVED},
            {"Princess Mae Ramos", "City College", "2nd Year College", 1.60, 11000.0, "Purok 4 San Jose", true, ApplicationStatus.APPROVED},
            {"Christian Dave Garcia", "Bulacan State University", "3rd Year College", 1.65, 14000.0, "Purok 2 Pag-asa", true, ApplicationStatus.APPROVED},
            {"Kristine Joy Mendoza", "Rizal Technological University", "1st Year College", 1.75, 16000.0, "Purok 5 Kawayan", true, ApplicationStatus.APPROVED},
            {"Mark Anthony Villanueva", "Pamantasan ng Lungsod", "2nd Year College", 1.80, 10500.0, "Purok 3 Riverside", true, ApplicationStatus.APPROVED},
            {"Alyssa Nicole Cruz", "Polytechnic University of the Philippines", "4th Year College", 1.85, 13000.0, "Purok 6 Ilaya", true, ApplicationStatus.APPROVED},
            {"John Paul Aquino", "Earist Institute of Science & Tech", "1st Year College", 1.90, 7500.0, "Purok 4 San Jose", true, ApplicationStatus.APPROVED},

            // Waitlisted applicants (qualified, but slots exceeded)
            {"Bea Camille Fernandez", "City College", "3rd Year College", 2.00, 17000.0, "Purok 1 Maligaya", true, ApplicationStatus.WAITLISTED},
            {"Jerome Patrick Lopez", "University of Caloocan City", "2nd Year College", 2.10, 14500.0, "Purok 2 Pag-asa", true, ApplicationStatus.WAITLISTED},
            {"Patricia Anne Castro", "Pamantasan ng Lungsod", "1st Year College", 2.15, 18000.0, "Purok 5 Kawayan", true, ApplicationStatus.WAITLISTED},
            {"Gabriel Luis Soriano", "Polytechnic University of the Philippines", "3rd Year College", 2.25, 19000.0, "Purok 3 Riverside", true, ApplicationStatus.WAITLISTED},

            // Verified applicants in Screening / Ready for slot ranking
            {"Katrina Denise Morales", "City College", "2nd Year College", 1.70, 12500.0, "Purok 6 Ilaya", true, ApplicationStatus.VERIFIED},
            {"Danilo Jose Mercado", "Rizal Technological University", "4th Year College", 1.95, 11500.0, "Purok 1 Maligaya", true, ApplicationStatus.VERIFIED},

            // Pending Verification (just registered, some documents missing)
            {"Rochelle Marie Tolentino", "Pamantasan ng Lungsod", "1st Year College", 1.60, 13500.0, "Purok 4 San Jose", false, ApplicationStatus.PENDING_VERIFICATION},
            {"Ezekiel Vance Perez", "University of the Philippines", "2nd Year College", 1.45, 16500.0, "Purok 2 Pag-asa", false, ApplicationStatus.PENDING_VERIFICATION},

            // High school / percentage scale applicants
            {"Hannah Sophia Del Rosario", "Barangay National High School", "Grade 12", 94.5, 9000.0, "Purok 3 Riverside", true, ApplicationStatus.VERIFIED},
            {"Carl Matthew Navarro", "Barangay National High School", "Grade 11", 82.0, 32000.0, "Purok 5 Kawayan", false, ApplicationStatus.REJECTED}
        };

        int appNum = 1;
        int schNum = 1;

        for (Object[] row : demoApplicants) {
            String name = (String) row[0];
            String school = (String) row[1];
            String year = (String) row[2];
            double gwa = (Double) row[3];
            double income = (Double) row[4];
            String purok = (String) row[5];
            boolean docsComplete = (Boolean) row[6];
            ApplicationStatus targetStatus = (ApplicationStatus) row[7];

            String appId = String.format("APP-2026-%03d", appNum++);
            String email = name.toLowerCase().replace(" ", ".") + "@email.ph";
            String phone = "09" + (170000000 + (appNum * 12345));

            DocumentChecklist docs = new DocumentChecklist(
                    docsComplete,
                    docsComplete,
                    docsComplete,
                    docsComplete || (appNum % 2 == 0)
            );

            Applicant applicant = new Applicant(
                    appId, name, email, phone, school, year, gwa, income, purok,
                    docs, targetStatus, LocalDateTime.now().minusDays(20 - appNum)
            );

            if (targetStatus == ApplicationStatus.APPROVED) {
                String schId = String.format("SCH-2026-%03d", schNum++);
                Scholar scholar = new Scholar(applicant, schId, manager.getGrantPerScholar(), "1st Sem AY 2026-2027");
                // Let the first 3 scholars have claimed their payouts already
                if (schNum <= 4) {
                    scholar.disburseGrant();
                    manager.recordDirectDisbursement(scholar);
                } else {
                    manager.recordDirectApproved(scholar);
                }
            } else if (targetStatus == ApplicationStatus.WAITLISTED) {
                manager.recordDirectWaitlisted(applicant);
            } else if (targetStatus == ApplicationStatus.VERIFIED) {
                manager.recordDirectVerified(applicant);
            } else if (targetStatus == ApplicationStatus.PENDING_VERIFICATION) {
                manager.recordDirectPending(applicant);
            } else if (targetStatus == ApplicationStatus.REJECTED) {
                manager.recordDirectRejected(applicant);
            }
        }
    }
}
