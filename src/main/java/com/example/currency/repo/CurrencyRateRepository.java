package com.example.currency.repo;

import com.example.currency.entities.Currency;
import com.example.currency.entities.CurrencyRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CurrencyRateRepository extends JpaRepository<CurrencyRate, Long> {

    List<CurrencyRate> findByDate(LocalDate date);

    boolean existsByCurrencyAndDate(Currency currency, LocalDate date);

    CurrencyRate findByCurrencyAndDate(Currency currency, LocalDate date);

}
