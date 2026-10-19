package com.barangay.scholarship.model;

import java.io.Serializable;

/**
 * Tracks physical document submissions required by the Barangay Council.
 */
public class DocumentChecklist implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean certificateOfIndigency;
    private boolean certifiedCopyOfGrades;
    private boolean enrollmentForm;
    private boolean validBarangayOrSchoolId;

    public DocumentChecklist() {
        this(false, false, false, false);
    }

    public DocumentChecklist(boolean certificateOfIndigency, boolean certifiedCopyOfGrades,
                             boolean enrollmentForm, boolean validBarangayOrSchoolId) {
        this.certificateOfIndigency = certificateOfIndigency;
        this.certifiedCopyOfGrades = certifiedCopyOfGrades;
        this.enrollmentForm = enrollmentForm;
        this.validBarangayOrSchoolId = validBarangayOrSchoolId;
    }

    public boolean isCertificateOfIndigency() {
        return certificateOfIndigency;
    }

    public void setCertificateOfIndigency(boolean certificateOfIndigency) {
        this.certificateOfIndigency = certificateOfIndigency;
    }

    public boolean isCertifiedCopyOfGrades() {
        return certifiedCopyOfGrades;
    }

    public void setCertifiedCopyOfGrades(boolean certifiedCopyOfGrades) {
        this.certifiedCopyOfGrades = certifiedCopyOfGrades;
    }

    public boolean isEnrollmentForm() {
        return enrollmentForm;
    }

    public void setEnrollmentForm(boolean enrollmentForm) {
        this.enrollmentForm = enrollmentForm;
    }

    public boolean isValidBarangayOrSchoolId() {
        return validBarangayOrSchoolId;
    }

    public void setValidBarangayOrSchoolId(boolean validBarangayOrSchoolId) {
        this.validBarangayOrSchoolId = validBarangayOrSchoolId;
    }

    /**
     * All 4 documents are required for complete verification.
     */
    public boolean isComplete() {
        return certificateOfIndigency && certifiedCopyOfGrades && enrollmentForm && validBarangayOrSchoolId;
    }

    public int getCompletedCount() {
        int count = 0;
        if (certificateOfIndigency) count++;
        if (certifiedCopyOfGrades) count++;
        if (enrollmentForm) count++;
        if (validBarangayOrSchoolId) count++;
        return count;
    }

    public int getTotalCount() {
        return 4;
    }

    public String getStatusSummary() {
        return getCompletedCount() + "/" + getTotalCount() + (isComplete() ? " Complete" : " Incomplete");
    }

    @Override
    public String toString() {
        return getStatusSummary();
    }
}
