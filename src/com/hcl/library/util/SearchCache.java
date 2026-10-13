package com.hcl.library.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** LRU cache holding the last 5 searched titles. */
public class SearchCache {
    private static final int CAPACITY = 5;
    private static final float LOAD_FACTOR = 0.75f;

    private final Map<String, String> cache = new LinkedHashMap<>(16, LOAD_FACTOR, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
            return size() > CAPACITY;
        }
    };

    public void record(String searchTerm) {
        String key = searchTerm.trim().toLowerCase();
        cache.put(key, searchTerm.trim());
    }

    /** Least recently used first, most recent last. */
    public List<String> getRecent() {
        return new ArrayList<>(cache.values());
    }
}
