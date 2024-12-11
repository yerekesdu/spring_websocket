package kz.bitlab.middle03.websocket.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.bitlab.middle03.websocket.chat.ConnectedUser;
import kz.bitlab.middle03.websocket.dto.ChatCustomMessage;
import kz.bitlab.middle03.websocket.dto.CustomMessage;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@Slf4j
public class WebSocketHandler extends AbstractWebSocketHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketHandler.class);
    private final Map<String, ConnectedUser> connectedUserSessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public WebSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        String response = connectedUserSessions.get(session.getId()).getUsername() + " : " + message.getPayload();
        sendMessageToChat(response);

    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String username = null;
        String query = Objects.requireNonNull(session.getUri()).getQuery();
        if (query != null) {
            String[] queryParams = query.split("&");
            for(String param : queryParams) {
                String[] keyValue = param.split("=");
                if (keyValue[0].equalsIgnoreCase("userName") && keyValue.length > 1) {
                    username = keyValue[1];
                    break;
                }
            }
        }
        if (username != null) {
            connectedUserSessions.put(session.getId(), new ConnectedUser(username, session));
            sendMessageToChat("User : " + username + " joined the chat");
        } else {
            session.sendMessage(new TextMessage("Error on joining"));
            session.close();
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        ConnectedUser connectedUser = connectedUserSessions.remove(session.getId());
        if (connectedUser != null) {
            sendMessageToChat("User : " + connectedUser.getUsername() + " left the chat");
        }
    }

    private void sendMessageToChat(String message) {
        for(ConnectedUser connectedUser : connectedUserSessions.values()) {
            try {
                connectedUser.getWebSocketSession().sendMessage(new TextMessage(message));
            } catch (Exception e) {
                LOGGER.error("some error happened: {}", String.valueOf(e));
            }
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        String payload = message.getPayload().toString();
        CustomMessage customMessage = parseCustomMessage(payload);

        if (customMessage != null) {
           switch (customMessage.getType()) {
               case "CHAT" -> handleChatMessage(session, customMessage);
               case "COMMAND" -> handleCommand(session, customMessage);
           }
        }
    }

    private void handleChatMessage(WebSocketSession session, CustomMessage message) {
        String userName = connectedUserSessions.get(session.getId()).getUsername();
        sendMessageToChat(userName + " : " + message.getContent());
    }

    private void handleCommand(WebSocketSession session, CustomMessage message) {
        switch (message.getContent()) {
            case "disconnect" -> {
                try {
                    session.close();
                } catch (IOException e) {
                    LOGGER.error("Couldn't disconnect: {}", String.valueOf(e));
                }
            }
            case "list_users" -> {
                String activeUsers = connectedUserSessions
                    .values()
                    .stream()
                    .map(ConnectedUser::getUsername)
                    .collect(Collectors.joining(", "));
                try {
                    session.sendMessage(new TextMessage("Active users: " + activeUsers));
                } catch (IOException e) {
                    LOGGER.error("Error on sending message: {}", String.valueOf(e));
                }
            }
        }
    }

    private CustomMessage parseCustomMessage(String payload) {
        try {
            return objectMapper.readValue(payload, CustomMessage.class);
        } catch (JsonProcessingException e) {
            LOGGER.error("some when parsing JSON to CustomMessage: {}", String.valueOf(e));
            return null;
        }
    }
}
