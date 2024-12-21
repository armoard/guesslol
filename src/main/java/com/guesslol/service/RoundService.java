package com.guesslol.service;


import com.guesslol.dto.RoundDTO;
import com.guesslol.model.Player;
import com.guesslol.model.Round;
import com.guesslol.model.WebSocketMessage;
import com.guesslol.exceptions.PlayerAlreadyInRoomException;
import com.guesslol.exceptions.RoomAlreadyExistsException;
import com.guesslol.exceptions.RoomNotFoundException;
import com.guesslol.exceptions.RoundAlreadyStartedException;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate;


import java.util.*;
import java.util.stream.Collectors;

@Service
public class RoundService {
    private final ChampionService championService;
    private final Map<String, Round> rounds = new HashMap<>();
    private final SimpMessagingTemplate messagingTemplate;


    public RoundService(ChampionService championService, SimpMessagingTemplate messagingTemplate) {
        this.championService = championService;
        this.messagingTemplate = messagingTemplate;
    }

    // only returns not started rounds
    public List<RoundDTO> getAllRoundsWithNames() {
        return rounds.entrySet()
                .stream()
                .filter(entry -> !entry.getValue().getState())
                .map(entry -> {
                    String name = entry.getKey();
                    Round round = entry.getValue();
                    int players = round.getPlayers().size();
                    return new RoundDTO(name, players);
                })
                .collect(Collectors.toList());
    }


    public void createRoom(String username, String roomName) {
        if (roundExists(roomName)) {
            throw new RoomAlreadyExistsException("The room '" + roomName + "' already exists.");
        }
        Round round = new Round();
        round.setState(false);
        round.getPlayers().add(new Player(username));
        rounds.put(roomName, round);

    }

    public void joinRoom(Player player, String roundName) {
        if (player == null || roundName == null || roundName.isEmpty()) {
            throw new IllegalArgumentException("Player or room name cannot be null or empty");
        }

        if (!roundExists(roundName)) {
            throw new RoomNotFoundException("The room '" + roundName + "' does not exist.");
        }

        Round targetRound = rounds.get(roundName);

        if (targetRound.getState()){
            throw new RoundAlreadyStartedException("The round is already started! Wait and try again.");
        }

        if (!targetRound.getPlayers().contains(player)) {
            targetRound.getPlayers().add(player);

            WebSocketMessage message = new WebSocketMessage("user_joined", player.getUsername(), roundName,null);

            messagingTemplate.convertAndSend("/topic/chat/" + roundName, message);
        } else {
            throw new PlayerAlreadyInRoomException("This username is already in the room!");
        }
    }


    public void startAndSendRound(String roomName) {
        Round round = rounds.get(roomName);

        if (round == null) {
            throw new RoomNotFoundException("Room not found: " + roomName);
        }
        //notify round start
        WebSocketMessage startingMessage = new WebSocketMessage("round_starting", null, roomName, "The round is starting...");
        messagingTemplate.convertAndSend("/topic/chat/" + roomName, startingMessage);


        round.setState(true);
        round.getPlayers().forEach(player -> player.setScore(0));
        round.setChampions(championService.generateRandomChampionsWithImages());

        //send round info
        WebSocketMessage roundMessage= new WebSocketMessage("round_info", null, roomName, null);
        roundMessage.setAdditionalData("round", round);

        messagingTemplate.convertAndSend("/topic/chat/" + roomName, roundMessage);
    }


    public void removeUser(String username, String roomName) {
        Round round = rounds.get(roomName);
        if (round == null) {
            throw new IllegalArgumentException("Room not found: " + roomName);
        }

        round.getPlayers().removeIf(player -> player.getUsername().equals(username));
        List<String> currentPlayers = round.getPlayers().stream()
                .map(Player::getUsername)
                .collect(Collectors.toList());

        WebSocketMessage message = new WebSocketMessage("user_left", username, roomName,null);
        message.setAdditionalData("players", currentPlayers);

        messagingTemplate.convertAndSend("/topic/chat/" + roomName, message);
        removeRoomIfLastUser(roomName);
    }

    public void removeRoomIfLastUser(String roomName) {
        Round round = rounds.get(roomName);
        if (round != null && round.getPlayers().isEmpty()) {
            rounds.remove(roomName);
        }
    }

    public void advanceChamp(String roomName, String username) {
        Round round = rounds.get(roomName);

        if (round == null) {
            throw new RoomNotFoundException("The room '" + roomName + "' does not exist.");
        }

        if (!round.getState()) {
            throw new IllegalStateException("The round has not been started yet.");
        }

        Player player = round.getPlayers().stream()
                .filter(p -> p.getUsername().equals(username))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("User not found in the room"));

        player.incrementScore();

        WebSocketMessage message = new WebSocketMessage("advance", username, roomName, null);

        messagingTemplate.convertAndSend("/topic/chat/" + roomName, message);
    }


    public void terminateRound(String roomName) {
        Round round = rounds.get(roomName);

        if (round == null) {
            throw new RoomNotFoundException("The round '" + roomName + "' does not exist.");
        }
        if (round.getState()) {
            Player winner = round.getPlayers().stream()
                    .max(Comparator.comparingInt(Player::getScore))
                    .orElse(null);

            String winnerMessage = (winner != null)
                    ? "Winner: " + winner.getUsername() + " with " + winner.getScore() + " points!"
                    : "No winner in this round.";

            round.setState(false);
            round.getChampions().clear();

            // notify winner
            WebSocketMessage message = new WebSocketMessage("round_terminated", null, roomName, winnerMessage);
            messagingTemplate.convertAndSend("/topic/chat/" + roomName, message);
        }
    }


    public boolean roundExists(String roomName) {
        if (roomName == null || roomName.isEmpty()) {
            throw new IllegalArgumentException("Room name cannot be null or empty");
        }
        return rounds.containsKey(roomName);
    }
    public boolean isRoundStarted(String roomName){
        return rounds.get(roomName).getState();
    }
    public List<Player> getAllPlayers(String roomName){
        Round round = rounds.get(roomName);
        return round.getPlayers();
    }

}