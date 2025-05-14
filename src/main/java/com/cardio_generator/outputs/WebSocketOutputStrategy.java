package com.cardio_generator.outputs;

import org.java_websocket.WebSocket;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.IllegalFormatException;

/**
 * Implementation of {@link OutputStrategy} that broadcasts patient data to all connected WebSocket clients.
 * <p>
 * This strategy formats patient data into a string and sends it via a WebSocket server.
 * Each message is sent in the format:
 * <pre>
 * patientId,timestamp,label,data
 * </pre>
 * Example:
 * <pre>
 * 5,1744113766180,HeartRate,85.0
 * </pre>
 */
public class WebSocketOutputStrategy implements OutputStrategy {

    private final WebSocketServer server;

    /**
     * Constructs a new WebSocketOutputStrategy and starts a WebSocket server on the specified port.
     *
     * @param port the port number to start the WebSocket server on
     */
    public WebSocketOutputStrategy(int port) {
        server = new SimpleWebSocketServer(new InetSocketAddress(port));
        System.out.println("WebSocket server created on port: " + port + ", listening for connections...");
        server.start();
    }

    /**
     * Formats the data and sends it to all connected WebSocket clients.
     *
     * @param patientId the ID of the patient
     * @param timestamp the timestamp of the data
     * @param label     the type of data
     * @param data      the actual data value
     */
    @Override
    public void output(int patientId, long timestamp, String label, String data) {
        String message;
        try {
            // Format the message
            message = String.format("%d,%d,%s,%s", patientId, timestamp, label, data);
        } catch (IllegalFormatException | NullPointerException ex) {
            // catch any formatting errors
            System.err.println("Error occurred while formatting message: " + ex.getMessage());
            ex.printStackTrace();
            return;
        }
        // send the message to all clients
        for (WebSocket conn : server.getConnections()) {
            try {
                // make sure the connection is open before sending
                if (conn.isOpen()) {
                    conn.send(message);
                } else {
                    System.err.println("Error: WebSocket connection is closed, skipping send.");
                }
            } catch (Exception ex) {
                System.err.println("Error occurred while broadcasting message to connection " + conn.getRemoteSocketAddress() + ": " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    /**
     * Returns the underlying WebSocket server.
     *
     * @return the active WebSocketServer instance
     */
    public WebSocketServer getServer() { //added for testing purposes
        return server;
    }

    /**
     * Inner class that represents a simple WebSocket server for broadcasting messages.
     * Handles basic events.
     */
    private static class SimpleWebSocketServer extends WebSocketServer {

        /**
         * Constructs the server on the given socket address.
         *
         * @param address the socket address to bind the server to
         */
        public SimpleWebSocketServer(InetSocketAddress address) {
            super(address);
        }

        /**
         * Called when a new client connection is established.
         *
         * @param conn      the new WebSocket connection
         * @param handshake the handshake data from the client
         */
        @Override
        public void onOpen(WebSocket conn, org.java_websocket.handshake.ClientHandshake handshake) {
            System.out.println("New connection: " + conn.getRemoteSocketAddress());
        }

        /**
         * Called when a client connection is closed.
         *
         * @param conn   the closed connection
         * @param code   the close code
         * @param reason the reason for closure
         * @param remote true if closed remotely
         */
        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            System.out.println("Closed connection: " + conn.getRemoteSocketAddress());
        }

        /**
         * Called when a message is received from a client. Not used in this context.
         *
         * @param conn    the client connection
         * @param message the received message
         */
        @Override
        public void onMessage(WebSocket conn, String message) {
            // Not used in this context
        }

        /**
         * Called when an error occurs on the server or a specific connection.
         *
         * @param conn the connection (can be null)
         * @param ex   the exception thrown
         */
        @Override
        public void onError(WebSocket conn, Exception ex) {
            ex.printStackTrace();
        }

        /**
         * Called when the server has successfully started.
         */
        @Override
        public void onStart() {
            System.out.println("Server started successfully");
        }
    }
}
