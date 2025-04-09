package com.example.currency.controllers;

import com.example.currency.dto.CurrencyDto;
import com.example.currency.dto.CurrencyRateResponseDto;
import com.example.currency.entities.CurrencyRate;
import com.example.currency.repo.CurrencyRateRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class MainController {

    private final CurrencyRateRepository currencyRateRepository;

    public MainController(CurrencyRateRepository currencyRateRepository) {
        this.currencyRateRepository = currencyRateRepository;
    }

    @GetMapping("/api/currency-rates/today")
    public List<CurrencyRateResponseDto> getTodayCurrencyRates() {
        return currencyRateRepository.findByDate(LocalDate.now())
                .stream()
                .map(rate -> new CurrencyRateResponseDto(
                        new CurrencyDto(
                                rate.getCurrency().getCode(),
                                rate.getCurrency().getName()
                        ),
                        rate.getRate(),
                        rate.getNominal(),
                        rate.getDate()
                ))
                .collect(Collectors.toList());
    }
}


