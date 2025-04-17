package alerts;

import com.alerts.AlertGenerator;
import com.data_management.DataStorage;
import com.data_management.Patient;
import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AlertGeneratorTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
    }

    @Test
    public void testNoAlertForNormalData() {
        Patient patient = new Patient(3);
        patient.addRecord(72.0, "HeartRate", System.currentTimeMillis());
        patient.addRecord(110.0, "BloodPressureSystolic", System.currentTimeMillis());

        AlertGenerator generator = new AlertGenerator(new DataStorage());
        generator.evaluateData(patient);

        assertTrue(outContent.toString().isBlank(), "No alert should be triggered for normal values");
    }

    @Test
    public void testSystolicBPTrendIncreasingAlert() {
        Patient patient = new Patient(4);
        long now = System.currentTimeMillis();
        patient.addRecord(100.0, "BloodPressureSystolic", now - 30000);
        patient.addRecord(111.0, "BloodPressureSystolic", now - 20000);
        patient.addRecord(123.0, "BloodPressureSystolic", now - 10000); // >10mmHg increase

        AlertGenerator generator = new AlertGenerator(new DataStorage());
        generator.evaluateData(patient);

        assertTrue(outContent.toString().contains("Systolic BP rising trend"));
    }

    @Test
    public void testDiastolicBPTrendDecreasingAlert() {
        Patient patient = new Patient(5);
        long now = System.currentTimeMillis();
        patient.addRecord(90.0, "BloodPressureDiastolic", now - 30000);
        patient.addRecord(75.0, "BloodPressureDiastolic", now - 20000);
        patient.addRecord(60.0, "BloodPressureDiastolic", now - 10000); // Decreasing

        AlertGenerator generator = new AlertGenerator(new DataStorage());
        generator.evaluateData(patient);

        assertTrue(outContent.toString().contains("Diastolic BP falling trend"));
    }

    @Test
    public void testCriticalThresholdAlertSystolic() {
        Patient patient = new Patient(6);
        patient.addRecord(185.0, "BloodPressureSystolic", System.currentTimeMillis()); // Over 180

        AlertGenerator generator = new AlertGenerator(new DataStorage());
        generator.evaluateData(patient);

        assertTrue(outContent.toString().contains("Systolic BP critical"));
    }

    @Test
    public void testCriticalThresholdAlertDiastolic() {
        Patient patient = new Patient(7);
        patient.addRecord(50.0, "BloodPressureDiastolic", System.currentTimeMillis()); // Below 60

        AlertGenerator generator = new AlertGenerator(new DataStorage());
        generator.evaluateData(patient);

        assertTrue(outContent.toString().contains("Diastolic BP critical"));
    }

    @Test
    public void testLowSaturationAlert() {
        Patient patient = new Patient(8);
        patient.addRecord(89.0, "BloodSaturation", System.currentTimeMillis()); // Below 92

        AlertGenerator generator = new AlertGenerator(new DataStorage());
        generator.evaluateData(patient);

        assertTrue(outContent.toString().contains("Low oxygen saturation"));
    }

    @Test
    public void testRapidDropInSaturationAlert() {
        Patient patient = new Patient(9);
        long now = System.currentTimeMillis();
        patient.addRecord(97.0, "BloodSaturation", now - 600000); // 10 minutes ago
        patient.addRecord(91.5, "BloodSaturation", now);

        AlertGenerator generator = new AlertGenerator(new DataStorage());
        generator.evaluateData(patient);

        assertTrue(outContent.toString().contains("Rapid drop in oxygen saturation"));
    }

    @Test
    public void testHypotensiveHypoxemiaAlert() {
        Patient patient = new Patient(10);
        long now = System.currentTimeMillis();
        patient.addRecord(85.0, "BloodPressureSystolic", now);
        patient.addRecord(88.0, "BloodSaturation", now);

        AlertGenerator generator = new AlertGenerator(new DataStorage());
        generator.evaluateData(patient);

        assertTrue(outContent.toString().contains("Hypotensive Hypoxemia Alert"));
    }

    @Test
    public void testNoECGAlertForNormalData() {
        Patient patient = new Patient(10);
        long now = System.currentTimeMillis();

        patient.addRecord(1.0, "ECG", now + 1000);
        patient.addRecord(1.1, "ECG", now + 2000);
        patient.addRecord(1.0, "ECG", now + 3000);
        patient.addRecord(1.0, "ECG", now + 4000);
        patient.addRecord(1.0, "ECG", now + 5000);

        AlertGenerator generator = new AlertGenerator(new DataStorage());
        generator.evaluateData(patient);

        String output = outContent.toString();
        assertTrue(output.isBlank(), "No alert should be triggered for normal ECG values");
    }

    @Test
    public void testNoECGAlertForSinglePeak() {
        Patient patient = new Patient(11);
        long now = System.currentTimeMillis();

        // single normal reading without enough data
        patient.addRecord(1.0, "ECG", now);

        AlertGenerator generator = new AlertGenerator(new DataStorage());
        generator.evaluateData(patient);

        String output = outContent.toString();
        assertTrue(output.isBlank(), "No alert should be triggered for a single ECG value");
    }

    @Test
    public void testECGAlertForAbnormalPeak() {
        Patient patient = new Patient(12);
        long now = System.currentTimeMillis();

        // Normal readings within expected range
        patient.addRecord(1.0, "ECG", now + 1000);
        patient.addRecord(1.1, "ECG", now + 2000);
        patient.addRecord(1.0, "ECG", now + 3000);
        patient.addRecord(1.0, "ECG", now + 4000);
        patient.addRecord(1.0, "ECG", now + 5000);

        // Abnormal peak
        patient.addRecord(2.0, "ECG", now + 6000);  // this should trigger an alert

        AlertGenerator generator = new AlertGenerator(new DataStorage());
        generator.evaluateData(patient);

        String output = outContent.toString();
        assertTrue(output.contains("Abnormal ECG peak detected"), "Should trigger ECG peak alert when a peak exceeds the threshold");
    }



}
