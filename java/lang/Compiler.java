/*
 * 版权所有 (c) 1995, 2008, Oracle 和/或其附属公司。保留所有权利。
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

/**
 * {@code Compiler} 类旨在支持从 Java 到本机代码的编译器及相关服务。设计上，{@code Compiler} 类不执行任何操作；它作为 JIT 编译器实现的占位符。
 *
 * <p> 当 Java 虚拟机首次启动时，它会确定系统属性 {@code java.compiler} 是否存在。（系统属性可以通过 {@link System#getProperty(String)} 和 {@link
 * System#getProperty(String, String)} 访问。）如果存在，假定其为库的名称（具有平台相关的精确位置和类型）；调用 {@link
 * System#loadLibrary} 加载该库。如果加载成功，将调用该库中的名为 {@code java_lang_Compiler_start()} 的函数。
 *
 * <p> 如果没有可用的编译器，这些方法将不执行任何操作。
 *
 * @author  Frank Yellin
 * @since   JDK1.0
 */
public final class Compiler  {
    private Compiler() {}               // 不创建实例

    private static native void initialize();

    private static native void registerNatives();

    static {
        registerNatives();
        java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction<Void>() {
                public Void run() {
                    boolean loaded = false;
                    String jit = System.getProperty("java.compiler");
                    if ((jit != null) && (!jit.equals("NONE")) &&
                        (!jit.equals("")))
                    {
                        try {
                            System.loadLibrary(jit);
                            initialize();
                            loaded = true;
                        } catch (UnsatisfiedLinkError e) {
                            System.err.println("警告: 未找到 JIT 编译器 \"" +
                              jit + "\"。将使用解释器。");
                        }
                    }
                    String info = System.getProperty("java.vm.info");
                    if (loaded) {
                        System.setProperty("java.vm.info", info + ", " + jit);
                    } else {
                        System.setProperty("java.vm.info", info + ", nojit");
                    }
                    return null;
                }
            });
    }

    /**
     * 编译指定的类。
     *
     * @param  clazz
     *         一个类
     *
     * @return  如果编译成功返回 {@code true}；如果编译失败或没有可用的编译器返回 {@code false}
     *
     * @throws  NullPointerException
     *          如果 {@code clazz} 是 {@code null}
     */
    public static native boolean compileClass(Class<?> clazz);

    /**
     * 编译名称与指定字符串匹配的所有类。
     *
     * @param  string
     *         要编译的类的名称
     *
     * @return  如果编译成功返回 {@code true}；如果编译失败或没有可用的编译器返回 {@code false}
     *
     * @throws  NullPointerException
     *          如果 {@code string} 是 {@code null}
     */
    public static native boolean compileClasses(String string);

    /**
     * 检查参数类型及其字段并执行某些记录的操作。不要求执行特定操作。
     *
     * @param  any
     *         一个参数
     *
     * @return  如果没有可用的编译器返回 {@code null}，否则返回编译器特定的值
     *
     * @throws  NullPointerException
     *          如果 {@code any} 是 {@code null}
     */
    public static native Object command(Object any);

    /**
     * 使编译器恢复运行。
     */
    public static native void enable();

    /**
     * 使编译器停止运行。
     */
    public static native void disable();
}
