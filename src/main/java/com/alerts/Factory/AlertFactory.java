package com.alerts.Factory;

import com.alerts.Alert;

// Comment: used alert factory as abstract class instead of implementing alert factory logic here
public abstract class AlertFactory {
    /**
     * Factory method to create an Alert.
     *
     * @param patientId ID of the patient
     * @param condition Description or condition of the alert
     * @param timestamp Time of alert creation
     * @return Alert object created by the factory
     */
    public abstract Alert createAlert(String patientId, String condition, long timestamp);
}