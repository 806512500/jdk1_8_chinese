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

package java.beans;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

/**
 * 基于哈希表的映射，使用弱引用存储键，并使用引用相等性而不是对象相等性来比较它们。
 * 当键不再被常规使用时，条目将自动被移除。支持 null 值和 null 键。
 * 本类不需要额外的同步。线程安全性由同步块和易失性字段的脆弱组合提供。
 * 编辑时要非常小心！
 *
 * @see java.util.IdentityHashMap
 * @see java.util.WeakHashMap
 */
abstract class WeakIdentityMap<T> {

    private static final int MAXIMUM_CAPACITY = 1 << 30; // 必须是2的幂
    private static final Object NULL = new Object(); // 用于 null 键的特殊对象

    private final ReferenceQueue<Object> queue = new ReferenceQueue<Object>();

    private volatile Entry<T>[] table = newTable(1<<3); // 表的长度必须是2的幂
    private int threshold = 6; // 下一个调整大小的值
    private int size = 0; // 键值映射的数量

    public T get(Object key) {
        removeStaleEntries();
        if (key == null) {
            key = NULL;
        }
        int hash = key.hashCode();
        Entry<T>[] table = this.table;
        // 未同步的搜索提高性能
        // null 值并不意味着没有所需的条目
        int index = getIndex(table, hash);
        for (Entry<T> entry = table[index]; entry != null; entry = entry.next) {
            if (entry.isMatched(key, hash)) {
                return entry.value;
            }
        }
        synchronized (NULL) {
            // 同步的搜索提高稳定性
            // 如果没有所需的条目，必须创建并添加新值
            index = getIndex(this.table, hash);
            for (Entry<T> entry = this.table[index]; entry != null; entry = entry.next) {
                if (entry.isMatched(key, hash)) {
                    return entry.value;
                }
            }
            T value = create(key);
            this.table[index] = new Entry<T>(key, hash, value, this.queue, this.table[index]);
            if (++this.size >= this.threshold) {
                if (this.table.length == MAXIMUM_CAPACITY) {
                    this.threshold = Integer.MAX_VALUE;
                }
                else {
                    removeStaleEntries();
                    table = newTable(this.table.length * 2);
                    transfer(this.table, table);
                    // 如果忽略 null 元素和处理引用队列导致大量收缩，则恢复旧表。
                    // 这应该是罕见的，但避免了充满垃圾的表的无限制扩展。
                    if (this.size >= this.threshold / 2) {
                        this.table = table;
                        this.threshold *= 2;
                    }
                    else {
                        transfer(table, this.table);
                    }
                }
            }
            return value;
        }
    }

    protected abstract T create(Object key);

    private void removeStaleEntries() {
        Object ref = this.queue.poll();
        if (ref != null) {
            synchronized (NULL) {
                do {
                    @SuppressWarnings("unchecked")
                    Entry<T> entry = (Entry<T>) ref;
                    int index = getIndex(this.table, entry.hash);

                    Entry<T> prev = this.table[index];
                    Entry<T> current = prev;
                    while (current != null) {
                        Entry<T> next = current.next;
                        if (current == entry) {
                            if (prev == entry) {
                                this.table[index] = next;
                            }
                            else {
                                prev.next = next;
                            }
                            entry.value = null; // 帮助垃圾回收
                            entry.next = null; // 帮助垃圾回收
                            this.size--;
                            break;
                        }
                        prev = current;
                        current = next;
                    }
                    ref = this.queue.poll();
                }
                while (ref != null);
            }
        }
    }

    private void transfer(Entry<T>[] oldTable, Entry<T>[] newTable) {
        for (int i = 0; i < oldTable.length; i++) {
            Entry<T> entry = oldTable[i];
            oldTable[i] = null;
            while (entry != null) {
                Entry<T> next = entry.next;
                Object key = entry.get();
                if (key == null) {
                    entry.value = null; // 帮助垃圾回收
                    entry.next = null; // 帮助垃圾回收
                    this.size--;
                }
                else {
                    int index = getIndex(newTable, entry.hash);
                    entry.next = newTable[index];
                    newTable[index] = entry;
                }
                entry = next;
            }
        }
    }


    @SuppressWarnings("unchecked")
    private Entry<T>[] newTable(int length) {
        return (Entry<T>[]) new Entry<?>[length];
    }

    private static int getIndex(Entry<?>[] table, int hash) {
        return hash & (table.length - 1);
    }

    private static class Entry<T> extends WeakReference<Object> {
        private final int hash;
        private volatile T value;
        private volatile Entry<T> next;

        Entry(Object key, int hash, T value, ReferenceQueue<Object> queue, Entry<T> next) {
            super(key, queue);
            this.hash = hash;
            this.value = value;
            this.next  = next;
        }

        boolean isMatched(Object key, int hash) {
            return (this.hash == hash) && (key == get());
        }
    }
}
