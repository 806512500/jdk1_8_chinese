/*
 * Copyright (c) 1998, 2008, Oracle and/or its affiliates. All rights reserved.
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

/* ********************************************************************
 **********************************************************************
 **********************************************************************
 *** COPYRIGHT (c) Eastman Kodak Company, 1997                      ***
 *** As  an unpublished  work pursuant to Title 17 of the United    ***
 *** States Code.  All rights reserved.                             ***
 **********************************************************************
 **********************************************************************
 **********************************************************************/

package java.awt.image.renderable;
import java.util.*;
import java.awt.geom.*;
import java.awt.*;
import java.awt.image.*;

/**
 * RenderContext 封装了从 RenderableImage 生成特定渲染所需的信息。它包含以渲染无关的方式指定的要渲染的区域、
 * 执行渲染的分辨率以及用于控制渲染过程的提示。
 *
 * <p> 用户创建 RenderContext 并通过 createRendering 方法传递给 RenderableImage。RenderContext 的大多数方法
 * 不是供应用程序直接使用的，而是由 RenderableImage 和传递给它的操作类使用的。
 *
 * <p> 传递到和从这个类传递的 AffineTransform 参数会被克隆。RenderingHints 和 Shape 参数不一定可克隆，
 * 因此只是引用复制。修改正在使用的 RenderContext 实例中的 RenderingHints 或 Shape 实例可能会产生不希望的副作用。
 */
public class RenderContext implements Cloneable {

    /** 提示表。可以为 null。 */
    RenderingHints hints;

    /** 将用户坐标转换为设备坐标的变换。 */
    AffineTransform usr2dev;

    /** 感兴趣的区域。可以为 null。 */
    Shape aoi;

    // 各种构造函数，允许不同级别的具体性。如果缺少 Shape，则假定为整个可渲染区域。
    // 如果缺少提示，则假定没有提示。

    /**
     * 使用给定的变换构造 RenderContext。
     * 感兴趣的区域作为 Shape 提供，渲染提示作为 RenderingHints 对象提供。
     *
     * @param usr2dev 一个 AffineTransform。
     * @param aoi 一个表示感兴趣区域的 Shape。
     * @param hints 一个包含渲染提示的 RenderingHints 对象。
     */
    public RenderContext(AffineTransform usr2dev,
                         Shape aoi,
                         RenderingHints hints) {
        this.hints = hints;
        this.aoi = aoi;
        this.usr2dev = (AffineTransform)usr2dev.clone();
    }

    /**
     * 使用给定的变换构造 RenderContext。
     * 感兴趣的区域假定为整个可渲染区域。
     * 不使用渲染提示。
     *
     * @param usr2dev 一个 AffineTransform。
     */
    public RenderContext(AffineTransform usr2dev) {
        this(usr2dev, null, null);
    }

    /**
     * 使用给定的变换和渲染提示构造 RenderContext。
     * 感兴趣的区域假定为整个可渲染区域。
     *
     * @param usr2dev 一个 AffineTransform。
     * @param hints 一个包含渲染提示的 RenderingHints 对象。
     */
    public RenderContext(AffineTransform usr2dev, RenderingHints hints) {
        this(usr2dev, null, hints);
    }

    /**
     * 使用给定的变换和感兴趣区域构造 RenderContext。
     * 感兴趣的区域作为 Shape 提供。
     * 不使用渲染提示。
     *
     * @param usr2dev 一个 AffineTransform。
     * @param aoi 一个表示感兴趣区域的 Shape。
     */
    public RenderContext(AffineTransform usr2dev, Shape aoi) {
        this(usr2dev, aoi, null);
    }

    /**
     * 获取此 <code>RenderContext</code> 的渲染提示。
     * @return 一个 <code>RenderingHints</code> 对象，表示此 <code>RenderContext</code> 的渲染提示。
     * @see #setRenderingHints(RenderingHints)
     */
    public RenderingHints getRenderingHints() {
        return hints;
    }

    /**
     * 设置此 <code>RenderContext</code> 的渲染提示。
     * @param hints 一个 <code>RenderingHints</code> 对象，表示要分配给此 <code>RenderContext</code> 的渲染提示。
     * @see #getRenderingHints
     */
    public void setRenderingHints(RenderingHints hints) {
        this.hints = hints;
    }

    /**
     * 将 RenderContext 中当前的用户到设备的 AffineTransform 设置为给定的变换。
     *
     * @param newTransform 新的 AffineTransform。
     * @see #getTransform
     */
    public void setTransform(AffineTransform newTransform) {
        usr2dev = (AffineTransform)newTransform.clone();
    }

    /**
     * 通过前置另一个变换来修改当前的用户到设备变换。在矩阵表示中，操作是：
     * <pre>
     * [this] = [modTransform] x [this]
     * </pre>
     *
     * @param modTransform 要前置到当前 usr2dev 变换的 AffineTransform。
     * @since 1.3
     */
    public void preConcatenateTransform(AffineTransform modTransform) {
        this.preConcetenateTransform(modTransform);
    }

    /**
     * 通过前置另一个变换来修改当前的用户到设备变换。在矩阵表示中，操作是：
     * <pre>
     * [this] = [modTransform] x [this]
     * </pre>
     * 此方法与 preConcatenateTransform 方法执行相同的操作。它是为了与以前版本的向后兼容而提供的，这些版本拼错了方法名。
     *
     * @param modTransform 要前置到当前 usr2dev 变换的 AffineTransform。
     * @deprecated     被 <code>preConcatenateTransform(AffineTransform)</code> 替代。
     */
    @Deprecated
    public void preConcetenateTransform(AffineTransform modTransform) {
        usr2dev.preConcatenate(modTransform);
    }

    /**
     * 通过后置另一个变换来修改当前的用户到设备变换。在矩阵表示中，操作是：
     * <pre>
     * [this] = [this] x [modTransform]
     * </pre>
     *
     * @param modTransform 要后置到当前 usr2dev 变换的 AffineTransform。
     * @since 1.3
     */
    public void concatenateTransform(AffineTransform modTransform) {
        this.concetenateTransform(modTransform);
    }

    /**
     * 通过后置另一个变换来修改当前的用户到设备变换。在矩阵表示中，操作是：
     * <pre>
     * [this] = [this] x [modTransform]
     * </pre>
     * 此方法与 concatenateTransform 方法执行相同的操作。它是为了与以前版本的向后兼容而提供的，这些版本拼错了方法名。
     *
     * @param modTransform 要后置到当前 usr2dev 变换的 AffineTransform。
     * @deprecated     被 <code>concatenateTransform(AffineTransform)</code> 替代。
     */
    @Deprecated
    public void concetenateTransform(AffineTransform modTransform) {
        usr2dev.concatenate(modTransform);
    }

    /**
     * 获取当前的用户到设备 AffineTransform。
     *
     * @return 当前 AffineTransform 的引用。
     * @see #setTransform(AffineTransform)
     */
    public AffineTransform getTransform() {
        return (AffineTransform)usr2dev.clone();
    }

    /**
     * 设置当前的感兴趣区域。旧的区域被丢弃。
     *
     * @param newAoi 新的感兴趣区域。
     * @see #getAreaOfInterest
     */
    public void setAreaOfInterest(Shape newAoi) {
        aoi = newAoi;
    }

    /**
     * 获取 RenderContext 当前包含的感兴趣区域。
     *
     * @return RenderContext 的感兴趣区域的引用，如果没有指定则为 null。
     * @see #setAreaOfInterest(Shape)
     */
    public Shape getAreaOfInterest() {
        return aoi;
    }

    /**
     * 复制一个 RenderContext。感兴趣区域通过引用复制。usr2dev AffineTransform 和提示被克隆，
     * 而感兴趣区域通过引用复制。
     *
     * @return 新的克隆的 RenderContext。
     */
    public Object clone() {
        RenderContext newRenderContext = new RenderContext(usr2dev,
                                                           aoi, hints);
        return newRenderContext;
    }
}
