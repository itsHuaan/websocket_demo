package com.example.websocket_demo.controller;

import com.example.websocket_demo.dto.request.score.UserScoreRequest;
import com.example.websocket_demo.service.score.UserScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/scores")
@RequiredArgsConstructor
public class UserScoreController {

    private final UserScoreService userScoreService;

    @PostMapping
    public ResponseEntity<?> submitScore(@RequestBody UserScoreRequest request) {
        return ResponseEntity.ok(userScoreService.saveScore(request));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllScores() {
        return ResponseEntity.ok(userScoreService.getAllScores());
    }
}
