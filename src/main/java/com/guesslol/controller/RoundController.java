package com.guesslol.controller;

import com.guesslol.dto.RoundDTO;
import com.guesslol.model.Player;
import com.guesslol.service.RoundService;
import com.guesslol.request.RoomRequest;
import com.guesslol.response.ApiResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/round")
public class RoundController {

    private final RoundService roundService;

    public RoundController(RoundService roundService) {
        this.roundService = roundService;
    }
    @GetMapping("/all")
    public ResponseEntity<ApiResponse> allRooms(){
        List<RoundDTO> rounds = roundService.getAllRoundsWithNames();
        ApiResponse response = new ApiResponse("Rooms obtained successfully", rounds);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/players/{roomName}")
    public ResponseEntity<ApiResponse> getAllPlayers(@PathVariable String roomName){
        List<Player> players = roundService.getAllPlayers(roomName);
        ApiResponse response = new ApiResponse("Players obtained successfully", players);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/createRoom")
    public ResponseEntity<ApiResponse> createRoom(@Valid @RequestBody RoomRequest request,HttpSession session) {
        roundService.createRoom(request.getUsername(), request.getRoomName());
        session.setAttribute("username", request.getUsername());
        session.setAttribute("roomName", request.getRoomName());
        ApiResponse response = new ApiResponse("Room created successfully", null);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/join")
    public ResponseEntity<ApiResponse> joinRoom(@RequestBody RoomRequest request, HttpSession session) {
        roundService.joinRoom(new Player(request.getUsername()), request.getRoomName());
        session.setAttribute("username", request.getUsername());
        session.setAttribute("roomName", request.getRoomName());
        return ResponseEntity.ok(new ApiResponse("Joined room successfully", null));
    }

    @PostMapping("/terminate/{roomName}")
    public ResponseEntity<ApiResponse> terminateRound(@PathVariable String roomName) {
        roundService.terminateRound(roomName);
        ApiResponse response = new ApiResponse("Room terminated successfully", null);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/advance")
    public ResponseEntity<ApiResponse> validateAnswer(@Valid @RequestBody RoomRequest request) {
        roundService.advanceChamp(request.getRoomName(), request.getUsername());
        ApiResponse response = new ApiResponse("Champion advanced successfully", null);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/start/{roomName}")
    public ResponseEntity<ApiResponse> startRound(@PathVariable String roomName) {
        roundService.startAndSendRound(roomName);
        ApiResponse response = new ApiResponse("Round started successfully", null);
        return ResponseEntity.ok(response);
    }

}