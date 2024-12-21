package com.guesslol.exceptions;

public class PlayerAlreadyInRoomException extends RuntimeException {
    public PlayerAlreadyInRoomException(String message) {
        super(message);
    }
}
