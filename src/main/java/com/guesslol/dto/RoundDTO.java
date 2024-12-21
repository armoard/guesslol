package com.guesslol.dto;

import com.guesslol.model.Round;

public class RoundDTO {
    private String name;
    private int players;


    public RoundDTO(String name, int players) {
        this.name = name;
        this.players = players;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPlayers(int players) {
        this.players = players;
    }

    public String getName() {
        return name;
    }

    public int getPlayers() {
        return players;
    }
}