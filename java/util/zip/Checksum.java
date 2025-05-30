/*
 * Copyright (c) 1996, 1999, Oracle and/or its affiliates. All rights reserved.
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
     * 返回当前校验和值。
     * @return 当前校验和值
     */
    public long getValue();

    /**
     * 将校验和重置为其初始值。
     */
    public void reset();
}
