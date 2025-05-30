
/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.beans.ConstructorProperties;

import java.lang.annotation.Native;

/**
 * <code>BasicStroke</code> 类定义了一组基本的渲染属性，用于绘制图形原语的轮廓，这些原语使用设置了此 <code>BasicStroke</code> 的 <code>Graphics2D</code> 对象进行渲染。
 * <code>BasicStroke</code> 定义的渲染属性描述了沿 <code>Shape</code> 轮廓绘制的笔迹的形状以及路径段的端点和交点处的装饰。
 * 这些渲染属性包括：
 * <dl>
 * <dt><i>宽度</i>
 * <dd>笔宽，垂直于笔迹测量。
 * <dt><i>端点样式</i>
 * <dd>应用于未闭合子路径和虚线段的端点的装饰。从同一点开始和结束的子路径仍然被认为是未闭合的，除非它们有 CLOSE 段。
 * 有关 CLOSE 段的更多信息，请参见 {@link java.awt.geom.PathIterator#SEG_CLOSE SEG_CLOSE}。
 * 三种不同的装饰是：{@link #CAP_BUTT}、{@link #CAP_ROUND} 和 {@link #CAP_SQUARE}。
 * <dt><i>线段连接样式</i>
 * <dd>应用于两个路径段的交点以及使用 {@link java.awt.geom.PathIterator#SEG_CLOSE SEG_CLOSE} 闭合的子路径的端点的装饰。
 * 三种不同的装饰是：{@link #JOIN_BEVEL}、{@link #JOIN_MITER} 和 {@link #JOIN_ROUND}。
 * <dt><i>斜接限制</i>
 * <dd>应用于具有 JOIN_MITER 装饰的线段连接的修剪限制。当斜接长度与笔宽的比值大于斜接限制值时，线段连接会被修剪。斜接长度是从交点的内角到外角的对角线长度。
 * 两个线段形成的角越小，斜接长度越长，交点的角度越尖锐。默认的斜接限制值为 10.0f，这会导致所有小于 11 度的角被修剪。修剪斜接会将线段连接的装饰转换为斜切。
 * <dt><i>虚线属性</i>
 * <dd>通过交替使用不透明和透明部分来定义虚线模式。
 * </dl>
 * 所有指定测量和距离以控制返回轮廓形状的属性都在与原始未绘制的 <code>Shape</code> 参数相同的坐标系中测量。当 <code>Graphics2D</code> 对象在执行其 <code>draw</code> 方法之一时使用 <code>Stroke</code> 对象重新定义路径时，几何形状以原始形式提供，尚未应用 <code>Graphics2D</code> 变换属性。因此，如笔宽等属性在 <code>Graphics2D</code> 对象的用户空间坐标系中解释，并受该特定 <code>Graphics2D</code> 的用户空间到设备空间变换的缩放和剪切效果的影响。
 * 例如，渲染形状的轮廓宽度不仅由此 <code>BasicStroke</code> 的宽度属性决定，还由 <code>Graphics2D</code> 对象的变换属性决定。考虑以下代码：
 * <blockquote><tt>
 *      // 设置 Graphics2D 对象的 Transform 属性
 *      g2d.scale(10, 10);
 *      // 设置 Graphics2D 对象的 Stroke 属性
 *      g2d.setStroke(new BasicStroke(1.5f));
 * </tt></blockquote>
 * 假设 <code>Graphics2D</code> 对象没有其他缩放变换，结果线宽约为 15 像素。
 * 如示例代码所示，使用浮点线可以提供更好的精度，尤其是在使用大变换的 <code>Graphics2D</code> 对象时。
 * 当线条为对角线时，确切的宽度取决于渲染管道在追踪理论加宽轮廓时选择填充哪些像素。选择哪些像素打开会受到抗锯齿属性的影响，因为抗锯齿渲染管道可以选择着色部分覆盖的像素。
 * <p>
 * 有关用户空间坐标系和渲染过程的更多信息，请参见 <code>Graphics2D</code> 类的注释。
 * @see Graphics2D
 * @author Jim Graham
 */
public class BasicStroke implements Stroke {

    /**
     * 通过扩展路径段的外边缘直到它们相交来连接路径段。
     */
    @Native public final static int JOIN_MITER = 0;

    /**
     * 通过在角处以半线宽的半径进行圆角处理来连接路径段。
     */
    @Native public final static int JOIN_ROUND = 1;

    /**
     * 通过用直线段连接路径段的外角来连接路径段。
     */
    @Native public final static int JOIN_BEVEL = 2;

    /**
     * 未闭合的子路径和虚线段的端点不添加任何装饰。
     */
    @Native public final static int CAP_BUTT = 0;

    /**
     * 未闭合的子路径和虚线段的端点添加一个半径等于笔宽一半的圆形装饰。
     */
    @Native public final static int CAP_ROUND = 1;

    /**
     * 未闭合的子路径和虚线段的端点添加一个方形投影，投影长度等于线宽的一半。
     */
    @Native public final static int CAP_SQUARE = 2;

    float width;

    int join;
    int cap;
    float miterlimit;

    float dash[];
    float dash_phase;

    /**
     * 使用指定的属性构造一个新的 <code>BasicStroke</code>。
     * @param width 此 <code>BasicStroke</code> 的宽度。宽度必须大于或等于 0.0f。如果宽度设置为 0.0f，则笔迹将渲染为目标设备和抗锯齿提示设置下的最细线。
     * @param cap <code>BasicStroke</code> 端点的装饰。
     * @param join 路径段相交处应用的装饰。
     * @param miterlimit 修剪斜接连接的限制。斜接限制必须大于或等于 1.0f。
     * @param dash 表示虚线模式的数组。
     * @param dash_phase 开始虚线模式的偏移。
     * @throws IllegalArgumentException 如果 <code>width</code> 为负。
     * @throws IllegalArgumentException 如果 <code>cap</code> 不是 CAP_BUTT、CAP_ROUND 或 CAP_SQUARE。
     * @throws IllegalArgumentException 如果 <code>miterlimit</code> 小于 1 且 <code>join</code> 为 JOIN_MITER。
     * @throws IllegalArgumentException 如果 <code>join</code> 不是 JOIN_ROUND、JOIN_BEVEL 或 JOIN_MITER。
     * @throws IllegalArgumentException 如果 <code>dash_phase</code> 为负且 <code>dash</code> 不为 <code>null</code>。
     * @throws IllegalArgumentException 如果 <code>dash</code> 的长度为零。
     * @throws IllegalArgumentException 如果所有虚线长度都为零。
     */
    @ConstructorProperties({ "lineWidth", "endCap", "lineJoin", "miterLimit", "dashArray", "dashPhase" })
    public BasicStroke(float width, int cap, int join, float miterlimit,
                       float dash[], float dash_phase) {
        if (width < 0.0f) {
            throw new IllegalArgumentException("负宽度");
        }
        if (cap != CAP_BUTT && cap != CAP_ROUND && cap != CAP_SQUARE) {
            throw new IllegalArgumentException("非法端点值");
        }
        if (join == JOIN_MITER) {
            if (miterlimit < 1.0f) {
                throw new IllegalArgumentException("斜接限制 < 1");
            }
        } else if (join != JOIN_ROUND && join != JOIN_BEVEL) {
            throw new IllegalArgumentException("非法线段连接值");
        }
        if (dash != null) {
            if (dash_phase < 0.0f) {
                throw new IllegalArgumentException("负虚线偏移");
            }
            boolean allzero = true;
            for (int i = 0; i < dash.length; i++) {
                float d = dash[i];
                if (d > 0.0) {
                    allzero = false;
                } else if (d < 0.0) {
                    throw new IllegalArgumentException("负虚线长度");
                }
            }
            if (allzero) {
                throw new IllegalArgumentException("所有虚线长度为零");
            }
        }
        this.width      = width;
        this.cap        = cap;
        this.join       = join;
        this.miterlimit = miterlimit;
        if (dash != null) {
            this.dash = (float []) dash.clone();
        }
        this.dash_phase = dash_phase;
    }

    /**
     * 使用指定的属性构造一个实线 <code>BasicStroke</code>。
     * @param width <code>BasicStroke</code> 的宽度。
     * @param cap <code>BasicStroke</code> 端点的装饰。
     * @param join 路径段相交处应用的装饰。
     * @param miterlimit 修剪斜接连接的限制。
     * @throws IllegalArgumentException 如果 <code>width</code> 为负。
     * @throws IllegalArgumentException 如果 <code>cap</code> 不是 CAP_BUTT、CAP_ROUND 或 CAP_SQUARE。
     * @throws IllegalArgumentException 如果 <code>miterlimit</code> 小于 1 且 <code>join</code> 为 JOIN_MITER。
     * @throws IllegalArgumentException 如果 <code>join</code> 不是 JOIN_ROUND、JOIN_BEVEL 或 JOIN_MITER。
     */
    public BasicStroke(float width, int cap, int join, float miterlimit) {
        this(width, cap, join, miterlimit, null, 0.0f);
    }

    /**
     * 使用指定的属性构造一个实线 <code>BasicStroke</code>。在默认值允许或线段连接不是 JOIN_MITER 的情况下，<code>miterlimit</code> 参数是不必要的。
     * @param width <code>BasicStroke</code> 的宽度。
     * @param cap <code>BasicStroke</code> 端点的装饰。
     * @param join 路径段相交处应用的装饰。
     * @throws IllegalArgumentException 如果 <code>width</code> 为负。
     * @throws IllegalArgumentException 如果 <code>cap</code> 不是 CAP_BUTT、CAP_ROUND 或 CAP_SQUARE。
     * @throws IllegalArgumentException 如果 <code>join</code> 不是 JOIN_ROUND、JOIN_BEVEL 或 JOIN_MITER。
     */
    public BasicStroke(float width, int cap, int join) {
        this(width, cap, join, 10.0f, null, 0.0f);
    }

    /**
     * 使用指定的线宽和默认的端点和线段连接样式构造一个实线 <code>BasicStroke</code>。
     * @param width <code>BasicStroke</code> 的宽度。
     * @throws IllegalArgumentException 如果 <code>width</code> 为负。
     */
    public BasicStroke(float width) {
        this(width, CAP_SQUARE, JOIN_MITER, 10.0f, null, 0.0f);
    }

    /**
     * 使用所有属性的默认值构造一个新的 <code>BasicStroke</code>。
     * 默认属性是宽度为 1.0 的实线，CAP_SQUARE，JOIN_MITER，斜接限制为 10.0。
     */
    public BasicStroke() {
        this(1.0f, CAP_SQUARE, JOIN_MITER, 10.0f, null, 0.0f);
    }


    /**
     * 返回一个 <code>Shape</code>，其内部定义了指定 <code>Shape</code> 的轮廓。
     * @param s 要绘制轮廓的 <code>Shape</code> 边界。
     * @return 轮廓的 <code>Shape</code>。
     */
    public Shape createStrokedShape(Shape s) {
        sun.java2d.pipe.RenderingEngine re =
            sun.java2d.pipe.RenderingEngine.getInstance();
        return re.createStrokedShape(s, width, cap, join, miterlimit,
                                     dash, dash_phase);
    }

    /**
     * 返回线宽。线宽表示在用户空间中，这是 Java 2D 使用的默认坐标空间。有关用户空间坐标系的更多信息，请参见 <code>Graphics2D</code> 类的注释。
     * @return 此 <code>BasicStroke</code> 的线宽。
     * @see Graphics2D
     */
    public float getLineWidth() {
        return width;
    }

    /**
     * 返回端点样式。
     * @return 此 <code>BasicStroke</code> 的端点样式，作为定义可能的端点样式的静态 <code>int</code> 值之一。
     */
    public int getEndCap() {
        return cap;
    }

    /**
     * 返回线段连接样式。
     * @return <code>BasicStroke</code> 的线段连接样式，作为定义可能的线段连接样式的静态 <code>int</code> 值之一。
     */
    public int getLineJoin() {
        return join;
    }

    /**
     * 返回斜接连接的限制。
     * @return 此 <code>BasicStroke</code> 的斜接连接限制。
     */
    public float getMiterLimit() {
        return miterlimit;
    }

    /**
     * 返回表示虚线段长度的数组。数组中的交替项表示用户空间中虚线的不透明和透明部分的长度。
     * 当笔沿要绘制的 <code>Shape</code> 轮廓移动时，笔在用户空间中移动的距离会累积。该距离值用于索引虚线数组。
     * 当笔当前的累积距离映射到虚线数组的偶数元素时，笔是不透明的，否则是透明的。
     * @return 虚线数组。
     */
    public float[] getDashArray() {
        if (dash == null) {
            return null;
        }

        return (float[]) dash.clone();
    }

    /**
     * 返回当前的虚线偏移。虚线偏移是用户坐标系中的一个距离，表示虚线模式中的一个偏移。换句话说，虚线偏移定义了虚线模式中将对应于笔迹开始的点。
     * @return 虚线偏移，作为 <code>float</code> 值。
     */
    public float getDashPhase() {
        return dash_phase;
    }


                /**
     * 返回此描边的哈希码。
     * @return      此描边的哈希码。
     */
    public int hashCode() {
        int hash = Float.floatToIntBits(width);
        hash = hash * 31 + join;
        hash = hash * 31 + cap;
        hash = hash * 31 + Float.floatToIntBits(miterlimit);
        if (dash != null) {
            hash = hash * 31 + Float.floatToIntBits(dash_phase);
            for (int i = 0; i < dash.length; i++) {
                hash = hash * 31 + Float.floatToIntBits(dash[i]);
            }
        }
        return hash;
    }

    /**
     * 如果此 BasicStroke 表示的描边操作与给定参数相同，则返回 true。
     */
   /**
    * 测试指定对象是否等于此 <code>BasicStroke</code>
    * 通过首先测试它是否是 <code>BasicStroke</code>，然后比较其宽度、连接、端点、斜接限制、虚线和虚线相位属性
    * 与此 <code>BasicStroke</code> 的属性。
    * @param  obj 要与此 <code>BasicStroke</code> 比较的指定对象
    * @return 如果两个对象的宽度、连接、端点、斜接限制、虚线和虚线相位相同，则返回 <code>true</code>；
    *            否则返回 <code>false</code>。
    */
    public boolean equals(Object obj) {
        if (!(obj instanceof BasicStroke)) {
            return false;
        }

        BasicStroke bs = (BasicStroke) obj;
        if (width != bs.width) {
            return false;
        }

        if (join != bs.join) {
            return false;
        }

        if (cap != bs.cap) {
            return false;
        }

        if (miterlimit != bs.miterlimit) {
            return false;
        }

        if (dash != null) {
            if (dash_phase != bs.dash_phase) {
                return false;
            }

            if (!java.util.Arrays.equals(dash, bs.dash)) {
                return false;
            }
        }
        else if (bs.dash != null) {
            return false;
        }

        return true;
    }
}
