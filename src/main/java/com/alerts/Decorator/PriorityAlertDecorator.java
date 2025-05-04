package com.alerts.Decorator;

import com.alerts.AlertComponent;

public class PriorityAlertDecorator extends AlertDecorator {
    private String priorityLevel;

    public PriorityAlertDecorator(AlertComponent alert, String priorityLevel) {
        super(alert);
        this.priorityLevel = priorityLevel;
    }

    @Override
    public String toString() {
        return "[PRIORITY: " + priorityLevel.toUpperCase() + "] " + alert.toString();
    }
}
