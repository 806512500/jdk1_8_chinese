/*
 * Copyright (c) 1996, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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

package java.security.acl;


/**
 * 此接口表示一个权限，例如用于授予对资源的特定类型访问。
 *
 * @author Satish Dharmaraj
 */
public interface Permission {

    /**
     * 如果传递的对象与此接口中表示的权限匹配，则返回 true。
     *
     * @param another 要比较的 Permission 对象。
     *
     * @return 如果 Permission 对象相等则返回 true，否则返回 false
     */
    public boolean equals(Object another);

    /**
     * 打印此权限的字符串表示形式。
     *
     * @return 权限的字符串表示形式。
     */
    public String toString();

}
