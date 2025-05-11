package com.example.currency.controllers;

import com.example.currency.entities.CurrencyRate;
import com.example.currency.services.CacheViewerService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cache")
public class CacheController {

    private final CacheViewerService cacheViewerService;

    public CacheController(CacheViewerService cacheViewerService) {
        this.cacheViewerService = cacheViewerService;
    }

    @GetMapping("/currency-rates/details")
    @Operation(summary = "Показать содержимое кэша")
    public Map<String, CurrencyRate> getCacheDetails() {
        return cacheViewerService.getCurrencyRatesCacheContent();
    }
}

