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

package java.awt;

import sun.security.util.SecurityConstants;
/**
 * <code>MouseInfo</code> 提供了获取鼠标信息的方法，例如鼠标指针的位置和鼠标按钮的数量。
 *
 * @author     Roman Poborchiy
 * @since 1.5
 */

public class MouseInfo {

    /**
     * 私有构造函数，防止实例化。
     */
    private MouseInfo() {
    }

    /**
     * 返回一个表示当前鼠标指针位置的 <code>PointerInfo</code> 实例。
     * 存储在此 <code>PointerInfo</code> 中的 <code>GraphicsDevice</code> 包含鼠标指针。用于鼠标位置的坐标系取决于 <code>GraphicsDevice</code> 是否是虚拟屏幕设备的一部分。
     * 对于虚拟屏幕设备，坐标在虚拟坐标系中给出，否则它们在 <code>GraphicsDevice</code> 的坐标系中返回。有关虚拟屏幕设备的更多信息，请参见 {@link GraphicsConfiguration}。
     * 在没有鼠标的情况下，返回 <code>null</code>。
     * <p>
     * 如果存在安全管理者，其 <code>checkPermission</code> 方法将使用 <code>AWTPermission("watchMousePointer")</code> 权限在创建和返回 <code>PointerInfo</code> 对象之前被调用。这可能导致 <code>SecurityException</code>。
     *
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless() 返回 true
     * @exception SecurityException 如果存在安全管理者且其 <code>checkPermission</code> 方法不允许该操作
     * @see       GraphicsConfiguration
     * @see       SecurityManager#checkPermission
     * @see       java.awt.AWTPermission
     * @return    鼠标指针的位置
     * @since     1.5
     */
    public static PointerInfo getPointerInfo() throws HeadlessException {
        if (GraphicsEnvironment.isHeadless()) {
            throw new HeadlessException();
        }

        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(SecurityConstants.AWT.WATCH_MOUSE_PERMISSION);
        }

        Point point = new Point(0, 0);
        int deviceNum = Toolkit.getDefaultToolkit().getMouseInfoPeer().fillPointWithCoords(point);
        GraphicsDevice[] gds = GraphicsEnvironment.getLocalGraphicsEnvironment().
                                   getScreenDevices();
        PointerInfo retval = null;
        if (areScreenDevicesIndependent(gds)) {
            retval = new PointerInfo(gds[deviceNum], point);
        } else {
            for (int i = 0; i < gds.length; i++) {
                GraphicsConfiguration gc = gds[i].getDefaultConfiguration();
                Rectangle bounds = gc.getBounds();
                if (bounds.contains(point)) {
                    retval = new PointerInfo(gds[i], point);
                }
            }
        }

        return retval;
    }

    private static boolean areScreenDevicesIndependent(GraphicsDevice[] gds) {
        for (int i = 0; i < gds.length; i++) {
            Rectangle bounds = gds[i].getDefaultConfiguration().getBounds();
            if (bounds.x != 0 || bounds.y != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * 返回鼠标上的按钮数量。
     * 在没有鼠标的情况下，返回 <code>-1</code>。
     *
     * @exception HeadlessException 如果 GraphicsEnvironment.isHeadless() 返回 true
     * @return 鼠标上的按钮数量
     * @since 1.5
     */
    public static int getNumberOfButtons() throws HeadlessException {
        if (GraphicsEnvironment.isHeadless()) {
            throw new HeadlessException();
        }
        Object prop = Toolkit.getDefaultToolkit().
                              getDesktopProperty("awt.mouse.numButtons");
        if (prop instanceof Integer) {
            return ((Integer)prop).intValue();
        }

        // 这不应该发生。
        assert false : "awt.mouse.numButtons is not an integer property";
        return 0;
    }

}
