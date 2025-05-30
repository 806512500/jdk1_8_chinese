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

package java.awt;

import java.lang.annotation.Native;

/**
 * <code>DisplayMode</code> 类封装了图形设备的位深度、高度、宽度和刷新率。更改图形设备的显示模式的能力取决于平台和配置，可能并不总是可用
 * （参见 {@link GraphicsDevice#isDisplayChangeSupported}）。
 * <p>
 * 有关全屏独占模式 API 的更多信息，请参见
 * <a href="https://docs.oracle.com/javase/tutorial/extra/fullscreen/index.html">
 * 全屏独占模式 API 教程</a>。
 *
 * @see GraphicsDevice
 * @see GraphicsDevice#isDisplayChangeSupported
 * @see GraphicsDevice#getDisplayModes
 * @see GraphicsDevice#setDisplayMode
 * @author Michael Martak
 * @since 1.4
 */

public final class DisplayMode {

    private Dimension size;
    private int bitDepth;
    private int refreshRate;

    /**
     * 使用提供的参数创建一个新的显示模式对象。
     * @param width 显示的宽度，以像素为单位
     * @param height 显示的高度，以像素为单位
     * @param bitDepth 显示的位深度，以每像素位数为单位。如果支持多种位深度，可以是 <code>BIT_DEPTH_MULTI</code>。
     * @param refreshRate 显示的刷新率，以赫兹为单位。如果信息不可用，可以是 <code>REFRESH_RATE_UNKNOWN</code>。
     * @see #BIT_DEPTH_MULTI
     * @see #REFRESH_RATE_UNKNOWN
     */
    public DisplayMode(int width, int height, int bitDepth, int refreshRate) {
        this.size = new Dimension(width, height);
        this.bitDepth = bitDepth;
        this.refreshRate = refreshRate;
    }

    /**
     * 返回显示的高度，以像素为单位。
     * @return 显示的高度，以像素为单位
     */
    public int getHeight() {
        return size.height;
    }

    /**
     * 返回显示的宽度，以像素为单位。
     * @return 显示的宽度，以像素为单位
     */
    public int getWidth() {
        return size.width;
    }

    /**
     * 如果此显示模式支持多种位深度，则位深度的值。
     * @see #getBitDepth
     */
    @Native public final static int BIT_DEPTH_MULTI = -1;

    /**
     * 返回显示的位深度，以每像素位数为单位。如果此显示模式支持多种位深度，可能是 <code>BIT_DEPTH_MULTI</code>。
     *
     * @return 显示的位深度，以每像素位数为单位。
     * @see #BIT_DEPTH_MULTI
     */
    public int getBitDepth() {
        return bitDepth;
    }

    /**
     * 如果未知，则刷新率的值。
     * @see #getRefreshRate
     */
    @Native public final static int REFRESH_RATE_UNKNOWN = 0;

    /**
     * 返回显示的刷新率，以赫兹为单位。如果信息不可用，可能是 <code>REFRESH_RATE_UNKNOWN</code>。
     *
     * @return 显示的刷新率，以赫兹为单位。
     * @see #REFRESH_RATE_UNKNOWN
     */
    public int getRefreshRate() {
        return refreshRate;
    }

    /**
     * 返回两个显示模式是否相等。
     * @return 两个显示模式是否相等
     */
    public boolean equals(DisplayMode dm) {
        if (dm == null) {
            return false;
        }
        return (getHeight() == dm.getHeight()
            && getWidth() == dm.getWidth()
            && getBitDepth() == dm.getBitDepth()
            && getRefreshRate() == dm.getRefreshRate());
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object dm) {
        if (dm instanceof DisplayMode) {
            return equals((DisplayMode)dm);
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return getWidth() + getHeight() + getBitDepth() * 7
            + getRefreshRate() * 13;
    }

}
