package io.github.cctyl.ws;

import lombok.extern.slf4j.Slf4j;
import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

@Slf4j
public class VchatWebSocketClient extends WebSocketClient {
    public VchatWebSocketClient(String serverUri) throws URISyntaxException {
        super(new URI(serverUri));
    }
    @Override
    public void onOpen(ServerHandshake handshakedata) {
        System.out.println("Connected to server");
    }

    @Override
    public void onMessage( String message) {
        System.out.println("Received message: " + message);
        // 在这里处理接收到的消息逻辑
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Connection closed with code " + code);
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("An error occurred: " + ex.getMessage());
    }

}