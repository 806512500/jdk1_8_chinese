/*
 * Copyright (c) 2003, Oracle and/or its affiliates. All rights reserved.
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

package java.awt.peer;

import java.awt.Window;
import java.awt.Point;

/**
 * {@link MouseInfo} 的对等接口。用于获取有关鼠标的一些附加信息。
 *
 * 对等接口仅用于移植 AWT。不建议应用程序开发人员使用这些接口，也不应直接调用对等实例上的任何对等方法。
 */
public interface MouseInfoPeer {

    /**
     * 此方法执行两项任务：它用当前鼠标光标的坐标填充点字段，并返回指针所在屏幕设备的编号。
     * 屏幕设备的编号仅针对独立设备（不是虚拟屏幕设备的一部分）返回。对于虚拟屏幕设备，返回 0。
     * 鼠标坐标也根据屏幕设备是否为虚拟设备进行计算。对于虚拟屏幕设备，指针坐标在虚拟坐标系统中计算。
     * 否则，坐标在指针所在屏幕设备的坐标系统中计算。
     * 有关虚拟屏幕设备的更多详细信息，请参阅 java.awt.GraphicsConfiguration 文档。
     */
    int fillPointWithCoords(Point point);

    /**
     * 返回窗口是否位于鼠标指针下方。如果窗口在屏幕上显示，并且鼠标指针位于窗口未被其他窗口遮挡的部分上方，则认为窗口位于鼠标指针下方。
     */
    boolean isWindowUnderMouse(Window w);

}
