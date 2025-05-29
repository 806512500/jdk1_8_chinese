/*
 * 版权所有 (c) 1997, 2013, Oracle 和/或其附属公司。保留所有权利。
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

package java.rmi.activation;

import java.rmi.server.UID;

/**
 * 注册激活组的标识符有几个用途： <ul>
 * <li>在激活系统内唯一标识该组，和
 * <li>包含对组的激活系统的引用，以便组在必要时可以联系其激活系统。</ul><p>
 *
 * <code>ActivationGroupID</code> 是从调用 <code>ActivationSystem.registerGroup</code> 返回的，并用于在激活系统中标识该组。当创建/重新创建激活组时，此组ID作为激活组的特殊构造函数的参数之一传递。
 *
 * @author      Ann Wollrath
 * @see         ActivationGroup
 * @see         ActivationGroupDesc
 * @since       1.2
 */
public class ActivationGroupID implements java.io.Serializable {
    /**
     * @serial 组的激活系统。
     */
    private ActivationSystem system;

    /**
     * @serial 组的唯一ID。
     */
    private UID uid = new UID();

    /** 表示与 Java 2 SDK v1.2 版本的类兼容 */
    private  static final long serialVersionUID = -1648432278909740833L;

    /**
     * 构建一个唯一的组ID。
     *
     * @param system 组的激活系统
     * @throws UnsupportedOperationException 如果且仅当此实现不支持激活时
     * @since 1.2
     */
    public ActivationGroupID(ActivationSystem system) {
        this.system = system;
    }

    /**
     * 返回组的激活系统。
     * @return 组的激活系统
     * @since 1.2
     */
    public ActivationSystem getSystem() {
        return system;
    }

    /**
     * 返回组标识符的哈希码。两个引用相同远程组的组标识符将具有相同的哈希码。
     *
     * @see java.util.Hashtable
     * @since 1.2
     */
    public int hashCode() {
        return uid.hashCode();
    }

    /**
     * 比较两个组标识符的内容是否相等。
     * 如果以下两个条件都为真，则返回 true：
     * 1) 唯一标识符内容等效，和
     * 2) 每个指定的激活系统引用相同的远程对象。
     *
     * @param   obj     要比较的对象
     * @return  如果这些对象相等则返回 true；否则返回 false。
     * @see             java.util.Hashtable
     * @since 1.2
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof ActivationGroupID) {
            ActivationGroupID id = (ActivationGroupID)obj;
            return (uid.equals(id.uid) && system.equals(id.system));
        } else {
            return false;
        }
    }
}
