package io.github.cctyl.config;

import io.github.cctyl.ws.VchatWebSocketClient;
import org.java_websocket.client.WebSocketClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.net.URISyntaxException;

@Configuration
public class WebSocketConfig {


    @Autowired
    private ApplicationProperties applicationProperties;

    @Bean("vchat")
    public WebSocketClient vchatWebSocketClient() throws URISyntaxException {
        WebSocketClient client = new VchatWebSocketClient(applicationProperties.getWs().getUrl());
        client.connect();
        return client;
    }
}
