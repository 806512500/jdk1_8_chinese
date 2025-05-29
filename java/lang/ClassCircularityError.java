/*
 * 版权所有 (c) 1995, 2008，Oracle 和/或其附属公司。保留所有权利。
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
 * 当 Java 虚拟机检测到正在加载的类的超类层次结构中存在循环时抛出。
 *
 * @author     未署名
 * @since      JDK1.0
 */
public class ClassCircularityError extends LinkageError {
    private static final long serialVersionUID = 1054362542914539689L;

    /**
     * 构造一个没有详细消息的 {@code ClassCircularityError}。
     */
    public ClassCircularityError() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个 {@code ClassCircularityError}。
     *
     * @param  s
     *         详细消息
     */
    public ClassCircularityError(String s) {
        super(s);
    }
}
