package com.alerts.Strategy;

import com.alerts.Alert;
import com.alerts.Factory.AlertFactory;
import com.alerts.Factory.BloodPressureAlertFactory;
import com.data_management.PatientRecord;

import java.util.Comparator;
import java.util.List;

public class BloodPressureStrategy implements AlertStrategy {

    String type;
    private final AlertFactory alertFactory;


    public BloodPressureStrategy(String type) {
        this.type = type;
        this.alertFactory = new BloodPressureAlertFactory();
    }

    @Override
    public Alert checkAlert(List<PatientRecord> records, int patientId) {
        if (records.size() < 3) return null;

        records.sort(Comparator.comparingLong(PatientRecord::getTimestamp));

        for (int i = 0; i <= records.size() - 3; i++) {
            double v1 = records.get(i).getMeasurementValue();
            double v2 = records.get(i + 1).getMeasurementValue();
            double v3 = records.get(i + 2).getMeasurementValue();

            boolean up = v2 - v1 > 10 && v3 - v2 > 10;
            boolean down = v1 - v2 > 10 && v2 - v3 > 10;

            if (up || down) {
                String message = type + " Blood Pressure " + (up ? "rising" : "falling") + " trend";
                return alertFactory.createAlert(
                        String.valueOf(patientId),
                        message,
                        records.get(i + 2).getTimestamp()
                );
            }
        }

        return null;
    }
}
