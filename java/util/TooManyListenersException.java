/*
 * 版权所有 (c) 1996, 2008, Oracle 和/或其关联公司。保留所有权利。
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

package java.util;

/**
 * <p>
 * <code> TooManyListenersException </code> 异常用于 Java 事件模型中，以注解和实现多播事件源的单播特殊情况。
 * </p>
 * <p>
 * 在任何给定的具体实现中，如果 "void addXyzEventListener" 事件监听器注册模式的通常多播形式上存在 "throws TooManyListenersException" 子句，
 * 则用于注解该接口实现了一个单播监听器的特殊情况，即，在特定的事件监听器源上同时只能注册一个监听器。
 * </p>
 *
 * @see java.util.EventObject
 * @see java.util.EventListener
 *
 * @author Laurence P. G. Cable
 * @since  JDK1.1
 */

public class TooManyListenersException extends Exception {
    private static final long serialVersionUID = 5074640544770687831L;

    /**
     * 构造一个没有详细消息的 TooManyListenersException。
     * 详细消息是一个描述此特定异常的字符串。
     */

    public TooManyListenersException() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个 TooManyListenersException。
     * 详细消息是一个描述此特定异常的字符串。
     * @param s 详细消息
     */

    public TooManyListenersException(String s) {
        super(s);
    }
}