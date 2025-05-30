/*
 * Copyright (c) 2000, 2011, Oracle and/or its affiliates. All rights reserved.
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

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import static java.util.Locale.ENGLISH;

/**
 * 一个用于生成对象实例唯一名称的工具类。
 * 名称将是未限定类名和实例编号的组合。
 * <p>
 * 例如，如果第一个 javax.swing.JButton 对象实例传递给 <code>instanceName</code>，
 * 则返回的字符串标识符将是 &quot;JButton0&quot;。
 *
 * @author Philip Milne
 */
class NameGenerator {

    private Map<Object, String> valueToName;
    private Map<String, Integer> nameToCount;

    public NameGenerator() {
        valueToName = new IdentityHashMap<>();
        nameToCount = new HashMap<>();
    }

    /**
     * 清除名称缓存。应在编码周期接近结束时调用。
     */
    public void clear() {
        valueToName.clear();
        nameToCount.clear();
    }

    /**
     * 返回类的根名称。
     */
    @SuppressWarnings("rawtypes")
    public static String unqualifiedClassName(Class type) {
        if (type.isArray()) {
            return unqualifiedClassName(type.getComponentType()) + "Array";
        }
        String name = type.getName();
        return name.substring(name.lastIndexOf('.') + 1);
    }

    /**
     * 返回一个字符串，该字符串将字符串的第一个字母大写。
     */
    public static String capitalize(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        return name.substring(0, 1).toUpperCase(ENGLISH) + name.substring(1);
    }

    /**
     * 返回一个唯一字符串，用于标识对象实例。
     * 调用是缓存的，因此如果对象之前已传递给此方法，则返回相同的标识符。
     *
     * @param instance 用于生成字符串的对象
     * @return 代表对象的唯一字符串
     */
    public String instanceName(Object instance) {
        if (instance == null) {
            return "null";
        }
        if (instance instanceof Class) {
            return unqualifiedClassName((Class) instance);
        } else {
            String result = valueToName.get(instance);
            if (result != null) {
                return result;
            }
            Class<?> type = instance.getClass();
            String className = unqualifiedClassName(type);

            Integer size = nameToCount.get(className);
            int instanceNumber = (size == null) ? 0 : (size).intValue() + 1;
            nameToCount.put(className, new Integer(instanceNumber));

            result = className + instanceNumber;
            valueToName.put(instance, result);
            return result;
        }
    }
}
