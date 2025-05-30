/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package java.lang.reflect;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * 缓存映射对 {@code (key, sub-key) -> value}。键和值是弱引用的，但子键是强引用的。键直接传递给
 * {@link #get} 方法，该方法还接受一个 {@code parameter}。子键通过传递给构造函数的 {@code subKeyFactory} 函数
 * 从键和参数计算得出。值通过传递给构造函数的 {@code valueFactory} 函数从键和参数计算得出。
 * 键可以为 {@code null} 并按身份比较，而由 {@code subKeyFactory} 或 {@code valueFactory} 返回的子键或值
 * 不能为 null。子键使用其 {@link #equals} 方法进行比较。
 * 在每次调用 {@link #get}、{@link #containsValue} 或 {@link #size} 方法时，当键的弱引用被清除时，
 * 缓存中的条目会被惰性地清除。清除的值的弱引用不会导致清除，但这些条目在逻辑上被视为不存在，并在请求其
 * key/subKey 时触发 {@code valueFactory} 的重新评估。
 *
 * @author Peter Levart
 * @param <K> 键的类型
 * @param <P> 参数的类型
 * @param <V> 值的类型
 */
final class WeakCache<K, P, V> {

    private final ReferenceQueue<K> refQueue
        = new ReferenceQueue<>();
    // 键类型为 Object 以支持 null 键
    private final ConcurrentMap<Object, ConcurrentMap<Object, Supplier<V>>> map
        = new ConcurrentHashMap<>();
    private final ConcurrentMap<Supplier<V>, Boolean> reverseMap
        = new ConcurrentHashMap<>();
    private final BiFunction<K, P, ?> subKeyFactory;
    private final BiFunction<K, P, V> valueFactory;

    /**
     * 构造 {@code WeakCache} 的实例
     *
     * @param subKeyFactory 一个函数，映射一对
     *                      {@code (key, parameter) -> sub-key}
     * @param valueFactory  一个函数，映射一对
     *                      {@code (key, parameter) -> value}
     * @throws NullPointerException 如果 {@code subKeyFactory} 或
     *                              {@code valueFactory} 为 null。
     */
    public WeakCache(BiFunction<K, P, ?> subKeyFactory,
                     BiFunction<K, P, V> valueFactory) {
        this.subKeyFactory = Objects.requireNonNull(subKeyFactory);
        this.valueFactory = Objects.requireNonNull(valueFactory);
    }

    /**
     * 通过缓存查找值。这总是评估
     * {@code subKeyFactory} 函数，并在给定的 (key, subKey) 对没有条目或条目已被清除时，
     * 选择性地评估 {@code valueFactory} 函数。
     *
     * @param key       可能为 null 的键
     * @param parameter 与键一起使用的参数（不应为 null）
     * @return 缓存的值（从不为 null）
     * @throws NullPointerException 如果传递的 {@code parameter} 或
     *                              由 {@code subKeyFactory} 计算的 {@code sub-key} 或
     *                              由 {@code valueFactory} 计算的 {@code value} 为 null。
     */
    public V get(K key, P parameter) {
        Objects.requireNonNull(parameter);

        expungeStaleEntries();

        Object cacheKey = CacheKey.valueOf(key, refQueue);

        // 惰性安装特定 cacheKey 的 2 级 valuesMap
        ConcurrentMap<Object, Supplier<V>> valuesMap = map.get(cacheKey);
        if (valuesMap == null) {
            ConcurrentMap<Object, Supplier<V>> oldValuesMap
                = map.putIfAbsent(cacheKey,
                                  valuesMap = new ConcurrentHashMap<>());
            if (oldValuesMap != null) {
                valuesMap = oldValuesMap;
            }
        }

        // 创建 subKey 并从 valuesMap 中检索可能存储的 Supplier<V>
        Object subKey = Objects.requireNonNull(subKeyFactory.apply(key, parameter));
        Supplier<V> supplier = valuesMap.get(subKey);
        Factory factory = null;

        while (true) {
            if (supplier != null) {
                // supplier 可能是一个 Factory 或 CacheValue<V> 实例
                V value = supplier.get();
                if (value != null) {
                    return value;
                }
            }
            // 否则缓存中没有 supplier
            // 或者 supplier 返回 null（可能是已清除的 CacheValue
            // 或未成功安装 CacheValue 的 Factory）

            // 惰性构造一个 Factory
            if (factory == null) {
                factory = new Factory(key, parameter, subKey, valuesMap);
            }

            if (supplier == null) {
                supplier = valuesMap.putIfAbsent(subKey, factory);
                if (supplier == null) {
                    // 成功安装 Factory
                    supplier = factory;
                }
                // 否则使用获胜的 supplier 重试
            } else {
                if (valuesMap.replace(subKey, supplier, factory)) {
                    // 成功替换
                    // 已清除的 CacheEntry / 未成功的 Factory
                    // 用我们的 Factory 替换
                    supplier = factory;
                } else {
                    // 使用当前 supplier 重试
                    supplier = valuesMap.get(subKey);
                }
            }
        }
    }

    /**
     * 检查指定的非 null 值是否已存在于此
     * {@code WeakCache} 中。检查使用身份比较，无论值的类是否重写了 {@link Object#equals}。
     *
     * @param value 要检查的非 null 值
     * @return 如果给定的 {@code value} 已被缓存，则返回 true
     * @throws NullPointerException 如果值为 null
     */
    public boolean containsValue(V value) {
        Objects.requireNonNull(value);

        expungeStaleEntries();
        return reverseMap.containsKey(new LookupValue<>(value));
    }

    /**
     * 返回当前缓存的条目数，该数量可能会随着时间的推移而减少，当键/值被垃圾回收时。
     */
    public int size() {
        expungeStaleEntries();
        return reverseMap.size();
    }

    private void expungeStaleEntries() {
        CacheKey<K> cacheKey;
        while ((cacheKey = (CacheKey<K>)refQueue.poll()) != null) {
            cacheKey.expungeFrom(map, reverseMap);
        }
    }

    /**
     * 实现值的懒惰同步构造和安装到缓存中的工厂 {@link Supplier}。
     */
    private final class Factory implements Supplier<V> {

        private final K key;
        private final P parameter;
        private final Object subKey;
        private final ConcurrentMap<Object, Supplier<V>> valuesMap;

        Factory(K key, P parameter, Object subKey,
                ConcurrentMap<Object, Supplier<V>> valuesMap) {
            this.key = key;
            this.parameter = parameter;
            this.subKey = subKey;
            this.valuesMap = valuesMap;
        }

        @Override
        public synchronized V get() { // 串行访问
            // 重新检查
            Supplier<V> supplier = valuesMap.get(subKey);
            if (supplier != this) {
                // 在我们等待期间发生了变化：
                // 可能我们被 CacheValue 替换
                // 或因失败而被移除 ->
                // 返回 null 以信号 WeakCache.get() 重试
                // 循环
                return null;
            }
            // 否则仍然是我们 (supplier == this)

            // 创建新值
            V value = null;
            try {
                value = Objects.requireNonNull(valueFactory.apply(key, parameter));
            } finally {
                if (value == null) { // 失败时移除我们
                    valuesMap.remove(subKey, this);
                }
            }
            // 唯一到达这里的路径是非 null 值
            assert value != null;

            // 用 CacheValue (WeakReference) 包装值
            CacheValue<V> cacheValue = new CacheValue<>(value);

            // 放入 reverseMap
            reverseMap.put(cacheValue, Boolean.TRUE);

            // 尝试用 CacheValue 替换我们（这应该总是成功）
            if (!valuesMap.replace(subKey, this, cacheValue)) {
                throw new AssertionError("不应到达这里");
            }

            // 成功用新的 CacheValue 替换我们 -> 返回它包装的值
            return value;
        }
    }

    /**
     * 持有引用的值供应商的公共类型。
     * 实现的 {@link #equals} 和 {@link #hashCode} 定义为按身份比较引用。
     */
    private interface Value<V> extends Supplier<V> {}

    /**
     * 用于在 {@link WeakCache#containsValue} 方法中查找值的优化 {@link Value}，
     * 以便我们不必为了查找引用而构造整个 {@link CacheValue}。
     */
    private static final class LookupValue<V> implements Value<V> {
        private final V value;

        LookupValue(V value) {
            this.value = value;
        }

        @Override
        public V get() {
            return value;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(value); // 按身份比较
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this ||
                   obj instanceof Value &&
                   this.value == ((Value<?>) obj).get();  // 按身份比较
        }
    }

    /**
     * 弱引用引用的 {@link Value}。
     */
    private static final class CacheValue<V>
        extends WeakReference<V> implements Value<V>
    {
        private final int hash;

        CacheValue(V value) {
            super(value);
            this.hash = System.identityHashCode(value); // 按身份比较
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            V value;
            return obj == this ||
                   obj instanceof Value &&
                   // 已清除的 CacheValue 仅等于自身
                   (value = get()) != null &&
                   value == ((Value<?>) obj).get(); // 按身份比较
        }
    }

    /**
     * 包含弱引用 {@code key} 的 CacheKey。它注册
     * 自身到 {@code refQueue}，以便在 {@link WeakReference} 被清除时可以用于清除条目。
     */
    private static final class CacheKey<K> extends WeakReference<K> {

        // null 键的替代
        private static final Object NULL_KEY = new Object();

        static <K> Object valueOf(K key, ReferenceQueue<K> refQueue) {
            return key == null
                   // null 键意味着我们不能弱引用它，
                   // 所以我们使用 NULL_KEY 单例作为缓存键
                   ? NULL_KEY
                   // 非 null 键需要包装为 WeakReference
                   : new CacheKey<>(key, refQueue);
        }

        private final int hash;

        private CacheKey(K key, ReferenceQueue<K> refQueue) {
            super(key, refQueue);
            this.hash = System.identityHashCode(key);  // 按身份比较
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            K key;
            return obj == this ||
                   obj != null &&
                   obj.getClass() == this.getClass() &&
                   // 已清除的 CacheKey 仅等于自身
                   (key = this.get()) != null &&
                   // 按身份比较键
                   key == ((CacheKey<K>) obj).get();
        }

        void expungeFrom(ConcurrentMap<?, ? extends ConcurrentMap<?, ?>> map,
                         ConcurrentMap<?, Boolean> reverseMap) {
            // 仅通过键移除总是安全的，因为 CacheKey
            // 被清除并入队后，它只等于自身
            // （参见 equals 方法）...
            ConcurrentMap<?, ?> valuesMap = map.remove(this);
            // 如果需要，也从 reverseMap 中移除
            if (valuesMap != null) {
                for (Object cacheValue : valuesMap.values()) {
                    reverseMap.remove(cacheValue);
                }
            }
        }
    }
}
