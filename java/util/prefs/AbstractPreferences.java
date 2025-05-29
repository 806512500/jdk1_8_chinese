
/*
 * 版权所有 (c) 2000, 2013, Oracle 和/或其附属公司。保留所有权利。
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
 * 该类提供了 {@link Preferences} 类的骨架实现，极大地简化了实现它的工作。
 *
 * <p><strong>此类仅适用于 <tt>Preferences</tt> 实现者。
 * <tt>Preferences</tt> 设施的普通用户无需查阅此文档。 {@link Preferences} 文档
 * 应该足够。</strong>
 *
 * <p>实现者必须覆盖九个抽象服务提供者接口 (SPI) 方法：{@link #getSpi(String)}，{@link #putSpi(String,String)}，
 * {@link #removeSpi(String)}，{@link #childSpi(String)}，{@link #removeNodeSpi()}，{@link #keysSpi()}，
 * {@link #childrenNamesSpi()}，{@link #syncSpi()} 和 {@link #flushSpi()}。 所有具体方法都精确地指定了它们是如何基于这些 SPI 方法实现的。
 * 实现者可以根据自己的判断覆盖一个或多个具体方法，如果默认实现因任何原因（如性能）不令人满意。
 *
 * <p>SPI 方法在异常行为方面分为三组。 <tt>getSpi</tt> 方法不应抛出异常，但这并不重要，因为此方法抛出的任何异常都会被
 * {@link #get(String,String)} 拦截，后者会向调用者返回指定的默认值。 <tt>removeNodeSpi, keysSpi,
 * childrenNamesSpi, syncSpi</tt> 和 <tt>flushSpi</tt> 方法被指定为抛出 {@link BackingStoreException}，
 * 并且实现必须在无法执行操作时抛出此检查异常。 该异常向外传播，导致相应的 API 方法失败。
 *
 * <p>剩余的 SPI 方法 {@link #putSpi(String,String)}，{@link #removeSpi(String)} 和 {@link #childSpi(String)}
 * 具有更复杂的异常行为。 它们未被指定为抛出 <tt>BackingStoreException</tt>，因为即使后端存储不可用，它们通常也能遵守其合同。
 * 这是因为它们不返回任何信息，而且它们的效果不需要在后续调用 {@link Preferences#flush()} 或
 * {@link Preferences#sync()} 之前变为永久。 通常情况下，这些 SPI 方法不应抛出异常。 在某些实现中，可能存在这些调用甚至无法将请求的操作排队以供稍后处理的情况。
 * 即使在这种情况下，通常最好忽略调用并返回，而不是抛出异常。 但是，在这些情况下，所有后续的 <tt>flush()</tt> 和 <tt>sync</tt> 调用都应返回 <tt>false</tt>，
 * 因为返回 <tt>true</tt> 会暗示所有先前的操作都已成功变为永久。
 *
 * <p>有一种情况下 <tt>putSpi, removeSpi 和 childSpi</tt> <i>应该</i> 抛出异常：如果调用者在底层操作系统上没有足够的权限来执行请求的操作。
 * 例如，如果非特权用户尝试修改系统首选项，这将在大多数系统上发生。 （所需的权限将因实现而异。 在某些实现中，它们是修改文件系统中某些目录内容的权利；
 * 在其他实现中，它们是修改注册表中某些键内容的权利。）在任何这些情况下，通常不希望让程序继续执行，就好像这些操作将在稍后变为永久一样。
 * 虽然实现不要求在这些情况下抛出异常，但鼓励这样做。 一个 {@link SecurityException} 是合适的。
 *
 * <p>大多数 SPI 方法要求实现读取或写入首选项节点的信息。 实现者应注意，另一个虚拟机可能已从后端存储中并发删除了此节点。
 * 重新创建已删除的节点是实现的责任。
 *
 * <p>实现说明：在 Sun 的默认 <tt>Preferences</tt> 实现中，用户的标识继承自底层操作系统，并且在虚拟机的生命周期内不会改变。
 * 人们认识到，服务器端的 <tt>Preferences</tt> 实现可能在每次请求之间更改用户标识，通过使用静态 {@link ThreadLocal} 实例隐式传递给 <tt>Preferences</tt> 方法。
 * 这种实现的作者 <i>强烈</i> 建议在访问首选项时确定用户（例如通过 {@link #get(String,String)} 或 {@link #put(String,String)} 方法），
 * 而不是将用户永久关联到每个 <tt>Preferences</tt> 实例。 后者的行为与正常的 <tt>Preferences</tt> 用法冲突，并会导致极大的困惑。
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
     * 如果此节点在创建此对象之前在后端存储中不存在，则此字段应为 <tt>true</tt>。 该字段初始化为 false，但可以在子类构造函数中设置为 true
     * （并且之后不应修改）。 该字段指示在创建完成时是否应触发节点更改事件。
     */
    protected boolean newNode = false;

                /**
     * 该节点的所有已知未删除的子节点。 (此“缓存”在调用 childSpi() 或 getChild() 之前会被查询)
     */
    private Map<String, AbstractPreferences> kidCache = new HashMap<>();

    /**
     * 该字段用于跟踪此节点是否已被删除。一旦设置为 true，将永远不会重置为 false。
     */
    private boolean removed = false;

    /**
     * 注册的偏好更改监听器。
     */
    private PreferenceChangeListener[] prefListeners =
        new PreferenceChangeListener[0];

    /**
     * 注册的节点更改监听器。
     */
    private NodeChangeListener[] nodeListeners = new NodeChangeListener[0];

    /**
     * 用于锁定此节点的对象。此对象优于节点本身使用，以减少由于锁定节点而导致的有意或无意的服务拒绝的可能性。
     * 为了避免死锁，一个节点<em>永远不会</em>被持有其后代节点锁的线程锁定。
     */
    protected final Object lock = new Object();

    /**
     * 使用指定的父节点和相对于其父节点的指定名称创建一个偏好节点。
     *
     * @param parent 该偏好节点的父节点，如果这是根节点，则为 null。
     * @param name 该偏好节点的名称，相对于其父节点，如果这是根节点，则为 <tt>""</tt>。
     * @throws IllegalArgumentException 如果 <tt>name</tt> 包含斜杠 (<tt>'/'</tt>)，或者 <tt>parent</tt> 为 <tt>null</tt> 且名称不是 <tt>""</tt>。
     */
    protected AbstractPreferences(AbstractPreferences parent, String name) {
        if (parent==null) {
            if (!name.equals(""))
                throw new IllegalArgumentException("根名称 '"+name+
                                                   "' 必须是 \"\"");
            this.absolutePath = "/";
            root = this;
        } else {
            if (name.indexOf('/') != -1)
                throw new IllegalArgumentException("名称 '" + name +
                                                 "' 包含 '/'");
            if (name.equals(""))
              throw new IllegalArgumentException("非法名称: 空字符串");

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
     * <p>此实现检查键和值是否合法，获取此偏好节点的锁，检查节点是否已被删除，调用 {@link #putSpi(String,String)}，如果有任何偏好更改监听器，则将通知事件排队，由事件分发线程处理。
     *
     * @param key 要与指定值关联的键。
     * @param value 要与指定键关联的值。
     * @throws NullPointerException 如果键或值为 <tt>null</tt>。
     * @throws IllegalArgumentException 如果 <tt>key.length()</tt> 超过 <tt>MAX_KEY_LENGTH</tt> 或 <tt>value.length</tt> 超过 <tt>MAX_VALUE_LENGTH</tt>。
     * @throws IllegalStateException 如果此节点（或其祖先）已使用 {@link #removeNode()} 方法删除。
     */
    public void put(String key, String value) {
        if (key==null || value==null)
            throw new NullPointerException();
        if (key.length() > MAX_KEY_LENGTH)
            throw new IllegalArgumentException("键太长: "+key);
        if (value.length() > MAX_VALUE_LENGTH)
            throw new IllegalArgumentException("值太长: "+value);

        synchronized(lock) {
            if (removed)
                throw new IllegalStateException("节点已被删除。");

            putSpi(key, value);
            enqueuePreferenceChangeEvent(key, value);
        }
    }

    /**
     * 按照 {@link Preferences#get(String,String)} 中的规范实现 <tt>get</tt> 方法。
     *
     * <p>此实现首先检查 <tt>key</tt> 是否为 <tt>null</tt>，如果是，则抛出 <tt>NullPointerException</tt>。然后获取此偏好节点的锁，检查节点是否已被删除，调用 {@link
     * #getSpi(String)}，并返回结果，除非 <tt>getSpi</tt> 调用返回 <tt>null</tt> 或抛出异常，在这种情况下，此调用返回 <tt>def</tt>。
     *
     * @param key 要返回其关联值的键。
     * @param def 如果此偏好节点没有与 <tt>key</tt> 关联的值，则返回的值。
     * @return 与 <tt>key</tt> 关联的值，如果没有与 <tt>key</tt> 关联的值，则返回 <tt>def</tt>。
     * @throws IllegalStateException 如果此节点（或其祖先）已使用 {@link #removeNode()} 方法删除。
     * @throws NullPointerException 如果键为 <tt>null</tt>。 (允许 <tt>null</tt> 的默认值)
     */
    public String get(String key, String def) {
        if (key==null)
            throw new NullPointerException("空键");
        synchronized(lock) {
            if (removed)
                throw new IllegalStateException("节点已被删除。");

            String result = null;
            try {
                result = getSpi(key);
            } catch (Exception e) {
                // 忽略异常导致返回默认值
            }
            return (result==null ? def : result);
        }
    }

    /**
     * 按照 {@link Preferences#remove(String)} 中的规范实现 <tt>remove(String)</tt> 方法。
     *
     * <p>此实现获取此偏好节点的锁，检查节点是否已被删除，调用 {@link #removeSpi(String)}，如果有任何偏好更改监听器，则将通知事件排队，由事件分发线程处理。
     *
     * @param key 要从偏好节点中移除其映射的键。
     * @throws IllegalStateException 如果此节点（或其祖先）已使用 {@link #removeNode()} 方法删除。
     * @throws NullPointerException {@inheritDoc}.
     */
    public void remove(String key) {
        Objects.requireNonNull(key, "指定的键不能为 null");
        synchronized(lock) {
            if (removed)
                throw new IllegalStateException("节点已被删除。");


                        removeSpi(key);
            enqueuePreferenceChangeEvent(key, null);
        }
    }

    /**
     * 按照 {@link Preferences#clear()} 中的规范实现 <tt>clear</tt> 方法。
     *
     * <p>此实现获取此首选项节点的锁，调用 {@link #keys()} 获取键数组，并
     * 遍历数组对每个键调用 {@link #remove(String)}。
     *
     * @throws BackingStoreException 如果由于后端存储失败或无法与其通信而无法完成此操作。
     * @throws IllegalStateException 如果此节点（或其祖先）已被 {@link #removeNode()} 方法移除。
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
     * <p>此实现使用 {@link Integer#toString(int)} 将 <tt>value</tt> 转换为字符串，并
     * 调用 {@link #put(String,String)} 处理结果。
     *
     * @param key 与值的字符串形式关联的键。
     * @param value 要与键关联的值的字符串形式。
     * @throws NullPointerException 如果键为 <tt>null</tt>。
     * @throws IllegalArgumentException 如果 <tt>key.length()</tt> 超过 <tt>MAX_KEY_LENGTH</tt>。
     * @throws IllegalStateException 如果此节点（或其祖先）已被 {@link #removeNode()} 方法移除。
     */
    public void putInt(String key, int value) {
        put(key, Integer.toString(value));
    }

    /**
     * 按照 {@link Preferences#getInt(String,int)} 中的规范实现 <tt>getInt</tt> 方法。
     *
     * <p>此实现调用 {@link #get(String,String) <tt>get(key, null)</tt>}。如果返回值非空，
     * 实现尝试使用 {@link Integer#parseInt(String)} 将其转换为 <tt>int</tt>。如果转换成功，
     * 返回值由此方法返回。否则，返回 <tt>def</tt>。
     *
     * @param key 要作为 int 返回的关联值的键。
     * @param def 如果此首选项节点没有与 <tt>key</tt> 关联的值或关联值无法解释为 int，则返回的值。
     * @return 与此首选项节点中的 <tt>key</tt> 关联的字符串表示的 int 值，或如果关联值不存在或无法解释为 int，则返回 <tt>def</tt>。
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
     * <p>此实现使用 {@link Long#toString(long)} 将 <tt>value</tt> 转换为字符串，并
     * 调用 {@link #put(String,String)} 处理结果。
     *
     * @param key 与值的字符串形式关联的键。
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
     * <p>此实现调用 {@link #get(String,String) <tt>get(key, null)</tt>}。如果返回值非空，
     * 实现尝试使用 {@link Long#parseLong(String)} 将其转换为 <tt>long</tt>。如果转换成功，
     * 返回值由此方法返回。否则，返回 <tt>def</tt>。
     *
     * @param key 要作为 long 返回的关联值的键。
     * @param def 如果此首选项节点没有与 <tt>key</tt> 关联的值或关联值无法解释为 long，则返回的值。
     * @return 与此首选项节点中的 <tt>key</tt> 关联的字符串表示的 long 值，或如果关联值不存在或无法解释为 long，则返回 <tt>def</tt>。
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
     * <p>此实现使用 {@link String#valueOf(boolean)} 将 <tt>value</tt> 转换为字符串，并
     * 调用 {@link #put(String,String)} 处理结果。
     *
     * @param key 与值的字符串形式关联的键。
     * @param value 要与键关联的值的字符串形式。
     * @throws NullPointerException 如果键为 <tt>null</tt>。
     * @throws IllegalArgumentException 如果 <tt>key.length()</tt> 超过 <tt>MAX_KEY_LENGTH</tt>。
     * @throws IllegalStateException 如果此节点（或其祖先）已被 {@link #removeNode()} 方法移除。
     */
    public void putBoolean(String key, boolean value) {
        put(key, String.valueOf(value));
    }

                /**
     * 实现了 {@link Preferences#getBoolean(String,boolean)} 中指定的 <tt>getBoolean</tt> 方法。
     *
     * <p>此实现调用 {@link #get(String,String) <tt>get(key,
     * null)</tt>}。如果返回值非空，则使用 {@link String#equalsIgnoreCase(String)} 将其与
     * <tt>"true"</tt> 进行比较。如果比较返回 <tt>true</tt>，则此调用返回
     * <tt>true</tt>。否则，原始返回值再次使用 {@link String#equalsIgnoreCase(String)} 与
     * <tt>"false"</tt> 进行比较。如果比较返回 <tt>true</tt>，则此调用返回
     * <tt>false</tt>。否则，此调用返回 <tt>def</tt>。
     *
     * @param key 要返回其布尔值的键。
     * @param def 如果此偏好设置节点没有与 <tt>key</tt> 关联的值或关联值无法解释为布尔值时返回的值。
     * @return 与此偏好设置节点中的 <tt>key</tt> 关联的字符串表示的布尔值，或如果关联值不存在或无法解释为布尔值时返回 <tt>def</tt>。
     * @throws IllegalStateException 如果此节点（或其祖先）已使用 {@link #removeNode()} 方法移除。
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
     * 实现了 {@link Preferences#putFloat(String,float)} 中指定的 <tt>putFloat</tt> 方法。
     *
     * <p>此实现使用 {@link Float#toString(float)} 将 <tt>value</tt> 转换为字符串，并调用 {@link #put(String,String)}
     * 处理结果。
     *
     * @param key 要与值的字符串形式关联的键。
     * @param value 要与键关联的值的字符串形式。
     * @throws NullPointerException 如果键为 <tt>null</tt>。
     * @throws IllegalArgumentException 如果 <tt>key.length()</tt> 超过 <tt>MAX_KEY_LENGTH</tt>。
     * @throws IllegalStateException 如果此节点（或其祖先）已使用 {@link #removeNode()} 方法移除。
     */
    public void putFloat(String key, float value) {
        put(key, Float.toString(value));
    }

    /**
     * 实现了 {@link Preferences#getFloat(String,float)} 中指定的 <tt>getFloat</tt> 方法。
     *
     * <p>此实现调用 {@link #get(String,String) <tt>get(key,
     * null)</tt>}。如果返回值非空，实现尝试使用 {@link Float#parseFloat(String)} 将其转换为 <tt>float</tt>。如果尝试成功，返回值由此方法返回。否则，返回 <tt>def</tt>。
     *
     * @param key 要返回其浮点值的键。
     * @param def 如果此偏好设置节点没有与 <tt>key</tt> 关联的值或关联值无法解释为浮点值时返回的值。
     * @return 与此偏好设置节点中的 <tt>key</tt> 关联的字符串表示的浮点值，或如果关联值不存在或无法解释为浮点值时返回 <tt>def</tt>。
     * @throws IllegalStateException 如果此节点（或其祖先）已使用 {@link #removeNode()} 方法移除。
     * @throws NullPointerException 如果 <tt>key</tt> 为 <tt>null</tt>。
     */
    public float getFloat(String key, float def) {
        float result = def;
        try {
            String value = get(key, null);
            if (value != null)
                result = Float.parseFloat(value);
        } catch (NumberFormatException e) {
            // 忽略异常，导致返回指定的默认值
        }

        return result;
    }

    /**
     * 实现了 {@link Preferences#putDouble(String,double)} 中指定的 <tt>putDouble</tt> 方法。
     *
     * <p>此实现使用 {@link Double#toString(double)} 将 <tt>value</tt> 转换为字符串，并调用 {@link #put(String,String)}
     * 处理结果。
     *
     * @param key 要与值的字符串形式关联的键。
     * @param value 要与键关联的值的字符串形式。
     * @throws NullPointerException 如果键为 <tt>null</tt>。
     * @throws IllegalArgumentException 如果 <tt>key.length()</tt> 超过 <tt>MAX_KEY_LENGTH</tt>。
     * @throws IllegalStateException 如果此节点（或其祖先）已使用 {@link #removeNode()} 方法移除。
     */
    public void putDouble(String key, double value) {
        put(key, Double.toString(value));
    }

    /**
     * 实现了 {@link Preferences#getDouble(String,double)} 中指定的 <tt>getDouble</tt> 方法。
     *
     * <p>此实现调用 {@link #get(String,String) <tt>get(key,
     * null)</tt>}。如果返回值非空，实现尝试使用 {@link Double#parseDouble(String)} 将其转换为 <tt>double</tt>。如果尝试成功，返回值由此方法返回。否则，返回 <tt>def</tt>。
     *
     * @param key 要返回其双精度浮点值的键。
     * @param def 如果此偏好设置节点没有与 <tt>key</tt> 关联的值或关联值无法解释为双精度浮点值时返回的值。
     * @return 与此偏好设置节点中的 <tt>key</tt> 关联的字符串表示的双精度浮点值，或如果关联值不存在或无法解释为双精度浮点值时返回 <tt>def</tt>。
     * @throws IllegalStateException 如果此节点（或其祖先）已使用 {@link #removeNode()} 方法移除。
     * @throws NullPointerException 如果 <tt>key</tt> 为 <tt>null</tt>。
     */
    public double getDouble(String key, double def) {
        double result = def;
        try {
            String value = get(key, null);
            if (value != null)
                result = Double.parseDouble(value);
        } catch (NumberFormatException e) {
            // 忽略异常，导致返回指定的默认值
        }

        return result;
    }


                    return result;
    }

    /**
     * 实现了 {@link Preferences#putByteArray(String,byte[])} 中指定的 <tt>putByteArray</tt> 方法。
     *
     * @param key 与值的字符串形式关联的键。
     * @param value 与键关联的值的字符串形式。
     * @throws NullPointerException 如果 key 或 value 为 <tt>null</tt>。
     * @throws IllegalArgumentException 如果 key.length() 超过 MAX_KEY_LENGTH
     *         或 value.length 超过 MAX_VALUE_LENGTH*3/4。
     * @throws IllegalStateException 如果此节点（或其祖先）已被 {@link #removeNode()} 方法移除。
     */
    public void putByteArray(String key, byte[] value) {
        put(key, Base64.byteArrayToBase64(value));
    }

    /**
     * 实现了 {@link Preferences#getByteArray(String,byte[])} 中指定的 <tt>getByteArray</tt> 方法。
     *
     * @param key 要以字节数组形式返回的关联值的键。
     * @param def 如果此偏好节点没有与 <tt>key</tt> 关联的值，或者关联的值无法解释为字节数组时返回的值。
     * @return 由与此偏好节点中的 <tt>key</tt> 关联的字符串表示的字节数组值，或如果关联的值不存在或无法解释为字节数组时返回 <tt>def</tt>。
     * @throws IllegalStateException 如果此节点（或其祖先）已被 {@link #removeNode()} 方法移除。
     * @throws NullPointerException 如果 <tt>key</tt> 为 <tt>null</tt>。（允许 <tt>def</tt> 为 <tt>null</tt>。）
     */
    public byte[] getByteArray(String key, byte[] def) {
        byte[] result = def;
        String value = get(key, null);
        try {
            if (value != null)
                result = Base64.base64ToByteArray(value);
        }
        catch (RuntimeException e) {
            // 忽略异常导致指定的默认值被返回
        }

        return result;
    }

    /**
     * 实现了 {@link Preferences#keys()} 中指定的 <tt>keys</tt> 方法。
     *
     * <p>此实现获取此偏好节点的锁，检查节点是否已被移除，并调用 {@link #keysSpi()}。
     *
     * @return 与此偏好节点关联的值的键的数组。
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
     * 实现了 {@link Preferences#childrenNames()} 中指定的 <tt>children</tt> 方法。
     *
     * <p>此实现获取此偏好节点的锁，检查节点是否已被移除，构造一个初始化为已缓存子节点名称的 <tt>TreeSet</tt>（此节点的“子缓存”中的子节点），调用 {@link #childrenNamesSpi()}，并将返回的所有子节点名称添加到集合中。使用 <tt>toArray</tt> 方法将树集中的元素转储到 <tt>String</tt> 数组中，并返回此数组。
     *
     * @return 此偏好节点的子节点的名称。
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
     * 返回此节点的所有已知未移除的子节点。
     *
     * @return 此节点的所有已知未移除的子节点。
     */
    protected final AbstractPreferences[] cachedChildren() {
        return kidCache.values().toArray(EMPTY_ABSTRACT_PREFS_ARRAY);
    }

    private static final AbstractPreferences[] EMPTY_ABSTRACT_PREFS_ARRAY
        = new AbstractPreferences[0];

    /**
     * 实现了 {@link Preferences#parent()} 中指定的 <tt>parent</tt> 方法。
     *
     * <p>此实现获取此偏好节点的锁，检查节点是否已被移除，并返回传递给此节点构造函数的父节点值。
     *
     * @return 此偏好节点的父节点。
     * @throws IllegalStateException 如果此节点（或其祖先）已被 {@link #removeNode()} 方法移除。
     */
    public Preferences parent() {
        synchronized(lock) {
            if (removed)
                throw new IllegalStateException("Node has been removed.");

            return parent;
        }
    }

    /**
     * 实现了 {@link Preferences#node(String)} 中指定的 <tt>node</tt> 方法。
     *
     * <p>此实现获取此偏好节点的锁，检查节点是否已被移除。如果 <tt>path</tt> 为 <tt>""</tt>，则返回此节点；如果 <tt>path</tt> 为 <tt>"/"</tt>，则返回此节点的根。如果 <tt>path</tt> 的第一个字符不是 <tt>'/'</tt>，则实现将 <tt>path</tt> 分割成标记，并从该节点递归遍历到命名节点，每次遍历时从 <tt>path</tt> 中“消耗”一个名称和一个斜杠。在每一步中，当前节点被锁定，并检查节点的子缓存中是否存在命名节点。如果未找到，则检查名称的长度是否不超过 <tt>MAX_NAME_LENGTH</tt>。然后调用 {@link #childSpi(String)} 方法，并将结果存储在此节点的子缓存中。如果新创建的 <tt>Preferences</tt> 对象的 {@link #newNode} 字段为 <tt>true</tt> 且存在任何节点更改监听器，则将通知事件排队，由事件分发线程处理。
     *
     * <p>当没有更多标记时，返回在子缓存中找到的最后一个值或由 <tt>childSpi</tt> 返回的值。如果在遍历过程中出现两个连续的 <tt>"/"</tt> 标记，或最终标记为 <tt>"/"</tt>（而不是名称），则抛出适当的 <tt>IllegalArgumentException</tt>。
     *
     * <p>如果 <tt>path</tt> 的第一个字符为 <tt>'/'</tt>（表示绝对路径名），则在将 <tt>path</tt> 分割成标记之前释放此节点的锁，并从根节点（而不是从此节点）开始递归遍历路径。其余遍历过程与相对路径名的遍历过程相同。在从根节点开始遍历之前释放此节点的锁对于避免死锁至关重要，符合 {@link #lock 锁定不变量}。
     *
     * @param path 要返回的偏好节点的路径名。
     * @return 指定的偏好节点。
     * @throws IllegalArgumentException 如果路径名无效（即，包含多个连续的斜杠字符，或以斜杠字符结尾且长度超过一个字符）。
     * @throws IllegalStateException 如果此节点（或其祖先）已被 {@link #removeNode()} 方法移除。
     */
    public Preferences node(String path) {
        synchronized(lock) {
            if (removed)
                throw new IllegalStateException("Node has been removed.");
            if (path.equals(""))
                return this;
            if (path.equals("/"))
                return root;
            if (path.charAt(0) != '/')
                return node(new StringTokenizer(path, "/", true));
        }


                    // 绝对路径。注意我们已经释放了锁以避免死锁
        return root.node(new StringTokenizer(path.substring(1), "/", true));
    }

    /**
     * 分词器包含 <name> {'/' <name>}*
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
     * <p>此实现与 {@link #node(String)} 非常相似，
     * 但使用了 {@link #getChild(String)} 而不是 {@link
     * #childSpi(String)}。
     *
     * @param path 要检查其存在的节点的路径名。
     * @return 如果指定的节点存在，则返回 true。
     * @throws BackingStoreException 如果由于后端存储失败或无法与其通信而无法完成此操作。
     * @throws IllegalArgumentException 如果路径名无效（即，包含多个连续的斜杠字符，或以斜杠字符结尾且长度超过一个字符）。
     * @throws IllegalStateException 如果此节点（或其祖先）已使用 {@link #removeNode()} 方法移除，并且 <tt>pathname</tt> 不是空字符串 (<tt>""</tt>)。
     */
    public boolean nodeExists(String path)
        throws BackingStoreException
    {
        synchronized(lock) {
            if (path.equals(""))
                return !removed;
            if (removed)
                throw new IllegalStateException("节点已被移除。");
            if (path.equals("/"))
                return true;
            if (path.charAt(0) != '/')
                return nodeExists(new StringTokenizer(path, "/", true));
        }

        // 绝对路径。注意我们已经释放了锁以避免死锁
        return root.nodeExists(new StringTokenizer(path.substring(1), "/",
                                                   true));
    }

    /**
     * 分词器包含 <name> {'/' <name>}*
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
     * <p>此实现检查此节点是否为根节点；如果是，则抛出适当的异常。然后，锁定此节点的父节点，
     * 并调用一个递归辅助方法，该方法遍历以该节点为根的子树。递归方法锁定调用它的节点，
     * 检查它是否已被移除，然后确保其所有子节点都已缓存：调用 {@link #childrenNamesSpi()} 方法
     * 并检查返回的每个子节点名称是否包含在子节点缓存中。如果子节点尚未缓存，则调用 {@link
     * #childSpi(String)} 方法为其创建一个 <tt>Preferences</tt> 实例，并将此实例放入子节点缓存中。
     * 然后，辅助方法递归地调用自身，处理其子节点缓存中的每个节点。接下来，它调用 {@link #removeNodeSpi()}，
     * 标记自身为已移除，并从其父节点的子节点缓存中移除自身。最后，如果有任何节点更改监听器，则将通知事件放入队列，
     * 以由事件调度线程处理。
     *
     * <p>注意，辅助方法总是以所有祖先节点（直到“最近的未移除祖先”）锁定的状态被调用。
     *
     * @throws IllegalStateException 如果此节点（或其祖先）已使用 {@link #removeNode()} 方法移除。
     * @throws UnsupportedOperationException 如果此方法在根节点上调用。
     * @throws BackingStoreException 如果由于后端存储失败或无法与其通信而无法完成此操作。
     */
    public void removeNode() throws BackingStoreException {
        if (this==root)
            throw new UnsupportedOperationException("不能移除根节点！");
        synchronized(parent.lock) {
            removeNode2();
            parent.kidCache.remove(name);
        }
    }

    /*
     * 调用时，从“移除根”的父节点到此节点（包括前者但不包括后者）的所有节点都已锁定。
     */
    private void removeNode2() throws BackingStoreException {
        synchronized(lock) {
            if (removed)
                throw new IllegalStateException("节点已移除。");


                        // 确保所有子节点都被缓存
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

            // 现在我们没有后代了 - 是时候消失了！
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
     * @return 相对于其父节点的此偏好节点的名称。
     */
    public String name() {
        return name;
    }

    /**
     * 按照 {@link Preferences#absolutePath()} 中的规范实现 <tt>absolutePath</tt> 方法。
     *
     * <p>此实现仅返回在构造此节点时计算的绝对路径名称（基于传递给此节点构造函数的名称，以及传递给此节点祖先构造函数的名称）。
     *
     * @return 此偏好节点的绝对路径名称。
     */
    public String absolutePath() {
        return absolutePath;
    }

    /**
     * 按照 {@link Preferences#isUserNode()} 中的规范实现 <tt>isUserNode</tt> 方法。
     *
     * <p>此实现将此节点的根节点（存储在私有字段中）与 {@link Preferences#userRoot()} 返回的值进行比较。如果两个对象引用相同，此方法返回 true。
     *
     * @return 如果此偏好节点在用户偏好树中，则返回 <tt>true</tt>；如果在系统偏好树中，则返回 <tt>false</tt>。
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
            throw new NullPointerException("Change listener is null.");
        synchronized(lock) {
            if (removed)
                throw new IllegalStateException("Node has been removed.");

            // 写时复制
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
                throw new IllegalStateException("Node has been removed.");
            if ((prefListeners == null) || (prefListeners.length == 0))
                throw new IllegalArgumentException("Listener not registered.");

            // 写时复制
            PreferenceChangeListener[] newPl =
                new PreferenceChangeListener[prefListeners.length - 1];
            int i = 0;
            while (i < newPl.length && prefListeners[i] != pcl)
                newPl[i] = prefListeners[i++];

            if (i == newPl.length &&  prefListeners[i] != pcl)
                throw new IllegalArgumentException("Listener not registered.");
            while (i < newPl.length)
                newPl[i] = prefListeners[++i];
            prefListeners = newPl;
        }
    }

    public void addNodeChangeListener(NodeChangeListener ncl) {
        if (ncl==null)
            throw new NullPointerException("Change listener is null.");
        synchronized(lock) {
            if (removed)
                throw new IllegalStateException("Node has been removed.");

            // 写时复制
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
                throw new IllegalStateException("Node has been removed.");
            if ((nodeListeners == null) || (nodeListeners.length == 0))
                throw new IllegalArgumentException("Listener not registered.");

            // 写时复制
            int i = 0;
            while (i < nodeListeners.length && nodeListeners[i] != ncl)
                i++;
            if (i == nodeListeners.length)
                throw new IllegalArgumentException("Listener not registered.");
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
     * 将给定的键值对放入此偏好节点。保证 <tt>key</tt> 和 <tt>value</tt> 都不为空且长度合法。此外，保证此节点未被删除。（实现者无需检查这些内容。）
     *
     * <p>此方法在持有此节点锁的情况下被调用。
     * @param key 键
     * @param value 值
     */
    protected abstract void putSpi(String key, String value);

                /**
     * 返回与此首选项节点关联的指定键的值，如果没有与此键的关联，或此时无法确定关联，则返回 <tt>null</tt>。
     * 保证 <tt>key</tt> 不为空。此外，保证此节点未被移除。（实现者无需检查这两点。）
     *
     * <p>通常情况下，此方法在任何情况下都不应抛出异常。但是，如果确实抛出了异常，该异常将被拦截并被视为 <tt>null</tt> 返回值。
     *
     * <p>此方法在持有此节点锁的情况下被调用。
     *
     * @param key 键
     * @return 与此首选项节点关联的指定键的值，如果没有与此键的关联，或此时无法确定关联，则返回 <tt>null</tt>。
     */
    protected abstract String getSpi(String key);

    /**
     * 移除此首选项节点中指定键的关联（如果存在）。保证 <tt>key</tt> 不为空。此外，保证此节点未被移除。（实现者无需检查这两点。）
     *
     * <p>此方法在持有此节点锁的情况下被调用。
     * @param key 键
     */
    protected abstract void removeSpi(String key);

    /**
     * 移除此首选项节点，使其失效并清除其包含的所有首选项。调用此方法时，命名的子节点将没有任何后代（即，{@link Preferences#removeNode()} 方法
     * 以自底向上的方式重复调用此方法，先移除每个节点的后代，然后再移除节点本身）。
     *
     * <p>此方法在持有此节点及其父节点（以及因单次调用 {@link Preferences#removeNode()} 而被移除的所有祖先节点）的锁的情况下被调用。
     *
     * <p>节点的移除无需立即持久化，直到对此节点（或其祖先）调用 <tt>flush</tt> 方法。
     *
     * <p>如果此节点抛出 <tt>BackingStoreException</tt>，该异常将传播到包含的 {@link #removeNode()} 调用之外。
     *
     * @throws BackingStoreException 如果由于后端存储故障或无法与后端存储通信而无法完成此操作。
     */
    protected abstract void removeNodeSpi() throws BackingStoreException;

    /**
     * 返回与此首选项节点关联的所有键的数组。（如果此节点没有首选项，返回的数组大小将为零。）保证此节点未被移除。
     *
     * <p>此方法在持有此节点锁的情况下被调用。
     *
     * <p>如果此节点抛出 <tt>BackingStoreException</tt>，该异常将传播到包含的 {@link #keys()} 调用之外。
     *
     * @return 与此首选项节点关联的所有键的数组。
     * @throws BackingStoreException 如果由于后端存储故障或无法与后端存储通信而无法完成此操作。
     */
    protected abstract String[] keysSpi() throws BackingStoreException;

    /**
     * 返回此首选项节点的子节点的名称。（如果此节点没有子节点，返回的数组大小将为零。）此方法无需返回任何已缓存的节点名称，但这样做不会造成伤害。
     *
     * <p>此方法在持有此节点锁的情况下被调用。
     *
     * <p>如果此节点抛出 <tt>BackingStoreException</tt>，该异常将传播到包含的 {@link #childrenNames()} 调用之外。
     *
     * @return 包含此首选项节点的子节点名称的数组。
     * @throws BackingStoreException 如果由于后端存储故障或无法与后端存储通信而无法完成此操作。
     */
    protected abstract String[] childrenNamesSpi()
        throws BackingStoreException;

    /**
     * 如果存在，则返回命名的子节点，否则返回 <tt>null</tt>。保证 <tt>nodeName</tt> 不为空、非空、不包含斜杠字符 ('/')，且长度不超过
     * {@link #MAX_NAME_LENGTH} 个字符。此外，保证此节点未被移除。（如果实现者选择重写此方法，则无需检查这些条件。）
     *
     * <p>最后，保证命名的节点在上次移除后未被此方法或 {@link #childSpi} 的先前调用返回。换句话说，总是优先使用缓存的值而不是调用此方法。
     * （如果实现者选择重写此方法，则无需维护已返回子节点的缓存。）
     *
     * <p>此实现获取此首选项节点的锁，调用 {@link #childrenNames()} 获取此节点子节点名称的数组，并迭代数组，将每个子节点的名称与指定的节点名称进行比较。
     * 如果找到具有正确名称的子节点，则调用 {@link #childSpi(String)} 方法并返回结果节点。如果迭代完成而未找到指定的名称，则返回 <tt>null</tt>。
     *
     * @param nodeName 要查找的子节点的名称。
     * @return 如果存在，则返回命名的子节点，否则返回 null。
     * @throws BackingStoreException 如果由于后端存储故障或无法与后端存储通信而无法完成此操作。
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
     * 返回此首选项节点的命名子节点，如果它还不存在，则创建它。保证 <tt>name</tt> 非空、非空字符串，不包含斜杠字符 ('/')，并且长度不超过 {@link #MAX_NAME_LENGTH} 个字符。此外，保证此节点未被移除。（实现者无需检查这些条件。）
     *
     * <p>最后，保证命名节点未被先前调用此方法或 {@link #getChild(String)} 方法后移除。换句话说，缓存的值总是优先于调用此方法。子类无需维护先前返回的子节点的缓存。
     *
     * <p>实现者必须确保返回的节点未被移除。如果此节点的同名子节点先前已被移除，实现者必须返回一个新构造的 <tt>AbstractPreferences</tt> 节点；一旦被移除，<tt>AbstractPreferences</tt> 节点不能“复活”。
     *
     * <p>如果此方法导致节点被创建，该节点在调用此节点或其祖先（或后代）的 <tt>flush</tt> 方法之前，不保证持久化。
     *
     * <p>此方法在持有此节点锁的情况下被调用。
     *
     * @param name 要返回的子节点的名称，相对于此首选项节点。
     * @return 命名的子节点。
     */
    protected abstract AbstractPreferences childSpi(String name);

    /**
     * 返回此首选项节点的绝对路径名称。
     */
    public String toString() {
        return (this.isUserNode() ? "User" : "System") +
               " Preference Node: " + this.absolutePath();
    }

    /**
     * 按照 {@link Preferences#sync()} 中的规范实现 <tt>sync</tt> 方法。
     *
     * <p>此实现调用一个递归辅助方法，该方法锁定此节点，调用其上的 syncSpi()，解锁此节点，并递归调用此方法于每个“缓存子节点”。缓存子节点是指在此 VM 中创建且未被随后移除的此节点的子节点。实际上，此方法对以该节点为根的“缓存子树”进行深度优先遍历，仅在锁定该节点时调用子树中每个节点的 syncSpi()。注意 syncSpi() 是自顶向下调用的。
     *
     * @throws BackingStoreException 如果由于后端存储失败或无法与之通信而无法完成此操作。
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
                throw new IllegalStateException("Node has been removed");
            syncSpi();
            cachedKids = cachedChildren();
        }

        for (int i=0; i<cachedKids.length; i++)
            cachedKids[i].sync2();
    }

    /**
     * 此方法在锁定此节点的情况下被调用。此方法的契约是同步存储在此节点中的任何缓存首选项与后端存储中的任何首选项。（完全有可能此节点在后端存储中不存在，因为它可能已被其他 VM 删除，或者尚未创建。）注意，此方法不应同步此节点的任何子节点中的首选项。如果后端存储自然地一次同步整个子树，建议实现者重写 sync()，而不仅仅是重写此方法。
     *
     * <p>如果此节点抛出 <tt>BackingStoreException</tt>，异常将传播到包含的 {@link #sync()} 调用之外。
     *
     * @throws BackingStoreException 如果由于后端存储失败或无法与之通信而无法完成此操作。
     */
    protected abstract void syncSpi() throws BackingStoreException;

    /**
     * 按照 {@link Preferences#flush()} 中的规范实现 <tt>flush</tt> 方法。
     *
     * <p>此实现调用一个递归辅助方法，该方法锁定此节点，调用其上的 flushSpi()，解锁此节点，并递归调用此方法于每个“缓存子节点”。缓存子节点是指在此 VM 中创建且未被随后移除的此节点的子节点。实际上，此方法对以该节点为根的“缓存子树”进行深度优先遍历，仅在锁定该节点时调用子树中每个节点的 flushSpi()。注意 flushSpi() 是自顶向下调用的。
     *
     * <p>如果此方法在已使用 {@link #removeNode()} 方法移除的节点上调用，flushSpi() 将在此节点上调用，但不会在其他节点上调用。
     *
     * @throws BackingStoreException 如果由于后端存储失败或无法与之通信而无法完成此操作。
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
     * 此方法在锁定此节点的情况下被调用。此方法的契约是将此首选项节点中的任何缓存更改强制写入后端存储，保证其持久性。（完全有可能此节点在后端存储中不存在，因为它可能已被其他 VM 删除，或者尚未创建。）注意，此方法不应刷新此节点的任何子节点中的首选项。如果后端存储自然地一次刷新整个子树，建议实现者重写 flush()，而不仅仅是重写此方法。
     *
     * <p>如果此节点抛出 <tt>BackingStoreException</tt>，异常将传播到包含的 {@link #flush()} 调用之外。
     *
     * @throws BackingStoreException 如果由于后端存储失败或无法与之通信而无法完成此操作。
     */
    protected abstract void flushSpi() throws BackingStoreException;

                /**
     * 如果此节点（或其祖先）已被 {@link #removeNode()} 方法移除，则返回 <tt>true</tt>。此方法在返回用于跟踪此状态的私有字段的内容之前，会锁定此节点。
     *
     * @return 如果此节点（或其祖先）已被 {@link #removeNode()} 方法移除，则返回 <tt>true</tt>。
     */
    protected boolean isRemoved() {
        synchronized(lock) {
            return removed;
        }
    }

    /**
     * 待处理通知事件的队列。当发生有监听器的偏好设置或节点更改事件时，它会被放置在此队列中，并通知队列。后台线程等待此队列并传递事件。这将事件传递与偏好设置活动解耦，大大简化了锁定并减少了死锁的机会。
     */
    private static final List<EventObject> eventQueue = new LinkedList<>();

    /**
     * 这两个类用于区分 eventQueue 上的 NodeChangeEvents，以便事件分发线程知道是调用 childAdded 还是 childRemoved。
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
     * 单个后台线程（“事件通知线程”）监控事件队列，并传递放置在队列上的事件。
     */
    private static class EventDispatchThread extends Thread {
        public void run() {
            while(true) {
                // 等待 eventQueue 直到有事件出现
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
     * 此方法在首次调用时启动事件分发线程。仅当有人注册监听器时，才会启动事件分发线程。
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
     * 返回此节点的偏好设置/节点更改监听器。即使我们使用的是写时复制列表，我们仍然使用同步访问器以确保从写线程到读线程的信息传输。
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
     * 将偏好设置更改事件排队以传递给注册的偏好设置更改监听器，除非没有注册的监听器。在持有 this.lock 时调用。
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
     * 将“节点添加”事件排队以传递给注册的节点更改监听器，除非没有注册的监听器。在持有 this.lock 时调用。
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
     * 将“节点移除”事件排队以传递给注册的节点更改监听器，除非没有注册的监听器。在持有 this.lock 时调用。
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
     * 实现了 {@link Preferences#exportNode(OutputStream)} 中指定的 <tt>exportNode</tt> 方法。
     *
     * @param os 用于输出 XML 文档的输出流。
     * @throws IOException 如果向指定的输出流写入时导致 <tt>IOException</tt>。
     * @throws BackingStoreException 如果无法从后端存储读取偏好设置数据。
     */
    public void exportNode(OutputStream os)
        throws IOException, BackingStoreException
    {
        XmlSupport.export(os, this, false);
    }

    /**
     * 实现了 {@link Preferences#exportSubtree(OutputStream)} 中指定的 <tt>exportSubtree</tt> 方法。
     *
     * @param os 用于输出 XML 文档的输出流。
     * @throws IOException 如果向指定的输出流写入时导致 <tt>IOException</tt>。
     * @throws BackingStoreException 如果无法从后端存储读取偏好设置数据。
     */
    public void exportSubtree(OutputStream os)
        throws IOException, BackingStoreException
    {
        XmlSupport.export(os, this, true);
    }
}
