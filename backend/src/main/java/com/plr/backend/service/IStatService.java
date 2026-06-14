package com.plr.backend.service;

import com.plr.backend.dto.StatSummaryResponse;

public interface IStatService {
    StatSummaryResponse getSummary(Long userId);
}
