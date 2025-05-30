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

import java.io.IOException;

/**
 * 定义了可以用于提供传输操作数据的类的接口。
 * <p>
 * 有关使用 Swing 进行数据传输的信息，请参阅
 * <a href="https://docs.oracle.com/javase/tutorial/uiswing/dnd/index.html">
 * 如何使用拖放和数据传输</a>，
 * 《Java教程》中的一个部分，以获取更多信息。
 *
 * @author      Amy Fowler
 */

public interface Transferable {

    /**
     * 返回一个 DataFlavor 对象数组，指示可以提供的数据格式。数组应按提供数据的偏好顺序排列
     * （从最丰富描述到最简单描述）。
     * @return 一个数据格式数组，表示此数据可以传输的数据格式
     */
    public DataFlavor[] getTransferDataFlavors();

    /**
     * 返回指定的数据格式是否支持此对象。
     * @param flavor 请求的数据格式
     * @return 布尔值，表示数据格式是否支持
     */
    public boolean isDataFlavorSupported(DataFlavor flavor);

    /**
     * 返回一个表示要传输的数据的对象。返回对象的类由数据格式的表示类定义。
     *
     * @param flavor 请求的数据格式
     * @see DataFlavor#getRepresentationClass
     * @exception IOException                如果请求的数据格式不再可用。
     * @exception UnsupportedFlavorException 如果请求的数据格式不受支持。
     */
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException;

}
