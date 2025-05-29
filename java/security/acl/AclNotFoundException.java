/*
 * Copyright (c) 1996, 2003, Oracle and/or its affiliates. All rights reserved.
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
 * 这是一个异常，当引用不存在的ACL（访问控制列表）时抛出。
 *
 * @author      Satish Dharmaraj
 */
public class AclNotFoundException extends Exception {

    private static final long serialVersionUID = 5684295034092681791L;

    /**
     * 构造一个AclNotFoundException。
     */
    public AclNotFoundException() {
    }

}
