package com.barangay.scholarship.model;

import java.io.File;
import java.io.Serializable;

/**
 * Tracks physical document submissions and digital file attachments required by the Barangay Council.
 */
public class DocumentChecklist implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean certificateOfIndigency;
    private boolean certifiedCopyOfGrades;
    private boolean enrollmentForm;
    private boolean validBarangayOrSchoolId;

    // Digital attachment file paths (relative to project root)
    private String indigencyFilePath;
    private String gradesFilePath;
    private String enrollmentFilePath;
    private String idFilePath;

    public DocumentChecklist() {
        this(false, false, false, false, null, null, null, null);
    }

    public DocumentChecklist(boolean certificateOfIndigency, boolean certifiedCopyOfGrades,
                             boolean enrollmentForm, boolean validBarangayOrSchoolId) {
        this(certificateOfIndigency, certifiedCopyOfGrades, enrollmentForm, validBarangayOrSchoolId, null, null, null, null);
    }

    public DocumentChecklist(boolean certificateOfIndigency, boolean certifiedCopyOfGrades,
                             boolean enrollmentForm, boolean validBarangayOrSchoolId,
                             String indigencyFilePath, String gradesFilePath,
                             String enrollmentFilePath, String idFilePath) {
        this.certificateOfIndigency = certificateOfIndigency || (indigencyFilePath != null && !indigencyFilePath.trim().isEmpty());
        this.certifiedCopyOfGrades = certifiedCopyOfGrades || (gradesFilePath != null && !gradesFilePath.trim().isEmpty());
        this.enrollmentForm = enrollmentForm || (enrollmentFilePath != null && !enrollmentFilePath.trim().isEmpty());
        this.validBarangayOrSchoolId = validBarangayOrSchoolId || (idFilePath != null && !idFilePath.trim().isEmpty());

        this.indigencyFilePath = indigencyFilePath;
        this.gradesFilePath = gradesFilePath;
        this.enrollmentFilePath = enrollmentFilePath;
        this.idFilePath = idFilePath;
    }

    // --- Physical Verification Flags ---

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

    // --- Digital Upload File Paths ---

    public String getIndigencyFilePath() {
        return indigencyFilePath;
    }

    public void setIndigencyFilePath(String indigencyFilePath) {
        this.indigencyFilePath = indigencyFilePath;
        if (indigencyFilePath != null && !indigencyFilePath.trim().isEmpty()) {
            this.certificateOfIndigency = true;
        }
    }

    public String getGradesFilePath() {
        return gradesFilePath;
    }

    public void setGradesFilePath(String gradesFilePath) {
        this.gradesFilePath = gradesFilePath;
        if (gradesFilePath != null && !gradesFilePath.trim().isEmpty()) {
            this.certifiedCopyOfGrades = true;
        }
    }

    public String getEnrollmentFilePath() {
        return enrollmentFilePath;
    }

    public void setEnrollmentFilePath(String enrollmentFilePath) {
        this.enrollmentFilePath = enrollmentFilePath;
        if (enrollmentFilePath != null && !enrollmentFilePath.trim().isEmpty()) {
            this.enrollmentForm = true;
        }
    }

    public String getIdFilePath() {
        return idFilePath;
    }

    public void setIdFilePath(String idFilePath) {
        this.idFilePath = idFilePath;
        if (idFilePath != null && !idFilePath.trim().isEmpty()) {
            this.validBarangayOrSchoolId = true;
        }
    }

    // --- Digital File Helper Checks ---

    public boolean hasIndigencyFile() {
        return indigencyFilePath != null && !indigencyFilePath.trim().isEmpty();
    }

    public boolean hasGradesFile() {
        return gradesFilePath != null && !gradesFilePath.trim().isEmpty();
    }

    public boolean hasEnrollmentFile() {
        return enrollmentFilePath != null && !enrollmentFilePath.trim().isEmpty();
    }

    public boolean hasIdFile() {
        return idFilePath != null && !idFilePath.trim().isEmpty();
    }

    public boolean hasAnyUploadedFiles() {
        return getUploadedFilesCount() > 0;
    }

    public int getUploadedFilesCount() {
        int count = 0;
        if (hasIndigencyFile()) count++;
        if (hasGradesFile()) count++;
        if (hasEnrollmentFile()) count++;
        if (hasIdFile()) count++;
        return count;
    }

    public String getIndigencyFileName() {
        return extractFileName(indigencyFilePath);
    }

    public String getGradesFileName() {
        return extractFileName(gradesFilePath);
    }

    public String getEnrollmentFileName() {
        return extractFileName(enrollmentFilePath);
    }

    public String getIdFileName() {
        return extractFileName(idFilePath);
    }

    private String extractFileName(String path) {
        if (path == null || path.trim().isEmpty()) return "";
        return new File(path).getName();
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
        String base = getStatusSummary();
        if (hasAnyUploadedFiles()) {
            base += " (" + getUploadedFilesCount() + " digital file" + (getUploadedFilesCount() > 1 ? "s" : "") + " attached)";
        }
        return base;
    }
}
