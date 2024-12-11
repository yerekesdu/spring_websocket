package kz.bitlab.middle03.websocket.chat;

import org.springframework.web.socket.WebSocketSession;

public class ConnectedUser {

    private String username;
    private WebSocketSession webSocketSession;

    public String getUsername() {
        return username;
    }

    public ConnectedUser(String username, WebSocketSession webSocketSession) {
        this.username = username;
        this.webSocketSession = webSocketSession;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public WebSocketSession getWebSocketSession() {
        return webSocketSession;
    }

    public void setWebSocketSession(WebSocketSession webSocketSession) {
        this.webSocketSession = webSocketSession;
    }
}
