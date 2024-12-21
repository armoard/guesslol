package com.guesslol.model;

import java.util.HashMap;
import java.util.Map;

public class WebSocketMessage {
    private String type; // (e.g., "chat_message", "user_joined")
    private String username;
    private String roomName;
    private String content; // only chat messages
    private Map<String, Object> additionalData = new HashMap<>();


    public WebSocketMessage() {}

    public WebSocketMessage(String type, String username, String roomName, String content) {
        this.type = type;
        this.username = username;
        this.roomName = roomName;
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Map<String, Object> getAdditionalData() {
        return additionalData;
    }

    public void setAdditionalData(String key, Object value) {
        this.additionalData.put(key, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WebSocketMessage that = (WebSocketMessage) o;

        if (!type.equals(that.type)) return false;
        if (!username.equals(that.username)) return false;
        return roomName.equals(that.roomName);
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + username.hashCode();
        result = 31 * result + roomName.hashCode();
        return result;
    }
}