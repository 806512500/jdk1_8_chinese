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

package java.io;

import java.lang.reflect.Field;
import sun.reflect.CallerSensitive;
import sun.reflect.Reflection;
import sun.reflect.misc.ReflectUtil;

/**
 * 可序列化类中的一个可序列化字段的描述。一个 ObjectStreamFields 数组用于声明类的可序列化字段。
 *
 * @author      Mike Warres
 * @author      Roger Riggs
 * @see ObjectStreamClass
 * @since 1.2
 */
public class ObjectStreamField
    implements Comparable<Object>
{

    /** 字段名 */
    private final String name;
    /** 字段类型的规范 JVM 签名 */
    private final String signature;
    /** 字段类型（如果未知的非基本类型则为 Object.class） */
    private final Class<?> type;
    /** 是否以非共享方式（反）序列化字段值 */
    private final boolean unshared;
    /** 对应的反射字段对象，如果有的话 */
    private final Field field;
    /** 字段值在包含字段组中的偏移量 */
    private int offset = 0;

    /**
     * 创建一个具有指定类型的可序列化字段。此字段应使用 <code>serialField</code> 标签进行文档化。
     *
     * @param   name 可序列化字段的名称
     * @param   type 可序列化字段的 <code>Class</code> 对象
     */
    public ObjectStreamField(String name, Class<?> type) {
        this(name, type, false);
    }

    /**
     * 创建一个表示具有给定名称和类型的可序列化字段的 ObjectStreamField。如果 unshared 为 false，字段值将以默认方式序列化和反序列化——如果字段是非基本类型，对象值将像调用 writeObject 和 readObject 一样进行序列化和反序列化。如果 unshared 为 true，字段值将像调用 writeUnshared 和 readUnshared 一样进行序列化和反序列化。
     *
     * @param   name 字段名
     * @param   type 字段类型
     * @param   unshared 如果为 false，则以与 writeObject/readObject 相同的方式写入/读取字段值；如果为 true，则以与 writeUnshared/readUnshared 相同的方式写入/读取
     * @since   1.4
     */
    public ObjectStreamField(String name, Class<?> type, boolean unshared) {
        if (name == null) {
            throw new NullPointerException();
        }
        this.name = name;
        this.type = type;
        this.unshared = unshared;
        signature = getClassSignature(type).intern();
        field = null;
    }

    /**
     * 创建一个表示具有给定名称、签名和非共享设置的字段的 ObjectStreamField。
     */
    ObjectStreamField(String name, String signature, boolean unshared) {
        if (name == null) {
            throw new NullPointerException();
        }
        this.name = name;
        this.signature = signature.intern();
        this.unshared = unshared;
        field = null;

        switch (signature.charAt(0)) {
            case 'Z': type = Boolean.TYPE; break;
            case 'B': type = Byte.TYPE; break;
            case 'C': type = Character.TYPE; break;
            case 'S': type = Short.TYPE; break;
            case 'I': type = Integer.TYPE; break;
            case 'J': type = Long.TYPE; break;
            case 'F': type = Float.TYPE; break;
            case 'D': type = Double.TYPE; break;
            case 'L':
            case '[': type = Object.class; break;
            default: throw new IllegalArgumentException("非法签名");
        }
    }

    /**
     * 创建一个表示具有给定字段和指定非共享设置的 ObjectStreamField。为了与早期序列化实现的行为兼容，需要一个 "showType" 参数来控制 getType() 调用（如果非基本类型）是否返回 Object.class（而不是更具体的引用类型）。
     */
    ObjectStreamField(Field field, boolean unshared, boolean showType) {
        this.field = field;
        this.unshared = unshared;
        name = field.getName();
        Class<?> ftype = field.getType();
        type = (showType || ftype.isPrimitive()) ? ftype : Object.class;
        signature = getClassSignature(ftype).intern();
    }

    /**
     * 获取此字段的名称。
     *
     * @return  一个表示可序列化字段名称的 <code>String</code>
     */
    public String getName() {
        return name;
    }

    /**
     * 获取字段的类型。如果类型是非基本类型且此 <code>ObjectStreamField</code> 是从反序列化的 {@link ObjectStreamClass} 实例中获得的，则返回 <code>Object.class</code>。否则，返回字段类型的 <code>Class</code> 对象。
     *
     * @return  一个表示可序列化字段类型的 <code>Class</code> 对象
     */
    @CallerSensitive
    public Class<?> getType() {
        if (System.getSecurityManager() != null) {
            Class<?> caller = Reflection.getCallerClass();
            if (ReflectUtil.needsPackageAccessCheck(caller.getClassLoader(), type.getClassLoader())) {
                ReflectUtil.checkPackageAccess(type);
            }
        }
        return type;
    }

    /**
     * 返回字段类型的字符编码。编码如下：
     * <blockquote><pre>
     * B            byte
     * C            char
     * D            double
     * F            float
     * I            int
     * J            long
     * L            class 或 interface
     * S            short
     * Z            boolean
     * [            array
     * </pre></blockquote>
     *
     * @return  可序列化字段的类型码
     */
    // REMIND: deprecate?
    public char getTypeCode() {
        return signature.charAt(0);
    }

    /**
     * 返回 JVM 类型签名。
     *
     * @return  如果此字段具有基本类型，则返回 null。
     */
    // REMIND: deprecate?
    public String getTypeString() {
        return isPrimitive() ? null : signature;
    }

    /**
     * 字段在实例数据中的偏移量。
     *
     * @return  此字段的偏移量
     * @see #setOffset
     */
    // REMIND: deprecate?
    public int getOffset() {
        return offset;
    }

    /**
     * 字段在实例数据中的偏移量。
     *
     * @param   offset 字段的偏移量
     * @see #getOffset
     */
    // REMIND: deprecate?
    protected void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     * 返回此字段是否具有基本类型。
     *
     * @return  如果且仅如果此字段对应于基本类型，则返回 true
     */
    // REMIND: deprecate?
    public boolean isPrimitive() {
        char tcode = signature.charAt(0);
        return ((tcode != 'L') && (tcode != '['));
    }

    /**
     * 返回一个布尔值，指示此 ObjectStreamField 实例表示的可序列化字段是否为非共享。
     *
     * @return 如果此字段是非共享的，则返回 {@code true}
     *
     * @since 1.4
     */
    public boolean isUnshared() {
        return unshared;
    }

    /**
     * 将此字段与另一个 <code>ObjectStreamField</code> 进行比较。如果此字段较小，则返回 -1；如果相等，则返回 0；如果较大，则返回 1。基本类型小于对象类型。如果相等，则比较字段名称。
     */
    // REMIND: deprecate?
    public int compareTo(Object obj) {
        ObjectStreamField other = (ObjectStreamField) obj;
        boolean isPrim = isPrimitive();
        if (isPrim != other.isPrimitive()) {
            return isPrim ? -1 : 1;
        }
        return name.compareTo(other.name);
    }

    /**
     * 返回描述此字段的字符串。
     */
    public String toString() {
        return signature + ' ' + name;
    }

    /**
     * 返回此 ObjectStreamField 表示的字段，如果 ObjectStreamField 未与实际字段关联，则返回 null。
     */
    Field getField() {
        return field;
    }

    /**
     * 返回字段的 JVM 类型签名（类似于 getTypeString，但对基本字段也返回签名字符串）。
     */
    String getSignature() {
        return signature;
    }

    /**
     * 返回给定类的 JVM 类型签名。
     */
    private static String getClassSignature(Class<?> cl) {
        StringBuilder sbuf = new StringBuilder();
        while (cl.isArray()) {
            sbuf.append('[');
            cl = cl.getComponentType();
        }
        if (cl.isPrimitive()) {
            if (cl == Integer.TYPE) {
                sbuf.append('I');
            } else if (cl == Byte.TYPE) {
                sbuf.append('B');
            } else if (cl == Long.TYPE) {
                sbuf.append('J');
            } else if (cl == Float.TYPE) {
                sbuf.append('F');
            } else if (cl == Double.TYPE) {
                sbuf.append('D');
            } else if (cl == Short.TYPE) {
                sbuf.append('S');
            } else if (cl == Character.TYPE) {
                sbuf.append('C');
            } else if (cl == Boolean.TYPE) {
                sbuf.append('Z');
            } else if (cl == Void.TYPE) {
                sbuf.append('V');
            } else {
                throw new InternalError();
            }
        } else {
            sbuf.append('L' + cl.getName().replace('.', '/') + ';');
        }
        return sbuf.toString();
    }
}
