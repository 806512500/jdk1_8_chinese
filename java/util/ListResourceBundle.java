/*
 * 版权所有 (c) 1996, 2013，Oracle 及/或其附属公司。保留所有权利。
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

/*
 * 版权所有 (C) 1996, 1997 - 保留所有权利，Taligent, Inc.
 * 版权所有 (C) 1996 - 1998 - 保留所有权利，IBM Corp.
 *
 * 本源代码和文档的原始版本受 Taligent, Inc. 的版权和所有权保护，Taligent, Inc. 是 IBM 的全资子公司。这些材料是根据 Taligent 和 Sun 之间的许可协议提供的。这项技术受到多项美国和国际专利的保护。
 *
 * 本通知和对 Taligent 的归属不得移除。Taligent 是 Taligent, Inc. 的注册商标。
 *
 */

package java.util;

import sun.util.ResourceBundleEnumeration;

/**
 * <code>ListResourceBundle</code> 是 <code>ResourceBundle</code> 的一个抽象子类，它以方便和易于使用的方式管理特定区域设置的资源。有关资源包的一般信息，请参阅 <code>ResourceBundle</code>。
 *
 * <P>
 * 子类必须覆盖 <code>getContents</code> 并提供一个数组，数组中的每个项目都是一对对象。每对中的第一个元素是键，必须是 <code>String</code> 类型，第二个元素是与该键关联的值。
 *
 * <p>
 * 以下 <a name="sample">示例</a> 显示了名为 "MyResources" 的资源包家族的两个成员。“MyResources” 是该资源包家族的默认成员，而 “MyResources_fr” 是法语成员。这些成员基于 <code>ListResourceBundle</code>（一个相关的 <a href="PropertyResourceBundle.html#sample">示例</a> 展示了如何向此家族添加基于属性文件的资源包）。本示例中的键形式为 "s1" 等。实际的键完全由您选择，只要它们与您在程序中用于从资源包中检索对象的键相同即可。键是区分大小写的。
 * <blockquote>
 * <pre>
 *
 * public class MyResources extends ListResourceBundle {
 *     protected Object[][] getContents() {
 *         return new Object[][] {
 *         // 本地化此部分
 *             {"s1", "The disk \"{1}\" contains {0}."},  // MessageFormat 模式
 *             {"s2", "1"},                               // 模式中 {0} 的位置
 *             {"s3", "My Disk"},                         // 示例磁盘名称
 *             {"s4", "no files"},                        // 第一个 ChoiceFormat 选项
 *             {"s5", "one file"},                        // 第二个 ChoiceFormat 选项
 *             {"s6", "{0,number} files"},                // 第三个 ChoiceFormat 选项
 *             {"s7", "3 Mar 96"},                        // 示例日期
 *             {"s8", new Dimension(1,5)}                 // 真实对象，而不仅仅是字符串
 *         // 本地化部分结束
 *         };
 *     }
 * }
 *
 * public class MyResources_fr extends ListResourceBundle {
 *     protected Object[][] getContents() {
 *         return new Object[][] {
 *         // 本地化此部分
 *             {"s1", "Le disque \"{1}\" {0}."},          // MessageFormat 模式
 *             {"s2", "1"},                               // 模式中 {0} 的位置
 *             {"s3", "Mon disque"},                      // 示例磁盘名称
 *             {"s4", "ne contient pas de fichiers"},     // 第一个 ChoiceFormat 选项
 *             {"s5", "contient un fichier"},             // 第二个 ChoiceFormat 选项
 *             {"s6", "contient {0,number} fichiers"},    // 第三个 ChoiceFormat 选项
 *             {"s7", "3 mars 1996"},                     // 示例日期
 *             {"s8", new Dimension(1,3)}                 // 真实对象，而不仅仅是字符串
 *         // 本地化部分结束
 *         };
 *     }
 * }
 * </pre>
 * </blockquote>
 *
 * <p>
 * 如果一个 {@code ListResourceBundle} 子类被多个线程同时使用，其实现必须是线程安全的。此类方法的默认实现是线程安全的。
 *
 * @see ResourceBundle
 * @see PropertyResourceBundle
 * @since JDK1.1
 */
public abstract class ListResourceBundle extends ResourceBundle {
    /**
     * 唯一的构造函数。通常由子类构造函数隐式调用。
     */
    public ListResourceBundle() {
    }

    // 实现 java.util.ResourceBundle.handleGetObject；继承 javadoc 规范。
    public final Object handleGetObject(String key) {
        // 惰性加载查找哈希表。
        if (lookup == null) {
            loadLookup();
        }
        if (key == null) {
            throw new NullPointerException();
        }
        return lookup.get(key); // 本类忽略区域设置
    }

    /**
     * 返回此 <code>ResourceBundle</code> 及其父资源包中包含的键的 <code>Enumeration</code>。
     *
     * @return 一个包含此 <code>ResourceBundle</code> 及其父资源包中包含的键的 <code>Enumeration</code>。
     * @see #keySet()
     */
    public Enumeration<String> getKeys() {
        // 惰性加载查找哈希表。
        if (lookup == null) {
            loadLookup();
        }


        ResourceBundle parent = this.parent;
        return new ResourceBundleEnumeration(lookup.keySet(),
                (parent != null) ? parent.getKeys() : null);
    }

    /**
     * 返回一个 <code>Set</code>，其中包含仅在此 <code>ResourceBundle</code> 中的键。
     *
     * @return 一个 <code>Set</code>，其中包含仅在此 <code>ResourceBundle</code> 中的键
     * @since 1.6
     * @see #keySet()
     */
    protected Set<String> handleKeySet() {
        if (lookup == null) {
            loadLookup();
        }
        return lookup.keySet();
    }

    /**
     * 返回一个数组，其中每个项是一个包含两个对象的 <code>Object</code> 数组。每个对的第一个元素
     * 是键，必须是 <code>String</code>，第二个元素是与该键关联的值。详细信息请参阅类描述。
     *
     * @return 一个表示键值对的 <code>Object</code> 数组的数组。
     */
    abstract protected Object[][] getContents();

    // ==================私有方法====================

    /**
     * 我们惰性加载 lookup 哈希表。此函数执行加载。
     */
    private synchronized void loadLookup() {
        if (lookup != null)
            return;

        Object[][] contents = getContents();
        HashMap<String,Object> temp = new HashMap<>(contents.length);
        for (int i = 0; i < contents.length; ++i) {
            // 键必须是非空字符串，值也必须是非空
            String key = (String) contents[i][0];
            Object value = contents[i][1];
            if (key == null || value == null) {
                throw new NullPointerException();
            }
            temp.put(key, value);
        }
        lookup = temp;
    }

    private Map<String,Object> lookup = null;
}
