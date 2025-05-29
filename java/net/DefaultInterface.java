/*
 * Copyright (c) 2011, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.net;

/**
 * 选择一个网络接口作为默认的接口，用于
 * 发出的IPv6流量未指定范围ID（且需要一个范围ID）的情况。
 *
 * 不需要默认接口的平台可以返回null，
 * 这就是此实现所做的。
 */

class DefaultInterface {

    static NetworkInterface getDefault() {
        return null;
    }
}
