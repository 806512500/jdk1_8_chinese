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
 * 当数据值因其他原因（而非超出 <code>MaxFieldSize</code>）而意外截断时，抛出的异常（在写入时）或报告的警告（在读取时）。
 *
 * <P>在读取期间发生 <code>DataTruncation</code> 时的 SQLstate 为 <code>01004</code>。
 * <P>在写入期间发生 <code>DataTruncation</code> 时的 SQLstate 为 <code>22001</code>。
 */

public class DataTruncation extends SQLWarning {

    /**
     * 创建一个 <code>DataTruncation</code> 对象，将 SQLState 初始化为当 <code>read</code> 设置为 <code>true</code> 时为 01004，当 <code>read</code> 设置为 <code>false</code> 时为 22001，
     * 将原因设置为 "Data truncation"，将供应商代码设置为 0，并将其他字段设置为给定的值。
     * <code>cause</code> 未初始化，可以通过调用
     * {@link Throwable#initCause(java.lang.Throwable)} 方法进行初始化。
     * <p>
     *
     * @param index 被截断的参数或列值的索引
     * @param parameter 如果截断的是参数值，则为 true
     * @param read 如果读取被截断，则为 true
     * @param dataSize 数据的原始大小
     * @param transferSize 截断后的大小
     */
    public DataTruncation(int index, boolean parameter,
                          boolean read, int dataSize,
                          int transferSize) {
        super("Data truncation", read == true?"01004":"22001");
        this.index = index;
        this.parameter = parameter;
        this.read = read;
        this.dataSize = dataSize;
        this.transferSize = transferSize;

    }

    /**
     * 创建一个 <code>DataTruncation</code> 对象，将 SQLState 初始化为当 <code>read</code> 设置为 <code>true</code> 时为 01004，当 <code>read</code> 设置为 <code>false</code> 时为 22001，
     * 将原因设置为 "Data truncation"，将供应商代码设置为 0，并将其他字段设置为给定的值。
     * <p>
     *
     * @param index 被截断的参数或列值的索引
     * @param parameter 如果截断的是参数值，则为 true
     * @param read 如果读取被截断，则为 true
     * @param dataSize 数据的原始大小
     * @param transferSize 截断后的大小
     * @param cause 造成此 <code>DataTruncation</code> 的根本原因（可通过 <code>getCause()</code> 方法稍后检索）；可以为 null，表示原因不存在或未知。
     *
     * @since 1.6
     */
    public DataTruncation(int index, boolean parameter,
                          boolean read, int dataSize,
                          int transferSize, Throwable cause) {
        super("Data truncation", read == true?"01004":"22001",cause);
        this.index = index;
        this.parameter = parameter;
        this.read = read;
        this.dataSize = dataSize;
        this.transferSize = transferSize;
    }

    /**
     * 检索被截断的列或参数的索引。
     *
     * <P>如果列或参数索引未知，这可能是 -1，在这种情况下，应忽略 <code>parameter</code> 和 <code>read</code> 字段。
     *
     * @return 被截断的参数或列值的索引
     */
    public int getIndex() {
        return index;
    }

    /**
     * 指示被截断的值是参数值还是列值。
     *
     * @return 如果被截断的值是参数，则为 <code>true</code>；如果是列值，则为 <code>false</code>
     */
    public boolean getParameter() {
        return parameter;
    }

    /**
     * 指示值是否在读取时被截断。
     *
     * @return 如果值在从数据库读取时被截断，则为 <code>true</code>；如果数据在写入时被截断，则为 <code>false</code>
     */
    public boolean getRead() {
        return read;
    }

    /**
     * 获取应传输的数据字节数。如果正在进行数据转换，此数字可能是近似值。如果大小未知，值可能是 <code>-1</code>。
     *
     * @return 应传输的数据字节数
     */
    public int getDataSize() {
        return dataSize;
    }

    /**
     * 获取实际传输的数据字节数。如果大小未知，值可能是 <code>-1</code>。
     *
     * @return 实际传输的数据字节数
     */
    public int getTransferSize() {
        return transferSize;
    }

        /**
        * @serial
        */
    private int index;

        /**
        * @serial
        */
    private boolean parameter;

        /**
        * @serial
        */
    private boolean read;

        /**
        * @serial
        */
    private int dataSize;

        /**
        * @serial
        */
    private int transferSize;

    /**
     * @serial
     */
    private static final long serialVersionUID = 6464298989504059473L;

}
