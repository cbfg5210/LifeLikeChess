package com.ue.gobang.ai;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Simple extension of LinkedHashMap which keeps elements in access order.
 * Elements are removed after the capacity is exceeded.
 *
 * @param <K> Key type
 * @param <V> Value type
 */
public class Cache<K, V> extends LinkedHashMap<K, V> {
    private final int maxEntries;

    public Cache(final int maxEntries) {
        super(maxEntries + 1, 1.0f, true);
        //accessOrder-false:按照插入顺序进行迭代；true:以访问顺序进行迭代。
        this.maxEntries = maxEntries;
    }

    /**
     * Removes the least used entry after the capacity is exceeded
     *
     * @param eldest Eldest entry
     * @return
     */
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return super.size() > maxEntries;
    }
}