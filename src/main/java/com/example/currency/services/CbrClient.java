package com.example.currency.services;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class CbrClient {

    private final HttpClient client;

    public CbrClient() {
        this.client = HttpClient.newHttpClient();
    }

    public String getRatesXml(String date) throws IOException, InterruptedException {
        String url = "https://www.cbr.ru/scripts/XML_daily.asp?date_req=" + date;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Ошибка получения данных: код " + response.statusCode());
        }

        return response.body();
    }
}

