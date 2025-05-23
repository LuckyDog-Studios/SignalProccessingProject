package com.alerts.Factory;

import com.alerts.Alert;

public class BloodOxygenAlertFactory extends AlertFactory {
    @Override
    public Alert createAlert(String patientId, String condition, long timestamp) {
        return new Alert(patientId, condition, timestamp, "BloodOxygen");
    }
}
