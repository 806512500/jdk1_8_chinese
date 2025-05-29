/*
 * Copyright (c) 2010, Oracle and/or its affiliates. All rights reserved.
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

import java.io.IOException;
import java.io.FileDescriptor;

import sun.net.sdp.SdpSupport;

/**
 * 支持 SDP 协议的 SocketImpl
 */
class SdpSocketImpl extends PlainSocketImpl {
    SdpSocketImpl(boolean isServer) {
        super(isServer);
    }

    @Override
    protected void create(boolean stream) throws IOException {
        if (!stream)
            throw new UnsupportedOperationException("必须是流式套接字");
        fd = SdpSupport.createSocket();
        if (socket != null)
            socket.setCreated();
        if (serverSocket != null)
            serverSocket.setCreated();
    }
}
