/*
 * 版权所有 (c) 2000, 2004, Oracle 和/或其附属公司。保留所有权利。
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
 * 一个抽象的包装类，用于封装一个 {@code EventListener} 类，
 * 并将一组额外的参数与监听器关联。子类必须提供额外参数的存储和访问方法。
 * <p>
 * 例如，支持命名属性的 bean 将具有一个带有两个参数的方法签名，用于添加
 * 一个 {@code PropertyChangeListener} 到属性：
 * <pre>
 * public void addPropertyChangeListener(String propertyName,
 *                                       PropertyChangeListener listener)
 * </pre>
 * 如果 bean 还实现了无参数的获取监听器方法：
 * <pre>
 * public PropertyChangeListener[] getPropertyChangeListeners()
 * </pre>
 * 那么数组中可能包含内部的 {@code PropertyChangeListeners}，
 * 它们同时也是 {@code PropertyChangeListenerProxy} 对象。
 * <p>
 * 如果调用方法对检索命名属性感兴趣，则必须测试元素是否为代理类。
 *
 * @since 1.4
 */
public abstract class EventListenerProxy<T extends EventListener>
        implements EventListener {

    private final T listener;

    /**
     * 为指定的监听器创建一个代理。
     *
     * @param listener  监听器对象
     */
    public EventListenerProxy(T listener) {
        this.listener = listener;
    }

    /**
     * 返回与代理关联的监听器。
     *
     * @return  与代理关联的监听器
     */
    public T getListener() {
        return this.listener;
    }
}