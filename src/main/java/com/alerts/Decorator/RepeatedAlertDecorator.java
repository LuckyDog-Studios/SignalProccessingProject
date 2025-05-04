package com.alerts.Decorator;

import com.alerts.AlertComponent;

public class RepeatedAlertDecorator extends AlertDecorator {
    private int repeatCount;
    private long intervalMillis;

    public RepeatedAlertDecorator(AlertComponent alert, int repeatCount, long intervalMillis) {
        super(alert);
        this.repeatCount = repeatCount;
        this.intervalMillis = intervalMillis;
    }

    public void performRechecks() {
        for (int i = 1; i <= repeatCount; i++) {
            try {
                Thread.sleep(intervalMillis); // simulate waiting
                System.out.println("[RE-CHECK #" + i + "] Re-evaluating alert: " + alert);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public String toString() {
        return alert.toString() + " [Repeated check x" + repeatCount + "]";
    }
}
