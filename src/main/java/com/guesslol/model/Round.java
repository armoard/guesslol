package com.guesslol.model;

import java.util.ArrayList;
import java.util.List;


public class Round {
    private List<Champion> champions = new ArrayList<>();
    private List<Player> players = new ArrayList<>();
    public boolean state;

    public Round() {
        this.champions = new ArrayList<>();
        this.players = new ArrayList<>();
        this.state = false;
    }

    public List<Champion> getChampions() {
        return champions;
    }


    public List<Player> getPlayers() {
        return players;
    }

    public boolean getState(){
        return state;
    }

    public void setChampions(List<Champion> champions) {
        this.champions = champions;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

}