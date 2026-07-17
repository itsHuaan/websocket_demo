package com.example.websocket_demo.service.score.impl;

import com.example.websocket_demo.common.DataUtil;
import com.example.websocket_demo.dto.request.score.UserScoreRequest;
import com.example.websocket_demo.entity.UserScoreEntity;
import com.example.websocket_demo.repository.UserScoreRepository;
import com.example.websocket_demo.service.score.UserScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserScoreServiceImpl implements UserScoreService {

    private final UserScoreRepository userScoreRepository;

    @Override
    @Transactional
    public UserScoreEntity saveScore(UserScoreRequest request) {
        String usernameSnakeCase = DataUtil.toSnakeCase(request.getName()).toLowerCase();

        UserScoreEntity newScore = new UserScoreEntity();
        newScore.setUsername(usernameSnakeCase);
        newScore.setCorrectAnswers(request.getCorrectAnswers());
        newScore.setIncorrectAnswers(request.getIncorrectAnswers());
        newScore.setSubmitDate(request.getSubmitDate());
        newScore.setEditDate(request.getEditDate());
        return userScoreRepository.save(newScore);
    }

    @Override
    public java.util.List<UserScoreEntity> getAllScores() {
        return userScoreRepository.findAll();
    }
}
