
/*
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
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
package java.beans;

import com.sun.beans.finder.PrimitiveWrapperMap;

import java.awt.AWTKeyStroke;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.font.TextAttribute;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.InvocationTargetException;

import java.security.AccessController;
import java.security.PrivilegedAction;

import java.util.*;

import javax.swing.Box;
import javax.swing.JLayeredPane;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.ColorUIResource;

import sun.swing.PrintColorUIResource;

import static sun.reflect.misc.ReflectUtil.isPackageAccessible;

/*
 * 与 <code>Intropector</code> 类似，<code>MetaData</code> 类
 * 包含描述类应如何通过其自身的公共 API 表达其状态的 <em>元</em> 对象。
 *
 * @see java.beans.Intropector
 *
 * @author Philip Milne
 * @author Steve Langley
 */
class MetaData {

static final class NullPersistenceDelegate extends PersistenceDelegate {
    // 注意这将被所有类调用，当它们到达其超类链的顶部时。
    protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
    }
    protected Expression instantiate(Object oldInstance, Encoder out) { return null; }

    public void writeObject(Object oldInstance, Encoder out) {
    // System.out.println("NullPersistenceDelegate:writeObject " + oldInstance);
    }
}

/**
 * <CODE>enum</CODE> 类的持久性代理。
 *
 * @author Sergey A. Malenkov
 */
static final class EnumPersistenceDelegate extends PersistenceDelegate {
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        return oldInstance == newInstance;
    }

    protected Expression instantiate(Object oldInstance, Encoder out) {
        Enum<?> e = (Enum<?>) oldInstance;
        return new Expression(e, Enum.class, "valueOf", new Object[]{e.getDeclaringClass(), e.name()});
    }
}

static final class PrimitivePersistenceDelegate extends PersistenceDelegate {
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        return oldInstance.equals(newInstance);
    }

    protected Expression instantiate(Object oldInstance, Encoder out) {
        return new Expression(oldInstance, oldInstance.getClass(),
                  "new", new Object[]{oldInstance.toString()});
    }
}

static final class ArrayPersistenceDelegate extends PersistenceDelegate {
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        return (newInstance != null &&
                oldInstance.getClass() == newInstance.getClass() && // 也确保子类型正确。
                Array.getLength(oldInstance) == Array.getLength(newInstance));
        }

    protected Expression instantiate(Object oldInstance, Encoder out) {
        // System.out.println("instantiate: " + type + " " + oldInstance);
        Class<?> oldClass = oldInstance.getClass();
        return new Expression(oldInstance, Array.class, "newInstance",
                   new Object[]{oldClass.getComponentType(),
                                new Integer(Array.getLength(oldInstance))});
        }

    protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
        int n = Array.getLength(oldInstance);
        for (int i = 0; i < n; i++) {
            Object index = new Integer(i);
            // Expression oldGetExp = new Expression(Array.class, "get", new Object[]{oldInstance, index});
            // Expression newGetExp = new Expression(Array.class, "get", new Object[]{newInstance, index});
            Expression oldGetExp = new Expression(oldInstance, "get", new Object[]{index});
            Expression newGetExp = new Expression(newInstance, "get", new Object[]{index});
            try {
                Object oldValue = oldGetExp.getValue();
                Object newValue = newGetExp.getValue();
                out.writeExpression(oldGetExp);
                if (!Objects.equals(newValue, out.get(oldValue))) {
                    // System.out.println("Not equal: " + newGetExp + " != " + actualGetExp);
                    // invokeStatement(Array.class, "set", new Object[]{oldInstance, index, oldValue}, out);
                    DefaultPersistenceDelegate.invokeStatement(oldInstance, "set", new Object[]{index, oldValue}, out);
                }
            }
            catch (Exception e) {
                // System.err.println("Warning:: failed to write: " + oldGetExp);
                out.getExceptionListener().exceptionThrown(e);
            }
        }
    }
}

static final class ProxyPersistenceDelegate extends PersistenceDelegate {
    protected Expression instantiate(Object oldInstance, Encoder out) {
        Class<?> type = oldInstance.getClass();
        java.lang.reflect.Proxy p = (java.lang.reflect.Proxy)oldInstance;
        // 这个不太美观的 hack 不是必需的，但它使 EventHandler 的表示更加简洁。
        java.lang.reflect.InvocationHandler ih = java.lang.reflect.Proxy.getInvocationHandler(p);
        if (ih instanceof EventHandler) {
            EventHandler eh = (EventHandler)ih;
            Vector<Object> args = new Vector<>();
            args.add(type.getInterfaces()[0]);
            args.add(eh.getTarget());
            args.add(eh.getAction());
            if (eh.getEventPropertyName() != null) {
                args.add(eh.getEventPropertyName());
            }
            if (eh.getListenerMethodName() != null) {
                args.setSize(4);
                args.add(eh.getListenerMethodName());
            }
            return new Expression(oldInstance,
                                  EventHandler.class,
                                  "create",
                                  args.toArray());
        }
        return new Expression(oldInstance,
                              java.lang.reflect.Proxy.class,
                              "newProxyInstance",
                              new Object[]{type.getClassLoader(),
                                           type.getInterfaces(),
                                           ih});
    }
}

// 字符串
static final class java_lang_String_PersistenceDelegate extends PersistenceDelegate {
    protected Expression instantiate(Object oldInstance, Encoder out) { return null; }

    public void writeObject(Object oldInstance, Encoder out) {
        // System.out.println("NullPersistenceDelegate:writeObject " + oldInstance);
    }
}

// 类
static final class java_lang_Class_PersistenceDelegate extends PersistenceDelegate {
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        return oldInstance.equals(newInstance);
    }

    protected Expression instantiate(Object oldInstance, Encoder out) {
        Class<?> c = (Class)oldInstance;
        // 从 1.3 开始，无法调用 Class.forName("int")，
        // 因此我们必须为基本类型生成不同的代码。
        // 这对于子类型可能是基本类型的数组是必需的。
        if (c.isPrimitive()) {
            Field field = null;
            try {
                field = PrimitiveWrapperMap.getType(c.getName()).getDeclaredField("TYPE");
            } catch (NoSuchFieldException ex) {
                System.err.println("Unknown primitive type: " + c);
            }
            return new Expression(oldInstance, field, "get", new Object[]{null});
        }
        else if (oldInstance == String.class) {
            return new Expression(oldInstance, "", "getClass", new Object[]{});
        }
        else if (oldInstance == Class.class) {
            return new Expression(oldInstance, String.class, "getClass", new Object[]{});
        }
        else {
            Expression newInstance = new Expression(oldInstance, Class.class, "forName", new Object[] { c.getName() });
            newInstance.loader = c.getClassLoader();
            return newInstance;
        }
    }
}

// 字段
static final class java_lang_reflect_Field_PersistenceDelegate extends PersistenceDelegate {
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        return oldInstance.equals(newInstance);
    }

    protected Expression instantiate(Object oldInstance, Encoder out) {
        Field f = (Field)oldInstance;
        return new Expression(oldInstance,
                f.getDeclaringClass(),
                "getField",
                new Object[]{f.getName()});
    }
}

// 方法
static final class java_lang_reflect_Method_PersistenceDelegate extends PersistenceDelegate {
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        return oldInstance.equals(newInstance);
    }

    protected Expression instantiate(Object oldInstance, Encoder out) {
        Method m = (Method)oldInstance;
        return new Expression(oldInstance,
                m.getDeclaringClass(),
                "getMethod",
                new Object[]{m.getName(), m.getParameterTypes()});
    }
}

// 日期

/**
 * <CODE>java.util.Date</CODE> 类的持久性代理。
 * 为了提高性能和避免与 <CODE>java.sql.Date</CODE>、
 * <CODE>java.sql.Time</CODE> 和 <CODE>java.sql.Timestamp</CODE> 的问题，
 * 不要扩展 DefaultPersistenceDelegate。
 *
 * @author Sergey A. Malenkov
 */
static class java_util_Date_PersistenceDelegate extends PersistenceDelegate {
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        if (!super.mutatesTo(oldInstance, newInstance)) {
            return false;
        }
        Date oldDate = (Date)oldInstance;
        Date newDate = (Date)newInstance;

        return oldDate.getTime() == newDate.getTime();
    }

    protected Expression instantiate(Object oldInstance, Encoder out) {
        Date date = (Date)oldInstance;
        return new Expression(date, date.getClass(), "new", new Object[] {date.getTime()});
    }
}

/**
 * <CODE>java.sql.Timestamp</CODE> 类的持久性代理。
 * 它支持纳秒。
 *
 * @author Sergey A. Malenkov
 */
static final class java_sql_Timestamp_PersistenceDelegate extends java_util_Date_PersistenceDelegate {
    private static final Method getNanosMethod = getNanosMethod();

    private static Method getNanosMethod() {
        try {
            Class<?> c = Class.forName("java.sql.Timestamp", true, null);
            return c.getMethod("getNanos");
        } catch (ClassNotFoundException e) {
            return null;
        } catch (NoSuchMethodException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * 调用 Timestamp getNanos。
     */
    private static int getNanos(Object obj) {
        if (getNanosMethod == null)
            throw new AssertionError("Should not get here");
        try {
            return (Integer)getNanosMethod.invoke(obj);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException)
                throw (RuntimeException)cause;
            if (cause instanceof Error)
                throw (Error)cause;
            throw new AssertionError(e);
        } catch (IllegalAccessException iae) {
            throw new AssertionError(iae);
        }
    }

    protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
        // 假设 oldInstance 和 newInstance 都是 Timestamps
        int nanos = getNanos(oldInstance);
        if (nanos != getNanos(newInstance)) {
            out.writeStatement(new Statement(oldInstance, "setNanos", new Object[] {nanos}));
        }
    }
}

// 集合

/*
Hashtable 和 AbstractMap 类没有共同的祖先，但可以使用单个持久性代理处理：
一个仅使用 Map 接口方法的代理。然而，将持久性代理附加到接口本身是危险的，
因为对于 Map 接口，AbstractMap 和 HashMap 类都被声明为实现 Map 接口，
这使得明显的实现容易重复初始化。这些问题以及与接口附加的代理顺序有关的问题，
导致我们忽略任何附加到接口的代理，并强制所有持久性代理都注册到具体类。
*/

/**
 * 内部类的持久性代理基类，
 * 可以使用 {@link Collections} 创建。
 *
 * @author Sergey A. Malenkov
 */
private static abstract class java_util_Collections extends PersistenceDelegate {
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        if (!super.mutatesTo(oldInstance, newInstance)) {
            return false;
        }
        if ((oldInstance instanceof List) || (oldInstance instanceof Set) || (oldInstance instanceof Map)) {
            return oldInstance.equals(newInstance);
        }
        Collection<?> oldC = (Collection<?>) oldInstance;
        Collection<?> newC = (Collection<?>) newInstance;
        return (oldC.size() == newC.size()) && oldC.containsAll(newC);
    }

    protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
        // 不以默认方式初始化这些自定义集合
    }

    static final class EmptyList_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            return new Expression(oldInstance, Collections.class, "emptyList", null);
        }
    }

    static final class EmptySet_PersistenceDelegate extends java_util_Collections {
        protected Expression instantiate(Object oldInstance, Encoder out) {
            return new Expression(oldInstance, Collections.class, "emptySet", null);
        }
    }
}


/**
 * The persistence delegate for <CODE>java.util.EnumMap</CODE> classes.
 *
 * @author Sergey A. Malenkov
 */
static final class java_util_EnumMap_PersistenceDelegate extends PersistenceDelegate {
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        return super.mutatesTo(oldInstance, newInstance) && (getType(oldInstance) == getType(newInstance));
    }

    protected Expression instantiate(Object oldInstance, Encoder out) {
        return new Expression(oldInstance, EnumMap.class, "new", new Object[] {getType(oldInstance)});
    }

    private static Object getType(Object instance) {
        return MetaData.getPrivateFieldValue(instance, "java.util.EnumMap.keyType");
    }
}

/**
 * The persistence delegate for <CODE>java.util.EnumSet</CODE> classes.
 *
 * @author Sergey A. Malenkov
 */
static final class java_util_EnumSet_PersistenceDelegate extends PersistenceDelegate {
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        return super.mutatesTo(oldInstance, newInstance) && (getType(oldInstance) == getType(newInstance));
    }

    protected Expression instantiate(Object oldInstance, Encoder out) {
        return new Expression(oldInstance, EnumSet.class, "noneOf", new Object[] {getType(oldInstance)});
    }

    private static Object getType(Object instance) {
        return MetaData.getPrivateFieldValue(instance, "java.util.EnumSet.elementType");
    }
}

// Collection
static class java_util_Collection_PersistenceDelegate extends DefaultPersistenceDelegate {
    protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
        java.util.Collection<?> oldO = (java.util.Collection)oldInstance;
        java.util.Collection<?> newO = (java.util.Collection)newInstance;

        if (newO.size() != 0) {
            invokeStatement(oldInstance, "clear", new Object[]{}, out);
        }
        for (Iterator<?> i = oldO.iterator(); i.hasNext();) {
            invokeStatement(oldInstance, "add", new Object[]{i.next()}, out);
        }
    }
}

// List
static class java_util_List_PersistenceDelegate extends DefaultPersistenceDelegate {
    protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
        java.util.List<?> oldO = (java.util.List<?>)oldInstance;
        java.util.List<?> newO = (java.util.List<?>)newInstance;
        int oldSize = oldO.size();
        int newSize = (newO == null) ? 0 : newO.size();
        if (oldSize < newSize) {
            invokeStatement(oldInstance, "clear", new Object[]{}, out);
            newSize = 0;
        }
        for (int i = 0; i < newSize; i++) {
            Object index = new Integer(i);

            Expression oldGetExp = new Expression(oldInstance, "get", new Object[]{index});
            Expression newGetExp = new Expression(newInstance, "get", new Object[]{index});
            try {
                Object oldValue = oldGetExp.getValue();
                Object newValue = newGetExp.getValue();
                out.writeExpression(oldGetExp);
                if (!Objects.equals(newValue, out.get(oldValue))) {
                    invokeStatement(oldInstance, "set", new Object[]{index, oldValue}, out);
                }
            }
            catch (Exception e) {
                out.getExceptionListener().exceptionThrown(e);
            }
        }
        for (int i = newSize; i < oldSize; i++) {
            invokeStatement(oldInstance, "add", new Object[]{oldO.get(i)}, out);
        }
    }
}


// Map
static class java_util_Map_PersistenceDelegate extends DefaultPersistenceDelegate {
    protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
        // System.out.println("Initializing: " + newInstance);
        java.util.Map<?,?> oldMap = (java.util.Map)oldInstance;
        java.util.Map<?,?> newMap = (java.util.Map)newInstance;
        // 移除新元素。
        // 先做这一步，否则会撤销添加的工作。
        if (newMap != null) {
            for (Object newKey : newMap.keySet().toArray()) {
               // PENDING: 这个 "key" 不在正确的环境中。
                if (!oldMap.containsKey(newKey)) {
                    invokeStatement(oldInstance, "remove", new Object[]{newKey}, out);
                }
            }
        }
        // 添加新元素。
        for ( Object oldKey : oldMap.keySet() ) {
            Expression oldGetExp = new Expression(oldInstance, "get", new Object[]{oldKey});
            // 待处理: 应该使用 newKey。
            Expression newGetExp = new Expression(newInstance, "get", new Object[]{oldKey});
            try {
                Object oldValue = oldGetExp.getValue();
                Object newValue = newGetExp.getValue();
                out.writeExpression(oldGetExp);
                if (!Objects.equals(newValue, out.get(oldValue))) {
                    invokeStatement(oldInstance, "put", new Object[]{oldKey, oldValue}, out);
                } else if ((newValue == null) && !newMap.containsKey(oldKey)) {
                    // 如果 oldKey 在 newMap 中不存在，则放入 oldValue(=null?)。
                    invokeStatement(oldInstance, "put", new Object[]{oldKey, oldValue}, out);
                }
            }
            catch (Exception e) {
                out.getExceptionListener().exceptionThrown(e);
            }
        }
    }
}


static final class java_util_AbstractCollection_PersistenceDelegate extends java_util_Collection_PersistenceDelegate {}
static final class java_util_AbstractList_PersistenceDelegate extends java_util_List_PersistenceDelegate {}
static final class java_util_AbstractMap_PersistenceDelegate extends java_util_Map_PersistenceDelegate {}
static final class java_util_Hashtable_PersistenceDelegate extends java_util_Map_PersistenceDelegate {}


// Beans
static final class java_beans_beancontext_BeanContextSupport_PersistenceDelegate extends java_util_Collection_PersistenceDelegate {}

// AWT

/**
 * {@link Insets} 的持久性代理。
 * 无法使用 {@link DefaultPersistenceDelegate}，
 * 因为这个类没有任何属性。
 *
 * @author Sergey A. Malenkov
 */
static final class java_awt_Insets_PersistenceDelegate extends PersistenceDelegate {
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        return oldInstance.equals(newInstance);
    }

    protected Expression instantiate(Object oldInstance, Encoder out) {
        Insets insets = (Insets) oldInstance;
        Object[] args = new Object[] {
                insets.top,
                insets.left,
                insets.bottom,
                insets.right,
        };
        return new Expression(insets, insets.getClass(), "new", args);
    }
}

/**
 * {@link Font} 的持久性代理。
 * 无法使用 {@link DefaultPersistenceDelegate}，
 * 因为字体的大小可以是浮点值。
 *
 * @author Sergey A. Malenkov
 */
static final class java_awt_Font_PersistenceDelegate extends PersistenceDelegate {
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        return oldInstance.equals(newInstance);
    }

    protected Expression instantiate(Object oldInstance, Encoder out) {
        Font font = (Font) oldInstance;

        int count = 0;
        String family = null;
        int style = Font.PLAIN;
        int size = 12;

        Map<TextAttribute, ?> basic = font.getAttributes();
        Map<TextAttribute, Object> clone = new HashMap<>(basic.size());
        for (TextAttribute key : basic.keySet()) {
            Object value = basic.get(key);
            if (value != null) {
                clone.put(key, value);
            }
            if (key == TextAttribute.FAMILY) {
                if (value instanceof String) {
                    count++;
                    family = (String) value;
                }
            }
            else if (key == TextAttribute.WEIGHT) {
                if (TextAttribute.WEIGHT_REGULAR.equals(value)) {
                    count++;
                } else if (TextAttribute.WEIGHT_BOLD.equals(value)) {
                    count++;
                    style |= Font.BOLD;
                }
            }
            else if (key == TextAttribute.POSTURE) {
                if (TextAttribute.POSTURE_REGULAR.equals(value)) {
                    count++;
                } else if (TextAttribute.POSTURE_OBLIQUE.equals(value)) {
                    count++;
                    style |= Font.ITALIC;
                }
            } else if (key == TextAttribute.SIZE) {
                if (value instanceof Number) {
                    Number number = (Number) value;
                    size = number.intValue();
                    if (size == number.floatValue()) {
                        count++;
                    }
                }
            }
        }
        Class<?> type = font.getClass();
        if (count == clone.size()) {
            return new Expression(font, type, "new", new Object[]{family, style, size});
        }
        if (type == Font.class) {
            return new Expression(font, type, "getFont", new Object[]{clone});
        }
        return new Expression(font, type, "new", new Object[]{Font.getFont(clone)});
    }
}

/**
 * {@link AWTKeyStroke} 的持久性代理。
 * 无法使用 {@link DefaultPersistenceDelegate}，
 * 因为这个类没有公共构造函数。
 *
 * @author Sergey A. Malenkov
 */
static final class java_awt_AWTKeyStroke_PersistenceDelegate extends PersistenceDelegate {
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        return oldInstance.equals(newInstance);
    }

    protected Expression instantiate(Object oldInstance, Encoder out) {
        AWTKeyStroke key = (AWTKeyStroke) oldInstance;

        char ch = key.getKeyChar();
        int code = key.getKeyCode();
        int mask = key.getModifiers();
        boolean onKeyRelease = key.isOnKeyRelease();

        Object[] args = null;
        if (ch == KeyEvent.CHAR_UNDEFINED) {
            args = !onKeyRelease
                    ? new Object[]{code, mask}
                    : new Object[]{code, mask, onKeyRelease};
        } else if (code == KeyEvent.VK_UNDEFINED) {
            if (!onKeyRelease) {
                args = (mask == 0)
                        ? new Object[]{ch}
                        : new Object[]{ch, mask};
            } else if (mask == 0) {
                args = new Object[]{ch, onKeyRelease};
            }
        }
        if (args == null) {
            throw new IllegalStateException("Unsupported KeyStroke: " + key);
        }
        Class<?> type = key.getClass();
        String name = type.getName();
        // 获取类的短名称
        int index = name.lastIndexOf('.') + 1;
        if (index > 0) {
            name = name.substring(index);
        }
        return new Expression( key, type, "get" + name, args );
    }
}

static class StaticFieldsPersistenceDelegate extends PersistenceDelegate {
    protected void installFields(Encoder out, Class<?> cls) {
        if (Modifier.isPublic(cls.getModifiers()) && isPackageAccessible(cls)) {
            Field fields[] = cls.getFields();
            for(int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                // 不安装原始类型，因为它们的身份不会通过包装来保留。
                if (Object.class.isAssignableFrom(field.getType())) {
                    out.writeExpression(new Expression(field, "get", new Object[]{null}));
                }
            }
        }
    }

    protected Expression instantiate(Object oldInstance, Encoder out) {
        throw new RuntimeException("未识别的实例: " + oldInstance);
    }

    public void writeObject(Object oldInstance, Encoder out) {
        if (out.getAttribute(this) == null) {
            out.setAttribute(this, Boolean.TRUE);
            installFields(out, oldInstance.getClass());
        }
        super.writeObject(oldInstance, out);
    }
}

// SystemColor
static final class java_awt_SystemColor_PersistenceDelegate extends StaticFieldsPersistenceDelegate {}

// TextAttribute
static final class java_awt_font_TextAttribute_PersistenceDelegate extends StaticFieldsPersistenceDelegate {}

// MenuShortcut
static final class java_awt_MenuShortcut_PersistenceDelegate extends PersistenceDelegate {
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        return oldInstance.equals(newInstance);
    }

    protected Expression instantiate(Object oldInstance, Encoder out) {
        java.awt.MenuShortcut m = (java.awt.MenuShortcut)oldInstance;
        return new Expression(oldInstance, m.getClass(), "new",
                   new Object[]{new Integer(m.getKey()), Boolean.valueOf(m.usesShiftModifier())});
    }
}

// Component
static final class java_awt_Component_PersistenceDelegate extends DefaultPersistenceDelegate {
    protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
        super.initialize(type, oldInstance, newInstance, out);
        java.awt.Component c = (java.awt.Component)oldInstance;
        java.awt.Component c2 = (java.awt.Component)newInstance;
        // "background", "foreground" 和 "font" 属性。
        // Windows 的前景色和字体属性在窗口可见后会从 null 变为定义值 -
        // 暂时特殊处理它们。
        if (!(oldInstance instanceof java.awt.Window)) {
            Object oldBackground = c.isBackgroundSet() ? c.getBackground() : null;
            Object newBackground = c2.isBackgroundSet() ? c2.getBackground() : null;
            if (!Objects.equals(oldBackground, newBackground)) {
                invokeStatement(oldInstance, "setBackground", new Object[] { oldBackground }, out);
            }
            Object oldForeground = c.isForegroundSet() ? c.getForeground() : null;
            Object newForeground = c2.isForegroundSet() ? c2.getForeground() : null;
            if (!Objects.equals(oldForeground, newForeground)) {
                invokeStatement(oldInstance, "setForeground", new Object[] { oldForeground }, out);
            }
            Object oldFont = c.isFontSet() ? c.getFont() : null;
            Object newFont = c2.isFontSet() ? c2.getFont() : null;
            if (!Objects.equals(oldFont, newFont)) {
                invokeStatement(oldInstance, "setFont", new Object[] { oldFont }, out);
            }
        }

        // 边界
        java.awt.Container p = c.getParent();
        if (p == null || p.getLayout() == null) {
            // 使用最简洁的构造。
            boolean locationCorrect = c.getLocation().equals(c2.getLocation());
            boolean sizeCorrect = c.getSize().equals(c2.getSize());
            if (!locationCorrect && !sizeCorrect) {
                invokeStatement(oldInstance, "setBounds", new Object[]{c.getBounds()}, out);
            }
            else if (!locationCorrect) {
                invokeStatement(oldInstance, "setLocation", new Object[]{c.getLocation()}, out);
            }
            else if (!sizeCorrect) {
                invokeStatement(oldInstance, "setSize", new Object[]{c.getSize()}, out);
            }
        }
    }
}

// Container
static final class java_awt_Container_PersistenceDelegate extends DefaultPersistenceDelegate {
    protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
        super.initialize(type, oldInstance, newInstance, out);
        // 忽略 JScrollPane 的子组件。
        // 待处理(milne) 找到更好的方法。
        if (oldInstance instanceof javax.swing.JScrollPane) {
            return;
        }
        java.awt.Container oldC = (java.awt.Container)oldInstance;
        java.awt.Component[] oldChildren = oldC.getComponents();
        java.awt.Container newC = (java.awt.Container)newInstance;
        java.awt.Component[] newChildren = (newC == null) ? new java.awt.Component[0] : newC.getComponents();

        BorderLayout layout = ( oldC.getLayout() instanceof BorderLayout )
                ? ( BorderLayout )oldC.getLayout()
                : null;

        JLayeredPane oldLayeredPane = (oldInstance instanceof JLayeredPane)
                ? (JLayeredPane) oldInstance
                : null;

        // 待处理。假设所有新的子组件都未更改。
        for(int i = newChildren.length; i < oldChildren.length; i++) {
            Object[] args = ( layout != null )
                    ? new Object[] {oldChildren[i], layout.getConstraints( oldChildren[i] )}
                    : (oldLayeredPane != null)
                            ? new Object[] {oldChildren[i], oldLayeredPane.getLayer(oldChildren[i]), Integer.valueOf(-1)}
                            : new Object[] {oldChildren[i]};

            invokeStatement(oldInstance, "add", args, out);
        }
    }
}

// Choice
static final class java_awt_Choice_PersistenceDelegate extends DefaultPersistenceDelegate {
    protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
        super.initialize(type, oldInstance, newInstance, out);
        java.awt.Choice m = (java.awt.Choice)oldInstance;
        java.awt.Choice n = (java.awt.Choice)newInstance;
        for (int i = n.getItemCount(); i < m.getItemCount(); i++) {
            invokeStatement(oldInstance, "add", new Object[]{m.getItem(i)}, out);
        }
    }
}

// Menu
static final class java_awt_Menu_PersistenceDelegate extends DefaultPersistenceDelegate {
    protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
        super.initialize(type, oldInstance, newInstance, out);
        java.awt.Menu m = (java.awt.Menu)oldInstance;
        java.awt.Menu n = (java.awt.Menu)newInstance;
        for (int i = n.getItemCount(); i < m.getItemCount(); i++) {
            invokeStatement(oldInstance, "add", new Object[]{m.getItem(i)}, out);
        }
    }
}

// MenuBar
static final class java_awt_MenuBar_PersistenceDelegate extends DefaultPersistenceDelegate {
    protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
        super.initialize(type, oldInstance, newInstance, out);
        java.awt.MenuBar m = (java.awt.MenuBar)oldInstance;
        java.awt.MenuBar n = (java.awt.MenuBar)newInstance;
        for (int i = n.getMenuCount(); i < m.getMenuCount(); i++) {
            invokeStatement(oldInstance, "add", new Object[]{m.getMenu(i)}, out);
        }
    }
}

// List
static final class java_awt_List_PersistenceDelegate extends DefaultPersistenceDelegate {
    protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
        super.initialize(type, oldInstance, newInstance, out);
        java.awt.List m = (java.awt.List)oldInstance;
        java.awt.List n = (java.awt.List)newInstance;
        for (int i = n.getItemCount(); i < m.getItemCount(); i++) {
            invokeStatement(oldInstance, "add", new Object[]{m.getItem(i)}, out);
        }
    }
}


// LayoutManagers

// BorderLayout
static final class java_awt_BorderLayout_PersistenceDelegate extends DefaultPersistenceDelegate {
    private static final String[] CONSTRAINTS = {
            BorderLayout.NORTH,
            BorderLayout.SOUTH,
            BorderLayout.EAST,
            BorderLayout.WEST,
            BorderLayout.CENTER,
            BorderLayout.PAGE_START,
            BorderLayout.PAGE_END,
            BorderLayout.LINE_START,
            BorderLayout.LINE_END,
    };
    @Override
    protected void initialize(Class<?> type, Object oldInstance,
                              Object newInstance, Encoder out) {
        super.initialize(type, oldInstance, newInstance, out);
        BorderLayout oldLayout = (BorderLayout) oldInstance;
        BorderLayout newLayout = (BorderLayout) newInstance;
        for (String constraints : CONSTRAINTS) {
            Object oldC = oldLayout.getLayoutComponent(constraints);
            Object newC = newLayout.getLayoutComponent(constraints);
            // 待处理，假设任何现有元素都是正确的。
            if (oldC != null && newC == null) {
                invokeStatement(oldInstance, "addLayoutComponent",
                                new Object[] { oldC, constraints }, out);
            }
        }
    }
}

// CardLayout
static final class java_awt_CardLayout_PersistenceDelegate extends DefaultPersistenceDelegate {
    protected void initialize(Class<?> type, Object oldInstance,
                              Object newInstance, Encoder out) {
        super.initialize(type, oldInstance, newInstance, out);
        if (getVector(newInstance).isEmpty()) {
            for (Object card : getVector(oldInstance)) {
                Object[] args = {MetaData.getPrivateFieldValue(card, "java.awt.CardLayout$Card.name"),
                                 MetaData.getPrivateFieldValue(card, "java.awt.CardLayout$Card.comp")};
                invokeStatement(oldInstance, "addLayoutComponent", args, out);
            }
        }
    }
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        return super.mutatesTo(oldInstance, newInstance) && getVector(newInstance).isEmpty();
    }
    private static Vector<?> getVector(Object instance) {
        return (Vector<?>) MetaData.getPrivateFieldValue(instance, "java.awt.CardLayout.vector");
    }
}


// GridBagLayout
static final class java_awt_GridBagLayout_PersistenceDelegate extends DefaultPersistenceDelegate {
    protected void initialize(Class<?> type, Object oldInstance,
                              Object newInstance, Encoder out) {
        super.initialize(type, oldInstance, newInstance, out);
        if (getHashtable(newInstance).isEmpty()) {
            for (Map.Entry<?,?> entry : getHashtable(oldInstance).entrySet()) {
                Object[] args = {entry.getKey(), entry.getValue()};
                invokeStatement(oldInstance, "addLayoutComponent", args, out);
            }
        }
    }
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        return super.mutatesTo(oldInstance, newInstance) && getHashtable(newInstance).isEmpty();
    }
    private static Hashtable<?,?> getHashtable(Object instance) {
        return (Hashtable<?,?>) MetaData.getPrivateFieldValue(instance, "java.awt.GridBagLayout.comptable");
    }
}

// Swing

// JFrame (如果我们对 Window 而不是 JFrame 进行此操作，setVisible 调用将在我们向 JFrame 添加所有子组件之前发出，并且会显示为空)。
static final class javax_swing_JFrame_PersistenceDelegate extends DefaultPersistenceDelegate {
    protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
        super.initialize(type, oldInstance, newInstance, out);
        java.awt.Window oldC = (java.awt.Window)oldInstance;
        java.awt.Window newC = (java.awt.Window)newInstance;
        boolean oldV = oldC.isVisible();
        boolean newV = newC.isVisible();
        if (newV != oldV) {
            // false 表示：不要在写入时执行此语句。
            boolean executeStatements = out.executeStatements;
            out.executeStatements = false;
            invokeStatement(oldInstance, "setVisible", new Object[]{Boolean.valueOf(oldV)}, out);
            out.executeStatements = executeStatements;
        }
    }
}

// 模型

// DefaultListModel
static final class javax_swing_DefaultListModel_PersistenceDelegate extends DefaultPersistenceDelegate {
    protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
        // 注意，"size" 属性将在此处设置。
        super.initialize(type, oldInstance, newInstance, out);
        javax.swing.DefaultListModel<?> m = (javax.swing.DefaultListModel<?>)oldInstance;
        javax.swing.DefaultListModel<?> n = (javax.swing.DefaultListModel<?>)newInstance;
        for (int i = n.getSize(); i < m.getSize(); i++) {
            invokeStatement(oldInstance, "add", // 也可以使用 "addElement"。
                    new Object[]{m.getElementAt(i)}, out);
        }
    }
}

// DefaultComboBoxModel
static final class javax_swing_DefaultComboBoxModel_PersistenceDelegate extends DefaultPersistenceDelegate {
    protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
        super.initialize(type, oldInstance, newInstance, out);
        javax.swing.DefaultComboBoxModel<?> m = (javax.swing.DefaultComboBoxModel<?>)oldInstance;
        for (int i = 0; i < m.getSize(); i++) {
            invokeStatement(oldInstance, "addElement", new Object[]{m.getElementAt(i)}, out);
        }
    }
}


// DefaultMutableTreeNode
static final class javax_swing_tree_DefaultMutableTreeNode_PersistenceDelegate extends DefaultPersistenceDelegate {
    protected void initialize(Class<?> type, Object oldInstance, Object
                              newInstance, Encoder out) {
        super.initialize(type, oldInstance, newInstance, out);
        javax.swing.tree.DefaultMutableTreeNode m =
            (javax.swing.tree.DefaultMutableTreeNode)oldInstance;
        javax.swing.tree.DefaultMutableTreeNode n =
            (javax.swing.tree.DefaultMutableTreeNode)newInstance;
        for (int i = n.getChildCount(); i < m.getChildCount(); i++) {
            invokeStatement(oldInstance, "add", new
                Object[]{m.getChildAt(i)}, out);
        }
    }
}

// ToolTipManager
static final class javax_swing_ToolTipManager_PersistenceDelegate extends PersistenceDelegate {
    protected Expression instantiate(Object oldInstance, Encoder out) {
        return new Expression(oldInstance, javax.swing.ToolTipManager.class,
                              "sharedInstance", new Object[]{});
    }
}

// JTabbedPane
static final class javax_swing_JTabbedPane_PersistenceDelegate extends DefaultPersistenceDelegate {
    protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
        super.initialize(type, oldInstance, newInstance, out);
        javax.swing.JTabbedPane p = (javax.swing.JTabbedPane)oldInstance;
        for (int i = 0; i < p.getTabCount(); i++) {
            invokeStatement(oldInstance, "addTab",
                                          new Object[]{
                                              p.getTitleAt(i),
                                              p.getIconAt(i),
                                              p.getComponentAt(i)}, out);
        }
    }
}

// Box
static final class javax_swing_Box_PersistenceDelegate extends DefaultPersistenceDelegate {
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        return super.mutatesTo(oldInstance, newInstance) && getAxis(oldInstance).equals(getAxis(newInstance));
    }

    protected Expression instantiate(Object oldInstance, Encoder out) {
        return new Expression(oldInstance, oldInstance.getClass(), "new", new Object[] {getAxis(oldInstance)});
    }

    private Integer getAxis(Object object) {
        Box box = (Box) object;
        return (Integer) MetaData.getPrivateFieldValue(box.getLayout(), "javax.swing.BoxLayout.axis");
    }
}

// JMenu
// 注意，我们不需要为 JMenuItems 声明初始化器，因为 Container 中定义的 getComponents() 方法将返回所有需要添加到菜单项的子菜单项。
// 对于 JMenu 来说，情况并非如此。
static final class javax_swing_JMenu_PersistenceDelegate extends DefaultPersistenceDelegate {
    protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
        super.initialize(type, oldInstance, newInstance, out);
        javax.swing.JMenu m = (javax.swing.JMenu)oldInstance;
        java.awt.Component[] c = m.getMenuComponents();
        for (int i = 0; i < c.length; i++) {
            invokeStatement(oldInstance, "add", new Object[]{c[i]}, out);
        }
    }
}

/**
 * {@link MatteBorder} 的持久化代理。
 * 由于此类没有可写属性，因此无法使用 {@link DefaultPersistenceDelegate}。
 *
 * @author Sergey A. Malenkov
 */
static final class javax_swing_border_MatteBorder_PersistenceDelegate extends PersistenceDelegate {
    protected Expression instantiate(Object oldInstance, Encoder out) {
        MatteBorder border = (MatteBorder) oldInstance;
        Insets insets = border.getBorderInsets();
        Object object = border.getTileIcon();
        if (object == null) {
            object = border.getMatteColor();
        }
        Object[] args = new Object[] {
                insets.top,
                insets.left,
                insets.bottom,
                insets.right,
                object,
        };
        return new Expression(border, border.getClass(), "new", args);
    }
}

/* XXX - 似乎不起作用。稍后调试。
static final class javax_swing_JMenu_PersistenceDelegate extends DefaultPersistenceDelegate {
    protected void initialize(Class<?> type, Object oldInstance, Object newInstance, Encoder out) {
        super.initialize(type, oldInstance, newInstance, out);
        javax.swing.JMenu m = (javax.swing.JMenu)oldInstance;
        javax.swing.JMenu n = (javax.swing.JMenu)newInstance;
        for (int i = n.getItemCount(); i < m.getItemCount(); i++) {
            invokeStatement(oldInstance, "add", new Object[]{m.getItem(i)}, out);
        }
    }
}
*/

/**
 * {@link PrintColorUIResource} 的持久化代理。
 * 由于此类具有特殊的序列化规则：应转换为 {@link ColorUIResource}，因此无法使用 {@link DefaultPersistenceDelegate}。
 *
 * @see PrintColorUIResource#writeReplace
 *
 * @author Sergey A. Malenkov
 */
static final class sun_swing_PrintColorUIResource_PersistenceDelegate extends PersistenceDelegate {
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        return oldInstance.equals(newInstance);
    }

    protected Expression instantiate(Object oldInstance, Encoder out) {
        Color color = (Color) oldInstance;
        Object[] args = new Object[] {color.getRGB()};
        return new Expression(color, ColorUIResource.class, "new", args);
    }
}

    private static final Map<String,Field> fields = Collections.synchronizedMap(new WeakHashMap<String, Field>());
    private static Hashtable<String, PersistenceDelegate> internalPersistenceDelegates = new Hashtable<>();

    private static PersistenceDelegate nullPersistenceDelegate = new NullPersistenceDelegate();
    private static PersistenceDelegate enumPersistenceDelegate = new EnumPersistenceDelegate();
    private static PersistenceDelegate primitivePersistenceDelegate = new PrimitivePersistenceDelegate();
    private static PersistenceDelegate defaultPersistenceDelegate = new DefaultPersistenceDelegate();
    private static PersistenceDelegate arrayPersistenceDelegate;
    private static PersistenceDelegate proxyPersistenceDelegate;

    static {

        internalPersistenceDelegates.put("java.net.URI",
                                         new PrimitivePersistenceDelegate());

        // 由于 MatteBorder 可以从 MatteBorderUIResource 转换而来
        internalPersistenceDelegates.put("javax.swing.plaf.BorderUIResource$MatteBorderUIResource",
                                         new javax_swing_border_MatteBorder_PersistenceDelegate());

        // 由于 FontUIResource 受 java_awt_Font_PersistenceDelegate 支持
        internalPersistenceDelegates.put("javax.swing.plaf.FontUIResource",
                                         new java_awt_Font_PersistenceDelegate());

        // 由于 KeyStroke 受 java_awt_AWTKeyStroke_PersistenceDelegate 支持
        internalPersistenceDelegates.put("javax.swing.KeyStroke",
                                         new java_awt_AWTKeyStroke_PersistenceDelegate());

        internalPersistenceDelegates.put("java.sql.Date", new java_util_Date_PersistenceDelegate());
        internalPersistenceDelegates.put("java.sql.Time", new java_util_Date_PersistenceDelegate());

        internalPersistenceDelegates.put("java.util.JumboEnumSet", new java_util_EnumSet_PersistenceDelegate());
        internalPersistenceDelegates.put("java.util.RegularEnumSet", new java_util_EnumSet_PersistenceDelegate());
    }

    @SuppressWarnings("rawtypes")
    public synchronized static PersistenceDelegate getPersistenceDelegate(Class type) {
        if (type == null) {
            return nullPersistenceDelegate;
        }
        if (Enum.class.isAssignableFrom(type)) {
            return enumPersistenceDelegate;
        }
        if (null != XMLEncoder.primitiveTypeFor(type)) {
            return primitivePersistenceDelegate;
        }
        // 数组的持久化代理较为复杂；延迟实例化。
        if (type.isArray()) {
            if (arrayPersistenceDelegate == null) {
                arrayPersistenceDelegate = new ArrayPersistenceDelegate();
            }
            return arrayPersistenceDelegate;
        }
        // 为向后兼容 1.2 版本，延迟处理代理。
        try {
            if (java.lang.reflect.Proxy.isProxyClass(type)) {
                if (proxyPersistenceDelegate == null) {
                    proxyPersistenceDelegate = new ProxyPersistenceDelegate();
                }
                return proxyPersistenceDelegate;
            }
        }
        catch(Exception e) {}
        // else if (type.getDeclaringClass() != null) {
        //     return new DefaultPersistenceDelegate(new String[]{"this$0"});
        // }

        String typeName = type.getName();
        PersistenceDelegate pd = (PersistenceDelegate)getBeanAttribute(type, "persistenceDelegate");
        if (pd == null) {
            pd = internalPersistenceDelegates.get(typeName);
            if (pd != null) {
                return pd;
            }
            internalPersistenceDelegates.put(typeName, defaultPersistenceDelegate);
            try {
                String name =  type.getName();
                Class c = Class.forName("java.beans.MetaData$" + name.replace('.', '_')
                                        + "_PersistenceDelegate");
                pd = (PersistenceDelegate)c.newInstance();
                internalPersistenceDelegates.put(typeName, pd);
            }
            catch (ClassNotFoundException e) {
                String[] properties = getConstructorProperties(type);
                if (properties != null) {
                    pd = new DefaultPersistenceDelegate(properties);
                    internalPersistenceDelegates.put(typeName, pd);
                }
            }
            catch (Exception e) {
                System.err.println("内部错误: " + e);
            }
        }

        return (pd != null) ? pd : defaultPersistenceDelegate;
    }

    private static String[] getConstructorProperties(Class<?> type) {
        String[] names = null;
        int length = 0;
        for (Constructor<?> constructor : type.getConstructors()) {
            String[] value = getAnnotationValue(constructor);
            if ((value != null) && (length < value.length) && isValid(constructor, value)) {
                names = value;
                length = value.length;
            }
        }
        return names;
    }

    private static String[] getAnnotationValue(Constructor<?> constructor) {
        ConstructorProperties annotation = constructor.getAnnotation(ConstructorProperties.class);
        return (annotation != null)
                ? annotation.value()
                : null;
    }

    private static boolean isValid(Constructor<?> constructor, String[] names) {
        Class[] parameters = constructor.getParameterTypes();
        if (names.length != parameters.length) {
            return false;
        }
        for (String name : names) {
            if (name == null) {
                return false;
            }
        }
        return true;
    }

    private static Object getBeanAttribute(Class<?> type, String attribute) {
        try {
            return Introspector.getBeanInfo(type).getBeanDescriptor().getValue(attribute);
        } catch (IntrospectionException exception) {
            return null;
        }
    }

    static Object getPrivateFieldValue(Object instance, String name) {
        Field field = fields.get(name);
        if (field == null) {
            int index = name.lastIndexOf('.');
            final String className = name.substring(0, index);
            final String fieldName = name.substring(1 + index);
            field = AccessController.doPrivileged(new PrivilegedAction<Field>() {
                public Field run() {
                    try {
                        Field field = Class.forName(className).getDeclaredField(fieldName);
                        field.setAccessible(true);
                        return field;
                    }
                    catch (ClassNotFoundException exception) {
                        throw new IllegalStateException("无法找到类", exception);
                    }
                    catch (NoSuchFieldException exception) {
                        throw new IllegalStateException("无法找到字段", exception);
                    }
                }
            });
            fields.put(name, field);
        }
        try {
            return field.get(instance);
        }
        catch (IllegalAccessException exception) {
            throw new IllegalStateException("无法获取字段的值", exception);
        }
    }
}
