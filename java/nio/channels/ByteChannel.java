/*
 * Copyright (c) 2000, 2001, Oracle and/or its affiliates. All rights reserved.
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

package java.nio.channels;

import java.io.IOException;


/**
 * 一个可以读写字节的通道。此接口仅统一了 {@link ReadableByteChannel} 和 {@link WritableByteChannel}；
 * 它没有指定任何新的操作。
 *
 * @author Mark Reinhold
 * @author JSR-51 Expert Group
 * @since 1.4
 */

public interface ByteChannel
    extends ReadableByteChannel, WritableByteChannel
{

}
