/*
 * Copyright (c) 2003, 2014, Oracle and/or its affiliates. All rights reserved.
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
 * 描述指针位置的类。
 * 它提供了指针所在的 {@code GraphicsDevice} 以及表示指针坐标的 {@code Point}。
 * <p>
 * 该类的实例应通过 {@link MouseInfo#getPointerInfo} 获取。
 * {@code PointerInfo} 实例不会随着鼠标移动而动态更新。要获取更新的位置，必须再次调用
 * {@link MouseInfo#getPointerInfo}。
 *
 * @see MouseInfo#getPointerInfo
 * @author Roman Poborchiy
 * @since 1.5
 */
public class PointerInfo {

    private final GraphicsDevice device;
    private final Point location;

    /**
     * 包私有构造函数，防止实例化。
     */
    PointerInfo(final GraphicsDevice device, final Point location) {
        this.device = device;
        this.location = location;
    }

    /**
     * 返回创建此 {@code PointerInfo} 时鼠标指针所在的 {@code GraphicsDevice}。
     *
     * @return 对应于指针的 {@code GraphicsDevice}
     * @since 1.5
     */
    public GraphicsDevice getDevice() {
        return device;
    }

    /**
     * 返回表示指针在屏幕上的坐标的 {@code Point}。有关多屏幕系统中坐标计算的更多信息，请参见
     * {@link MouseInfo#getPointerInfo}。
     *
     * @return 鼠标指针的坐标
     * @see MouseInfo
     * @see MouseInfo#getPointerInfo
     * @since 1.5
     */
    public Point getLocation() {
        return location;
    }
}
