package design_patterns_test;

import com.alerts.Alert;
import com.alerts.Factory.AlertFactory;
import com.alerts.Factory.ECGAlertFactory;
import com.alerts.Factory.BloodPressureAlertFactory;
import com.alerts.Factory.BloodOxygenAlertFactory;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FactoryTest {

    @Test
    void testECGAlertFactory() {
        // Arrange
        AlertFactory factory = new ECGAlertFactory();
        String patientId = "123";
        String condition = "ECG Abnormality";
        long timestamp = 1000L;

        // Act
        Alert alert = factory.createAlert(patientId, condition, timestamp);

        // Assert
        assertNotNull(alert, "Alert should not be null");
        assertEquals(patientId, alert.getPatientId(), "Patient ID should match");
        assertEquals(condition, alert.getCondition(), "Condition should match");
        assertEquals(timestamp, alert.getTimestamp(), "Timestamp should match");
        assertEquals("ECG", alert.getAlertType(), "Alert type should be 'ECG'");
    }

    @Test
    void testBloodPressureAlertFactory() {
        // Arrange
        AlertFactory factory = new BloodPressureAlertFactory();
        String patientId = "456";
        String condition = "High Blood Pressure";
        long timestamp = 2000L;

        // Act
        Alert alert = factory.createAlert(patientId, condition, timestamp);

        // Assert
        assertNotNull(alert, "Alert should not be null");
        assertEquals(patientId, alert.getPatientId(), "Patient ID should match");
        assertEquals(condition, alert.getCondition(), "Condition should match");
        assertEquals(timestamp, alert.getTimestamp(), "Timestamp should match");
        assertEquals("BloodPressure", alert.getAlertType(), "Alert type should be 'BloodPressure'");
    }

    @Test
    void testBloodOxygenAlertFactory() {
        // Arrange
        AlertFactory factory = new BloodOxygenAlertFactory();
        String patientId = "789";
        String condition = "Low Blood Oxygen";
        long timestamp = 3000L;

        // Act
        Alert alert = factory.createAlert(patientId, condition, timestamp);

        // Assert
        assertNotNull(alert, "Alert should not be null");
        assertEquals(patientId, alert.getPatientId(), "Patient ID should match");
        assertEquals(condition, alert.getCondition(), "Condition should match");
        assertEquals(timestamp, alert.getTimestamp(), "Timestamp should match");
        assertEquals("BloodOxygen", alert.getAlertType(), "Alert type should be 'BloodOxygen'");
    }

    @Test
    void testFactoryIsAbstract() {
        // Ensure AlertFactory is abstract and cannot be instantiated directly
        assertThrows(InstantiationException.class, () -> {
            AlertFactory factory = (AlertFactory) Class.forName("com.alerts.Factory.AlertFactory").newInstance();
        });
    }

    @Test
    void testAlertTypesAreDistinct() {
        // Arrange
        AlertFactory ecgFactory = new ECGAlertFactory();
        AlertFactory bpFactory = new BloodPressureAlertFactory();
        AlertFactory oxygenFactory = new BloodOxygenAlertFactory();

        // Act
        Alert ecgAlert = ecgFactory.createAlert("101", "ECG Abnormality", 1000L);
        Alert bpAlert = bpFactory.createAlert("102", "High Blood Pressure", 2000L);
        Alert oxygenAlert = oxygenFactory.createAlert("103", "Low Oxygen Level", 3000L);

        // Assert that alert types are distinct
        assertNotEquals(ecgAlert.getAlertType(), bpAlert.getAlertType(), "ECG and BloodPressure alert types should be different");
        assertNotEquals(bpAlert.getAlertType(), oxygenAlert.getAlertType(), "BloodPressure and BloodOxygen alert types should be different");
        assertNotEquals(ecgAlert.getAlertType(), oxygenAlert.getAlertType(), "ECG and BloodOxygen alert types should be different");
    }
}
