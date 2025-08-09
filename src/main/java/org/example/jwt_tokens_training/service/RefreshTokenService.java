package org.example.jwt_tokens_training.service;

import org.example.jwt_tokens_training.model.RefreshToken;
import org.example.jwt_tokens_training.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    public void saveRefreshToken(String refreshToken, String username, long expiryDate) {
        RefreshToken token = new RefreshToken(refreshToken, username, expiryDate);
        refreshTokenRepository.save(token);
    }
}
