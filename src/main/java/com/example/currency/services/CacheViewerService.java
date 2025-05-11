package com.example.currency.services;

import com.example.currency.entities.CurrencyRate;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CacheViewerService {

    private final CacheManager cacheManager;

    public CacheViewerService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public Map<String, Object> getCurrencyRatesCache() {
        Cache cache = cacheManager.getCache("currencyRates");
        if (cache == null) return Collections.singletonMap("error", "Cache 'currencyRates' not found");

        Map<String, Object> result = new HashMap<>();
        // Spring's default cache doesn't expose all entries directly. This will work if using a custom cache (like Caffeine, EhCache, etc.)
        result.put("note", "Default cache doesn't expose all keys unless you're using a concurrent map based cache.");
        result.put("hint", "Consider using Caffeine or ConcurrentMapCache to enable introspection.");

        return result;
    }

    public Map<String, CurrencyRate> getCurrencyRatesCacheContent() {
        Map<String, CurrencyRate> result = new HashMap<>();
        Cache cache = cacheManager.getCache("currencyRates");

        if (cache instanceof ConcurrentMapCache mapCache) {
            mapCache.getNativeCache().forEach((key, value) -> {
                if (value instanceof CurrencyRate rate) {
                    result.put((String) key, rate);
                }
            });
        }

        return result;
    }

}
