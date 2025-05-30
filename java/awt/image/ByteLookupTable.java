/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * 此类定义了一个查找表对象。使用此类对象进行查找操作的输出被解释为无符号字节量。查找表包含一个或多个图像带（或组件）的字节数组，并包含一个偏移量，该偏移量将从输入值中减去，然后用于索引数组。这允许为受限输入提供一个比原生数据大小小的数组。如果查找表中只有一个数组，它将应用于所有带。
 *
 * @see ShortLookupTable
 * @see LookupOp
 */
public class ByteLookupTable extends LookupTable {

    /**
     * 常量
     */

    byte data[][];

    /**
     * 从表示每个带查找表的字节数组数组构造一个 ByteLookupTable 对象。偏移量将从输入值中减去，然后用于索引数组。带的数量是 data 参数的长度。每个带的数据数组作为引用存储。
     * @param offset 从输入值中减去的值
     * @param data 表示每个带查找表的字节数组数组
     * @throws IllegalArgumentException 如果 <code>offset</code> 小于 0 或 <code>data</code> 的长度小于 1
     */
    public ByteLookupTable(int offset, byte data[][]) {
        super(offset,data.length);
        numComponents = data.length;
        numEntries    = data[0].length;
        this.data = new byte[numComponents][];
        // 分配数组并复制数据引用
        for (int i=0; i < numComponents; i++) {
            this.data[i] = data[i];
        }
    }

    /**
     * 从表示应用于所有带的查找表的字节数组构造一个 ByteLookupTable 对象。偏移量将从输入值中减去，然后用于索引数组。数据数组作为引用存储。
     * @param offset 从输入值中减去的值
     * @param data 表示查找表的字节数组
     * @throws IllegalArgumentException 如果 <code>offset</code> 小于 0 或 <code>data</code> 的长度小于 1
     */
    public ByteLookupTable(int offset, byte data[]) {
        super(offset,data.length);
        numComponents = 1;
        numEntries    = data.length;
        this.data = new byte[1][];
        this.data[0] = data;
    }

    /**
     * 通过引用返回查找表数据。如果此 ByteLookupTable 是使用单个字节数组构造的，则返回数组的长度为一。
     * @return 此 <code>ByteLookupTable</code> 的数据数组。
     */
    public final byte[][] getTable(){
        return data;
    }

    /**
     * 返回使用查找表转换的像素样本数组。源数组和目标数组可以是同一个数组。返回数组 <code>dst</code>。
     *
     * @param src 源数组。
     * @param dst 目标数组。此数组必须至少与 <code>src</code> 一样长。如果 <code>dst</code> 为 <code>null</code>，将分配一个与 <code>src</code> 长度相同的新数组。
     * @return 数组 <code>dst</code>，一个 <code>int</code> 样本数组。
     * @exception ArrayIndexOutOfBoundsException 如果 <code>src</code> 比 <code>dst</code> 长，或者对于 <code>src</code> 的任何元素 <code>i</code>，<code>src[i]-offset</code> 小于零或大于等于任何带的查找表长度。
     */
    public int[] lookupPixel(int[] src, int[] dst){
        if (dst == null) {
            // 需要分配一个新的目标数组
            dst = new int[src.length];
        }

        if (numComponents == 1) {
            // 将一个查找表应用于所有带
            for (int i=0; i < src.length; i++) {
                int s = src[i] - offset;
                if (s < 0) {
                    throw new ArrayIndexOutOfBoundsException("src["+i+
                                                             "]-offset is "+
                                                             "less than zero");
                }
                dst[i] = (int) data[0][s];
            }
        }
        else {
            for (int i=0; i < src.length; i++) {
                int s = src[i] - offset;
                if (s < 0) {
                    throw new ArrayIndexOutOfBoundsException("src["+i+
                                                             "]-offset is "+
                                                             "less than zero");
                }
                dst[i] = (int) data[i][s];
            }
        }
        return dst;
    }

    /**
     * 返回使用查找表转换的像素样本数组。源数组和目标数组可以是同一个数组。返回数组 <code>dst</code>。
     *
     * @param src 源数组。
     * @param dst 目标数组。此数组必须至少与 <code>src</code> 一样长。如果 <code>dst</code> 为 <code>null</code>，将分配一个与 <code>src</code> 长度相同的新数组。
     * @return 数组 <code>dst</code>，一个 <code>int</code> 样本数组。
     * @exception ArrayIndexOutOfBoundsException 如果 <code>src</code> 比 <code>dst</code> 长，或者对于 <code>src</code> 的任何元素 <code>i</code>，<code>(src[i]&0xff)-offset</code> 小于零或大于等于任何带的查找表长度。
     */
    public byte[] lookupPixel(byte[] src, byte[] dst){
        if (dst == null) {
            // 需要分配一个新的目标数组
            dst = new byte[src.length];
        }

        if (numComponents == 1) {
            // 将一个查找表应用于所有带
            for (int i=0; i < src.length; i++) {
                int s = (src[i]&0xff) - offset;
                if (s < 0) {
                    throw new ArrayIndexOutOfBoundsException("src["+i+
                                                             "]-offset is "+
                                                             "less than zero");
                }
                dst[i] = data[0][s];
            }
        }
        else {
            for (int i=0; i < src.length; i++) {
                int s = (src[i]&0xff) - offset;
                if (s < 0) {
                    throw new ArrayIndexOutOfBoundsException("src["+i+
                                                             "]-offset is "+
                                                             "less than zero");
                }
                dst[i] = data[i][s];
            }
        }
        return dst;
    }

}
