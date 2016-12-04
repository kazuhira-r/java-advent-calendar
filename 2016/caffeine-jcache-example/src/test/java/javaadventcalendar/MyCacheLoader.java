package javaadventcalendar;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheLoaderException;

public class MyCacheLoader implements CacheLoader<String, String> {
    @Override
    public String load(String key) throws CacheLoaderException {
        return "value" + key.replace("key", "");
    }

    @Override
    public Map<String, String> loadAll(Iterable<? extends String> keys) throws CacheLoaderException {
        return StreamSupport
                .stream(keys.spliterator(), false)
                .collect(Collectors.toMap(key -> key, key -> load(key)));
    }
}
