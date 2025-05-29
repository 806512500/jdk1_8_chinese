/*
 * Copyright (c) 1999, 2012, Oracle and/or its affiliates. All rights reserved.
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

package java.lang;

import sun.misc.Signal;
import sun.misc.SignalHandler;


/**
 * 包私有实用类，用于设置和拆除终止触发的关闭的平台特定支持。
 *
 * @author   Mark Reinhold
 * @since    1.3
 */

class Terminator {

    private static SignalHandler handler = null;

    /* setup 和 teardown 的调用已经在关闭锁上同步，
     * 因此这里不需要进一步的同步
     */

    static void setup() {
        if (handler != null) return;
        SignalHandler sh = new SignalHandler() {
            public void handle(Signal sig) {
                Shutdown.exit(sig.getNumber() + 0200);
            }
        };
        handler = sh;

        // 当指定了 -Xrs 时，用户负责通过调用
        // System.exit() 来确保关闭挂钩的运行
        try {
            Signal.handle(new Signal("INT"), sh);
        } catch (IllegalArgumentException e) {
        }
        try {
            Signal.handle(new Signal("TERM"), sh);
        } catch (IllegalArgumentException e) {
        }
    }

    static void teardown() {
        /* 当前的 sun.misc.Signal 类不支持
         * 取消处理程序
         */
    }

}
