package javaadventcalendar;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.IntStream;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CaffeineTest {
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
    public void gettingStarted() {
        // キャッシュの作成
        Cache<String, String> cache =
                Caffeine
                        .newBuilder()
                        .build();

        // 登録
        cache.put("key1", "value1");
        cache.put("key2", "value2");

        // 取得
        assertThat(cache.getIfPresent("key1"))
                .isEqualTo("value1");
        assertThat(cache.getIfPresent("key2"))
                .isEqualTo("value2");

        // エントリ数
        assertThat(cache.estimatedSize())
                .isEqualTo(2L);

        // 登録していないキーに対しては、nullが返る
        assertThat(cache.getIfPresent("missing-key"))
                .isNull();

        // 登録していないキーに対する呼び出しに対して、Functionを実行することもできる
        assertThat(cache.get("key3", key -> "value" + key.replace("key", "")))
                .isEqualTo("value3");

        // 遅いFunction
        Function<String, String> slowEntryLoader = key -> {
            sleep(3L);
            return "value" + key.replace("key", "");
        };

        // 1回目は低速
        assertThat(
                sw(() -> assertThat(cache.get("key4", slowEntryLoader)).isEqualTo("value4"))
        ).isGreaterThanOrEqualTo(3L);

        // 2回目は高速
        assertThat(
                sw(() -> assertThat(cache.get("key4", slowEntryLoader)).isEqualTo("value4"))
        ).isLessThan(1L);

        // エントリの削除
        cache.invalidate("key1");
        assertThat(cache.getIfPresent("key1"))
                .isNull();

        // エントリの全削除
        cache.invalidateAll();
        assertThat(cache.estimatedSize())
                .isZero();
    }

    @Test
    public void loadingCache() {
        // 低速なエントリロード用のCacheLoader
        CacheLoader<String, String> slowLoader = key -> {
            sleep(3L);
            return "value" + key.replace("key", "");
        };

        // CacheLoaderを使用して、LoadingCacheを作成
        LoadingCache<String, String> cache =
                Caffeine
                        .newBuilder()
                        .build(slowLoader);

        // 登録していないキーに対しても、いきなり値を取得できる
        assertThat(cache.get("key1"))
                .isEqualTo("value1");

        // 呼び出しは、1回目は低速（CacheLoaderが低速なので）
        assertThat(
                sw(() -> assertThat(cache.get("key2")))
        ).isGreaterThanOrEqualTo(3L);

        // 2回目は、ロード済みなので高速
        assertThat(
                sw(() -> assertThat(cache.get("key2")))
        ).isLessThan(1L);
    }

    @Test
    public void expire() {
        // アクセス後の有効期限を5秒に設定したCacheを作成
        Cache<String, String> cache =
                Caffeine
                        .newBuilder()
                        .expireAfterAccess(5L, TimeUnit.SECONDS)
                        .build();

        // エントリを2つ登録
        cache.put("key1", "value1");
        cache.put("key2", "value2");

        sleep(3L);

        // 3秒後、片方にアクセス
        cache.getIfPresent("key1");

        sleep(3L);

        // 3秒後の時点でアクセスした方は、エントリが残っている
        assertThat(cache.getIfPresent("key1"))
                .isEqualTo("value1");
        // アクセスしなかった方は、エントリが消えている
        assertThat(cache.getIfPresent("key2"))
                .isNull();

        sleep(5L);

        // 5秒後、すべてのエントリがクリアされている
        // ※Cache#estimatedSizeはexpireをすぐには反映しないので、Cache#cleanUpを呼び出している
        cache.cleanUp();
        assertThat(cache.estimatedSize())
                .isZero();
    }

    @Test
    public void caffeineSpec() {
        Cache<String, String> cache =
                Caffeine.from("maximumSize=10,expireAfterAccess=5s").build();

        IntStream.rangeClosed(1, 20).forEach(i -> cache.put("key" + i, "value" + i));

        cache.cleanUp();
        assertThat(cache.estimatedSize())
                .isEqualTo(10L);

        cache.invalidateAll();

        cache.put("key1", "value1");
        cache.put("key2", "value2");

        sleep(3L);

        cache.getIfPresent("key1");

        sleep(3L);

        assertThat(cache.getIfPresent("key1"))
                .isEqualTo("value1");
        assertThat(cache.getIfPresent("key2"))
                .isNull();
    }
}
