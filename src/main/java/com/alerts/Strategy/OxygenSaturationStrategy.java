package com.alerts.Strategy;

import com.alerts.Alert;
import com.alerts.Factory.AlertFactory;
import com.alerts.Factory.BloodOxygenAlertFactory;
import com.data_management.PatientRecord;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class OxygenSaturationStrategy implements AlertStrategy {

    private final AlertFactory alertFactory;

    public OxygenSaturationStrategy() {
        this.alertFactory = new BloodOxygenAlertFactory();
    }

    @Override
    public Alert checkAlert(List<PatientRecord> records, int patientId) {
        records = records.stream()
                .filter(r -> r.getPatientId() == patientId && "Oxygen Saturation".equals(r.getRecordType()))
                .sorted(Comparator.comparingLong(PatientRecord::getTimestamp))
                .collect(Collectors.toList());

        for (int i = 1; i < records.size(); i++) {
            double previous = records.get(i - 1).getMeasurementValue();
            double current = records.get(i).getMeasurementValue();
            long timeDiff = records.get(i).getTimestamp() - records.get(i - 1).getTimestamp();

            // Detect rapid drop (> 5% in < 2000 ms)
            if (previous - current > 5 && timeDiff < 2000) {
                return alertFactory.createAlert(String.valueOf(patientId), "Rapid drop in oxygen saturation", records.get(i).getTimestamp());
            }

            // Also detect dangerously low value
            if (current < 90) {
                return alertFactory.createAlert(String.valueOf(patientId), "Low oxygen saturation", records.get(i).getTimestamp());
            }
        }

        return null;
    }

}
