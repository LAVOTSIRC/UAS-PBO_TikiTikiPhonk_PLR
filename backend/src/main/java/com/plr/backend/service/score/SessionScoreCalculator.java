package com.plr.backend.service.score;

public abstract class SessionScoreCalculator {
    public abstract int calculatePoints(int durationMinutes);
}
