package javaadventcalendar;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.guava.CaffeinatedGuava;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CaffeineGuavaTest {
    void sleep(long sleepSec) {
        try {
            TimeUnit.SECONDS.sleep(sleepSec);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    // stop-watch
    long sw(Runnable runnable) {
        long startTime = System.nanoTime();
        runnable.run();
        long elapsedTime = System.nanoTime() - startTime;

        return TimeUnit.SECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS);
    }

    @Test
    public void gettingStarted() throws ExecutionException {
        CacheLoader<String, String> loader = key -> {
            sleep(3L);
            return "value" + key.replace("key", "");
        };

        // Caffeineから、Guava Cacheを作成することができる
        com.google.common.cache.LoadingCache<String, String> cache =
                CaffeinatedGuava.build(
                        Caffeine
                                .newBuilder()
                                .expireAfterWrite(5L, TimeUnit.SECONDS),
                        loader
                );

        // 初回は低速
        assertThat(
                sw(() -> {
                    try {
                        assertThat(cache.get("key1")).isEqualTo("value1");
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                })
        ).isGreaterThanOrEqualTo(3L);

        // 2回目は高速
        assertThat(
                sw(() -> {
                    try {
                        assertThat(cache.get("key1")).isEqualTo("value1");
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                })
        ).isLessThan(1L);

        // 有効期限切れまで待つ
        sleep(5L);

        // 再度低速になる
        assertThat(
                sw(() -> {
                    try {
                        assertThat(cache.get("key1")).isEqualTo("value1");
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                })
        ).isGreaterThanOrEqualTo(3L);
    }
}
