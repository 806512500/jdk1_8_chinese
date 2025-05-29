
/*
 * 版权所有 (c) 1996, 2013, Oracle 和/或其附属公司。保留所有权利。
 * ORACLE 专有/机密。使用受许可条款约束。
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package java.io;

import java.lang.reflect.Field;
import sun.reflect.CallerSensitive;
import sun.reflect.Reflection;
import sun.reflect.misc.ReflectUtil;

/**
 * 一个来自可序列化类的可序列化字段的描述。一个 ObjectStreamFields 数组用于声明一个类的可序列化字段。
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
     * 使用指定类型创建一个可序列化字段。此字段应使用 <code>serialField</code> 标签进行文档化。
     *
     * @param   name 可序列化字段的名称
     * @param   type 可序列化字段的 <code>Class</code> 对象
     */
    public ObjectStreamField(String name, Class<?> type) {
        this(name, type, false);
    }

    /**
     * 创建一个表示具有给定名称和类型的可序列化字段的 ObjectStreamField。如果 unshared 为 false，字段值将以默认方式序列化和反序列化——如果字段是非基本类型，对象值将如同通过调用 writeObject 和 readObject 写入和读取一样进行序列化和反序列化。如果 unshared 为 true，字段值将如同通过调用 writeUnshared 和 readUnshared 写入和读取一样进行序列化和反序列化。
     *
     * @param   name 字段名称
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
     * 创建一个表示具有给定字段和指定非共享设置的 ObjectStreamField。为了与早期序列化实现的行为兼容，需要一个 "showType" 参数来控制是否在调用此 ObjectStreamField（如果非基本类型）的 getType() 时返回 Object.class（而不是更具体的引用类型）。
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
     * @return  表示可序列化字段名称的 <code>String</code>
     */
    public String getName() {
        return name;
    }

    /**
     * 获取字段的类型。如果类型是非基本类型且此 <code>ObjectStreamField</code> 是从反序列化的 {@link
     * ObjectStreamClass} 实例中获得的，则返回 <code>Object.class</code>。否则，返回字段类型的 <code>Class</code> 对象。
     *
     * @return  表示可序列化字段类型的 <code>Class</code> 对象
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
     * L            类或接口
     * S            short
     * Z            boolean
     * [            数组
     * </pre></blockquote>
     *
     * @return  可序列化字段的类型码
     */
    // 提醒：弃用？
    public char getTypeCode() {
        return signature.charAt(0);
    }


                /**
     * 返回JVM类型签名。
     *
     * @return  如果此字段具有基本类型，则返回null。
     */
    // 提醒：弃用？
    public String getTypeString() {
        return isPrimitive() ? null : signature;
    }

    /**
     * 实例数据中字段的偏移量。
     *
     * @return  此字段的偏移量
     * @see #setOffset
     */
    // 提醒：弃用？
    public int getOffset() {
        return offset;
    }

    /**
     * 实例数据中的偏移量。
     *
     * @param   offset 字段的偏移量
     * @see #getOffset
     */
    // 提醒：弃用？
    protected void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     * 如果此字段具有基本类型，则返回true。
     *
     * @return  如果且仅当此字段对应于基本类型时返回true
     */
    // 提醒：弃用？
    public boolean isPrimitive() {
        char tcode = signature.charAt(0);
        return ((tcode != 'L') && (tcode != '['));
    }

    /**
     * 返回一个布尔值，指示由此ObjectStreamField实例表示的可序列化字段是否未共享。
     *
     * @return 如果此字段未共享，则返回{@code true}
     *
     * @since 1.4
     */
    public boolean isUnshared() {
        return unshared;
    }

    /**
     * 比较此字段与另一个<code>ObjectStreamField</code>。如果此字段较小，则返回-1；如果相等，则返回0；如果较大，则返回1。
     * 基本类型比对象类型“小”。如果相等，则比较字段名称。
     */
    // 提醒：弃用？
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
     * 返回由此ObjectStreamField表示的字段，如果ObjectStreamField未与实际字段关联，则返回null。
     */
    Field getField() {
        return field;
    }

    /**
     * 返回字段的JVM类型签名（类似于getTypeString，但即使是基本字段也会返回签名字符串）。
     */
    String getSignature() {
        return signature;
    }

    /**
     * 返回给定类的JVM类型签名。
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
