package com.guesslol.request;


import jakarta.validation.constraints.NotBlank;

public class RoomRequest {
    @NotBlank(message = "Username cannot be blank")
    private String username;

    @NotBlank(message = "Room name cannot be blank")
    private String roomName;

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
}