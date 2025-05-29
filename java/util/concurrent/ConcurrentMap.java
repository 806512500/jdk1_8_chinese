/*
 * ORACLE 专有/机密。使用受许可条款约束。
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

/*
 *
 *
 *
 *
 *
 * 由 Doug Lea 在 JCP JSR-166 专家小组成员的帮助下编写，并按照
 * http://creativecommons.org/publicdomain/zero/1.0/ 的说明发布到公共领域。
 */

package java.util.concurrent;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * 一个提供线程安全和原子性保证的 {@link java.util.Map}。
 *
 * <p>内存一致性效果：与其他并发集合一样，线程中在将对象作为键或值放入 {@code ConcurrentMap} 之前的操作
 * <a href="package-summary.html#MemoryVisibility"><i>先于</i></a>
 * 在另一个线程中访问或移除该对象后的操作。
 *
 * <p>此接口是
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java 集合框架</a>的成员。
 *
 * @since 1.5
 * @author Doug Lea
 * @param <K> 该映射维护的键的类型
 * @param <V> 映射值的类型
 */
public interface ConcurrentMap<K, V> extends Map<K, V> {

    /**
     * {@inheritDoc}
     *
     * @implNote 此实现假设 ConcurrentMap 不能包含 null 值，且 {@code get()} 返回 null 无疑意味着
     * 键不存在。支持 null 值的实现
     * <strong>必须</strong> 覆盖此默认实现。
     *
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     * @since 1.8
     */
    @Override
    default V getOrDefault(Object key, V defaultValue) {
        V v;
        return ((v = get(key)) != null) ? v : defaultValue;
    }

   /**
     * {@inheritDoc}
     *
     * @implSpec 默认实现等效于，对于此
     * {@code map}：
     * <pre> {@code
     * for ((Map.Entry<K, V> entry : map.entrySet())
     *     action.accept(entry.getKey(), entry.getValue());
     * }</pre>
     *
     * @implNote 默认实现假设
     * {@code getKey()} 或 {@code getValue()} 抛出的 {@code IllegalStateException} 表示该条目已被移除，无法处理。
     * 操作将继续处理后续条目。
     *
     * @throws NullPointerException {@inheritDoc}
     * @since 1.8
     */
    @Override
    default void forEach(BiConsumer<? super K, ? super V> action) {
        Objects.requireNonNull(action);
        for (Map.Entry<K, V> entry : entrySet()) {
            K k;
            V v;
            try {
                k = entry.getKey();
                v = entry.getValue();
            } catch(IllegalStateException ise) {
                // 这通常意味着条目已不再存在于映射中。
                continue;
            }
            action.accept(k, v);
        }
    }

    /**
     * 如果指定的键尚未与值关联，则将其与给定值关联。
     * 这等效于
     *  <pre> {@code
     * if (!map.containsKey(key))
     *   return map.put(key, value);
     * else
     *   return map.get(key);
     * }</pre>
     *
     * 除了操作是原子执行的。
     *
     * @implNote 此实现有意重新抽象化 {@code Map} 中提供的不合适的默认实现。
     *
     * @param key 要与指定值关联的键
     * @param value 要与指定键关联的值
     * @return 与指定键关联的前一个值，或者如果键没有映射，则返回
     *         {@code null}。
     *         （返回 {@code null} 也可以表示映射之前将 {@code null} 与键关联，
     *         如果实现支持 null 值的话。）
     * @throws UnsupportedOperationException 如果此映射不支持 {@code put} 操作
     * @throws ClassCastException 如果指定键或值的类阻止其存储在映射中
     * @throws NullPointerException 如果指定键或值为 null，且此映射不允许 null 键或值
     * @throws IllegalArgumentException 如果指定键或值的某些属性阻止其存储在映射中
     */
     V putIfAbsent(K key, V value);

    /**
     * 仅当键当前映射到给定值时，才移除该键的条目。
     * 这等效于
     *  <pre> {@code
     * if (map.containsKey(key) && Objects.equals(map.get(key), value)) {
     *   map.remove(key);
     *   return true;
     * } else
     *   return false;
     * }</pre>
     *
     * 除了操作是原子执行的。
     *
     * @implNote 此实现有意重新抽象化 {@code Map} 中提供的不合适的默认实现。
     *
     * @param key 与指定值关联的键
     * @param value 期望与指定键关联的值
     * @return 如果值被移除，则返回 {@code true}
     * @throws UnsupportedOperationException 如果此映射不支持 {@code remove} 操作
     * @throws ClassCastException 如果键或值的类型对于此映射不合适
     *         (<a href="../Collection.html#optional-restrictions">可选</a>)
     * @throws NullPointerException 如果指定键或值为 null，且此映射不允许 null 键或值
     *         (<a href="../Collection.html#optional-restrictions">可选</a>)
     */
    boolean remove(Object key, Object value);

    /**
     * 仅当键当前映射到给定值时，才替换该键的条目。
     * 这等效于
     *  <pre> {@code
     * if (map.containsKey(key) && Objects.equals(map.get(key), oldValue)) {
     *   map.put(key, newValue);
     *   return true;
     * } else
     *   return false;
     * }</pre>
     *
     * 除了操作是原子执行的。
     *
     * @implNote 此实现有意重新抽象化 {@code Map} 中提供的不合适的默认实现。
     *
     * @param key 与指定值关联的键
     * @param oldValue 期望与指定键关联的旧值
     * @param newValue 要与指定键关联的新值
     * @return 如果值被替换，则返回 {@code true}
     * @throws UnsupportedOperationException 如果此映射不支持 {@code put} 操作
     * @throws ClassCastException 如果指定键或值的类阻止其存储在映射中
     * @throws NullPointerException 如果指定键或值为 null，且此映射不允许 null 键或值
     * @throws IllegalArgumentException 如果指定键或值的某些属性阻止其存储在映射中
     */
    boolean replace(K key, V oldValue, V newValue);


    /**
     * 仅当键当前映射到某个值时，替换该键的条目。
     * 这等同于
     *  <pre> {@code
     * if (map.containsKey(key)) {
     *   return map.put(key, value);
     * } else
     *   return null;
     * }</pre>
     *
     * 除了操作是原子执行的。
     *
     * @implNote 该实现有意重新抽象化了 {@code Map} 中提供的不合适的默认实现。
     *
     * @param key 与指定值关联的键
     * @param value 要与指定键关联的值
     * @return 与指定键关联的前一个值，如果键没有映射，则返回 {@code null}。
     *         （返回 {@code null} 也可能表示该映射之前将 {@code null} 与键关联，
     *         如果实现支持 null 值的话。）
     * @throws UnsupportedOperationException 如果此映射不支持 {@code put} 操作
     * @throws ClassCastException 如果指定键或值的类阻止其存储在此映射中
     * @throws NullPointerException 如果指定的键或值为 null，而此映射不允许 null 键或值
     * @throws IllegalArgumentException 如果指定键或值的某些属性阻止其存储在此映射中
     */
    V replace(K key, V value);

    /**
     * {@inheritDoc}
     *
     * @implSpec
     * <p>默认实现等同于，对于此 {@code map}：
     * <pre> {@code
     * for ((Map.Entry<K, V> entry : map.entrySet())
     *     do {
     *        K k = entry.getKey();
     *        V v = entry.getValue();
     *     } while(!replace(k, v, function.apply(k, v)));
     * }</pre>
     *
     * 当多个线程尝试更新时，默认实现可能会重试这些步骤，包括可能多次调用该函数。
     *
     * <p>此实现假设 ConcurrentMap 不能包含 null 值，且 {@code get()} 返回 null 明确表示键不存在。支持 null 值的实现 <strong>必须</strong> 覆盖此默认实现。
     *
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     * @throws ClassCastException {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     * @since 1.8
     */
    @Override
    default void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        Objects.requireNonNull(function);
        forEach((k,v) -> {
            while(!replace(k, v, function.apply(k, v))) {
                // v changed or k is gone
                if ( (v = get(k)) == null) {
                    // k is no longer in the map.
                    break;
                }
            }
        });
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec
     * 默认实现等同于对 {@code map} 执行以下步骤，然后返回当前值或如果现在不存在则返回 {@code null}：
     *
     * <pre> {@code
     * if (map.get(key) == null) {
     *     V newValue = mappingFunction.apply(key);
     *     if (newValue != null)
     *         return map.putIfAbsent(key, newValue);
     * }
     * }</pre>
     *
     * 当多个线程尝试更新时，默认实现可能会重试这些步骤，包括可能多次调用映射函数。
     *
     * <p>此实现假设 ConcurrentMap 不能包含 null 值，且 {@code get()} 返回 null 明确表示键不存在。支持 null 值的实现 <strong>必须</strong> 覆盖此默认实现。
     *
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     * @since 1.8
     */
    @Override
    default V computeIfAbsent(K key,
            Function<? super K, ? extends V> mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        V v, newValue;
        return ((v = get(key)) == null &&
                (newValue = mappingFunction.apply(key)) != null &&
                (v = putIfAbsent(key, newValue)) == null) ? newValue : v;
    }

    /**
     * {@inheritDoc}
     *
     * @implSpec
     * 默认实现等同于对 {@code map} 执行以下步骤，然后返回当前值或如果现在不存在则返回 {@code null}：
     *
     * <pre> {@code
     * if (map.get(key) != null) {
     *     V oldValue = map.get(key);
     *     V newValue = remappingFunction.apply(key, oldValue);
     *     if (newValue != null)
     *         map.replace(key, oldValue, newValue);
     *     else
     *         map.remove(key, oldValue);
     * }
     * }</pre>
     *
     * 当多个线程尝试更新时，默认实现可能会重试这些步骤，包括可能多次调用重映射函数。
     *
     * <p>此实现假设 ConcurrentMap 不能包含 null 值，且 {@code get()} 返回 null 明确表示键不存在。支持 null 值的实现 <strong>必须</strong> 覆盖此默认实现。
     *
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     * @since 1.8
     */
    @Override
    default V computeIfPresent(K key,
            BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        V oldValue;
        while((oldValue = get(key)) != null) {
            V newValue = remappingFunction.apply(key, oldValue);
            if (newValue != null) {
                if (replace(key, oldValue, newValue))
                    return newValue;
            } else if (remove(key, oldValue))
               return null;
        }
        return oldValue;
    }


    /**
     * {@inheritDoc}
     *
     * @implSpec
     * 默认实现等同于对这个 {@code map} 执行以下步骤，然后返回当前值或如果不存在则返回 {@code null}：
     *
     * <pre> {@code
     * V oldValue = map.get(key);
     * V newValue = remappingFunction.apply(key, oldValue);
     * if (oldValue != null ) {
     *    if (newValue != null)
     *       map.replace(key, oldValue, newValue);
     *    else
     *       map.remove(key, oldValue);
     * } else {
     *    if (newValue != null)
     *       map.putIfAbsent(key, newValue);
     *    else
     *       return null;
     * }
     * }</pre>
     *
     * 当多个线程尝试更新时，包括可能多次调用重映射函数，默认实现可能会重试这些步骤。
     *
     * <p>此实现假设 ConcurrentMap 不能包含 null 值，并且 {@code get()} 返回 null 毫无歧义地表示键不存在。支持 null 值的实现 <strong>必须</strong> 覆盖此默认实现。
     *
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     * @since 1.8
     */
    @Override
    default V compute(K key,
            BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        V oldValue = get(key);
        for(;;) {
            V newValue = remappingFunction.apply(key, oldValue);
            if (newValue == null) {
                // 删除映射
                if (oldValue != null || containsKey(key)) {
                    // 有东西需要删除
                    if (remove(key, oldValue)) {
                        // 如预期删除了旧值
                        return null;
                    }

                    // 其他值替换了旧值。重试。
                    oldValue = get(key);
                } else {
                    // 没有需要做的。保持原样。
                    return null;
                }
            } else {
                // 添加或替换旧映射
                if (oldValue != null) {
                    // 替换
                    if (replace(key, oldValue, newValue)) {
                        // 如预期替换了。
                        return newValue;
                    }

                    // 其他值替换了旧值。重试。
                    oldValue = get(key);
                } else {
                    // 添加（如果 oldValue 为 null 则替换）
                    if ((oldValue = putIfAbsent(key, newValue)) == null) {
                        // 替换
                        return newValue;
                    }

                    // 其他值替换了旧值。重试。
                }
            }
        }
    }


    /**
     * {@inheritDoc}
     *
     * @implSpec
     * 默认实现等同于对这个 {@code map} 执行以下步骤，然后返回当前值或如果不存在则返回 {@code null}：
     *
     * <pre> {@code
     * V oldValue = map.get(key);
     * V newValue = (oldValue == null) ? value :
     *              remappingFunction.apply(oldValue, value);
     * if (newValue == null)
     *     map.remove(key);
     * else
     *     map.put(key, newValue);
     * }</pre>
     *
     * <p>当多个线程尝试更新时，包括可能多次调用重映射函数，默认实现可能会重试这些步骤。
     *
     * <p>此实现假设 ConcurrentMap 不能包含 null 值，并且 {@code get()} 返回 null 毫无歧义地表示键不存在。支持 null 值的实现 <strong>必须</strong> 覆盖此默认实现。
     *
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     * @since 1.8
     */
    @Override
    default V merge(K key, V value,
            BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        Objects.requireNonNull(value);
        V oldValue = get(key);
        for (;;) {
            if (oldValue != null) {
                V newValue = remappingFunction.apply(oldValue, value);
                if (newValue != null) {
                    if (replace(key, oldValue, newValue))
                        return newValue;
                } else if (remove(key, oldValue)) {
                    return null;
                }
                oldValue = get(key);
            } else {
                if ((oldValue = putIfAbsent(key, value)) == null) {
                    return value;
                }
            }
        }
    }

}
