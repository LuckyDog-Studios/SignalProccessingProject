package websocket;

import com.cardio_generator.websocket.SimpleWebSocketClient;
import com.data_management.DataStorage;
import com.data_management.WebSocketDataReader;
import org.java_websocket.server.WebSocketServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class WebSocketDataReaderTest {

    private static final int TEST_PORT = 9999;
    private static TestWebSocketServer server;
    private DataStorage dataStorage;

    @BeforeAll
    static void startServer() {
        server = new TestWebSocketServer(new InetSocketAddress(TEST_PORT));
        server.start();
        try {
            Thread.sleep(500);  // Allow time for server to start
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @AfterAll
    static void stopServer() throws IOException, InterruptedException {
        server.stop(1000);
    }

    @BeforeEach
    void setup() {
        dataStorage = DataStorage.getInstance();
    }

    @Test
    void testWebSocketDataReaderReceivesAndStoresData() throws IOException {
        WebSocketDataReader reader = new WebSocketDataReader("ws://localhost:" + TEST_PORT);
        new Thread(() -> {
            try {
                reader.readData(dataStorage);
            } catch (IOException e) {
                fail("Failed to connect to WebSocket server: " + e.getMessage());
            }
        }).start();

        // Wait for connection and send message
        try {
            TimeUnit.SECONDS.sleep(1);
            server.sendTestMessage("0,1747209839355,Cholesterol,150.0");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Allow time for message to be received and processed
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertFalse(dataStorage.getRecords(0, 0, Long.MAX_VALUE).isEmpty(),
                "DataStorage should contain at least one record for patient 0");
    }

    // test server
    static class TestWebSocketServer extends WebSocketServer {
        private WebSocket conn;

        public TestWebSocketServer(InetSocketAddress address) {
            super(address);
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            this.conn = conn;
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
        public void onStart() {}

        public void sendTestMessage(String message) {
            if (conn != null && conn.isOpen()) {
                conn.send(message);
            }
        }
    }
}
