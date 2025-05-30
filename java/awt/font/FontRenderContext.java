/*
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
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
 * @author Charlton Innovations, Inc.
 */

package java.awt.font;

import java.awt.RenderingHints;
import static java.awt.RenderingHints.*;
import java.awt.geom.AffineTransform;

/**
*   <code>FontRenderContext</code> 类是一个包含正确测量文本所需信息的容器。文本的测量可能会因将轮廓映射到像素的规则和应用程序提供的渲染提示而有所不同。
*   <p>
*   一个这样的信息是将排版点转换为像素的变换。一个点被定义为恰好是 1/72 英寸，这与传统的机械测量略有不同。在 600dpi 设备上以 12pt 渲染的字符可能与在 72dpi 设备上以 12pt 渲染的同一字符大小不同，因为存在将轮廓映射到像素边界和字体设计者可能指定的提示等因素。
*   <p>
*   应用程序指定的抗锯齿和分数度量也可以影响字符的大小，因为存在将轮廓映射到像素边界的舍入。
*   <p>
*   通常，<code>FontRenderContext</code> 的实例是从 {@link java.awt.Graphics2D Graphics2D} 对象中获取的。直接构造的 <code>FontRenderContext</code> 可能不代表任何实际的图形设备，可能会导致意外或不正确的结果。
*   @see java.awt.RenderingHints#KEY_TEXT_ANTIALIASING
*   @see java.awt.RenderingHints#KEY_FRACTIONALMETRICS
*   @see java.awt.Graphics2D#getFontRenderContext()
*   @see java.awt.font.LineMetrics
*/

public class FontRenderContext {
    private transient AffineTransform tx;
    private transient Object aaHintValue;
    private transient Object fmHintValue;
    private transient boolean defaulting;

    /**
     * 构造一个新的 <code>FontRenderContext</code> 对象。
     *
     */
    protected FontRenderContext() {
        aaHintValue = VALUE_TEXT_ANTIALIAS_DEFAULT;
        fmHintValue = VALUE_FRACTIONALMETRICS_DEFAULT;
        defaulting = true;
    }

    /**
     * 从一个可选的 {@link AffineTransform} 和两个 <code>boolean</code> 值构造一个 <code>FontRenderContext</code> 对象，这两个值确定新构造的对象是否具有抗锯齿或分数度量。
     * 在每种情况下，布尔值 <CODE>true</CODE> 和 <CODE>false</CODE> 分别对应于渲染提示值 <CODE>ON</CODE> 和 <CODE>OFF</CODE>。
     * <p>
     * 要指定其他提示值，请使用指定渲染提示值作为参数的构造函数：{@link #FontRenderContext(AffineTransform, Object, Object)}。
     * @param tx 用于将排版点转换为像素的变换。如果为 null，则使用单位变换。
     * @param isAntiAliased 确定新构造的对象是否具有抗锯齿。
     * @param usesFractionalMetrics 确定新构造的对象是否具有分数度量。
     */
    public FontRenderContext(AffineTransform tx,
                            boolean isAntiAliased,
                            boolean usesFractionalMetrics) {
        if (tx != null && !tx.isIdentity()) {
            this.tx = new AffineTransform(tx);
        }
        if (isAntiAliased) {
            aaHintValue = VALUE_TEXT_ANTIALIAS_ON;
        } else {
            aaHintValue = VALUE_TEXT_ANTIALIAS_OFF;
        }
        if (usesFractionalMetrics) {
            fmHintValue = VALUE_FRACTIONALMETRICS_ON;
        } else {
            fmHintValue = VALUE_FRACTIONALMETRICS_OFF;
        }
    }

    /**
     * 从一个可选的 {@link AffineTransform} 和两个 <code>Object</code> 值构造一个 <code>FontRenderContext</code> 对象，这两个值确定新构造的对象是否具有抗锯齿或分数度量。
     * @param tx 用于将排版点转换为像素的变换。如果为 null，则使用单位变换。
     * @param aaHint - 在 {@link java.awt.RenderingHints java.awt.RenderingHints} 中定义的文本抗锯齿渲染提示值之一。
     * 任何其他值将抛出 <code>IllegalArgumentException</code>。可以指定 {@link java.awt.RenderingHints#VALUE_TEXT_ANTIALIAS_DEFAULT VALUE_TEXT_ANTIALIAS_DEFAULT}，
     * 在这种情况下，使用的模式取决于实现。
     * @param fmHint - 在 {@link java.awt.RenderingHints java.awt.RenderingHints} 中定义的文本分数渲染提示值之一。
     * 可以指定 {@link java.awt.RenderingHints#VALUE_FRACTIONALMETRICS_DEFAULT VALUE_FRACTIONALMETRICS_DEFAULT}，在这种情况下，使用的模式取决于实现。
     * 任何其他值将抛出 <code>IllegalArgumentException</code>
     * @throws IllegalArgumentException 如果提示不是合法值。
     * @since 1.6
     */
    public FontRenderContext(AffineTransform tx, Object aaHint, Object fmHint){
        if (tx != null && !tx.isIdentity()) {
            this.tx = new AffineTransform(tx);
        }
        try {
            if (KEY_TEXT_ANTIALIASING.isCompatibleValue(aaHint)) {
                aaHintValue = aaHint;
            } else {
                throw new IllegalArgumentException("AA hint:" + aaHint);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("AA hint:" +aaHint);
        }
        try {
            if (KEY_FRACTIONALMETRICS.isCompatibleValue(fmHint)) {
                fmHintValue = fmHint;
            } else {
                throw new IllegalArgumentException("FM hint:" + fmHint);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("FM hint:" +fmHint);
        }
    }

    /**
     * 指示此 <code>FontRenderContext</code> 对象是否在变换渲染上下文中测量文本。
     * @return  如果此 <code>FontRenderContext</code> 对象具有非单位的 AffineTransform 属性，则返回 <code>true</code>；否则返回 <code>false</code>。
     * @see     java.awt.font.FontRenderContext#getTransform
     * @since   1.6
     */
    public boolean isTransformed() {
        if (!defaulting) {
            return tx != null;
        } else {
            return !getTransform().isIdentity();
        }
    }

    /**
     * 返回此 <code>FontRenderContext</code> 的仿射变换的类型，如 {@link java.awt.geom.AffineTransform#getType()} 所指定。
     * @return 变换的类型。
     * @see AffineTransform
     * @since 1.6
     */
    public int getTransformType() {
        if (!defaulting) {
            if (tx == null) {
                return AffineTransform.TYPE_IDENTITY;
            } else {
                return tx.getType();
            }
        } else {
            return getTransform().getType();
        }
    }

    /**
    *   获取用于将排版点转换为像素的变换。
    *   @return 此 <code>FontRenderContext</code> 的 <code>AffineTransform</code>。
    *   @see AffineTransform
    */
    public AffineTransform getTransform() {
        return (tx == null) ? new AffineTransform() : new AffineTransform(tx);
    }

    /**
    * 返回一个布尔值，指示此 <code>FontRenderContext</code> 是否指定了某种形式的抗锯齿。
    * 调用 {@link #getAntiAliasingHint() getAntiAliasingHint()} 获取具体的渲染提示值。
    *   @return    如果文本在此 <code>FontRenderContext</code> 中抗锯齿，则返回 <code>true</code>；否则返回 <code>false</code>。
    *   @see        java.awt.RenderingHints#KEY_TEXT_ANTIALIASING
    *   @see #FontRenderContext(AffineTransform,boolean,boolean)
    *   @see #FontRenderContext(AffineTransform,Object,Object)
    */
    public boolean isAntiAliased() {
        return !(aaHintValue == VALUE_TEXT_ANTIALIAS_OFF ||
                 aaHintValue == VALUE_TEXT_ANTIALIAS_DEFAULT);
    }

    /**
    * 返回一个布尔值，指示此 <code>FontRenderContext</code> 是否使用文本分数度量模式。
    * 调用 {@link #getFractionalMetricsHint() getFractionalMetricsHint()} 获取对应的渲染提示值。
    *   @return    如果布局应使用分数度量，则返回 <code>true</code>；否则返回 <code>false</code>。
    *   @see java.awt.RenderingHints#KEY_FRACTIONALMETRICS
    *   @see #FontRenderContext(AffineTransform,boolean,boolean)
    *   @see #FontRenderContext(AffineTransform,Object,Object)
    */
    public boolean usesFractionalMetrics() {
        return !(fmHintValue == VALUE_FRACTIONALMETRICS_OFF ||
                 fmHintValue == VALUE_FRACTIONALMETRICS_DEFAULT);
    }

    /**
     * 返回此 <code>FontRenderContext</code> 中使用的文本抗锯齿渲染模式提示。
     * 这将是 {@link java.awt.RenderingHints java.awt.RenderingHints} 中定义的文本抗锯齿渲染提示值之一。
     * @return  此 <code>FontRenderContext</code> 中使用的文本抗锯齿渲染模式提示。
     * @since 1.6
     */
    public Object getAntiAliasingHint() {
        if (defaulting) {
            if (isAntiAliased()) {
                 return VALUE_TEXT_ANTIALIAS_ON;
            } else {
                return VALUE_TEXT_ANTIALIAS_OFF;
            }
        }
        return aaHintValue;
    }

    /**
     * 返回此 <code>FontRenderContext</code> 中使用的文本分数度量渲染模式提示。
     * 这将是 {@link java.awt.RenderingHints java.awt.RenderingHints} 中定义的文本分数度量渲染提示值之一。
     * @return 此 <code>FontRenderContext</code> 中使用的文本分数度量渲染模式提示。
     * @since 1.6
     */
    public Object getFractionalMetricsHint() {
        if (defaulting) {
            if (usesFractionalMetrics()) {
                 return VALUE_FRACTIONALMETRICS_ON;
            } else {
                return VALUE_FRACTIONALMETRICS_OFF;
            }
        }
        return fmHintValue;
    }

    /**
     * 如果 obj 是 FontRenderContext 的实例，并且具有与此相同的变换、抗锯齿和分数度量值，则返回 true。
     * @param obj 要测试的等价性的对象
     * @return 如果指定的对象等于此 <code>FontRenderContext</code>，则返回 <code>true</code>；否则返回 <code>false</code>。
     */
    public boolean equals(Object obj) {
        try {
            return equals((FontRenderContext)obj);
        }
        catch (ClassCastException e) {
            return false;
        }
    }

    /**
     * 如果 rhs 具有与此相同的变换、抗锯齿和分数度量值，则返回 true。
     * @param rhs 要测试的等价性的 <code>FontRenderContext</code>
     * @return 如果 <code>rhs</code> 等于此 <code>FontRenderContext</code>，则返回 <code>true</code>；否则返回 <code>false</code>。
     * @since 1.4
     */
    public boolean equals(FontRenderContext rhs) {
        if (this == rhs) {
            return true;
        }
        if (rhs == null) {
            return false;
        }

        /* 如果两个实例都不是子类，则直接引用值。 */
        if (!rhs.defaulting && !defaulting) {
            if (rhs.aaHintValue == aaHintValue &&
                rhs.fmHintValue == fmHintValue) {

                return tx == null ? rhs.tx == null : tx.equals(rhs.tx);
            }
            return false;
        } else {
            return
                rhs.getAntiAliasingHint() == getAntiAliasingHint() &&
                rhs.getFractionalMetricsHint() == getFractionalMetricsHint() &&
                rhs.getTransform().equals(getTransform());
        }
    }

    /**
     * 返回此 FontRenderContext 的哈希码。
     */
    public int hashCode() {
        int hash = tx == null ? 0 : tx.hashCode();
        /* SunHints 值对象具有身份哈希码，因此我们可以依赖这一点来确保两个相等的 FRC 具有相同的哈希码。 */
        if (defaulting) {
            hash += getAntiAliasingHint().hashCode();
            hash += getFractionalMetricsHint().hashCode();
        } else {
            hash += aaHintValue.hashCode();
            hash += fmHintValue.hashCode();
        }
        return hash;
    }
}
