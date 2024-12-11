package kz.bitlab.middle03.websocket.config;

import kz.bitlab.middle03.websocket.handler.ChatWebSocketHandler;
import kz.bitlab.middle03.websocket.handler.WebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final WebSocketHandler webSocketHandler;
    private final ChatWebSocketHandler chatWebSocketHandler;

    public WebSocketConfig(WebSocketHandler webSocketHandler, ChatWebSocketHandler chatWebSocketHandler) {
        this.webSocketHandler = webSocketHandler;
        this.chatWebSocketHandler = chatWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler, "/chat");
        registry.addHandler(chatWebSocketHandler, "/messenger");
    }
}
