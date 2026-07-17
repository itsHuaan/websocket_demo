package com.example.websocket_demo.service.score;

import com.example.websocket_demo.dto.request.score.UserScoreRequest;
import com.example.websocket_demo.entity.UserScoreEntity;

public interface UserScoreService {
    UserScoreEntity saveScore(UserScoreRequest request);
    java.util.List<UserScoreEntity> getAllScores();
}
