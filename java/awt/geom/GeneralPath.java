/*
 * Copyright (c) 1996, 2006, Oracle and/or its affiliates. All rights reserved.
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

package java.awt.geom;

import java.awt.Shape;

/**
 * {@code GeneralPath} 类表示由直线和二次、三次（Bézier）曲线构造的几何路径。它可以包含多个子路径。
 * <p>
 * {@code GeneralPath} 是一个遗留的最终类，完全实现了其超类 {@link Path2D.Float} 的行为。
 * 与 {@link Path2D.Double} 一起，{@link Path2D} 类提供了通用几何路径的完整实现，支持 {@link Shape} 和
 * {@link PathIterator} 接口的所有功能，并能够显式选择不同的内部坐标精度。
 * <p>
 * 在处理可以用浮点精度表示和使用的数据时，使用 {@code Path2D.Float}（或此遗留的 {@code GeneralPath} 子类）。
 * 对于需要双精度精度或范围的数据，使用 {@code Path2D.Double}。
 *
 * @author Jim Graham
 * @since 1.2
 */
public final class GeneralPath extends Path2D.Float {
    /**
     * 构造一个新的空单精度 {@code GeneralPath} 对象，其默认绕组规则为 {@link #WIND_NON_ZERO}。
     *
     * @since 1.2
     */
    public GeneralPath() {
        super(WIND_NON_ZERO, INIT_SIZE);
    }

    /**
     * 构造一个新的 {@code GeneralPath} 对象，使用指定的绕组规则来控制需要定义路径内部的操作。
     *
     * @param rule 绕组规则
     * @see #WIND_EVEN_ODD
     * @see #WIND_NON_ZERO
     * @since 1.2
     */
    public GeneralPath(int rule) {
        super(rule, INIT_SIZE);
    }

    /**
     * 构造一个新的 {@code GeneralPath} 对象，使用指定的绕组规则和指定的初始容量来存储路径坐标。
     * 这个数字是路径中路径段数量的初始估计，但存储会根据需要扩展以存储添加的任何路径段。
     *
     * @param rule 绕组规则
     * @param initialCapacity 路径段数量的估计值
     * @see #WIND_EVEN_ODD
     * @see #WIND_NON_ZERO
     * @since 1.2
     */
    public GeneralPath(int rule, int initialCapacity) {
        super(rule, initialCapacity);
    }

    /**
     * 从任意 {@link Shape} 对象构造一个新的 {@code GeneralPath} 对象。
     * 该路径的初始几何形状和绕组规则均来自指定的 {@code Shape} 对象。
     *
     * @param s 指定的 {@code Shape} 对象
     * @since 1.2
     */
    public GeneralPath(Shape s) {
        super(s, null);
    }

    GeneralPath(int windingRule,
                byte[] pointTypes,
                int numTypes,
                float[] pointCoords,
                int numCoords)
    {
        // 用于从本地构造

        this.windingRule = windingRule;
        this.pointTypes = pointTypes;
        this.numTypes = numTypes;
        this.floatCoords = pointCoords;
        this.numCoords = numCoords;
    }

    /*
     * JDK 1.6 serialVersionUID
     */
    private static final long serialVersionUID = -8327096662768731142L;
}
