/*
 * 版权所有 (c) 1994, 2012, Oracle 和/或其附属公司。保留所有权利。
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
 * 该类表示一个可观察的对象，或在模型-视图范式中的“数据”。
 * 它可以被子类化以表示应用程序希望被观察的对象。
 * <p>
 * 一个可观察对象可以有一个或多个观察者。观察者可以是实现 <tt>Observer</tt> 接口的任何对象。
 * 在可观察实例更改后，应用程序调用 <code>Observable</code> 的 <code>notifyObservers</code> 方法
 * 会导致所有观察者通过调用它们的 <code>update</code> 方法来通知更改。
 * <p>
 * 通知的顺序是未指定的。Observable 类提供的默认实现将按照它们注册兴趣的顺序通知观察者，
 * 但子类可以更改此顺序，不使用任何保证的顺序，使用单独的线程进行通知，或者保证其子类遵循此顺序，具体取决于子类的选择。
 * <p>
 * 请注意，此通知机制与线程无关，与 <tt>Object</tt> 类的 <tt>wait</tt> 和 <tt>notify</tt> 机制完全分开。
 * <p>
 * 当一个可观察对象新创建时，其观察者集为空。两个观察者被认为是相同的，当且仅当它们的 <tt>equals</tt> 方法返回 true。
 *
 * @author  Chris Warth
 * @see     java.util.Observable#notifyObservers()
 * @see     java.util.Observable#notifyObservers(java.lang.Object)
 * @see     java.util.Observer
 * @see     java.util.Observer#update(java.util.Observable, java.lang.Object)
 * @since   JDK1.0
 */
public class Observable {
    private boolean changed = false;
    private Vector<Observer> obs;

    /** 构造一个没有观察者的 Observable。 */

    public Observable() {
        obs = new Vector<>();
    }

    /**
     * 如果观察者集不包含相同的观察者，则将观察者添加到此对象的观察者集中。
     * 通知多个观察者的顺序是未指定的。请参阅类注释。
     *
     * @param   o   要添加的观察者。
     * @throws NullPointerException   如果参数 o 为 null。
     */
    public synchronized void addObserver(Observer o) {
        if (o == null)
            throw new NullPointerException();
        if (!obs.contains(o)) {
            obs.addElement(o);
        }
    }

    /**
     * 从此对象的观察者集中删除一个观察者。
     * 向此方法传递 <CODE>null</CODE> 将不会产生任何效果。
     * @param   o   要删除的观察者。
     */
    public synchronized void deleteObserver(Observer o) {
        obs.removeElement(o);
    }

    /**
     * 如果此对象已更改，如 <code>hasChanged</code> 方法所指示，则通知其所有观察者，
     * 然后调用 <code>clearChanged</code> 方法表示此对象不再更改。
     * <p>
     * 每个观察者的 <code>update</code> 方法都会被调用，传入两个参数：这个可观察对象和 <code>null</code>。
     * 换句话说，此方法等效于：
     * <blockquote><tt>
     * notifyObservers(null)</tt></blockquote>
     *
     * @see     java.util.Observable#clearChanged()
     * @see     java.util.Observable#hasChanged()
     * @see     java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    public void notifyObservers() {
        notifyObservers(null);
    }

    /**
     * 如果此对象已更改，如 <code>hasChanged</code> 方法所指示，则通知其所有观察者，
     * 然后调用 <code>clearChanged</code> 方法表示此对象不再更改。
     * <p>
     * 每个观察者的 <code>update</code> 方法都会被调用，传入两个参数：这个可观察对象和 <code>arg</code> 参数。
     *
     * @param   arg   任何对象。
     * @see     java.util.Observable#clearChanged()
     * @see     java.util.Observable#hasChanged()
     * @see     java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    public void notifyObservers(Object arg) {
        /*
         * 一个临时数组缓冲区，用作当前观察者状态的快照。
         */
        Object[] arrLocal;

        synchronized (this) {
            /* 我们不希望观察者在持有自己的 Monitor 时进行回调到任意代码。
             * 从 Vector 中提取每个可观察对象并存储观察者状态的代码需要同步，
             * 但通知观察者不需要（不应该）。这里的任何潜在竞争条件的最坏结果是：
             * 1) 新添加的观察者将错过正在进行的通知
             * 2) 最近注销的观察者将被错误地通知，尽管它不再关心
             */
            if (!changed)
                return;
            arrLocal = obs.toArray();
            clearChanged();
        }


        for (int i = arrLocal.length-1; i>=0; i--)
            ((Observer)arrLocal[i]).update(this, arg);
    }

    /**
     * 清除观察者列表，使此对象不再有任何观察者。
     */
    public synchronized void deleteObservers() {
        obs.removeAllElements();
    }

    /**
     * 标记此 <tt>Observable</tt> 对象已更改；现在 <tt>hasChanged</tt> 方法将返回 <tt>true</tt>。
     */
    protected synchronized void setChanged() {
        changed = true;
    }

    /**
     * 表示此对象不再更改，或者它已经通知了所有观察者其最近的更改，
     * 因此 <tt>hasChanged</tt> 方法现在将返回 <tt>false</tt>。
     * 此方法由 <code>notifyObservers</code> 方法自动调用。
     *
     * @see     java.util.Observable#notifyObservers()
     * @see     java.util.Observable#notifyObservers(java.lang.Object)
     */
    protected synchronized void clearChanged() {
        changed = false;
    }

    /**
     * 测试此对象是否已更改。
     *
     * @return  如果 <code>setChanged</code> 方法比 <code>clearChanged</code> 方法更近地调用在该对象上，则返回 <code>true</code>；
     *          否则返回 <code>false</code>。
     * @see     java.util.Observable#clearChanged()
     * @see     java.util.Observable#setChanged()
     */
    public synchronized boolean hasChanged() {
        return changed;
    }

    /**
     * 返回此 <tt>Observable</tt> 对象的观察者数量。
     *
     * @return  此对象的观察者数量。
     */
    public synchronized int countObservers() {
        return obs.size();
    }
}
