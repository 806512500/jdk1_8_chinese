/*
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.nio;


/**
 * 一个类型安全的字节序枚举。
 *
 * @author Mark Reinhold
 * @author JSR-51 专家组
 * @since 1.4
 */

public final class ByteOrder {

    private String name;

    private ByteOrder(String name) {
        this.name = name;
    }

    /**
     * 表示大端字节序的常量。在这种顺序中，多字节值的字节从最高有效位到最低有效位排列。
     */
    public static final ByteOrder BIG_ENDIAN
        = new ByteOrder("BIG_ENDIAN");

    /**
     * 表示小端字节序的常量。在这种顺序中，多字节值的字节从最低有效位到最高有效位排列。
     */
    public static final ByteOrder LITTLE_ENDIAN
        = new ByteOrder("LITTLE_ENDIAN");

    /**
     * 获取底层平台的本机字节序。
     *
     * <p> 定义此方法是为了使性能敏感的 Java 代码可以使用与硬件相同的字节序分配直接缓冲区。
     * 使用这样的缓冲区时，本机代码库通常更高效。 </p>
     *
     * @return  此 Java 虚拟机运行的硬件的本机字节序
     */
    public static ByteOrder nativeOrder() {
        return Bits.byteOrder();
    }

    /**
     * 构造描述此对象的字符串。
     *
     * <p> 此方法为 {@link
     * #BIG_ENDIAN} 返回字符串 <tt>"BIG_ENDIAN"</tt>，为 {@link #LITTLE_ENDIAN} 返回 <tt>"LITTLE_ENDIAN"</tt>。
     * </p>
     *
     * @return  指定的字符串
     */
    public String toString() {
        return name;
    }

}
