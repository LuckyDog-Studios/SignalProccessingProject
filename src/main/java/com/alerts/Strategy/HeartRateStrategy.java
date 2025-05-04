package com.alerts.Strategy;

import com.alerts.Alert;
import com.alerts.Factory.AlertFactory;
import com.alerts.Factory.ECGAlertFactory;
import com.data_management.PatientRecord;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HeartRateStrategy implements AlertStrategy {

    private final AlertFactory alertFactory;

    public HeartRateStrategy() {
        this.alertFactory = new ECGAlertFactory();
    }

    @Override
    public Alert checkAlert(List<PatientRecord> records, int patientId) {
        if (records.isEmpty()) return null;

        PatientRecord latest = Collections.max(records, Comparator.comparingLong(PatientRecord::getTimestamp));
        double value = latest.getMeasurementValue();

        if (value < 60 || value > 100) {
            String condition = value < 60 ? "Bradycardia (low heart rate)" : "Tachycardia (high heart rate)";
            return alertFactory.createAlert(
                    String.valueOf(patientId),
                    condition,
                    latest.getTimestamp()
            );
        }

        return null;
    }
}
