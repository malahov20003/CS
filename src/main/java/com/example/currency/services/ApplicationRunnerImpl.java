package com.example.currency.services;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
public class ApplicationRunnerImpl implements CommandLineRunner {

    private final CurrencyRateLoaderService loaderService;

    public ApplicationRunnerImpl(CurrencyRateLoaderService loaderService) {
        this.loaderService = loaderService;
    }

    @Override
    public void run(String... args) {
        loaderService.loadRatesByDate(LocalDate.now());
    }
}
