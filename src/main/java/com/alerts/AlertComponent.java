package com.alerts;

public interface AlertComponent {
    String getPatientId();
    String getCondition();
    long getTimestamp();
    String getAlertType();
    String toString();
}
