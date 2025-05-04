package com.alerts;

import com.alerts.Decorator.PriorityAlertDecorator;
import com.alerts.Decorator.RepeatedAlertDecorator;
import com.alerts.Factory.AlertFactory;
import com.alerts.Factory.BloodOxygenAlertFactory;
import com.alerts.Factory.BloodPressureAlertFactory;
import com.alerts.Factory.ECGAlertFactory;
import com.alerts.Strategy.*;
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

    private final Map<String, AlertStrategy> alertStrategies;

    /**
     * Constructs an {@code AlertGenerator} with a specified {@code DataStorage}.
     * The {@code DataStorage} is used to retrieve patient data that this class
     * will monitor and evaluate.
     *
     */
    public AlertGenerator() {

        // Initialize all available strategies
        alertStrategies = new HashMap<>();
        alertStrategies.put("BloodPressureSystolic", new BloodPressureStrategy("Systolic"));
        alertStrategies.put("BloodPressureDiastolic", new BloodPressureStrategy("Diastolic"));
        alertStrategies.put("HeartRate", new HeartRateStrategy());  // Assuming ECG data is managed by HeartRateStrategy
        alertStrategies.put("BloodSaturation", new OxygenSaturationStrategy());
        alertStrategies.put("HypotensiveHypoxemia", new HypotensiveHypoxemiaStrategy());
        alertStrategies.put("ECG", new ECGStrategy());

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

        for (PatientRecord record : records) {
            AlertStrategy strategy = alertStrategies.get(record.getRecordType());
            if (strategy != null) {
                Alert alert = strategy.checkAlert(Collections.singletonList(record), patient.getId());
                if (alert != null) {
                    triggerAlert(alert);
                }
            }
        }
    }

    // handles manual triggered alert
    public void handleTriggeredAlert(int patientId, long timestamp) {
        Alert alert = new Alert(
                String.valueOf(patientId),
                "Manual Triggered Alert",
                timestamp,
                "Manual"
        );
        triggerAlert(alert);
    }


    /**
     * Triggers an alert for the monitoring system. This method can be extended to
     * notify medical staff, log the alert, or perform other actions. The method
     * currently assumes that the alert information is fully formed when passed as
     * an argument.
     *
     * @param alert the alert object containing details about the alert condition
     */
    private void triggerAlert(AlertComponent alert) {
        // Apply priority level if condition is met
        if (alert.getCondition().contains("critical")) {
            alert = new PriorityAlertDecorator(alert, "high");
        } else if (alert.getCondition().contains("warning")) {
            alert = new PriorityAlertDecorator(alert, "medium");
        } else {
            alert = new PriorityAlertDecorator(alert, "low");
        }

        // Apply repeated alert for certain conditions
        if (alert.getCondition().contains("ECG")) {
            alert = new RepeatedAlertDecorator(alert, 3, 60000);
        }

        // Perform repeated checks
        if (alert instanceof RepeatedAlertDecorator) {
            ((RepeatedAlertDecorator) alert).performRechecks();
        }

        // Now trigger the alert
        System.out.println("ALERT: " + alert);
    }
}
