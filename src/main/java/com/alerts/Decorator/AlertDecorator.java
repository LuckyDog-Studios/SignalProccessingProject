package com.alerts.Decorator;

import com.alerts.AlertComponent;

public abstract class AlertDecorator implements AlertComponent {
    protected AlertComponent alert;

    public AlertDecorator(AlertComponent alert) {
        this.alert = alert;
    }

    @Override
    public String getPatientId() {
        return alert.getPatientId();
    }

    @Override
    public String getCondition() {
        return alert.getCondition();
    }

    @Override
    public long getTimestamp() {
        return alert.getTimestamp();
    }

    @Override
    public String getAlertType() {
        return alert.getAlertType();
    }

    @Override
    public String toString() {
        return alert.toString();
    }
}
