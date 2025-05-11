package com.example.currency.services;

import com.example.currency.dto.CurrencyCbrDailyDto;
import com.example.currency.dto.CurrencyCbrDto;
import com.example.currency.entities.Currency;
import com.example.currency.entities.CurrencyRate;
import com.example.currency.repo.CurrencyRateRepository;
import com.example.currency.repo.CurrencyRepository;
import com.example.currency.utils.XmlParser;
import jakarta.transaction.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class CurrencyRateLoaderService {

    private final CbrClient cbrClient;
    private final XmlParser xmlParser;
    private final CurrencyRepository currencyRepository;
    private final CurrencyRateRepository currencyRateRepository;

    public CurrencyRateLoaderService(CbrClient cbrClient,
                                     XmlParser xmlParser,
                                     CurrencyRepository currencyRepository,
                                     CurrencyRateRepository currencyRateRepository) {
        this.cbrClient = cbrClient;
        this.xmlParser = xmlParser;
        this.currencyRepository = currencyRepository;
        this.currencyRateRepository = currencyRateRepository;
    }

    @Transactional
    public void loadRatesByDate(LocalDate date) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String xml = cbrClient.getRatesXml(date.format(formatter));

            CurrencyCbrDailyDto dailyDto = xmlParser.parseCurrencyRates(xml);

            for (CurrencyCbrDto dto : dailyDto.getCurrencies()) {
                Currency currency = currencyRepository.findByCode(dto.getCharCode())
                        .orElseGet(() -> {
                            Currency newCurrency = new Currency();
                            newCurrency.setCode(dto.getCharCode());
                            newCurrency.setName(dto.getName());
                            return currencyRepository.save(newCurrency);
                        });

                if (!currencyRateRepository.existsByCurrencyAndDate(currency, date)) {
                    saveCurrencyRate(currency, date, dto);// добавка
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Ошибка загрузки курсов валют: " + e.getMessage(), e);
        }
    }

    @Cacheable(value = "currencyRates", key = "#date.toString() + '_' + #code", condition = "#date.equals(T(java.time.LocalDate).now())")
    public CurrencyRate getRateForDateAndCode(LocalDate date, String code) {

        System.out.println(">>> [METHOD EXECUTED] This means that the result was not in the cache: " + code + " on date " + date);

        try {
            Currency currency = currencyRepository.findByCode(code)
                    .orElseThrow(() -> new RuntimeException("Currency not found: " + code));

            CurrencyRate existingRate = currencyRateRepository.findByCurrencyAndDate(currency, date);
            if (existingRate != null) {
                return existingRate;
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String xml = cbrClient.getRatesXml(date.format(formatter));
            CurrencyCbrDailyDto parsed = xmlParser.parseCurrencyRates(xml);

            CurrencyCbrDto dto = parsed.getCurrencies().stream()
                    .filter(c -> c.getCharCode().equals(code))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Currency not found in XML: " + code));

            saveCurrencyRate(currency, date, dto);
            return currencyRateRepository.findByCurrencyAndDate(currency, date);

        } catch (Exception e) {
            throw new RuntimeException("Ошибка получения курса валют: " + e.getMessage(), e);
        }
    }

    public void loadRatesForLastYear() {
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 365; i++) {
            loadRatesByDate(today.minusDays(i));
        }
    }

    @CacheEvict(value = "currencyRates", allEntries = true)
    @Transactional
    public void updateTodayRates() {
        LocalDate today = LocalDate.now();

        try {
            String xml = cbrClient.getRatesXml(today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            CurrencyCbrDailyDto parsed = xmlParser.parseCurrencyRates(xml);

            for (CurrencyCbrDto dto : parsed.getCurrencies()) {
                Currency currency = currencyRepository.findByCode(dto.getCharCode())
                        .orElseGet(() -> {
                            Currency newCurrency = new Currency();
                            newCurrency.setCode(dto.getCharCode());
                            newCurrency.setName(dto.getName());
                            return currencyRepository.save(newCurrency);
                        });

                CurrencyRate existingRate = currencyRateRepository.findByCurrencyAndDate(currency, today);
                if (existingRate == null) {
                    saveCurrencyRate(currency, today, dto);//длбавка
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Ошибка: Курсы за сегодня: " + e.getMessage(), e);
        }
    }

    // вынесли
    private void saveCurrencyRate(Currency currency, LocalDate date, CurrencyCbrDto dto) {
        BigDecimal rate = new BigDecimal(dto.getValue().replace(",", "."));
        CurrencyRate currencyRate = new CurrencyRate();
        currencyRate.setCurrency(currency);
        currencyRate.setDate(date);
        currencyRate.setNominal(dto.getNominal());
        currencyRate.setRate(rate);
        currencyRateRepository.save(currencyRate);
    }

    public List<CurrencyRate> loadRatesForDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        try {
            String xml = cbrClient.getRatesXml(date.format(formatter));
            CurrencyCbrDailyDto dailyDto = xmlParser.parseCurrencyRates(xml);
            return dailyDto.getCurrencies().stream().map(dto -> {
                Currency currency = currencyRepository.findByCode(dto.getCharCode())
                        .orElseGet(() -> {
                            Currency newCurrency = new Currency();
                            newCurrency.setCode(dto.getCharCode());
                            newCurrency.setName(dto.getName());
                            return currencyRepository.save(newCurrency);
                        });

                if (!currencyRateRepository.existsByCurrencyAndDate(currency, date)) {
                    saveCurrencyRate(currency, date, dto);
                }

                return currencyRateRepository.findByCurrencyAndDate(currency, date);
            }).toList();

        } catch (Exception e) {
            throw new RuntimeException("Ошибка загрузки курсов валют: " + e.getMessage(), e);
        }
    }

}
