/*
 * Copyright (c) 2007, 2011, Oracle and/or its affiliates. All rights reserved.
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

package java.nio.file;

/**
 * 定义用于测试文件可访问性的访问模式。
 *
 * @since 1.7
 */

public enum AccessMode {
    /**
     * 测试读取访问。
     */
    READ,
    /**
     * 测试写入访问。
     */
    WRITE,
    /**
     * 测试执行访问。
     */
    EXECUTE;
}
