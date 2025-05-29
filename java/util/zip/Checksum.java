/*
 * Copyright (c) 1996, 1999, Oracle and/or its affiliates. All rights reserved.
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

package java.util.zip;

/**
 * 表示数据校验和的接口。
 *
 * @author      David Connelly
 */
public
interface Checksum {
    /**
     * 使用指定的字节更新当前校验和。
     *
     * @param b 用于更新校验和的字节
     */
    public void update(int b);

    /**
     * 使用指定的字节数组更新当前校验和。
     * @param b 用于更新校验和的字节数组
     * @param off 数据的起始偏移量
     * @param len 用于更新的字节数
     */
    public void update(byte[] b, int off, int len);

    /**
     * 返回当前的校验和值。
     * @return 当前的校验和值
     */
    public long getValue();

    /**
     * 将校验和重置为其初始值。
     */
    public void reset();
}
