
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

package java.util.prefs;

import java.util.*;
import java.io.*;
import java.security.AccessController;
import java.security.PrivilegedAction;
// 这些导入仅作为 JavaDoc 错误的变通方法
import java.lang.Integer;
import java.lang.Long;
import java.lang.Float;
import java.lang.Double;

/**
 * 该类为 {@link Preferences} 类提供了骨架实现，极大地简化了实现它的任务。
 *
 * <p><strong>此类仅适用于 <tt>Preferences</tt> 实现者。
 * <tt>Preferences</tt> 设施的普通用户无需查阅此文档。 {@link Preferences} 文档
 * 应该足够。</strong>
 *
 * <p>实现者必须覆盖九个抽象的服务提供者接口 (SPI) 方法：{@link #getSpi(String)}，{@link #putSpi(String,String)}，
 * {@link #removeSpi(String)}，{@link #childSpi(String)}，{@link #removeNodeSpi()}，{@link #keysSpi()}，
 * {@link #childrenNamesSpi()}，{@link #syncSpi()} 和 {@link #flushSpi()}。所有具体方法都精确地指定了
 * 它们是如何基于这些 SPI 方法实现的。实现者可以根据需要自行决定覆盖一个或多个具体方法，如果默认实现因任何原因
 * 不令人满意，例如性能问题。
 *
 * <p>SPI 方法在异常行为方面分为三组。 <tt>getSpi</tt> 方法不应抛出异常，但这并不重要，因为此方法抛出的任何异常
 * 都将被 {@link #get(String,String)} 拦截，后者将返回指定的默认值给调用者。 <tt>removeNodeSpi, keysSpi,
 * childrenNamesSpi, syncSpi</tt> 和 <tt>flushSpi</tt> 方法指定抛出 {@link BackingStoreException}，
 * 并且如果实现无法执行操作，则必须抛出此检查异常。该异常会向外传播，导致相应的 API 方法失败。
 *
 * <p>其余的 SPI 方法 {@link #putSpi(String,String)}，{@link #removeSpi(String)} 和 {@link #childSpi(String)}
 * 具有更复杂的异常行为。它们不指定抛出 <tt>BackingStoreException</tt>，因为即使后端存储不可用，它们通常也能遵守其合同。
 * 这是因为它们不返回任何信息，且其效果不需要在后续调用 {@link Preferences#flush()} 或
 * {@link Preferences#sync()} 之前变为永久。通常情况下，这些 SPI 方法不应抛出异常。在某些实现中，可能会有情况
 * 使得这些调用甚至无法将请求的操作排队以供稍后处理。即使在这种情况下，通常最好忽略调用并返回，而不是抛出异常。
 * 但在这些情况下，所有后续的 <tt>flush()</tt> 和 <tt>sync</tt> 调用应返回 <tt>false</tt>，因为返回 <tt>true</tt>
 * 会暗示所有先前的操作都已成功变为永久。
 *
 * <p>有一种情况 <tt>putSpi, removeSpi 和 childSpi</tt> <i>应该</i> 抛出异常：如果调用者在底层操作系统上缺乏
 * 执行请求操作的足够权限。例如，如果非特权用户尝试修改系统首选项，这将在大多数系统上发生。
 * （所需的权限将因实现而异。在某些实现中，它们是修改文件系统中某些目录内容的权利；在其他实现中，它们是修改注册表中
 * 某些键内容的权利。）在这些情况下，通常不希望让程序继续执行，好像这些操作将在稍后变为永久一样。虽然实现不要求
 * 在这些情况下抛出异常，但强烈鼓励这样做。抛出 {@link SecurityException} 是合适的。
 *
 * <p>大多数 SPI 方法要求实现从首选项节点读取或写入信息。实现者应注意，另一个虚拟机可能已并发地从后端存储中删除了
 * 该节点。如果该节点已被删除，由实现负责重新创建该节点。
 *
 * <p>实现说明：在 Sun 的默认 <tt>Preferences</tt> 实现中，用户的标识继承自底层操作系统，并且在虚拟机的生命周期内不会改变。
 * 但认识到服务器端 <tt>Preferences</tt> 实现的用户标识可能会从请求到请求发生变化，通过使用静态 {@link ThreadLocal}
 * 实例隐式传递给 <tt>Preferences</tt> 方法。此类实现的作者 <i>强烈</i> 建议在访问首选项时（例如通过 {@link #get(String,String)}
 * 或 {@link #put(String,String)} 方法）确定用户，而不是将用户永久关联到每个 <tt>Preferences</tt> 实例。后一种行为与
 * 正常的 <tt>Preferences</tt> 用法冲突，并会导致极大的混乱。
 *
 * @author  Josh Bloch
 * @see     Preferences
 * @since   1.4
 */
public abstract class AbstractPreferences extends Preferences {
    /**
     * 相对于父节点的名称。
     */
    private final String name;

    /**
     * 绝对路径名称。
     */
    private final String absolutePath;

    /**
     * 父节点。
     */
    final AbstractPreferences parent;

    /**
     * 根节点。
     */
    private final AbstractPreferences root; // 相对于此节点

    /**
     * 如果此节点在创建此对象之前在后端存储中不存在，则此字段应为 <tt>true</tt>。该字段初始化为 false，但可以由子类构造函数
     * 设置为 true（并且不应在此之后修改）。此字段指示在创建完成时是否应触发节点更改事件。
     */
    protected boolean newNode = false;

    /**
     * 本节点的所有已知未删除的子节点。 （此“缓存”在调用 childSpi() 或 getChild() 之前被查询。）
     */
    private Map<String, AbstractPreferences> kidCache = new HashMap<>();

    /**
     * 用于跟踪此节点是否已被删除的字段。一旦设置为 true，将永远不会重置为 false。
     */
    private boolean removed = false;

    /**
     * 注册的首选项更改监听器。
     */
    private PreferenceChangeListener[] prefListeners =
        new PreferenceChangeListener[0];

    /**
     * 注册的节点更改监听器。
     */
    private NodeChangeListener[] nodeListeners = new NodeChangeListener[0];

    /**
     * 用于锁定此节点的对象。此对象优先于节点本身使用，以减少因锁定节点而导致的有意或无意的拒绝服务的可能性。
     * 为了避免死锁，一个线程永远不会锁定其后代节点的锁。
     */
    protected final Object lock = new Object();

    /**
     * 创建具有指定父节点和相对于其父节点的指定名称的首选项节点。
     *
     * @param parent 本首选项节点的父节点，如果这是根节点，则为 null。
     * @param name 本首选项节点的名称，相对于其父节点，如果这是根节点，则为 <tt>""</tt>。
     * @throws IllegalArgumentException 如果 <tt>name</tt> 包含斜杠 (<tt>'/'</tt>)，或者 <tt>parent</tt> 为 <tt>null</tt>
     *          且 name 不是 <tt>""</tt>。
     */
    protected AbstractPreferences(AbstractPreferences parent, String name) {
        if (parent==null) {
            if (!name.equals(""))
                throw new IllegalArgumentException("Root name '"+name+
                                                   "' must be \"\"");
            this.absolutePath = "/";
            root = this;
        } else {
            if (name.indexOf('/') != -1)
                throw new IllegalArgumentException("Name '" + name +
                                                 "' contains '/'");
            if (name.equals(""))
              throw new IllegalArgumentException("Illegal name: empty string");

            root = parent.root;
            absolutePath = (parent==root ? "/" + name
                                         : parent.absolutePath() + "/" + name);
        }
        this.name = name;
        this.parent = parent;
    }

    /**
     * 按照 {@link Preferences#put(String,String)} 中的规范实现 <tt>put</tt> 方法。
     *
     * <p>此实现检查键和值是否合法，获取此首选项节点的锁，检查节点是否已被删除，调用 {@link #putSpi(String,String)}，
     * 并且如果有任何首选项更改监听器，则将通知事件排队以由事件调度线程处理。
     *
     * @param key 要与指定值关联的键。
     * @param value 要与指定键关联的值。
     * @throws NullPointerException 如果 key 或 value 为 <tt>null</tt>。
     * @throws IllegalArgumentException 如果 <tt>key.length()</tt> 超过 <tt>MAX_KEY_LENGTH</tt> 或
     *       <tt>value.length</tt> 超过 <tt>MAX_VALUE_LENGTH</tt>。
     * @throws IllegalStateException 如果此节点（或其祖先）已通过 {@link #removeNode()} 方法删除。
     */
    public void put(String key, String value) {
        if (key==null || value==null)
            throw new NullPointerException();
        if (key.length() > MAX_KEY_LENGTH)
            throw new IllegalArgumentException("Key too long: "+key);
        if (value.length() > MAX_VALUE_LENGTH)
            throw new IllegalArgumentException("Value too long: "+value);

        synchronized(lock) {
            if (removed)
                throw new IllegalStateException("Node has been removed.");

            putSpi(key, value);
            enqueuePreferenceChangeEvent(key, value);
        }
    }

    /**
     * 按照 {@link Preferences#get(String,String)} 中的规范实现 <tt>get</tt> 方法。
     *
     * <p>此实现首先检查 <tt>key</tt> 是否为 <tt>null</tt>，如果是，则抛出 <tt>NullPointerException</tt>。
     * 然后获取此首选项节点的锁，检查节点是否已被删除，调用 {@link #getSpi(String)}，并返回结果，除非 <tt>getSpi</tt>
     * 调用返回 <tt>null</tt> 或抛出异常，在这种情况下，此调用返回 <tt>def</tt>。
     *
     * @param key 要返回其关联值的键。
     * @param def 如果此首选项节点没有与 <tt>key</tt> 关联的值，则返回此值。
     * @return 与 <tt>key</tt> 关联的值，如果没有与 <tt>key</tt> 关联的值，则返回 <tt>def</tt>。
     * @throws IllegalStateException 如果此节点（或其祖先）已通过 {@link #removeNode()} 方法删除。
     * @throws NullPointerException 如果 key 为 <tt>null</tt>。 （<tt>null</tt> 默认值是允许的。）
     */
    public String get(String key, String def) {
        if (key==null)
            throw new NullPointerException("Null key");
        synchronized(lock) {
            if (removed)
                throw new IllegalStateException("Node has been removed.");

            String result = null;
            try {
                result = getSpi(key);
            } catch (Exception e) {
                // 忽略异常会导致返回默认值
            }
            return (result==null ? def : result);
        }
    }

    /**
     * 按照 {@link Preferences#remove(String)} 中的规范实现 <tt>remove(String)</tt> 方法。
     *
     * <p>此实现获取此首选项节点的锁，检查节点是否已被删除，调用 {@link #removeSpi(String)}，并且如果有任何首选项
     * 更改监听器，则将通知事件排队以由事件调度线程处理。
     *
     * @param key 要从首选项节点中移除其映射的键。
     * @throws IllegalStateException 如果此节点（或其祖先）已通过 {@link #removeNode()} 方法删除。
     * @throws NullPointerException {@inheritDoc}.
     */
    public void remove(String key) {
        Objects.requireNonNull(key, "Specified key cannot be null");
        synchronized(lock) {
            if (removed)
                throw new IllegalStateException("Node has been removed.");

            removeSpi(key);
            enqueuePreferenceChangeEvent(key, null);
        }
    }

    /**
     * 按照 {@link Preferences#clear()} 中的规范实现 <tt>clear</tt> 方法。
     *
     * <p>此实现获取此首选项节点的锁，调用 {@link #keys()} 以获取键数组，并迭代数组对每个键调用 {@link #remove(String)}。
     *
     * @throws BackingStoreException 如果由于后端存储故障或无法与其通信而无法完成此操作。
     * @throws IllegalStateException 如果此节点（或其祖先）已通过 {@link #removeNode()} 方法删除。
     */
    public void clear() throws BackingStoreException {
        synchronized(lock) {
            String[] keys = keys();
            for (int i=0; i<keys.length; i++)
                remove(keys[i]);
        }
    }

    /**
     * 按照 {@link Preferences#putInt(String,int)} 中的规范实现 <tt>putInt</tt> 方法。
     *
     * <p>此实现使用 {@link Integer#toString(int)} 将 <tt>value</tt> 转换为字符串，并对结果调用 {@link #put(String,String)}。
     *
     * @param key 要与值的字符串形式关联的键。
     * @param value 要与键关联的值的字符串形式。
     * @throws NullPointerException 如果 key 为 <tt>null</tt>。
     * @throws IllegalArgumentException 如果 <tt>key.length()</tt> 超过 <tt>MAX_KEY_LENGTH</tt>。
     * @throws IllegalStateException 如果此节点（或其祖先）已通过 {@link #removeNode()} 方法删除。
     */
    public void putInt(String key, int value) {
        put(key, Integer.toString(value));
    }


                /**
     * 按照 {@link Preferences#getInt(String,int)} 中的规范实现 <tt>getInt</tt> 方法。
     *
     * <p>此实现调用 {@link #get(String,String) <tt>get(key,
     * null)</tt>}。如果返回值非空，实现尝试使用 {@link Integer#parseInt(String)} 将其转换为 <tt>int</tt>。如果尝试成功，返回值由本方法返回。否则，返回 <tt>def</tt>。
     *
     * @param key 要返回其值（作为 int）的键。
     * @param def 如果此偏好节点没有与 <tt>key</tt> 关联的值，或者关联值无法解释为 int，则返回此值。
     * @return 与 <tt>key</tt> 关联的字符串在本偏好节点中表示的 int 值，或如果关联值不存在或无法解释为 int，则返回 <tt>def</tt>。
     * @throws IllegalStateException 如果此节点（或其祖先）已被 {@link #removeNode()} 方法移除。
     * @throws NullPointerException 如果 <tt>key</tt> 为 <tt>null</tt>。
     */
    public int getInt(String key, int def) {
        int result = def;
        try {
            String value = get(key, null);
            if (value != null)
                result = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            // 忽略异常会导致指定的默认值被返回
        }

        return result;
    }

    /**
     * 按照 {@link Preferences#putLong(String,long)} 中的规范实现 <tt>putLong</tt> 方法。
     *
     * <p>此实现使用 {@link Long#toString(long)} 将 <tt>value</tt> 转换为字符串，并调用 {@link #put(String,String)}。
     *
     * @param key 要与值的字符串形式关联的键。
     * @param value 要与键关联的值的字符串形式。
     * @throws NullPointerException 如果键为 <tt>null</tt>。
     * @throws IllegalArgumentException 如果 <tt>key.length()</tt> 超过 <tt>MAX_KEY_LENGTH</tt>。
     * @throws IllegalStateException 如果此节点（或其祖先）已被 {@link #removeNode()} 方法移除。
     */
    public void putLong(String key, long value) {
        put(key, Long.toString(value));
    }

    /**
     * 按照 {@link Preferences#getLong(String,long)} 中的规范实现 <tt>getLong</tt> 方法。
     *
     * <p>此实现调用 {@link #get(String,String) <tt>get(key,
     * null)</tt>}。如果返回值非空，实现尝试使用 {@link Long#parseLong(String)} 将其转换为 <tt>long</tt>。如果尝试成功，返回值由本方法返回。否则，返回 <tt>def</tt>。
     *
     * @param key 要返回其值（作为 long）的键。
     * @param def 如果此偏好节点没有与 <tt>key</tt> 关联的值，或者关联值无法解释为 long，则返回此值。
     * @return 与 <tt>key</tt> 关联的字符串在本偏好节点中表示的 long 值，或如果关联值不存在或无法解释为 long，则返回 <tt>def</tt>。
     * @throws IllegalStateException 如果此节点（或其祖先）已被 {@link #removeNode()} 方法移除。
     * @throws NullPointerException 如果 <tt>key</tt> 为 <tt>null</tt>。
     */
    public long getLong(String key, long def) {
        long result = def;
        try {
            String value = get(key, null);
            if (value != null)
                result = Long.parseLong(value);
        } catch (NumberFormatException e) {
            // 忽略异常会导致指定的默认值被返回
        }

        return result;
    }

    /**
     * 按照 {@link Preferences#putBoolean(String,boolean)} 中的规范实现 <tt>putBoolean</tt> 方法。
     *
     * <p>此实现使用 {@link String#valueOf(boolean)} 将 <tt>value</tt> 转换为字符串，并调用 {@link #put(String,String)}。
     *
     * @param key 要与值的字符串形式关联的键。
     * @param value 要与键关联的值的字符串形式。
     * @throws NullPointerException 如果键为 <tt>null</tt>。
     * @throws IllegalArgumentException 如果 <tt>key.length()</tt> 超过 <tt>MAX_KEY_LENGTH</tt>。
     * @throws IllegalStateException 如果此节点（或其祖先）已被 {@link #removeNode()} 方法移除。
     */
    public void putBoolean(String key, boolean value) {
        put(key, String.valueOf(value));
    }

    /**
     * 按照 {@link Preferences#getBoolean(String,boolean)} 中的规范实现 <tt>getBoolean</tt> 方法。
     *
     * <p>此实现调用 {@link #get(String,String) <tt>get(key,
     * null)</tt>}。如果返回值非空，使用 {@link String#equalsIgnoreCase(String)} 与 <tt>"true"</tt> 进行比较。如果比较返回 <tt>true</tt>，此调用返回 <tt>true</tt>。否则，返回值与 <tt>"false"</tt> 进行比较，再次使用 {@link String#equalsIgnoreCase(String)}。如果比较返回 <tt>true</tt>，此调用返回 <tt>false</tt>。否则，此调用返回 <tt>def</tt>。
     *
     * @param key 要返回其值（作为 boolean）的键。
     * @param def 如果此偏好节点没有与 <tt>key</tt> 关联的值，或者关联值无法解释为 boolean，则返回此值。
     * @return 与 <tt>key</tt> 关联的字符串在本偏好节点中表示的 boolean 值，或如果关联值不存在或无法解释为 boolean，则返回 <tt>def</tt>。
     * @throws IllegalStateException 如果此节点（或其祖先）已被 {@link #removeNode()} 方法移除。
     * @throws NullPointerException 如果 <tt>key</tt> 为 <tt>null</tt>。
     */
    public boolean getBoolean(String key, boolean def) {
        boolean result = def;
        String value = get(key, null);
        if (value != null) {
            if (value.equalsIgnoreCase("true"))
                result = true;
            else if (value.equalsIgnoreCase("false"))
                result = false;
        }

        return result;
    }

    /**
     * 按照 {@link Preferences#putFloat(String,float)} 中的规范实现 <tt>putFloat</tt> 方法。
     *
     * <p>此实现使用 {@link Float#toString(float)} 将 <tt>value</tt> 转换为字符串，并调用 {@link #put(String,String)}。
     *
     * @param key 要与值的字符串形式关联的键。
     * @param value 要与键关联的值的字符串形式。
     * @throws NullPointerException 如果键为 <tt>null</tt>。
     * @throws IllegalArgumentException 如果 <tt>key.length()</tt> 超过 <tt>MAX_KEY_LENGTH</tt>。
     * @throws IllegalStateException 如果此节点（或其祖先）已被 {@link #removeNode()} 方法移除。
     */
    public void putFloat(String key, float value) {
        put(key, Float.toString(value));
    }

    /**
     * 按照 {@link Preferences#getFloat(String,float)} 中的规范实现 <tt>getFloat</tt> 方法。
     *
     * <p>此实现调用 {@link #get(String,String) <tt>get(key,
     * null)</tt>}。如果返回值非空，实现尝试使用 {@link Float#parseFloat(String)} 将其转换为 <tt>float</tt>。如果尝试成功，返回值由本方法返回。否则，返回 <tt>def</tt>。
     *
     * @param key 要返回其值（作为 float）的键。
     * @param def 如果此偏好节点没有与 <tt>key</tt> 关联的值，或者关联值无法解释为 float，则返回此值。
     * @return 与 <tt>key</tt> 关联的字符串在本偏好节点中表示的 float 值，或如果关联值不存在或无法解释为 float，则返回 <tt>def</tt>。
     * @throws IllegalStateException 如果此节点（或其祖先）已被 {@link #removeNode()} 方法移除。
     * @throws NullPointerException 如果 <tt>key</tt> 为 <tt>null</tt>。
     */
    public float getFloat(String key, float def) {
        float result = def;
        try {
            String value = get(key, null);
            if (value != null)
                result = Float.parseFloat(value);
        } catch (NumberFormatException e) {
            // 忽略异常会导致指定的默认值被返回
        }

        return result;
    }

    /**
     * 按照 {@link Preferences#putDouble(String,double)} 中的规范实现 <tt>putDouble</tt> 方法。
     *
     * <p>此实现使用 {@link Double#toString(double)} 将 <tt>value</tt> 转换为字符串，并调用 {@link #put(String,String)}。
     *
     * @param key 要与值的字符串形式关联的键。
     * @param value 要与键关联的值的字符串形式。
     * @throws NullPointerException 如果键为 <tt>null</tt>。
     * @throws IllegalArgumentException 如果 <tt>key.length()</tt> 超过 <tt>MAX_KEY_LENGTH</tt>。
     * @throws IllegalStateException 如果此节点（或其祖先）已被 {@link #removeNode()} 方法移除。
     */
    public void putDouble(String key, double value) {
        put(key, Double.toString(value));
    }

    /**
     * 按照 {@link Preferences#getDouble(String,double)} 中的规范实现 <tt>getDouble</tt> 方法。
     *
     * <p>此实现调用 {@link #get(String,String) <tt>get(key,
     * null)</tt>}。如果返回值非空，实现尝试使用 {@link Double#parseDouble(String)} 将其转换为 <tt>double</tt>。如果尝试成功，返回值由本方法返回。否则，返回 <tt>def</tt>。
     *
     * @param key 要返回其值（作为 double）的键。
     * @param def 如果此偏好节点没有与 <tt>key</tt> 关联的值，或者关联值无法解释为 double，则返回此值。
     * @return 与 <tt>key</tt> 关联的字符串在本偏好节点中表示的 double 值，或如果关联值不存在或无法解释为 double，则返回 <tt>def</tt>。
     * @throws IllegalStateException 如果此节点（或其祖先）已被 {@link #removeNode()} 方法移除。
     * @throws NullPointerException 如果 <tt>key</tt> 为 <tt>null</tt>。
     */
    public double getDouble(String key, double def) {
        double result = def;
        try {
            String value = get(key, null);
            if (value != null)
                result = Double.parseDouble(value);
        } catch (NumberFormatException e) {
            // 忽略异常会导致指定的默认值被返回
        }

        return result;
    }

    /**
     * 按照 {@link Preferences#putByteArray(String,byte[])} 中的规范实现 <tt>putByteArray</tt> 方法。
     *
     * @param key 要与值的字符串形式关联的键。
     * @param value 要与键关联的值的字符串形式。
     * @throws NullPointerException 如果键或值为 <tt>null</tt>。
     * @throws IllegalArgumentException 如果 key.length() 超过 MAX_KEY_LENGTH
     *         或者 value.length 超过 MAX_VALUE_LENGTH*3/4。
     * @throws IllegalStateException 如果此节点（或其祖先）已被 {@link #removeNode()} 方法移除。
     */
    public void putByteArray(String key, byte[] value) {
        put(key, Base64.byteArrayToBase64(value));
    }

    /**
     * 按照 {@link Preferences#getByteArray(String,byte[])} 中的规范实现 <tt>getByteArray</tt> 方法。
     *
     * @param key 要返回其值（作为 byte 数组）的键。
     * @param def 如果此偏好节点没有与 <tt>key</tt> 关联的值，或者关联值无法解释为 byte 数组，则返回此值。
     * @return 与 <tt>key</tt> 关联的字符串在本偏好节点中表示的 byte 数组值，或如果关联值不存在或无法解释为 byte 数组，则返回 <tt>def</tt>。
     * @throws IllegalStateException 如果此节点（或其祖先）已被 {@link #removeNode()} 方法移除。
     * @throws NullPointerException 如果 <tt>key</tt> 为 <tt>null</tt>。允许 <tt>def</tt> 为 <tt>null</tt>。
     */
    public byte[] getByteArray(String key, byte[] def) {
        byte[] result = def;
        String value = get(key, null);
        try {
            if (value != null)
                result = Base64.base64ToByteArray(value);
        }
        catch (RuntimeException e) {
            // 忽略异常会导致指定的默认值被返回
        }

        return result;
    }

    /**
     * 按照 {@link Preferences#keys()} 中的规范实现 <tt>keys</tt> 方法。
     *
     * <p>此实现获取此偏好节点的锁，检查节点是否已被移除，并调用 {@link #keysSpi()}。
     *
     * @return 与此偏好节点关联的值的键数组。
     * @throws BackingStoreException 如果由于后端存储故障或无法与后端存储通信而无法完成此操作。
     * @throws IllegalStateException 如果此节点（或其祖先）已被 {@link #removeNode()} 方法移除。
     */
    public String[] keys() throws BackingStoreException {
        synchronized(lock) {
            if (removed)
                throw new IllegalStateException("Node has been removed.");

            return keysSpi();
        }
    }

    /**
     * 按照 {@link Preferences#childrenNames()} 中的规范实现 <tt>children</tt> 方法。
     *
     * <p>此实现获取此偏好节点的锁，检查节点是否已被移除，构造一个初始化为已缓存的子节点名称（此节点的“子节点缓存”中的子节点）的 <tt>TreeSet</tt>，调用 {@link #childrenNamesSpi()}，并将所有返回的子节点名称添加到集合中。使用 <tt>toArray</tt> 方法将集合中的元素转储到 <tt>String</tt> 数组中，并返回此数组。
     *
     * @return 此偏好节点的子节点名称。
     * @throws BackingStoreException 如果由于后端存储故障或无法与后端存储通信而无法完成此操作。
     * @throws IllegalStateException 如果此节点（或其祖先）已被 {@link #removeNode()} 方法移除。
     * @see #cachedChildren()
     */
    public String[] childrenNames() throws BackingStoreException {
        synchronized(lock) {
            if (removed)
                throw new IllegalStateException("Node has been removed.");


                        Set<String> s = new TreeSet<>(kidCache.keySet());
            for (String kid : childrenNamesSpi())
                s.add(kid);
            return s.toArray(EMPTY_STRING_ARRAY);
        }
    }

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    /**
     * 返回此节点的所有已知未删除的子节点。
     *
     * @return 此节点的所有已知未删除的子节点。
     */
    protected final AbstractPreferences[] cachedChildren() {
        return kidCache.values().toArray(EMPTY_ABSTRACT_PREFS_ARRAY);
    }

    private static final AbstractPreferences[] EMPTY_ABSTRACT_PREFS_ARRAY
        = new AbstractPreferences[0];

    /**
     * 按照 {@link Preferences#parent()} 中的规范实现 <tt>parent</tt> 方法。
     *
     * <p>此实现获取此首选项节点的锁，检查该节点是否未被删除，并返回传递给此节点构造函数的父节点值。
     *
     * @return 此首选项节点的父节点。
     * @throws IllegalStateException 如果此节点（或其祖先）已被 {@link #removeNode()} 方法删除。
     */
    public Preferences parent() {
        synchronized(lock) {
            if (removed)
                throw new IllegalStateException("节点已被删除。");

            return parent;
        }
    }

    /**
     * 按照 {@link Preferences#node(String)} 中的规范实现 <tt>node</tt> 方法。
     *
     * <p>此实现获取此首选项节点的锁并检查该节点是否未被删除。如果 <tt>path</tt> 为 <tt>""</tt>，
     * 则返回此节点；如果 <tt>path</tt> 为 <tt>"/"</tt>，则返回此节点的根。如果 <tt>path</tt>
     * 的第一个字符不是 <tt>'/'</tt>，则将 <tt>path</tt> 分解为标记，并从该节点递归遍历到命名节点，
     * 每次遍历时“消耗”一个名称和一个斜杠。在每次遍历时，当前节点被锁定，并检查节点的子节点缓存中是否包含命名节点。
     * 如果未找到，则检查名称的长度是否不超过 <tt>MAX_NAME_LENGTH</tt>。然后调用 {@link #childSpi(String)}
     * 方法，并将结果存储在此节点的子节点缓存中。如果新创建的 <tt>Preferences</tt> 对象的 {@link #newNode}
     * 字段为 <tt>true</tt> 且存在任何节点更改监听器，则将通知事件排队，由事件分发线程处理。
     *
     * <p>当没有更多标记时，返回在子节点缓存中找到的最后一个值或由 <tt>childSpi</tt> 返回的值。如果在遍历过程中，
     * 出现两个连续的 <tt>"/"</tt> 标记，或最终标记为 <tt>"/"</tt>（而不是名称），则抛出适当的 <tt>IllegalArgumentException</tt>。
     *
     * <p>如果 <tt>path</tt> 的第一个字符为 <tt>'/'</tt>（表示绝对路径名），则在将 <tt>path</tt> 分解为标记之前，
     * 释放此首选项节点的锁，并从根节点开始递归遍历（而不是从该节点开始）。遍历过程与相对路径名的遍历过程相同。
     * 在从根节点开始遍历之前释放此节点的锁，以避免死锁，如 {@link #lock 锁定不变量} 所述。
     *
     * @param path 要返回的首选项节点的路径名。
     * @return 指定的首选项节点。
     * @throws IllegalArgumentException 如果路径名无效（即，包含多个连续的斜杠字符，或以斜杠字符结尾且长度超过一个字符）。
     * @throws IllegalStateException 如果此节点（或其祖先）已被 {@link #removeNode()} 方法删除。
     */
    public Preferences node(String path) {
        synchronized(lock) {
            if (removed)
                throw new IllegalStateException("节点已被删除。");
            if (path.equals(""))
                return this;
            if (path.equals("/"))
                return root;
            if (path.charAt(0) != '/')
                return node(new StringTokenizer(path, "/", true));
        }

        // 绝对路径。注意我们已释放锁以避免死锁
        return root.node(new StringTokenizer(path.substring(1), "/", true));
    }

    /**
     * tokenizer 包含 <name> {'/' <name>}*
     */
    private Preferences node(StringTokenizer path) {
        String token = path.nextToken();
        if (token.equals("/"))  // 检查连续的斜杠
            throw new IllegalArgumentException("路径中存在连续的斜杠");
        synchronized(lock) {
            AbstractPreferences child = kidCache.get(token);
            if (child == null) {
                if (token.length() > MAX_NAME_LENGTH)
                    throw new IllegalArgumentException(
                        "节点名称 " + token + " 太长");
                child = childSpi(token);
                if (child.newNode)
                    enqueueNodeAddedEvent(child);
                kidCache.put(token, child);
            }
            if (!path.hasMoreTokens())
                return child;
            path.nextToken();  // 消耗斜杠
            if (!path.hasMoreTokens())
                throw new IllegalArgumentException("路径以斜杠结尾");
            return child.node(path);
        }
    }

    /**
     * 按照 {@link Preferences#nodeExists(String)} 中的规范实现 <tt>nodeExists</tt> 方法。
     *
     * <p>此实现与 {@link #node(String)} 非常相似，只是使用 {@link #getChild(String)} 而不是 {@link
     * #childSpi(String)}。
     *
     * @param path 要检查其存在的节点的路径名。
     * @return 如果指定的节点存在，则返回 true。
     * @throws BackingStoreException 如果由于后端存储故障或无法与其通信而无法完成此操作。
     * @throws IllegalArgumentException 如果路径名无效（即，包含多个连续的斜杠字符，或以斜杠字符结尾且长度超过一个字符）。
     * @throws IllegalStateException 如果此节点（或其祖先）已被 {@link #removeNode()} 方法删除且 <tt>pathname</tt> 不是空字符串 (<tt>""</tt>)。
     */
    public boolean nodeExists(String path)
        throws BackingStoreException
    {
        synchronized(lock) {
            if (path.equals(""))
                return !removed;
            if (removed)
                throw new IllegalStateException("节点已被删除。");
            if (path.equals("/"))
                return true;
            if (path.charAt(0) != '/')
                return nodeExists(new StringTokenizer(path, "/", true));
        }

        // 绝对路径。注意我们已释放锁以避免死锁
        return root.nodeExists(new StringTokenizer(path.substring(1), "/",
                                                   true));
    }

    /**
     * tokenizer 包含 <name> {'/' <name>}*
     */
    private boolean nodeExists(StringTokenizer path)
        throws BackingStoreException
    {
        String token = path.nextToken();
        if (token.equals("/"))  // 检查连续的斜杠
            throw new IllegalArgumentException("路径中存在连续的斜杠");
        synchronized(lock) {
            AbstractPreferences child = kidCache.get(token);
            if (child == null)
                child = getChild(token);
            if (child==null)
                return false;
            if (!path.hasMoreTokens())
                return true;
            path.nextToken();  // 消耗斜杠
            if (!path.hasMoreTokens())
                throw new IllegalArgumentException("路径以斜杠结尾");
            return child.nodeExists(path);
        }
    }

    /**
     * 按照 {@link Preferences#removeNode()} 中的规范实现 <tt>removeNode()</tt> 方法。
     *
     * <p>此实现检查此节点是否为根节点；如果是，则抛出适当的异常。然后，锁定此节点的父节点，并调用一个递归辅助方法，
     * 该方法遍历以此节点为根的子树。递归方法锁定调用它的节点，检查该节点是否已被删除，然后确保其所有子节点都已缓存：
     * 调用 {@link #childrenNamesSpi()} 方法并检查返回的每个子节点名称是否包含在子节点缓存中。如果子节点尚未缓存，
     * 则调用 {@link #childSpi(String)} 方法为其创建一个 <tt>Preferences</tt> 实例，并将该实例放入子节点缓存中。
     * 然后，辅助方法递归调用自身，遍历其子节点缓存中的每个节点。接下来，调用 {@link #removeNodeSpi()}，
     * 标记自身为已删除，并从其父节点的子节点缓存中删除自身。最后，如果有任何节点更改监听器，则将通知事件排队，
     * 由事件分发线程处理。
     *
     * <p>注意，辅助方法总是以所有祖先节点（直到“最近的未删除祖先”）锁定的状态被调用。
     *
     * @throws IllegalStateException 如果此节点（或其祖先）已被 {@link #removeNode()} 方法删除。
     * @throws UnsupportedOperationException 如果此方法在根节点上调用。
     * @throws BackingStoreException 如果由于后端存储故障或无法与其通信而无法完成此操作。
     */
    public void removeNode() throws BackingStoreException {
        if (this==root)
            throw new UnsupportedOperationException("不能删除根节点！");
        synchronized(parent.lock) {
            removeNode2();
            parent.kidCache.remove(name);
        }
    }

    /*
     * 调用时，从“删除根”的父节点到此节点（包括前者但不包括后者）的所有节点都已锁定。
     */
    private void removeNode2() throws BackingStoreException {
        synchronized(lock) {
            if (removed)
                throw new IllegalStateException("节点已被删除。");

            // 确保所有子节点都已缓存
            String[] kidNames = childrenNamesSpi();
            for (int i=0; i<kidNames.length; i++)
                if (!kidCache.containsKey(kidNames[i]))
                    kidCache.put(kidNames[i], childSpi(kidNames[i]));

            // 递归删除所有缓存的子节点
            for (Iterator<AbstractPreferences> i = kidCache.values().iterator();
                 i.hasNext();) {
                try {
                    i.next().removeNode2();
                    i.remove();
                } catch (BackingStoreException x) { }
            }

            // 现在没有后代了 - 是时候消失了！
            removeNodeSpi();
            removed = true;
            parent.enqueueNodeRemovedEvent(this);
        }
    }

    /**
     * 按照 {@link Preferences#name()} 中的规范实现 <tt>name</tt> 方法。
     *
     * <p>此实现仅返回传递给此节点构造函数的名称。
     *
     * @return 相对于其父节点的此首选项节点的名称。
     */
    public String name() {
        return name;
    }

    /**
     * 按照 {@link Preferences#absolutePath()} 中的规范实现 <tt>absolutePath</tt> 方法。
     *
     * <p>此实现仅返回在构造此节点时计算的绝对路径名（基于传递给此节点构造函数的名称以及传递给此节点祖先构造函数的名称）。
     *
     * @return 此首选项节点的绝对路径名。
     */
    public String absolutePath() {
        return absolutePath;
    }

    /**
     * 按照 {@link Preferences#isUserNode()} 中的规范实现 <tt>isUserNode</tt> 方法。
     *
     * <p>此实现将此节点的根节点（存储在私有字段中）与 {@link Preferences#userRoot()} 返回的值进行比较。
     * 如果两个对象引用相同，则此方法返回 true。
     *
     * @return <tt>true</tt> 如果此首选项节点在用户首选项树中，<tt>false</tt> 如果它在系统首选项树中。
     */
    public boolean isUserNode() {
        return AccessController.doPrivileged(
            new PrivilegedAction<Boolean>() {
                public Boolean run() {
                    return root == Preferences.userRoot();
            }
            }).booleanValue();
    }

    public void addPreferenceChangeListener(PreferenceChangeListener pcl) {
        if (pcl==null)
            throw new NullPointerException("更改监听器为空。");
        synchronized(lock) {
            if (removed)
                throw new IllegalStateException("节点已被删除。");

            // 复制-写入
            PreferenceChangeListener[] old = prefListeners;
            prefListeners = new PreferenceChangeListener[old.length + 1];
            System.arraycopy(old, 0, prefListeners, 0, old.length);
            prefListeners[old.length] = pcl;
        }
        startEventDispatchThreadIfNecessary();
    }

    public void removePreferenceChangeListener(PreferenceChangeListener pcl) {
        synchronized(lock) {
            if (removed)
                throw new IllegalStateException("节点已被删除。");
            if ((prefListeners == null) || (prefListeners.length == 0))
                throw new IllegalArgumentException("监听器未注册。");

            // 复制-写入
            PreferenceChangeListener[] newPl =
                new PreferenceChangeListener[prefListeners.length - 1];
            int i = 0;
            while (i < newPl.length && prefListeners[i] != pcl)
                newPl[i] = prefListeners[i++];

            if (i == newPl.length &&  prefListeners[i] != pcl)
                throw new IllegalArgumentException("监听器未注册。");
            while (i < newPl.length)
                newPl[i] = prefListeners[++i];
            prefListeners = newPl;
        }
    }

    public void addNodeChangeListener(NodeChangeListener ncl) {
        if (ncl==null)
            throw new NullPointerException("更改监听器为空。");
        synchronized(lock) {
            if (removed)
                throw new IllegalStateException("节点已被删除。");

            // 复制-写入
            if (nodeListeners == null) {
                nodeListeners = new NodeChangeListener[1];
                nodeListeners[0] = ncl;
            } else {
                NodeChangeListener[] old = nodeListeners;
                nodeListeners = new NodeChangeListener[old.length + 1];
                System.arraycopy(old, 0, nodeListeners, 0, old.length);
                nodeListeners[old.length] = ncl;
            }
        }
        startEventDispatchThreadIfNecessary();
    }


                public void removeNodeChangeListener(NodeChangeListener ncl) {
        synchronized(lock) {
            if (removed)
                throw new IllegalStateException("节点已被移除。");
            if ((nodeListeners == null) || (nodeListeners.length == 0))
                throw new IllegalArgumentException("监听器未注册。");

            // 复制-写入
            int i = 0;
            while (i < nodeListeners.length && nodeListeners[i] != ncl)
                i++;
            if (i == nodeListeners.length)
                throw new IllegalArgumentException("监听器未注册。");
            NodeChangeListener[] newNl =
                new NodeChangeListener[nodeListeners.length - 1];
            if (i != 0)
                System.arraycopy(nodeListeners, 0, newNl, 0, i);
            if (i != newNl.length)
                System.arraycopy(nodeListeners, i + 1,
                                 newNl, i, newNl.length - i);
            nodeListeners = newNl;
        }
    }

    // "SPI" 方法

    /**
     * 将给定的键值对放入此首选项节点。保证 <tt>key</tt> 和 <tt>value</tt> 非空且长度合法。此外，保证此节点未被移除。（实现者无需检查这些条件。）
     *
     * <p>此方法在持有此节点锁的情况下被调用。
     * @param key 键
     * @param value 值
     */
    protected abstract void putSpi(String key, String value);

    /**
     * 返回此首选项节点中指定键关联的值，如果没有此键的关联，或当前无法确定关联，则返回 <tt>null</tt>。保证 <tt>key</tt> 非空。此外，保证此节点未被移除。（实现者无需检查这些条件。）
     *
     * <p>通常情况下，此方法不应在任何情况下抛出异常。然而，如果确实抛出异常，该异常将被捕获并视为 <tt>null</tt> 返回值。
     *
     * <p>此方法在持有此节点锁的情况下被调用。
     *
     * @param key 键
     * @return 此首选项节点中指定键关联的值，如果没有此键的关联，或当前无法确定关联，则返回 <tt>null</tt>。
     */
    protected abstract String getSpi(String key);

    /**
     * 移除此首选项节点中指定键的关联（如果存在）。保证 <tt>key</tt> 非空。此外，保证此节点未被移除。（实现者无需检查这些条件。）
     *
     * <p>此方法在持有此节点锁的情况下被调用。
     * @param key 键
     */
    protected abstract void removeSpi(String key);

    /**
     * 移除此首选项节点，使其失效并移除其中的所有偏好设置。调用此方法时，命名的子节点将没有后代（即，{@link Preferences#removeNode()} 方法会反复调用此方法，自底向上地移除每个节点的后代，然后再移除该节点本身）。
     *
     * <p>此方法在持有此节点及其父节点（以及因单次调用 {@link Preferences#removeNode()} 而移除的所有祖先节点）的锁的情况下被调用。
     *
     * <p>节点的移除无需在调用 <tt>flush</tt> 方法之前持久化。
     *
     * <p>如果此节点抛出 <tt>BackingStoreException</tt>，该异常将传播到外部的 {@link #removeNode()} 调用。
     *
     * @throws BackingStoreException 如果由于后端存储失败或无法与其通信而导致此操作无法完成。
     */
    protected abstract void removeNodeSpi() throws BackingStoreException;

    /**
     * 返回此首选项节点中所有具有关联值的键。（如果此节点没有偏好设置，返回的数组大小为零。）保证此节点未被移除。
     *
     * <p>此方法在持有此节点锁的情况下被调用。
     *
     * <p>如果此节点抛出 <tt>BackingStoreException</tt>，该异常将传播到外部的 {@link #keys()} 调用。
     *
     * @return 此首选项节点中所有具有关联值的键的数组。
     * @throws BackingStoreException 如果由于后端存储失败或无法与其通信而导致此操作无法完成。
     */
    protected abstract String[] keysSpi() throws BackingStoreException;

    /**
     * 返回此首选项节点的子节点名称。（如果此节点没有子节点，返回的数组大小为零。）此方法无需返回任何已缓存的节点名称，但这样做也不会造成伤害。
     *
     * <p>此方法在持有此节点锁的情况下被调用。
     *
     * <p>如果此节点抛出 <tt>BackingStoreException</tt>，该异常将传播到外部的 {@link #childrenNames()} 调用。
     *
     * @return 此首选项节点的子节点名称的数组。
     * @throws BackingStoreException 如果由于后端存储失败或无法与其通信而导致此操作无法完成。
     */
    protected abstract String[] childrenNamesSpi()
        throws BackingStoreException;

    /**
     * 如果存在，则返回命名的子节点，否则返回 <tt>null</tt>。保证 <tt>nodeName</tt> 非空、非空字符串，不包含斜杠字符 ('/')，且长度不超过 {@link #MAX_NAME_LENGTH} 个字符。此外，保证此节点未被移除。（如果实现者选择重写此方法，则无需检查这些条件。）
     *
     * <p>最后，保证命名的节点在上次移除后未被之前的调用返回。换句话说，总是优先使用缓存的值而不是调用此方法。（如果实现者选择重写此方法，则无需维护之前返回的子节点的缓存。）
     *
     * <p>此实现获取此首选项节点的锁，调用 {@link #childrenNames()} 获取此节点的子节点名称数组，并迭代数组，将每个子节点的名称与指定的节点名称进行比较。如果找到具有正确名称的子节点，则调用 {@link #childSpi(String)} 方法并返回结果节点。如果迭代完成而未找到指定的名称，则返回 <tt>null</tt>。
     *
     * @param nodeName 要搜索的子节点的名称。
     * @return 如果存在，则返回命名的子节点，否则返回 <tt>null</tt>。
     * @throws BackingStoreException 如果由于后端存储失败或无法与其通信而导致此操作无法完成。
     */
    protected AbstractPreferences getChild(String nodeName)
            throws BackingStoreException {
        synchronized(lock) {
            // assert kidCache.get(nodeName)==null;
            String[] kidNames = childrenNames();
            for (int i=0; i<kidNames.length; i++)
                if (kidNames[i].equals(nodeName))
                    return childSpi(kidNames[i]);
        }
        return null;
    }

    /**
     * 返回此首选项节点的命名子节点，如果该子节点尚不存在，则创建它。保证 <tt>name</tt> 非空、非空字符串，不包含斜杠字符 ('/')，且长度不超过 {@link #MAX_NAME_LENGTH} 个字符。此外，保证此节点未被移除。（实现者无需检查这些条件。）
     *
     * <p>最后，保证命名的节点在上次移除后未被之前的调用返回。换句话说，总是优先使用缓存的值而不是调用此方法。子类无需维护之前返回的子节点的缓存。
     *
     * <p>实现者必须确保返回的节点未被移除。如果此节点的同名子节点之前已被移除，实现者必须返回一个新构造的 <tt>AbstractPreferences</tt> 节点；一旦移除，<tt>AbstractPreferences</tt> 节点无法“复活”。
     *
     * <p>如果此方法导致创建节点，则该节点在调用 <tt>flush</tt> 方法之前不保证持久化。
     *
     * <p>此方法在持有此节点锁的情况下被调用。
     *
     * @param name 相对于此首选项节点的子节点的名称。
     * @return 命名的子节点。
     */
    protected abstract AbstractPreferences childSpi(String name);

    /**
     * 返回此偏好设置节点的绝对路径名称。
     */
    public String toString() {
        return (this.isUserNode() ? "用户" : "系统") +
               " 偏好设置节点: " + this.absolutePath();
    }

    /**
     * 按照 {@link Preferences#sync()} 中的规范实现 <tt>sync</tt> 方法。
     *
     * <p>此实现调用一个递归辅助方法，该方法锁定此节点，调用 syncSpi()，解锁此节点，并递归调用此方法的每个“缓存子节点”。缓存子节点是指在此 VM 中创建且未被移除的此节点的子节点。实际上，此方法对以该节点为根的“缓存子树”进行深度优先遍历，调用每个子树节点的 syncSpi()，同时仅锁定该节点。注意 syncSpi() 是自顶向下调用的。
     *
     * @throws BackingStoreException 如果由于后端存储失败或无法与其通信而导致此操作无法完成。
     * @throws IllegalStateException 如果此节点（或其祖先）已使用 {@link #removeNode()} 方法移除。
     * @see #flush()
     */
    public void sync() throws BackingStoreException {
        sync2();
    }

    private void sync2() throws BackingStoreException {
        AbstractPreferences[] cachedKids;

        synchronized(lock) {
            if (removed)
                throw new IllegalStateException("节点已被移除");
            syncSpi();
            cachedKids = cachedChildren();
        }

        for (int i=0; i<cachedKids.length; i++)
            cachedKids[i].sync2();
    }

    /**
     * 此方法在锁定此节点的情况下被调用。此方法的合同是将此节点中缓存的偏好设置与后端存储中的偏好设置同步。（此节点可能在后端存储中不存在，因为可能已被其他 VM 删除，或者尚未创建。）注意，此方法不应同步此节点的任何子节点中的偏好设置。如果后端存储自然地一次同步整个子树，建议实现者重写 sync()，而不仅仅是重写此方法。
     *
     * <p>如果此节点抛出 <tt>BackingStoreException</tt>，该异常将传播到外部的 {@link #sync()} 调用。
     *
     * @throws BackingStoreException 如果由于后端存储失败或无法与其通信而导致此操作无法完成。
     */
    protected abstract void syncSpi() throws BackingStoreException;

    /**
     * 按照 {@link Preferences#flush()} 中的规范实现 <tt>flush</tt> 方法。
     *
     * <p>此实现调用一个递归辅助方法，该方法锁定此节点，调用 flushSpi()，解锁此节点，并递归调用此方法的每个“缓存子节点”。缓存子节点是指在此 VM 中创建且未被移除的此节点的子节点。实际上，此方法对以该节点为根的“缓存子树”进行深度优先遍历，调用每个子树节点的 flushSpi()，同时仅锁定该节点。注意 flushSpi() 是自顶向下调用的。
     *
     * <p>如果此方法在已使用 {@link #removeNode()} 方法移除的节点上调用，则调用此节点的 flushSpi()，但不调用其他节点。
     *
     * @throws BackingStoreException 如果由于后端存储失败或无法与其通信而导致此操作无法完成。
     * @see #flush()
     */
    public void flush() throws BackingStoreException {
        flush2();
    }

    private void flush2() throws BackingStoreException {
        AbstractPreferences[] cachedKids;

        synchronized(lock) {
            flushSpi();
            if(removed)
                return;
            cachedKids = cachedChildren();
        }

        for (int i = 0; i < cachedKids.length; i++)
            cachedKids[i].flush2();
    }

    /**
     * 此方法在锁定此节点的情况下被调用。此方法的合同是将此偏好设置节点中缓存的更改强制写入后端存储，保证其持久性。（此节点可能在后端存储中不存在，因为可能已被其他 VM 删除，或者尚未创建。）注意，此方法不应刷新此节点的任何子节点中的偏好设置。如果后端存储自然地一次刷新整个子树，建议实现者重写 flush()，而不仅仅是重写此方法。
     *
     * <p>如果此节点抛出 <tt>BackingStoreException</tt>，该异常将传播到外部的 {@link #flush()} 调用。
     *
     * @throws BackingStoreException 如果由于后端存储失败或无法与其通信而导致此操作无法完成。
     */
    protected abstract void flushSpi() throws BackingStoreException;

    /**
     * 如果此节点（或其祖先）已使用 {@link #removeNode()} 方法移除，则返回 <tt>true</tt>。此方法在返回用于跟踪此状态的私有字段内容之前锁定此节点。
     *
     * @return 如果此节点（或其祖先）已使用 {@link #removeNode()} 方法移除，则返回 <tt>true</tt>。
     */
    protected boolean isRemoved() {
        synchronized(lock) {
            return removed;
        }
    }


                /**
     * 待处理通知事件的队列。当发生一个有监听器的偏好设置或节点更改事件时，
     * 该事件会被放入此队列，并通知队列。后台线程等待此队列并传递事件。
     * 这样可以将事件传递与偏好设置活动解耦，大大简化锁定并减少死锁的机会。
     */
    private static final List<EventObject> eventQueue = new LinkedList<>();

    /**
     * 这两个类用于区分 eventQueue 中的 NodeChangeEvents，以便事件分发线程知道是调用
     * childAdded 还是 childRemoved。
     */
    private class NodeAddedEvent extends NodeChangeEvent {
        private static final long serialVersionUID = -6743557530157328528L;
        NodeAddedEvent(Preferences parent, Preferences child) {
            super(parent, child);
        }
    }
    private class NodeRemovedEvent extends NodeChangeEvent {
        private static final long serialVersionUID = 8735497392918824837L;
        NodeRemovedEvent(Preferences parent, Preferences child) {
            super(parent, child);
        }
    }

    /**
     * 单个后台线程（“事件通知线程”）监控事件队列并传递队列中的事件。
     */
    private static class EventDispatchThread extends Thread {
        public void run() {
            while(true) {
                // 等待 eventQueue 直到有事件
                EventObject event = null;
                synchronized(eventQueue) {
                    try {
                        while (eventQueue.isEmpty())
                            eventQueue.wait();
                        event = eventQueue.remove(0);
                    } catch (InterruptedException e) {
                        // XXX 记录“事件分发线程被中断。退出”
                        return;
                    }
                }

                // 现在我们有事件且不持有任何锁；向监听器传递事件
                AbstractPreferences src=(AbstractPreferences)event.getSource();
                if (event instanceof PreferenceChangeEvent) {
                    PreferenceChangeEvent pce = (PreferenceChangeEvent)event;
                    PreferenceChangeListener[] listeners = src.prefListeners();
                    for (int i=0; i<listeners.length; i++)
                        listeners[i].preferenceChange(pce);
                } else {
                    NodeChangeEvent nce = (NodeChangeEvent)event;
                    NodeChangeListener[] listeners = src.nodeListeners();
                    if (nce instanceof NodeAddedEvent) {
                        for (int i=0; i<listeners.length; i++)
                            listeners[i].childAdded(nce);
                    } else {
                        // assert nce instanceof NodeRemovedEvent;
                        for (int i=0; i<listeners.length; i++)
                            listeners[i].childRemoved(nce);
                    }
                }
            }
        }
    }

    private static Thread eventDispatchThread = null;

    /**
     * 当首次调用此方法时启动事件分发线程。只有当有人注册监听器时，才会启动事件分发线程。
     */
    private static synchronized void startEventDispatchThreadIfNecessary() {
        if (eventDispatchThread == null) {
            // XXX 记录“启动事件分发线程”
            eventDispatchThread = new EventDispatchThread();
            eventDispatchThread.setDaemon(true);
            eventDispatchThread.start();
        }
    }

    /**
     * 返回此节点的偏好设置/节点更改监听器。即使我们使用的是写时复制列表，我们仍使用同步访问器
     * 以确保从写线程到读线程的信息传输。
     */
    PreferenceChangeListener[] prefListeners() {
        synchronized(lock) {
            return prefListeners;
        }
    }
    NodeChangeListener[] nodeListeners() {
        synchronized(lock) {
            return nodeListeners;
        }
    }

    /**
     * 将偏好设置更改事件放入队列，以传递给已注册的偏好设置更改监听器，除非没有已注册的监听器。
     * 在持有 this.lock 时调用。
     */
    private void enqueuePreferenceChangeEvent(String key, String newValue) {
        if (prefListeners.length != 0) {
            synchronized(eventQueue) {
                eventQueue.add(new PreferenceChangeEvent(this, key, newValue));
                eventQueue.notify();
            }
        }
    }

    /**
     * 将“节点添加”事件放入队列，以传递给已注册的节点更改监听器，除非没有已注册的监听器。
     * 在持有 this.lock 时调用。
     */
    private void enqueueNodeAddedEvent(Preferences child) {
        if (nodeListeners.length != 0) {
            synchronized(eventQueue) {
                eventQueue.add(new NodeAddedEvent(this, child));
                eventQueue.notify();
            }
        }
    }

    /**
     * 将“节点移除”事件放入队列，以传递给已注册的节点更改监听器，除非没有已注册的监听器。
     * 在持有 this.lock 时调用。
     */
    private void enqueueNodeRemovedEvent(Preferences child) {
        if (nodeListeners.length != 0) {
            synchronized(eventQueue) {
                eventQueue.add(new NodeRemovedEvent(this, child));
                eventQueue.notify();
            }
        }
    }

    /**
     * 根据 {@link Preferences#exportNode(OutputStream)} 中的规范实现 <tt>exportNode</tt> 方法。
     *
     * @param os 用于输出 XML 文档的输出流。
     * @throws IOException 如果写入指定的输出流时发生 <tt>IOException</tt>。
     * @throws BackingStoreException 如果无法从后端存储读取偏好设置数据。
     */
    public void exportNode(OutputStream os)
        throws IOException, BackingStoreException
    {
        XmlSupport.export(os, this, false);
    }

    /**
     * 根据 {@link Preferences#exportSubtree(OutputStream)} 中的规范实现 <tt>exportSubtree</tt> 方法。
     *
     * @param os 用于输出 XML 文档的输出流。
     * @throws IOException 如果写入指定的输出流时发生 <tt>IOException</tt>。
     * @throws BackingStoreException 如果无法从后端存储读取偏好设置数据。
     */
    public void exportSubtree(OutputStream os)
        throws IOException, BackingStoreException
    {
        XmlSupport.export(os, this, true);
    }
}
