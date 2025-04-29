package id.ac.ui.cs.advprog.udehnihauth.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private final Map<String, Date> blacklistedTokens = new ConcurrentHashMap<>();

    @Override
    public void addToBlacklist(String token, Date expiry) {
        blacklistedTokens.put(token, expiry);
    }

    @Override
    public boolean isBlacklisted(String token) {
        return blacklistedTokens.containsKey(token);
    }

    @Override
    @Scheduled(fixedRate = 3600000)
    public void cleanupExpiredTokens() {
        Date now = new Date();
        blacklistedTokens.entrySet().removeIf(entry -> entry.getValue().before(now));
    }
}