
/*
 * Copyright (c) 1998, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package java.awt.event;

import sun.awt.AWTAccessor;

import java.awt.ActiveEvent;
import java.awt.AWTEvent;

/**
 * 当由AWT事件分发线程分发时，执行<code>Runnable</code>的<code>run()</code>方法的事件。此类可以用作<code>ActiveEvent</code>的参考实现，
 * 而不是声明一个新类并定义<code>dispatch()</code>。<p>
 *
 * 此类的实例通过调用<code>invokeLater</code>和<code>invokeAndWait</code>放置在<code>EventQueue</code>上。客户端代码可以利用这一事实来编写
 * <code>invokeLater</code>和<code>invokeAndWait</code>的替代函数，而无需在任何<code>AWTEventListener</code>对象中编写特殊代码。
 * <p>
 * 如果任何特定的<code>InvocationEvent</code>实例的<code>id</code>参数不在<code>INVOCATION_FIRST</code>到<code>INVOCATION_LAST</code>的范围内，
 * 将导致未指定的行为。
 *
 * @author      Fred Ecks
 * @author      David Mendenhall
 *
 * @see         java.awt.ActiveEvent
 * @see         java.awt.EventQueue#invokeLater
 * @see         java.awt.EventQueue#invokeAndWait
 * @see         AWTEventListener
 *
 * @since       1.2
 */
public class InvocationEvent extends AWTEvent implements ActiveEvent {

    static {
        AWTAccessor.setInvocationEventAccessor(new AWTAccessor.InvocationEventAccessor() {
            @Override
            public void dispose(InvocationEvent invocationEvent) {
                invocationEvent.finishedDispatching(false);
            }
        });
    }

    /**
     * 标记调用事件ID范围的第一个整数ID。
     */
    public static final int INVOCATION_FIRST = 1200;

    /**
     * 所有InvocationEvents的默认ID。
     */
    public static final int INVOCATION_DEFAULT = INVOCATION_FIRST;

    /**
     * 标记调用事件ID范围的最后一个整数ID。
     */
    public static final int INVOCATION_LAST = INVOCATION_DEFAULT;

    /**
     * 将要调用其<code>run()</code>方法的<code>Runnable</code>。
     */
    protected Runnable runnable;

    /**
     * 在<code>Runnable.run()</code>方法返回或抛出异常后，或在事件被处理后立即调用其<code>notifyAll()</code>方法的（可能为null的）对象。
     *
     * @see #isDispatched
     */
    protected volatile Object notifier;

    /**
     * 在事件被处理或处理后立即调用其<code>run()</code>方法的（可能为null的）<code>Runnable</code>。
     *
     * @see #isDispatched
     * @since 1.8
     */
    private final Runnable listener;

    /**
     * 指示<code>runnable</code>的<code>run()</code>方法是否已执行。
     *
     * @see #isDispatched
     * @since 1.7
     */
    private volatile boolean dispatched = false;

    /**
     * 如果<code>dispatch()</code>捕获了<code>Throwable</code>并将其存储在<code>exception</code>实例变量中，则设置为true。如果为false，则将异常传播到EventDispatchThread的分发循环。
     */
    protected boolean catchExceptions;

    /**
     * 在执行<code>Runnable.run()</code>方法期间抛出的（可能为null的）异常。如果特定实例不捕获异常，则此变量也将为null。
     */
    private Exception exception = null;

    /**
     * 在执行<code>Runnable.run()</code>方法期间抛出的（可能为null的）异常。如果特定实例不捕获异常，则此变量也将为null。
     */
    private Throwable throwable = null;

    /**
     * 事件发生的时间戳。
     *
     * @serial
     * @see #getWhen
     */
    private long when;

    /*
     * JDK 1.1 serialVersionUID。
     */
    private static final long serialVersionUID = 436056344909459450L;

    /**
     * 构造一个具有指定源的<code>InvocationEvent</code>，该事件在分发时将执行<code>runnable</code>的<code>run</code>方法。
     * <p>这是一个便利构造函数。形式为<tt>InvocationEvent(source, runnable)</tt>的调用与形式为
     * <tt>{@link #InvocationEvent(Object, Runnable, Object, boolean) InvocationEvent}(source, runnable, null, false)</tt>的调用行为完全相同。
     * <p>如果<code>source</code>为null，此方法将抛出<code>IllegalArgumentException</code>。
     *
     * @param source    事件的源对象
     * @param runnable  将要执行其<code>run</code>方法的<code>Runnable</code>
     * @throws IllegalArgumentException 如果<code>source</code>为null
     *
     * @see #getSource()
     * @see #InvocationEvent(Object, Runnable, Object, boolean)
     */
    public InvocationEvent(Object source, Runnable runnable) {
        this(source, INVOCATION_DEFAULT, runnable, null, null, false);
    }

    /**
     * 构造一个具有指定源的<code>InvocationEvent</code>，该事件在分发时将执行<code>runnable</code>的<code>run</code>方法。如果<code>notifier</code>不为null，
     * 则在<code>run</code>返回或抛出异常后立即调用其<code>notifyAll()</code>方法。
     * <p>形式为<tt>InvocationEvent(source, runnable, notifier, catchThrowables)</tt>的调用与形式为
     * <tt>{@link #InvocationEvent(Object, int, Runnable, Object, boolean) InvocationEvent}(source, InvocationEvent.INVOCATION_DEFAULT, runnable, notifier, catchThrowables)</tt>的调用行为完全相同。
     * <p>如果<code>source</code>为null，此方法将抛出<code>IllegalArgumentException</code>。
     *
     * @param source            事件的源对象
     * @param runnable          将要执行其<code>run</code>方法的<code>Runnable</code>
     * @param notifier          在<code>Runnable.run</code>返回或抛出异常后，或在事件被处理后立即调用其<code>notifyAll</code>方法的对象
     * @param catchThrowables   指示<code>dispatch</code>是否应在执行<code>Runnable</code>的<code>run</code>方法时捕获<code>Throwable</code>，还是将这些<code>Throwable</code>传播到EventDispatchThread的分发循环
     * @throws IllegalArgumentException 如果<code>source</code>为null
     *
     * @see #getSource()
     * @see     #InvocationEvent(Object, int, Runnable, Object, boolean)
     */
    public InvocationEvent(Object source, Runnable runnable, Object notifier,
                           boolean catchThrowables) {
        this(source, INVOCATION_DEFAULT, runnable, notifier, null, catchThrowables);
    }

    /**
     * 构造一个具有指定源的<code>InvocationEvent</code>，该事件在分发时将执行<code>runnable</code>的<code>run</code>方法。如果<code>listener</code>不为null，
     * 则在<code>run</code>返回、抛出异常或事件被处理后立即调用其<code>run()</code>方法。
     * <p>如果<code>source</code>为null，此方法将抛出<code>IllegalArgumentException</code>。
     *
     * @param source            事件的源对象
     * @param runnable          将要执行其<code>run</code>方法的<code>Runnable</code>
     * @param listener          在<code>InvocationEvent</code>被处理或处理后立即调用其<code>run()</code>方法的<code>Runnable</code>
     * @param catchThrowables   指示<code>dispatch</code>是否应在执行<code>Runnable</code>的<code>run</code>方法时捕获<code>Throwable</code>，还是将这些<code>Throwable</code>传播到EventDispatchThread的分发循环
     * @throws IllegalArgumentException 如果<code>source</code>为null
     */
    public InvocationEvent(Object source, Runnable runnable, Runnable listener,
                           boolean catchThrowables)  {
        this(source, INVOCATION_DEFAULT, runnable, null, listener, catchThrowables);
    }

    /**
     * 构造一个具有指定源和ID的<code>InvocationEvent</code>，该事件在分发时将执行<code>runnable</code>的<code>run</code>方法。如果<code>notifier</code>不为null，
     * 则在<code>run</code>返回或抛出异常后立即调用其<code>notifyAll</code>方法。
     * <p>如果<code>source</code>为null，此方法将抛出<code>IllegalArgumentException</code>。
     *
     * @param source            事件的源对象
     * @param id     表示事件类型的整数。有关允许值的信息，请参阅<code>InvocationEvent</code>的类描述
     * @param runnable          将要执行其<code>run</code>方法的<code>Runnable</code>
     * @param notifier          在<code>Runnable.run</code>返回或抛出异常后，或在事件被处理后立即调用其<code>notifyAll</code>方法的对象
     * @param catchThrowables   指示<code>dispatch</code>是否应在执行<code>Runnable</code>的<code>run</code>方法时捕获<code>Throwable</code>，还是将这些<code>Throwable</code>传播到EventDispatchThread的分发循环
     * @throws IllegalArgumentException 如果<code>source</code>为null
     * @see #getSource()
     * @see #getID()
     */
    protected InvocationEvent(Object source, int id, Runnable runnable,
                              Object notifier, boolean catchThrowables) {
        this(source, id, runnable, notifier, null, catchThrowables);
    }

    private InvocationEvent(Object source, int id, Runnable runnable,
                            Object notifier, Runnable listener, boolean catchThrowables) {
        super(source, id);
        this.runnable = runnable;
        this.notifier = notifier;
        this.listener = listener;
        this.catchExceptions = catchThrowables;
        this.when = System.currentTimeMillis();
    }
    /**
     * 执行<code>Runnable</code>的<code>run()</code>方法，并在<code>run()</code>返回或抛出异常后通知<code>notifier</code>（如果有）。
     *
     * @see #isDispatched
     */
    public void dispatch() {
        try {
            if (catchExceptions) {
                try {
                    runnable.run();
                }
                catch (Throwable t) {
                    if (t instanceof Exception) {
                        exception = (Exception) t;
                    }
                    throwable = t;
                }
            }
            else {
                runnable.run();
            }
        } finally {
            finishedDispatching(true);
        }
    }

    /**
     * 返回在执行<code>Runnable</code>的<code>run()</code>方法时捕获的任何异常。
     *
     * @return 如果抛出了异常，则返回异常的引用；如果没有抛出异常或此<code>InvocationEvent</code>不捕获异常，则返回null
     */
    public Exception getException() {
        return (catchExceptions) ? exception : null;
    }

    /**
     * 返回在执行<code>Runnable</code>的<code>run()</code>方法时捕获的任何异常。
     *
     * @return 如果抛出了异常，则返回异常的引用；如果没有抛出异常或此<code>InvocationEvent</code>不捕获异常，则返回null
     * @since 1.5
     */
    public Throwable getThrowable() {
        return (catchExceptions) ? throwable : null;
    }

    /**
     * 返回事件发生的时间戳。
     *
     * @return 事件的时间戳
     * @since 1.4
     */
    public long getWhen() {
        return when;
    }

    /**
     * 如果事件已分发或在分发过程中抛出了任何异常，则返回<code>true</code>，否则返回<code>false</code>。该方法应由调用<code>notifier.wait()</code>方法的等待线程调用。
     * 由于可能存在虚假唤醒（如<code>Object#wait()</code>中所述），因此应在等待循环中使用此方法以确保事件已分发：
     * <pre>
     *     while (!event.isDispatched()) {
     *         notifier.wait();
     *     }
     * </pre>
     * 如果等待线程在没有分发事件的情况下唤醒，<code>isDispatched()</code>方法将返回<code>false</code>，并且<code>while</code>循环将再次执行，从而导致唤醒的线程重新进入等待模式。
     * <p>
     * 如果<code>notifier.notifyAll()</code>发生在等待线程进入<code>notifier.wait()</code>方法之前，<code>while</code>循环确保等待线程不会进入<code>notifier.wait()</code>方法。
     * 否则，无法保证等待线程将从等待中唤醒。
     *
     * @return 如果事件已分发或在分发过程中抛出了任何异常，则返回<code>true</code>，否则返回<code>false</code>
     * @see #dispatch
     * @see #notifier
     * @see #catchExceptions
     * @since 1.7
     */
    public boolean isDispatched() {
        return dispatched;
    }


                /**
     * 当事件被分发或处理完毕时调用
     * @param dispatched 如果事件被分发则为 true
     *                   如果事件被处理完毕则为 false
     */
    private void finishedDispatching(boolean dispatched) {
        this.dispatched = dispatched;

        if (notifier != null) {
            synchronized (notifier) {
                notifier.notifyAll();
            }
        }

        if (listener != null) {
            listener.run();
        }
    }

    /**
     * 返回一个标识此事件的参数字符串。
     * 此方法对于事件日志记录和调试非常有用。
     *
     * @return  一个标识事件及其属性的字符串
     */
    public String paramString() {
        String typeStr;
        switch(id) {
            case INVOCATION_DEFAULT:
                typeStr = "INVOCATION_DEFAULT";
                break;
            default:
                typeStr = "未知类型";
        }
        return typeStr + ",runnable=" + runnable + ",notifier=" + notifier +
            ",catchExceptions=" + catchExceptions + ",when=" + when;
    }
}
