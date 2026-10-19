package com.barangay.scholarship.model;

import java.time.LocalDateTime;

/**
 * Represents an officially approved scholar who has been granted a slot.
 * Demonstrates OOP Inheritance (Scholar extends Applicant) as required by rubric.
 */
public class Scholar extends Applicant {
    private static final long serialVersionUID = 1L;

    private String scholarId;
    private double grantAmount;
    private boolean stipendDisbursed;
    private LocalDateTime disbursedAt;
    private String semester;
    private String remarks;

    public Scholar(Applicant applicant, String scholarId, double grantAmount, String semester) {
        super(applicant.getApplicantId(), applicant.getFullName(), applicant.getEmail(),
              applicant.getContactNumber(), applicant.getSchool(), applicant.getYearLevel(),
              applicant.getGwa(), applicant.getMonthlyFamilyIncome(), applicant.getPurok(),
              applicant.getDocuments(), ApplicationStatus.APPROVED, applicant.getRegisteredAt());
        this.scholarId = scholarId;
        this.grantAmount = grantAmount;
        this.stipendDisbursed = false;
        this.disbursedAt = null;
        this.semester = semester != null ? semester : "1st Sem AY 2026-2027";
        this.remarks = "Official Barangay Scholar";
    }

    public Scholar(String applicantId, String fullName, String email, String contactNumber,
                   String school, String yearLevel, double gwa, double monthlyFamilyIncome,
                   String purok, String scholarId, double grantAmount, boolean stipendDisbursed,
                   LocalDateTime disbursedAt, String semester, String remarks) {
        super(applicantId, fullName, email, contactNumber, school, yearLevel, gwa, monthlyFamilyIncome, purok);
        this.scholarId = scholarId;
        this.grantAmount = grantAmount;
        this.stipendDisbursed = stipendDisbursed;
        this.disbursedAt = disbursedAt;
        this.semester = semester;
        this.remarks = remarks;
        this.status = stipendDisbursed ? ApplicationStatus.DISBURSED : ApplicationStatus.APPROVED;
    }

    public String getScholarId() {
        return scholarId;
    }

    public void setScholarId(String scholarId) {
        this.scholarId = scholarId;
    }

    public double getGrantAmount() {
        return grantAmount;
    }

    public void setGrantAmount(double grantAmount) {
        this.grantAmount = grantAmount;
    }

    public boolean isStipendDisbursed() {
        return stipendDisbursed;
    }

    public void setStipendDisbursed(boolean stipendDisbursed) {
        this.stipendDisbursed = stipendDisbursed;
        if (stipendDisbursed) {
            this.status = ApplicationStatus.DISBURSED;
            if (this.disbursedAt == null) {
                this.disbursedAt = LocalDateTime.now();
            }
        } else {
            this.status = ApplicationStatus.APPROVED;
            this.disbursedAt = null;
        }
    }

    public LocalDateTime getDisbursedAt() {
        return disbursedAt;
    }

    public void setDisbursedAt(LocalDateTime disbursedAt) {
        this.disbursedAt = disbursedAt;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public void disburseGrant() {
        this.stipendDisbursed = true;
        this.disbursedAt = LocalDateTime.now();
        this.status = ApplicationStatus.DISBURSED;
    }

    public void reverseDisbursement() {
        this.stipendDisbursed = false;
        this.disbursedAt = null;
        this.status = ApplicationStatus.APPROVED;
    }

    public String getFormattedGrant() {
        return String.format("₱%,.2f", grantAmount);
    }

    public String getFormattedDisbursedDate() {
        return disbursedAt != null ? disbursedAt.format(DATE_FORMATTER) : "Pending Payout";
    }

    @Override
    public String toString() {
        return String.format("[Scholar ID: %s] %s | Grant: %s | Payout: %s (%s)",
                scholarId, fullName, getFormattedGrant(), (stipendDisbursed ? "CLAIMED" : "UNCLAIMED"), status);
    }
}
