package design_patterns_test;

import com.alerts.AlertGenerator;
import com.alerts.Strategy.*;
import com.data_management.Patient;
import com.data_management.PatientRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class AlertGeneratorTest {

    private AlertGenerator alertGenerator;

    @BeforeEach
    void setUp() {
        alertGenerator = new AlertGenerator();
    }

    // === AlertGenerator Tests ===

    @Test
    void testEvaluateData_triggersBloodPressureAlert() {
        Patient patient = new Patient(1);
        patient.addRecord(new PatientRecord(1, 100, "BloodPressureSystolic", 1000));
        patient.addRecord(new PatientRecord(1, 115, "BloodPressureSystolic", 2000));
        patient.addRecord(new PatientRecord(1, 130, "BloodPressureSystolic", 3000));

        alertGenerator.evaluateData(patient);
    }

    @Test
    void testHandleTriggeredAlert_shouldTriggerManualAlert() {
        alertGenerator.handleTriggeredAlert(99, System.currentTimeMillis());
    }

    // === BloodPressureStrategy Tests ===

    @Test
    void testBloodPressureStrategy_risingTrendTriggersAlert() {
        AlertStrategy strategy = new BloodPressureStrategy("Systolic");
        List<PatientRecord> records = Arrays.asList(
                new PatientRecord(1, 90, "Blood Pressure", 1000),
                new PatientRecord(1, 105, "Blood Pressure", 2000),
                new PatientRecord(1, 120, "Blood Pressure", 3000)
        );

        var alert = strategy.checkAlert(records, 1);
        assertNotNull(alert);
        assertTrue(alert.getCondition().contains("rising"));
    }

    @Test
    void testBloodPressureStrategy_noTrendNoAlert() {
        AlertStrategy strategy = new BloodPressureStrategy("Systolic");
        List<PatientRecord> records = Arrays.asList(
                new PatientRecord(1, 100, "Blood Pressure", 1000),
                new PatientRecord(1, 101, "Blood Pressure", 2000),
                new PatientRecord(1, 102, "Blood Pressure", 3000)
        );

        var alert = strategy.checkAlert(records, 1);
        assertNull(alert);
    }

    // === OxygenSaturationStrategy Tests ===

    @Test
    void testOxygenSaturationStrategy_lowOxygenTriggersAlert() {
        AlertStrategy strategy = new OxygenSaturationStrategy();
        List<PatientRecord> records = Arrays.asList(
                new PatientRecord(1, 89, "Oxygen Saturation", 1000),
                new PatientRecord(1, 90, "Oxygen Saturation", 2000),
                new PatientRecord(1, 89, "Oxygen Saturation", 3000)

        );

        var alert = strategy.checkAlert(records, 1);
        assertNotNull(alert, "Alert should not be null for low oxygen saturation");
        assertEquals("Low oxygen saturation", alert.getCondition(), "Alert condition message is incorrect");
    }


    @Test
    void testOxygenSaturationStrategy_rapidDropTriggersAlert() {
        AlertStrategy strategy = new OxygenSaturationStrategy();
        List<PatientRecord> records = Arrays.asList(
                new PatientRecord(1, 98, "Oxygen Saturation", 1000),
                new PatientRecord(1, 92, "Oxygen Saturation", 2000),
                new PatientRecord(1, 87, "Oxygen Saturation", 2500)
        );

        var alert = strategy.checkAlert(records, 1);
        assertNotNull(alert);
        assertEquals("Rapid drop in oxygen saturation", alert.getCondition());
    }

    // === HeartRateStrategy Tests ===

    @Test
    void testHeartRateStrategy_triggersBradycardiaAlert() {
        AlertStrategy strategy = new HeartRateStrategy();
        List<PatientRecord> records = List.of(
                new PatientRecord(1, 55, "Heart Rate", 1000)
        );

        var alert = strategy.checkAlert(records, 1);
        assertNotNull(alert);
        assertTrue(alert.getCondition().contains("Bradycardia"));
    }

    @Test
    void testHeartRateStrategy_normalHeartRateNoAlert() {
        AlertStrategy strategy = new HeartRateStrategy();
        List<PatientRecord> records = List.of(
                new PatientRecord(1, 80, "Heart Rate", 1000)
        );

        var alert = strategy.checkAlert(records, 1);
        assertNull(alert);
    }

    // === ECGStrategy Tests ===

    @Test
    void testECGStrategy_abnormalECGTriggersAlert() {
        AlertStrategy strategy = new ECGStrategy();
        List<PatientRecord> records = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            records.add(new PatientRecord(1, 1.0, "ECG", 1000 * i));
        }
        records.add(new PatientRecord(1, 2.0, "ECG", 6000)); // Spike

        var alert = strategy.checkAlert(records, 1);
        assertNotNull(alert);
        assertTrue(alert.getCondition().contains("Abnormal ECG"));
    }

    @Test
    void testECGStrategy_normalECGNoAlert() {
        AlertStrategy strategy = new ECGStrategy();
        List<PatientRecord> records = List.of(
                new PatientRecord(1, 1.0, "ECG", 1000),
                new PatientRecord(1, 1.0, "ECG", 2000),
                new PatientRecord(1, 1.0, "ECG", 3000),
                new PatientRecord(1, 1.0, "ECG", 4000),
                new PatientRecord(1, 1.0, "ECG", 5000),
                new PatientRecord(1, 1.1, "ECG", 6000)

        );

        var alert = strategy.checkAlert(records, 1);
        assertNull(alert);
    }

    // === HypotensiveHypoxemiaStrategy Tests ===

    @Test
    void testHypotensiveHypoxemiaStrategy_combinedConditionTriggersAlert() {
        AlertStrategy strategy = new HypotensiveHypoxemiaStrategy();
        List<PatientRecord> records = Arrays.asList(
                new PatientRecord(1, 89, "Blood Pressure", 1000),
                new PatientRecord(1, 91, "Oxygen Saturation", 1000)
        );

        var alert = strategy.checkAlert(records, 1);
        assertNotNull(alert);
        assertEquals("Hypotensive Hypoxemia Alert", alert.getCondition());
    }

    @Test
    void testHypotensiveHypoxemiaStrategy_onlyOneConditionNoAlert() {
        AlertStrategy strategy = new HypotensiveHypoxemiaStrategy();
        List<PatientRecord> records = Arrays.asList(
                new PatientRecord(1, 91, "Blood Pressure", 1000),
                new PatientRecord(1, 91, "Oxygen Saturation", 1000)
        );

        var alert = strategy.checkAlert(records, 1);
        assertNull(alert);
    }
}
