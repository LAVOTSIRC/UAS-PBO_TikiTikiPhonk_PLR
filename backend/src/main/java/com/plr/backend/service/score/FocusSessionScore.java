package com.plr.backend.service.score;

public class FocusSessionScore extends SessionScoreCalculator {
    @Override
    public int calculatePoints(int durationMinutes) {
        return durationMinutes * 10;
    }
}
