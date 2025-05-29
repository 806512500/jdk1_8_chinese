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
package java.io;

import java.util.*;
import java.io.File;

/**
 * 该类通过一个关闭钩子在 VM 退出时删除一组文件名。
 * 使用集合既可防止同一文件的重复插入，也提供快速删除。
 */

class DeleteOnExitHook {
    private static LinkedHashSet<String> files = new LinkedHashSet<>();
    static {
        // DeleteOnExitHook 必须是最后一个被调用的关闭钩子。
        // 应用程序关闭钩子可能在关闭过程中添加第一个文件到
        // 删除列表，导致 DeleteOnExitHook 在关闭过程中注册。因此，将
        // registerShutdownInProgress 参数设置为 true。
        sun.misc.SharedSecrets.getJavaLangAccess()
            .registerShutdownHook(2 /* 关闭钩子调用顺序 */,
                true /* 即使关闭正在进行中也要注册 */,
                new Runnable() {
                    public void run() {
                       runHooks();
                    }
                }
        );
    }

    private DeleteOnExitHook() {}

    static synchronized void add(String file) {
        if(files == null) {
            // DeleteOnExitHook 正在运行。添加文件为时已晚
            throw new IllegalStateException("关闭正在进行中");
        }

        files.add(file);
    }

    static void runHooks() {
        LinkedHashSet<String> theFiles;

        synchronized (DeleteOnExitHook.class) {
            theFiles = files;
            files = null;
        }

        ArrayList<String> toBeDeleted = new ArrayList<>(theFiles);

        // 反转列表以保持之前的 JDK 删除顺序。
        // 最后添加的文件最先被删除。
        Collections.reverse(toBeDeleted);
        for (String filename : toBeDeleted) {
            (new File(filename)).delete();
        }
    }
}
