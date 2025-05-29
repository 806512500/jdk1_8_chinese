/*
 * Copyright (c) 2017, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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

package java.lang.ref;


import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Comparator;

/**
 * 该 FinalizerHistogram 类用于支持 GC.finalizer_info 诊断命令。
 * 它由 VM 调用。
 */

final class FinalizerHistogram {

    private static final class Entry {
        private int instanceCount;
        private final String className;

        int getInstanceCount() {
            return instanceCount;
        }

        void increment() {
            instanceCount += 1;
        }

        Entry(String className) {
            this.className = className;
        }
    }

    // 下面的方法由 VM 调用，VM 期望特定的条目类布局。

    static Entry[] getFinalizerHistogram() {
        Map<String, Entry> countMap = new HashMap<>();
        ReferenceQueue<Object> queue = Finalizer.getQueue();
        queue.forEach(r -> {
            Object referent = r.get();
            if (referent != null) {
                countMap.computeIfAbsent(
                    referent.getClass().getName(), Entry::new).increment();
                /* 清除包含此变量的堆栈槽，以减少保守 GC 误保留的机会 */
                referent = null;
            }
        });

        Entry fhe[] = countMap.values().toArray(new Entry[countMap.size()]);
        Arrays.sort(fhe,
                Comparator.comparingInt(Entry::getInstanceCount).reversed());
        return fhe;
    }
}
