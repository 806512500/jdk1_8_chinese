/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.sql;

/**
 * <p>用于建立连接的驱动属性。 <code>DriverPropertyInfo</code> 类仅对需要通过
 * <code>getDriverProperties</code> 方法与驱动交互以发现和提供连接属性的高级程序员感兴趣。
 */

public class DriverPropertyInfo {

    /**
     * 使用给定的名称和值构造一个 <code>DriverPropertyInfo</code> 对象。 <code>description</code>
     * 和 <code>choices</code> 初始化为 <code>null</code>，<code>required</code> 初始化为 <code>false</code>。
     *
     * @param name 属性的名称
     * @param value 当前值，可能为 null
     */
    public DriverPropertyInfo(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * 属性的名称。
     */
    public String name;

    /**
     * 属性的简要描述，可能为 null。
     */
    public String description = null;

    /**
     * 如果在 <code>Driver.connect</code> 期间必须为此属性提供值，则 <code>required</code> 字段为 <code>true</code>，
     * 否则为 <code>false</code>。
     */
    public boolean required = false;

    /**
     * <code>value</code> 字段指定了属性的当前值，基于传递给 <code>getPropertyInfo</code> 方法的信息、
     * Java 环境和驱动提供的默认值的组合。 如果没有已知的值，此字段可能为 null。
     */
    public String value = null;

    /**
     * 如果 <code>DriverPropertyInfo.value</code> 字段的值可以从特定的一组值中选择，
     * 则为这些可能的值的数组；否则为 null。
     */
    public String[] choices = null;
}
