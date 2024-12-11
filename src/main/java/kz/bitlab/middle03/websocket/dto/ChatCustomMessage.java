package kz.bitlab.middle03.websocket.dto;

public class ChatCustomMessage {

    private String message;
    private String receiver;

    public ChatCustomMessage() {
    }

    public ChatCustomMessage(String message, String receiver) {}

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }
}
