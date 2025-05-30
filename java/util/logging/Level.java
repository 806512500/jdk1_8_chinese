
/*
 * Copyright (c) 2000, 2017, Oracle and/or its affiliates. All rights reserved.
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

package java.util.logging;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Level 类定义了一组标准的日志级别，可以用于控制日志输出。日志 Level 对象是有序的，并由有序的整数指定。启用给定级别的日志记录也会启用所有更高级别的日志记录。
 * <p>
 * 客户端通常应使用预定义的 Level 常量，例如 Level.SEVERE。
 * <p>
 * 从高到低的级别顺序为：
 * <ul>
 * <li>SEVERE（最高级别）
 * <li>WARNING
 * <li>INFO
 * <li>CONFIG
 * <li>FINE
 * <li>FINER
 * <li>FINEST  （最低级别）
 * </ul>
 * 另外还有一个 OFF 级别，可以用于关闭日志记录，以及一个 ALL 级别，可以用于启用所有消息的日志记录。
 * <p>
 * 第三方可以通过扩展 Level 来定义额外的日志级别。在这种情况下，子类应选择唯一的整数级别值，并确保在序列化过程中维护对象的唯一性属性，通过定义合适的 readResolve 方法。
 *
 * @since 1.4
 */

public class Level implements java.io.Serializable {
    private static final String defaultBundle = "sun.util.logging.resources.logging";

    /**
     * @serial 日志级别的非本地化名称。
     */
    private final String name;

    /**
     * @serial 日志级别的整数值。
     */
    private final int value;

    /**
     * @serial 用于本地化日志级别名称的资源包名称。
     */
    private final String resourceBundleName;

    // 本地化的日志级别名称
    private transient String localizedLevelName;
    private transient Locale cachedLocale;

    /**
     * OFF 是一个特殊的级别，可以用于关闭日志记录。
     * 这个级别初始化为 <CODE>Integer.MAX_VALUE</CODE>。
     */
    public static final Level OFF = new Level("OFF", Integer.MAX_VALUE, defaultBundle);

    /**
     * SEVERE 是一个表示严重故障的消息级别。
     * <p>
     * 一般来说，SEVERE 消息应描述对程序正常执行有重大影响的事件，并且应使最终用户和系统管理员能够理解。
     * 这个级别初始化为 <CODE>1000</CODE>。
     */
    public static final Level SEVERE = new Level("SEVERE", 1000, defaultBundle);

    /**
     * WARNING 是一个表示潜在问题的消息级别。
     * <p>
     * 一般来说，WARNING 消息应描述对最终用户或系统管理员感兴趣的事件，或表示潜在问题。
     * 这个级别初始化为 <CODE>900</CODE>。
     */
    public static final Level WARNING = new Level("WARNING", 900, defaultBundle);

    /**
     * INFO 是一个用于信息性消息的消息级别。
     * <p>
     * 通常情况下，INFO 消息将写入控制台或其等效设备。因此，INFO 级别应仅用于对最终用户和系统管理员有意义的较为重要的消息。
     * 这个级别初始化为 <CODE>800</CODE>。
     */
    public static final Level INFO = new Level("INFO", 800, defaultBundle);

    /**
     * CONFIG 是一个用于静态配置消息的消息级别。
     * <p>
     * CONFIG 消息旨在提供各种静态配置信息，以帮助解决与特定配置相关的问题。例如，CONFIG 消息可能包括 CPU 类型、图形深度、GUI 外观等。
     * 这个级别初始化为 <CODE>700</CODE>。
     */
    public static final Level CONFIG = new Level("CONFIG", 700, defaultBundle);

    /**
     * FINE 是一个提供跟踪信息的消息级别。
     * <p>
     * FINE、FINER 和 FINEST 都用于相对详细的跟踪。三个级别的确切含义因子系统而异，但一般来说，FINEST 应用于最详细（且最频繁）的输出，FINER 用于稍少详细度的输出，FINE 用于最低详细度（且最重要）的消息。
     * <p>
     * 一般来说，FINE 级别应用于对没有特定子系统兴趣的开发人员广泛感兴趣的信信息。
     * <p>
     * FINE 消息可能包括轻微（可恢复）的失败。表示潜在性能问题的问题也值得记录为 FINE。
     * 这个级别初始化为 <CODE>500</CODE>。
     */
    public static final Level FINE = new Level("FINE", 500, defaultBundle);

    /**
     * FINER 表示一个较为详细的跟踪消息。
     * 默认情况下，进入、返回或抛出异常的跟踪调用在此级别进行。
     * 这个级别初始化为 <CODE>400</CODE>。
     */
    public static final Level FINER = new Level("FINER", 400, defaultBundle);

    /**
     * FINEST 表示一个非常详细的跟踪消息。
     * 这个级别初始化为 <CODE>300</CODE>。
     */
    public static final Level FINEST = new Level("FINEST", 300, defaultBundle);

    /**
     * ALL 表示应记录所有消息。
     * 这个级别初始化为 <CODE>Integer.MIN_VALUE</CODE>。
     */
    public static final Level ALL = new Level("ALL", Integer.MIN_VALUE, defaultBundle);

    /**
     * 创建一个具有给定整数值的命名 Level。
     * <p>
     * 注意，此构造函数是“受保护的”，以允许子类化。一般来说，日志记录的客户端应使用一个常量 Level 对象，如 SEVERE 或 FINEST。但是，如果客户端需要添加新的日志级别，可以扩展 Level 并定义新的常量。
     * @param name  日志级别的名称，例如 "SEVERE"。
     * @param value 日志级别的整数值。
     * @throws NullPointerException 如果名称为 null
     */
    protected Level(String name, int value) {
        this(name, value, null);
    }

    /**
     * 创建一个具有给定整数值和给定本地化资源名称的命名 Level。
     * <p>
     * @param name  日志级别的名称，例如 "SEVERE"。
     * @param value 日志级别的整数值。
     * @param resourceBundleName 用于本地化给定名称的资源包名称。如果 resourceBundleName 为 null 或空字符串，则忽略。
     * @throws NullPointerException 如果名称为 null
     */
    protected Level(String name, int value, String resourceBundleName) {
        this(name, value, resourceBundleName, true);
    }

    // 私有构造函数，用于指定此实例是否应添加到 Level.parse 方法查找的 KnownLevel 列表中
    private Level(String name, int value, String resourceBundleName, boolean visible) {
        if (name == null) {
            throw new NullPointerException();
        }
        this.name = name;
        this.value = value;
        this.resourceBundleName = resourceBundleName;
        this.localizedLevelName = resourceBundleName == null ? name : null;
        this.cachedLocale = null;
        if (visible) {
            KnownLevel.add(this);
        }
    }

    /**
     * 返回日志级别的本地化资源包名称，如果未定义本地化包，则返回 null。
     *
     * @return 本地化资源包名称
     */
    public String getResourceBundleName() {
        return resourceBundleName;
    }

    /**
     * 返回日志级别的非本地化字符串名称。
     *
     * @return 非本地化名称
     */
    public String getName() {
        return name;
    }

    /**
     * 返回日志级别的本地化字符串名称，对于当前默认区域设置。
     * <p>
     * 如果没有可用的本地化信息，则返回非本地化名称。
     *
     * @return 本地化名称
     */
    public String getLocalizedName() {
        return getLocalizedLevelName();
    }

    // 包私有的 getLevelName() 用于实现，而不是调用子类的版本
    final String getLevelName() {
        return this.name;
    }

    private String computeLocalizedLevelName(Locale newLocale) {
        // 如果这是自定义 Level，从类路径加载资源包并返回。
        if (!defaultBundle.equals(resourceBundleName)) {
            return ResourceBundle.getBundle(resourceBundleName, newLocale,
                       ClassLoader.getSystemClassLoader()).getString(name);
        }

        // 默认包 "sun.util.logging.resources.logging" 应仅从运行时加载；因此使用扩展类加载器；
        final ResourceBundle rb = ResourceBundle.getBundle(defaultBundle, newLocale);
        final String localizedName = rb.getString(name);

        // 这是一个技巧，用于确定名称是否已翻译。如果未翻译，我们需要使用 Locale.ROOT 调用 toUpperCase()。
        final Locale rbLocale = rb.getLocale();
        final Locale locale =
                Locale.ROOT.equals(rbLocale)
                || name.equals(localizedName.toUpperCase(Locale.ROOT))
                ? Locale.ROOT : rbLocale;

        // 资源包中的全大写消息表示不需要翻译，这是 Oracle 翻译指南的一部分。为了与 Oracle JDK 实现兼容，将本地化级别名称转换为大写。
        return Locale.ROOT.equals(locale) ? name : localizedName.toUpperCase(locale);
    }

    // 避免两次查找本地化名称，如果已经缓存。
    final String getCachedLocalizedLevelName() {

        if (localizedLevelName != null) {
            if (cachedLocale != null) {
                if (cachedLocale.equals(Locale.getDefault())) {
                    // 好的：我们的缓存值是用相同的区域设置查找的。我们可以使用它。
                    return localizedLevelName;
                }
            }
        }

        if (resourceBundleName == null) {
            // 没有资源包：直接使用名称。
            return name;
        }

        // 需要计算本地化名称。
        // 要么是因为这是第一次，要么是因为我们的缓存值是为不同的区域设置的。直接返回 null。
        return null;
    }

    final synchronized String getLocalizedLevelName() {

        // 查看是否有缓存的本地化名称
        final String cachedLocalizedName = getCachedLocalizedLevelName();
        if (cachedLocalizedName != null) {
            return cachedLocalizedName;
        }

        // 没有缓存的本地化名称或缓存无效。
        // 需要计算本地化名称。
        final Locale newLocale = Locale.getDefault();
        try {
            localizedLevelName = computeLocalizedLevelName(newLocale);
        } catch (Exception ex) {
            localizedLevelName = name;
        }
        cachedLocale = newLocale;
        return localizedLevelName;
    }

    // 返回一个与给定名称匹配的镜像 Level 对象，如 Level.parse 方法中指定的。如果未找到，则返回 null。
    //
    // 如果给定名称是非本地化名称或整数，则返回与 Level.parse 方法返回的相同的 Level 对象。
    //
    // 如果名称是本地化名称，findLevel 和 parse 方法可能会返回不同的级别值，如果存在一个自定义 Level 子类覆盖了 Level.getLocalizedName() 方法，返回与默认实现不同的字符串。
    //
    static Level findLevel(String name) {
        if (name == null) {
            throw new NullPointerException();
        }

        KnownLevel level;

        // 查找具有给定非本地化名称的已知 Level。
        level = KnownLevel.findByName(name);
        if (level != null) {
            return level.mirroredLevel;
        }

        // 现在，检查给定名称是否为整数。如果是，
        // 首先查找具有给定值的 Level，然后
        // 如果必要，创建一个。
        try {
            int x = Integer.parseInt(name);
            level = KnownLevel.findByValue(x);
            if (level == null) {
                // 添加新的 Level
                Level levelObject = new Level(name, x);
                level = KnownLevel.findByValue(x);
            }
            return level.mirroredLevel;
        } catch (NumberFormatException ex) {
            // 不是整数。
            // 跳过。
        }

        level = KnownLevel.findByLocalizedLevelName(name);
        if (level != null) {
            return level.mirroredLevel;
        }

        return null;
    }

    /**
     * 返回此 Level 的字符串表示形式。
     *
     * @return 日志级别的非本地化名称，例如 "INFO"。
     */
    @Override
    public final String toString() {
        return name;
    }

    /**
     * 获取此级别的整数值。此整数值可用于在 Level 对象之间进行高效的顺序比较。
     * @return 此级别的整数值。
     */
    public final int intValue() {
        return value;
    }

    private static final long serialVersionUID = -8176160795706313070L;

    // 序列化魔法，防止“双胞胎”。
    // 这是一个性能优化。
    private Object readResolve() {
        KnownLevel o = KnownLevel.matches(this);
        if (o != null) {
            return o.levelObject;
        }

        // 哦哦。发送此对象的人知道
        // 一个新的日志级别。将其添加到我们的列表中。
        Level level = new Level(this.name, this.value, this.resourceBundleName);
        return level;
    }


                /**
     * 将级别名称字符串解析为 Level。
     * <p>
     * 参数字符串可以是级别名称
     * 或整数值。
     * <p>
     * 例如：
     * <ul>
     * <li>     "SEVERE"
     * <li>     "1000"
     * </ul>
     *
     * @param  name   要解析的字符串
     * @throws NullPointerException 如果名称为 null
     * @throws IllegalArgumentException 如果值无效。
     * 有效值是介于 <CODE>Integer.MIN_VALUE</CODE>
     * 和 <CODE>Integer.MAX_VALUE</CODE> 之间的整数，以及所有已知的级别名称。
     * 已知名称是此类定义的级别（例如，<CODE>FINE</CODE>，
     * <CODE>FINER</CODE>，<CODE>FINEST</CODE>），或由此类创建的具有适当包访问权限的级别，
     * 或由子类定义或创建的新级别。
     *
     * @return 解析的值。传递一个对应于已知名称的整数
     * （例如，700）将返回关联的名称（例如，<CODE>CONFIG</CODE>）。
     * 传递一个不对应于已知名称的整数（例如，1）将返回一个初始化为该值的新级别名称。
     */
    public static synchronized Level parse(String name) throws IllegalArgumentException {
        // 检查名称是否为 null。
        name.length();

        KnownLevel level;

        // 查找具有给定非本地化名称的已知级别。
        level = KnownLevel.findByName(name);
        if (level != null) {
            return level.levelObject;
        }

        // 现在，检查给定名称是否为整数。如果是，
        // 首先查找具有给定值的级别，然后
        // 如有必要，创建一个。
        try {
            int x = Integer.parseInt(name);
            level = KnownLevel.findByValue(x);
            if (level == null) {
                // 添加新级别
                Level levelObject = new Level(name, x);
                level = KnownLevel.findByValue(x);
            }
            return level.levelObject;
        } catch (NumberFormatException ex) {
            // 不是整数。
            // 继续。
        }

        // 最后，查找具有给定本地化名称的已知级别，
        // 在当前默认区域设置中。
        // 这相对昂贵，但不过分。
        level = KnownLevel.findByLocalizedLevelName(name);
        if (level != null) {
            return level.levelObject;
        }

        // 好的，我们已经尝试了一切但失败了
        throw new IllegalArgumentException("Bad level \"" + name + "\"");
    }

    /**
     * 比较两个对象的值是否相等。
     * @return 如果且仅当两个对象具有相同的级别值时返回 true。
     */
    @Override
    public boolean equals(Object ox) {
        try {
            Level lx = (Level)ox;
            return (lx.value == this.value);
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * 生成哈希码。
     * @return 基于级别值的哈希码
     */
    @Override
    public int hashCode() {
        return this.value;
    }

    // KnownLevel 类维护所有已知级别的全局列表。
    // API 允许创建多个具有相同名称/值的自定义 Level 实例。
    // 该类提供了方便的方法来查找给定名称、给定值或给定本地化名称的级别。
    //
    // KnownLevel 包装了以下 Level 对象：
    // 1. levelObject:   标准 Level 对象或自定义 Level 对象
    // 2. mirroredLevel: 表示在日志配置中指定的级别的 Level 对象。
    //
    // Level.getName, Level.getLocalizedName, Level.getResourceBundleName 方法
    // 是非 final 的，但名称和资源包名称是 Level 构造函数的参数。
    // 使用 mirroredLevel 对象而不是 levelObject 以防止日志框架执行
    // 由不可信的 Level 子类实现的外部代码。
    //
    // 实现说明：
    // 如果 Level.getName, Level.getLocalizedName, Level.getResourceBundleName 方法
    // 是 final 的，以下 KnownLevel 实现可以移除。
    // 未来的 API 更改应考虑这一点。
    static final class KnownLevel {
        private static Map<String, List<KnownLevel>> nameToLevels = new HashMap<>();
        private static Map<Integer, List<KnownLevel>> intToLevels = new HashMap<>();
        final Level levelObject;     // Level 类或 Level 子类的实例
        final Level mirroredLevel;   // 自定义 Level 的镜像
        KnownLevel(Level l) {
            this.levelObject = l;
            if (l.getClass() == Level.class) {
                this.mirroredLevel = l;
            } else {
                // 此镜像级别对象是隐藏的
                this.mirroredLevel = new Level(l.name, l.value, l.resourceBundleName, false);
            }
        }

        static synchronized void add(Level l) {
            // 镜像级别对象始终在自定义 Level 实例之前添加到列表中
            KnownLevel o = new KnownLevel(l);
            List<KnownLevel> list = nameToLevels.get(l.name);
            if (list == null) {
                list = new ArrayList<>();
                nameToLevels.put(l.name, list);
            }
            list.add(o);

            list = intToLevels.get(l.value);
            if (list == null) {
                list = new ArrayList<>();
                intToLevels.put(l.value, list);
            }
            list.add(o);
        }

        // 返回具有给定非本地化名称的 KnownLevel。
        static synchronized KnownLevel findByName(String name) {
            List<KnownLevel> list = nameToLevels.get(name);
            if (list != null) {
                return list.get(0);
            }
            return null;
        }

        // 返回具有给定值的 KnownLevel。
        static synchronized KnownLevel findByValue(int value) {
            List<KnownLevel> list = intToLevels.get(value);
            if (list != null) {
                return list.get(0);
            }
            return null;
        }

        // 返回具有给定本地化名称的 KnownLevel，通过调用
        // Level.getLocalizedLevelName() 方法（即从与 Level 对象关联的资源包中找到）。
        // 此方法不调用 Level.getLocalizedName()，该方法可能在子类实现中被重写。
        static synchronized KnownLevel findByLocalizedLevelName(String name) {
            for (List<KnownLevel> levels : nameToLevels.values()) {
                for (KnownLevel l : levels) {
                    String lname = l.levelObject.getLocalizedLevelName();
                    if (name.equals(lname)) {
                        return l;
                    }
                }
            }
            return null;
        }

        static synchronized KnownLevel matches(Level l) {
            List<KnownLevel> list = nameToLevels.get(l.name);
            if (list != null) {
                for (KnownLevel level : list) {
                    Level other = level.mirroredLevel;
                    Class<? extends Level> type = level.levelObject.getClass();
                    if (l.value == other.value &&
                           (l.resourceBundleName == other.resourceBundleName ||
                               (l.resourceBundleName != null &&
                                l.resourceBundleName.equals(other.resourceBundleName)))) {
                        if (type == l.getClass()) {
                            return level;
                        }
                    }
                }
            }
            return null;
        }
    }

}
