package com.plr.backend.controller;

import com.plr.backend.dto.StatSummaryResponse;
import com.plr.backend.service.IStatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
public class StatController {

    @Autowired
    private IStatService statService;

    @GetMapping
    public StatSummaryResponse getStats(Authentication authentication) {
        return statService.getSummary(authentication.getName());
    }
}
