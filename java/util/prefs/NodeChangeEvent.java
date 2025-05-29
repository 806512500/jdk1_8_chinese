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
 * 由 <tt>Preferences</tt> 节点发出的事件，以指示该节点的子节点已被添加或删除。<p>
 *
 * 注意，虽然 NodeChangeEvent 继承了来自 java.util.EventObject 的 Serializable 接口，但它不打算被序列化。适当的序列化方法实现会抛出 NotSerializableException。
 *
 * @author  Josh Bloch
 * @see     Preferences
 * @see     NodeChangeListener
 * @see     PreferenceChangeEvent
 * @since   1.4
 * @serial  exclude
 */

public class NodeChangeEvent extends java.util.EventObject {
    /**
     * 被添加或删除的节点。
     *
     * @serial
     */
    private Preferences child;

    /**
     * 构造一个新的 <code>NodeChangeEvent</code> 实例。
     *
     * @param parent  被添加或删除的节点的父节点。
     * @param child   被添加或删除的节点。
     */
    public NodeChangeEvent(Preferences parent, Preferences child) {
        super(parent);
        this.child = child;
    }

    /**
     * 返回被添加或删除的节点的父节点。
     *
     * @return  被添加或删除的子节点的父 Preferences 节点
     */
    public Preferences getParent() {
        return (Preferences) getSource();
    }

    /**
     * 返回被添加或删除的节点。
     *
     * @return  被添加或删除的节点。
     */
    public Preferences getChild() {
        return child;
    }

    /**
     * 抛出 NotSerializableException，因为 NodeChangeEvent 对象不打算被序列化。
     */
     private void writeObject(java.io.ObjectOutputStream out)
                                               throws NotSerializableException {
         throw new NotSerializableException("Not serializable.");
     }

    /**
     * 抛出 NotSerializableException，因为 NodeChangeEvent 对象不打算被序列化。
     */
     private void readObject(java.io.ObjectInputStream in)
                                               throws NotSerializableException {
         throw new NotSerializableException("Not serializable.");
     }

    // 定义此字段是为了在搜索缺少 serialVersionUID 字段时，此类不会被标记为潜在问题。
    private static final long serialVersionUID = 8068949086596572957L;
}
