package id.ac.ui.cs.advprog.udehnihauth.service;

import java.util.Date;

public interface TokenBlacklistService {
    void addToBlacklist(String token, Date expiry);
    boolean isBlacklisted(String token);
    void cleanupExpiredTokens();
}