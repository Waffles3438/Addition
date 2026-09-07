package me.waffles.additional.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class EldestRemovalMap<K, V> extends LinkedHashMap<K, V> {
    private final int MAX_SIZE;

    public EldestRemovalMap(int maxSize) {
        super(maxSize + 1, 1.0f, true); // true = access order (LRU behavior)
        this.MAX_SIZE = maxSize;
    }

    @Override
    public synchronized V get(Object key) {
        return super.get(key);
    }

    @Override
    public synchronized boolean containsKey(Object key) {
        return super.containsKey(key);
    }

    @Override
    public synchronized V put(K key, V value) {
        return super.put(key, value);
    }

    @Override
    public synchronized V remove(Object key) {
        return super.remove(key);
    }

    @Override
    public synchronized void clear() {
        super.clear();
    }

    @Override
    public synchronized int size() {
        return super.size();
    }

    @Override
    protected synchronized boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > MAX_SIZE; // Remove oldest if size exceeds MAX_SIZE
    }
}
