
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
package java.awt;

import java.awt.event.KeyEvent;
import sun.awt.AppContext;
import java.awt.event.InputEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.io.Serializable;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Field;

/**
 * 一个 <code>AWTKeyStroke</code> 表示键盘或等效输入设备上的一个按键操作。<code>AWTKeyStroke</code> 可以对应于特定键的按下或释放，
 * 就像 <code>KEY_PRESSED</code> 和 <code>KEY_RELEASED</code> <code>KeyEvent</code> 一样；
 * 或者，它们可以对应于输入特定的 Java 字符，就像 <code>KEY_TYPED</code> <code>KeyEvent</code> 一样。
 * 在所有情况下，<code>AWTKeyStroke</code> 可以指定修饰符（alt、shift、control、meta、altGraph 或其组合），这些修饰符在操作期间必须存在才能精确匹配。
 * <p>
 * <code>AWTKeyStrokes</code> 是不可变的，并且旨在是唯一的。客户端代码不应自行创建 <code>AWTKeyStroke</code>，
 * 而应使用 <code>getAWTKeyStroke</code> 的某个变体。客户端使用这些工厂方法允许 <code>AWTKeyStroke</code> 实现有效地缓存和共享实例。
 *
 * @see #getAWTKeyStroke
 *
 * @author Arnaud Weber
 * @author David Mendenhall
 * @since 1.4
 */
public class AWTKeyStroke implements Serializable {
    static final long serialVersionUID = -6430539691155161871L;

    private static Map<String, Integer> modifierKeywords;
    /**
     * 将 VK_XXX（作为字符串）与代码（作为 Integer）关联。这是为了避免反射调用查找常量的开销。
     */
    private static VKCollection vks;

    // 在 AppContext 中的 AWTKeyStrokes 集合的键。
    private static Object APP_CONTEXT_CACHE_KEY = new Object();
    // 缓存中的键
    private static AWTKeyStroke APP_CONTEXT_KEYSTROKE_KEY = new AWTKeyStroke();

    /*
     * 从 AppContext 读取 keystroke 类，如果为 null，则将 AWTKeyStroke 类放入其中。
     * 必须在锁定 AWTKeyStro 下调用
     */
    private static Class<AWTKeyStroke> getAWTKeyStrokeClass() {
        Class<AWTKeyStroke> clazz = (Class)AppContext.getAppContext().get(AWTKeyStroke.class);
        if (clazz == null) {
            clazz = AWTKeyStroke.class;
            AppContext.getAppContext().put(AWTKeyStroke.class, AWTKeyStroke.class);
        }
        return clazz;
    }

    private char keyChar = KeyEvent.CHAR_UNDEFINED;
    private int keyCode = KeyEvent.VK_UNDEFINED;
    private int modifiers;
    private boolean onKeyRelease;

    static {
        /* 确保加载必要的本机库 */
        Toolkit.loadLibraries();
    }

    /**
     * 使用默认值构造一个 <code>AWTKeyStroke</code>。使用的默认值如下：
     * <table border="1" summary="AWTKeyStroke 默认值">
     * <tr><th>属性</th><th>默认值</th></tr>
     * <tr>
     *    <td>键字符</td>
     *    <td><code>KeyEvent.CHAR_UNDEFINED</code></td>
     * </tr>
     * <tr>
     *    <td>键码</td>
     *    <td><code>KeyEvent.VK_UNDEFINED</code></td>
     * </tr>
     * <tr>
     *    <td>修饰符</td>
     *    <td>无</td>
     * </tr>
     * <tr>
     *    <td>是否在键释放时？</td>
     *    <td><code>false</code></td>
     * </tr>
     * </table>
     *
     * 客户端代码不应构造 <code>AWTKeyStroke</code>。相反，应使用 <code>getAWTKeyStroke</code> 的某个变体。
     *
     * @see #getAWTKeyStroke
     */
    protected AWTKeyStroke() {
    }

    /**
     * 使用指定的值构造一个 <code>AWTKeyStroke</code>。客户端代码不应构造 <code>AWTKeyStroke</code>。相反，应使用 <code>getAWTKeyStroke</code> 的某个变体。
     *
     * @param keyChar 键盘键的字符值
     * @param keyCode 此 <code>AWTKeyStroke</code> 的键码
     * @param modifiers 任何修饰符的按位或组合
     * @param onKeyRelease 如果此 <code>AWTKeyStroke</code> 对应于键释放，则为 <code>true</code>；否则为 <code>false</code>
     * @see #getAWTKeyStroke
     */
    protected AWTKeyStroke(char keyChar, int keyCode, int modifiers,
                           boolean onKeyRelease) {
        this.keyChar = keyChar;
        this.keyCode = keyCode;
        this.modifiers = modifiers;
        this.onKeyRelease = onKeyRelease;
    }

    /**
     * 注册一个新类，<code>AWTKeyStroke</code> 的工厂方法将使用该类生成新的 <code>AWTKeyStroke</code> 实例。调用此方法后，
     * 工厂方法将返回指定类的实例。指定的类必须是 <code>AWTKeyStroke</code> 或派生自 <code>AWTKeyStroke</code>，
     * 并且必须有一个无参数构造函数。构造函数可以是任何可访问性，包括 <code>private</code>。此操作将清除当前的 <code>AWTKeyStroke</code> 缓存。
     *
     * @param subclass 工厂方法应创建实例的新类
     * @throws IllegalArgumentException 如果 subclass 为 <code>null</code>，或 subclass 没有无参数构造函数
     * @throws ClassCastException 如果 subclass 不是 <code>AWTKeyStroke</code>，或不是派生自 <code>AWTKeyStroke</code> 的类
     */
    protected static void registerSubclass(Class<?> subclass) {
        if (subclass == null) {
            throw new IllegalArgumentException("subclass cannot be null");
        }
        synchronized (AWTKeyStroke.class) {
            Class<AWTKeyStroke> keyStrokeClass = (Class)AppContext.getAppContext().get(AWTKeyStroke.class);
            if (keyStrokeClass != null && keyStrokeClass.equals(subclass)){
                // 已注册
                return;
            }
        }
        if (!AWTKeyStroke.class.isAssignableFrom(subclass)) {
            throw new ClassCastException("subclass is not derived from AWTKeyStroke");
        }

        Constructor ctor = getCtor(subclass);

        String couldNotInstantiate = "subclass could not be instantiated";

        if (ctor == null) {
            throw new IllegalArgumentException(couldNotInstantiate);
        }
        try {
            AWTKeyStroke stroke = (AWTKeyStroke)ctor.newInstance((Object[]) null);
            if (stroke == null) {
                throw new IllegalArgumentException(couldNotInstantiate);
            }
        } catch (NoSuchMethodError e) {
            throw new IllegalArgumentException(couldNotInstantiate);
        } catch (ExceptionInInitializerError e) {
            throw new IllegalArgumentException(couldNotInstantiate);
        } catch (InstantiationException e) {
            throw new IllegalArgumentException(couldNotInstantiate);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(couldNotInstantiate);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException(couldNotInstantiate);
        }

        synchronized (AWTKeyStroke.class) {
            AppContext.getAppContext().put(AWTKeyStroke.class, subclass);
            AppContext.getAppContext().remove(APP_CONTEXT_CACHE_KEY);
            AppContext.getAppContext().remove(APP_CONTEXT_KEYSTROKE_KEY);
        }
    }

    /* 返回具有可访问标志的无参数构造函数。没有安全威胁，因为可访问标志仅设置在此构造函数对象上，而不是在类构造函数上。
     */
    private static Constructor getCtor(final Class clazz)
    {
        Constructor ctor = AccessController.doPrivileged(new PrivilegedAction<Constructor>() {
            public Constructor run() {
                try {
                    Constructor ctor = clazz.getDeclaredConstructor((Class[]) null);
                    if (ctor != null) {
                        ctor.setAccessible(true);
                    }
                    return ctor;
                } catch (SecurityException e) {
                } catch (NoSuchMethodException e) {
                }
                return null;
            }
        });
        return (Constructor)ctor;
    }

    private static synchronized AWTKeyStroke getCachedStroke
        (char keyChar, int keyCode, int modifiers, boolean onKeyRelease)
    {
        Map<AWTKeyStroke, AWTKeyStroke> cache = (Map)AppContext.getAppContext().get(APP_CONTEXT_CACHE_KEY);
        AWTKeyStroke cacheKey = (AWTKeyStroke)AppContext.getAppContext().get(APP_CONTEXT_KEYSTROKE_KEY);

        if (cache == null) {
            cache = new HashMap<>();
            AppContext.getAppContext().put(APP_CONTEXT_CACHE_KEY, cache);
        }

        if (cacheKey == null) {
            try {
                Class<AWTKeyStroke> clazz = getAWTKeyStrokeClass();
                cacheKey = (AWTKeyStroke)getCtor(clazz).newInstance((Object[]) null);
                AppContext.getAppContext().put(APP_CONTEXT_KEYSTROKE_KEY, cacheKey);
            } catch (InstantiationException e) {
                assert(false);
            } catch (IllegalAccessException e) {
                assert(false);
            } catch (InvocationTargetException e) {
                assert(false);
            }
        }
        cacheKey.keyChar = keyChar;
        cacheKey.keyCode = keyCode;
        cacheKey.modifiers = mapNewModifiers(mapOldModifiers(modifiers));
        cacheKey.onKeyRelease = onKeyRelease;

        AWTKeyStroke stroke = (AWTKeyStroke)cache.get(cacheKey);
        if (stroke == null) {
            stroke = cacheKey;
            cache.put(stroke, stroke);
            AppContext.getAppContext().remove(APP_CONTEXT_KEYSTROKE_KEY);
        }
        return stroke;
    }

    /**
     * 返回一个表示指定字符的 <code>KEY_TYPED</code> 事件的 <code>AWTKeyStroke</code> 共享实例。
     *
     * @param keyChar 键盘键的字符值
     * @return 该键的 <code>AWTKeyStroke</code> 对象
     */
    public static AWTKeyStroke getAWTKeyStroke(char keyChar) {
        return getCachedStroke(keyChar, KeyEvent.VK_UNDEFINED, 0, false);
    }

    /**
     * 返回一个表示指定 Character 对象和一组修饰符的 <code>KEY_TYPED</code> 事件的 <code>AWTKeyStroke</code> 共享实例。
     * 注意，第一个参数是 Character 类型而不是 char。这是为了避免与 <code>getAWTKeyStroke(int keyCode, int modifiers)</code> 的调用发生意外冲突。
     *
     * 修饰符可以是以下任何组合：<ul>
     * <li>java.awt.event.InputEvent.SHIFT_DOWN_MASK
     * <li>java.awt.event.InputEvent.CTRL_DOWN_MASK
     * <li>java.awt.event.InputEvent.META_DOWN_MASK
     * <li>java.awt.event.InputEvent.ALT_DOWN_MASK
     * <li>java.awt.event.InputEvent.ALT_GRAPH_DOWN_MASK
     * </ul>
     * 也可以使用以下旧修饰符，但它们会被映射为 _DOWN_ 修饰符：<ul>
     * <li>java.awt.event.InputEvent.SHIFT_MASK
     * <li>java.awt.event.InputEvent.CTRL_MASK
     * <li>java.awt.event.InputEvent.META_MASK
     * <li>java.awt.event.InputEvent.ALT_MASK
     * <li>java.awt.event.InputEvent.ALT_GRAPH_MASK
     * </ul>
     *
     * 由于这些数字都是不同的二的幂，因此它们的任何组合都是一个整数，其中每一位代表一个不同的修饰键。使用 0 表示没有修饰符。
     *
     * @param keyChar 键盘字符的 Character 对象
     * @param modifiers 任何修饰符的按位或组合
     * @return 该键的 <code>AWTKeyStroke</code> 对象
     * @throws IllegalArgumentException 如果 <code>keyChar</code> 为 <code>null</code>
     *
     * @see java.awt.event.InputEvent
     */
    public static AWTKeyStroke getAWTKeyStroke(Character keyChar, int modifiers)
    {
        if (keyChar == null) {
            throw new IllegalArgumentException("keyChar cannot be null");
        }
        return getCachedStroke(keyChar.charValue(), KeyEvent.VK_UNDEFINED,
                               modifiers, false);
    }

    /**
     * 返回一个表示指定数字键码和一组修饰符的 <code>AWTKeyStroke</code> 共享实例，指定该键是在按下时还是释放时激活。
     * <p>
     * 可以使用 <code>java.awt.event.KeyEvent</code> 中定义的“虚拟键”常量来指定键码。例如：<ul>
     * <li><code>java.awt.event.KeyEvent.VK_ENTER</code>
     * <li><code>java.awt.event.KeyEvent.VK_TAB</code>
     * <li><code>java.awt.event.KeyEvent.VK_SPACE</code>
     * </ul>
     * 或者，可以通过调用 <code>java.awt.event.KeyEvent.getExtendedKeyCodeForChar</code> 来获取键码。
     *
     * 修饰符可以是以下任何组合：<ul>
     * <li>java.awt.event.InputEvent.SHIFT_DOWN_MASK
     * <li>java.awt.event.InputEvent.CTRL_DOWN_MASK
     * <li>java.awt.event.InputEvent.META_DOWN_MASK
     * <li>java.awt.event.InputEvent.ALT_DOWN_MASK
     * <li>java.awt.event.InputEvent.ALT_GRAPH_DOWN_MASK
     * </ul>
     * 也可以使用以下旧修饰符，但它们会被映射为 _DOWN_ 修饰符：<ul>
     * <li>java.awt.event.InputEvent.SHIFT_MASK
     * <li>java.awt.event.InputEvent.CTRL_MASK
     * <li>java.awt.event.InputEvent.META_MASK
     * <li>java.awt.event.InputEvent.ALT_MASK
     * <li>java.awt.event.InputEvent.ALT_GRAPH_MASK
     * </ul>
     *
     * 由于这些数字都是不同的二的幂，因此它们的任何组合都是一个整数，其中每一位代表一个不同的修饰键。使用 0 表示没有修饰符。
     *
     * @param keyCode 指定键盘键的数字码
     * @param modifiers 任何修饰符的按位或组合
     * @param onKeyRelease 如果 <code>AWTKeyStroke</code> 应表示键释放，则为 <code>true</code>；否则为 <code>false</code>
     * @return 该键的 AWTKeyStroke 对象
     *
     * @see java.awt.event.KeyEvent
     * @see java.awt.event.InputEvent
     */
    public static AWTKeyStroke getAWTKeyStroke(int keyCode, int modifiers,
                                               boolean onKeyRelease) {
        return getCachedStroke(KeyEvent.CHAR_UNDEFINED, keyCode, modifiers,
                               onKeyRelease);
    }


                /**
     * 返回一个共享的 <code>AWTKeyStroke</code> 实例，给定一个数字键码和一组修饰符。返回的
     * <code>AWTKeyStroke</code> 将对应于一个按键。
     * <p>
     * 可以使用 <code>java.awt.event.KeyEvent</code> 中定义的“虚拟键”常量来指定键码。例如：<ul>
     * <li><code>java.awt.event.KeyEvent.VK_ENTER</code>
     * <li><code>java.awt.event.KeyEvent.VK_TAB</code>
     * <li><code>java.awt.event.KeyEvent.VK_SPACE</code>
     * </ul>
     * 修饰符可以由以下任意组合组成：<ul>
     * <li>java.awt.event.InputEvent.SHIFT_DOWN_MASK
     * <li>java.awt.event.InputEvent.CTRL_DOWN_MASK
     * <li>java.awt.event.InputEvent.META_DOWN_MASK
     * <li>java.awt.event.InputEvent.ALT_DOWN_MASK
     * <li>java.awt.event.InputEvent.ALT_GRAPH_DOWN_MASK
     * </ul>
     * 也可以使用旧的修饰符：<ul>
     * <li>java.awt.event.InputEvent.SHIFT_MASK
     * <li>java.awt.event.InputEvent.CTRL_MASK
     * <li>java.awt.event.InputEvent.META_MASK
     * <li>java.awt.event.InputEvent.ALT_MASK
     * <li>java.awt.event.InputEvent.ALT_GRAPH_MASK
     * </ul>
     * 但它们会被映射到 _DOWN_ 修饰符。
     *
     * 由于这些数字都是2的不同幂次，因此它们的任意组合都是一个整数，其中每一位代表一个不同的修饰键。
     * 使用 0 来指定没有修饰符。
     *
     * @param keyCode 指定键盘键的数字代码
     * @param modifiers 修饰符的按位或组合
     * @return 该键的 <code>AWTKeyStroke</code> 对象
     *
     * @see java.awt.event.KeyEvent
     * @see java.awt.event.InputEvent
     */
    public static AWTKeyStroke getAWTKeyStroke(int keyCode, int modifiers) {
        return getCachedStroke(KeyEvent.CHAR_UNDEFINED, keyCode, modifiers,
                               false);
    }

    /**
     * 返回一个表示给定 <code>KeyEvent</code> 生成的 <code>AWTKeyStroke</code>。
     * <p>
     * 该方法从 <code>KeyTyped</code> 事件中获取 keyChar，从 <code>KeyPressed</code> 或
     * <code>KeyReleased</code> 事件中获取 keyCode。对于所有三种类型的 <code>KeyEvent</code>，
     * 都会获取 <code>KeyEvent</code> 的修饰符。
     *
     * @param anEvent 从中获取 <code>AWTKeyStroke</code> 的 <code>KeyEvent</code>
     * @throws NullPointerException 如果 <code>anEvent</code> 为 null
     * @return 引发事件的 <code>AWTKeyStroke</code>
     */
    public static AWTKeyStroke getAWTKeyStrokeForEvent(KeyEvent anEvent) {
        int id = anEvent.getID();
        switch(id) {
          case KeyEvent.KEY_PRESSED:
          case KeyEvent.KEY_RELEASED:
            return getCachedStroke(KeyEvent.CHAR_UNDEFINED,
                                   anEvent.getKeyCode(),
                                   anEvent.getModifiers(),
                                   (id == KeyEvent.KEY_RELEASED));
          case KeyEvent.KEY_TYPED:
            return getCachedStroke(anEvent.getKeyChar(),
                                   KeyEvent.VK_UNDEFINED,
                                   anEvent.getModifiers(),
                                   false);
          default:
            // 无效的 KeyEvent ID
            return null;
        }
    }

    /**
     * 解析字符串并返回一个 <code>AWTKeyStroke</code>。字符串必须具有以下语法：
     * <pre>
     *    &lt;modifiers&gt;* (&lt;typedID&gt; | &lt;pressedReleasedID&gt;)
     *
     *    modifiers := shift | control | ctrl | meta | alt | altGraph
     *    typedID := typed &lt;typedKey&gt;
     *    typedKey := 长度为1的字符串，给出Unicode字符。
     *    pressedReleasedID := (pressed | released) key
     *    key := KeyEvent 键码名称，即 "VK_" 之后的名称。
     * </pre>
     * 如果未指定 typed、pressed 或 released，则假设为 pressed。以下是一些示例：
     * <pre>
     *     "INSERT" =&gt; getAWTKeyStroke(KeyEvent.VK_INSERT, 0);
     *     "control DELETE" =&gt; getAWTKeyStroke(KeyEvent.VK_DELETE, InputEvent.CTRL_MASK);
     *     "alt shift X" =&gt; getAWTKeyStroke(KeyEvent.VK_X, InputEvent.ALT_MASK | InputEvent.SHIFT_MASK);
     *     "alt shift released X" =&gt; getAWTKeyStroke(KeyEvent.VK_X, InputEvent.ALT_MASK | InputEvent.SHIFT_MASK, true);
     *     "typed a" =&gt; getAWTKeyStroke('a');
     * </pre>
     *
     * @param s 格式如上所述的字符串
     * @return 该字符串的 <code>AWTKeyStroke</code> 对象
     * @throws IllegalArgumentException 如果 <code>s</code> 为 <code>null</code> 或格式不正确
     */
    public static AWTKeyStroke getAWTKeyStroke(String s) {
        if (s == null) {
            throw new IllegalArgumentException("字符串不能为 null");
        }

        final String errmsg = "字符串格式不正确";

        StringTokenizer st = new StringTokenizer(s, " ");

        int mask = 0;
        boolean released = false;
        boolean typed = false;
        boolean pressed = false;

        synchronized (AWTKeyStroke.class) {
            if (modifierKeywords == null) {
                Map<String, Integer> uninitializedMap = new HashMap<>(8, 1.0f);
                uninitializedMap.put("shift",
                                     Integer.valueOf(InputEvent.SHIFT_DOWN_MASK
                                                     |InputEvent.SHIFT_MASK));
                uninitializedMap.put("control",
                                     Integer.valueOf(InputEvent.CTRL_DOWN_MASK
                                                     |InputEvent.CTRL_MASK));
                uninitializedMap.put("ctrl",
                                     Integer.valueOf(InputEvent.CTRL_DOWN_MASK
                                                     |InputEvent.CTRL_MASK));
                uninitializedMap.put("meta",
                                     Integer.valueOf(InputEvent.META_DOWN_MASK
                                                     |InputEvent.META_MASK));
                uninitializedMap.put("alt",
                                     Integer.valueOf(InputEvent.ALT_DOWN_MASK
                                                     |InputEvent.ALT_MASK));
                uninitializedMap.put("altGraph",
                                     Integer.valueOf(InputEvent.ALT_GRAPH_DOWN_MASK
                                                     |InputEvent.ALT_GRAPH_MASK));
                uninitializedMap.put("button1",
                                     Integer.valueOf(InputEvent.BUTTON1_DOWN_MASK));
                uninitializedMap.put("button2",
                                     Integer.valueOf(InputEvent.BUTTON2_DOWN_MASK));
                uninitializedMap.put("button3",
                                     Integer.valueOf(InputEvent.BUTTON3_DOWN_MASK));
                modifierKeywords =
                    Collections.synchronizedMap(uninitializedMap);
            }
        }

        int count = st.countTokens();

        for (int i = 1; i <= count; i++) {
            String token = st.nextToken();

            if (typed) {
                if (token.length() != 1 || i != count) {
                    throw new IllegalArgumentException(errmsg);
                }
                return getCachedStroke(token.charAt(0), KeyEvent.VK_UNDEFINED,
                                       mask, false);
            }

            if (pressed || released || i == count) {
                if (i != count) {
                    throw new IllegalArgumentException(errmsg);
                }

                String keyCodeName = "VK_" + token;
                int keyCode = getVKValue(keyCodeName);

                return getCachedStroke(KeyEvent.CHAR_UNDEFINED, keyCode,
                                       mask, released);
            }

            if (token.equals("released")) {
                released = true;
                continue;
            }
            if (token.equals("pressed")) {
                pressed = true;
                continue;
            }
            if (token.equals("typed")) {
                typed = true;
                continue;
            }

            Integer tokenMask = (Integer)modifierKeywords.get(token);
            if (tokenMask != null) {
                mask |= tokenMask.intValue();
            } else {
                throw new IllegalArgumentException(errmsg);
            }
        }

        throw new IllegalArgumentException(errmsg);
    }

    private static VKCollection getVKCollection() {
        if (vks == null) {
            vks = new VKCollection();
        }
        return vks;
    }
    /**
     * 返回名为 <code>key</code> 的 KeyEvent.VK 字段的整数常量。如果 <code>key</code>
     * 不是有效的常量，将抛出 <code>IllegalArgumentException</code>。
     */
    private static int getVKValue(String key) {
        VKCollection vkCollect = getVKCollection();

        Integer value = vkCollect.findCode(key);

        if (value == null) {
            int keyCode = 0;
            final String errmsg = "字符串格式不正确";

            try {
                keyCode = KeyEvent.class.getField(key).getInt(KeyEvent.class);
            } catch (NoSuchFieldException nsfe) {
                throw new IllegalArgumentException(errmsg);
            } catch (IllegalAccessException iae) {
                throw new IllegalArgumentException(errmsg);
            }
            value = Integer.valueOf(keyCode);
            vkCollect.put(key, value);
        }
        return value.intValue();
    }

    /**
     * 返回此 <code>AWTKeyStroke</code> 的字符。
     *
     * @return 一个 char 值
     * @see #getAWTKeyStroke(char)
     * @see KeyEvent#getKeyChar
     */
    public final char getKeyChar() {
        return keyChar;
    }

    /**
     * 返回此 <code>AWTKeyStroke</code> 的数字键码。
     *
     * @return 包含键码值的 int
     * @see #getAWTKeyStroke(int,int)
     * @see KeyEvent#getKeyCode
     */
    public final int getKeyCode() {
        return keyCode;
    }

    /**
     * 返回此 <code>AWTKeyStroke</code> 的修饰键。
     *
     * @return 包含修饰符的 int
     * @see #getAWTKeyStroke(int,int)
     */
    public final int getModifiers() {
        return modifiers;
    }

    /**
     * 返回此 <code>AWTKeyStroke</code> 是否表示键释放。
     *
     * @return 如果此 <code>AWTKeyStroke</code> 表示键释放，则返回 <code>true</code>；否则返回 <code>false</code>
     * @see #getAWTKeyStroke(int,int,boolean)
     */
    public final boolean isOnKeyRelease() {
        return onKeyRelease;
    }

    /**
     * 返回与此 <code>AWTKeyStroke</code> 对应的 <code>KeyEvent</code> 类型。
     *
     * @return <code>KeyEvent.KEY_PRESSED</code>、
     *         <code>KeyEvent.KEY_TYPED</code> 或
     *         <code>KeyEvent.KEY_RELEASED</code>
     * @see java.awt.event.KeyEvent
     */
    public final int getKeyEventType() {
        if (keyCode == KeyEvent.VK_UNDEFINED) {
            return KeyEvent.KEY_TYPED;
        } else {
            return (onKeyRelease)
                ? KeyEvent.KEY_RELEASED
                : KeyEvent.KEY_PRESSED;
        }
    }

    /**
     * 返回一个可能唯一的数值，使其成为哈希表中的索引值的良好选择。
     *
     * @return 代表此对象的 int
     */
    public int hashCode() {
        return (((int)keyChar) + 1) * (2 * (keyCode + 1)) * (modifiers + 1) +
            (onKeyRelease ? 1 : 2);
    }

    /**
     * 如果此对象与指定对象相同，则返回 true。
     *
     * @param anObject 要与此对象比较的对象
     * @return 如果对象相同，则返回 true
     */
    public final boolean equals(Object anObject) {
        if (anObject instanceof AWTKeyStroke) {
            AWTKeyStroke ks = (AWTKeyStroke)anObject;
            return (ks.keyChar == keyChar && ks.keyCode == keyCode &&
                    ks.onKeyRelease == onKeyRelease &&
                    ks.modifiers == modifiers);
        }
        return false;
    }

    /**
     * 返回一个显示并标识此对象属性的字符串。此方法返回的 <code>String</code> 可以作为参数传递给
     * <code>getAWTKeyStroke(String)</code> 以生成与此键击相同的键击。
     *
     * @return 此对象的字符串表示形式
     * @see #getAWTKeyStroke(String)
     */
    public String toString() {
        if (keyCode == KeyEvent.VK_UNDEFINED) {
            return getModifiersText(modifiers) + "typed " + keyChar;
        } else {
            return getModifiersText(modifiers) +
                (onKeyRelease ? "released" : "pressed") + " " +
                getVKText(keyCode);
        }
    }

    static String getModifiersText(int modifiers) {
        StringBuilder buf = new StringBuilder();

        if ((modifiers & InputEvent.SHIFT_DOWN_MASK) != 0 ) {
            buf.append("shift ");
        }
        if ((modifiers & InputEvent.CTRL_DOWN_MASK) != 0 ) {
            buf.append("ctrl ");
        }
        if ((modifiers & InputEvent.META_DOWN_MASK) != 0 ) {
            buf.append("meta ");
        }
        if ((modifiers & InputEvent.ALT_DOWN_MASK) != 0 ) {
            buf.append("alt ");
        }
        if ((modifiers & InputEvent.ALT_GRAPH_DOWN_MASK) != 0 ) {
            buf.append("altGraph ");
        }
        if ((modifiers & InputEvent.BUTTON1_DOWN_MASK) != 0 ) {
            buf.append("button1 ");
        }
        if ((modifiers & InputEvent.BUTTON2_DOWN_MASK) != 0 ) {
            buf.append("button2 ");
        }
        if ((modifiers & InputEvent.BUTTON3_DOWN_MASK) != 0 ) {
            buf.append("button3 ");
        }

        return buf.toString();
    }

    static String getVKText(int keyCode) {
        VKCollection vkCollect = getVKCollection();
        Integer key = Integer.valueOf(keyCode);
        String name = vkCollect.findName(key);
        if (name != null) {
            return name.substring(3);
        }
        int expected_modifiers =
            (Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL);

        Field[] fields = KeyEvent.class.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            try {
                if (fields[i].getModifiers() == expected_modifiers
                    && fields[i].getType() == Integer.TYPE
                    && fields[i].getName().startsWith("VK_")
                    && fields[i].getInt(KeyEvent.class) == keyCode)
                {
                    name = fields[i].getName();
                    vkCollect.put(name, key);
                    return name.substring(3);
                }
            } catch (IllegalAccessException e) {
                assert(false);
            }
        }
        return "UNKNOWN";
    }

    /**
     * 返回一个与此实例相等的缓存的 <code>AWTKeyStroke</code> 实例（或 <code>AWTKeyStroke</code> 的子类实例）。
     *
     * @return 与此实例相等的缓存实例
     */
    protected Object readResolve() throws java.io.ObjectStreamException {
        synchronized (AWTKeyStroke.class) {
            if (getClass().equals(getAWTKeyStrokeClass())) {
                return  getCachedStroke(keyChar, keyCode, modifiers, onKeyRelease);
            }
        }
        return this;
    }


                private static int mapOldModifiers(int modifiers) {
        if ((modifiers & InputEvent.SHIFT_MASK) != 0) {
            modifiers |= InputEvent.SHIFT_DOWN_MASK;
        }
        if ((modifiers & InputEvent.ALT_MASK) != 0) {
            modifiers |= InputEvent.ALT_DOWN_MASK;
        }
        if ((modifiers & InputEvent.ALT_GRAPH_MASK) != 0) {
            modifiers |= InputEvent.ALT_GRAPH_DOWN_MASK;
        }
        if ((modifiers & InputEvent.CTRL_MASK) != 0) {
            modifiers |= InputEvent.CTRL_DOWN_MASK;
        }
        if ((modifiers & InputEvent.META_MASK) != 0) {
            modifiers |= InputEvent.META_DOWN_MASK;
        }

        modifiers &= InputEvent.SHIFT_DOWN_MASK
            | InputEvent.ALT_DOWN_MASK
            | InputEvent.ALT_GRAPH_DOWN_MASK
            | InputEvent.CTRL_DOWN_MASK
            | InputEvent.META_DOWN_MASK
            | InputEvent.BUTTON1_DOWN_MASK
            | InputEvent.BUTTON2_DOWN_MASK
            | InputEvent.BUTTON3_DOWN_MASK;

        return modifiers;
    }

    private static int mapNewModifiers(int modifiers) {
        if ((modifiers & InputEvent.SHIFT_DOWN_MASK) != 0) {
            modifiers |= InputEvent.SHIFT_MASK;
        }
        if ((modifiers & InputEvent.ALT_DOWN_MASK) != 0) {
            modifiers |= InputEvent.ALT_MASK;
        }
        if ((modifiers & InputEvent.ALT_GRAPH_DOWN_MASK) != 0) {
            modifiers |= InputEvent.ALT_GRAPH_MASK;
        }
        if ((modifiers & InputEvent.CTRL_DOWN_MASK) != 0) {
            modifiers |= InputEvent.CTRL_MASK;
        }
        if ((modifiers & InputEvent.META_DOWN_MASK) != 0) {
            modifiers |= InputEvent.META_MASK;
        }

        return modifiers;
    }

}

class VKCollection {
    Map<Integer, String> code2name;
    Map<String, Integer> name2code;

    public VKCollection() {
        code2name = new HashMap<>();
        name2code = new HashMap<>();
    }

    public synchronized void put(String name, Integer code) {
        assert((name != null) && (code != null));
        assert(findName(code) == null);
        assert(findCode(name) == null);
        code2name.put(code, name);
        name2code.put(name, code);
    }

    public synchronized Integer findCode(String name) {
        assert(name != null);
        return (Integer)name2code.get(name);
    }

    public synchronized String findName(Integer code) {
        assert(code != null);
        return (String)code2name.get(code);
    }
}
