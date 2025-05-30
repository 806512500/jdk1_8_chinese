/*
 * Copyright (c) 1999, 2008, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.*;

/**
 * RobotPeer 定义了一个接口，通过该接口工具包支持自动化测试，允许从 Java 代码生成本机输入事件。
 *
 * 代码外部的 java.awt.* 层次结构不应直接导入此接口；它不应被视为公共接口，并且可能会发生变化。
 *
 * @author      Robi Khan
 */
public interface RobotPeer
{
    /**
     * 将鼠标指针移动到指定的屏幕位置。
     *
     * @param x 屏幕上的 X 位置
     * @param y 屏幕上的 Y 位置
     *
     * @see Robot#mouseMove(int, int)
     */
    void mouseMove(int x, int y);

    /**
     * 模拟指定按钮的鼠标按下操作。
     *
     * @param buttons 按钮掩码
     *
     * @see Robot#mousePress(int)
     */
    void mousePress(int buttons);

    /**
     * 模拟指定按钮的鼠标释放操作。
     *
     * @param buttons 按钮掩码
     *
     * @see Robot#mouseRelease(int)
     */
    void mouseRelease(int buttons);

    /**
     * 模拟鼠标滚轮操作。
     *
     * @param wheelAmt 鼠标滚轮移动的刻度数
     *
     * @see Robot#mouseWheel(int)
     */
    void mouseWheel(int wheelAmt);

    /**
     * 模拟指定键的按键操作。
     *
     * @param keycode 要按下的键码
     *
     * @see Robot#keyPress(int)
     */
    void keyPress(int keycode);

    /**
     * 模拟指定键的释放操作。
     *
     * @param keycode 要释放的键码
     *
     * @see Robot#keyRelease(int)
     */
    void keyRelease(int keycode);

    /**
     * 获取屏幕上指定像素的 RGB 值。
     *
     * @param x 屏幕上的 X 坐标
     * @param y 屏幕上的 Y 坐标
     *
     * @return 屏幕上指定像素的 RGB 值
     *
     * @see Robot#getPixelColor(int, int)
     */
    int getRGBPixel(int x, int y);

    /**
     * 获取指定屏幕区域的 RGB 值数组。
     *
     * @param bounds 要捕获 RGB 值的屏幕区域
     *
     * @return 指定屏幕区域的 RGB 值
     *
     * @see Robot#createScreenCapture(Rectangle)
     */
    int[] getRGBPixels(Rectangle bounds);

    /**
     * 当不再需要机器人对等体时，释放它。
     */
    void dispose();
}
