package com.alerts;

import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;

import java.util.*;

/**
 * The {@code AlertGenerator} class is responsible for monitoring patient data
 * and generating alerts when certain predefined conditions are met. This class
 * relies on a {@link DataStorage} instance to access patient data and evaluate
 * it against specific health criteria.
 */
public class AlertGenerator {
    private DataStorage dataStorage;

    private final AlertFactory bpAlertFactory = new BloodPressureAlertFactory();
    private final AlertFactory oxygenAlertFactory = new BloodOxygenAlertFactory();
    private final AlertFactory ecgAlertFactory = new ECGAlertFactory();
    /**
     * Constructs an {@code AlertGenerator} with a specified {@code DataStorage}.
     * The {@code DataStorage} is used to retrieve patient data that this class
     * will monitor and evaluate.
     *
     * @param dataStorage the data storage system that provides access to patient
     *                    data
     */
    public AlertGenerator(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
    }

    /**
     * Evaluates the specified patient's data to determine if any alert conditions
     * are met. If a condition is met, an alert is triggered via the
     * {@link #triggerAlert}
     * method. This method should define the specific conditions under which an
     * alert
     * will be triggered.
     *
     * @param patient the patient data to evaluate for alert conditions
     */
    public void evaluateData(Patient patient) {
        List<PatientRecord> records = patient.getRecords();

        List<PatientRecord> systolic = new ArrayList<>();
        List<PatientRecord> diastolic = new ArrayList<>();
        List<PatientRecord> saturation = new ArrayList<>();

        for (PatientRecord record : records) {
            switch (record.getRecordType()) {
                case "BloodPressureSystolic":
                    systolic.add(record);
                    break;
                case "BloodPressureDiastolic":
                    diastolic.add(record);
                    break;
                case "BloodSaturation":
                    saturation.add(record);
                    break;
            }
        }

        checkBloodPressureTrends(systolic, patient.getId(), "Systolic");
        checkBloodPressureTrends(diastolic, patient.getId(), "Diastolic");

        checkCriticalBPThreshold(systolic, patient.getId(), "Systolic", 90, 180);
        checkCriticalBPThreshold(diastolic, patient.getId(), "Diastolic", 60, 120);

        checkSaturation(saturation, patient.getId());
        checkCombinedHypotensiveHypoxemia(systolic, saturation, patient.getId());

        checkECGPeaks(records, patient.getId());
    }

    // checks for a rising or falling blood pressure trend over three readings
    private void checkBloodPressureTrends(List<PatientRecord> records, int patientId, String label) {
        if (records.size() < 3) return;

        records.sort(Comparator.comparingLong(PatientRecord::getTimestamp));

        for (int i = 0; i <= records.size() - 3; i++) {
            double v1 = records.get(i).getMeasurementValue();
            double v2 = records.get(i + 1).getMeasurementValue();
            double v3 = records.get(i + 2).getMeasurementValue();

            boolean up = v2 - v1 > 10 && v3 - v2 > 10;
            boolean down = v1 - v2 > 10 && v2 - v3 > 10;

            if (up) {
                Alert alert = bpAlertFactory.createAlert(
                        String.valueOf(patientId),
                        label + " BP rising trend",
                        records.get(i + 2).getTimestamp()
                );
                triggerAlert(alert);
            } else if (down) {
                Alert alert = bpAlertFactory.createAlert(
                        String.valueOf(patientId),
                        label + " BP falling trend",
                        records.get(i + 2).getTimestamp()
                );
                triggerAlert(alert);
            }
        }
    }

    // triggers an alert if the latest blood pressure reading exceeds a threshold
    private void checkCriticalBPThreshold(List<PatientRecord> records, int patientId, String label, double min, double max) {
        if (records.isEmpty()) return;

        PatientRecord latest = Collections.max(records, Comparator.comparingLong(PatientRecord::getTimestamp));
        double value = latest.getMeasurementValue();

        if (value < min || value > max) {
            Alert alert = bpAlertFactory.createAlert(
                    String.valueOf(patientId),
                    label + " BP critical",
                    latest.getTimestamp()
            );
            triggerAlert(alert);
        }
    }

    // triggers alerts for low oxygen saturation or a rapid drop within 10 minutes
    private void checkSaturation(List<PatientRecord> records, int patientId) {
        if (records.isEmpty()) return;

        records.sort(Comparator.comparingLong(PatientRecord::getTimestamp));
        PatientRecord latest = records.get(records.size() - 1);

        if (latest.getMeasurementValue() < 92) {
            Alert alert = oxygenAlertFactory.createAlert(
                    String.valueOf(patientId),
                    "Low oxygen saturation",
                    latest.getTimestamp()
            );
            triggerAlert(alert);
        }

        for (int i = records.size() - 2; i >= 0; i--) {
            PatientRecord previous = records.get(i);
            long timeDiff = latest.getTimestamp() - previous.getTimestamp();

            if (timeDiff > 600_000) break;

            if (previous.getMeasurementValue() - latest.getMeasurementValue() >= 5) {
                Alert alert = oxygenAlertFactory.createAlert(
                        String.valueOf(patientId),
                        "Rapid drop in oxygen saturation",
                        latest.getTimestamp()
                );
                triggerAlert(alert);
                break;
            }
        }
    }

    // triggers a combined alert when both systolic BP is low and oxygen saturation is below safe levels
    private void checkCombinedHypotensiveHypoxemia(List<PatientRecord> systolic, List<PatientRecord> saturation, int patientId) {
        if (systolic.isEmpty() || saturation.isEmpty()) return;

        PatientRecord latestSys = Collections.max(systolic, Comparator.comparingLong(PatientRecord::getTimestamp));
        PatientRecord latestSat = Collections.max(saturation, Comparator.comparingLong(PatientRecord::getTimestamp));

        if (latestSys.getMeasurementValue() < 90 && latestSat.getMeasurementValue() < 92) {
            // You may want a separate factory for combined alerts; using bpAlertFactory here for simplicity
            Alert alert = bpAlertFactory.createAlert(
                    String.valueOf(patientId),
                    "Hypotensive Hypoxemia Alert",
                    Math.max(latestSys.getTimestamp(), latestSat.getTimestamp())
            );
            triggerAlert(alert);
        }
    }

    // there's no HealthDataGenerator class, so I just put a method here that handles manual triggered alert
    public void handleTriggeredAlert(int patientId, long timestamp) {
        Alert alert = new Alert(
                String.valueOf(patientId),
                "Manual Triggered Alert",
                timestamp,
                "Manual"
        );
        triggerAlert(alert);
    }

    // checks ECG readings for abnormal peaks using a simple window average
    private void checkECGPeaks(List<PatientRecord> ecgRecords, int patientId) {
        if (ecgRecords.size() < 5) return;

        ecgRecords.sort(Comparator.comparingLong(PatientRecord::getTimestamp));
        int windowSize = 5;

        for (int i = windowSize; i < ecgRecords.size(); i++) {
            double sum = 0;
            for (int j = i - windowSize; j < i; j++) {
                sum += ecgRecords.get(j).getMeasurementValue();
            }
            double avg = sum / windowSize;
            double current = ecgRecords.get(i).getMeasurementValue();

            if (current > avg * 1.5) {
                Alert alert = ecgAlertFactory.createAlert(
                        String.valueOf(patientId),
                        "Abnormal ECG peak detected",
                        ecgRecords.get(i).getTimestamp()
                );
                triggerAlert(alert);
            }
        }
    }


    /**
     * Triggers an alert for the monitoring system. This method can be extended to
     * notify medical staff, log the alert, or perform other actions. The method
     * currently assumes that the alert information is fully formed when passed as
     * an argument.
     *
     * @param alert the alert object containing details about the alert condition
     */
    private void triggerAlert(Alert alert) {
        // Implementation might involve logging the alert or notifying staff
        System.out.println("ALERT: " + alert);
    }
}
