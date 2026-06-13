package com.plr.backend.service.score;

public class LongBreakSessionScore extends SessionScoreCalculator {
    @Override
    public int calculatePoints(int durationMinutes) {
        return durationMinutes * 3;
    }
}
