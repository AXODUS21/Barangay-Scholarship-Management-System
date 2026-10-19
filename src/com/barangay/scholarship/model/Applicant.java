package com.barangay.scholarship.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Base entity representing a scholarship applicant residing in the Barangay.
 */
public class Applicant implements Serializable, Comparable<Applicant> {
    private static final long serialVersionUID = 1L;
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    protected String applicantId;
    protected String fullName;
    protected String email;
    protected String contactNumber;
    protected String school;
    protected String yearLevel;
    protected double gwa;
    protected double monthlyFamilyIncome;
    protected String purok;
    protected DocumentChecklist documents;
    protected ApplicationStatus status;
    protected LocalDateTime registeredAt;

    public Applicant(String applicantId, String fullName, String email, String contactNumber,
                     String school, String yearLevel, double gwa, double monthlyFamilyIncome,
                     String purok) {
        this.applicantId = applicantId;
        this.fullName = fullName;
        this.email = email;
        this.contactNumber = contactNumber;
        this.school = school;
        this.yearLevel = yearLevel;
        this.gwa = gwa;
        this.monthlyFamilyIncome = monthlyFamilyIncome;
        this.purok = purok;
        this.documents = new DocumentChecklist();
        this.status = ApplicationStatus.PENDING_VERIFICATION;
        this.registeredAt = LocalDateTime.now();
    }

    public Applicant(String applicantId, String fullName, String email, String contactNumber,
                     String school, String yearLevel, double gwa, double monthlyFamilyIncome,
                     String purok, DocumentChecklist documents, ApplicationStatus status,
                     LocalDateTime registeredAt) {
        this.applicantId = applicantId;
        this.fullName = fullName;
        this.email = email;
        this.contactNumber = contactNumber;
        this.school = school;
        this.yearLevel = yearLevel;
        this.gwa = gwa;
        this.monthlyFamilyIncome = monthlyFamilyIncome;
        this.purok = purok;
        this.documents = documents != null ? documents : new DocumentChecklist();
        this.status = status != null ? status : ApplicationStatus.PENDING_VERIFICATION;
        this.registeredAt = registeredAt != null ? registeredAt : LocalDateTime.now();
    }

    // Getters and Setters
    public String getApplicantId() {
        return applicantId;
    }

    public void setApplicantId(String applicantId) {
        this.applicantId = applicantId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getYearLevel() {
        return yearLevel;
    }

    public void setYearLevel(String yearLevel) {
        this.yearLevel = yearLevel;
    }

    public double getGwa() {
        return gwa;
    }

    public void setGwa(double gwa) {
        this.gwa = gwa;
    }

    public double getMonthlyFamilyIncome() {
        return monthlyFamilyIncome;
    }

    public void setMonthlyFamilyIncome(double monthlyFamilyIncome) {
        this.monthlyFamilyIncome = monthlyFamilyIncome;
    }

    public String getPurok() {
        return purok;
    }

    public void setPurok(String purok) {
        this.purok = purok;
    }

    public DocumentChecklist getDocuments() {
        return documents;
    }

    public void setDocuments(DocumentChecklist documents) {
        this.documents = documents;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }

    /**
     * Checks if GWA is in collegiate scale (1.0 - 5.0) or high school / percentage scale (75 - 100).
     */
    public boolean isCollegeScale() {
        return this.gwa <= 5.0;
    }

    /**
     * Evaluates basic qualification criteria:
     * 1. Income <= ₱25,000 / month
     * 2. College GWA <= 2.50 OR Percentage GWA >= 85.0%
     */
    public boolean isEligible() {
        boolean incomeOk = this.monthlyFamilyIncome <= 25000.0;
        boolean gwaOk = isCollegeScale() ? (this.gwa <= 2.50 && this.gwa >= 1.0) : (this.gwa >= 85.0 && this.gwa <= 100.0);
        return incomeOk && gwaOk;
    }

    public String getEligibilityReason() {
        if (isEligible()) {
            return "Eligible (Meets grade & income criteria)";
        }
        StringBuilder reason = new StringBuilder("Ineligible: ");
        if (monthlyFamilyIncome > 25000.0) {
            reason.append("Income exceeds ₱25,000 threshold; ");
        }
        if (isCollegeScale()) {
            if (gwa > 2.50) reason.append("GWA exceeds 2.50 limit (Collegiate Scale); ");
        } else {
            if (gwa < 85.0) reason.append("GWA is below 85.0% minimum; ");
        }
        return reason.toString().trim();
    }

    public String getFormattedIncome() {
        return String.format("₱%,.2f", monthlyFamilyIncome);
    }

    public String getFormattedGwa() {
        return String.format("%.2f", gwa);
    }

    public String getFormattedDate() {
        return registeredAt != null ? registeredAt.format(DATE_FORMATTER) : "N/A";
    }

    @Override
    public int compareTo(Applicant other) {
        if (other == null) return 1;
        return this.applicantId.compareTo(other.applicantId);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s - GWA: %.2f | Income: %s | %s (%s)",
                applicantId, fullName, gwa, getFormattedIncome(), purok, status);
    }
}
