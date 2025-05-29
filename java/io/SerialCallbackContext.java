/*
 * Copyright (c) 2006, 2012, Oracle and/or its affiliates. All rights reserved.
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

package java.io;

/**
 * 在对象流调用到类定义的 readObject/writeObject 方法期间的上下文。
 * 保存当前正在反序列化的对象和当前类的描述符。
 *
 * 此上下文跟踪其构造所在的线程，并允许仅调用一次 defaultReadObject、readFields、defaultWriteObject
 * 或 writeFields，这些方法必须在类的 readObject/writeObject 方法返回之前在同一线程上调用。
 * 如果未设置为当前线程，getObj 方法会抛出 NotActiveException。
 */
final class SerialCallbackContext {
    private final Object obj;
    private final ObjectStreamClass desc;
    /**
     * 此上下文正在使用的线程。
     * 由于这仅在一个线程中工作，因此我们不必担心线程安全性。
     */
    private Thread thread;

    public SerialCallbackContext(Object obj, ObjectStreamClass desc) {
        this.obj = obj;
        this.desc = desc;
        this.thread = Thread.currentThread();
    }

    public Object getObj() throws NotActiveException {
        checkAndSetUsed();
        return obj;
    }

    public ObjectStreamClass getDesc() {
        return desc;
    }

    public void check() throws NotActiveException {
        if (thread != null && thread != Thread.currentThread()) {
            throw new NotActiveException(
                "expected thread: " + thread + ", but got: " + Thread.currentThread());
        }
    }

    private void checkAndSetUsed() throws NotActiveException {
        if (thread != Thread.currentThread()) {
             throw new NotActiveException(
              "not in readObject invocation or fields already read");
        }
        thread = null;
    }

    public void setUsed() {
        thread = null;
    }
}
