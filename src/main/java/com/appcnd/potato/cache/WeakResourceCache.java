package com.appcnd.potato.cache;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author nihao 2020/11/17
 */
public class WeakResourceCache<K,V> implements ResourceCache<K,V> {
    private Map<K,WeakReference<V>> cache = null;
    public WeakResourceCache() {
        cache = new ConcurrentHashMap<>();
    }

    @Override
    public V get(K k) {
        if (cache.containsKey(k)) {
            WeakReference<V> weakReference = cache.get(k);
            return weakReference.get();
        }
        return null;
    }

    @Override
    public void set(K k, V v) {
        cache.put(k, new WeakReference<>(v));
    }

    @Override
    public void remove(K k) {
        cache.remove(k);
    }
}
