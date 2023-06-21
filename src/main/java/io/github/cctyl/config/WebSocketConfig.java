package io.github.cctyl.config;

import io.github.cctyl.ws.VchatWebSocketClient;
import org.java_websocket.client.WebSocketClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.net.URISyntaxException;

@Configuration
public class WebSocketConfig {

    @Value("${ws.url}")
    private String serverUrl;

    @Bean("vchat")
    public WebSocketClient vchatWebSocketClient() throws URISyntaxException {
        WebSocketClient client = new VchatWebSocketClient(serverUrl);
        client.connect();
        return client;
    }
}
