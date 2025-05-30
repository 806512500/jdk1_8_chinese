/*
 * Copyright (c) 1997, 2000, Oracle and/or its affiliates. All rights reserved.
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

package java.awt.image;


/**
 * 此抽象类定义了一个查找表对象。ByteLookupTable 和 ShortLookupTable 是子类，
 * 分别包含 byte 和 short 数据。查找表包含一个或多个图像带（或组件）的数据数组
 * （例如，R、G 和 B 的单独数组），并且它包含一个偏移量，该偏移量将从输入值中减去，
 * 然后用于索引数组。这允许为受限输入提供比本机数据大小小的数组。如果查找表中只有一个数组，
 * 它将应用于所有带。所有数组必须是相同的大小。
 *
 * @see ByteLookupTable
 * @see ShortLookupTable
 * @see LookupOp
 */
public abstract class LookupTable extends Object {

    /**
     * 常量
     */

    int  numComponents;
    int  offset;
    int  numEntries;

    /**
     * 从组件数量和查找表的偏移量构造一个新的查找表。
     * @param offset 从输入值中减去的偏移量，用于索引此 <code>LookupTable</code> 的数据数组
     * @param numComponents 此 <code>LookupTable</code> 中的数据数组数量
     * @throws IllegalArgumentException 如果 <code>offset</code> 小于 0
     *         或 <code>numComponents</code> 小于 1
     */
    protected LookupTable(int offset, int numComponents) {
        if (offset < 0) {
            throw new
                IllegalArgumentException("Offset must be greater than 0");
        }
        if (numComponents < 1) {
            throw new IllegalArgumentException("Number of components must " +
                                               " be at least 1");
        }
        this.numComponents = numComponents;
        this.offset = offset;
    }

    /**
     * 返回查找表中的组件数量。
     * @return 此 <code>LookupTable</code> 中的组件数量。
     */
    public int getNumComponents() {
        return numComponents;
    }

    /**
     * 返回偏移量。
     * @return 此 <code>LookupTable</code> 的偏移量。
     */
    public int getOffset() {
        return offset;
    }

    /**
     * 返回一个像素的组件的 <code>int</code> 数组。<code>dest</code> 数组包含查找结果并返回。
     * 如果 dest 为 <code>null</code>，则分配一个新数组。源和目标可以相等。
     * @param src 一个像素的组件的源数组
     * @param dest 一个像素的组件的目标数组，使用此 <code>LookupTable</code> 进行转换
     * @return 一个像素的组件的 <code>int</code> 数组。
     */
    public abstract int[] lookupPixel(int[] src, int[] dest);

}
