package websocket;

import com.alerts.AlertGenerator;
import com.data_management.DataStorage;
import com.data_management.Patient;
import com.data_management.PatientRecord;
import com.data_management.WebSocketDataReader;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WebSocketIntegrationTest {

    private static TestWebSocketServer server;

    static class TestWebSocketServer extends WebSocketServer {
        private WebSocket conn;

        public TestWebSocketServer(int port) {
            super(new InetSocketAddress(port));
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            this.conn = conn;
            // send testing data
            conn.send("1,1700000000000,HeartRate,85");
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {}

        @Override
        public void onMessage(WebSocket conn, String message) {}

        @Override
        public void onError(WebSocket conn, Exception ex) {
            ex.printStackTrace();
        }

        @Override
        public void onStart() {
            System.out.println("WebSocket server started");
        }

        public void stopServer() throws InterruptedException {
            this.stop();
        }
    }

    @BeforeAll
    public static void startServer() throws InterruptedException {
        server = new TestWebSocketServer(8887);
        server.start();
        Thread.sleep(1000);
    }

    @AfterAll
    public static void stopServer() throws IOException, InterruptedException {
        server.stopServer();
    }

    @Test
    public void testWebSocketClientIntegration() throws IOException, InterruptedException {
        DataStorage storage = DataStorage.getInstance();
        WebSocketDataReader reader = new WebSocketDataReader("ws://localhost:8887");
        reader.readData(storage);

        Thread.sleep(2000);

        List<PatientRecord> records = storage.getRecords(1, 0, Long.MAX_VALUE);
        assertFalse(records.isEmpty(), "Data should be stored after WebSocket message.");

        PatientRecord record = records.get(0);
        assertEquals(1, record.getPatientId());
        assertEquals("HeartRate", record.getRecordType());
        assertEquals(85.0, record.getMeasurementValue());
        assertEquals(1700000000000L, record.getTimestamp());
    }

    @Test
    public void testAlertEvaluation_afterWebSocketData() throws IOException, InterruptedException {
        DataStorage storage = DataStorage.getInstance();
        WebSocketDataReader reader = new WebSocketDataReader("ws://localhost:8887");
        reader.readData(storage);
        Thread.sleep(2000);

        Patient patient = storage.getAllPatients().get(0);
        AlertGenerator alertGenerator = new AlertGenerator();

        // To capture System.out
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        // Trigger some alerts
        patient.addRecord(new PatientRecord(1, 55, "HeartRate", System.currentTimeMillis()));
        alertGenerator.evaluateData(patient);

        patient.addRecord(new PatientRecord(1, 89, "BloodSaturation", System.currentTimeMillis()));
        alertGenerator.evaluateData(patient);

        patient.addRecord(new PatientRecord(1, 85, "BloodPressureSystolic", System.currentTimeMillis()));
        alertGenerator.evaluateData(patient);

        patient.addRecord(new PatientRecord(1, 1.0, "ECG", System.currentTimeMillis()));
        alertGenerator.evaluateData(patient);

        // Restore System.out
        System.setOut(System.out);

        String output = outContent.toString();
        assertTrue(output.contains("ALERT:"), "Expected alert output.");
        assertTrue(output.contains("HeartRate") || output.contains("BloodSaturation") || output.contains("ECG"), "Expected specific alert content.");
    }

}
