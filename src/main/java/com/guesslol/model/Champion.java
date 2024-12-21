package com.guesslol.model;

import java.util.Map;

public class Champion {
    private String name;
    private String championImage;
    private Map<String, String> spellImages;

    public Champion(String name, String championImage, Map<String, String> spellImages) {
        this.name = name;
        this.championImage = championImage;
        this.spellImages = spellImages;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getChampionImage() {
        return championImage;
    }

    public void setChampionImage(String championImage) {
        this.championImage = championImage;
    }

    public Map<String, String> getSpellImages() {
        return spellImages;
    }

    public void setSpellImages(Map<String, String> spellImages) {
        this.spellImages = spellImages;
    }
}