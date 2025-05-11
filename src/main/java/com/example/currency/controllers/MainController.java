package com.example.currency.controllers;

import com.example.currency.entities.Currency;
import com.example.currency.entities.CurrencyRate;
import com.example.currency.repo.CurrencyRateRepository;
import com.example.currency.repo.CurrencyRepository;
import com.example.currency.services.CurrencyRateLoaderService;
import com.jayway.jsonpath.spi.cache.CacheProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Currency Controller", description = "Контроллер для работы с валютами и курсами")
public class MainController {

    private final CurrencyRepository currencyRepository;
    private final CurrencyRateRepository currencyRateRepository;
    private final CurrencyRateLoaderService currencyRateLoaderService;

    public MainController(CurrencyRepository currencyRepository,
                          CurrencyRateRepository currencyRateRepository,
                          CurrencyRateLoaderService currencyRateLoaderService) {
        this.currencyRepository = currencyRepository;
        this.currencyRateRepository = currencyRateRepository;
        this.currencyRateLoaderService = currencyRateLoaderService;
    }

    @GetMapping("/currencies")
    @Operation(summary = "Список всех валют", description = "Возвращает все валюты, сохранённые в базе данных")
    public List<Currency> getAllCurrencies() {
        return currencyRepository.findAll();
    }

    @GetMapping("/rates")
    @Operation(summary = "Список всех курсов", description = "Возвращает все записи курсов валют из базы данных")
    public List<CurrencyRate> getAllRates() {
        return currencyRateRepository.findAll();
    }

    @GetMapping("/rates/by-date")
    @Operation(summary = "Курсы на дату", description = "Возвращает список курсов валют, сохранённых на указанную дату")
    public List<CurrencyRate> getRatesByDate(@RequestParam("date") String date) {
        return currencyRateRepository.findByDate(LocalDate.parse(date));
    }

    @GetMapping("/load-rates")
    @Operation(summary = "Загрузить курсы по дате", description = "Скачивает XML с сайта ЦБ РФ и сохраняет курсы в базу данных")
    public String loadRates(@RequestParam("date") String date) {
        currencyRateLoaderService.loadRatesByDate(LocalDate.parse(date));
        return "Загрузка завершена для даты: " + date;
    }

    @GetMapping("/currency-rate/{date}/{code}")
    @Operation(summary = "Курс валюты на дату", description = "Возвращает курс валюты по коду и дате. Использует кэш, если дата — текущая.")
    public CurrencyRate getCurrencyRateByDateAndCode(
            @PathVariable("date") String date,
            @PathVariable("code") String code) {
        return currencyRateLoaderService.getRateForDateAndCode(LocalDate.parse(date), code);
    }

    @PostMapping("/update-today-rates")
    @Operation(summary = "Обновить курсы на сегодня", description = "Очищает кэш и загружает актуальные курсы валют за сегодняшний день")
    public String updateTodayRates() {
        currencyRateLoaderService.updateTodayRates();
        return "Курсы валют за сегодня обновлены и кэш очищен.";
    }
}
