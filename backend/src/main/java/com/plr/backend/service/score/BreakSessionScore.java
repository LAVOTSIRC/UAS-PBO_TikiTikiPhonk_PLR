package com.plr.backend.service.score;

public class BreakSessionScore extends SessionScoreCalculator {
    @Override
    public int calculatePoints(int durationMinutes) {
        return durationMinutes * 2;
    }
}
