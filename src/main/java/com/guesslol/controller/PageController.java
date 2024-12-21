package com.guesslol.controller;


import com.guesslol.dto.RoundDTO;
import com.guesslol.model.Round;
import com.guesslol.service.RoundService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
public class PageController {

    private final RoundService roundService;

    public PageController(RoundService roundService) {
        this.roundService = roundService;
    }

    @GetMapping
    public String index() {
        return "index";
    }

    @GetMapping("/room")
    public String roomPage(HttpSession session, Model model) {
        String roomName = (String) session.getAttribute("roomName");
        if (roomName == null) {
            return "redirect:/";
        }
        boolean isStarted = roundService.isRoundStarted(roomName);
        if (isStarted) {
            return "redirect:/started";
        }
        return "room";
    }

    @GetMapping("/started")
    public String startedPage() {
        return "started";
    }

}