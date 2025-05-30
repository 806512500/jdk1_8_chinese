
/*
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.text.CharacterIterator;

/**
 * <code>FontMetrics</code> 类定义了一个字体度量对象，该对象封装了特定字体在特定屏幕上渲染的信息。
 * <p>
 * <b>子类注意</b>：由于这些方法中的许多形成了封闭的、相互递归的循环，因此必须注意实现每个循环中的至少一个方法，以防止在使用子类时发生无限递归。
 * 特别是，以下是最小建议覆盖的方法集，以确保正确性和防止无限递归（尽管其他子集同样可行）：
 * <ul>
 * <li>{@link #getAscent()}
 * <li>{@link #getLeading()}
 * <li>{@link #getMaxAdvance()}
 * <li>{@link #charWidth(char)}
 * <li>{@link #charsWidth(char[], int, int)}
 * </ul>
 * <p>
 * <img src="doc-files/FontMetrics-1.gif" alt="字母 'p' 显示其 '参考点'"
 * style="border:15px; float:right; margin: 7px 10px;">
 * 请注意，这些方法的实现是低效的，因此通常会被更高效的工具特定实现所覆盖。
 * <p>
 * 当应用程序要求将字符放置在位置 (<i>x</i>,&nbsp;<i>y</i>) 时，字符会被放置在其参考点（如附图中的点所示）位于该位置。参考点指定了一条称为字符的<i>基线</i>的水平线。在正常打印中，字符的基线应该对齐。
 * <p>
 * 此外，每个字符都有一个<i>上升高度</i>、一个<i>下降高度</i>和一个<i>前进宽度</i>。上升高度是指字符在基线上方的高度。下降高度是指字符在基线下方的高度。前进宽度指示 AWT 应该放置下一个字符的位置。
 * <p>
 * 字符数组或字符串也可以有上升高度、下降高度和前进宽度。数组的上升高度是数组中任何字符的最大上升高度。下降高度是数组中任何字符的最大下降高度。前进宽度是字符数组中每个字符的前进宽度之和。字符串的前进宽度是指字符串沿基线的距离。此距离是用于居中或右对齐字符串的宽度。
 * <p>请注意，字符串的前进宽度不一定是其字符的前进宽度之和，因为字符的宽度可能因其上下文而异。
 * 例如，在阿拉伯文中，字符的形状可能会改变以连接到其他字符。此外，在某些脚本中，某些字符序列可以由一个称为<em>连字</em>的单个形状表示。单独测量字符不能解释这些转换。
 * <p>字体度量是基线相对的，这意味着它们通常与应用到字体的旋转无关（模可能的网格提示效果）。请参阅 {@link java.awt.Font Font}。
 *
 * @author      Jim Graham
 * @see         java.awt.Font
 * @since       JDK1.0
 */
public abstract class FontMetrics implements java.io.Serializable {

    static {
        /* 确保加载必要的本机库 */
        Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            initIDs();
        }
    }

    private static final FontRenderContext
        DEFAULT_FRC = new FontRenderContext(null, false, false);

    /**
     * 用于创建字体度量的实际 {@link Font}。
     * 此值不能为空。
     *
     * @serial
     * @see #getFont()
     */
    protected Font font;

    /*
     * JDK 1.1 序列化版本ID
     */
    private static final long serialVersionUID = 1681126225205050147L;

    /**
     * 创建一个新的 <code>FontMetrics</code> 对象，用于查找指定 <code>Font</code> 中字符字形的高度和宽度信息。
     * @param     font <code>Font</code>
     * @see       java.awt.Font
     */
    protected FontMetrics(Font font) {
        this.font = font;
    }

    /**
     * 获取此 <code>FontMetrics</code> 对象描述的 <code>Font</code>。
     * @return    此 <code>FontMetrics</code> 对象描述的 <code>Font</code>。
     */
    public Font getFont() {
        return font;
    }

    /**
     * 获取此 <code>FontMetrics</code> 对象用于测量文本的 <code>FontRenderContext</code>。
     * <p>
     * 请注意，此类中接受 <code>Graphics</code> 参数的方法使用该 <code>Graphics</code> 对象的 <code>FontRenderContext</code> 来测量文本，而不是此 <code>FontRenderContext</code>。
     * @return    此 <code>FontMetrics</code> 对象使用的 <code>FontRenderContext</code>。
     * @since 1.6
     */
    public FontRenderContext getFontRenderContext() {
        return DEFAULT_FRC;
    }

    /**
     * 确定此 <code>FontMetrics</code> 对象描述的 <code>Font</code> 的 <em>标准行间距</em>。
     * 标准行间距，或行间距，是逻辑上保留的用于一行文本的下降高度和下一行文本的上升高度之间的空间。高度度量计算时包括这个额外的空间。
     * @return    <code>Font</code> 的标准行间距。
     * @see   #getHeight()
     * @see   #getAscent()
     * @see   #getDescent()
     */
    public int getLeading() {
        return 0;
    }

    /**
     * 确定此 <code>FontMetrics</code> 对象描述的 <code>Font</code> 的 <em>字体上升高度</em>。
     * 字体上升高度是从字体的基线到大多数字母数字字符顶部的距离。字体中的一些字符可能会超出字体上升高度线。
     * @return     <code>Font</code> 的字体上升高度。
     * @see        #getMaxAscent()
     */
    public int getAscent() {
        return font.getSize();
    }

    /**
     * 确定此 <code>FontMetrics</code> 对象描述的 <code>Font</code> 的 <em>字体下降高度</em>。
     * 字体下降高度是从字体的基线到大多数带有下降部分的字母数字字符底部的距离。字体中的一些字符可能会超出字体下降高度线。
     * @return     <code>Font</code> 的字体下降高度。
     * @see        #getMaxDescent()
     */
    public int getDescent() {
        return 0;
    }

    /**
     * 获取此字体中一行文本的标准高度。这是相邻文本行基线之间的距离。
     * 它是行间距 + 上升高度 + 下降高度的总和。由于舍入，这可能不等于 getAscent() + getDescent() + getLeading()。
     * 没有保证以该距离间隔的文本行是不重叠的；如果某些字符超出标准上升高度或标准下降高度度量，这样的行可能会重叠。
     * @return    字体的标准高度。
     * @see       #getLeading()
     * @see       #getAscent()
     * @see       #getDescent()
     */
    public int getHeight() {
        return getLeading() + getAscent() + getDescent();
    }

    /**
     * 确定此 <code>FontMetrics</code> 对象描述的 <code>Font</code> 的最大上升高度。没有字符超出此高度。
     * @return    <code>Font</code> 中任何字符的最大上升高度。
     * @see       #getAscent()
     */
    public int getMaxAscent() {
        return getAscent();
    }

    /**
     * 确定此 <code>FontMetrics</code> 对象描述的 <code>Font</code> 的最大下降高度。没有字符超出此高度。
     * @return    <code>Font</code> 中任何字符的最大下降高度。
     * @see       #getDescent()
     */
    public int getMaxDescent() {
        return getDescent();
    }

    /**
     * 仅用于向后兼容。
     * @return    <code>Font</code> 中任何字符的最大下降高度。
     * @see #getMaxDescent()
     * @deprecated 自 JDK 版本 1.1.1 起，
     * 替换为 <code>getMaxDescent()</code>。
     */
    @Deprecated
    public int getMaxDecent() {
        return getMaxDescent();
    }

    /**
     * 获取此 <code>Font</code> 中任何字符的最大前进宽度。前进宽度是从字符串基线最左点到最右点的距离。字符串的前进宽度不一定是其字符的前进宽度之和。
     * @return    <code>Font</code> 中任何字符的最大前进宽度，或如果最大前进宽度未知则返回 <code>-1</code>。
     */
    public int getMaxAdvance() {
        return -1;
    }

    /**
     * 返回此 <code>Font</code> 中指定字符的前进宽度。前进宽度是从字符基线最左点到最右点的距离。请注意，字符串的前进宽度不一定是其字符的前进宽度之和。
     *
     * <p>此方法不会验证指定字符是否为有效的 Unicode 代码点。如有必要，调用者必须使用 {@link
     * java.lang.Character#isValidCodePoint(int)
     * Character.isValidCodePoint} 验证字符值。
     *
     * @param codePoint 要测量的字符（Unicode 代码点）
     * @return    指定字符在由此 <code>FontMetrics</code> 对象描述的 <code>Font</code> 中的前进宽度。
     * @see   #charsWidth(char[], int, int)
     * @see   #stringWidth(String)
     */
    public int charWidth(int codePoint) {
        if (!Character.isValidCodePoint(codePoint)) {
            codePoint = 0xffff; // 替换为缺失字形的宽度
        }

        if (codePoint < 256) {
            return getWidths()[codePoint];
        } else {
            char[] buffer = new char[2];
            int len = Character.toChars(codePoint, buffer, 0);
            return charsWidth(buffer, 0, len);
        }
    }

    /**
     * 返回此 <code>Font</code> 中指定字符的前进宽度。前进宽度是从字符基线最左点到最右点的距离。请注意，字符串的前进宽度不一定是其字符的前进宽度之和。
     *
     * <p><b>注意：</b>此方法无法处理 <a
     * href="../lang/Character.html#supplementary"> 补充字符</a>。要支持所有 Unicode 字符，包括补充字符，请使用 {@link #charWidth(int)} 方法。
     *
     * @param ch 要测量的字符
     * @return     指定字符在由此 <code>FontMetrics</code> 对象描述的 <code>Font</code> 中的前进宽度。
     * @see        #charsWidth(char[], int, int)
     * @see        #stringWidth(String)
     */
    public int charWidth(char ch) {
        if (ch < 256) {
            return getWidths()[ch];
        }
        char data[] = {ch};
        return charsWidth(data, 0, 1);
    }

    /**
     * 返回在该 <code>Font</code> 中显示指定 <code>String</code> 的总前进宽度。前进宽度是从字符串基线最左点到最右点的距离。
     * <p>
     * 请注意，字符串的前进宽度不一定是其字符的前进宽度之和。
     * @param str 要测量的 <code>String</code>
     * @return    指定 <code>String</code> 在由该 <code>FontMetrics</code> 对象描述的 <code>Font</code> 中的前进宽度。
     * @throws NullPointerException 如果 str 为 null。
     * @see       #bytesWidth(byte[], int, int)
     * @see       #charsWidth(char[], int, int)
     * @see       #getStringBounds(String, Graphics)
     */
    public int stringWidth(String str) {
        int len = str.length();
        char data[] = new char[len];
        str.getChars(0, len, data, 0);
        return charsWidth(data, 0, len);
    }

    /**
     * 返回在该 <code>Font</code> 中显示指定字符数组的总前进宽度。前进宽度是从字符串基线最左点到最右点的距离。字符串的前进宽度不一定是其字符的前进宽度之和。
     * 这相当于测量指定范围内的字符组成的字符串。
     * @param data 要测量的字符数组
     * @param off 字符数组中的起始偏移量
     * @param len 从数组中测量的字符数
     * @return    指定 <code>char</code> 数组子数组在由该 <code>FontMetrics</code> 对象描述的字体中的前进宽度。
     * @throws    NullPointerException 如果 <code>data</code> 为 null。
     * @throws    IndexOutOfBoundsException 如果 <code>off</code> 和 <code>len</code> 参数索引的字符超出 <code>data</code> 数组的边界。
     * @see       #charWidth(int)
     * @see       #charWidth(char)
     * @see       #bytesWidth(byte[], int, int)
     * @see       #stringWidth(String)
     */
    public int charsWidth(char data[], int off, int len) {
        return stringWidth(new String(data, off, len));
    }


/**
 * 返回显示指定字节数组在该 <code>Font</code> 中的总前进宽度。前进宽度是从字符串基线的最左点到最右点的距离。字符串的前进宽度不一定等于其字符前进宽度的总和。
 * 这相当于测量指定范围内的字符组成的字符串。
 * @param data 要测量的字节数组
 * @param off 字节数组中的起始偏移量
 * @param len 要从数组中测量的字节数
 * @return    指定 <code>byte</code> 数组在该 <code>Font</code> 中的前进宽度。
 * @throws    NullPointerException 如果 <code>data</code> 为 null。
 * @throws    IndexOutOfBoundsException 如果 <code>off</code> 和 <code>len</code> 参数索引的字节超出 <code>data</code> 数组的范围。
 * @see       #charsWidth(char[], int, int)
 * @see       #stringWidth(String)
 */
public int bytesWidth(byte data[], int off, int len) {
    return stringWidth(new String(data, 0, off, len));
}

/**
 * 获取 <code>Font</code> 中前 256 个字符的前进宽度。前进宽度是从字符基线的最左点到最右点的距离。注意，字符串的前进宽度不一定等于其字符前进宽度的总和。
 * @return    一个存储该 <code>Font</code> 中字符前进宽度的数组。
 * @see       #stringWidth(String)
 */
public int[] getWidths() {
    int widths[] = new int[256];
    for (char ch = 0 ; ch < 256 ; ch++) {
        widths[ch] = charWidth(ch);
    }
    return widths;
}

/**
 * 检查 <code>Font</code> 是否具有统一的行度量。复合字体可能由几种不同的字体组成，以覆盖各种字符集。在这种情况下，<code>FontLineMetrics</code> 对象是不统一的。
 * 不同的字体可能具有不同的上升高度、下降高度、度量等。这些信息有时对于行测量和行断开是必要的。
 * @return <code>true</code> 如果字体具有统一的行度量；<code>false</code> 否则。
 * @see java.awt.Font#hasUniformLineMetrics()
 */
public boolean hasUniformLineMetrics() {
    return font.hasUniformLineMetrics();
}

/**
 * 返回指定 <code>String</code> 在指定 <code>Graphics</code> 上下文中的 {@link LineMetrics} 对象。
 * @param str 指定的 <code>String</code>
 * @param context 指定的 <code>Graphics</code> 上下文
 * @return 一个使用指定 <code>String</code> 和 <code>Graphics</code> 上下文创建的 <code>LineMetrics</code> 对象。
 * @see java.awt.Font#getLineMetrics(String, FontRenderContext)
 */
public LineMetrics getLineMetrics( String str, Graphics context) {
    return font.getLineMetrics(str, myFRC(context));
}

/**
 * 返回指定 <code>String</code> 在指定 <code>Graphics</code> 上下文中的 {@link LineMetrics} 对象。
 * @param str 指定的 <code>String</code>
 * @param beginIndex <code>str</code> 的初始偏移量
 * @param limit <code>str</code> 的结束偏移量
 * @param context 指定的 <code>Graphics</code> 上下文
 * @return 一个使用指定 <code>String</code> 和 <code>Graphics</code> 上下文创建的 <code>LineMetrics</code> 对象。
 * @see java.awt.Font#getLineMetrics(String, int, int, FontRenderContext)
 */
public LineMetrics getLineMetrics( String str,
                                    int beginIndex, int limit,
                                    Graphics context) {
    return font.getLineMetrics(str, beginIndex, limit, myFRC(context));
}

/**
 * 返回指定字符数组在指定 <code>Graphics</code> 上下文中的 {@link LineMetrics} 对象。
 * @param chars 指定的字符数组
 * @param beginIndex <code>chars</code> 的初始偏移量
 * @param limit <code>chars</code> 的结束偏移量
 * @param context 指定的 <code>Graphics</code> 上下文
 * @return 一个使用指定字符数组和 <code>Graphics</code> 上下文创建的 <code>LineMetrics</code> 对象。
 * @see java.awt.Font#getLineMetrics(char[], int, int, FontRenderContext)
 */
public LineMetrics getLineMetrics(char [] chars,
                                    int beginIndex, int limit,
                                    Graphics context) {
    return font.getLineMetrics(
                                chars, beginIndex, limit, myFRC(context));
}

/**
 * 返回指定 <code>CharacterIterator</code> 在指定 <code>Graphics</code> 上下文中的 {@link LineMetrics} 对象。
 * @param ci 指定的 <code>CharacterIterator</code>
 * @param beginIndex <code>ci</code> 中的初始偏移量
 * @param limit <code>ci</code> 的结束索引
 * @param context 指定的 <code>Graphics</code> 上下文
 * @return 一个使用指定参数创建的 <code>LineMetrics</code> 对象。
 * @see java.awt.Font#getLineMetrics(CharacterIterator, int, int, FontRenderContext)
 */
public LineMetrics getLineMetrics(CharacterIterator ci,
                                    int beginIndex, int limit,
                                    Graphics context) {
    return font.getLineMetrics(ci, beginIndex, limit, myFRC(context));
}

/**
 * 返回指定 <code>String</code> 在指定 <code>Graphics</code> 上下文中的边界。边界用于布局 <code>String</code>。
 * <p>注意：返回的边界是基线相对坐标（参见 {@link java.awt.FontMetrics 类说明}）。
 * @param str 指定的 <code>String</code>
 * @param context 指定的 <code>Graphics</code> 上下文
 * @return 一个表示指定 <code>String</code> 在指定 <code>Graphics</code> 上下文中的边界框的 {@link Rectangle2D}。
 * @see java.awt.Font#getStringBounds(String, FontRenderContext)
 */
public Rectangle2D getStringBounds( String str, Graphics context) {
    return font.getStringBounds(str, myFRC(context));
}

/**
 * 返回指定 <code>String</code> 在指定 <code>Graphics</code> 上下文中的边界。边界用于布局 <code>String</code>。
 * <p>注意：返回的边界是基线相对坐标（参见 {@link java.awt.FontMetrics 类说明}）。
 * @param str 指定的 <code>String</code>
 * @param beginIndex <code>str</code> 的起始偏移量
 * @param limit <code>str</code> 的结束偏移量
 * @param context 指定的 <code>Graphics</code> 上下文
 * @return 一个表示指定 <code>String</code> 在指定 <code>Graphics</code> 上下文中的边界框的 <code>Rectangle2D</code>。
 * @see java.awt.Font#getStringBounds(String, int, int, FontRenderContext)
 */
public Rectangle2D getStringBounds( String str,
                                    int beginIndex, int limit,
                                    Graphics context) {
    return font.getStringBounds(str, beginIndex, limit,
                                    myFRC(context));
}

/**
 * 返回指定字符数组在指定 <code>Graphics</code> 上下文中的边界。边界用于布局使用指定字符数组、<code>beginIndex</code> 和 <code>limit</code> 创建的 <code>String</code>。
 * <p>注意：返回的边界是基线相对坐标（参见 {@link java.awt.FontMetrics 类说明}）。
 * @param chars 字符数组
 * @param beginIndex 字符数组的初始偏移量
 * @param limit 字符数组的结束偏移量
 * @param context 指定的 <code>Graphics</code> 上下文
 * @return 一个表示指定字符数组在指定 <code>Graphics</code> 上下文中的边界框的 <code>Rectangle2D</code>。
 * @see java.awt.Font#getStringBounds(char[], int, int, FontRenderContext)
 */
public Rectangle2D getStringBounds( char [] chars,
                                    int beginIndex, int limit,
                                    Graphics context) {
    return font.getStringBounds(chars, beginIndex, limit,
                                    myFRC(context));
}

/**
 * 返回指定 <code>CharacterIterator</code> 中索引的字符在指定 <code>Graphics</code> 上下文中的边界。
 * <p>注意：返回的边界是基线相对坐标（参见 {@link java.awt.FontMetrics 类说明}）。
 * @param ci 指定的 <code>CharacterIterator</code>
 * @param beginIndex <code>ci</code> 中的初始偏移量
 * @param limit <code>ci</code> 的结束索引
 * @param context 指定的 <code>Graphics</code> 上下文
 * @return 一个表示指定 <code>CharacterIterator</code> 中索引的字符在指定 <code>Graphics</code> 上下文中的边界框的 <code>Rectangle2D</code>。
 * @see java.awt.Font#getStringBounds(CharacterIterator, int, int, FontRenderContext)
 */
public Rectangle2D getStringBounds(CharacterIterator ci,
                                    int beginIndex, int limit,
                                    Graphics context) {
    return font.getStringBounds(ci, beginIndex, limit,
                                    myFRC(context));
}

/**
 * 返回指定 <code>Graphics</code> 上下文中具有最大边界的字符的边界。
 * @param context 指定的 <code>Graphics</code> 上下文
 * @return 一个表示具有最大边界的字符的边界框的 <code>Rectangle2D</code>。
 * @see java.awt.Font#getMaxCharBounds(FontRenderContext)
 */
public Rectangle2D getMaxCharBounds(Graphics context) {
    return font.getMaxCharBounds(myFRC(context));
}

private FontRenderContext myFRC(Graphics context) {
    if (context instanceof Graphics2D) {
        return ((Graphics2D)context).getFontRenderContext();
    }
    return DEFAULT_FRC;
}

/**
 * 返回此 <code>FontMetrics</code> 对象值的 <code>String</code> 表示形式。
 * @return    此 <code>FontMetrics</code> 对象的 <code>String</code> 表示形式。
 * @since     JDK1.0.
 */
public String toString() {
    return getClass().getName() +
        "[font=" + getFont() +
        "ascent=" + getAscent() +
        ", descent=" + getDescent() +
        ", height=" + getHeight() + "]";
}

/**
 * 初始化 JNI 字段和方法 ID
 */
private static native void initIDs();
}
