package com.alerts.Strategy;

import com.alerts.Alert;
import com.alerts.Factory.ECGAlertFactory;
import com.alerts.Factory.AlertFactory;
import com.data_management.PatientRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ECGStrategy implements AlertStrategy {

    private final AlertFactory alertFactory;

    public ECGStrategy() {
        this.alertFactory = new ECGAlertFactory();
    }

    @Override
    public Alert checkAlert(List<PatientRecord> records, int patientId) {
        if (records.isEmpty()) return null;

        // Sort records by timestamp to ensure chronological order
        records = new ArrayList<>(records);
        records.sort(Comparator.comparingLong(PatientRecord::getTimestamp));


        int windowSize = 5;
        if (records.size() < windowSize) return null;

        for (int i = windowSize; i < records.size(); i++) {
            // Calculate the average of the previous `windowSize` records
            double sum = 0;
            for (int j = i - windowSize; j < i; j++) {
                sum += records.get(j).getMeasurementValue();
            }
            double average = sum / windowSize;

            double currentECG = records.get(i).getMeasurementValue();

            // Check if the current ECG value is significantly higher than the average (1.5 times)
            if (currentECG > average * 1.5) {
                return alertFactory.createAlert(
                        String.valueOf(patientId),
                        "Abnormal ECG peak detected",
                        records.get(i).getTimestamp()
                );
            }
        }

        return null;
    }
}
