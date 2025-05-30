
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
package java.awt;

import java.io.File;
import java.io.FileInputStream;

import java.beans.ConstructorProperties;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;

import java.security.AccessController;

import sun.util.logging.PlatformLogger;
import sun.awt.AWTAccessor;

/**
 * 一个类，用于封装鼠标光标的位图表示。
 *
 * @see Component#setCursor
 * @author      Amy Fowler
 */
public class Cursor implements java.io.Serializable {

    /**
     * 默认光标类型（如果未定义光标，则设置此类型）。
     */
    public static final int     DEFAULT_CURSOR                  = 0;

    /**
     * 十字光标类型。
     */
    public static final int     CROSSHAIR_CURSOR                = 1;

    /**
     * 文本光标类型。
     */
    public static final int     TEXT_CURSOR                     = 2;

    /**
     * 等待光标类型。
     */
    public static final int     WAIT_CURSOR                     = 3;

    /**
     * 西南调整大小光标类型。
     */
    public static final int     SW_RESIZE_CURSOR                = 4;

    /**
     * 东南调整大小光标类型。
     */
    public static final int     SE_RESIZE_CURSOR                = 5;

    /**
     * 西北调整大小光标类型。
     */
    public static final int     NW_RESIZE_CURSOR                = 6;

    /**
     * 东北调整大小光标类型。
     */
    public static final int     NE_RESIZE_CURSOR                = 7;

    /**
     * 北调整大小光标类型。
     */
    public static final int     N_RESIZE_CURSOR                 = 8;

    /**
     * 南调整大小光标类型。
     */
    public static final int     S_RESIZE_CURSOR                 = 9;

    /**
     * 西调整大小光标类型。
     */
    public static final int     W_RESIZE_CURSOR                 = 10;

    /**
     * 东调整大小光标类型。
     */
    public static final int     E_RESIZE_CURSOR                 = 11;

    /**
     * 手形光标类型。
     */
    public static final int     HAND_CURSOR                     = 12;

    /**
     * 移动光标类型。
     */
    public static final int     MOVE_CURSOR                     = 13;

    /**
      * @deprecated 自JDK 1.7版本起，应使用 {@link #getPredefinedCursor(int)}
      * 方法代替。
      */
    @Deprecated
    protected static Cursor predefined[] = new Cursor[14];

    /**
     * 这个字段是 'predefined' 数组的私有替代。
     */
    private final static Cursor[] predefinedPrivate = new Cursor[14];

    /* 本地化名称和默认值 */
    static final String[][] cursorProperties = {
        { "AWT.DefaultCursor", "默认光标" },
        { "AWT.CrosshairCursor", "十字光标" },
        { "AWT.TextCursor", "文本光标" },
        { "AWT.WaitCursor", "等待光标" },
        { "AWT.SWResizeCursor", "西南调整大小光标" },
        { "AWT.SEResizeCursor", "东南调整大小光标" },
        { "AWT.NWResizeCursor", "西北调整大小光标" },
        { "AWT.NEResizeCursor", "东北调整大小光标" },
        { "AWT.NResizeCursor", "北调整大小光标" },
        { "AWT.SResizeCursor", "南调整大小光标" },
        { "AWT.WResizeCursor", "西调整大小光标" },
        { "AWT.EResizeCursor", "东调整大小光标" },
        { "AWT.HandCursor", "手形光标" },
        { "AWT.MoveCursor", "移动光标" },
    };

    /**
     * 初始设置为 <code>DEFAULT_CURSOR</code> 的选定光标类型。
     *
     * @serial
     * @see #getType()
     */
    int type = DEFAULT_CURSOR;

    /**
     * 与所有自定义光标关联的类型。
     */
    public static final int     CUSTOM_CURSOR                   = -1;

    /*
     * 自定义光标支持的哈希表、文件系统目录前缀、文件名和属性
     */

    private static final Hashtable<String,Cursor> systemCustomCursors = new Hashtable<>(1);
    private static final String systemCustomCursorDirPrefix = initCursorDir();

    private static String initCursorDir() {
        String jhome = java.security.AccessController.doPrivileged(
               new sun.security.action.GetPropertyAction("java.home"));
        return jhome +
            File.separator + "lib" + File.separator + "images" +
            File.separator + "cursors" + File.separator;
    }

    private static final String     systemCustomCursorPropertiesFile = systemCustomCursorDirPrefix + "cursors.properties";

    private static       Properties systemCustomCursorProperties = null;

    private static final String CursorDotPrefix  = "Cursor.";
    private static final String DotFileSuffix    = ".File";
    private static final String DotHotspotSuffix = ".HotSpot";
    private static final String DotNameSuffix    = ".Name";

    /*
     * JDK 1.1 serialVersionUID
     */
    private static final long serialVersionUID = 8028237497568985504L;

    private static final PlatformLogger log = PlatformLogger.getLogger("java.awt.Cursor");

    static {
        /* 确保加载必要的本机库 */
        Toolkit.loadLibraries();
        if (!GraphicsEnvironment.isHeadless()) {
            initIDs();
        }

        AWTAccessor.setCursorAccessor(
            new AWTAccessor.CursorAccessor() {
                public long getPData(Cursor cursor) {
                    return cursor.pData;
                }

                public void setPData(Cursor cursor, long pData) {
                    cursor.pData = pData;
                }

                public int getType(Cursor cursor) {
                    return cursor.type;
                }
            });
    }

    /**
     * 初始化可以从C访问的字段的JNI字段和方法ID。
     */
    private static native void initIDs();

    /**
     * 连接到本机数据。
     */
    private transient long pData;

    private transient Object anchor = new Object();

    static class CursorDisposer implements sun.java2d.DisposerRecord {
        volatile long pData;
        public CursorDisposer(long pData) {
            this.pData = pData;
        }
        public void dispose() {
            if (pData != 0) {
                finalizeImpl(pData);
            }
        }
    }
    transient CursorDisposer disposer;
    private void setPData(long pData) {
        this.pData = pData;
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        if (disposer == null) {
            disposer = new CursorDisposer(pData);
            // 反序列化后 anchor 为 null
            if (anchor == null) {
                anchor = new Object();
            }
            sun.java2d.Disposer.addRecord(anchor, disposer);
        } else {
            disposer.pData = pData;
        }
    }

    /**
     * 光标的用户可见名称。
     *
     * @serial
     * @see #getName()
     */
    protected String name;

    /**
     * 返回具有指定预定义类型的光标对象。
     *
     * @param type 预定义光标的类型
     * @return 指定的预定义光标
     * @throws IllegalArgumentException 如果指定的光标类型无效
     */
    static public Cursor getPredefinedCursor(int type) {
        if (type < Cursor.DEFAULT_CURSOR || type > Cursor.MOVE_CURSOR) {
            throw new IllegalArgumentException("非法光标类型");
        }
        Cursor c = predefinedPrivate[type];
        if (c == null) {
            predefinedPrivate[type] = c = new Cursor(type);
        }
        // 为向后兼容填充 'predefined' 数组。
        if (predefined[type] == null) {
            predefined[type] = c;
        }
        return c;
    }

    /**
     * 返回与指定名称匹配的系统特定自定义光标对象。光标名称例如： "Invalid.16x16"
     *
     * @param name 描述所需系统特定自定义光标的字符串
     * @return 系统特定的自定义光标
     * @exception HeadlessException 如果
     * <code>GraphicsEnvironment.isHeadless</code> 返回 true
     */
    static public Cursor getSystemCustomCursor(final String name)
        throws AWTException, HeadlessException {
        GraphicsEnvironment.checkHeadless();
        Cursor cursor = systemCustomCursors.get(name);

        if (cursor == null) {
            synchronized(systemCustomCursors) {
                if (systemCustomCursorProperties == null)
                    loadSystemCustomCursorProperties();
            }

            String prefix = CursorDotPrefix + name;
            String key    = prefix + DotFileSuffix;

            if (!systemCustomCursorProperties.containsKey(key)) {
                if (log.isLoggable(PlatformLogger.Level.FINER)) {
                    log.finer("Cursor.getSystemCustomCursor(" + name + ") 返回 null");
                }
                return null;
            }

            final String fileName =
                systemCustomCursorProperties.getProperty(key);

            String localized = systemCustomCursorProperties.getProperty(prefix + DotNameSuffix);

            if (localized == null) localized = name;

            String hotspot = systemCustomCursorProperties.getProperty(prefix + DotHotspotSuffix);

            if (hotspot == null)
                throw new AWTException("未定义光标 " + name + " 的热点属性");

            StringTokenizer st = new StringTokenizer(hotspot, ",");

            if (st.countTokens() != 2)
                throw new AWTException("解析光标 " + name + " 的热点属性失败");

            int x = 0;
            int y = 0;

            try {
                x = Integer.parseInt(st.nextToken());
                y = Integer.parseInt(st.nextToken());
            } catch (NumberFormatException nfe) {
                throw new AWTException("解析光标 " + name + " 的热点属性失败");
            }

            try {
                final int fx = x;
                final int fy = y;
                final String flocalized = localized;

                cursor = java.security.AccessController.<Cursor>doPrivileged(
                    new java.security.PrivilegedExceptionAction<Cursor>() {
                    public Cursor run() throws Exception {
                        Toolkit toolkit = Toolkit.getDefaultToolkit();
                        Image image = toolkit.getImage(
                           systemCustomCursorDirPrefix + fileName);
                        return toolkit.createCustomCursor(
                                    image, new Point(fx,fy), flocalized);
                    }
                });
            } catch (Exception e) {
                throw new AWTException(
                    "创建光标 " + name + " 时发生异常: " + e.getClass() + " " + e.getMessage());
            }

            if (cursor == null) {
                if (log.isLoggable(PlatformLogger.Level.FINER)) {
                    log.finer("Cursor.getSystemCustomCursor(" + name + ") 返回 null");
                }
            } else {
                systemCustomCursors.put(name, cursor);
            }
        }

        return cursor;
    }

    /**
     * 返回系统默认光标。
     */
    static public Cursor getDefaultCursor() {
        return getPredefinedCursor(Cursor.DEFAULT_CURSOR);
    }

    /**
     * 创建具有指定类型的新的光标对象。
     * @param type 光标的类型
     * @throws IllegalArgumentException 如果指定的光标类型无效
     */
    @ConstructorProperties({"type"})
    public Cursor(int type) {
        if (type < Cursor.DEFAULT_CURSOR || type > Cursor.MOVE_CURSOR) {
            throw new IllegalArgumentException("非法光标类型");
        }
        this.type = type;

        // 查找本地化名称。
        name = Toolkit.getProperty(cursorProperties[type][0],
                                   cursorProperties[type][1]);
    }

    /**
     * 创建具有指定名称的新自定义光标对象。<p>
     * 注意：此构造函数仅应由 AWT 实现用于支持自定义光标。应用程序应使用 Toolkit.createCustomCursor()。
     * @param name 光标的用户可见名称。
     * @see java.awt.Toolkit#createCustomCursor
     */
    protected Cursor(String name) {
        this.type = Cursor.CUSTOM_CURSOR;
        this.name = name;
    }

    /**
     * 返回此光标的类型。
     */
    public int getType() {
        return type;
    }

    /**
     * 返回此光标的名称。
     * @return 此光标的本地化描述。
     * @since     1.2
     */
    public String getName() {
        return name;
    }

    /**
     * 返回此光标的字符串表示形式。
     * @return 此光标的字符串表示形式。
     * @since     1.2
     */
    public String toString() {
        return getClass().getName() + "[" + getName() + "]";
    }

    /*
     * 加载 cursor.properties 文件
     */
    private static void loadSystemCustomCursorProperties() throws AWTException {
        synchronized(systemCustomCursors) {
            systemCustomCursorProperties = new Properties();

            try {
                AccessController.<Object>doPrivileged(
                      new java.security.PrivilegedExceptionAction<Object>() {
                    public Object run() throws Exception {
                        FileInputStream fis = null;
                        try {
                            fis = new FileInputStream(
                                           systemCustomCursorPropertiesFile);
                            systemCustomCursorProperties.load(fis);
                        } finally {
                            if (fis != null)
                                fis.close();
                        }
                        return null;
                    }
                });
            } catch (Exception e) {
                systemCustomCursorProperties = null;
                 throw new AWTException("加载 " +
                                        systemCustomCursorPropertiesFile +
                                        " 时发生异常: " + e.getClass() + " " +
                   e.getMessage());
            }
        }
    }

    private native static void finalizeImpl(long pData);
}
