
/*
 * 版权所有 (c) 2005, 2010, Oracle 和/或其附属公司。保留所有权利。
 * ORACLE 专有/机密。使用受许可条款约束。
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

import java.util.*;

/*
 * 用于跟踪和运行通过 <tt>{@link Runtime#addShutdownHook Runtime.addShutdownHook}</tt> 注册的用户级关闭钩子的类。
 *
 * @see java.lang.Runtime#addShutdownHook
 * @see java.lang.Runtime#removeShutdownHook
 */

class ApplicationShutdownHooks {
    /* 注册的钩子集合 */
    private static IdentityHashMap<Thread, Thread> hooks;
    static {
        try {
            Shutdown.add(1 /* 关闭钩子调用顺序 */,
                false /* 如果关闭正在进行，则不注册 */,
                new Runnable() {
                    public void run() {
                        runHooks();
                    }
                }
            );
            hooks = new IdentityHashMap<>();
        } catch (IllegalStateException e) {
            // 如果关闭正在进行，则不能添加应用程序关闭钩子。
            hooks = null;
        }
    }


    private ApplicationShutdownHooks() {}

    /* 添加一个新的关闭钩子。检查关闭状态和钩子本身，但不执行任何安全检查。 */
    static synchronized void add(Thread hook) {
        if(hooks == null)
            throw new IllegalStateException("关闭正在进行");

        if (hook.isAlive())
            throw new IllegalArgumentException("钩子已经在运行");

        if (hooks.containsKey(hook))
            throw new IllegalArgumentException("钩子已注册");

        hooks.put(hook, hook);
    }

    /* 移除一个之前注册的钩子。与 add 方法类似，此方法不执行任何安全检查。 */
    static synchronized boolean remove(Thread hook) {
        if(hooks == null)
            throw new IllegalStateException("关闭正在进行");

        if (hook == null)
            throw new NullPointerException();

        return hooks.remove(hook) != null;
    }

    /* 遍历所有应用程序钩子，为每个钩子创建一个新的线程来运行。钩子并发运行，此方法等待它们完成。 */
    static void runHooks() {
        Collection<Thread> threads;
        synchronized(ApplicationShutdownHooks.class) {
            threads = hooks.keySet();
            hooks = null;
        }

        for (Thread hook : threads) {
            hook.start();
        }
        for (Thread hook : threads) {
            while (true) {
                try {
                    hook.join();
                    break;
                } catch (InterruptedException ignored) {
                }
            }
        }
    }
}