package com.example.currency.services;

import com.example.currency.dto.CurrencyCbrDailyDto;
import com.example.currency.dto.CurrencyCbrDto;
import com.example.currency.entities.Currency;
import com.example.currency.entities.CurrencyRate;
import com.example.currency.repo.CurrencyRateRepository;
import com.example.currency.repo.CurrencyRepository;
import com.example.currency.utils.XmlParser;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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

                BigDecimal rate = new BigDecimal(dto.getValue().replace(",", "."));

                CurrencyRate currencyRate = new CurrencyRate();
                currencyRate.setCurrency(currency);
                currencyRate.setDate(date);
                currencyRate.setNominal(dto.getNominal());
                currencyRate.setRate(rate);

                currencyRateRepository.save(currencyRate);
            }
        } catch (Exception e) {
            throw new RuntimeException("Ошибка загрузки курсов валют: " + e.getMessage(), e);
        }
    }
}
