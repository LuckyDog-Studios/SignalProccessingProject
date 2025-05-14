package websocket;

import com.cardio_generator.outputs.WebSocketOutputStrategy;
import com.cardio_generator.websocket.SimpleWebSocketClient;
import com.data_management.DataStorage;
import org.java_websocket.WebSocket;
import org.java_websocket.server.WebSocketServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class WebSocketErrorHandlingTest {

    private static final String WS_URI = "ws://localhost:8887";
    private WebSocketOutputStrategy strategy;
    private DataStorage dataStorage;

    @BeforeEach
    public void setUp() {
        strategy = new WebSocketOutputStrategy(8887);
        dataStorage = DataStorage.getInstance();
        dataStorage.clear();
    }
    @AfterEach
    void tearDown() throws Exception {
        WebSocketServer server = strategy.getServer();
        if (server != null) {
            try {
                server.stop();
                System.out.println("WebSocket server stopped.");
            } catch (Exception e) {
                System.err.println("Error stopping WebSocket server: " + e.getMessage());
            }
        }
    }

    // Test 1: Simulate a client disconnect and reconnect
    @Test
    public void testClientDisconnectAndReconnect() throws Exception {
        URI uri = new URI(WS_URI);

        SimpleWebSocketClient client = new SimpleWebSocketClient(uri);
        client.connectBlocking();

        // Simulate unexpected disconnect
        client.closeConnection(1006, "Simulated network failure");

        // Wait to simulate downtime
        TimeUnit.SECONDS.sleep(2);

        // Reconnect
        client.reconnectBlocking();
        assertTrue(client.isOpen(), "Client should reconnect after failure.");
    }

    // Test 2: Send malformed data (Server to Client)
    @Test
    public void testMalformedMessageHandling() throws Exception {
        URI uri = new URI(WS_URI);

        SimpleWebSocketClient client = new SimpleWebSocketClient(uri);
        client.connectBlocking();

        // Send malformed data
        strategy.output(1, System.currentTimeMillis(), "HeartRate", "MalformedData");

        // Wait for processing
        TimeUnit.SECONDS.sleep(1);

        // Validate that no data was added due to malformed message
        assertEquals(0, dataStorage.getAllPatients().size(), "No patient data should be added from malformed data.");
    }

    // Test 3: Stress testing with multiple rapid messages
    @Test
    public void testStressWithMultipleMessages() throws Exception {
        URI uri = new URI(WS_URI);

        // Create a client and connect
        SimpleWebSocketClient client = new SimpleWebSocketClient(uri);
        client.connectBlocking();

        // Stress the WebSocket server with multiple messages
        for (int i = 0; i < 100; i++) {
            strategy.output(i, System.currentTimeMillis(), "HeartRate", "80");
        }

        // Allow time for processing
        TimeUnit.SECONDS.sleep(1);

        // Check if the system still operates correctly under load
        assertTrue(!dataStorage.getAllPatients().isEmpty(), "Data should have been added after stress test.");
    }

    // Test 4: Simulate network error during message receiving (client side)
    @Test
    public void testClientSideErrorHandling() throws Exception {
        URI uri = new URI(WS_URI);

        SimpleWebSocketClient client = new SimpleWebSocketClient(uri) {
            @Override
            public void onMessage(String message) {
                try {
                    throw new RuntimeException("Simulated processing error");
                } catch (RuntimeException ex) {
                    System.err.println("Handled expected error: " + ex.getMessage());
                }
            }
        };

        client.connectBlocking();

        // Simulate receiving a bad message
        client.onMessage("BAD_DATA_FORMAT");

        // Check that no data was added after error
        TimeUnit.SECONDS.sleep(1);
        assertEquals(0, dataStorage.getAllPatients().size(), "No data should be added after error.");
    }
}
