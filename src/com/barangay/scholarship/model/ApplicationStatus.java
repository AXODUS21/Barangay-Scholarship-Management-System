package com.barangay.scholarship.model;

/**
 * Represents the lifecycle stages of a scholarship application.
 */
public enum ApplicationStatus {
    PENDING_VERIFICATION("Pending Verification", "Registered but documents are awaiting screening"),
    VERIFIED("Verified", "Documents checked and placed in evaluation queue"),
    APPROVED("Approved", "Granted an official scholarship slot"),
    WAITLISTED("Waitlisted", "Qualified but slots are filled; on FIFO waitlist"),
    REJECTED("Rejected", "Disqualified due to income, GWA, or missing documents"),
    DISBURSED("Disbursed", "Stipend/grant has been successfully claimed");

    private final String displayName;
    private final String description;

    ApplicationStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
