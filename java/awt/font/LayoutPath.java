/*
 * Copyright (c) 2005, Oracle and/or its affiliates. All rights reserved.
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
/*
 * (C) Copyright IBM Corp. 2005, All Rights Reserved.
 */
package java.awt.font;

import java.awt.geom.Point2D;

/**
 * LayoutPath 提供了基线相对位置与用户空间点之间的映射。位置由基线上的前进距离和基线前进点处的垂直偏移组成。垂直方向上的正值是基线向量顺时针90度的方向。位置表示为一个 <code>Point2D</code>，其中 x 是前进距离，y 是偏移。
 *
 * @since 1.6
 */
public abstract class LayoutPath {
    /**
     * 将用户空间中的点转换为相对于路径的位置。选择该位置以最小化点到路径的距离（例如，偏移的大小将是最小的）。如果有多个这样的位置，则选择前进距离最小的位置。
     * @param point 要转换的点。如果它不是与位置相同的对象，则此调用不会修改点。
     * @param location 一个 <code>Point2D</code>，用于保存返回的位置。它可以是与点相同的对象。
     * @return 如果点与路径前部分相关联，则返回 true；如果与后部分相关联，则返回 false。如果位置不在路径的断裂或急转弯处，默认返回 true。
     * @throws NullPointerException 如果点或位置为 null
     * @since 1.6
     */
    public abstract boolean pointToPath(Point2D point, Point2D location);

    /**
     * 将相对于路径的位置转换为用户坐标中的点。路径可能在位置的前进距离处突然弯曲或不连续。如果发生这种情况，'preceding' 的值用于确定用于解释偏移的路径部分。
     * @param location 一个 <code>Point2D</code>，表示相对于路径的前进距离（在 x 中）和偏移（在 y 中）。如果位置不是与点相同的对象，则此调用不会修改位置。
     * @param preceding 如果为 true，则应使用前进距离前的部分；如果为 false，则应使用后部分。如果路径在前进距离处没有断裂或急转弯，这将不起作用。
     * @param point 一个 <code>Point2D</code>，用于保存返回的点。它可以是与位置相同的对象。
     * @throws NullPointerException 如果位置或点为 null
     * @since 1.6
     */
    public abstract void pathToPoint(Point2D location, boolean preceding,
                                     Point2D point);
}
