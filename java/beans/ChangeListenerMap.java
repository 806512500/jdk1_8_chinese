/*
 * Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved.
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
package java.beans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;
import java.util.EventListenerProxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * 这是一个抽象类，为 {@link PropertyChangeSupport PropertyChangeSupport} 类
 * 和 {@link VetoableChangeSupport VetoableChangeSupport} 类提供基础功能。
 *
 * @see PropertyChangeListenerMap
 * @see VetoableChangeListenerMap
 *
 * @author Sergey A. Malenkov
 */
abstract class ChangeListenerMap<L extends EventListener> {
    private Map<String, L[]> map;

    /**
     * 创建一个监听器数组。
     * 当 {@code length} 等于 {@code 0} 时，可以通过使用同一个空数组实例来优化此方法。
     *
     * @param length  数组长度
     * @return        指定长度的数组
     */
    protected abstract L[] newArray(int length);

    /**
     * 为指定属性创建一个代理监听器。
     *
     * @param name      要监听的属性名称
     * @param listener  处理事件的监听器
     * @return          代理监听器
     */
    protected abstract L newProxy(String name, L listener);

    /**
     * 将监听器添加到指定属性的监听器列表中。
     * 此监听器将被调用与其添加次数相同次数。
     *
     * @param name      要监听的属性名称
     * @param listener  处理事件的监听器
     */
    public final synchronized void add(String name, L listener) {
        if (this.map == null) {
            this.map = new HashMap<>();
        }
        L[] array = this.map.get(name);
        int size = (array != null)
                ? array.length
                : 0;

        L[] clone = newArray(size + 1);
        clone[size] = listener;
        if (array != null) {
            System.arraycopy(array, 0, clone, 0, size);
        }
        this.map.put(name, clone);
    }

    /**
     * 从指定属性的监听器列表中移除一个监听器。
     * 如果监听器被添加到同一个事件源多次，则移除后该监听器将少被通知一次。
     *
     * @param name      要监听的属性名称
     * @param listener  处理事件的监听器
     */
    public final synchronized void remove(String name, L listener) {
        if (this.map != null) {
            L[] array = this.map.get(name);
            if (array != null) {
                for (int i = 0; i < array.length; i++) {
                    if (listener.equals(array[i])) {
                        int size = array.length - 1;
                        if (size > 0) {
                            L[] clone = newArray(size);
                            System.arraycopy(array, 0, clone, 0, i);
                            System.arraycopy(array, i + 1, clone, i, size - i);
                            this.map.put(name, clone);
                        }
                        else {
                            this.map.remove(name);
                            if (this.map.isEmpty()) {
                                this.map = null;
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    /**
     * 返回指定属性的监听器列表。
     *
     * @param name  属性名称
     * @return      对应的监听器列表
     */
    public final synchronized L[] get(String name) {
        return (this.map != null)
                ? this.map.get(name)
                : null;
    }

    /**
     * 设置指定属性的新监听器列表。
     *
     * @param name       属性名称
     * @param listeners  新的监听器列表
     */
    public final void set(String name, L[] listeners) {
        if (listeners != null) {
            if (this.map == null) {
                this.map = new HashMap<>();
            }
            this.map.put(name, listeners);
        }
        else if (this.map != null) {
            this.map.remove(name);
            if (this.map.isEmpty()) {
                this.map = null;
            }
        }
    }

    /**
     * 返回映射中的所有监听器。
     *
     * @return 所有监听器的数组
     */
    public final synchronized L[] getListeners() {
        if (this.map == null) {
            return newArray(0);
        }
        List<L> list = new ArrayList<>();

        L[] listeners = this.map.get(null);
        if (listeners != null) {
            for (L listener : listeners) {
                list.add(listener);
            }
        }
        for (Entry<String, L[]> entry : this.map.entrySet()) {
            String name = entry.getKey();
            if (name != null) {
                for (L listener : entry.getValue()) {
                    list.add(newProxy(name, listener));
                }
            }
        }
        return list.toArray(newArray(list.size()));
    }

    /**
     * 返回与命名属性关联的监听器。
     *
     * @param name  属性名称
     * @return 命名属性的监听器数组
     */
    public final L[] getListeners(String name) {
        if (name != null) {
            L[] listeners = get(name);
            if (listeners != null) {
                return listeners.clone();
            }
        }
        return newArray(0);
    }

    /**
     * 指示映射中是否至少有一个要通知的监听器。
     *
     * @param name  属性名称
     * @return      如果至少存在一个监听器则返回 {@code true}，否则返回 {@code false}
     */
    public final synchronized boolean hasListeners(String name) {
        if (this.map == null) {
            return false;
        }
        L[] array = this.map.get(null);
        return (array != null) || ((name != null) && (null != this.map.get(name)));
    }

    /**
     * 返回映射中的条目集。
     * 每个条目由属性名称和对应的监听器列表组成。
     *
     * @return 映射中的条目集
     */
    public final Set<Entry<String, L[]>> getEntries() {
        return (this.map != null)
                ? this.map.entrySet()
                : Collections.<Entry<String, L[]>>emptySet();
    }

    /**
     * 从代理监听器中提取实际监听器。
     * 这是必要的，因为默认的代理类是不可序列化的。
     *
     * @return 实际监听器
     */
    public abstract L extract(L listener);
}
