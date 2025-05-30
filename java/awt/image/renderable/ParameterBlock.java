
/*
 * Copyright (c) 1998, 2004, Oracle and/or its affiliates. All rights reserved.
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

package java.awt.image.renderable;
import java.awt.image.RenderedImage;
import java.io.Serializable;
import java.util.Vector;

/**
 * <code>ParameterBlock</code> 封装了 RenderableImageOp 或其他处理图像的类所需的所有关于源和参数（对象）的信息。
 *
 * <p> 虽然可以在源 Vector 中放置任意对象，但此类的用户可能会施加语义约束，例如要求所有源都是 RenderedImages 或
 * RenderableImage。 <code>ParameterBlock</code> 本身只是一个容器，不对源或参数类型进行检查。
 *
 * <p> <code>ParameterBlock</code> 中的所有参数都是对象；提供了方便的添加和设置方法，这些方法接受基本类型的参数
 * 并构造适当的 Number 子类（如 Integer 或 Float）。对应的获取方法执行向下转型并返回基本类型；如果存储的值
 * 没有正确的类型，将抛出异常。无法区分 "short s; add(s)" 和 "add(new Short(s))" 的结果。
 *
 * <p> 注意，获取和设置方法操作的是引用。因此，必须小心不要在不适当的情况下在 <code>ParameterBlock</code> 之间共享引用。
 * 例如，要创建一个与旧的 <code>ParameterBlock</code> 相等的新 <code>ParameterBlock</code>，除了添加一个源之外，可能会
 * 想写：
 *
 * <pre>
 * ParameterBlock addSource(ParameterBlock pb, RenderableImage im) {
 *     ParameterBlock pb1 = new ParameterBlock(pb.getSources());
 *     pb1.addSource(im);
 *     return pb1;
 * }
 * </pre>
 *
 * <p> 这段代码将对原始 <code>ParameterBlock</code> 产生副作用，因为 getSources 操作返回了其源 Vector 的引用。
 * pb 和 pb1 共享源 Vector，任何一方的更改都对另一方可见。
 *
 * <p> 正确的编写 addSource 函数的方法是克隆源 Vector：
 *
 * <pre>
 * ParameterBlock addSource (ParameterBlock pb, RenderableImage im) {
 *     ParameterBlock pb1 = new ParameterBlock(pb.getSources().clone());
 *     pb1.addSource(im);
 *     return pb1;
 * }
 * </pre>
 *
 * <p> 为了这个原因，<code>ParameterBlock</code> 的 clone 方法被定义为克隆源和参数 Vector。浅克隆可通过
 * shallowClone 获得。
 *
 * <p> addSource、setSource、add 和 set 方法定义为在添加参数后返回 'this'。这允许使用如下语法：
 *
 * <pre>
 * ParameterBlock pb = new ParameterBlock();
 * op = new RenderableImageOp("operation", pb.add(arg1).add(arg2));
 * </pre>
 * */
public class ParameterBlock implements Cloneable, Serializable {
    /** 源的 Vector，存储为任意对象。 */
    protected Vector<Object> sources = new Vector<Object>();

    /** 非源参数的 Vector，存储为任意对象。 */
    protected Vector<Object> parameters = new Vector<Object>();

    /** 一个虚拟构造函数。 */
    public ParameterBlock() {}

    /**
     * 使用给定的源 Vector 构造 <code>ParameterBlock</code>。
     * @param sources 源图像的 <code>Vector</code>
     */
    public ParameterBlock(Vector<Object> sources) {
        setSources(sources);
    }

    /**
     * 使用给定的源 Vector 和参数 Vector 构造 <code>ParameterBlock</code>。
     * @param sources 源图像的 <code>Vector</code>
     * @param parameters 在渲染操作中使用的参数 <code>Vector</code>
     */
    public ParameterBlock(Vector<Object> sources,
                          Vector<Object> parameters)
    {
        setSources(sources);
        setParameters(parameters);
    }

    /**
     * 创建 <code>ParameterBlock</code> 的浅拷贝。源和参数 Vector 通过引用复制——添加或更改将对两个版本可见。
     *
     * @return <code>ParameterBlock</code> 的 Object 克隆。
     */
    public Object shallowClone() {
        try {
            return super.clone();
        } catch (Exception e) {
            // 我们不能在这里，因为我们实现了 Cloneable。
            return null;
        }
    }

    /**
     * 创建 <code>ParameterBlock</code> 的拷贝。源和参数 Vector 被克隆，但实际的源和参数通过引用复制。这允许在克隆中
     * 修改源和参数的顺序和数量对原始 <code>ParameterBlock</code> 不可见。对共享源或参数本身的更改仍然可见。
     *
     * @return <code>ParameterBlock</code> 的 Object 克隆。
     */
    public Object clone() {
        ParameterBlock theClone;

        try {
            theClone = (ParameterBlock) super.clone();
        } catch (Exception e) {
            // 我们不能在这里，因为我们实现了 Cloneable。
            return null;
        }

        if (sources != null) {
            theClone.setSources((Vector)sources.clone());
        }
        if (parameters != null) {
            theClone.setParameters((Vector)parameters.clone());
        }
        return (Object) theClone;
    }

    /**
     * 将图像添加到源列表的末尾。图像以对象形式存储，以便将来支持新的节点类型。
     *
     * @param source 要存储在源列表中的图像对象。
     * @return 包含指定 <code>source</code> 的新 <code>ParameterBlock</code>。
     */
    public ParameterBlock addSource(Object source) {
        sources.addElement(source);
        return this;
    }

    /**
     * 返回一个源作为通用对象。调用者必须将其转换为适当的类型。
     *
     * @param index 要返回的源的索引。
     * @return 位于 <code>sources</code> <code>Vector</code> 指定索引处的 <code>Object</code>。
     * @see #setSource(Object, int)
     */
    public Object getSource(int index) {
        return sources.elementAt(index);
    }

    /**
     * 用新源替换源列表中的条目。如果索引超出当前源列表，列表将使用 null 扩展。
     * @param source 指定的源图像
     * @param index 要插入指定 <code>source</code> 的 <code>sources</code>
     *              <code>Vector</code> 的索引
     * @return 包含指定 <code>source</code> 的新 <code>ParameterBlock</code>，位于指定的
     *         <code>index</code>。
     * @see #getSource(int)
     */
    public ParameterBlock setSource(Object source, int index) {
        int oldSize = sources.size();
        int newSize = index + 1;
        if (oldSize < newSize) {
            sources.setSize(newSize);
        }
        sources.setElementAt(source, index);
        return this;
    }

    /**
     * 返回一个 <code>RenderedImage</code> 源。此方法是一个便利方法。
     * 如果源不是 RenderedImage，将抛出异常。
     *
     * @param index 要返回的源的索引
     * @return 位于 <code>sources</code> <code>Vector</code> 指定索引处的 <code>RenderedImage</code>。
     */
    public RenderedImage getRenderedSource(int index) {
        return (RenderedImage) sources.elementAt(index);
    }

    /**
     * 返回一个 RenderableImage 源。此方法是一个便利方法。
     * 如果源不是 RenderableImage，将抛出异常。
     *
     * @param index 要返回的源的索引
     * @return 位于 <code>sources</code> <code>Vector</code> 指定索引处的 <code>RenderableImage</code>。
     */
    public RenderableImage getRenderableSource(int index) {
        return (RenderableImage) sources.elementAt(index);
    }

    /**
     * 返回源图像的数量。
     * @return <code>sources</code> <code>Vector</code> 中的源图像数量。
     */
    public int getNumSources() {
        return sources.size();
    }

    /**
     * 返回整个源 Vector。
     * @return <code>sources</code> <code>Vector</code>。
     * @see #setSources(Vector)
     */
    public Vector<Object> getSources() {
        return sources;
    }

    /**
     * 将整个源 Vector 设置为给定的 Vector。
     * @param sources 源图像的 <code>Vector</code>
     * @see #getSources
     */
    public void setSources(Vector<Object> sources) {
        this.sources = sources;
    }

    /** 清除源图像列表。 */
    public void removeSources() {
        sources = new Vector();
    }

    /**
     * 返回参数的数量（不包括源图像）。
     * @return <code>parameters</code> <code>Vector</code> 中的参数数量。
     */
    public int getNumParameters() {
        return parameters.size();
    }

    /**
     * 返回整个参数 Vector。
     * @return <code>parameters</code> <code>Vector</code>。
     * @see #setParameters(Vector)
     */
    public Vector<Object> getParameters() {
        return parameters;
    }

    /**
     * 将整个参数 Vector 设置为给定的 Vector。
     * @param parameters 指定的 <code>Vector</code> 参数
     * @see #getParameters
     */
    public void setParameters(Vector<Object> parameters) {
        this.parameters = parameters;
    }

    /** 清除参数列表。 */
    public void removeParameters() {
        parameters = new Vector();
    }

    /**
     * 将对象添加到参数列表中。
     * @param obj 要添加到 <code>parameters</code> <code>Vector</code> 的 <code>Object</code>
     * @return 包含指定参数的新 <code>ParameterBlock</code>。
     */
    public ParameterBlock add(Object obj) {
        parameters.addElement(obj);
        return this;
    }

    /**
     * 将 Byte 添加到参数列表中。
     * @param b 要添加到 <code>parameters</code> <code>Vector</code> 的字节
     * @return 包含指定参数的新 <code>ParameterBlock</code>。
     */
    public ParameterBlock add(byte b) {
        return add(new Byte(b));
    }

    /**
     * 将 Character 添加到参数列表中。
     * @param c 要添加到 <code>parameters</code> <code>Vector</code> 的字符
     * @return 包含指定参数的新 <code>ParameterBlock</code>。
     */
    public ParameterBlock add(char c) {
        return add(new Character(c));
    }

    /**
     * 将 Short 添加到参数列表中。
     * @param s 要添加到 <code>parameters</code> <code>Vector</code> 的短整型
     * @return 包含指定参数的新 <code>ParameterBlock</code>。
     */
    public ParameterBlock add(short s) {
        return add(new Short(s));
    }

    /**
     * 将 Integer 添加到参数列表中。
     * @param i 要添加到 <code>parameters</code> <code>Vector</code> 的整型
     * @return 包含指定参数的新 <code>ParameterBlock</code>。
     */
    public ParameterBlock add(int i) {
        return add(new Integer(i));
    }

    /**
     * 将 Long 添加到参数列表中。
     * @param l 要添加到 <code>parameters</code> <code>Vector</code> 的长整型
     * @return 包含指定参数的新 <code>ParameterBlock</code>。
     */
    public ParameterBlock add(long l) {
        return add(new Long(l));
    }

    /**
     * 将 Float 添加到参数列表中。
     * @param f 要添加到 <code>parameters</code> <code>Vector</code> 的浮点型
     * @return 包含指定参数的新 <code>ParameterBlock</code>。
     */
    public ParameterBlock add(float f) {
        return add(new Float(f));
    }

    /**
     * 将 Double 添加到参数列表中。
     * @param d 要添加到 <code>parameters</code> <code>Vector</code> 的双精度浮点型
     * @return 包含指定参数的新 <code>ParameterBlock</code>。
     */
    public ParameterBlock add(double d) {
        return add(new Double(d));
    }

    /**
     * 用对象替换参数列表中的对象。如果索引超出当前源列表，列表将使用 null 扩展。
     * @param obj 替换 <code>parameters</code> <code>Vector</code> 中指定索引处参数的参数
     * @param index 要替换的参数的索引
     * @return 包含指定参数的新 <code>ParameterBlock</code>。
     */
    public ParameterBlock set(Object obj, int index) {
        int oldSize = parameters.size();
        int newSize = index + 1;
        if (oldSize < newSize) {
            parameters.setSize(newSize);
        }
        parameters.setElementAt(obj, index);
        return this;
    }

    /**
     * 用 Byte 替换参数列表中的对象。如果索引超出当前源列表，列表将使用 null 扩展。
     * @param b 替换 <code>parameters</code> <code>Vector</code> 中指定索引处参数的参数
     * @param index 要替换的参数的索引
     * @return 包含指定参数的新 <code>ParameterBlock</code>。
     */
    public ParameterBlock set(byte b, int index) {
        return set(new Byte(b), index);
    }


                /**
     * 用一个字符替换参数列表中的对象。
     * 如果索引超出当前源列表的范围，
     * 则根据需要扩展列表，填充 null。
     * @param c 用于替换指定索引处参数的参数
     *        在 <code>parameters</code> <code>Vector</code> 中
     * @param index 要被指定参数替换的参数的索引
     * @return 包含指定参数的新 <code>ParameterBlock</code>。
     */
    public ParameterBlock set(char c, int index) {
        return set(new Character(c), index);
    }

    /**
     * 用一个短整型替换参数列表中的对象。
     * 如果索引超出当前源列表的范围，
     * 则根据需要扩展列表，填充 null。
     * @param s 用于替换指定索引处参数的参数
     *        在 <code>parameters</code> <code>Vector</code> 中
     * @param index 要被指定参数替换的参数的索引
     * @return 包含指定参数的新 <code>ParameterBlock</code>。
     */
    public ParameterBlock set(short s, int index) {
        return set(new Short(s), index);
    }

    /**
     * 用一个整型替换参数列表中的对象。
     * 如果索引超出当前源列表的范围，
     * 则根据需要扩展列表，填充 null。
     * @param i 用于替换指定索引处参数的参数
     *        在 <code>parameters</code> <code>Vector</code> 中
     * @param index 要被指定参数替换的参数的索引
     * @return 包含指定参数的新 <code>ParameterBlock</code>。
     */
    public ParameterBlock set(int i, int index) {
        return set(new Integer(i), index);
    }

    /**
     * 用一个长整型替换参数列表中的对象。
     * 如果索引超出当前源列表的范围，
     * 则根据需要扩展列表，填充 null。
     * @param l 用于替换指定索引处参数的参数
     *        在 <code>parameters</code> <code>Vector</code> 中
     * @param index 要被指定参数替换的参数的索引
     * @return 包含指定参数的新 <code>ParameterBlock</code>。
     */
    public ParameterBlock set(long l, int index) {
        return set(new Long(l), index);
    }

    /**
     * 用一个浮点型替换参数列表中的对象。
     * 如果索引超出当前源列表的范围，
     * 则根据需要扩展列表，填充 null。
     * @param f 用于替换指定索引处参数的参数
     *        在 <code>parameters</code> <code>Vector</code> 中
     * @param index 要被指定参数替换的参数的索引
     * @return 包含指定参数的新 <code>ParameterBlock</code>。
     */
    public ParameterBlock set(float f, int index) {
        return set(new Float(f), index);
    }

    /**
     * 用一个双精度浮点型替换参数列表中的对象。
     * 如果索引超出当前源列表的范围，
     * 则根据需要扩展列表，填充 null。
     * @param d 用于替换指定索引处参数的参数
     *        在 <code>parameters</code> <code>Vector</code> 中
     * @param index 要被指定参数替换的参数的索引
     * @return 包含指定参数的新 <code>ParameterBlock</code>。
     */
    public ParameterBlock set(double d, int index) {
        return set(new Double(d), index);
    }

    /**
     * 获取参数作为对象。
     * @param index 要获取的参数的索引
     * @return 代表指定索引处参数的 <code>Object</code>。
     */
    public Object getObjectParameter(int index) {
        return parameters.elementAt(index);
    }

    /**
     * 一个方便的方法，用于返回参数作为字节。如果参数是
     * <code>null</code> 或不是一个 <code>Byte</code>，则抛出异常。
     *
     * @param index 要返回的参数的索引。
     * @return 指定索引处的参数
     *         作为 <code>byte</code> 值。
     * @throws ClassCastException 如果指定索引处的参数不是 <code>Byte</code>
     * @throws NullPointerException 如果指定索引处的参数是 <code>null</code>
     * @throws ArrayIndexOutOfBoundsException 如果 <code>index</code>
     *         为负或不小于此 <code>ParameterBlock</code> 对象的当前大小
     */
    public byte getByteParameter(int index) {
        return ((Byte)parameters.elementAt(index)).byteValue();
    }

    /**
     * 一个方便的方法，用于返回参数作为字符。如果参数是
     * <code>null</code> 或不是一个 <code>Character</code>，则抛出异常。
     *
     * @param index 要返回的参数的索引。
     * @return 指定索引处的参数
     *         作为 <code>char</code> 值。
     * @throws ClassCastException 如果指定索引处的参数不是 <code>Character</code>
     * @throws NullPointerException 如果指定索引处的参数是 <code>null</code>
     * @throws ArrayIndexOutOfBoundsException 如果 <code>index</code>
     *         为负或不小于此 <code>ParameterBlock</code> 对象的当前大小
     */
    public char getCharParameter(int index) {
        return ((Character)parameters.elementAt(index)).charValue();
    }

    /**
     * 一个方便的方法，用于返回参数作为短整型。如果参数是
     * <code>null</code> 或不是一个 <code>Short</code>，则抛出异常。
     *
     * @param index 要返回的参数的索引。
     * @return 指定索引处的参数
     *         作为 <code>short</code> 值。
     * @throws ClassCastException 如果指定索引处的参数不是 <code>Short</code>
     * @throws NullPointerException 如果指定索引处的参数是 <code>null</code>
     * @throws ArrayIndexOutOfBoundsException 如果 <code>index</code>
     *         为负或不小于此 <code>ParameterBlock</code> 对象的当前大小
     */
    public short getShortParameter(int index) {
        return ((Short)parameters.elementAt(index)).shortValue();
    }

    /**
     * 一个方便的方法，用于返回参数作为整型。如果参数是
     * <code>null</code> 或不是一个 <code>Integer</code>，则抛出异常。
     *
     * @param index 要返回的参数的索引。
     * @return 指定索引处的参数
     *         作为 <code>int</code> 值。
     * @throws ClassCastException 如果指定索引处的参数不是 <code>Integer</code>
     * @throws NullPointerException 如果指定索引处的参数是 <code>null</code>
     * @throws ArrayIndexOutOfBoundsException 如果 <code>index</code>
     *         为负或不小于此 <code>ParameterBlock</code> 对象的当前大小
     */
    public int getIntParameter(int index) {
        return ((Integer)parameters.elementAt(index)).intValue();
    }

    /**
     * 一个方便的方法，用于返回参数作为长整型。如果参数是
     * <code>null</code> 或不是一个 <code>Long</code>，则抛出异常。
     *
     * @param index 要返回的参数的索引。
     * @return 指定索引处的参数
     *         作为 <code>long</code> 值。
     * @throws ClassCastException 如果指定索引处的参数不是 <code>Long</code>
     * @throws NullPointerException 如果指定索引处的参数是 <code>null</code>
     * @throws ArrayIndexOutOfBoundsException 如果 <code>index</code>
     *         为负或不小于此 <code>ParameterBlock</code> 对象的当前大小
     */
    public long getLongParameter(int index) {
        return ((Long)parameters.elementAt(index)).longValue();
    }

    /**
     * 一个方便的方法，用于返回参数作为浮点型。如果参数是
     * <code>null</code> 或不是一个 <code>Float</code>，则抛出异常。
     *
     * @param index 要返回的参数的索引。
     * @return 指定索引处的参数
     *         作为 <code>float</code> 值。
     * @throws ClassCastException 如果指定索引处的参数不是 <code>Float</code>
     * @throws NullPointerException 如果指定索引处的参数是 <code>null</code>
     * @throws ArrayIndexOutOfBoundsException 如果 <code>index</code>
     *         为负或不小于此 <code>ParameterBlock</code> 对象的当前大小
     */
    public float getFloatParameter(int index) {
        return ((Float)parameters.elementAt(index)).floatValue();
    }

    /**
     * 一个方便的方法，用于返回参数作为双精度浮点型。如果参数是
     * <code>null</code> 或不是一个 <code>Double</code>，则抛出异常。
     *
     * @param index 要返回的参数的索引。
     * @return 指定索引处的参数
     *         作为 <code>double</code> 值。
     * @throws ClassCastException 如果指定索引处的参数不是 <code>Double</code>
     * @throws NullPointerException 如果指定索引处的参数是 <code>null</code>
     * @throws ArrayIndexOutOfBoundsException 如果 <code>index</code>
     *         为负或不小于此 <code>ParameterBlock</code> 对象的当前大小
     */
    public double getDoubleParameter(int index) {
        return ((Double)parameters.elementAt(index)).doubleValue();
    }

    /**
     * 返回描述参数类型的类对象数组。
     * @return <code>Class</code> 对象数组。
     */
    public Class [] getParamClasses() {
        int numParams = getNumParameters();
        Class [] classes = new Class[numParams];
        int i;

        for (i = 0; i < numParams; i++) {
            Object obj = getObjectParameter(i);
            if (obj instanceof Byte) {
              classes[i] = byte.class;
            } else if (obj instanceof Character) {
              classes[i] = char.class;
            } else if (obj instanceof Short) {
              classes[i] = short.class;
            } else if (obj instanceof Integer) {
              classes[i] = int.class;
            } else if (obj instanceof Long) {
              classes[i] = long.class;
            } else if (obj instanceof Float) {
              classes[i] = float.class;
            } else if (obj instanceof Double) {
              classes[i] = double.class;
            } else {
              classes[i] = obj.getClass();
            }
        }

        return classes;
    }
}
