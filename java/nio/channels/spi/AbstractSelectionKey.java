/*
 * Copyright (c) 2000, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.nio.channels.spi;

import java.nio.channels.*;


/**
 * 选择键的基实现类。
 *
 * <p> 该类跟踪键的有效性并实现取消操作。 </p>
 *
 * @author Mark Reinhold
 * @author JSR-51 专家组
 * @since 1.4
 */

public abstract class AbstractSelectionKey
    extends SelectionKey
{

    /**
     * 初始化此类的新实例。
     */
    protected AbstractSelectionKey() { }

    private volatile boolean valid = true;

    public final boolean isValid() {
        return valid;
    }

    void invalidate() {                                 // 包私有
        valid = false;
    }

    /**
     * 取消此键。
     *
     * <p> 如果此键尚未被取消，则它将被添加到其选择器的已取消键集合中，同时同步该集合。 </p>
     */
    public final void cancel() {
        // 同步 "this" 以防止此键被不同线程多次取消，这可能会导致选择器的 select() 和通道的 close() 之间的竞争条件。
        synchronized (this) {
            if (valid) {
                valid = false;
                ((AbstractSelector)selector()).cancel(this);
            }
        }
    }
}
