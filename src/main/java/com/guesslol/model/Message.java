package com.guesslol.model;

public class Message {
    private String from;
    private String text;


    public Message() {}

    public Message(String from, String text) {
        this.from = from;
        this.text = text;
    }

    // Getters y setters
    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}