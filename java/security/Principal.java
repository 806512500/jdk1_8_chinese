/*
 * 版权所有 (c) 1996, 2013, Oracle 和/或其附属公司。保留所有权利。
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

package java.security;

import javax.security.auth.Subject;

/**
 * 此接口表示主体的抽象概念，可用于表示任何实体，如个人、公司和登录ID。
 *
 * @see java.security.cert.X509Certificate
 *
 * @author Li Gong
 */
public interface Principal {

    /**
     * 将此主体与指定的对象进行比较。如果传递的对象与实现此接口的主体匹配，则返回true。
     *
     * @param another 要比较的主体。
     *
     * @return 如果传递的主体与此主体相同，则返回true，否则返回false。
     */
    public boolean equals(Object another);

    /**
     * 返回此主体的字符串表示形式。
     *
     * @return 此主体的字符串表示形式。
     */
    public String toString();

    /**
     * 返回此主体的哈希码。
     *
     * @return 此主体的哈希码。
     */
    public int hashCode();

    /**
     * 返回此主体的名称。
     *
     * @return 此主体的名称。
     */
    public String getName();

    /**
     * 如果指定的主体由此主体隐含，则返回true。
     *
     * <p>此方法的默认实现如果 {@code subject} 非空且包含至少一个与此主体相等的主体，则返回true。
     *
     * <p>子类可以根据需要覆盖此实现。
     *
     * @param subject {@code Subject}
     * @return 如果 {@code subject} 非空且由此主体隐含，则返回true，否则返回false。
     * @since 1.8
     */
    public default boolean implies(Subject subject) {
        if (subject == null)
            return false;
        return subject.getPrincipals().contains(this);
    }
}
