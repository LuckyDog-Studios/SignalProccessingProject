package websocket;

import com.cardio_generator.websocket.SimpleWebSocketClient;
import com.data_management.DataStorage;
import com.data_management.PatientRecord;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.junit.jupiter.api.*;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class SimpleWebSocketClientTest {

    private static final int TEST_PORT = 8889;
    private static TestWebSocketServer server;
    private SimpleWebSocketClient client;
    private DataStorage dataStorage;

    @BeforeAll
    static void startServer() throws Exception {
        server = new TestWebSocketServer(new InetSocketAddress(TEST_PORT));
        server.start();
        Thread.sleep(500);
    }

    @AfterAll
    static void stopServer() throws Exception {
        server.stop(1000);
    }

    @BeforeEach
    void setUp() throws Exception {
        dataStorage = DataStorage.getInstance();
        URI uri = new URI("ws://localhost:" + TEST_PORT);
        client = new SimpleWebSocketClient(uri);
        client.connectBlocking();
    }

    @AfterEach
    void tearDown() throws Exception {
        client.closeBlocking();
    }

    @Test
    void testValidMessageIsParsedAndStored() throws Exception {
        String validMessage = "1,1747209839355,Cholesterol,180.5";
        server.broadcast(validMessage);
        TimeUnit.MILLISECONDS.sleep(500);

        List<PatientRecord> records = dataStorage.getRecords(1, 0, Long.MAX_VALUE);
        assertFalse(records.isEmpty(), "Expected data storage to have at least one record");
        PatientRecord record = records.get(0);
        assertEquals("Cholesterol", record.getRecordType());
        assertEquals(180.5, record.getMeasurementValue(), 0.01);
    }

    @Test
    void testInvalidMessageIsIgnored() throws Exception {
        String invalidMessage = "InvalidMessageFormat";
        server.broadcast(invalidMessage);
        TimeUnit.MILLISECONDS.sleep(300);

        assertTrue(dataStorage.getRecords(99, 0, Long.MAX_VALUE).isEmpty());
    }

    @Test
    void testPercentageSymbolIsStripped() throws Exception {
        String message = "2,1747209839356,Saturation,95.0%";
        server.broadcast(message);
        TimeUnit.MILLISECONDS.sleep(300);

        List<PatientRecord> records = dataStorage.getRecords(2, 0, Long.MAX_VALUE);
        assertFalse(records.isEmpty(), "Expected a record for patient 2");
        assertEquals(95.0, records.get(0).getMeasurementValue(), 0.01);
    }

    // test server
    static class TestWebSocketServer extends WebSocketServer {
        public TestWebSocketServer(InetSocketAddress address) {
            super(address);
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            System.out.println("Test server accepted connection");
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
            System.out.println("Test WebSocket server started on port " + getPort());
        }
    }
}
