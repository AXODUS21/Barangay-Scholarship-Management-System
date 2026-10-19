package com.barangay.scholarship.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Encapsulates an action executed by staff, stored in the ActionStack for 1-click Undo.
 */
public class ActionRecord implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public enum ActionType {
        REGISTER_APPLICANT("Register Applicant"),
        VERIFY_DOCUMENTS("Verify Documents"),
        ALLOCATE_SLOTS("Slot Allocation"),
        RELEASE_STIPEND("Release Stipend"),
        DROP_SCHOLAR("Disqualify / Drop Scholar"),
        PROMOTE_WAITLIST("Waitlist Promotion"),
        MANUAL_STATUS_CHANGE("Manual Status Change");

        private final String label;
        ActionType(String label) {
            this.label = label;
        }
        public String getLabel() {
            return label;
        }
    }

    private final String actionId;
    private final ActionType type;
    private final String targetId;
    private final String targetName;
    private final ApplicationStatus previousStatus;
    private final ApplicationStatus newStatus;
    private final double amount;
    private final LocalDateTime timestamp;
    private final String description;
    private final Object payload; // optional snapshot for restoration

    public ActionRecord(String actionId, ActionType type, String targetId, String targetName,
                        ApplicationStatus previousStatus, ApplicationStatus newStatus,
                        double amount, String description, Object payload) {
        this.actionId = actionId;
        this.type = type;
        this.targetId = targetId;
        this.targetName = targetName;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
        this.description = description;
        this.payload = payload;
    }

    public String getActionId() {
        return actionId;
    }

    public ActionType getType() {
        return type;
    }

    public String getTargetId() {
        return targetId;
    }

    public String getTargetName() {
        return targetName;
    }

    public ApplicationStatus getPreviousStatus() {
        return previousStatus;
    }

    public ApplicationStatus getNewStatus() {
        return newStatus;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getDescription() {
        return description;
    }

    public Object getPayload() {
        return payload;
    }

    public String getFormattedTime() {
        return timestamp.format(FORMATTER);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s - %s (%s)", getFormattedTime(), type.getLabel(), description, targetName);
    }
}
