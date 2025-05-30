
/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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
import java.beans.ConstructorProperties;

/**
 * <code>AffineTransform</code> 类表示一个 2D 仿射变换，该变换执行从 2D 坐标到其他 2D 坐标的线性映射，保留直线的“直线性”和“平行性”。仿射变换可以使用平移、缩放、翻转、旋转和剪切的序列构建。
 * <p>
 * 这样的坐标变换可以用一个 3 行 3 列的矩阵表示，隐含的最后一行为 [0 0 1]。该矩阵通过将坐标向量视为列向量并按以下过程乘以矩阵，将源坐标 (x, y) 转换为目标坐标 (x', y')：
 * <pre>
 *      [ x']   [  m00  m01  m02  ] [ x ]   [ m00x + m01y + m02 ]
 *      [ y'] = [  m10  m11  m12  ] [ y ] = [ m10x + m11y + m12 ]
 *      [ 1 ]   [   0    0    1   ] [ 1 ]   [         1         ]
 * </pre>
 * <h3><a name="quadrantapproximation">处理 90 度旋转</a></h3>
 * <p>
 * 在 <code>AffineTransform</code> 类的某些 <code>rotate</code> 方法中，一个双精度参数指定旋转角度（以弧度为单位）。这些方法对大约 90 度（包括 180、270 和 360 度等倍数）的旋转有特殊处理，以使常见的象限旋转处理得更高效。这种特殊处理可能导致非常接近 90 度倍数的角度被视为精确的 90 度倍数。对于小的 90 度倍数，被处理为象限旋转的角度范围大约为 0.00000121 度宽。本节解释了为什么需要这种特殊处理以及它是如何实现的。
 * <p>
 * 由于 90 度在弧度中表示为 <code>PI/2</code>，而 PI 是一个超越数（因此也是无理数），因此不可能用精确的双精度值表示 90 度的倍数。结果是理论上不可能使用这些值描述象限旋转（90、180、270 或 360 度）。双精度浮点值可以非常接近非零的 <code>PI/2</code> 倍数，但永远不会接近到正弦或余弦值为 0.0、1.0 或 -1.0 的程度。<code>Math.sin()</code> 和 <code>Math.cos()</code> 的实现相应地永远不会为任何其他情况返回 0.0，除了 <code>Math.sin(0.0)</code>。然而，这些实现确实为每个 90 度倍数附近的一些数值返回 1.0 和 -1.0，因为正确答案与 1.0 或 -1.0 非常接近，双精度尾数无法准确表示差异，就像对于接近 0.0 的数值一样。
 * <p>
 * 这些问题的最终结果是，如果直接使用 <code>Math.sin()</code> 和 <code>Math.cos()</code> 方法在基于弧度的旋转操作中生成矩阵修改值，那么即使对于简单的 <code>rotate(Math.PI/2.0)</code> 情况，由于正弦和余弦值非零导致的矩阵中的微小变化，生成的变换也永远不会被严格分类为象限旋转。如果这些变换不被分类为象限旋转，那么尝试基于变换类型优化后续操作的代码将被降级为其最通用的实现。
 * <p>
 * 由于象限旋转相当常见，这个类应该能够快速处理这些情况，既包括将旋转应用于变换，也包括将结果变换应用于坐标。为了实现这种最优处理，接受以弧度为单位的旋转角度的方法尝试检测旨在成为象限旋转的角度并将其视为象限旋转。因此，如果 <code>Math.sin(<em>theta</em>)</code> 或 <code>Math.cos(<em>theta</em>)</code> 返回 1.0 或 -1.0，这些方法会将角度 <em>theta</em> 视为象限旋转。作为一个经验法则，这个属性对于大约 0.0000000211 弧度（或 0.00000121 度）范围内的小 90 度倍数成立。
 *
 * @author Jim Graham
 * @since 1.2
 */
public class AffineTransform implements Cloneable, java.io.Serializable {

    /*
     * 这个常量仅对缓存的类型字段有用。
     * 它表示类型已被取消缓存，必须重新计算。
     */
    private static final int TYPE_UNKNOWN = -1;

    /**
     * 这个常量表示此对象定义的变换是恒等变换。
     * 恒等变换是指输出坐标始终与输入坐标相同。
     * 如果此变换不是恒等变换，则类型将是常量 GENERAL_TRANSFORM 或表示此变换执行的各种坐标转换的适当标志位的组合。
     * @see #TYPE_TRANSLATION
     * @see #TYPE_UNIFORM_SCALE
     * @see #TYPE_GENERAL_SCALE
     * @see #TYPE_FLIP
     * @see #TYPE_QUADRANT_ROTATION
     * @see #TYPE_GENERAL_ROTATION
     * @see #TYPE_GENERAL_TRANSFORM
     * @see #getType
     * @since 1.2
     */
    public static final int TYPE_IDENTITY = 0;

    /**
     * 这个标志位表示此对象定义的变换除了其他标志位指示的转换外，还执行平移。
     * 平移将坐标在 x 和 y 方向上恒定地移动，而不改变向量的长度或角度。
     * @see #TYPE_IDENTITY
     * @see #TYPE_UNIFORM_SCALE
     * @see #TYPE_GENERAL_SCALE
     * @see #TYPE_FLIP
     * @see #TYPE_QUADRANT_ROTATION
     * @see #TYPE_GENERAL_ROTATION
     * @see #TYPE_GENERAL_TRANSFORM
     * @see #getType
     * @since 1.2
     */
    public static final int TYPE_TRANSLATION = 1;

    /**
     * 这个标志位表示此对象定义的变换除了其他标志位指示的转换外，还执行均匀缩放。
     * 均匀缩放将向量的长度在 x 和 y 方向上以相同的比例乘以，而不改变向量之间的角度。
     * 这个标志位与 TYPE_GENERAL_SCALE 标志位互斥。
     * @see #TYPE_IDENTITY
     * @see #TYPE_TRANSLATION
     * @see #TYPE_GENERAL_SCALE
     * @see #TYPE_FLIP
     * @see #TYPE_QUADRANT_ROTATION
     * @see #TYPE_GENERAL_ROTATION
     * @see #TYPE_GENERAL_TRANSFORM
     * @see #getType
     * @since 1.2
     */
    public static final int TYPE_UNIFORM_SCALE = 2;

    /**
     * 这个标志位表示此对象定义的变换除了其他标志位指示的转换外，还执行一般缩放。
     * 一般缩放将向量的长度在 x 和 y 方向上以不同的比例乘以，而不改变垂直向量之间的角度。
     * 这个标志位与 TYPE_UNIFORM_SCALE 标志位互斥。
     * @see #TYPE_IDENTITY
     * @see #TYPE_TRANSLATION
     * @see #TYPE_UNIFORM_SCALE
     * @see #TYPE_FLIP
     * @see #TYPE_QUADRANT_ROTATION
     * @see #TYPE_GENERAL_ROTATION
     * @see #TYPE_GENERAL_TRANSFORM
     * @see #getType
     * @since 1.2
     */
    public static final int TYPE_GENERAL_SCALE = 4;

    /**
     * 这个常量是任何缩放标志位的位掩码。
     * @see #TYPE_UNIFORM_SCALE
     * @see #TYPE_GENERAL_SCALE
     * @since 1.2
     */
    public static final int TYPE_MASK_SCALE = (TYPE_UNIFORM_SCALE |
                                               TYPE_GENERAL_SCALE);

    /**
     * 这个标志位表示此对象定义的变换除了其他标志位指示的转换外，还执行关于某个轴的镜像翻转，将通常的右手坐标系变为左手坐标系。
     * 右手坐标系是指正 X 轴逆时针旋转以覆盖正 Y 轴，类似于你盯着拇指末端时右手指的方向。
     * 左手坐标系是指正 X 轴顺时针旋转以覆盖正 Y 轴，类似于你盯着拇指末端时左手指的方向。
     * 无法通过数学方法确定原始翻转或镜像变换的角度，因为所有翻转角度在适当调整旋转后都是相同的。
     * @see #TYPE_IDENTITY
     * @see #TYPE_TRANSLATION
     * @see #TYPE_UNIFORM_SCALE
     * @see #TYPE_GENERAL_SCALE
     * @see #TYPE_QUADRANT_ROTATION
     * @see #TYPE_GENERAL_ROTATION
     * @see #TYPE_GENERAL_TRANSFORM
     * @see #getType
     * @since 1.2
     */
    public static final int TYPE_FLIP = 64;
    /* NOTE: TYPE_FLIP 是在 GENERAL_TRANSFORM 公开流通后添加的，标志位不能方便地重新编号，否则会在外部代码中引入二进制不兼容。 */

    /**
     * 这个标志位表示此对象定义的变换除了其他标志位指示的转换外，还执行某个 90 度倍数的象限旋转。
     * 旋转将向量的角度改变相同的量，无论向量的原始方向如何，并且不改变向量的长度。
     * 这个标志位与 TYPE_GENERAL_ROTATION 标志位互斥。
     * @see #TYPE_IDENTITY
     * @see #TYPE_TRANSLATION
     * @see #TYPE_UNIFORM_SCALE
     * @see #TYPE_GENERAL_SCALE
     * @see #TYPE_FLIP
     * @see #TYPE_GENERAL_ROTATION
     * @see #TYPE_GENERAL_TRANSFORM
     * @see #getType
     * @since 1.2
     */
    public static final int TYPE_QUADRANT_ROTATION = 8;

    /**
     * 这个标志位表示此对象定义的变换除了其他标志位指示的转换外，还执行任意角度的旋转。
     * 旋转将向量的角度改变相同的量，无论向量的原始方向如何，并且不改变向量的长度。
     * 这个标志位与 TYPE_QUADRANT_ROTATION 标志位互斥。
     * @see #TYPE_IDENTITY
     * @see #TYPE_TRANSLATION
     * @see #TYPE_UNIFORM_SCALE
     * @see #TYPE_GENERAL_SCALE
     * @see #TYPE_FLIP
     * @see #TYPE_QUADRANT_ROTATION
     * @see #TYPE_GENERAL_TRANSFORM
     * @see #getType
     * @since 1.2
     */
    public static final int TYPE_GENERAL_ROTATION = 16;

    /**
     * 这个常量是任何旋转标志位的位掩码。
     * @see #TYPE_QUADRANT_ROTATION
     * @see #TYPE_GENERAL_ROTATION
     * @since 1.2
     */
    public static final int TYPE_MASK_ROTATION = (TYPE_QUADRANT_ROTATION |
                                                  TYPE_GENERAL_ROTATION);

    /**
     * 这个常量表示此对象定义的变换执行输入坐标的任意转换。
     * 如果此变换可以用上述常量中的任何一个分类，类型将要么是常量 TYPE_IDENTITY，要么是表示此变换执行的各种坐标转换的适当标志位的组合。
     * @see #TYPE_IDENTITY
     * @see #TYPE_TRANSLATION
     * @see #TYPE_UNIFORM_SCALE
     * @see #TYPE_GENERAL_SCALE
     * @see #TYPE_FLIP
     * @see #TYPE_QUADRANT_ROTATION
     * @see #TYPE_GENERAL_ROTATION
     * @see #getType
     * @since 1.2
     */
    public static final int TYPE_GENERAL_TRANSFORM = 32;

    /**
     * 这个常量用于内部状态变量，表示不需要执行任何计算，只需将源坐标复制到目标位置即可完成此变换的变换方程。
     * @see #APPLY_TRANSLATE
     * @see #APPLY_SCALE
     * @see #APPLY_SHEAR
     * @see #state
     */
    static final int APPLY_IDENTITY = 0;

    /**
     * 这个常量用于内部状态变量，表示矩阵的平移分量（m02 和 m12）需要被添加以完成此变换的变换方程。
     * @see #APPLY_IDENTITY
     * @see #APPLY_SCALE
     * @see #APPLY_SHEAR
     * @see #state
     */
    static final int APPLY_TRANSLATE = 1;

    /**
     * 这个常量用于内部状态变量，表示矩阵的缩放分量（m00 和 m11）需要被考虑以完成此变换的变换方程。如果设置了 APPLY_SHEAR 位，则表示缩放分量不全为 0.0。如果未设置 APPLY_SHEAR 位，则表示缩放分量不全为 1.0。如果未设置 APPLY_SHEAR 和 APPLY_SCALE 位，则缩放分量全为 1.0，这意味着 x 和 y 分量对变换后的坐标有贡献，但它们没有被任何缩放因子乘以。
     * @see #APPLY_IDENTITY
     * @see #APPLY_TRANSLATE
     * @see #APPLY_SHEAR
     * @see #state
     */
    static final int APPLY_SCALE = 2;

    /**
     * 这个常量用于内部状态变量，表示矩阵的剪切分量（m01 和 m10）需要被考虑以完成此变换的变换方程。状态变量中存在此位会改变 APPLY_SCALE 位的解释，如其文档中所述。
     * @see #APPLY_IDENTITY
     * @see #APPLY_TRANSLATE
     * @see #APPLY_SCALE
     * @see #state
     */
    static final int APPLY_SHEAR = 4;


                /*
     * For methods which combine together the state of two separate
     * transforms and dispatch based upon the combination, these constants
     * specify how far to shift one of the states so that the two states
     * are mutually non-interfering and provide constants for testing the
     * bits of the shifted (HI) state.  The methods in this class use
     * the convention that the state of "this" transform is unshifted and
     * the state of the "other" or "argument" transform is shifted (HI).
     */
    private static final int HI_SHIFT = 3;
    private static final int HI_IDENTITY = APPLY_IDENTITY << HI_SHIFT;
    private static final int HI_TRANSLATE = APPLY_TRANSLATE << HI_SHIFT;
    private static final int HI_SCALE = APPLY_SCALE << HI_SHIFT;
    private static final int HI_SHEAR = APPLY_SHEAR << HI_SHIFT;

    /**
     * 3x3 仿射变换矩阵的 X 坐标缩放元素。
     *
     * @serial
     */
    double m00;

    /**
     * 3x3 仿射变换矩阵的 Y 坐标剪切元素。
     *
     * @serial
     */
     double m10;

    /**
     * 3x3 仿射变换矩阵的 X 坐标剪切元素。
     *
     * @serial
     */
     double m01;

    /**
     * 3x3 仿射变换矩阵的 Y 坐标缩放元素。
     *
     * @serial
     */
     double m11;

    /**
     * 3x3 仿射变换矩阵的平移元素的 X 坐标。
     *
     * @serial
     */
     double m02;

    /**
     * 3x3 仿射变换矩阵的平移元素的 Y 坐标。
     *
     * @serial
     */
     double m12;

    /**
     * 此字段跟踪在执行变换时需要应用的矩阵组件。
     * @see #APPLY_IDENTITY
     * @see #APPLY_TRANSLATE
     * @see #APPLY_SCALE
     * @see #APPLY_SHEAR
     */
    transient int state;

    /**
     * 此字段缓存矩阵的当前变换类型。
     * @see #TYPE_IDENTITY
     * @see #TYPE_TRANSLATION
     * @see #TYPE_UNIFORM_SCALE
     * @see #TYPE_GENERAL_SCALE
     * @see #TYPE_FLIP
     * @see #TYPE_QUADRANT_ROTATION
     * @see #TYPE_GENERAL_ROTATION
     * @see #TYPE_GENERAL_TRANSFORM
     * @see #TYPE_UNKNOWN
     * @see #getType
     */
    private transient int type;

    private AffineTransform(double m00, double m10,
                            double m01, double m11,
                            double m02, double m12,
                            int state) {
        this.m00 = m00;
        this.m10 = m10;
        this.m01 = m01;
        this.m11 = m11;
        this.m02 = m02;
        this.m12 = m12;
        this.state = state;
        this.type = TYPE_UNKNOWN;
    }

    /**
     * 构造一个新的表示恒等变换的 <code>AffineTransform</code>。
     * @since 1.2
     */
    public AffineTransform() {
        m00 = m11 = 1.0;
        // m01 = m10 = m02 = m12 = 0.0;         /* Not needed. */
        // state = APPLY_IDENTITY;              /* Not needed. */
        // type = TYPE_IDENTITY;                /* Not needed. */
    }

    /**
     * 构造一个新的 <code>AffineTransform</code>，它是指定的 <code>AffineTransform</code> 对象的副本。
     * @param Tx 要复制的 <code>AffineTransform</code> 对象
     * @since 1.2
     */
    public AffineTransform(AffineTransform Tx) {
        this.m00 = Tx.m00;
        this.m10 = Tx.m10;
        this.m01 = Tx.m01;
        this.m11 = Tx.m11;
        this.m02 = Tx.m02;
        this.m12 = Tx.m12;
        this.state = Tx.state;
        this.type = Tx.type;
    }

    /**
     * 从表示 3x3 变换矩阵的 6 个可指定条目的 6 个浮点值构造一个新的 <code>AffineTransform</code>。
     *
     * @param m00 3x3 矩阵的 X 坐标缩放元素
     * @param m10 3x3 矩阵的 Y 坐标剪切元素
     * @param m01 3x3 矩阵的 X 坐标剪切元素
     * @param m11 3x3 矩阵的 Y 坐标缩放元素
     * @param m02 3x3 矩阵的 X 坐标平移元素
     * @param m12 3x3 矩阵的 Y 坐标平移元素
     * @since 1.2
     */
    @ConstructorProperties({ "scaleX", "shearY", "shearX", "scaleY", "translateX", "translateY" })
    public AffineTransform(float m00, float m10,
                           float m01, float m11,
                           float m02, float m12) {
        this.m00 = m00;
        this.m10 = m10;
        this.m01 = m01;
        this.m11 = m11;
        this.m02 = m02;
        this.m12 = m12;
        updateState();
    }

    /**
     * 从表示 3x3 变换矩阵的 4 个非平移条目或 6 个可指定条目的浮点值数组构造一个新的 <code>AffineTransform</code>。
     * 数值从数组中获取为 {&nbsp;m00&nbsp;m10&nbsp;m01&nbsp;m11&nbsp;[m02&nbsp;m12]}。
     * @param flatmatrix 包含要设置在新的 <code>AffineTransform</code> 对象中的值的浮点数组。假设数组的长度至少为 4。
     * 如果数组的长度小于 6，则只取前 4 个值。如果数组的长度大于 6，则取前 6 个值。
     * @since 1.2
     */
    public AffineTransform(float[] flatmatrix) {
        m00 = flatmatrix[0];
        m10 = flatmatrix[1];
        m01 = flatmatrix[2];
        m11 = flatmatrix[3];
        if (flatmatrix.length > 5) {
            m02 = flatmatrix[4];
            m12 = flatmatrix[5];
        }
        updateState();
    }

    /**
     * 从表示 3x3 变换矩阵的 6 个可指定条目的 6 个双精度值构造一个新的 <code>AffineTransform</code>。
     *
     * @param m00 3x3 矩阵的 X 坐标缩放元素
     * @param m10 3x3 矩阵的 Y 坐标剪切元素
     * @param m01 3x3 矩阵的 X 坐标剪切元素
     * @param m11 3x3 矩阵的 Y 坐标缩放元素
     * @param m02 3x3 矩阵的 X 坐标平移元素
     * @param m12 3x3 矩阵的 Y 坐标平移元素
     * @since 1.2
     */
    public AffineTransform(double m00, double m10,
                           double m01, double m11,
                           double m02, double m12) {
        this.m00 = m00;
        this.m10 = m10;
        this.m01 = m01;
        this.m11 = m11;
        this.m02 = m02;
        this.m12 = m12;
        updateState();
    }

    /**
     * 从表示 3x3 变换矩阵的 4 个非平移条目或 6 个可指定条目的双精度值数组构造一个新的 <code>AffineTransform</code>。
     * 数值从数组中获取为 {&nbsp;m00&nbsp;m10&nbsp;m01&nbsp;m11&nbsp;[m02&nbsp;m12]}。
     * @param flatmatrix 包含要设置在新的 <code>AffineTransform</code> 对象中的值的双精度数组。假设数组的长度至少为 4。
     * 如果数组的长度小于 6，则只取前 4 个值。如果数组的长度大于 6，则取前 6 个值。
     * @since 1.2
     */
    public AffineTransform(double[] flatmatrix) {
        m00 = flatmatrix[0];
        m10 = flatmatrix[1];
        m01 = flatmatrix[2];
        m11 = flatmatrix[3];
        if (flatmatrix.length > 5) {
            m02 = flatmatrix[4];
            m12 = flatmatrix[5];
        }
        updateState();
    }

    /**
     * 返回表示平移变换的变换。
     * 表示返回的变换的矩阵是：
     * <pre>
     *          [   1    0    tx  ]
     *          [   0    1    ty  ]
     *          [   0    0    1   ]
     * </pre>
     * @param tx 坐标在 X 轴方向上平移的距离
     * @param ty 坐标在 Y 轴方向上平移的距离
     * @return 一个表示平移变换的 <code>AffineTransform</code> 对象，使用指定的向量创建。
     * @since 1.2
     */
    public static AffineTransform getTranslateInstance(double tx, double ty) {
        AffineTransform Tx = new AffineTransform();
        Tx.setToTranslation(tx, ty);
        return Tx;
    }

    /**
     * 返回表示旋转变换的变换。
     * 表示返回的变换的矩阵是：
     * <pre>
     *          [   cos(theta)    -sin(theta)    0   ]
     *          [   sin(theta)     cos(theta)    0   ]
     *          [       0              0         1   ]
     * </pre>
     * 用正角度 theta 旋转将正 X 轴上的点旋转到正 Y 轴方向。
     * 请注意关于
     * <a href="#quadrantapproximation">处理 90 度旋转</a>
     * 的讨论。
     * @param theta 以弧度为单位的旋转角度
     * @return 一个表示旋转变换的 <code>AffineTransform</code> 对象，使用指定的旋转角度创建。
     * @since 1.2
     */
    public static AffineTransform getRotateInstance(double theta) {
        AffineTransform Tx = new AffineTransform();
        Tx.setToRotation(theta);
        return Tx;
    }

    /**
     * 返回一个绕锚点旋转坐标的变换。
     * 此操作等效于将坐标平移，使锚点位于原点 (S1)，然后绕新原点旋转 (S2)，最后平移以恢复中间原点到原始锚点的坐标 (S3)。
     * <p>
     * 此操作等效于以下调用序列：
     * <pre>
     *     AffineTransform Tx = new AffineTransform();
     *     Tx.translate(anchorx, anchory);    // S3: 最终平移
     *     Tx.rotate(theta);                  // S2: 绕锚点旋转
     *     Tx.translate(-anchorx, -anchory);  // S1: 将锚点平移到原点
     * </pre>
     * 表示返回的变换的矩阵是：
     * <pre>
     *          [   cos(theta)    -sin(theta)    x-x*cos+y*sin  ]
     *          [   sin(theta)     cos(theta)    y-x*sin-y*cos  ]
     *          [       0              0               1        ]
     * </pre>
     * 用正角度 theta 旋转将正 X 轴上的点旋转到正 Y 轴方向。
     * 请注意关于
     * <a href="#quadrantapproximation">处理 90 度旋转</a>
     * 的讨论。
     *
     * @param theta 以弧度为单位的旋转角度
     * @param anchorx 旋转锚点的 X 坐标
     * @param anchory 旋转锚点的 Y 坐标
     * @return 一个绕指定点旋转坐标的 <code>AffineTransform</code> 对象，使用指定的旋转角度创建。
     * @since 1.2
     */
    public static AffineTransform getRotateInstance(double theta,
                                                    double anchorx,
                                                    double anchory)
    {
        AffineTransform Tx = new AffineTransform();
        Tx.setToRotation(theta, anchorx, anchory);
        return Tx;
    }

    /**
     * 返回一个根据旋转向量旋转坐标的变换。
     * 所有坐标都绕原点旋转相同的量。
     * 旋转量使得正 X 轴上的坐标将随后与从原点指向指定向量坐标的向量对齐。
     * 如果 <code>vecx</code> 和 <code>vecy</code> 都为 0.0，则返回恒等变换。
     * 此操作等效于调用：
     * <pre>
     *     AffineTransform.getRotateInstance(Math.atan2(vecy, vecx));
     * </pre>
     *
     * @param vecx 旋转向量的 X 坐标
     * @param vecy 旋转向量的 Y 坐标
     * @return 一个根据指定旋转向量旋转坐标的 <code>AffineTransform</code> 对象。
     * @since 1.6
     */
    public static AffineTransform getRotateInstance(double vecx, double vecy) {
        AffineTransform Tx = new AffineTransform();
        Tx.setToRotation(vecx, vecy);
        return Tx;
    }

    /**
     * 返回一个根据旋转向量绕锚点旋转坐标的变换。
     * 所有坐标都绕指定的锚点坐标旋转相同的量。
     * 旋转量使得正 X 轴上的坐标将随后与从原点指向指定向量坐标的向量对齐。
     * 如果 <code>vecx</code> 和 <code>vecy</code> 都为 0.0，则返回恒等变换。
     * 此操作等效于调用：
     * <pre>
     *     AffineTransform.getRotateInstance(Math.atan2(vecy, vecx),
     *                                       anchorx, anchory);
     * </pre>
     *
     * @param vecx 旋转向量的 X 坐标
     * @param vecy 旋转向量的 Y 坐标
     * @param anchorx 旋转锚点的 X 坐标
     * @param anchory 旋转锚点的 Y 坐标
     * @return 一个绕指定点根据指定旋转向量旋转坐标的 <code>AffineTransform</code> 对象。
     * @since 1.6
     */
    public static AffineTransform getRotateInstance(double vecx,
                                                    double vecy,
                                                    double anchorx,
                                                    double anchory)
    {
        AffineTransform Tx = new AffineTransform();
        Tx.setToRotation(vecx, vecy, anchorx, anchory);
        return Tx;
    }

    /**
     * 返回一个按指定象限数旋转坐标的变换。
     * 此操作等效于调用：
     * <pre>
     *     AffineTransform.getRotateInstance(numquadrants * Math.PI / 2.0);
     * </pre>
     * 用正象限数旋转将正 X 轴上的点旋转到正 Y 轴方向。
     * @param numquadrants 要旋转的 90 度弧的数量
     * @return 一个按指定象限数旋转坐标的 <code>AffineTransform</code> 对象。
     * @since 1.6
     */
    public static AffineTransform getQuadrantRotateInstance(int numquadrants) {
        AffineTransform Tx = new AffineTransform();
        Tx.setToQuadrantRotation(numquadrants);
        return Tx;
    }

    /**
     * 返回一个按指定象限数绕指定锚点旋转坐标的变换。
     * 此操作等效于调用：
     * <pre>
     *     AffineTransform.getRotateInstance(numquadrants * Math.PI / 2.0,
     *                                       anchorx, anchory);
     * </pre>
     * 用正象限数旋转将正 X 轴上的点旋转到正 Y 轴方向。
     *
     * @param numquadrants 要旋转的 90 度弧的数量
     * @param anchorx 旋转锚点的 X 坐标
     * @param anchory 旋转锚点的 Y 坐标
     * @return 一个按指定象限数绕指定锚点旋转坐标的 <code>AffineTransform</code> 对象。
     * @since 1.6
     */
    public static AffineTransform getQuadrantRotateInstance(int numquadrants,
                                                            double anchorx,
                                                            double anchory)
    {
        AffineTransform Tx = new AffineTransform();
        Tx.setToQuadrantRotation(numquadrants, anchorx, anchory);
        return Tx;
    }


                /**
     * 返回一个表示缩放变换的变换。
     * 该变换的矩阵表示为：
     * <pre>
     *          [   sx   0    0   ]
     *          [   0    sy   0   ]
     *          [   0    0    1   ]
     * </pre>
     * @param sx 沿X轴方向缩放坐标的因子
     * @param sy 沿Y轴方向缩放坐标的因子
     * @return 一个<code>AffineTransform</code>对象，该对象按指定因子缩放坐标。
     * @since 1.2
     */
    public static AffineTransform getScaleInstance(double sx, double sy) {
        AffineTransform Tx = new AffineTransform();
        Tx.setToScale(sx, sy);
        return Tx;
    }

    /**
     * 返回一个表示剪切变换的变换。
     * 该变换的矩阵表示为：
     * <pre>
     *          [   1   shx   0   ]
     *          [  shy   1    0   ]
     *          [   0    0    1   ]
     * </pre>
     * @param shx 沿正X轴方向按Y坐标因子移动坐标的乘数
     * @param shy 沿正Y轴方向按X坐标因子移动坐标的乘数
     * @return 一个<code>AffineTransform</code>对象，该对象按指定乘数剪切坐标。
     * @since 1.2
     */
    public static AffineTransform getShearInstance(double shx, double shy) {
        AffineTransform Tx = new AffineTransform();
        Tx.setToShear(shx, shy);
        return Tx;
    }

    /**
     * 检索描述此变换转换属性的标志位。
     * 返回值是常量TYPE_IDENTITY或TYPE_GENERAL_TRANSFORM之一，或者是适当的标志位的组合。
     * 有效的标志位组合是独占或操作，可以组合
     * TYPE_TRANSLATION标志位
     * 以及以下任一标志位：
     * TYPE_UNIFORM_SCALE或TYPE_GENERAL_SCALE
     * 以及以下任一标志位：
     * TYPE_QUADRANT_ROTATION或TYPE_GENERAL_ROTATION。
     * @return 适用于此变换的任何指示标志的或组合
     * @see #TYPE_IDENTITY
     * @see #TYPE_TRANSLATION
     * @see #TYPE_UNIFORM_SCALE
     * @see #TYPE_GENERAL_SCALE
     * @see #TYPE_QUADRANT_ROTATION
     * @see #TYPE_GENERAL_ROTATION
     * @see #TYPE_GENERAL_TRANSFORM
     * @since 1.2
     */
    public int getType() {
        if (type == TYPE_UNKNOWN) {
            calculateType();
        }
        return type;
    }

    /**
     * 这是一个实用函数，用于在标志位未被缓存时计算它们。
     * @see #getType
     */
    @SuppressWarnings("fallthrough")
    private void calculateType() {
        int ret = TYPE_IDENTITY;
        boolean sgn0, sgn1;
        double M0, M1, M2, M3;
        updateState();
        switch (state) {
        default:
            stateError();
            /* NOTREACHED */
        case (APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE):
            ret = TYPE_TRANSLATION;
            /* NOBREAK */
        case (APPLY_SHEAR | APPLY_SCALE):
            if ((M0 = m00) * (M2 = m01) + (M3 = m10) * (M1 = m11) != 0) {
                // 转换后的单位向量不垂直...
                this.type = TYPE_GENERAL_TRANSFORM;
                return;
            }
            sgn0 = (M0 >= 0.0);
            sgn1 = (M1 >= 0.0);
            if (sgn0 == sgn1) {
                // sgn(M0) == sgn(M1) 因此 sgn(M2) == -sgn(M3)
                // 这是“未翻转”（右手）状态
                if (M0 != M1 || M2 != -M3) {
                    ret |= (TYPE_GENERAL_ROTATION | TYPE_GENERAL_SCALE);
                } else if (M0 * M1 - M2 * M3 != 1.0) {
                    ret |= (TYPE_GENERAL_ROTATION | TYPE_UNIFORM_SCALE);
                } else {
                    ret |= TYPE_GENERAL_ROTATION;
                }
            } else {
                // sgn(M0) == -sgn(M1) 因此 sgn(M2) == sgn(M3)
                // 这是“翻转”（左手）状态
                if (M0 != -M1 || M2 != M3) {
                    ret |= (TYPE_GENERAL_ROTATION |
                            TYPE_FLIP |
                            TYPE_GENERAL_SCALE);
                } else if (M0 * M1 - M2 * M3 != 1.0) {
                    ret |= (TYPE_GENERAL_ROTATION |
                            TYPE_FLIP |
                            TYPE_UNIFORM_SCALE);
                } else {
                    ret |= (TYPE_GENERAL_ROTATION | TYPE_FLIP);
                }
            }
            break;
        case (APPLY_SHEAR | APPLY_TRANSLATE):
            ret = TYPE_TRANSLATION;
            /* NOBREAK */
        case (APPLY_SHEAR):
            sgn0 = ((M0 = m01) >= 0.0);
            sgn1 = ((M1 = m10) >= 0.0);
            if (sgn0 != sgn1) {
                // 符号不同 - 简单的90度旋转
                if (M0 != -M1) {
                    ret |= (TYPE_QUADRANT_ROTATION | TYPE_GENERAL_SCALE);
                } else if (M0 != 1.0 && M0 != -1.0) {
                    ret |= (TYPE_QUADRANT_ROTATION | TYPE_UNIFORM_SCALE);
                } else {
                    ret |= TYPE_QUADRANT_ROTATION;
                }
            } else {
                // 符号相同 - 90度旋转加上轴翻转
                if (M0 == M1) {
                    ret |= (TYPE_QUADRANT_ROTATION |
                            TYPE_FLIP |
                            TYPE_UNIFORM_SCALE);
                } else {
                    ret |= (TYPE_QUADRANT_ROTATION |
                            TYPE_FLIP |
                            TYPE_GENERAL_SCALE);
                }
            }
            break;
        case (APPLY_SCALE | APPLY_TRANSLATE):
            ret = TYPE_TRANSLATION;
            /* NOBREAK */
        case (APPLY_SCALE):
            sgn0 = ((M0 = m00) >= 0.0);
            sgn1 = ((M1 = m11) >= 0.0);
            if (sgn0 == sgn1) {
                if (sgn0) {
                    // 两个缩放因子非负 - 简单缩放
                    // 注意：APPLY_SCALE 意味着 M0, M1 不都是 1
                    if (M0 == M1) {
                        ret |= TYPE_UNIFORM_SCALE;
                    } else {
                        ret |= TYPE_GENERAL_SCALE;
                    }
                } else {
                    // 两个缩放因子为负 - 180度旋转
                    if (M0 != M1) {
                        ret |= (TYPE_QUADRANT_ROTATION | TYPE_GENERAL_SCALE);
                    } else if (M0 != -1.0) {
                        ret |= (TYPE_QUADRANT_ROTATION | TYPE_UNIFORM_SCALE);
                    } else {
                        ret |= TYPE_QUADRANT_ROTATION;
                    }
                }
            } else {
                // 缩放因子符号不同 - 某轴翻转
                if (M0 == -M1) {
                    if (M0 == 1.0 || M0 == -1.0) {
                        ret |= TYPE_FLIP;
                    } else {
                        ret |= (TYPE_FLIP | TYPE_UNIFORM_SCALE);
                    }
                } else {
                    ret |= (TYPE_FLIP | TYPE_GENERAL_SCALE);
                }
            }
            break;
        case (APPLY_TRANSLATE):
            ret = TYPE_TRANSLATION;
            break;
        case (APPLY_IDENTITY):
            break;
        }
        this.type = ret;
    }

    /**
     * 返回变换矩阵的行列式。
     * 行列式不仅用于确定变换是否可逆，还用于获取表示变换的X和Y缩放的单个值。
     * <p>
     * 如果行列式非零，则此变换可逆，依赖于逆变换的各种方法不需要抛出
     * {@link NoninvertibleTransformException}。
     * 如果行列式为零，则此变换不能逆，因为变换将所有输入坐标映射到一条线或一个点。
     * 如果行列式接近零，则逆变换操作可能无法携带足够的精度以产生有意义的结果。
     * <p>
     * 如果此变换表示均匀缩放，如<code>getType</code>方法所指示的，那么行列式也表示所有点从或向原点扩展或收缩的均匀缩放因子的平方。
     * 如果此变换表示非均匀缩放或更一般的变换，则行列式可能不表示任何其他用途的有用值，除了确定逆变换是否可能。
     * <p>
     * 数学上，行列式使用以下公式计算：
     * <pre>
     *          |  m00  m01  m02  |
     *          |  m10  m11  m12  |  =  m00 * m11 - m01 * m10
     *          |   0    0    1   |
     * </pre>
     *
     * @return 用于变换坐标的矩阵的行列式。
     * @see #getType
     * @see #createInverse
     * @see #inverseTransform
     * @see #TYPE_UNIFORM_SCALE
     * @since 1.2
     */
    @SuppressWarnings("fallthrough")
    public double getDeterminant() {
        switch (state) {
        default:
            stateError();
            /* NOTREACHED */
        case (APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE):
        case (APPLY_SHEAR | APPLY_SCALE):
            return m00 * m11 - m01 * m10;
        case (APPLY_SHEAR | APPLY_TRANSLATE):
        case (APPLY_SHEAR):
            return -(m01 * m10);
        case (APPLY_SCALE | APPLY_TRANSLATE):
        case (APPLY_SCALE):
            return m00 * m11;
        case (APPLY_TRANSLATE):
        case (APPLY_IDENTITY):
            return 1.0;
        }
    }

    /**
     * 当矩阵变化太大以至于无法预测其对状态的影响时，手动重新计算变换的状态。
     * 下表指定了状态字段的各种设置对相应矩阵元素字段值的含义。
     * 请注意，SCALE字段的规则取决于SHEAR标志是否也设置。
     * <pre>
     *                     SCALE            SHEAR          TRANSLATE
     *                    m00/m11          m01/m10          m02/m12
     *
     * IDENTITY             1.0              0.0              0.0
     * TRANSLATE (TR)       1.0              0.0          not both 0.0
     * SCALE (SC)       not both 1.0         0.0              0.0
     * TR | SC          not both 1.0         0.0          not both 0.0
     * SHEAR (SH)           0.0          not both 0.0         0.0
     * TR | SH              0.0          not both 0.0     not both 0.0
     * SC | SH          not both 0.0     not both 0.0         0.0
     * TR | SC | SH     not both 0.0     not both 0.0     not both 0.0
     * </pre>
     */
    void updateState() {
        if (m01 == 0.0 && m10 == 0.0) {
            if (m00 == 1.0 && m11 == 1.0) {
                if (m02 == 0.0 && m12 == 0.0) {
                    state = APPLY_IDENTITY;
                    type = TYPE_IDENTITY;
                } else {
                    state = APPLY_TRANSLATE;
                    type = TYPE_TRANSLATION;
                }
            } else {
                if (m02 == 0.0 && m12 == 0.0) {
                    state = APPLY_SCALE;
                    type = TYPE_UNKNOWN;
                } else {
                    state = (APPLY_SCALE | APPLY_TRANSLATE);
                    type = TYPE_UNKNOWN;
                }
            }
        } else {
            if (m00 == 0.0 && m11 == 0.0) {
                if (m02 == 0.0 && m12 == 0.0) {
                    state = APPLY_SHEAR;
                    type = TYPE_UNKNOWN;
                } else {
                    state = (APPLY_SHEAR | APPLY_TRANSLATE);
                    type = TYPE_UNKNOWN;
                }
            } else {
                if (m02 == 0.0 && m12 == 0.0) {
                    state = (APPLY_SHEAR | APPLY_SCALE);
                    type = TYPE_UNKNOWN;
                } else {
                    state = (APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE);
                    type = TYPE_UNKNOWN;
                }
            }
        }
    }

    /*
     * 用于在switch语句中忘记某个情况时抛出异常的内部便利方法。
     */
    private void stateError() {
        throw new InternalError("missing case in transform state switch");
    }

    /**
     * 检索3x3仿射变换矩阵中的6个可指定值，并将它们放入一个double精度值数组中。
     * 值存储在数组中，格式为
     * {&nbsp;m00&nbsp;m10&nbsp;m01&nbsp;m11&nbsp;m02&nbsp;m12&nbsp;}。
     * 也可以指定一个4个double的数组，此时只检索表示非变换部分的前四个元素，值存储在数组中，格式为
     * {&nbsp;m00&nbsp;m10&nbsp;m01&nbsp;m11&nbsp;}
     * @param flatmatrix 用于存储返回值的double数组。
     * @see #getScaleX
     * @see #getScaleY
     * @see #getShearX
     * @see #getShearY
     * @see #getTranslateX
     * @see #getTranslateY
     * @since 1.2
     */
    public void getMatrix(double[] flatmatrix) {
        flatmatrix[0] = m00;
        flatmatrix[1] = m10;
        flatmatrix[2] = m01;
        flatmatrix[3] = m11;
        if (flatmatrix.length > 5) {
            flatmatrix[4] = m02;
            flatmatrix[5] = m12;
        }
    }

    /**
     * 返回3x3仿射变换矩阵中的X坐标缩放元素（m00）。
     * @return 一个double值，表示仿射变换矩阵中的X坐标缩放元素。
     * @see #getMatrix
     * @since 1.2
     */
    public double getScaleX() {
        return m00;
    }

    /**
     * 返回3x3仿射变换矩阵中的Y坐标缩放元素（m11）。
     * @return 一个double值，表示仿射变换矩阵中的Y坐标缩放元素。
     * @see #getMatrix
     * @since 1.2
     */
    public double getScaleY() {
        return m11;
    }

    /**
     * 返回3x3仿射变换矩阵中的X坐标剪切元素（m01）。
     * @return 一个double值，表示仿射变换矩阵中的X坐标剪切元素。
     * @see #getMatrix
     * @since 1.2
     */
    public double getShearX() {
        return m01;
    }

    /**
     * 返回3x3仿射变换矩阵中的Y坐标剪切元素（m10）。
     * @return 一个double值，表示仿射变换矩阵中的Y坐标剪切元素。
     * @see #getMatrix
     * @since 1.2
     */
    public double getShearY() {
        return m10;
    }

    /**
     * 返回3x3仿射变换矩阵中的平移元素（m02）的X坐标。
     * @return 一个double值，表示仿射变换矩阵中的平移元素的X坐标。
     * @see #getMatrix
     * @since 1.2
     */
    public double getTranslateX() {
        return m02;
    }


                /**
     * 返回 3x3 仿射变换矩阵的平移元素 (m12) 的 Y 坐标。
     * @return 一个双精度值，表示仿射变换矩阵的平移元素的 Y 坐标。
     * @see #getMatrix
     * @since 1.2
     */
    public double getTranslateY() {
        return m12;
    }

    /**
     * 将此变换与平移变换连接。
     * 这相当于调用 concatenate(T)，其中 T 是一个表示如下矩阵的 <code>AffineTransform</code>：
     * <pre>
     *          [   1    0    tx  ]
     *          [   0    1    ty  ]
     *          [   0    0    1   ]
     * </pre>
     * @param tx 坐标沿 X 轴方向平移的距离
     * @param ty 坐标沿 Y 轴方向平移的距离
     * @since 1.2
     */
    public void translate(double tx, double ty) {
        switch (state) {
        default:
            stateError();
            /* NOTREACHED */
            return;
        case (APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE):
            m02 = tx * m00 + ty * m01 + m02;
            m12 = tx * m10 + ty * m11 + m12;
            if (m02 == 0.0 && m12 == 0.0) {
                state = APPLY_SHEAR | APPLY_SCALE;
                if (type != TYPE_UNKNOWN) {
                    type -= TYPE_TRANSLATION;
                }
            }
            return;
        case (APPLY_SHEAR | APPLY_SCALE):
            m02 = tx * m00 + ty * m01;
            m12 = tx * m10 + ty * m11;
            if (m02 != 0.0 || m12 != 0.0) {
                state = APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE;
                type |= TYPE_TRANSLATION;
            }
            return;
        case (APPLY_SHEAR | APPLY_TRANSLATE):
            m02 = ty * m01 + m02;
            m12 = tx * m10 + m12;
            if (m02 == 0.0 && m12 == 0.0) {
                state = APPLY_SHEAR;
                if (type != TYPE_UNKNOWN) {
                    type -= TYPE_TRANSLATION;
                }
            }
            return;
        case (APPLY_SHEAR):
            m02 = ty * m01;
            m12 = tx * m10;
            if (m02 != 0.0 || m12 != 0.0) {
                state = APPLY_SHEAR | APPLY_TRANSLATE;
                type |= TYPE_TRANSLATION;
            }
            return;
        case (APPLY_SCALE | APPLY_TRANSLATE):
            m02 = tx * m00 + m02;
            m12 = ty * m11 + m12;
            if (m02 == 0.0 && m12 == 0.0) {
                state = APPLY_SCALE;
                if (type != TYPE_UNKNOWN) {
                    type -= TYPE_TRANSLATION;
                }
            }
            return;
        case (APPLY_SCALE):
            m02 = tx * m00;
            m12 = ty * m11;
            if (m02 != 0.0 || m12 != 0.0) {
                state = APPLY_SCALE | APPLY_TRANSLATE;
                type |= TYPE_TRANSLATION;
            }
            return;
        case (APPLY_TRANSLATE):
            m02 = tx + m02;
            m12 = ty + m12;
            if (m02 == 0.0 && m12 == 0.0) {
                state = APPLY_IDENTITY;
                type = TYPE_IDENTITY;
            }
            return;
        case (APPLY_IDENTITY):
            m02 = tx;
            m12 = ty;
            if (tx != 0.0 || ty != 0.0) {
                state = APPLY_TRANSLATE;
                type = TYPE_TRANSLATION;
            }
            return;
        }
    }

    // 优化旋转方法的实用方法。
    // 这些表在可预测的象限旋转中转换标志，其中剪切和缩放值被交换和取反。
    private static final int rot90conversion[] = {
        /* IDENTITY => */        APPLY_SHEAR,
        /* TRANSLATE (TR) => */  APPLY_SHEAR | APPLY_TRANSLATE,
        /* SCALE (SC) => */      APPLY_SHEAR,
        /* SC | TR => */         APPLY_SHEAR | APPLY_TRANSLATE,
        /* SHEAR (SH) => */      APPLY_SCALE,
        /* SH | TR => */         APPLY_SCALE | APPLY_TRANSLATE,
        /* SH | SC => */         APPLY_SHEAR | APPLY_SCALE,
        /* SH | SC | TR => */    APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE,
    };
    private final void rotate90() {
        double M0 = m00;
        m00 = m01;
        m01 = -M0;
        M0 = m10;
        m10 = m11;
        m11 = -M0;
        int state = rot90conversion[this.state];
        if ((state & (APPLY_SHEAR | APPLY_SCALE)) == APPLY_SCALE &&
            m00 == 1.0 && m11 == 1.0)
        {
            state -= APPLY_SCALE;
        }
        this.state = state;
        type = TYPE_UNKNOWN;
    }
    private final void rotate180() {
        m00 = -m00;
        m11 = -m11;
        int state = this.state;
        if ((state & (APPLY_SHEAR)) != 0) {
            // 如果有剪切，则此旋转对状态没有影响。
            m01 = -m01;
            m10 = -m10;
        } else {
            // 没有剪切意味着当 m00 和 m11 被取反时，SCALE 状态可能会切换。
            if (m00 == 1.0 && m11 == 1.0) {
                this.state = state & ~APPLY_SCALE;
            } else {
                this.state = state | APPLY_SCALE;
            }
        }
        type = TYPE_UNKNOWN;
    }
    private final void rotate270() {
        double M0 = m00;
        m00 = -m01;
        m01 = M0;
        M0 = m10;
        m10 = -m11;
        m11 = M0;
        int state = rot90conversion[this.state];
        if ((state & (APPLY_SHEAR | APPLY_SCALE)) == APPLY_SCALE &&
            m00 == 1.0 && m11 == 1.0)
        {
            state -= APPLY_SCALE;
        }
        this.state = state;
        type = TYPE_UNKNOWN;
    }

    /**
     * 将此变换与旋转变换连接。
     * 这相当于调用 concatenate(R)，其中 R 是一个表示如下矩阵的 <code>AffineTransform</code>：
     * <pre>
     *          [   cos(theta)    -sin(theta)    0   ]
     *          [   sin(theta)     cos(theta)    0   ]
     *          [       0              0         1   ]
     * </pre>
     * 以正角度 theta 旋转会将正 X 轴上的点旋转到正 Y 轴。
     * 有关处理 90 度旋转的讨论，请参见上方。
     * @param theta 以弧度为单位的旋转角度
     * @since 1.2
     */
    public void rotate(double theta) {
        double sin = Math.sin(theta);
        if (sin == 1.0) {
            rotate90();
        } else if (sin == -1.0) {
            rotate270();
        } else {
            double cos = Math.cos(theta);
            if (cos == -1.0) {
                rotate180();
            } else if (cos != 1.0) {
                double M0, M1;
                M0 = m00;
                M1 = m01;
                m00 =  cos * M0 + sin * M1;
                m01 = -sin * M0 + cos * M1;
                M0 = m10;
                M1 = m11;
                m10 =  cos * M0 + sin * M1;
                m11 = -sin * M0 + cos * M1;
                updateState();
            }
        }
    }

    /**
     * 将此变换与绕锚点旋转的变换连接。
     * 此操作相当于将坐标平移，使锚点位于原点 (S1)，然后绕新原点旋转 (S2)，最后平移以将中间原点恢复到原始锚点的坐标 (S3)。
     * <p>
     * 此操作相当于以下调用序列：
     * <pre>
     *     translate(anchorx, anchory);      // S3: 最终平移
     *     rotate(theta);                    // S2: 绕锚点旋转
     *     translate(-anchorx, -anchory);    // S1: 将锚点平移到原点
     * </pre>
     * 以正角度 theta 旋转会将正 X 轴上的点旋转到正 Y 轴。
     * 有关处理 90 度旋转的讨论，请参见上方。
     *
     * @param theta 以弧度为单位的旋转角度
     * @param anchorx 旋转锚点的 X 坐标
     * @param anchory 旋转锚点的 Y 坐标
     * @since 1.2
     */
    public void rotate(double theta, double anchorx, double anchory) {
        // REMIND: 简单处理 - 以后优化
        translate(anchorx, anchory);
        rotate(theta);
        translate(-anchorx, -anchory);
    }

    /**
     * 将此变换与根据旋转向量旋转坐标的变换连接。
     * 所有坐标都绕原点旋转相同的量。
     * 旋转量使得正 X 轴上的坐标将随后与从原点指向指定向量坐标的向量对齐。
     * 如果 <code>vecx</code> 和 <code>vecy</code> 都为 0.0，则不会向此变换添加任何额外的旋转。
     * 此操作相当于调用：
     * <pre>
     *          rotate(Math.atan2(vecy, vecx));
     * </pre>
     *
     * @param vecx 旋转向量的 X 坐标
     * @param vecy 旋转向量的 Y 坐标
     * @since 1.6
     */
    public void rotate(double vecx, double vecy) {
        if (vecy == 0.0) {
            if (vecx < 0.0) {
                rotate180();
            }
            // 如果 vecx > 0.0 - 不旋转
            // 如果 vecx == 0.0 - 旋转未定义 - 视为不旋转
        } else if (vecx == 0.0) {
            if (vecy > 0.0) {
                rotate90();
            } else {  // vecy 必须 < 0.0
                rotate270();
            }
        } else {
            double len = Math.sqrt(vecx * vecx + vecy * vecy);
            double sin = vecy / len;
            double cos = vecx / len;
            double M0, M1;
            M0 = m00;
            M1 = m01;
            m00 =  cos * M0 + sin * M1;
            m01 = -sin * M0 + cos * M1;
            M0 = m10;
            M1 = m11;
            m10 =  cos * M0 + sin * M1;
            m11 = -sin * M0 + cos * M1;
            updateState();
        }
    }

    /**
     * 将此变换与根据旋转向量绕锚点旋转坐标的变换连接。
     * 所有坐标都绕指定的锚点坐标旋转相同的量。
     * 旋转量使得正 X 轴上的坐标将随后与从原点指向指定向量坐标的向量对齐。
     * 如果 <code>vecx</code> 和 <code>vecy</code> 都为 0.0，则变换不会以任何方式修改。
     * 此方法相当于调用：
     * <pre>
     *     rotate(Math.atan2(vecy, vecx), anchorx, anchory);
     * </pre>
     *
     * @param vecx 旋转向量的 X 坐标
     * @param vecy 旋转向量的 Y 坐标
     * @param anchorx 旋转锚点的 X 坐标
     * @param anchory 旋转锚点的 Y 坐标
     * @since 1.6
     */
    public void rotate(double vecx, double vecy,
                       double anchorx, double anchory)
    {
        // REMIND: 简单处理 - 以后优化
        translate(anchorx, anchory);
        rotate(vecx, vecy);
        translate(-anchorx, -anchory);
    }

    /**
     * 将此变换与按指定象限数旋转坐标的变换连接。
     * 这相当于调用：
     * <pre>
     *     rotate(numquadrants * Math.PI / 2.0);
     * </pre>
     * 以正象限数旋转会将正 X 轴上的点旋转到正 Y 轴。
     * @param numquadrants 旋转的 90 度弧数
     * @since 1.6
     */
    public void quadrantRotate(int numquadrants) {
        switch (numquadrants & 3) {
        case 0:
            break;
        case 1:
            rotate90();
            break;
        case 2:
            rotate180();
            break;
        case 3:
            rotate270();
            break;
        }
    }

    /**
     * 将此变换与按指定象限数绕指定锚点旋转坐标的变换连接。
     * 此方法相当于调用：
     * <pre>
     *     rotate(numquadrants * Math.PI / 2.0, anchorx, anchory);
     * </pre>
     * 以正象限数旋转会将正 X 轴上的点旋转到正 Y 轴。
     *
     * @param numquadrants 旋转的 90 度弧数
     * @param anchorx 旋转锚点的 X 坐标
     * @param anchory 旋转锚点的 Y 坐标
     * @since 1.6
     */
    public void quadrantRotate(int numquadrants,
                               double anchorx, double anchory)
    {
        switch (numquadrants & 3) {
        case 0:
            return;
        case 1:
            m02 += anchorx * (m00 - m01) + anchory * (m01 + m00);
            m12 += anchorx * (m10 - m11) + anchory * (m11 + m10);
            rotate90();
            break;
        case 2:
            m02 += anchorx * (m00 + m00) + anchory * (m01 + m01);
            m12 += anchorx * (m10 + m10) + anchory * (m11 + m11);
            rotate180();
            break;
        case 3:
            m02 += anchorx * (m00 + m01) + anchory * (m01 - m00);
            m12 += anchorx * (m10 + m11) + anchory * (m11 - m10);
            rotate270();
            break;
        }
        if (m02 == 0.0 && m12 == 0.0) {
            state &= ~APPLY_TRANSLATE;
        } else {
            state |= APPLY_TRANSLATE;
        }
    }

    /**
     * 将此变换与缩放变换连接。
     * 这相当于调用 concatenate(S)，其中 S 是一个表示如下矩阵的 <code>AffineTransform</code>：
     * <pre>
     *          [   sx   0    0   ]
     *          [   0    sy   0   ]
     *          [   0    0    1   ]
     * </pre>
     * @param sx 沿 X 轴方向缩放坐标的因子
     * @param sy 沿 Y 轴方向缩放坐标的因子
     * @since 1.2
     */
    @SuppressWarnings("fallthrough")
    public void scale(double sx, double sy) {
        int state = this.state;
        switch (state) {
        default:
            stateError();
            /* NOTREACHED */
        case (APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE):
        case (APPLY_SHEAR | APPLY_SCALE):
            m00 *= sx;
            m11 *= sy;
            /* NOBREAK */
        case (APPLY_SHEAR | APPLY_TRANSLATE):
        case (APPLY_SHEAR):
            m01 *= sy;
            m10 *= sx;
            if (m01 == 0 && m10 == 0) {
                state &= APPLY_TRANSLATE;
                if (m00 == 1.0 && m11 == 1.0) {
                    this.type = (state == APPLY_IDENTITY
                                 ? TYPE_IDENTITY
                                 : TYPE_TRANSLATION);
                } else {
                    state |= APPLY_SCALE;
                    this.type = TYPE_UNKNOWN;
                }
                this.state = state;
            }
            return;
        case (APPLY_SCALE | APPLY_TRANSLATE):
        case (APPLY_SCALE):
            m00 *= sx;
            m11 *= sy;
            if (m00 == 1.0 && m11 == 1.0) {
                this.state = (state &= APPLY_TRANSLATE);
                this.type = (state == APPLY_IDENTITY
                             ? TYPE_IDENTITY
                             : TYPE_TRANSLATION);
            } else {
                this.type = TYPE_UNKNOWN;
            }
            return;
        case (APPLY_TRANSLATE):
        case (APPLY_IDENTITY):
            m00 = sx;
            m11 = sy;
            if (sx != 1.0 || sy != 1.0) {
                this.state = state | APPLY_SCALE;
                this.type = TYPE_UNKNOWN;
            }
            return;
        }
    }


                /**
     * 将此变换与一个剪切变换连接。
     * 这相当于调用 concatenate(SH)，其中 SH 是一个
     * <code>AffineTransform</code>，表示如下矩阵：
     * <pre>
     *          [   1   shx   0   ]
     *          [  shy   1    0   ]
     *          [   0    0    1   ]
     * </pre>
     * @param shx 作为其 Y 坐标的因子，坐标沿正 X 轴方向移动的乘数
     * @param shy 作为其 X 坐标的因子，坐标沿正 Y 轴方向移动的乘数
     * @since 1.2
     */
    public void shear(double shx, double shy) {
        int state = this.state;
        switch (state) {
        default:
            stateError();
            /* NOTREACHED */
            return;
        case (APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE):
        case (APPLY_SHEAR | APPLY_SCALE):
            double M0, M1;
            M0 = m00;
            M1 = m01;
            m00 = M0 + M1 * shy;
            m01 = M0 * shx + M1;

            M0 = m10;
            M1 = m11;
            m10 = M0 + M1 * shy;
            m11 = M0 * shx + M1;
            updateState();
            return;
        case (APPLY_SHEAR | APPLY_TRANSLATE):
        case (APPLY_SHEAR):
            m00 = m01 * shy;
            m11 = m10 * shx;
            if (m00 != 0.0 || m11 != 0.0) {
                this.state = state | APPLY_SCALE;
            }
            this.type = TYPE_UNKNOWN;
            return;
        case (APPLY_SCALE | APPLY_TRANSLATE):
        case (APPLY_SCALE):
            m01 = m00 * shx;
            m10 = m11 * shy;
            if (m01 != 0.0 || m10 != 0.0) {
                this.state = state | APPLY_SHEAR;
            }
            this.type = TYPE_UNKNOWN;
            return;
        case (APPLY_TRANSLATE):
        case (APPLY_IDENTITY):
            m01 = shx;
            m10 = shy;
            if (m01 != 0.0 || m10 != 0.0) {
                this.state = state | APPLY_SCALE | APPLY_SHEAR;
                this.type = TYPE_UNKNOWN;
            }
            return;
        }
    }

    /**
     * 重置此变换为单位变换。
     * @since 1.2
     */
    public void setToIdentity() {
        m00 = m11 = 1.0;
        m10 = m01 = m02 = m12 = 0.0;
        state = APPLY_IDENTITY;
        type = TYPE_IDENTITY;
    }

    /**
     * 将此变换设置为平移变换。
     * 表示此变换的矩阵变为：
     * <pre>
     *          [   1    0    tx  ]
     *          [   0    1    ty  ]
     *          [   0    0    1   ]
     * </pre>
     * @param tx 坐标沿 X 轴方向平移的距离
     * @param ty 坐标沿 Y 轴方向平移的距离
     * @since 1.2
     */
    public void setToTranslation(double tx, double ty) {
        m00 = 1.0;
        m10 = 0.0;
        m01 = 0.0;
        m11 = 1.0;
        m02 = tx;
        m12 = ty;
        if (tx != 0.0 || ty != 0.0) {
            state = APPLY_TRANSLATE;
            type = TYPE_TRANSLATION;
        } else {
            state = APPLY_IDENTITY;
            type = TYPE_IDENTITY;
        }
    }

    /**
     * 将此变换设置为旋转变换。
     * 表示此变换的矩阵变为：
     * <pre>
     *          [   cos(theta)    -sin(theta)    0   ]
     *          [   sin(theta)     cos(theta)    0   ]
     *          [       0              0         1   ]
     * </pre>
     * 以正角度 theta 旋转将正 X 轴上的点旋转到正 Y 轴。
     * 有关处理 90 度旋转的讨论，请参见上方的
     * <a href="#quadrantapproximation">处理 90 度旋转</a>。
     * @param theta 以弧度为单位的旋转角度
     * @since 1.2
     */
    public void setToRotation(double theta) {
        double sin = Math.sin(theta);
        double cos;
        if (sin == 1.0 || sin == -1.0) {
            cos = 0.0;
            state = APPLY_SHEAR;
            type = TYPE_QUADRANT_ROTATION;
        } else {
            cos = Math.cos(theta);
            if (cos == -1.0) {
                sin = 0.0;
                state = APPLY_SCALE;
                type = TYPE_QUADRANT_ROTATION;
            } else if (cos == 1.0) {
                sin = 0.0;
                state = APPLY_IDENTITY;
                type = TYPE_IDENTITY;
            } else {
                state = APPLY_SHEAR | APPLY_SCALE;
                type = TYPE_GENERAL_ROTATION;
            }
        }
        m00 =  cos;
        m10 =  sin;
        m01 = -sin;
        m11 =  cos;
        m02 =  0.0;
        m12 =  0.0;
    }

    /**
     * 将此变换设置为带有平移的旋转变换。
     * 此操作相当于将坐标平移，使锚点位于原点 (S1)，然后绕新原点旋转 (S2)，最后平移以将中间原点恢复到原始锚点的坐标 (S3)。
     * <p>
     * 此操作相当于以下调用序列：
     * <pre>
     *     setToTranslation(anchorx, anchory); // S3: 最终平移
     *     rotate(theta);                      // S2: 绕锚点旋转
     *     translate(-anchorx, -anchory);      // S1: 将锚点平移到原点
     * </pre>
     * 表示此变换的矩阵变为：
     * <pre>
     *          [   cos(theta)    -sin(theta)    x-x*cos+y*sin  ]
     *          [   sin(theta)     cos(theta)    y-x*sin-y*cos  ]
     *          [       0              0               1        ]
     * </pre>
     * 以正角度 theta 旋转将正 X 轴上的点旋转到正 Y 轴。
     * 有关处理 90 度旋转的讨论，请参见上方的
     * <a href="#quadrantapproximation">处理 90 度旋转</a>。
     *
     * @param theta 以弧度为单位的旋转角度
     * @param anchorx 旋转锚点的 X 坐标
     * @param anchory 旋转锚点的 Y 坐标
     * @since 1.2
     */
    public void setToRotation(double theta, double anchorx, double anchory) {
        setToRotation(theta);
        double sin = m10;
        double oneMinusCos = 1.0 - m00;
        m02 = anchorx * oneMinusCos + anchory * sin;
        m12 = anchory * oneMinusCos - anchorx * sin;
        if (m02 != 0.0 || m12 != 0.0) {
            state |= APPLY_TRANSLATE;
            type |= TYPE_TRANSLATION;
        }
    }

    /**
     * 将此变换设置为根据旋转向量旋转坐标的旋转变换。
     * 所有坐标都绕原点旋转相同的角度。
     * 旋转的角度使得正 X 轴上的坐标将与从原点指向指定向量坐标的向量对齐。
     * 如果 <code>vecx</code> 和 <code>vecy</code> 都为 0.0，则将变换设置为单位变换。
     * 此操作相当于调用：
     * <pre>
     *     setToRotation(Math.atan2(vecy, vecx));
     * </pre>
     *
     * @param vecx 旋转向量的 X 坐标
     * @param vecy 旋转向量的 Y 坐标
     * @since 1.6
     */
    public void setToRotation(double vecx, double vecy) {
        double sin, cos;
        if (vecy == 0) {
            sin = 0.0;
            if (vecx < 0.0) {
                cos = -1.0;
                state = APPLY_SCALE;
                type = TYPE_QUADRANT_ROTATION;
            } else {
                cos = 1.0;
                state = APPLY_IDENTITY;
                type = TYPE_IDENTITY;
            }
        } else if (vecx == 0) {
            cos = 0.0;
            sin = (vecy > 0.0) ? 1.0 : -1.0;
            state = APPLY_SHEAR;
            type = TYPE_QUADRANT_ROTATION;
        } else {
            double len = Math.sqrt(vecx * vecx + vecy * vecy);
            cos = vecx / len;
            sin = vecy / len;
            state = APPLY_SHEAR | APPLY_SCALE;
            type = TYPE_GENERAL_ROTATION;
        }
        m00 =  cos;
        m10 =  sin;
        m01 = -sin;
        m11 =  cos;
        m02 =  0.0;
        m12 =  0.0;
    }

    /**
     * 将此变换设置为根据旋转向量绕指定锚点旋转坐标的旋转变换。
     * 所有坐标都绕指定的锚点坐标旋转相同的角度。
     * 旋转的角度使得正 X 轴上的坐标将与从原点指向指定向量坐标的向量对齐。
     * 如果 <code>vecx</code> 和 <code>vecy</code> 都为 0.0，则将变换设置为单位变换。
     * 此操作相当于调用：
     * <pre>
     *     setToTranslation(Math.atan2(vecy, vecx), anchorx, anchory);
     * </pre>
     *
     * @param vecx 旋转向量的 X 坐标
     * @param vecy 旋转向量的 Y 坐标
     * @param anchorx 旋转锚点的 X 坐标
     * @param anchory 旋转锚点的 Y 坐标
     * @since 1.6
     */
    public void setToRotation(double vecx, double vecy,
                              double anchorx, double anchory)
    {
        setToRotation(vecx, vecy);
        double sin = m10;
        double oneMinusCos = 1.0 - m00;
        m02 = anchorx * oneMinusCos + anchory * sin;
        m12 = anchory * oneMinusCos - anchorx * sin;
        if (m02 != 0.0 || m12 != 0.0) {
            state |= APPLY_TRANSLATE;
            type |= TYPE_TRANSLATION;
        }
    }

    /**
     * 将此变换设置为按指定的象限数旋转坐标的旋转变换。
     * 此操作相当于调用：
     * <pre>
     *     setToRotation(numquadrants * Math.PI / 2.0);
     * </pre>
     * 以正数象限数旋转将正 X 轴上的点旋转到正 Y 轴。
     * @param numquadrants 要旋转的 90 度弧的数量
     * @since 1.6
     */
    public void setToQuadrantRotation(int numquadrants) {
        switch (numquadrants & 3) {
        case 0:
            m00 =  1.0;
            m10 =  0.0;
            m01 =  0.0;
            m11 =  1.0;
            m02 =  0.0;
            m12 =  0.0;
            state = APPLY_IDENTITY;
            type = TYPE_IDENTITY;
            break;
        case 1:
            m00 =  0.0;
            m10 =  1.0;
            m01 = -1.0;
            m11 =  0.0;
            m02 =  0.0;
            m12 =  0.0;
            state = APPLY_SHEAR;
            type = TYPE_QUADRANT_ROTATION;
            break;
        case 2:
            m00 = -1.0;
            m10 =  0.0;
            m01 =  0.0;
            m11 = -1.0;
            m02 =  0.0;
            m12 =  0.0;
            state = APPLY_SCALE;
            type = TYPE_QUADRANT_ROTATION;
            break;
        case 3:
            m00 =  0.0;
            m10 = -1.0;
            m01 =  1.0;
            m11 =  0.0;
            m02 =  0.0;
            m12 =  0.0;
            state = APPLY_SHEAR;
            type = TYPE_QUADRANT_ROTATION;
            break;
        }
    }

    /**
     * 将此变换设置为按指定的象限数绕指定锚点旋转坐标的旋转变换。
     * 此操作相当于调用：
     * <pre>
     *     setToRotation(numquadrants * Math.PI / 2.0, anchorx, anchory);
     * </pre>
     * 以正数象限数旋转将正 X 轴上的点旋转到正 Y 轴。
     *
     * @param numquadrants 要旋转的 90 度弧的数量
     * @param anchorx 旋转锚点的 X 坐标
     * @param anchory 旋转锚点的 Y 坐标
     * @since 1.6
     */
    public void setToQuadrantRotation(int numquadrants,
                                      double anchorx, double anchory)
    {
        switch (numquadrants & 3) {
        case 0:
            m00 =  1.0;
            m10 =  0.0;
            m01 =  0.0;
            m11 =  1.0;
            m02 =  0.0;
            m12 =  0.0;
            state = APPLY_IDENTITY;
            type = TYPE_IDENTITY;
            break;
        case 1:
            m00 =  0.0;
            m10 =  1.0;
            m01 = -1.0;
            m11 =  0.0;
            m02 =  anchorx + anchory;
            m12 =  anchory - anchorx;
            if (m02 == 0.0 && m12 == 0.0) {
                state = APPLY_SHEAR;
                type = TYPE_QUADRANT_ROTATION;
            } else {
                state = APPLY_SHEAR | APPLY_TRANSLATE;
                type = TYPE_QUADRANT_ROTATION | TYPE_TRANSLATION;
            }
            break;
        case 2:
            m00 = -1.0;
            m10 =  0.0;
            m01 =  0.0;
            m11 = -1.0;
            m02 =  anchorx + anchorx;
            m12 =  anchory + anchory;
            if (m02 == 0.0 && m12 == 0.0) {
                state = APPLY_SCALE;
                type = TYPE_QUADRANT_ROTATION;
            } else {
                state = APPLY_SCALE | APPLY_TRANSLATE;
                type = TYPE_QUADRANT_ROTATION | TYPE_TRANSLATION;
            }
            break;
        case 3:
            m00 =  0.0;
            m10 = -1.0;
            m01 =  1.0;
            m11 =  0.0;
            m02 =  anchorx - anchory;
            m12 =  anchory + anchorx;
            if (m02 == 0.0 && m12 == 0.0) {
                state = APPLY_SHEAR;
                type = TYPE_QUADRANT_ROTATION;
            } else {
                state = APPLY_SHEAR | APPLY_TRANSLATE;
                type = TYPE_QUADRANT_ROTATION | TYPE_TRANSLATION;
            }
            break;
        }
    }

    /**
     * 将此变换设置为缩放变换。
     * 表示此变换的矩阵变为：
     * <pre>
     *          [   sx   0    0   ]
     *          [   0    sy   0   ]
     *          [   0    0    1   ]
     * </pre>
     * @param sx 沿 X 轴方向缩放坐标的因子
     * @param sy 沿 Y 轴方向缩放坐标的因子
     * @since 1.2
     */
    public void setToScale(double sx, double sy) {
        m00 = sx;
        m10 = 0.0;
        m01 = 0.0;
        m11 = sy;
        m02 = 0.0;
        m12 = 0.0;
        if (sx != 1.0 || sy != 1.0) {
            state = APPLY_SCALE;
            type = TYPE_UNKNOWN;
        } else {
            state = APPLY_IDENTITY;
            type = TYPE_IDENTITY;
        }
    }

    /**
     * 将此变换设置为剪切变换。
     * 表示此变换的矩阵变为：
     * <pre>
     *          [   1   shx   0   ]
     *          [  shy   1    0   ]
     *          [   0    0    1   ]
     * </pre>
     * @param shx 作为其 Y 坐标的因子，坐标沿正 X 轴方向移动的乘数
     * @param shy 作为其 X 坐标的因子，坐标沿正 Y 轴方向移动的乘数
     * @since 1.2
     */
    public void setToShear(double shx, double shy) {
        m00 = 1.0;
        m01 = shx;
        m10 = shy;
        m11 = 1.0;
        m02 = 0.0;
        m12 = 0.0;
        if (shx != 0.0 || shy != 0.0) {
            state = (APPLY_SHEAR | APPLY_SCALE);
            type = TYPE_UNKNOWN;
        } else {
            state = APPLY_IDENTITY;
            type = TYPE_IDENTITY;
        }
    }


                /**
     * 将此变换设置为指定的 <code>AffineTransform</code> 对象中的变换的副本。
     * @param Tx 要从中复制变换的 <code>AffineTransform</code> 对象
     * @since 1.2
     */
    public void setTransform(AffineTransform Tx) {
        this.m00 = Tx.m00;
        this.m10 = Tx.m10;
        this.m01 = Tx.m01;
        this.m11 = Tx.m11;
        this.m02 = Tx.m02;
        this.m12 = Tx.m12;
        this.state = Tx.state;
        this.type = Tx.type;
    }

    /**
     * 将此变换设置为由 6 个双精度值指定的矩阵。
     *
     * @param m00 3x3 矩阵的 X 坐标缩放元素
     * @param m10 3x3 矩阵的 Y 坐标剪切元素
     * @param m01 3x3 矩阵的 X 坐标剪切元素
     * @param m11 3x3 矩阵的 Y 坐标缩放元素
     * @param m02 3x3 矩阵的 X 坐标平移元素
     * @param m12 3x3 矩阵的 Y 坐标平移元素
     * @since 1.2
     */
    public void setTransform(double m00, double m10,
                             double m01, double m11,
                             double m02, double m12) {
        this.m00 = m00;
        this.m10 = m10;
        this.m01 = m01;
        this.m11 = m11;
        this.m02 = m02;
        this.m12 = m12;
        updateState();
    }

    /**
     * 以最常用的方式将 <code>AffineTransform</code> <code>Tx</code> 连接到此
     * <code>AffineTransform</code> Cx，以提供一个新的用户空间，该空间通过 <code>Tx</code>
     * 映射到以前的用户空间。Cx 被更新以执行组合变换。
     * 将点 p 通过更新后的变换 Cx' 变换等同于先将 p 通过 <code>Tx</code> 变换，然后
     * 将结果通过原始变换 Cx 变换，如下所示：
     * Cx'(p) = Cx(Tx(p))
     * 以矩阵表示，如果此变换 Cx 由矩阵 [this] 表示，<code>Tx</code> 由矩阵 [Tx] 表示，
     * 则此方法执行以下操作：
     * <pre>
     *          [this] = [this] x [Tx]
     * </pre>
     * @param Tx 要与此 <code>AffineTransform</code> 对象连接的 <code>AffineTransform</code> 对象。
     * @see #preConcatenate
     * @since 1.2
     */
    @SuppressWarnings("fallthrough")
    public void concatenate(AffineTransform Tx) {
        double M0, M1;
        double T00, T01, T10, T11;
        double T02, T12;
        int mystate = state;
        int txstate = Tx.state;
        switch ((txstate << HI_SHIFT) | mystate) {

            /* ---------- Tx == IDENTITY cases ---------- */
        case (HI_IDENTITY | APPLY_IDENTITY):
        case (HI_IDENTITY | APPLY_TRANSLATE):
        case (HI_IDENTITY | APPLY_SCALE):
        case (HI_IDENTITY | APPLY_SCALE | APPLY_TRANSLATE):
        case (HI_IDENTITY | APPLY_SHEAR):
        case (HI_IDENTITY | APPLY_SHEAR | APPLY_TRANSLATE):
        case (HI_IDENTITY | APPLY_SHEAR | APPLY_SCALE):
        case (HI_IDENTITY | APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE):
            return;

            /* ---------- this == IDENTITY cases ---------- */
        case (HI_SHEAR | HI_SCALE | HI_TRANSLATE | APPLY_IDENTITY):
            m01 = Tx.m01;
            m10 = Tx.m10;
            /* NOBREAK */
        case (HI_SCALE | HI_TRANSLATE | APPLY_IDENTITY):
            m00 = Tx.m00;
            m11 = Tx.m11;
            /* NOBREAK */
        case (HI_TRANSLATE | APPLY_IDENTITY):
            m02 = Tx.m02;
            m12 = Tx.m12;
            state = txstate;
            type = Tx.type;
            return;
        case (HI_SHEAR | HI_SCALE | APPLY_IDENTITY):
            m01 = Tx.m01;
            m10 = Tx.m10;
            /* NOBREAK */
        case (HI_SCALE | APPLY_IDENTITY):
            m00 = Tx.m00;
            m11 = Tx.m11;
            state = txstate;
            type = Tx.type;
            return;
        case (HI_SHEAR | HI_TRANSLATE | APPLY_IDENTITY):
            m02 = Tx.m02;
            m12 = Tx.m12;
            /* NOBREAK */
        case (HI_SHEAR | APPLY_IDENTITY):
            m01 = Tx.m01;
            m10 = Tx.m10;
            m00 = m11 = 0.0;
            state = txstate;
            type = Tx.type;
            return;

            /* ---------- Tx == TRANSLATE cases ---------- */
        case (HI_TRANSLATE | APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE):
        case (HI_TRANSLATE | APPLY_SHEAR | APPLY_SCALE):
        case (HI_TRANSLATE | APPLY_SHEAR | APPLY_TRANSLATE):
        case (HI_TRANSLATE | APPLY_SHEAR):
        case (HI_TRANSLATE | APPLY_SCALE | APPLY_TRANSLATE):
        case (HI_TRANSLATE | APPLY_SCALE):
        case (HI_TRANSLATE | APPLY_TRANSLATE):
            translate(Tx.m02, Tx.m12);
            return;

            /* ---------- Tx == SCALE cases ---------- */
        case (HI_SCALE | APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE):
        case (HI_SCALE | APPLY_SHEAR | APPLY_SCALE):
        case (HI_SCALE | APPLY_SHEAR | APPLY_TRANSLATE):
        case (HI_SCALE | APPLY_SHEAR):
        case (HI_SCALE | APPLY_SCALE | APPLY_TRANSLATE):
        case (HI_SCALE | APPLY_SCALE):
        case (HI_SCALE | APPLY_TRANSLATE):
            scale(Tx.m00, Tx.m11);
            return;

            /* ---------- Tx == SHEAR cases ---------- */
        case (HI_SHEAR | APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE):
        case (HI_SHEAR | APPLY_SHEAR | APPLY_SCALE):
            T01 = Tx.m01; T10 = Tx.m10;
            M0 = m00;
            m00 = m01 * T10;
            m01 = M0 * T01;
            M0 = m10;
            m10 = m11 * T10;
            m11 = M0 * T01;
            type = TYPE_UNKNOWN;
            return;
        case (HI_SHEAR | APPLY_SHEAR | APPLY_TRANSLATE):
        case (HI_SHEAR | APPLY_SHEAR):
            m00 = m01 * Tx.m10;
            m01 = 0.0;
            m11 = m10 * Tx.m01;
            m10 = 0.0;
            state = mystate ^ (APPLY_SHEAR | APPLY_SCALE);
            type = TYPE_UNKNOWN;
            return;
        case (HI_SHEAR | APPLY_SCALE | APPLY_TRANSLATE):
        case (HI_SHEAR | APPLY_SCALE):
            m01 = m00 * Tx.m01;
            m00 = 0.0;
            m10 = m11 * Tx.m10;
            m11 = 0.0;
            state = mystate ^ (APPLY_SHEAR | APPLY_SCALE);
            type = TYPE_UNKNOWN;
            return;
        case (HI_SHEAR | APPLY_TRANSLATE):
            m00 = 0.0;
            m01 = Tx.m01;
            m10 = Tx.m10;
            m11 = 0.0;
            state = APPLY_TRANSLATE | APPLY_SHEAR;
            type = TYPE_UNKNOWN;
            return;
        }
        // 如果 Tx 具有多个属性，则不值得优化所有这些情况...
        T00 = Tx.m00; T01 = Tx.m01; T02 = Tx.m02;
        T10 = Tx.m10; T11 = Tx.m11; T12 = Tx.m12;
        switch (mystate) {
        default:
            stateError();
            /* NOTREACHED */
        case (APPLY_SHEAR | APPLY_SCALE):
            state = mystate | txstate;
            /* NOBREAK */
        case (APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE):
            M0 = m00;
            M1 = m01;
            m00  = T00 * M0 + T10 * M1;
            m01  = T01 * M0 + T11 * M1;
            m02 += T02 * M0 + T12 * M1;

            M0 = m10;
            M1 = m11;
            m10  = T00 * M0 + T10 * M1;
            m11  = T01 * M0 + T11 * M1;
            m12 += T02 * M0 + T12 * M1;
            type = TYPE_UNKNOWN;
            return;

        case (APPLY_SHEAR | APPLY_TRANSLATE):
        case (APPLY_SHEAR):
            M0 = m01;
            m00  = T10 * M0;
            m01  = T11 * M0;
            m02 += T12 * M0;

            M0 = m10;
            m10  = T00 * M0;
            m11  = T01 * M0;
            m12 += T02 * M0;
            break;

        case (APPLY_SCALE | APPLY_TRANSLATE):
        case (APPLY_SCALE):
            M0 = m00;
            m00  = T00 * M0;
            m01  = T01 * M0;
            m02 += T02 * M0;

            M0 = m11;
            m10  = T10 * M0;
            m11  = T11 * M0;
            m12 += T12 * M0;
            break;

        case (APPLY_TRANSLATE):
            m00  = T00;
            m01  = T01;
            m02 += T02;

            m10  = T10;
            m11  = T11;
            m12 += T12;
            state = txstate | APPLY_TRANSLATE;
            type = TYPE_UNKNOWN;
            return;
        }
        updateState();
    }

    /**
     * 以较少常用的方式将 <code>AffineTransform</code> <code>Tx</code> 连接到此
     * <code>AffineTransform</code> Cx，使得 <code>Tx</code> 修改相对于绝对像素
     * 空间的坐标变换，而不是相对于现有的用户空间。
     * Cx 被更新以执行组合变换。
     * 将点 p 通过更新后的变换 Cx' 变换等同于先将 p 通过原始变换
     * Cx 变换，然后将结果通过 <code>Tx</code> 变换，如下所示：
     * Cx'(p) = Tx(Cx(p))
     * 以矩阵表示，如果此变换 Cx 由矩阵 [this] 表示，<code>Tx</code> 由矩阵 [Tx] 表示，
     * 则此方法执行以下操作：
     * <pre>
     *          [this] = [Tx] x [this]
     * </pre>
     * @param Tx 要与此 <code>AffineTransform</code> 对象连接的 <code>AffineTransform</code> 对象。
     * @see #concatenate
     * @since 1.2
     */
    @SuppressWarnings("fallthrough")
    public void preConcatenate(AffineTransform Tx) {
        double M0, M1;
        double T00, T01, T10, T11;
        double T02, T12;
        int mystate = state;
        int txstate = Tx.state;
        switch ((txstate << HI_SHIFT) | mystate) {
        case (HI_IDENTITY | APPLY_IDENTITY):
        case (HI_IDENTITY | APPLY_TRANSLATE):
        case (HI_IDENTITY | APPLY_SCALE):
        case (HI_IDENTITY | APPLY_SCALE | APPLY_TRANSLATE):
        case (HI_IDENTITY | APPLY_SHEAR):
        case (HI_IDENTITY | APPLY_SHEAR | APPLY_TRANSLATE):
        case (HI_IDENTITY | APPLY_SHEAR | APPLY_SCALE):
        case (HI_IDENTITY | APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE):
            // Tx 是 IDENTITY...
            return;

        case (HI_TRANSLATE | APPLY_IDENTITY):
        case (HI_TRANSLATE | APPLY_SCALE):
        case (HI_TRANSLATE | APPLY_SHEAR):
        case (HI_TRANSLATE | APPLY_SHEAR | APPLY_SCALE):
            // Tx 是 TRANSLATE，此对象没有 TRANSLATE
            m02 = Tx.m02;
            m12 = Tx.m12;
            state = mystate | APPLY_TRANSLATE;
            type |= TYPE_TRANSLATION;
            return;

        case (HI_TRANSLATE | APPLY_TRANSLATE):
        case (HI_TRANSLATE | APPLY_SCALE | APPLY_TRANSLATE):
        case (HI_TRANSLATE | APPLY_SHEAR | APPLY_TRANSLATE):
        case (HI_TRANSLATE | APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE):
            // Tx 是 TRANSLATE，此对象也有一个
            m02 = m02 + Tx.m02;
            m12 = m12 + Tx.m12;
            return;

        case (HI_SCALE | APPLY_TRANSLATE):
        case (HI_SCALE | APPLY_IDENTITY):
            // 只有这两个现有状态需要新的状态
            state = mystate | APPLY_SCALE;
            /* NOBREAK */
        case (HI_SCALE | APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE):
        case (HI_SCALE | APPLY_SHEAR | APPLY_SCALE):
        case (HI_SCALE | APPLY_SHEAR | APPLY_TRANSLATE):
        case (HI_SCALE | APPLY_SHEAR):
        case (HI_SCALE | APPLY_SCALE | APPLY_TRANSLATE):
        case (HI_SCALE | APPLY_SCALE):
            // Tx 是 SCALE，此对象是任何状态
            T00 = Tx.m00;
            T11 = Tx.m11;
            if ((mystate & APPLY_SHEAR) != 0) {
                m01 = m01 * T00;
                m10 = m10 * T11;
                if ((mystate & APPLY_SCALE) != 0) {
                    m00 = m00 * T00;
                    m11 = m11 * T11;
                }
            } else {
                m00 = m00 * T00;
                m11 = m11 * T11;
            }
            if ((mystate & APPLY_TRANSLATE) != 0) {
                m02 = m02 * T00;
                m12 = m12 * T11;
            }
            type = TYPE_UNKNOWN;
            return;
        case (HI_SHEAR | APPLY_SHEAR | APPLY_TRANSLATE):
        case (HI_SHEAR | APPLY_SHEAR):
            mystate = mystate | APPLY_SCALE;
            /* NOBREAK */
        case (HI_SHEAR | APPLY_TRANSLATE):
        case (HI_SHEAR | APPLY_IDENTITY):
        case (HI_SHEAR | APPLY_SCALE | APPLY_TRANSLATE):
        case (HI_SHEAR | APPLY_SCALE):
            state = mystate ^ APPLY_SHEAR;
            /* NOBREAK */
        case (HI_SHEAR | APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE):
        case (HI_SHEAR | APPLY_SHEAR | APPLY_SCALE):
            // Tx 是 SHEAR，此对象是任何状态
            T01 = Tx.m01;
            T10 = Tx.m10;

            M0 = m00;
            m00 = m10 * T01;
            m10 = M0 * T10;

            M0 = m01;
            m01 = m11 * T01;
            m11 = M0 * T10;

            M0 = m02;
            m02 = m12 * T01;
            m12 = M0 * T10;
            type = TYPE_UNKNOWN;
            return;
        }
        // 如果 Tx 具有多个属性，则不值得优化所有这些情况...
        T00 = Tx.m00; T01 = Tx.m01; T02 = Tx.m02;
        T10 = Tx.m10; T11 = Tx.m11; T12 = Tx.m12;
        switch (mystate) {
        default:
            stateError();
            /* NOTREACHED */
        case (APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE):
            M0 = m02;
            M1 = m12;
            T02 += M0 * T00 + M1 * T01;
            T12 += M0 * T10 + M1 * T11;

            /* NOBREAK */
        case (APPLY_SHEAR | APPLY_SCALE):
            m02 = T02;
            m12 = T12;

            M0 = m00;
            M1 = m10;
            m00 = M0 * T00 + M1 * T01;
            m10 = M0 * T10 + M1 * T11;

            M0 = m01;
            M1 = m11;
            m01 = M0 * T00 + M1 * T01;
            m11 = M0 * T10 + M1 * T11;
            break;

        case (APPLY_SHEAR | APPLY_TRANSLATE):
            M0 = m02;
            M1 = m12;
            T02 += M0 * T00 + M1 * T01;
            T12 += M0 * T10 + M1 * T11;

            /* NOBREAK */
        case (APPLY_SHEAR):
            m02 = T02;
            m12 = T12;

            M0 = m10;
            m00 = M0 * T01;
            m10 = M0 * T11;

            M0 = m01;
            m01 = M0 * T00;
            m11 = M0 * T10;
            break;

        case (APPLY_SCALE | APPLY_TRANSLATE):
            M0 = m02;
            M1 = m12;
            T02 += M0 * T00 + M1 * T01;
            T12 += M0 * T10 + M1 * T11;

            /* NOBREAK */
        case (APPLY_SCALE):
            m02 = T02;
            m12 = T12;

            M0 = m00;
            m00 = M0 * T00;
            m10 = M0 * T10;

            M0 = m11;
            m01 = M0 * T01;
            m11 = M0 * T11;
            break;

        case (APPLY_TRANSLATE):
            M0 = m02;
            M1 = m12;
            T02 += M0 * T00 + M1 * T01;
            T12 += M0 * T10 + M1 * T11;

            /* NOBREAK */
        case (APPLY_IDENTITY):
            m02 = T02;
            m12 = T12;

            m00 = T00;
            m10 = T10;

            m01 = T01;
            m11 = T11;

            state = mystate | txstate;
            type = TYPE_UNKNOWN;
            return;
        }
        updateState();
    }


                /**
     * 返回一个表示逆变换的 <code>AffineTransform</code> 对象。
     * 该逆变换 Tx' 将由 Tx 变换的坐标映射回其原始坐标。
     * 换句话说，Tx'(Tx(p)) = p = Tx(Tx'(p))。
     * <p>
     * 如果此变换将所有坐标映射到一个点或一条线上，则它将没有逆变换，因为不在目标点或线上的坐标将没有逆映射。
     * 可以使用 <code>getDeterminant</code> 方法来确定此变换是否有逆变换，如果没有逆变换，调用 <code>createInverse</code> 方法时将抛出异常。
     * @return 一个新的 <code>AffineTransform</code> 对象，表示逆变换。
     * @see #getDeterminant
     * @exception NoninvertibleTransformException
     * 如果矩阵不能被逆变换。
     * @since 1.2
     */
    public AffineTransform createInverse()
        throws NoninvertibleTransformException
    {
        double det;
        switch (state) {
        default:
            stateError();
            /* NOTREACHED */
            return null;
        case (APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE):
            det = m00 * m11 - m01 * m10;
            if (Math.abs(det) <= Double.MIN_VALUE) {
                throw new NoninvertibleTransformException("Determinant is "+
                                                          det);
            }
            return new AffineTransform( m11 / det, -m10 / det,
                                       -m01 / det,  m00 / det,
                                       (m01 * m12 - m11 * m02) / det,
                                       (m10 * m02 - m00 * m12) / det,
                                       (APPLY_SHEAR |
                                        APPLY_SCALE |
                                        APPLY_TRANSLATE));
        case (APPLY_SHEAR | APPLY_SCALE):
            det = m00 * m11 - m01 * m10;
            if (Math.abs(det) <= Double.MIN_VALUE) {
                throw new NoninvertibleTransformException("Determinant is "+
                                                          det);
            }
            return new AffineTransform( m11 / det, -m10 / det,
                                       -m01 / det,  m00 / det,
                                        0.0,        0.0,
                                       (APPLY_SHEAR | APPLY_SCALE));
        case (APPLY_SHEAR | APPLY_TRANSLATE):
            if (m01 == 0.0 || m10 == 0.0) {
                throw new NoninvertibleTransformException("Determinant is 0");
            }
            return new AffineTransform( 0.0,        1.0 / m01,
                                        1.0 / m10,  0.0,
                                       -m12 / m10, -m02 / m01,
                                       (APPLY_SHEAR | APPLY_TRANSLATE));
        case (APPLY_SHEAR):
            if (m01 == 0.0 || m10 == 0.0) {
                throw new NoninvertibleTransformException("Determinant is 0");
            }
            return new AffineTransform(0.0,       1.0 / m01,
                                       1.0 / m10, 0.0,
                                       0.0,       0.0,
                                       (APPLY_SHEAR));
        case (APPLY_SCALE | APPLY_TRANSLATE):
            if (m00 == 0.0 || m11 == 0.0) {
                throw new NoninvertibleTransformException("Determinant is 0");
            }
            return new AffineTransform( 1.0 / m00,  0.0,
                                        0.0,        1.0 / m11,
                                       -m02 / m00, -m12 / m11,
                                       (APPLY_SCALE | APPLY_TRANSLATE));
        case (APPLY_SCALE):
            if (m00 == 0.0 || m11 == 0.0) {
                throw new NoninvertibleTransformException("Determinant is 0");
            }
            return new AffineTransform(1.0 / m00, 0.0,
                                       0.0,       1.0 / m11,
                                       0.0,       0.0,
                                       (APPLY_SCALE));
        case (APPLY_TRANSLATE):
            return new AffineTransform( 1.0,  0.0,
                                        0.0,  1.0,
                                       -m02, -m12,
                                       (APPLY_TRANSLATE));
        case (APPLY_IDENTITY):
            return new AffineTransform();
        }

        /* NOTREACHED */
    }

    /**
     * 将此变换设置为其自身的逆变换。
     * 该逆变换 Tx' 将由 Tx 变换的坐标映射回其原始坐标。
     * 换句话说，Tx'(Tx(p)) = p = Tx(Tx'(p))。
     * <p>
     * 如果此变换将所有坐标映射到一个点或一条线上，则它将没有逆变换，因为不在目标点或线上的坐标将没有逆映射。
     * 可以使用 <code>getDeterminant</code> 方法来确定此变换是否有逆变换，如果没有逆变换，调用 <code>invert</code> 方法时将抛出异常。
     * @see #getDeterminant
     * @exception NoninvertibleTransformException
     * 如果矩阵不能被逆变换。
     * @since 1.6
     */
    public void invert()
        throws NoninvertibleTransformException
    {
        double M00, M01, M02;
        double M10, M11, M12;
        double det;
        switch (state) {
        default:
            stateError();
            /* NOTREACHED */
            return;
        case (APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE):
            M00 = m00; M01 = m01; M02 = m02;
            M10 = m10; M11 = m11; M12 = m12;
            det = M00 * M11 - M01 * M10;
            if (Math.abs(det) <= Double.MIN_VALUE) {
                throw new NoninvertibleTransformException("Determinant is "+
                                                          det);
            }
            m00 =  M11 / det;
            m10 = -M10 / det;
            m01 = -M01 / det;
            m11 =  M00 / det;
            m02 = (M01 * M12 - M11 * M02) / det;
            m12 = (M10 * M02 - M00 * M12) / det;
            break;
        case (APPLY_SHEAR | APPLY_SCALE):
            M00 = m00; M01 = m01;
            M10 = m10; M11 = m11;
            det = M00 * M11 - M01 * M10;
            if (Math.abs(det) <= Double.MIN_VALUE) {
                throw new NoninvertibleTransformException("Determinant is "+
                                                          det);
            }
            m00 =  M11 / det;
            m10 = -M10 / det;
            m01 = -M01 / det;
            m11 =  M00 / det;
            // m02 = 0.0;
            // m12 = 0.0;
            break;
        case (APPLY_SHEAR | APPLY_TRANSLATE):
            M01 = m01; M02 = m02;
            M10 = m10; M12 = m12;
            if (M01 == 0.0 || M10 == 0.0) {
                throw new NoninvertibleTransformException("Determinant is 0");
            }
            // m00 = 0.0;
            m10 = 1.0 / M01;
            m01 = 1.0 / M10;
            // m11 = 0.0;
            m02 = -M12 / M10;
            m12 = -M02 / M01;
            break;
        case (APPLY_SHEAR):
            M01 = m01;
            M10 = m10;
            if (M01 == 0.0 || M10 == 0.0) {
                throw new NoninvertibleTransformException("Determinant is 0");
            }
            // m00 = 0.0;
            m10 = 1.0 / M01;
            m01 = 1.0 / M10;
            // m11 = 0.0;
            // m02 = 0.0;
            // m12 = 0.0;
            break;
        case (APPLY_SCALE | APPLY_TRANSLATE):
            M00 = m00; M02 = m02;
            M11 = m11; M12 = m12;
            if (M00 == 0.0 || M11 == 0.0) {
                throw new NoninvertibleTransformException("Determinant is 0");
            }
            m00 = 1.0 / M00;
            // m10 = 0.0;
            // m01 = 0.0;
            m11 = 1.0 / M11;
            m02 = -M02 / M00;
            m12 = -M12 / M11;
            break;
        case (APPLY_SCALE):
            M00 = m00;
            M11 = m11;
            if (M00 == 0.0 || M11 == 0.0) {
                throw new NoninvertibleTransformException("Determinant is 0");
            }
            m00 = 1.0 / M00;
            // m10 = 0.0;
            // m01 = 0.0;
            m11 = 1.0 / M11;
            // m02 = 0.0;
            // m12 = 0.0;
            break;
        case (APPLY_TRANSLATE):
            // m00 = 1.0;
            // m10 = 0.0;
            // m01 = 0.0;
            // m11 = 1.0;
            m02 = -m02;
            m12 = -m12;
            break;
        case (APPLY_IDENTITY):
            // m00 = 1.0;
            // m10 = 0.0;
            // m01 = 0.0;
            // m11 = 1.0;
            // m02 = 0.0;
            // m12 = 0.0;
            break;
        }
    }

    /**
     * 通过此变换将指定的 <code>ptSrc</code> 变换，并将结果存储在 <code>ptDst</code> 中。
     * 如果 <code>ptDst</code> 为 <code>null</code>，则分配一个新的 {@link Point2D} 对象，并将变换结果存储在该对象中。
     * 无论哪种情况，<code>ptDst</code> 都会返回，以便于使用。
     * 如果 <code>ptSrc</code> 和 <code>ptDst</code> 是同一个对象，则输入点将正确地被变换后的点覆盖。
     * @param ptSrc 要变换的指定 <code>Point2D</code>
     * @param ptDst 存储变换后的 <code>ptSrc</code> 的指定 <code>Point2D</code>
     * @return 变换 <code>ptSrc</code> 并将结果存储在 <code>ptDst</code> 中后的 <code>ptDst</code>。
     * @since 1.2
     */
    public Point2D transform(Point2D ptSrc, Point2D ptDst) {
        if (ptDst == null) {
            if (ptSrc instanceof Point2D.Double) {
                ptDst = new Point2D.Double();
            } else {
                ptDst = new Point2D.Float();
            }
        }
        // 如果 src == dst，则将源坐标复制到局部变量中
        double x = ptSrc.getX();
        double y = ptSrc.getY();
        switch (state) {
        default:
            stateError();
            /* NOTREACHED */
            return null;
        case (APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE):
            ptDst.setLocation(x * m00 + y * m01 + m02,
                              x * m10 + y * m11 + m12);
            return ptDst;
        case (APPLY_SHEAR | APPLY_SCALE):
            ptDst.setLocation(x * m00 + y * m01, x * m10 + y * m11);
            return ptDst;
        case (APPLY_SHEAR | APPLY_TRANSLATE):
            ptDst.setLocation(y * m01 + m02, x * m10 + m12);
            return ptDst;
        case (APPLY_SHEAR):
            ptDst.setLocation(y * m01, x * m10);
            return ptDst;
        case (APPLY_SCALE | APPLY_TRANSLATE):
            ptDst.setLocation(x * m00 + m02, y * m11 + m12);
            return ptDst;
        case (APPLY_SCALE):
            ptDst.setLocation(x * m00, y * m11);
            return ptDst;
        case (APPLY_TRANSLATE):
            ptDst.setLocation(x + m02, y + m12);
            return ptDst;
        case (APPLY_IDENTITY):
            ptDst.setLocation(x, y);
            return ptDst;
        }

        /* NOTREACHED */
    }

    /**
     * 通过此变换将点对象数组进行变换。
     * 如果 <code>ptDst</code> 数组中的任何元素为 <code>null</code>，则分配一个新的 <code>Point2D</code> 对象并存储到该元素中，然后再存储变换结果。
     * <p>
     * 请注意，此方法不会采取任何预防措施来避免将结果存储到将用于后续源数组计算的 <code>Point2D</code> 对象中。
     * 但是，此方法保证，如果一个指定的 <code>Point2D</code> 对象既是单个点变换操作的源又是目标，则结果不会在计算完成之前被存储，以避免将结果覆盖在操作数上。
     * 但是，如果一个操作的目标 <code>Point2D</code> 对象是另一个操作的源 <code>Point2D</code> 对象，则该点中的原始坐标会在转换之前被覆盖。
     * @param ptSrc 包含源点对象的数组
     * @param ptDst 返回变换后的点对象的数组
     * @param srcOff 源数组中要变换的第一个点对象的偏移量
     * @param dstOff 目标数组中存储第一个变换点对象的位置的偏移量
     * @param numPts 要变换的点对象的数量
     * @since 1.2
     */
    public void transform(Point2D[] ptSrc, int srcOff,
                          Point2D[] ptDst, int dstOff,
                          int numPts) {
        int state = this.state;
        while (--numPts >= 0) {
            // 如果 src == dst，则将源坐标复制到局部变量中
            Point2D src = ptSrc[srcOff++];
            double x = src.getX();
            double y = src.getY();
            Point2D dst = ptDst[dstOff++];
            if (dst == null) {
                if (src instanceof Point2D.Double) {
                    dst = new Point2D.Double();
                } else {
                    dst = new Point2D.Float();
                }
                ptDst[dstOff - 1] = dst;
            }
            switch (state) {
            default:
                stateError();
                /* NOTREACHED */
                return;
            case (APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE):
                dst.setLocation(x * m00 + y * m01 + m02,
                                x * m10 + y * m11 + m12);
                break;
            case (APPLY_SHEAR | APPLY_SCALE):
                dst.setLocation(x * m00 + y * m01, x * m10 + y * m11);
                break;
            case (APPLY_SHEAR | APPLY_TRANSLATE):
                dst.setLocation(y * m01 + m02, x * m10 + m12);
                break;
            case (APPLY_SHEAR):
                dst.setLocation(y * m01, x * m10);
                break;
            case (APPLY_SCALE | APPLY_TRANSLATE):
                dst.setLocation(x * m00 + m02, y * m11 + m12);
                break;
            case (APPLY_SCALE):
                dst.setLocation(x * m00, y * m11);
                break;
            case (APPLY_TRANSLATE):
                dst.setLocation(x + m02, y + m12);
                break;
            case (APPLY_IDENTITY):
                dst.setLocation(x, y);
                break;
            }
        }

        /* NOTREACHED */
    }

    /**
     * 通过此变换将浮点坐标数组进行变换。
     * 两个坐标数组部分可以完全相同，也可以是同一数组的部分重叠，而不会影响结果的有效性。
     * 此方法确保在它们可以被变换之前，不会被前一个操作的结果覆盖。
     * 坐标存储在数组中，从指定的偏移量开始，顺序为 <code>[x0, y0, x1, y1, ..., xn, yn]</code>。
     * @param srcPts 包含源点坐标的数组。每个点存储为一对 x, y 坐标。
     * @param dstPts 存储变换后的点坐标的数组。每个点存储为一对 x, y 坐标。
     * @param srcOff 源数组中要变换的第一个点的偏移量
     * @param dstOff 目标数组中存储第一个变换点的位置的偏移量
     * @param numPts 要变换的点的数量
     * @since 1.2
     */
    public void transform(float[] srcPts, int srcOff,
                          float[] dstPts, int dstOff,
                          int numPts) {
        double M00, M01, M02, M10, M11, M12;    // 用于缓存
        if (dstPts == srcPts &&
            dstOff > srcOff && dstOff < srcOff + numPts * 2)
        {
            // 如果数组部分重叠且目标位置高于源位置，正常变换坐标会导致覆盖后续的源坐标。
            // 为了解决这个问题，我们使用 arraycopy 将点复制到它们的最终位置，确保正确的覆盖处理，然后在新的、更安全的位置进行变换。
            System.arraycopy(srcPts, srcOff, dstPts, dstOff, numPts * 2);
            // srcPts = dstPts;         // 它们已知是相等的。
            srcOff = dstOff;
        }
        switch (state) {
        default:
            stateError();
            /* NOTREACHED */
            return;
        case (APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE):
            M00 = m00; M01 = m01; M02 = m02;
            M10 = m10; M11 = m11; M12 = m12;
            while (--numPts >= 0) {
                double x = srcPts[srcOff++];
                double y = srcPts[srcOff++];
                dstPts[dstOff++] = (float) (M00 * x + M01 * y + M02);
                dstPts[dstOff++] = (float) (M10 * x + M11 * y + M12);
            }
            return;
        case (APPLY_SHEAR | APPLY_SCALE):
            M00 = m00; M01 = m01;
            M10 = m10; M11 = m11;
            while (--numPts >= 0) {
                double x = srcPts[srcOff++];
                double y = srcPts[srcOff++];
                dstPts[dstOff++] = (float) (M00 * x + M01 * y);
                dstPts[dstOff++] = (float) (M10 * x + M11 * y);
            }
            return;
        case (APPLY_SHEAR | APPLY_TRANSLATE):
            M01 = m01; M02 = m02;
            M10 = m10; M12 = m12;
            while (--numPts >= 0) {
                double x = srcPts[srcOff++];
                dstPts[dstOff++] = (float) (M01 * srcPts[srcOff++] + M02);
                dstPts[dstOff++] = (float) (M10 * x + M12);
            }
            return;
        case (APPLY_SHEAR):
            M01 = m01; M10 = m10;
            while (--numPts >= 0) {
                double x = srcPts[srcOff++];
                dstPts[dstOff++] = (float) (M01 * srcPts[srcOff++]);
                dstPts[dstOff++] = (float) (M10 * x);
            }
            return;
        case (APPLY_SCALE | APPLY_TRANSLATE):
            M00 = m00; M02 = m02;
            M11 = m11; M12 = m12;
            while (--numPts >= 0) {
                dstPts[dstOff++] = (float) (M00 * srcPts[srcOff++] + M02);
                dstPts[dstOff++] = (float) (M11 * srcPts[srcOff++] + M12);
            }
            return;
        case (APPLY_SCALE):
            M00 = m00; M11 = m11;
            while (--numPts >= 0) {
                dstPts[dstOff++] = (float) (M00 * srcPts[srcOff++]);
                dstPts[dstOff++] = (float) (M11 * srcPts[srcOff++]);
            }
            return;
        case (APPLY_TRANSLATE):
            M02 = m02; M12 = m12;
            while (--numPts >= 0) {
                dstPts[dstOff++] = (float) (srcPts[srcOff++] + M02);
                dstPts[dstOff++] = (float) (srcPts[srcOff++] + M12);
            }
            return;
        case (APPLY_IDENTITY):
            if (srcPts != dstPts || srcOff != dstOff) {
                System.arraycopy(srcPts, srcOff, dstPts, dstOff,
                                 numPts * 2);
            }
            return;
        }

        /* NOTREACHED */
    }


                    /* NOTREACHED */
    }

    /**
     * 通过此变换转换双精度坐标数组。
     * 两个坐标数组部分可以完全相同，也可以是同一数组中的部分重叠，而不会影响结果的有效性。
     * 此方法确保在它们可以被转换之前，不会被先前的操作覆盖任何源坐标。
     * 坐标以 <code>[x0, y0, x1, y1, ..., xn, yn]</code> 的顺序存储在数组中，从指定的偏移量开始。
     * @param srcPts 包含源点坐标的数组。每个点存储为一对 x,&nbsp;y 坐标。
     * @param dstPts 用于返回转换后的点坐标的数组。每个点存储为一对 x,&nbsp;y 坐标。
     * @param srcOff 源数组中第一个要转换的点的偏移量
     * @param dstOff 目标数组中第一个转换点的存储位置的偏移量
     * @param numPts 要转换的点对象的数量
     * @since 1.2
     */
    public void transform(double[] srcPts, int srcOff,
                          double[] dstPts, int dstOff,
                          int numPts) {
        double M00, M01, M02, M10, M11, M12;    // 用于缓存
        if (dstPts == srcPts &&
            dstOff > srcOff && dstOff < srcOff + numPts * 2)
        {
            // 如果数组部分重叠且目标位置高于源位置，我们按正常方式转换坐标
            // 会覆盖一些后续的源坐标。
            // 为了解决这个问题，我们使用 arraycopy 将点复制到它们的最终目标位置，
            // 并正确处理覆盖，然后在新的更安全的位置就地转换它们。
            System.arraycopy(srcPts, srcOff, dstPts, dstOff, numPts * 2);
            // srcPts = dstPts;         // 它们已知是相等的。
            srcOff = dstOff;
        }
        switch (state) {
        default:
            stateError();
            /* NOTREACHED */
            return;
        case (APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE):
            M00 = m00; M01 = m01; M02 = m02;
            M10 = m10; M11 = m11; M12 = m12;
            while (--numPts >= 0) {
                double x = srcPts[srcOff++];
                double y = srcPts[srcOff++];
                dstPts[dstOff++] = M00 * x + M01 * y + M02;
                dstPts[dstOff++] = M10 * x + M11 * y + M12;
            }
            return;
        case (APPLY_SHEAR | APPLY_SCALE):
            M00 = m00; M01 = m01;
            M10 = m10; M11 = m11;
            while (--numPts >= 0) {
                double x = srcPts[srcOff++];
                double y = srcPts[srcOff++];
                dstPts[dstOff++] = M00 * x + M01 * y;
                dstPts[dstOff++] = M10 * x + M11 * y;
            }
            return;
        case (APPLY_SHEAR | APPLY_TRANSLATE):
            M01 = m01; M02 = m02;
            M10 = m10; M12 = m12;
            while (--numPts >= 0) {
                double x = srcPts[srcOff++];
                dstPts[dstOff++] = M01 * srcPts[srcOff++] + M02;
                dstPts[dstOff++] = M10 * x + M12;
            }
            return;
        case (APPLY_SHEAR):
            M01 = m01; M10 = m10;
            while (--numPts >= 0) {
                double x = srcPts[srcOff++];
                dstPts[dstOff++] = M01 * srcPts[srcOff++];
                dstPts[dstOff++] = M10 * x;
            }
            return;
        case (APPLY_SCALE | APPLY_TRANSLATE):
            M00 = m00; M02 = m02;
            M11 = m11; M12 = m12;
            while (--numPts >= 0) {
                dstPts[dstOff++] = M00 * srcPts[srcOff++] + M02;
                dstPts[dstOff++] = M11 * srcPts[srcOff++] + M12;
            }
            return;
        case (APPLY_SCALE):
            M00 = m00; M11 = m11;
            while (--numPts >= 0) {
                dstPts[dstOff++] = M00 * srcPts[srcOff++];
                dstPts[dstOff++] = M11 * srcPts[srcOff++];
            }
            return;
        case (APPLY_TRANSLATE):
            M02 = m02; M12 = m12;
            while (--numPts >= 0) {
                dstPts[dstOff++] = srcPts[srcOff++] + M02;
                dstPts[dstOff++] = srcPts[srcOff++] + M12;
            }
            return;
        case (APPLY_IDENTITY):
            if (srcPts != dstPts || srcOff != dstOff) {
                System.arraycopy(srcPts, srcOff, dstPts, dstOff,
                                 numPts * 2);
            }
            return;
        }

        /* NOTREACHED */
    }

    /**
     * 通过此变换转换浮点坐标数组，并将结果存储在双精度数组中。
     * 坐标以 <code>[x0, y0, x1, y1, ..., xn, yn]</code> 的顺序存储在数组中，从指定的偏移量开始。
     * @param srcPts 包含源点坐标的数组。每个点存储为一对 x,&nbsp;y 坐标。
     * @param dstPts 用于返回转换后的点坐标的数组。每个点存储为一对 x,&nbsp;y 坐标。
     * @param srcOff 源数组中第一个要转换的点的偏移量
     * @param dstOff 目标数组中第一个转换点的存储位置的偏移量
     * @param numPts 要转换的点的数量
     * @since 1.2
     */
    public void transform(float[] srcPts, int srcOff,
                          double[] dstPts, int dstOff,
                          int numPts) {
        double M00, M01, M02, M10, M11, M12;    // 用于缓存
        switch (state) {
        default:
            stateError();
            /* NOTREACHED */
            return;
        case (APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE):
            M00 = m00; M01 = m01; M02 = m02;
            M10 = m10; M11 = m11; M12 = m12;
            while (--numPts >= 0) {
                double x = srcPts[srcOff++];
                double y = srcPts[srcOff++];
                dstPts[dstOff++] = M00 * x + M01 * y + M02;
                dstPts[dstOff++] = M10 * x + M11 * y + M12;
            }
            return;
        case (APPLY_SHEAR | APPLY_SCALE):
            M00 = m00; M01 = m01;
            M10 = m10; M11 = m11;
            while (--numPts >= 0) {
                double x = srcPts[srcOff++];
                double y = srcPts[srcOff++];
                dstPts[dstOff++] = M00 * x + M01 * y;
                dstPts[dstOff++] = M10 * x + M11 * y;
            }
            return;
        case (APPLY_SHEAR | APPLY_TRANSLATE):
            M01 = m01; M02 = m02;
            M10 = m10; M12 = m12;
            while (--numPts >= 0) {
                double x = srcPts[srcOff++];
                dstPts[dstOff++] = M01 * srcPts[srcOff++] + M02;
                dstPts[dstOff++] = M10 * x + M12;
            }
            return;
        case (APPLY_SHEAR):
            M01 = m01; M10 = m10;
            while (--numPts >= 0) {
                double x = srcPts[srcOff++];
                dstPts[dstOff++] = M01 * srcPts[srcOff++];
                dstPts[dstOff++] = M10 * x;
            }
            return;
        case (APPLY_SCALE | APPLY_TRANSLATE):
            M00 = m00; M02 = m02;
            M11 = m11; M12 = m12;
            while (--numPts >= 0) {
                dstPts[dstOff++] = M00 * srcPts[srcOff++] + M02;
                dstPts[dstOff++] = M11 * srcPts[srcOff++] + M12;
            }
            return;
        case (APPLY_SCALE):
            M00 = m00; M11 = m11;
            while (--numPts >= 0) {
                dstPts[dstOff++] = M00 * srcPts[srcOff++];
                dstPts[dstOff++] = M11 * srcPts[srcOff++];
            }
            return;
        case (APPLY_TRANSLATE):
            M02 = m02; M12 = m12;
            while (--numPts >= 0) {
                dstPts[dstOff++] = srcPts[srcOff++] + M02;
                dstPts[dstOff++] = srcPts[srcOff++] + M12;
            }
            return;
        case (APPLY_IDENTITY):
            while (--numPts >= 0) {
                dstPts[dstOff++] = srcPts[srcOff++];
                dstPts[dstOff++] = srcPts[srcOff++];
            }
            return;
        }

        /* NOTREACHED */
    }

    /**
     * 通过此变换转换双精度坐标数组，并将结果存储在浮点数组中。
     * 坐标以 <code>[x0, y0, x1, y1, ..., xn, yn]</code> 的顺序存储在数组中，从指定的偏移量开始。
     * @param srcPts 包含源点坐标的数组。每个点存储为一对 x,&nbsp;y 坐标。
     * @param dstPts 用于返回转换后的点坐标的数组。每个点存储为一对 x,&nbsp;y 坐标。
     * @param srcOff 源数组中第一个要转换的点的偏移量
     * @param dstOff 目标数组中第一个转换点的存储位置的偏移量
     * @param numPts 要转换的点对象的数量
     * @since 1.2
     */
    public void transform(double[] srcPts, int srcOff,
                          float[] dstPts, int dstOff,
                          int numPts) {
        double M00, M01, M02, M10, M11, M12;    // 用于缓存
        switch (state) {
        default:
            stateError();
            /* NOTREACHED */
            return;
        case (APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE):
            M00 = m00; M01 = m01; M02 = m02;
            M10 = m10; M11 = m11; M12 = m12;
            while (--numPts >= 0) {
                double x = srcPts[srcOff++];
                double y = srcPts[srcOff++];
                dstPts[dstOff++] = (float) (M00 * x + M01 * y + M02);
                dstPts[dstOff++] = (float) (M10 * x + M11 * y + M12);
            }
            return;
        case (APPLY_SHEAR | APPLY_SCALE):
            M00 = m00; M01 = m01;
            M10 = m10; M11 = m11;
            while (--numPts >= 0) {
                double x = srcPts[srcOff++];
                double y = srcPts[srcOff++];
                dstPts[dstOff++] = (float) (M00 * x + M01 * y);
                dstPts[dstOff++] = (float) (M10 * x + M11 * y);
            }
            return;
        case (APPLY_SHEAR | APPLY_TRANSLATE):
            M01 = m01; M02 = m02;
            M10 = m10; M12 = m12;
            while (--numPts >= 0) {
                double x = srcPts[srcOff++];
                dstPts[dstOff++] = (float) (M01 * srcPts[srcOff++] + M02);
                dstPts[dstOff++] = (float) (M10 * x + M12);
            }
            return;
        case (APPLY_SHEAR):
            M01 = m01; M10 = m10;
            while (--numPts >= 0) {
                double x = srcPts[srcOff++];
                dstPts[dstOff++] = (float) (M01 * srcPts[srcOff++]);
                dstPts[dstOff++] = (float) (M10 * x);
            }
            return;
        case (APPLY_SCALE | APPLY_TRANSLATE):
            M00 = m00; M02 = m02;
            M11 = m11; M12 = m12;
            while (--numPts >= 0) {
                dstPts[dstOff++] = (float) (M00 * srcPts[srcOff++] + M02);
                dstPts[dstOff++] = (float) (M11 * srcPts[srcOff++] + M12);
            }
            return;
        case (APPLY_SCALE):
            M00 = m00; M11 = m11;
            while (--numPts >= 0) {
                dstPts[dstOff++] = (float) (M00 * srcPts[srcOff++]);
                dstPts[dstOff++] = (float) (M11 * srcPts[srcOff++]);
            }
            return;
        case (APPLY_TRANSLATE):
            M02 = m02; M12 = m12;
            while (--numPts >= 0) {
                dstPts[dstOff++] = (float) (srcPts[srcOff++] + M02);
                dstPts[dstOff++] = (float) (srcPts[srcOff++] + M12);
            }
            return;
        case (APPLY_IDENTITY):
            while (--numPts >= 0) {
                dstPts[dstOff++] = (float) (srcPts[srcOff++]);
                dstPts[dstOff++] = (float) (srcPts[srcOff++]);
            }
            return;
        }

        /* NOTREACHED */
    }

    /**
     * 对指定的 <code>ptSrc</code> 进行逆变换，并将结果存储在 <code>ptDst</code> 中。
     * 如果 <code>ptDst</code> 为 <code>null</code>，则分配一个新的 <code>Point2D</code> 对象，
     * 并将变换结果存储在此对象中。
     * 无论哪种情况，<code>ptDst</code>，包含变换后的点，将返回以方便使用。
     * 如果 <code>ptSrc</code> 和 <code>ptDst</code> 是同一个对象，输入点将被正确覆盖为变换后的点。
     * @param ptSrc 要逆变换的点
     * @param ptDst 变换后的结果点
     * @return <code>ptDst</code>，包含逆变换的结果。
     * @exception NoninvertibleTransformException 如果矩阵不能被逆变换。
     * @since 1.2
     */
    @SuppressWarnings("fallthrough")
    public Point2D inverseTransform(Point2D ptSrc, Point2D ptDst)
        throws NoninvertibleTransformException
    {
        if (ptDst == null) {
            if (ptSrc instanceof Point2D.Double) {
                ptDst = new Point2D.Double();
            } else {
                ptDst = new Point2D.Float();
            }
        }
        // 如果 src == dst，则将源坐标复制到局部变量中
        double x = ptSrc.getX();
        double y = ptSrc.getY();
        switch (state) {
        default:
            stateError();
            /* NOTREACHED */
        case (APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE):
            x -= m02;
            y -= m12;
            /* NOBREAK */
        case (APPLY_SHEAR | APPLY_SCALE):
            double det = m00 * m11 - m01 * m10;
            if (Math.abs(det) <= Double.MIN_VALUE) {
                throw new NoninvertibleTransformException("Determinant is "+
                                                          det);
            }
            ptDst.setLocation((x * m11 - y * m01) / det,
                              (y * m00 - x * m10) / det);
            return ptDst;
        case (APPLY_SHEAR | APPLY_TRANSLATE):
            x -= m02;
            y -= m12;
            /* NOBREAK */
        case (APPLY_SHEAR):
            if (m01 == 0.0 || m10 == 0.0) {
                throw new NoninvertibleTransformException("Determinant is 0");
            }
            ptDst.setLocation(y / m10, x / m01);
            return ptDst;
        case (APPLY_SCALE | APPLY_TRANSLATE):
            x -= m02;
            y -= m12;
            /* NOBREAK */
        case (APPLY_SCALE):
            if (m00 == 0.0 || m11 == 0.0) {
                throw new NoninvertibleTransformException("Determinant is 0");
            }
            ptDst.setLocation(x / m00, y / m11);
            return ptDst;
        case (APPLY_TRANSLATE):
            ptDst.setLocation(x - m02, y - m12);
            return ptDst;
        case (APPLY_IDENTITY):
            ptDst.setLocation(x, y);
            return ptDst;
        }

        /* NOTREACHED */
    }


                    /* NOTREACHED */
    }

    /**
     * 通过此变换逆变换一个双精度坐标数组。
     * 两个坐标数组部分可以完全相同，也可以是同一数组的重叠部分，这不会影响结果的有效性。
     * 该方法确保在它们可以被变换之前，不会被前一个操作覆盖。
     * 坐标以指定偏移量开始，按 <code>[x0, y0, x1, y1, ..., xn, yn]</code> 的顺序存储在数组中。
     * @param srcPts 包含源点坐标的数组。每个点存储为一对 x,&nbsp;y 坐标。
     * @param dstPts 存储变换后点坐标的数组。每个点存储为一对 x,&nbsp;y 坐标。
     * @param srcOff 源数组中第一个要变换的点的偏移量
     * @param dstOff 目标数组中第一个变换点的存储位置的偏移量
     * @param numPts 要变换的点对象数量
     * @exception NoninvertibleTransformException 如果矩阵无法被逆变换。
     * @since 1.2
     */
    public void inverseTransform(double[] srcPts, int srcOff,
                                 double[] dstPts, int dstOff,
                                 int numPts)
        throws NoninvertibleTransformException
    {
        double M00, M01, M02, M10, M11, M12;    // 用于缓存
        double det;
        if (dstPts == srcPts &&
            dstOff > srcOff && dstOff < srcOff + numPts * 2)
        {
            // 如果数组部分重叠且目标位置高于源位置，我们通常变换坐标
            // 会导致覆盖一些后续的源坐标。
            // 为了解决这个问题，我们使用 arraycopy 将点复制到最终目的地
            // 并正确处理覆盖，然后在新的、更安全的位置就地变换它们。
            System.arraycopy(srcPts, srcOff, dstPts, dstOff, numPts * 2);
            // srcPts = dstPts;         // 它们已知是相等的。
            srcOff = dstOff;
        }
        switch (state) {
        default:
            stateError();
            /* NOTREACHED */
            return;
        case (APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE):
            M00 = m00; M01 = m01; M02 = m02;
            M10 = m10; M11 = m11; M12 = m12;
            det = M00 * M11 - M01 * M10;
            if (Math.abs(det) <= Double.MIN_VALUE) {
                throw new NoninvertibleTransformException("行列式为 " +
                                                          det);
            }
            while (--numPts >= 0) {
                double x = srcPts[srcOff++] - M02;
                double y = srcPts[srcOff++] - M12;
                dstPts[dstOff++] = (x * M11 - y * M01) / det;
                dstPts[dstOff++] = (y * M00 - x * M10) / det;
            }
            return;
        case (APPLY_SHEAR | APPLY_SCALE):
            M00 = m00; M01 = m01;
            M10 = m10; M11 = m11;
            det = M00 * M11 - M01 * M10;
            if (Math.abs(det) <= Double.MIN_VALUE) {
                throw new NoninvertibleTransformException("行列式为 " +
                                                          det);
            }
            while (--numPts >= 0) {
                double x = srcPts[srcOff++];
                double y = srcPts[srcOff++];
                dstPts[dstOff++] = (x * M11 - y * M01) / det;
                dstPts[dstOff++] = (y * M00 - x * M10) / det;
            }
            return;
        case (APPLY_SHEAR | APPLY_TRANSLATE):
            M01 = m01; M02 = m02;
            M10 = m10; M12 = m12;
            if (M01 == 0.0 || M10 == 0.0) {
                throw new NoninvertibleTransformException("行列式为 0");
            }
            while (--numPts >= 0) {
                double x = srcPts[srcOff++] - M02;
                dstPts[dstOff++] = (srcPts[srcOff++] - M12) / M10;
                dstPts[dstOff++] = x / M01;
            }
            return;
        case (APPLY_SHEAR):
            M01 = m01; M10 = m10;
            if (M01 == 0.0 || M10 == 0.0) {
                throw new NoninvertibleTransformException("行列式为 0");
            }
            while (--numPts >= 0) {
                double x = srcPts[srcOff++];
                dstPts[dstOff++] = srcPts[srcOff++] / M10;
                dstPts[dstOff++] = x / M01;
            }
            return;
        case (APPLY_SCALE | APPLY_TRANSLATE):
            M00 = m00; M02 = m02;
            M11 = m11; M12 = m12;
            if (M00 == 0.0 || M11 == 0.0) {
                throw new NoninvertibleTransformException("行列式为 0");
            }
            while (--numPts >= 0) {
                dstPts[dstOff++] = (srcPts[srcOff++] - M02) / M00;
                dstPts[dstOff++] = (srcPts[srcOff++] - M12) / M11;
            }
            return;
        case (APPLY_SCALE):
            M00 = m00; M11 = m11;
            if (M00 == 0.0 || M11 == 0.0) {
                throw new NoninvertibleTransformException("行列式为 0");
            }
            while (--numPts >= 0) {
                dstPts[dstOff++] = srcPts[srcOff++] / M00;
                dstPts[dstOff++] = srcPts[srcOff++] / M11;
            }
            return;
        case (APPLY_TRANSLATE):
            M02 = m02; M12 = m12;
            while (--numPts >= 0) {
                dstPts[dstOff++] = srcPts[srcOff++] - M02;
                dstPts[dstOff++] = srcPts[srcOff++] - M12;
            }
            return;
        case (APPLY_IDENTITY):
            if (srcPts != dstPts || srcOff != dstOff) {
                System.arraycopy(srcPts, srcOff, dstPts, dstOff,
                                 numPts * 2);
            }
            return;
        }

        /* NOTREACHED */
    }

    /**
     * 变换由 <code>ptSrc</code> 指定的相对距离向量，并将结果存储在 <code>ptDst</code> 中。
     * 相对距离向量在不应用仿射变换矩阵的平移分量的情况下进行变换，使用以下方程：
     * <pre>
     *  [  x' ]   [  m00  m01 (m02) ] [  x  ]   [ m00x + m01y ]
     *  [  y' ] = [  m10  m11 (m12) ] [  y  ] = [ m10x + m11y ]
     *  [ (1) ]   [  (0)  (0) ( 1 ) ] [ (1) ]   [     (1)     ]
     * </pre>
     * 如果 <code>ptDst</code> 为 <code>null</code>，则分配一个新的 <code>Point2D</code> 对象，并将变换结果存储在该对象中。
     * 无论哪种情况，<code>ptDst</code>（包含变换后的点）都会返回以方便使用。
     * 如果 <code>ptSrc</code> 和 <code>ptDst</code> 是同一个对象，则输入点会被正确地覆盖为变换后的点。
     * @param ptSrc 要增量变换的距离向量
     * @param ptDst 变换后距离向量的结果
     * @return <code>ptDst</code>，包含变换结果。
     * @since 1.2
     */
    public Point2D deltaTransform(Point2D ptSrc, Point2D ptDst) {
        if (ptDst == null) {
            if (ptSrc instanceof Point2D.Double) {
                ptDst = new Point2D.Double();
            } else {
                ptDst = new Point2D.Float();
            }
        }
        // 如果 src == dst，则将源坐标复制到局部变量中
        double x = ptSrc.getX();
        double y = ptSrc.getY();
        switch (state) {
        default:
            stateError();
            /* NOTREACHED */
            return null;
        case (APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE):
        case (APPLY_SHEAR | APPLY_SCALE):
            ptDst.setLocation(x * m00 + y * m01, x * m10 + y * m11);
            return ptDst;
        case (APPLY_SHEAR | APPLY_TRANSLATE):
        case (APPLY_SHEAR):
            ptDst.setLocation(y * m01, x * m10);
            return ptDst;
        case (APPLY_SCALE | APPLY_TRANSLATE):
        case (APPLY_SCALE):
            ptDst.setLocation(x * m00, y * m11);
            return ptDst;
        case (APPLY_TRANSLATE):
        case (APPLY_IDENTITY):
            ptDst.setLocation(x, y);
            return ptDst;
        }

        /* NOTREACHED */
    }

    /**
     * 通过此变换变换一个相对距离向量数组。
     * 相对距离向量在不应用仿射变换矩阵的平移分量的情况下进行变换，使用以下方程：
     * <pre>
     *  [  x' ]   [  m00  m01 (m02) ] [  x  ]   [ m00x + m01y ]
     *  [  y' ] = [  m10  m11 (m12) ] [  y  ] = [ m10x + m11y ]
     *  [ (1) ]   [  (0)  (0) ( 1 ) ] [ (1) ]   [     (1)     ]
     * </pre>
     * 两个坐标数组部分可以完全相同，也可以是同一数组的重叠部分，这不会影响结果的有效性。
     * 该方法确保在它们可以被变换之前，不会被前一个操作覆盖。
     * 坐标以指定偏移量开始，按 <code>[x0, y0, x1, y1, ..., xn, yn]</code> 的顺序存储在数组中。
     * @param srcPts 包含源距离向量的数组。每个向量存储为一对相对 x,&nbsp;y 坐标。
     * @param dstPts 存储变换后距离向量的数组。每个向量存储为一对相对 x,&nbsp;y 坐标。
     * @param srcOff 源数组中第一个要变换的向量的偏移量
     * @param dstOff 目标数组中第一个变换向量的存储位置的偏移量
     * @param numPts 要变换的向量坐标对数量
     * @since 1.2
     */
    public void deltaTransform(double[] srcPts, int srcOff,
                               double[] dstPts, int dstOff,
                               int numPts) {
        double M00, M01, M10, M11;      // 用于缓存
        if (dstPts == srcPts &&
            dstOff > srcOff && dstOff < srcOff + numPts * 2)
        {
            // 如果数组部分重叠且目标位置高于源位置，我们通常变换坐标
            // 会导致覆盖一些后续的源坐标。
            // 为了解决这个问题，我们使用 arraycopy 将点复制到最终目的地
            // 并正确处理覆盖，然后在新的、更安全的位置就地变换它们。
            System.arraycopy(srcPts, srcOff, dstPts, dstOff, numPts * 2);
            // srcPts = dstPts;         // 它们已知是相等的。
            srcOff = dstOff;
        }
        switch (state) {
        default:
            stateError();
            /* NOTREACHED */
            return;
        case (APPLY_SHEAR | APPLY_SCALE | APPLY_TRANSLATE):
        case (APPLY_SHEAR | APPLY_SCALE):
            M00 = m00; M01 = m01;
            M10 = m10; M11 = m11;
            while (--numPts >= 0) {
                double x = srcPts[srcOff++];
                double y = srcPts[srcOff++];
                dstPts[dstOff++] = x * M00 + y * M01;
                dstPts[dstOff++] = x * M10 + y * M11;
            }
            return;
        case (APPLY_SHEAR | APPLY_TRANSLATE):
        case (APPLY_SHEAR):
            M01 = m01; M10 = m10;
            while (--numPts >= 0) {
                double x = srcPts[srcOff++];
                dstPts[dstOff++] = srcPts[srcOff++] * M01;
                dstPts[dstOff++] = x * M10;
            }
            return;
        case (APPLY_SCALE | APPLY_TRANSLATE):
        case (APPLY_SCALE):
            M00 = m00; M11 = m11;
            while (--numPts >= 0) {
                dstPts[dstOff++] = srcPts[srcOff++] * M00;
                dstPts[dstOff++] = srcPts[srcOff++] * M11;
            }
            return;
        case (APPLY_TRANSLATE):
        case (APPLY_IDENTITY):
            if (srcPts != dstPts || srcOff != dstOff) {
                System.arraycopy(srcPts, srcOff, dstPts, dstOff,
                                 numPts * 2);
            }
            return;
        }

        /* NOTREACHED */
    }

    /**
     * 返回一个由指定 <code>Shape</code> 的几何形状在被此变换变换后定义的新 <code>Shape</code> 对象。
     * @param pSrc 要被此变换变换的指定 <code>Shape</code> 对象。
     * @return 一个定义了变换后的 <code>Shape</code> 几何形状的新 <code>Shape</code> 对象，如果 {@code pSrc} 为 null，则返回 null。
     * @since 1.2
     */
    public Shape createTransformedShape(Shape pSrc) {
        if (pSrc == null) {
            return null;
        }
        return new Path2D.Double(pSrc, this);
    }

    // 为打印时舍入值到合理的精度
    // 注意 Math.sin(Math.PI) 的误差约为 10^-16
    private static double _matround(double matval) {
        return Math.rint(matval * 1E15) / 1E15;
    }

    /**
     * 返回表示此 <code>Object</code> 值的 <code>String</code>。
     * @return 表示此 <code>Object</code> 值的 <code>String</code>。
     * @since 1.2
     */
    public String toString() {
        return ("AffineTransform[["
                + _matround(m00) + ", "
                + _matround(m01) + ", "
                + _matround(m02) + "], ["
                + _matround(m10) + ", "
                + _matround(m11) + ", "
                + _matround(m12) + "]]");
    }

    /**
     * 如果此 <code>AffineTransform</code> 是单位变换，则返回 <code>true</code>。
     * @return 如果此 <code>AffineTransform</code> 是单位变换，则返回 <code>true</code>；否则返回 <code>false</code>。
     * @since 1.2
     */
    public boolean isIdentity() {
        return (state == APPLY_IDENTITY || (getType() == TYPE_IDENTITY));
    }

    /**
     * 返回此 <code>AffineTransform</code> 对象的副本。
     * @return 一个 <code>Object</code>，它是此 <code>AffineTransform</code> 对象的副本。
     * @since 1.2
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            // 这不应该发生，因为我们是 Cloneable 的
            throw new InternalError(e);
        }
    }

    /**
     * 返回此变换的哈希码。
     * @return 此变换的哈希码。
     * @since 1.2
     */
    public int hashCode() {
        long bits = Double.doubleToLongBits(m00);
        bits = bits * 31 + Double.doubleToLongBits(m01);
        bits = bits * 31 + Double.doubleToLongBits(m02);
        bits = bits * 31 + Double.doubleToLongBits(m10);
        bits = bits * 31 + Double.doubleToLongBits(m11);
        bits = bits * 31 + Double.doubleToLongBits(m12);
        return (((int) bits) ^ ((int) (bits >> 32)));
    }


                /**
     * 如果此 <code>AffineTransform</code> 表示与指定参数相同的仿射坐标变换，则返回 <code>true</code>。
     * @param obj 要测试是否与此 <code>AffineTransform</code> 相等的 <code>Object</code>
     * @return 如果 <code>obj</code> 等于此 <code>AffineTransform</code> 对象，则返回 <code>true</code>；否则返回 <code>false</code>。
     * @since 1.2
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof AffineTransform)) {
            return false;
        }

        AffineTransform a = (AffineTransform)obj;

        return ((m00 == a.m00) && (m01 == a.m01) && (m02 == a.m02) &&
                (m10 == a.m10) && (m11 == a.m11) && (m12 == a.m12));
    }

    /* 序列化支持。需要一个 readObject 方法，因为 state 字段是此特定 AffineTransform 实现的一部分，而不是公共规范的一部分。state 变量的值需要由 readObject 方法动态计算，就像在 6 参数矩阵构造函数中一样。 */

    /*
     * JDK 1.2 serialVersionUID
     */
    private static final long serialVersionUID = 1330973210523860834L;

    private void writeObject(java.io.ObjectOutputStream s)
        throws java.lang.ClassNotFoundException, java.io.IOException
    {
        s.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream s)
        throws java.lang.ClassNotFoundException, java.io.IOException
    {
        s.defaultReadObject();
        updateState();
    }
}
