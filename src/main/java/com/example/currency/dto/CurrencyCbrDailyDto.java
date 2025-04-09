package com.example.currency.dto;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlAttribute;
import java.util.List;

@XmlRootElement(name = "ValCurs")
public class CurrencyCbrDailyDto {

    private String date;
    private List<CurrencyCbrDto> currencies;

    public CurrencyCbrDailyDto() {}

    @XmlAttribute(name = "Date")
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @XmlElement(name = "Valute")
    public List<CurrencyCbrDto> getCurrencies() {
        return currencies;
    }

    public void setCurrencies(List<CurrencyCbrDto> currencies) {
        this.currencies = currencies;
    }
}

