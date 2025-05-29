/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.sql;

/**
 * <p>用于建立连接的驱动属性。只有高级程序员需要通过方法
 * <code>getDriverProperties</code> 与驱动交互，以发现和提供连接属性时，
 * <code>DriverPropertyInfo</code> 类才具有实际意义。
 */

public class DriverPropertyInfo {

    /**
     * 使用给定的名称和值构造一个 <code>DriverPropertyInfo</code> 对象。
     * <code>description</code> 和 <code>choices</code> 被初始化为 <code>null</code>，
     * <code>required</code> 被初始化为 <code>false</code>。
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
     * 如果必须在 <code>Driver.connect</code> 期间为该属性提供值，则 <code>required</code> 字段为 <code>true</code>，
     * 否则为 <code>false</code>。
     */
    public boolean required = false;

    /**
     * <code>value</code> 字段指定了属性的当前值，该值基于传递给方法 <code>getPropertyInfo</code> 的信息、
     * Java 环境以及驱动提供的默认值的组合。如果未知，则该字段可能为 null。
     */
    public String value = null;

    /**
     * 如果 <code>DriverPropertyInfo.value</code> 字段的值可以从特定值集中选择，则为可能的值数组；
     * 否则为 null。
     */
    public String[] choices = null;
}
