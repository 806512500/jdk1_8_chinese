/*
 * 版权所有 (c) 1997, 2013, Oracle 和/或其附属公司。保留所有权利。
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

package java.security;

/**
 * GuardedObject 是一个用于保护对另一个对象访问的对象。
 *
 * <p>GuardedObject 封装了一个目标对象和一个 Guard 对象，使得只有当 Guard 对象允许时，
 * 才能访问目标对象。一旦一个对象被 GuardedObject 封装，对该对象的访问就由 {@code getObject}
 * 方法控制，该方法会调用保护访问的 Guard 对象的
 * {@code checkGuard} 方法。如果访问不允许，则会抛出异常。
 *
 * @see Guard
 * @see Permission
 *
 * @author Roland Schemers
 * @author Li Gong
 */

public class GuardedObject implements java.io.Serializable {

    private static final long serialVersionUID = -5240450096227834308L;

    private Object object; // 我们保护的对象
    private Guard guard;   // 保护者

    /**
     * 使用指定的对象和保护者构造一个 GuardedObject。
     * 如果 Guard 对象为 null，则不会对谁可以访问该对象施加任何限制。
     *
     * @param object 要保护的对象。
     *
     * @param guard 保护访问该对象的 Guard 对象。
     */

    public GuardedObject(Object object, Guard guard)
    {
        this.guard = guard;
        this.object = object;
    }

    /**
     * 检索受保护的对象，如果保护者拒绝访问受保护的对象，则抛出异常。
     *
     * @return 受保护的对象。
     *
     * @exception SecurityException 如果访问受保护的对象被拒绝。
     */
    public Object getObject()
        throws SecurityException
    {
        if (guard != null)
            guard.checkGuard(object);

        return object;
    }

    /**
     * 将此对象写入流（即序列化它）。如果有保护者，我们检查保护者。
     */
    private void writeObject(java.io.ObjectOutputStream oos)
        throws java.io.IOException
    {
        if (guard != null)
            guard.checkGuard(object);

        oos.defaultWriteObject();
    }
}
