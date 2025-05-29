/*
 * 版权所有 (c) 1994, 1998, Oracle 和/或其附属公司。保留所有权利。
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
 * 当一个类希望在可观察对象发生变化时得到通知，它可以实现 <code>Observer</code> 接口。
 *
 * @author  Chris Warth
 * @see     java.util.Observable
 * @since   JDK1.0
 */
public interface Observer {
    /**
     * 当被观察的对象发生改变时，此方法将被调用。应用程序调用一个 <tt>Observable</tt> 对象的
     * <code>notifyObservers</code> 方法，以通知该对象的所有观察者发生了改变。
     *
     * @param   o     可观察对象。
     * @param   arg   传递给 <code>notifyObservers</code> 方法的参数。
     */
    void update(Observable o, Object arg);
}