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

package java.awt.datatransfer;

import java.io.*;


/**
 * 实现了传输 <code>String</code> 所需的能力的 <code>Transferable</code>。
 *
 * 此 <code>Transferable</code> 正确支持 <code>DataFlavor.stringFlavor</code>
 * 和所有等效的 flavor。对 <code>DataFlavor.plainTextFlavor</code>
 * 和所有等效的 flavor 的支持已 <b>弃用</b>。不支持其他任何 <code>DataFlavor</code>。
 *
 * @see java.awt.datatransfer.DataFlavor#stringFlavor
 * @see java.awt.datatransfer.DataFlavor#plainTextFlavor
 */
public class StringSelection implements Transferable, ClipboardOwner {

    private static final int STRING = 0;
    private static final int PLAIN_TEXT = 1;

    private static final DataFlavor[] flavors = {
        DataFlavor.stringFlavor,
        DataFlavor.plainTextFlavor // 已弃用
    };

    private String data;

    /**
     * 创建一个能够传输指定 <code>String</code> 的 <code>Transferable</code>。
     */
    public StringSelection(String data) {
        this.data = data;
    }

    /**
     * 返回此 <code>Transferable</code> 可以提供的数据的 flavor 数组。支持 <code>DataFlavor.stringFlavor</code>。
     * 对 <code>DataFlavor.plainTextFlavor</code> 的支持已 <b>弃用</b>。
     *
     * @return 长度为二的数组，其元素为 <code>DataFlavor.stringFlavor</code>
     *         和 <code>DataFlavor.plainTextFlavor</code>
     */
    public DataFlavor[] getTransferDataFlavors() {
        // 返回 flavors 本身会允许客户端代码修改
        // 我们的内部行为
        return (DataFlavor[])flavors.clone();
    }

    /**
     * 返回此 <code>Transferable</code> 是否支持请求的 flavor。
     *
     * @param flavor 请求的数据 flavor
     * @return 如果 <code>flavor</code> 等于 <code>DataFlavor.stringFlavor</code>
     *         或 <code>DataFlavor.plainTextFlavor</code>，则返回 true；
     *         如果 <code>flavor</code> 不是上述 flavor 之一，则返回 false
     * @throws NullPointerException 如果 flavor 为 <code>null</code>
     */
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        // JCK Test StringSelection0003: 如果 'flavor' 为 null，抛出 NPE
        for (int i = 0; i < flavors.length; i++) {
            if (flavor.equals(flavors[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * 如果可能，返回 <code>Transferable</code> 的数据在请求的 <code>DataFlavor</code> 中。
     * 如果所需的 flavor 是 <code>DataFlavor.stringFlavor</code> 或等效 flavor，
     * 则返回表示选择的 <code>String</code>。如果所需的 flavor 是
     * <code>DataFlavor.plainTextFlavor</code> 或等效 flavor，
     * 则返回一个 <code>Reader</code>。<b>注意：</b> 对于 <code>DataFlavor.plainTextFlavor</code>
     * 和等效 <code>DataFlavor</code>，此方法的行为与 <code>DataFlavor.plainTextFlavor</code>
     * 的定义不一致。
     *
     * @param flavor 请求的数据 flavor
     * @return 以请求的 flavor 返回的数据，如上所述
     * @throws UnsupportedFlavorException 如果请求的数据 flavor 不等效于
     *         <code>DataFlavor.stringFlavor</code> 或 <code>DataFlavor.plainTextFlavor</code>
     * @throws IOException 如果在检索数据时发生 IOException。默认情况下，StringSelection 从不抛出此异常，但子类可以抛出。
     * @throws NullPointerException 如果 flavor 为 <code>null</code>
     * @see java.io.Reader
     */
    public Object getTransferData(DataFlavor flavor)
        throws UnsupportedFlavorException, IOException
    {
        // JCK Test StringSelection0007: 如果 'flavor' 为 null，抛出 NPE
        if (flavor.equals(flavors[STRING])) {
            return (Object)data;
        } else if (flavor.equals(flavors[PLAIN_TEXT])) {
            return new StringReader(data == null ? "" : data);
        } else {
            throw new UnsupportedFlavorException(flavor);
        }
    }

    public void lostOwnership(Clipboard clipboard, Transferable contents) {
    }
}
