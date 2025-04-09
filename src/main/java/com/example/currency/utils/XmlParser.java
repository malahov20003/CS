package com.example.currency.utils;

import com.example.currency.dto.CurrencyCbrDailyDto;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import org.springframework.stereotype.Component;
import java.io.StringReader;

@Component
public class XmlParser {

    public XmlParser() {}

    public CurrencyCbrDailyDto parseCurrencyRates(String xml) throws Exception {
        JAXBContext context = JAXBContext.newInstance(CurrencyCbrDailyDto.class);
        Unmarshaller parse = context.createUnmarshaller();
        return (CurrencyCbrDailyDto) parse.unmarshal(new StringReader(xml));
    }
}
