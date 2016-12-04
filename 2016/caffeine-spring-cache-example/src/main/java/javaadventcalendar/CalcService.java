package javaadventcalendar;

import java.util.concurrent.TimeUnit;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@CacheConfig(cacheNames = "calcCache")
@Service
public class CalcService {
    @Cacheable
    public int add(int a, int b) {
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            // ignore
        }

        return a + b;
    }
}
