package javaadventcalendar;

import java.util.Arrays;
import java.util.HashSet;
import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.CompleteConfiguration;
import javax.cache.configuration.Configuration;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;

import org.assertj.core.data.MapEntry;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CaffeineJCacheTest {
    @Test
    public void gettingStarted() {
        Configuration<String, String> configuration =
                new MutableConfiguration<String, String>()
                        .setTypes(String.class, String.class);

        try (CachingProvider provider = Caching.getCachingProvider();
             CacheManager manager = provider.getCacheManager();
             Cache<String, String> cache = manager.createCache("caffeineCache", configuration)) {

            cache.put("key1", "value1");
            assertThat(cache.get("key1"))
                    .isEqualTo("value1");
        }
    }

    @Test
    public void useDefaultCache() {
        try (CachingProvider provider = Caching.getCachingProvider();
             CacheManager manager = provider.getCacheManager();
             // いきなり「default」という名前のCacheが使える
             Cache<String, String> defaultCache = manager.getCache("default")) {

            defaultCache.put("key1", "value1");
            assertThat(defaultCache.get("key1"))
                    .isEqualTo("value1");
        }
    }

    @Test
    public void useDefinedCache() {
        try (CachingProvider provider = Caching.getCachingProvider();
             CacheManager manager = provider.getCacheManager();
             // application.confで定義したCache
             Cache<String, String> definedCache = manager.getCache("definedCaffeineCache")) {

            // CacheLoaderが適用されていることが確認できる
            definedCache.put("key1", "value1");
            assertThat(definedCache.get("key1"))
                    .isEqualTo("value1");

            assertThat(definedCache.get("key2"))
                    .isEqualTo("value2");
            assertThat(definedCache.getAll(new HashSet<>(Arrays.asList("key3", "key4"))))
                    .containsExactly(MapEntry.entry("key3", "value3"), MapEntry.entry("key4", "value4"));

            // application.confで設定した内容が確認できる
            CompleteConfiguration<String, String> definedConfiguration =
                    definedCache.getConfiguration(CompleteConfiguration.class);
            assertThat(definedConfiguration.isStoreByValue())
                    .isTrue();
            assertThat(definedConfiguration.isReadThrough())
                    .isTrue();
        }
    }
}
