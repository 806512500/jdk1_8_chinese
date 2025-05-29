/*
 * Copyright (c) 2009, Oracle and/or its affiliates. All rights reserved.
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

package java.util.zip;

/**
 * 对原生 zlib 的 z_stream 结构的引用。
 */

class ZStreamRef {

    private volatile long address;
    ZStreamRef (long address) {
        this.address = address;
    }

    long address() {
        return address;
    }

    void clear() {
        address = 0;
    }
}
