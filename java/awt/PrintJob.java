/*
 * Copyright (c) 1996, Oracle and/or its affiliates. All rights reserved.
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

package java.awt;

/**
 * 一个抽象类，用于启动和执行打印作业。
 * 它提供了一个打印图形对象，该对象渲染到适当的打印设备。
 *
 * @see Toolkit#getPrintJob
 *
 * @author      Amy Fowler
 */
public abstract class PrintJob {

    /**
     * 获取一个将绘制到下一页的图形对象。
     * 当图形对象被释放时，页面将被发送到打印机。此图形对象还将实现
     * PrintGraphics 接口。
     * @see PrintGraphics
     */
    public abstract Graphics getGraphics();

    /**
     * 返回页面的像素尺寸。
     * 页面的分辨率被选择为与屏幕分辨率相似。
     */
    public abstract Dimension getPageDimension();

    /**
     * 返回页面的每英寸像素分辨率。
     * 请注意，这不一定对应于打印机的物理分辨率。
     */
    public abstract int getPageResolution();

    /**
     * 如果最后一页将首先打印，则返回 true。
     */
    public abstract boolean lastPageFirst();

    /**
     * 结束打印作业并执行任何必要的清理。
     */
    public abstract void end();

    /**
     * 当此打印作业不再被引用时，结束此打印作业。
     * @see #end
     */
    public void finalize() {
        end();
    }

}
