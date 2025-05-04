package com.alerts.Strategy;

import com.alerts.Alert;
import com.alerts.Factory.AlertFactory;
import com.alerts.Factory.BloodPressureAlertFactory;
import com.alerts.Factory.BloodOxygenAlertFactory;
import com.data_management.PatientRecord;

import java.util.List;
import java.util.stream.Collectors;

public class HypotensiveHypoxemiaStrategy implements AlertStrategy {

    private final AlertFactory bpAlertFactory;

    public HypotensiveHypoxemiaStrategy() {
        this.bpAlertFactory = new BloodPressureAlertFactory();
    }

    @Override
    public Alert checkAlert(List<PatientRecord> records, int patientId) {
        List<PatientRecord> systolicRecords = getSystolicBPRecords(records);
        List<PatientRecord> oxygenRecords = getOxygenSaturationRecords(records);

        if (systolicRecords.isEmpty() || oxygenRecords.isEmpty()) return null;

        PatientRecord latestSystolic = systolicRecords.get(systolicRecords.size() - 1);
        PatientRecord latestOxygen = oxygenRecords.get(oxygenRecords.size() - 1);

        if (latestSystolic.getMeasurementValue() < 90 && latestOxygen.getMeasurementValue() < 92) {
            return bpAlertFactory.createAlert(
                    String.valueOf(patientId),
                    "Hypotensive Hypoxemia Alert",
                    Math.max(latestSystolic.getTimestamp(), latestOxygen.getTimestamp())
            );
        }

        return null;
    }

    private List<PatientRecord> getSystolicBPRecords(List<PatientRecord> records) {
        return records.stream()
                .filter(record -> "Blood Pressure".equals(record.getRecordType()))
                .collect(Collectors.toList());
    }


    private List<PatientRecord> getOxygenSaturationRecords(List<PatientRecord> records) {
        return records.stream()
                .filter(record -> "Oxygen Saturation".equals(record.getRecordType()))
                .collect(Collectors.toList());
    }
}
