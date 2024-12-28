package model;

import java.sql.Timestamp;

// ChatMessage class to hold the chat message details
public class ChatMessage {
    private String username;
    private String message;
    private Timestamp timestamp;

    public ChatMessage(String username, String message, Timestamp timestamp) {
        this.username = username;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}
