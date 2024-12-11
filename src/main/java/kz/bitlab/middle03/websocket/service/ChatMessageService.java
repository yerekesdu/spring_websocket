package kz.bitlab.middle03.websocket.service;

import kz.bitlab.middle03.websocket.model.ChatMessage;
import kz.bitlab.middle03.websocket.repository.ChatMessageRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;

    public ChatMessageService(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    public void saveMessage(String sender, String receiver, String content) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSender(sender);
        chatMessage.setReceiver(receiver);
        chatMessage.setContent(content);
        chatMessage.setSentTime(LocalDateTime.now());

        chatMessageRepository.save(chatMessage);
    }
}
