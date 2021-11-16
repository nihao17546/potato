package cn.thecover.potato.cache;

import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author nihao 2020/11/17
 */
public class SoftResourceCache<K,V> implements ResourceCache<K,V> {
    private Map<K,SoftReference<V>> cache = null;
    public SoftResourceCache() {
        cache = new ConcurrentHashMap<>();
    }

    @Override
    public V get(K k) {
        if (cache.containsKey(k)) {
            SoftReference<V> softReference = cache.get(k);
            return softReference.get();
        }
        return null;
    }

    @Override
    public void set(K k, V v) {
        cache.put(k, new SoftReference<>(v));
    }

    @Override
    public void remove(K k) {
        cache.remove(k);
    }
}
