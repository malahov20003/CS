package com.example.currency.repo;

import com.example.currency.entities.CurrencyRate;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CurrencyRateRepository extends JpaRepository<CurrencyRate, Long> {
    List<CurrencyRate> findByDate(LocalDate date);
    Optional<CurrencyRate> findByCurrency_CodeAndDate(String code, LocalDate date);
}

