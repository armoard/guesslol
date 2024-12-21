package com.guesslol.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.guesslol.model.Champion;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

@Service
public class ChampionService {
    public final List<String> championsIDs = new ArrayList<>();

    @PostConstruct
    public void init() {
        getChampionsNames();
    }

    public void getChampionsNames() {
        if (!championsIDs.isEmpty()) {
            return;
        }

        String apiUrl = "https://ddragon.leagueoflegends.com/cdn/14.19.1/data/en_US/champion.json";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode dataNode = new ObjectMapper().readTree(response.body()).get("data");
                dataNode.fieldNames().forEachRemaining(championsIDs::add);
            } else {
                System.out.println("Error: " + response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Champion> generateRandomChampionsWithImages() {
        Collections.shuffle(championsIDs);
        List<String> randomChampions = championsIDs.subList(0, 10);

        List<Champion> champions = new ArrayList<>();
        for (String championId : randomChampions) {
            Champion champion = fetchChampionData(championId);
            if (champion != null) champions.add(champion);
        }

        return champions;
    }

    private Champion fetchChampionData(String championId) {
        String apiUrl = "https://ddragon.leagueoflegends.com/cdn/14.19.1/data/en_US/champion/" + championId + ".json";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .GET()
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode championData = new ObjectMapper().readTree(response.body()).get("data").get(championId);
                if (championData != null) {
                    return createChampion(championId, championData);
                }
            } else {
                System.out.println("Error fetching champion data: " + response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Champion createChampion(String championId, JsonNode championData) {
        String championImageUrl = "https://ddragon.leagueoflegends.com/cdn/14.19.1/img/champion/" + championId + ".png";

        Map<String, String> spellImages = new LinkedHashMap<>();
        String[] spellKeys = {"Q", "W", "E", "R"};

        JsonNode spells = championData.get("spells");
        if (spells != null && spells.isArray()) {
            for (int i = 0; i < Math.min(spells.size(), 4); i++) {
                String spellId = spells.get(i).get("id").asText();
                spellImages.put(spellKeys[i], "https://ddragon.leagueoflegends.com/cdn/14.19.1/img/spell/" + spellId + ".png");
            }
        }

        return new Champion(championId, championImageUrl, spellImages);
    }
}