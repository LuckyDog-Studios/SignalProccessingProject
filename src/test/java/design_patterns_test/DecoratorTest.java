package design_patterns_test;

import com.alerts.AlertComponent;
import com.alerts.Decorator.PriorityAlertDecorator;
import com.alerts.Decorator.RepeatedAlertDecorator;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DecoratorTest {

    // Basic implementation of AlertComponent for testing
    static class BasicAlert implements AlertComponent {
        private String patientId;
        private String condition;
        private long timestamp;
        private String alertType;

        public BasicAlert(String patientId, String condition, long timestamp, String alertType) {
            this.patientId = patientId;
            this.condition = condition;
            this.timestamp = timestamp;
            this.alertType = alertType;
        }

        @Override
        public String getPatientId() {
            return patientId;
        }

        @Override
        public String getCondition() {
            return condition;
        }

        @Override
        public long getTimestamp() {
            return timestamp;
        }

        @Override
        public String getAlertType() {
            return alertType;
        }

        @Override
        public String toString() {
            return "Patient: " + patientId + ", Condition: " + condition + ", Type: " + alertType + ", Time: " + timestamp;
        }
    }

    @Test
    void testPriorityAlertDecorator() {
        // Setup
        AlertComponent alert = new BasicAlert("123", "Low Oxygen Saturation", 1000L, "Critical");

        // Decorate with Priority
        AlertComponent priorityAlert = new PriorityAlertDecorator(alert, "High");

        // Test that priority is added to the alert string
        assertEquals("[PRIORITY: HIGH] Patient: 123, Condition: Low Oxygen Saturation, Type: Critical, Time: 1000", priorityAlert.toString());
    }

    @Test
    void testRepeatedAlertDecorator() {
        // Setup
        AlertComponent alert = new BasicAlert("123", "Low Oxygen Saturation", 1000L, "Critical");

        // Decorate with Repeated Alert
        RepeatedAlertDecorator repeatedAlert = new RepeatedAlertDecorator(alert, 3, 1000L);

        // Test that repeated check string is appended
        assertEquals("Patient: 123, Condition: Low Oxygen Saturation, Type: Critical, Time: 1000 [Repeated check x3]", repeatedAlert.toString());

        // This method is more difficult to test in a traditional way because it has a side effect (printing to console).
        // We'll assume it runs correctly since it's a simple loop with Thread.sleep.
        // Test can be more comprehensive with mock or other testing approaches for async behavior.
    }

    @Test
    void testDecoratorChaining() {
        // Setup
        AlertComponent alert = new BasicAlert("123", "Low Oxygen Saturation", 1000L, "Critical");

        // Apply both decorators
        AlertComponent priorityAlert = new PriorityAlertDecorator(alert, "High");
        RepeatedAlertDecorator repeatedAlert = new RepeatedAlertDecorator(priorityAlert, 2, 500L);

        // Test that both decorators have been applied correctly
        assertEquals("[PRIORITY: HIGH] Patient: 123, Condition: Low Oxygen Saturation, Type: Critical, Time: 1000 [Repeated check x2]", repeatedAlert.toString());
    }

    @Test
    void testRepeatedAlertPerformRechecks() {
        // Setup
        AlertComponent alert = new BasicAlert("123", "Low Oxygen Saturation", 1000L, "Critical");

        // Decorate with Repeated Alert
        RepeatedAlertDecorator repeatedAlert = new RepeatedAlertDecorator(alert, 2, 500L);

        // Perform rechecks (simulate waiting)
        // We are not directly testing the time behavior, but we ensure no exception is thrown
        repeatedAlert.performRechecks();
    }

    @Test
    void testAlertToString() {
        // Setup
        AlertComponent alert = new BasicAlert("123", "Low Oxygen Saturation", 1000L, "Critical");

        // Apply the decorator and check the toString result
        PriorityAlertDecorator priorityAlert = new PriorityAlertDecorator(alert, "Low");

        // Test the toString output
        assertEquals("[PRIORITY: LOW] Patient: 123, Condition: Low Oxygen Saturation, Type: Critical, Time: 1000", priorityAlert.toString());
    }
}
