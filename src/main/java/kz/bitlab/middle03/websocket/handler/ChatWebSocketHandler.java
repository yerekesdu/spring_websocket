package kz.bitlab.middle03.websocket.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.bitlab.middle03.websocket.chat.ConnectedUser;
import kz.bitlab.middle03.websocket.dto.ChatCustomMessage;
import kz.bitlab.middle03.websocket.service.ChatMessageService;
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

@Component
public class ChatWebSocketHandler extends AbstractWebSocketHandler{

    private final ChatMessageService chatMessageService;
    private final Map<String, ConnectedUser> sessions = new ConcurrentHashMap<>();
    private final Map<String, String> usernameSessonIdMap = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatWebSocketHandler.class);

    public ChatWebSocketHandler(ChatMessageService chatMessageService, ObjectMapper objectMapper) {
        this.chatMessageService = chatMessageService;
        this.objectMapper = objectMapper;
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
            sessions.put(session.getId(), new ConnectedUser(username, session));
            usernameSessonIdMap.put(username, session.getId());
        } else {
            session.sendMessage(new TextMessage("Error on joining"));
            session.close();
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        String payload = message.getPayload().toString();
        ChatCustomMessage chatCustomMessage = parseMessage(payload);

        if (chatCustomMessage != null) {
            handleChatMessage(session, chatCustomMessage);
        }
    }

    private ChatCustomMessage parseMessage(String payload) {
        try {
            return objectMapper.readValue(payload, ChatCustomMessage.class);
        } catch (JsonProcessingException e) {
            LOGGER.error("some when parsing JSON to CustomMessage: {}", String.valueOf(e));
            return null;
        }
    }

    private void handleChatMessage(WebSocketSession session, ChatCustomMessage customMessage) {

        String sender = sessions.get(session.getId()).getUsername();
        String receiver = customMessage.getReceiver();

        chatMessageService.saveMessage(sender, receiver, customMessage.getMessage());

        String sessionId = usernameSessonIdMap.get(receiver);

        if (sessionId != null) {
            ConnectedUser connectedUser = sessions.get(sessionId);
            if (connectedUser != null) {
                try {
                    connectedUser.getWebSocketSession().sendMessage(new TextMessage(customMessage.getMessage()));
                } catch (IOException e) {
                    LOGGER.error(e.getMessage());
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session.getId());
    }
}
