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
 * 这是一个异常，当尝试删除访问控制列表的最后一个所有者时抛出。
 *
 * @see java.security.acl.Owner#deleteOwner
 *
 * @author Satish Dharmaraj
 */
public class LastOwnerException extends Exception {

    private static final long serialVersionUID = -5141997548211140359L;

    /**
     * 构造一个 LastOwnerException。
     */
    public LastOwnerException() {
    }
}
