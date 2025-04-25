package com.alerts;

// Represents an alert
public class Alert {
    private String patientId;
    private String condition;
    private long timestamp;
    private String alertType;

    public Alert(String patientId, String condition, long timestamp, String alertType) {
        this.patientId = patientId;
        this.condition = condition;
        this.timestamp = timestamp;
        this.alertType = alertType;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getCondition() {
        return condition;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getAlertType() {
        return alertType;
    }

    @Override
    public String toString() {
        return "Alert{" +
                "patientId='" + patientId + '\'' +
                ", condition='" + condition + '\'' +
                ", timestamp=" + timestamp +
                ", alertType='" + alertType + '\'' +
                '}';
    }
}
