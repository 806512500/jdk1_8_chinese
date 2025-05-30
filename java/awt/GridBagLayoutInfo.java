/*
 * Copyright (c) 2006, Oracle and/or its affiliates. All rights reserved.
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
 * {@code GridBagLayoutInfo} 是一个用于 {@code GridBagLayout} 布局管理器的工具类。
 * 它存储容器中每个组件的对齐、大小和基线参数。
 * <p>
 * @see       java.awt.GridBagLayout
 * @see       java.awt.GridBagConstraints
 * @since 1.6
 */
public class GridBagLayoutInfo implements java.io.Serializable {
    /*
     * serialVersionUID
     */
    private static final long serialVersionUID = -4899416460737170217L;

    int width, height;          /* 水平和垂直的单元格数量 */
    int startx, starty;         /* 布局的起点 */
    int minWidth[];             /* 每列中最大的最小宽度 */
    int minHeight[];            /* 每行中最大的最小高度 */
    double weightX[];           /* 每列中最大的权重 */
    double weightY[];           /* 每行中最大的权重 */
    boolean hasBaseline;        /* 是否请求了基线布局并且其中一个组件具有有效的基线。 */
    // 这些只有在 hasBaseline 为 true 时才有效，并且按行索引。
    short baselineType[];       /* 某一行的基线类型。这是 BaselineResizeBehavior 常量的混合 (1 << ordinal()) */
    int maxAscent[];            /* 最大上升（基线）。 */
    int maxDescent[];           /* 最大下降（高度 - 基线）。 */

    /**
     * 创建一个表示 {@code GridBagLayout} 网格单元及其自身参数的 GridBagLayoutInfo 实例。
     * @param width 列数
     * @param height 行数
     * @since 6.0
     */
    GridBagLayoutInfo(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * 如果指定行中的任何组件具有基线调整行为为 CONSTANT_DESCENT，则返回 true。
     */
    boolean hasConstantDescent(int row) {
        return ((baselineType[row] & (1 << Component.BaselineResizeBehavior.
                                      CONSTANT_DESCENT.ordinal())) != 0);
    }

    /**
     * 如果指定行有基线，则返回 true。
     */
    boolean hasBaseline(int row) {
        return (hasBaseline && baselineType[row] != 0);
    }
}
