package com.alerts.Strategy;

import java.util.List;
import com.alerts.Alert;
import com.data_management.PatientRecord;

public interface AlertStrategy {

    /**
     * Determines whether an alert should be triggered
     *
     * @param records List of patient records for a specific measurement type
     * @param patientId ID of the patient
     * @return An Alert if a condition is met, or null if no alert is needed
     */
    Alert checkAlert(List<PatientRecord> records, int patientId);
}
