/*
 * 版权所有 (c) 2000, 2003, Oracle 和/或其附属公司。保留所有权利。
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

import java.io.NotSerializableException;

/**
 * 由 <tt>Preferences</tt> 节点发出的事件，以指示一个偏好设置已被添加、删除或其值已更改。<p>
 *
 * 注意，虽然 PreferenceChangeEvent 继承了 EventObject 的 Serializable 接口，但它并不打算被序列化。适当的序列化方法已实现为抛出 NotSerializableException。
 *
 * @author  Josh Bloch
 * @see Preferences
 * @see PreferenceChangeListener
 * @see NodeChangeEvent
 * @since   1.4
 * @serial exclude
 */
public class PreferenceChangeEvent extends java.util.EventObject {

    /**
     * 发生变化的偏好的键。
     *
     * @serial
     */
    private String key;

    /**
     * 偏好的新值，如果已被删除，则为 <tt>null</tt>。
     *
     * @serial
     */
    private String newValue;

    /**
     * 构造一个新的 <code>PreferenceChangeEvent</code> 实例。
     *
     * @param node  发出事件的 Preferences 节点。
     * @param key  被更改的偏好的键。
     * @param newValue  偏好的新值，如果偏好被删除，则为 <tt>null</tt>。
     */
    public PreferenceChangeEvent(Preferences node, String key,
                                 String newValue) {
        super(node);
        this.key = key;
        this.newValue = newValue;
    }

    /**
     * 返回发出事件的偏好节点。
     *
     * @return  发出事件的偏好节点。
     */
    public Preferences getNode() {
        return (Preferences) getSource();
    }

    /**
     * 返回被更改的偏好的键。
     *
     * @return  被更改的偏好的键。
     */
    public String getKey() {
        return key;
    }

    /**
     * 返回偏好的新值。
     *
     * @return  偏好的新值，如果偏好被删除，则为 <tt>null</tt>。
     */
    public String getNewValue() {
        return newValue;
    }

    /**
     * 抛出 NotSerializableException，因为 NodeChangeEvent 对象不打算被序列化。
     */
     private void writeObject(java.io.ObjectOutputStream out)
                                               throws NotSerializableException {
         throw new NotSerializableException("Not serializable.");
     }

    /**
     * 抛出 NotSerializableException，因为 PreferenceChangeEvent 对象不打算被序列化。
     */
     private void readObject(java.io.ObjectInputStream in)
                                               throws NotSerializableException {
         throw new NotSerializableException("Not serializable.");
     }

    // 定义此字段是为了在搜索缺少 serialVersionUID 字段时不会将此类标记为潜在问题。
    private static final long serialVersionUID = 793724513368024975L;
}
