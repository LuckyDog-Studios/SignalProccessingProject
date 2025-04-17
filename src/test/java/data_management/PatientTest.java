package data_management;

import com.data_management.Patient;
import com.data_management.PatientRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PatientTest {

    private Patient patient;
    private long now;
    private long oneHour;
    private long twoHours;

    @BeforeEach
    public void setUp() {
        patient = new Patient(1);
        now = System.currentTimeMillis();
        oneHour = now + 3600000;  // +1 hour
        twoHours = now + 7200000; // +2 hours

        patient.addRecord(70.0, "HeartRate", now);
        patient.addRecord(120.0, "BloodPressureSystolic", oneHour);
        patient.addRecord(85.0, "BloodPressureDiastolic", twoHours);
    }

    @Test
    public void testGetRecordsInRange() {
        List<PatientRecord> records = patient.getRecords(now, oneHour);
        assertEquals(2, records.size(), "Should return 2 records in the specified range");

        assertTrue(records.stream().anyMatch(r -> r.getRecordType().equals("HeartRate")));
        assertTrue(records.stream().anyMatch(r -> r.getRecordType().equals("BloodPressureSystolic")));
    }

    @Test
    public void testGetRecordsOutOfRange() {
        long future = twoHours + 3600000;
        List<PatientRecord> records = patient.getRecords(future, future + 1000);
        assertTrue(records.isEmpty(), "Should return 0 records for a future range");
    }

    @Test
    public void testGetRecordsExactMatch() {
        List<PatientRecord> records = patient.getRecords(twoHours, twoHours);
        assertEquals(1, records.size(), "Should return 1 record for exact timestamp match");
        assertEquals("BloodPressureDiastolic", records.get(0).getRecordType());
    }
}
