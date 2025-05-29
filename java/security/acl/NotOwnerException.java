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
 * 这是一个异常，当对象（如访问控制列表）的修改仅允许由对象的所有者进行，但尝试进行修改的Principal不是所有者时抛出。
 *
 * @author      Satish Dharmaraj
 */
public class NotOwnerException extends Exception {

    private static final long serialVersionUID = -5555597911163362399L;

    /**
     * 构造一个 NotOwnerException。
     */
    public NotOwnerException() {
    }
}
