package com.data_management;

import com.cardio_generator.websocket.SimpleWebSocketClient;

import java.io.IOException;
import java.net.URI;

/**
 * Reads real-time patient data from a WebSocket server and stores it in {@link DataStorage}.
 * <p>
 * This class implements the {@link DataReader} interface and acts as a client
 * connecting to a WebSocket server to receive data updates.
 */
public class WebSocketDataReader implements DataReader {

    private final String websocketUrl;

    /**
     * Constructs a new WebSocketDataReader with the specified WebSocket server URL.
     *
     * @param websocketUrl the URL of the WebSocket server
     */
    public WebSocketDataReader(String websocketUrl) {
        this.websocketUrl = websocketUrl;
    }

    /**
     * Connects to the WebSocket server and starts receiving data.
     * Received data is passed to a {@link SimpleWebSocketClient}, which adds it to the provided {@link DataStorage} instance.
     *
     * @param dataStorage the {@link DataStorage} instance to populate with incoming data
     * @throws IOException if there is a failure connecting to the WebSocket server or processing the URI
     */
    @Override
    public void readData(DataStorage dataStorage) throws IOException {
        // sets up to read data that is sent to the server
        try {
            URI uri = new URI(websocketUrl);
            SimpleWebSocketClient client = new SimpleWebSocketClient(uri);
            client.connectBlocking();
        } catch (Exception e) {
            throw new IOException("Failed to connect to WebSocket server", e);
        }
    }
}
