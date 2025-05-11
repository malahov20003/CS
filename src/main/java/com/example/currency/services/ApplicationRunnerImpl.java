package com.example.currency.services;

import com.example.currency.entities.CurrencyRate;
import com.example.currency.repo.CurrencyRateRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class ApplicationRunnerImpl implements CommandLineRunner {

    private final CurrencyRateLoaderService loaderService;
    private final CurrencyRateRepository rateRepository;
    private final CacheManager cacheManager;

    public ApplicationRunnerImpl(CurrencyRateLoaderService loaderService,
                                 CurrencyRateRepository rateRepository,
                                 CacheManager cacheManager) {
        this.loaderService = loaderService;
        this.rateRepository = rateRepository;
        this.cacheManager = cacheManager;
    }

    @Override
    public void run(String... args) {
        LocalDate today = LocalDate.now();
        List<CurrencyRate> todayRates = rateRepository.findByDate(today);
        if (todayRates.isEmpty()) {
            System.out.println("Данные за сегодня не найдены. Загружаю из ЦБ...");
            todayRates = loaderService.loadRatesForDate(today);
            rateRepository.saveAll(todayRates);
        } else {
            System.out.println("Данные за сегодня найдены в БД.");
        }
        if (cacheManager.getCache("currencyRates") != null) {
            todayRates.forEach(rate -> cacheManager.getCache("currencyRates").put(rate.getCurrency().getCode(), rate));
            System.out.println("Кэш успешно обновлён.");
        } else {
            System.err.println("Кэш currencyRates не найден!");
        }
    }
}
