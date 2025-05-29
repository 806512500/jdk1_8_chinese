
/*
 * Copyright (c) 1996, 2020, Oracle and/or its affiliates. All rights reserved.
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

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import sun.misc.JavaSecurityAccess;
import sun.misc.SharedSecrets;
import sun.misc.Unsafe;
import sun.reflect.CallerSensitive;
import sun.reflect.Reflection;
import sun.reflect.ReflectionFactory;
import sun.reflect.misc.ReflectUtil;

/**
 * 类的序列化描述符。它包含类的名称和 serialVersionUID。可以通过 lookup 方法在当前 Java VM 中查找/创建特定类的 ObjectStreamClass。
 *
 * <p>计算 SerialVersionUID 的算法在
 * <a href="../../../platform/serialization/spec/class.html#4100">对象序列化规范，第 4.6 节，流唯一标识符</a> 中描述。
 *
 * @author      Mike Warres
 * @author      Roger Riggs
 * @see ObjectStreamField
 * @see <a href="../../../platform/serialization/spec/class.html">对象序列化规范，第 4 节，类描述符</a>
 * @since   JDK1.1
 */
public class ObjectStreamClass implements Serializable {

    /** 表示没有可序列化字段的 serialPersistentFields 值 */
    public static final ObjectStreamField[] NO_FIELDS =
        new ObjectStreamField[0];

    private static final long serialVersionUID = -6120832682080437368L;
    private static final ObjectStreamField[] serialPersistentFields =
        NO_FIELDS;

    /** 如果禁用了反序列化构造函数检查，则为 true */
    private static boolean disableSerialConstructorChecks =
        AccessController.doPrivileged(
            new PrivilegedAction<Boolean>() {
                public Boolean run() {
                    String prop = "jdk.disableSerialConstructorChecks";
                    return "true".equals(System.getProperty(prop))
                            ? Boolean.TRUE : Boolean.FALSE;
                }
            }
        ).booleanValue();

    /** 用于获取序列化构造函数的反射工厂 */
    private static final ReflectionFactory reflFactory =
        AccessController.doPrivileged(
            new ReflectionFactory.GetReflectionFactoryAction());

    private static class Caches {
        /** 缓存映射本地类 -> 描述符 */
        static final ConcurrentMap<WeakClassKey,Reference<?>> localDescs =
            new ConcurrentHashMap<>();

        /** 缓存映射字段组/本地描述符对 -> 字段反射器 */
        static final ConcurrentMap<FieldReflectorKey,Reference<?>> reflectors =
            new ConcurrentHashMap<>();

        /** 本地类的 WeakReferences 队列 */
        private static final ReferenceQueue<Class<?>> localDescsQueue =
            new ReferenceQueue<>();
        /** 字段反射器键的 WeakReferences 队列 */
        private static final ReferenceQueue<Class<?>> reflectorsQueue =
            new ReferenceQueue<>();
    }

    /** 与此描述符关联的类（如果有） */
    private Class<?> cl;
    /** 由该描述符表示的类的名称 */
    private String name;
    /** 由该描述符表示的类的 serialVersionUID（如果尚未计算，则为 null） */
    private volatile Long suid;

    /** 如果表示动态代理类，则为 true */
    private boolean isProxy;
    /** 如果表示枚举类型，则为 true */
    private boolean isEnum;
    /** 如果表示的类实现了 Serializable，则为 true */
    private boolean serializable;
    /** 如果表示的类实现了 Externalizable，则为 true */
    private boolean externalizable;
    /** 如果描述符具有由类定义的 writeObject 方法写入的数据，则为 true */
    private boolean hasWriteObjectData;
    /**
     * 如果描述符以块数据格式写入外部数据，则为 true；默认情况下必须为 true，以适应重写 readClassDescriptor() 返回从
     * ObjectStreamClass.lookup() 获得的类描述符的 ObjectInputStream 子类（参见 4461737）
     */
    private boolean hasBlockExternalData = true;

    /**
     * 包含在尝试对无效类进行操作时要抛出的 InvalidClassException 实例的信息。注意，此类的实例是不可变的，并且可能在
     * ObjectStreamClass 实例之间共享。
     */
    private static class ExceptionInfo {
        private final String className;
        private final String message;

        ExceptionInfo(String cn, String msg) {
            className = cn;
            message = msg;
        }

        /**
         * 返回（而不是抛出）一个由该对象中的信息创建的 InvalidClassException 实例，适合由调用者抛出。
         */
        InvalidClassException newInvalidClassException() {
            return new InvalidClassException(className, message);
        }
    }

    /** 在尝试解析类时抛出的异常（如果有） */
    private ClassNotFoundException resolveEx;
    /** 如果尝试非枚举反序列化，则抛出的异常（如果有） */
    private ExceptionInfo deserializeEx;
    /** 如果尝试非枚举序列化，则抛出的异常（如果有） */
    private ExceptionInfo serializeEx;
    /** 如果尝试默认序列化，则抛出的异常（如果有） */
    private ExceptionInfo defaultSerializeEx;


                /** 可序列化的字段 */
    private ObjectStreamField[] fields;
    /** 基本字段的聚合序列化大小 */
    private int primDataSize;
    /** 非基本字段的数量 */
    private int numObjFields;
    /** 用于设置/获取可序列化字段值的反射器 */
    private FieldReflector fieldRefl;
    /** 由此类描述的序列化对象的数据布局 */
    private volatile ClassDataSlot[] dataLayout;

    /** 适用于序列化的构造函数，如果没有则为 null */
    private Constructor<?> cons;
    /** 调用构造函数时需要检查的保护域 */
    private ProtectionDomain[] domains;

    /** 类定义的 writeObject 方法，如果没有则为 null */
    private Method writeObjectMethod;
    /** 类定义的 readObject 方法，如果没有则为 null */
    private Method readObjectMethod;
    /** 类定义的 readObjectNoData 方法，如果没有则为 null */
    private Method readObjectNoDataMethod;
    /** 类定义的 writeReplace 方法，如果没有则为 null */
    private Method writeReplaceMethod;
    /** 类定义的 readResolve 方法，如果没有则为 null */
    private Method readResolveMethod;

    /** 表示类的本地类描述符（可能指向自身） */
    private ObjectStreamClass localDesc;
    /** 流中出现的超类描述符 */
    private ObjectStreamClass superDesc;

    /** 如果且仅当对象已正确初始化时为 true */
    private boolean initialized;

    /**
     * 初始化本地代码。
     */
    private static native void initNative();
    static {
        initNative();
    }

    /**
     * 查找可以序列化的类的描述符。如果此类尚未存在 ObjectStreamClass 实例，则创建一个。
     * 如果指定的类没有实现 java.io.Serializable 或 java.io.Externalizable，则返回 null。
     *
     * @param   cl 要获取描述符的类
     * @return  指定类的类描述符
     */
    public static ObjectStreamClass lookup(Class<?> cl) {
        return lookup(cl, false);
    }

    /**
     * 返回任何类的描述符，无论其是否实现了 {@link Serializable}。
     *
     * @param        cl 要获取描述符的类
     * @return       指定类的类描述符
     * @since 1.6
     */
    public static ObjectStreamClass lookupAny(Class<?> cl) {
        return lookup(cl, true);
    }

    /**
     * 返回此描述符描述的类的名称。
     * 此方法返回的类名格式与 {@link Class#getName} 方法使用的格式相同。
     *
     * @return  代表类名的字符串
     */
    public String getName() {
        return name;
    }

    /**
     * 返回此类的 serialVersionUID。serialVersionUID 定义了一组具有相同名称的类，
     * 这些类从一个共同的根类演变而来，并同意使用共同的格式进行序列化和反序列化。
     * 非可序列化类的 serialVersionUID 为 0L。
     *
     * @return  由此描述符描述的类的 SUID
     */
    public long getSerialVersionUID() {
        // 提醒：是否应该使用同步而不是依赖于 volatile？
        if (suid == null) {
            suid = AccessController.doPrivileged(
                new PrivilegedAction<Long>() {
                    public Long run() {
                        return computeDefaultSUID(cl);
                    }
                }
            );
        }
        return suid.longValue();
    }

    /**
     * 返回此版本映射到的本地 VM 中的类。如果没有对应的本地类，则返回 null。
     *
     * @return  此描述符表示的 <code>Class</code> 实例
     */
    @CallerSensitive
    public Class<?> forClass() {
        if (cl == null) {
            return null;
        }
        requireInitialized();
        if (System.getSecurityManager() != null) {
            Class<?> caller = Reflection.getCallerClass();
            if (ReflectUtil.needsPackageAccessCheck(caller.getClassLoader(), cl.getClassLoader())) {
                ReflectUtil.checkPackageAccess(cl);
            }
        }
        return cl;
    }

    /**
     * 返回此可序列化类的字段数组。
     *
     * @return  包含此类每个持久字段的数组元素。如果没有字段，则返回长度为零的数组。
     * @since 1.2
     */
    public ObjectStreamField[] getFields() {
        return getFields(true);
    }

    /**
     * 按名称获取此类的字段。
     *
     * @param   name 要查找的数据字段的名称
     * @return  命名字段的 ObjectStreamField 对象，如果没有这样的命名字段，则返回 null。
     */
    public ObjectStreamField getField(String name) {
        return getField(name, null);
    }

    /**
     * 返回描述此 ObjectStreamClass 的字符串。
     */
    public String toString() {
        return name + ": static final long serialVersionUID = " +
            getSerialVersionUID() + "L;";
    }

    /**
     * 查找并返回给定类的类描述符，如果类不可序列化且 "all" 设置为 false，则返回 null。
     *
     * @param   cl 要查找的类
     * @param   all 如果为 true，则返回所有类的描述符；如果为 false，则仅返回可序列化类的描述符
     */
    static ObjectStreamClass lookup(Class<?> cl, boolean all) {
        if (!(all || Serializable.class.isAssignableFrom(cl))) {
            return null;
        }
        processQueue(Caches.localDescsQueue, Caches.localDescs);
        WeakClassKey key = new WeakClassKey(cl, Caches.localDescsQueue);
        Reference<?> ref = Caches.localDescs.get(key);
        Object entry = null;
        if (ref != null) {
            entry = ref.get();
        }
        EntryFuture future = null;
        if (entry == null) {
            EntryFuture newEntry = new EntryFuture();
            Reference<?> newRef = new SoftReference<>(newEntry);
            do {
                if (ref != null) {
                    Caches.localDescs.remove(key, ref);
                }
                ref = Caches.localDescs.putIfAbsent(key, newRef);
                if (ref != null) {
                    entry = ref.get();
                }
            } while (ref != null && entry == null);
            if (entry == null) {
                future = newEntry;
            }
        }


                    if (entry instanceof ObjectStreamClass) {  // 首先检查常见情况
            return (ObjectStreamClass) entry;
        }
        if (entry instanceof EntryFuture) {
            future = (EntryFuture) entry;
            if (future.getOwner() == Thread.currentThread()) {
                /*
                 * 处理4803747描述的嵌套调用情况：等待lookup()调用进一步设置future值
                 * 将导致死锁，因此在这里计算并设置future值。
                 */
                entry = null;
            } else {
                entry = future.get();
            }
        }
        if (entry == null) {
            try {
                entry = new ObjectStreamClass(cl);
            } catch (Throwable th) {
                entry = th;
            }
            if (future.set(entry)) {
                Caches.localDescs.put(key, new SoftReference<Object>(entry));
            } else {
                // 嵌套查找调用已经设置了future
                entry = future.get();
            }
        }

        if (entry instanceof ObjectStreamClass) {
            return (ObjectStreamClass) entry;
        } else if (entry instanceof RuntimeException) {
            throw (RuntimeException) entry;
        } else if (entry instanceof Error) {
            throw (Error) entry;
        } else {
            throw new InternalError("意外的条目: " + entry);
        }
    }

    /**
     * 用作类描述符和字段反射器查找表中的占位符，表示正在初始化的条目。
     * （内部）调用者如果在查找结果中收到属于另一个线程的EntryFuture，
     * 应该调用EntryFuture的get()方法；这将在条目准备好并被设置后返回实际条目。
     * 为了节省对象，EntryFutures在自身上同步。
     */
    private static class EntryFuture {

        private static final Object unset = new Object();
        private final Thread owner = Thread.currentThread();
        private Object entry = unset;

        /**
         * 尝试设置此EntryFuture包含的值。如果EntryFuture的值尚未被设置，
         * 则保存该值，通知在get()方法中阻塞的任何调用者，并返回true。
         * 如果值已被设置，则不进行保存或通知，并返回false。
         */
        synchronized boolean set(Object entry) {
            if (this.entry != unset) {
                return false;
            }
            this.entry = entry;
            notifyAll();
            return true;
        }

        /**
         * 返回此EntryFuture包含的值，必要时阻塞直到值被设置。
         */
        synchronized Object get() {
            boolean interrupted = false;
            while (entry == unset) {
                try {
                    wait();
                } catch (InterruptedException ex) {
                    interrupted = true;
                }
            }
            if (interrupted) {
                AccessController.doPrivileged(
                    new PrivilegedAction<Void>() {
                        public Void run() {
                            Thread.currentThread().interrupt();
                            return null;
                        }
                    }
                );
            }
            return entry;
        }

        /**
         * 返回创建此EntryFuture的线程。
         */
        Thread getOwner() {
            return owner;
        }
    }

    /**
     * 创建给定类的本地类描述符。
     */
    private ObjectStreamClass(final Class<?> cl) {
        this.cl = cl;
        name = cl.getName();
        isProxy = Proxy.isProxyClass(cl);
        isEnum = Enum.class.isAssignableFrom(cl);
        serializable = Serializable.class.isAssignableFrom(cl);
        externalizable = Externalizable.class.isAssignableFrom(cl);

        Class<?> superCl = cl.getSuperclass();
        superDesc = (superCl != null) ? lookup(superCl, false) : null;
        localDesc = this;

        if (serializable) {
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                public Void run() {
                    if (isEnum) {
                        suid = Long.valueOf(0);
                        fields = NO_FIELDS;
                        return null;
                    }
                    if (cl.isArray()) {
                        fields = NO_FIELDS;
                        return null;
                    }

                    suid = getDeclaredSUID(cl);
                    try {
                        fields = getSerialFields(cl);
                        computeFieldOffsets();
                    } catch (InvalidClassException e) {
                        serializeEx = deserializeEx =
                            new ExceptionInfo(e.classname, e.getMessage());
                        fields = NO_FIELDS;
                    }

                    if (externalizable) {
                        cons = getExternalizableConstructor(cl);
                    } else {
                        cons = getSerializableConstructor(cl);
                        writeObjectMethod = getPrivateMethod(cl, "writeObject",
                            new Class<?>[] { ObjectOutputStream.class },
                            Void.TYPE);
                        readObjectMethod = getPrivateMethod(cl, "readObject",
                            new Class<?>[] { ObjectInputStream.class },
                            Void.TYPE);
                        readObjectNoDataMethod = getPrivateMethod(
                            cl, "readObjectNoData", null, Void.TYPE);
                        hasWriteObjectData = (writeObjectMethod != null);
                    }
                    domains = getProtectionDomains(cons, cl);
                    writeReplaceMethod = getInheritableMethod(
                        cl, "writeReplace", null, Object.class);
                    readResolveMethod = getInheritableMethod(
                        cl, "readResolve", null, Object.class);
                    return null;
                }
            });
        } else {
            suid = Long.valueOf(0);
            fields = NO_FIELDS;
        }


                    try {
            fieldRefl = getReflector(fields, this);
        } catch (InvalidClassException ex) {
            // 当匹配本地字段与自身时，字段不匹配是不可能的
            throw new InternalError(ex);
        }

        if (deserializeEx == null) {
            if (isEnum) {
                deserializeEx = new ExceptionInfo(name, "枚举类型");
            } else if (cons == null) {
                deserializeEx = new ExceptionInfo(name, "没有有效的构造函数");
            }
        }
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].getField() == null) {
                defaultSerializeEx = new ExceptionInfo(
                    name, "声明了不匹配的可序列化字段");
            }
        }
        initialized = true;
    }

    /**
     * 创建一个空白的类描述符，该描述符应通过后续调用 initProxy()、initNonProxy() 或 readNonProxy() 进行初始化。
     */
    ObjectStreamClass() {
    }

    /**
     * 创建一个不授予任何权限的 PermissionDomain。
     */
    private ProtectionDomain noPermissionsDomain() {
        PermissionCollection perms = new Permissions();
        perms.setReadOnly();
        return new ProtectionDomain(null, perms);
    }

    /**
     * 聚合所有将具体类 {@code cl} 与其祖先类声明的构造函数 {@code cons} 分开的 ProtectionDomains。
     *
     * 如果 {@code cl} 由引导加载器定义，或者构造函数 {@code cons} 由 {@code cl} 声明，或者没有安全管理器，
     * 那么此方法不执行任何操作并返回 {@code null}。
     *
     * @param cons 由 {@code cl} 或其祖先之一声明的构造函数。
     * @param cl 具体类，可以是声明构造函数 {@code cons} 的类，也可以是该类的可序列化子类。
     * @return 一个 ProtectionDomain 数组，表示将具体类 {@code cl} 与其祖先类声明的 {@code cons} 分开的 ProtectionDomain 集合，或 {@code null}。
     */
    private ProtectionDomain[] getProtectionDomains(Constructor<?> cons,
                                                    Class<?> cl) {
        ProtectionDomain[] domains = null;
        if (cons != null && cl.getClassLoader() != null
                && System.getSecurityManager() != null) {
            Class<?> cls = cl;
            Class<?> fnscl = cons.getDeclaringClass();
            Set<ProtectionDomain> pds = null;
            while (cls != fnscl) {
                ProtectionDomain pd = cls.getProtectionDomain();
                if (pd != null) {
                    if (pds == null) pds = new HashSet<>();
                    pds.add(pd);
                }
                cls = cls.getSuperclass();
                if (cls == null) {
                    // 这不应该发生
                    // 创建一个没有权限的 ProtectionDomain。
                    // 我们应该抛出异常吗？
                    if (pds == null) pds = new HashSet<>();
                    else pds.clear();
                    pds.add(noPermissionsDomain());
                    break;
                }
            }
            if (pds != null) {
                domains = pds.toArray(new ProtectionDomain[0]);
            }
        }
        return domains;
    }

    /**
     * 初始化表示代理类的类描述符。
     */
    void initProxy(Class<?> cl,
                   ClassNotFoundException resolveEx,
                   ObjectStreamClass superDesc)
        throws InvalidClassException
    {
        ObjectStreamClass osc = null;
        if (cl != null) {
            osc = lookup(cl, true);
            if (!osc.isProxy) {
                throw new InvalidClassException(
                    "无法将代理描述符绑定到非代理类");
            }
        }
        this.cl = cl;
        this.resolveEx = resolveEx;
        this.superDesc = superDesc;
        isProxy = true;
        serializable = true;
        suid = Long.valueOf(0);
        fields = NO_FIELDS;
        if (osc != null) {
            localDesc = osc;
            name = localDesc.name;
            externalizable = localDesc.externalizable;
            writeReplaceMethod = localDesc.writeReplaceMethod;
            readResolveMethod = localDesc.readResolveMethod;
            deserializeEx = localDesc.deserializeEx;
            domains = localDesc.domains;
            cons = localDesc.cons;
        }
        fieldRefl = getReflector(fields, localDesc);
        initialized = true;
    }

    /**
     * 初始化表示非代理类的类描述符。
     */
    void initNonProxy(ObjectStreamClass model,
                      Class<?> cl,
                      ClassNotFoundException resolveEx,
                      ObjectStreamClass superDesc)
        throws InvalidClassException
    {
        long suid = Long.valueOf(model.getSerialVersionUID());
        ObjectStreamClass osc = null;
        if (cl != null) {
            osc = lookup(cl, true);
            if (osc.isProxy) {
                throw new InvalidClassException(
                        "无法将非代理描述符绑定到代理类");
            }
            if (model.isEnum != osc.isEnum) {
                throw new InvalidClassException(model.isEnum ?
                        "无法将枚举描述符绑定到非枚举类" :
                        "无法将非枚举描述符绑定到枚举类");
            }

            if (model.serializable == osc.serializable &&
                    !cl.isArray() &&
                    suid != osc.getSerialVersionUID()) {
                throw new InvalidClassException(osc.name,
                        "本地类不兼容: " +
                                "流类描述符的 serialVersionUID = " + suid +
                                ", 本地类的 serialVersionUID = " +
                                osc.getSerialVersionUID());
            }

            if (!classNamesEqual(model.name, osc.name)) {
                throw new InvalidClassException(osc.name,
                        "本地类名与流类名不兼容 " +
                                "名称 \"" + model.name + "\"");
            }


                        if (!model.isEnum) {
                if ((model.serializable == osc.serializable) &&
                        (model.externalizable != osc.externalizable)) {
                    throw new InvalidClassException(osc.name,
                            "Serializable incompatible with Externalizable");
                }

                if ((model.serializable != osc.serializable) ||
                        (model.externalizable != osc.externalizable) ||
                        !(model.serializable || model.externalizable)) {
                    deserializeEx = new ExceptionInfo(
                            osc.name, "class invalid for deserialization");
                }
            }
        }

        this.cl = cl;
        this.resolveEx = resolveEx;
        this.superDesc = superDesc;
        name = model.name;
        this.suid = suid;
        isProxy = false;
        isEnum = model.isEnum;
        serializable = model.serializable;
        externalizable = model.externalizable;
        hasBlockExternalData = model.hasBlockExternalData;
        hasWriteObjectData = model.hasWriteObjectData;
        fields = model.fields;
        primDataSize = model.primDataSize;
        numObjFields = model.numObjFields;

        if (osc != null) {
            localDesc = osc;
            writeObjectMethod = localDesc.writeObjectMethod;
            readObjectMethod = localDesc.readObjectMethod;
            readObjectNoDataMethod = localDesc.readObjectNoDataMethod;
            writeReplaceMethod = localDesc.writeReplaceMethod;
            readResolveMethod = localDesc.readResolveMethod;
            if (deserializeEx == null) {
                deserializeEx = localDesc.deserializeEx;
            }
            domains = localDesc.domains;
            cons = localDesc.cons;
        }

        fieldRefl = getReflector(fields, localDesc);
        // 重新分配给匹配的字段，以便反映本地未共享的设置
        fields = fieldRefl.getFields();
        initialized = true;
    }

    /**
     * 从给定的输入流中读取非代理类描述符信息。
     * 结果类描述符不是完全功能性的；它只能用作 ObjectInputStream.resolveClass() 和
     * ObjectStreamClass.initNonProxy() 方法的输入。
     */
    void readNonProxy(ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        name = in.readUTF();
        suid = Long.valueOf(in.readLong());
        isProxy = false;

        byte flags = in.readByte();
        hasWriteObjectData =
            ((flags & ObjectStreamConstants.SC_WRITE_METHOD) != 0);
        hasBlockExternalData =
            ((flags & ObjectStreamConstants.SC_BLOCK_DATA) != 0);
        externalizable =
            ((flags & ObjectStreamConstants.SC_EXTERNALIZABLE) != 0);
        boolean sflag =
            ((flags & ObjectStreamConstants.SC_SERIALIZABLE) != 0);
        if (externalizable && sflag) {
            throw new InvalidClassException(
                name, "serializable and externalizable flags conflict");
        }
        serializable = externalizable || sflag;
        isEnum = ((flags & ObjectStreamConstants.SC_ENUM) != 0);
        if (isEnum && suid.longValue() != 0L) {
            throw new InvalidClassException(name,
                "enum descriptor has non-zero serialVersionUID: " + suid);
        }

        int numFields = in.readShort();
        if (isEnum && numFields != 0) {
            throw new InvalidClassException(name,
                "enum descriptor has non-zero field count: " + numFields);
        }
        fields = (numFields > 0) ?
            new ObjectStreamField[numFields] : NO_FIELDS;
        for (int i = 0; i < numFields; i++) {
            char tcode = (char) in.readByte();
            String fname = in.readUTF();
            String signature = ((tcode == 'L') || (tcode == '[')) ?
                in.readTypeString() : new String(new char[] { tcode });
            try {
                fields[i] = new ObjectStreamField(fname, signature, false);
            } catch (RuntimeException e) {
                throw (IOException) new InvalidClassException(name,
                    "invalid descriptor for field " + fname).initCause(e);
            }
        }
        computeFieldOffsets();
    }

    /**
     * 将非代理类描述符信息写入给定的输出流。
     */
    void writeNonProxy(ObjectOutputStream out) throws IOException {
        out.writeUTF(name);
        out.writeLong(getSerialVersionUID());

        byte flags = 0;
        if (externalizable) {
            flags |= ObjectStreamConstants.SC_EXTERNALIZABLE;
            int protocol = out.getProtocolVersion();
            if (protocol != ObjectStreamConstants.PROTOCOL_VERSION_1) {
                flags |= ObjectStreamConstants.SC_BLOCK_DATA;
            }
        } else if (serializable) {
            flags |= ObjectStreamConstants.SC_SERIALIZABLE;
        }
        if (hasWriteObjectData) {
            flags |= ObjectStreamConstants.SC_WRITE_METHOD;
        }
        if (isEnum) {
            flags |= ObjectStreamConstants.SC_ENUM;
        }
        out.writeByte(flags);

        out.writeShort(fields.length);
        for (int i = 0; i < fields.length; i++) {
            ObjectStreamField f = fields[i];
            out.writeByte(f.getTypeCode());
            out.writeUTF(f.getName());
            if (!f.isPrimitive()) {
                out.writeTypeString(f.getTypeString());
            }
        }
    }

    /**
     * 返回在尝试解析与此类描述符对应的本地类时抛出的 ClassNotFoundException（如果有）。
     */
    ClassNotFoundException getResolveException() {
        return resolveEx;
    }

    /**
     * 如果未初始化，则抛出 InternalError。
     */
    private final void requireInitialized() {
        if (!initialized)
            throw new InternalError("Unexpected call when not initialized");
    }

    /**
     * 如果未初始化，则抛出 InvalidClassException。
     * 在未初始化的类描述符表示序列化流中的问题时调用。
     */
    final void checkInitialized() throws InvalidClassException {
        if (!initialized) {
            throw new InvalidClassException("Class descriptor should be initialized");
        }
    }


    /**
     * 如果引用此类描述符的对象实例不应允许反序列化，则抛出 InvalidClassException。此方法不适用于枚举常量的反序列化。
     */
    void checkDeserialize() throws InvalidClassException {
        requireInitialized();
        if (deserializeEx != null) {
            throw deserializeEx.newInvalidClassException();
        }
    }

    /**
     * 如果此类描述符表示的类的对象不应允许序列化，则抛出 InvalidClassException。此方法不适用于枚举常量的序列化。
     */
    void checkSerialize() throws InvalidClassException {
        requireInitialized();
        if (serializeEx != null) {
            throw serializeEx.newInvalidClassException();
        }
    }

    /**
     * 如果此类描述符表示的类的对象不应被允许使用默认序列化（例如，如果类声明了不可对应实际字段的可序列化字段，因此必须使用 GetField API），则抛出 InvalidClassException。此方法不适用于枚举常量的反序列化。
     */
    void checkDefaultSerialize() throws InvalidClassException {
        requireInitialized();
        if (defaultSerializeEx != null) {
            throw defaultSerializeEx.newInvalidClassException();
        }
    }

    /**
     * 返回超类描述符。请注意，在接收方，超类描述符可能绑定到一个不是子类描述符绑定类的超类的类。
     */
    ObjectStreamClass getSuperDesc() {
        requireInitialized();
        return superDesc;
    }

    /**
     * 返回与此类描述符关联的类的“本地”类描述符（即 ObjectStreamClass.lookup(this.forClass()) 的结果），如果没有类与此描述符关联，则返回 null。
     */
    ObjectStreamClass getLocalDesc() {
        requireInitialized();
        return localDesc;
    }

    /**
     * 返回表示所表示类的可序列化字段的 ObjectStreamFields 数组。如果 copy 为 true，则返回此类描述符字段数组的克隆，否则返回数组本身。
     */
    ObjectStreamField[] getFields(boolean copy) {
        return copy ? fields.clone() : fields;
    }

    /**
     * 通过名称和类型查找所表示类的可序列化字段。指定的类型为 null 匹配所有类型，Object.class 匹配所有非基本类型，任何其他非 null 类型仅匹配可赋值类型。返回匹配的字段，如果没有找到匹配项，则返回 null。
     */
    ObjectStreamField getField(String name, Class<?> type) {
        for (int i = 0; i < fields.length; i++) {
            ObjectStreamField f = fields[i];
            if (f.getName().equals(name)) {
                if (type == null ||
                    (type == Object.class && !f.isPrimitive()))
                {
                    return f;
                }
                Class<?> ftype = f.getType();
                if (ftype != null && type.isAssignableFrom(ftype)) {
                    return f;
                }
            }
        }
        return null;
    }

    /**
     * 如果类描述符表示动态代理类，则返回 true，否则返回 false。
     */
    boolean isProxy() {
        requireInitialized();
        return isProxy;
    }

    /**
     * 如果类描述符表示枚举类型，则返回 true，否则返回 false。
     */
    boolean isEnum() {
        requireInitialized();
        return isEnum;
    }

    /**
     * 如果所表示的类实现了 Externalizable，则返回 true，否则返回 false。
     */
    boolean isExternalizable() {
        requireInitialized();
        return externalizable;
    }

    /**
     * 如果所表示的类实现了 Serializable，则返回 true，否则返回 false。
     */
    boolean isSerializable() {
        requireInitialized();
        return serializable;
    }

    /**
     * 如果类描述符表示以 1.2（块数据）格式写入其数据的可外部化类，则返回 true，否则返回 false。
     */
    boolean hasBlockExternalData() {
        requireInitialized();
        return hasBlockExternalData;
    }

    /**
     * 如果所表示的类是可序列化的（但不是可外部化的），并且通过自定义的 writeObject() 方法写入其数据，则返回 true，否则返回 false。
     */
    boolean hasWriteObjectData() {
        requireInitialized();
        return hasWriteObjectData;
    }

    /**
     * 如果类描述符表示可序列化/可外部化的类，并且可以由序列化运行时实例化——即，如果它是可外部化的并且定义了公共无参构造函数，或者如果它是非外部化的并且其第一个非可序列化的超类定义了可访问的无参构造函数。否则，返回 false。
     */
    boolean isInstantiable() {
        requireInitialized();
        return (cons != null);
    }

    /**
     * 如果所表示的类是可序列化的（但不是可外部化的），并且定义了符合要求的 writeObject 方法，则返回 true，否则返回 false。
     */
    boolean hasWriteObjectMethod() {
        requireInitialized();
        return (writeObjectMethod != null);
    }

    /**
     * 如果所表示的类是可序列化的（但不是可外部化的），并且定义了符合要求的 readObject 方法，则返回 true，否则返回 false。
     */
    boolean hasReadObjectMethod() {
        requireInitialized();
        return (readObjectMethod != null);
    }

    /**
     * 如果所表示的类是可序列化的（但不是可外部化的），并且定义了符合要求的 readObjectNoData 方法，则返回 true，否则返回 false。
     */
    boolean hasReadObjectNoDataMethod() {
        requireInitialized();
        return (readObjectNoDataMethod != null);
    }

                /**
     * 如果表示的类是可序列化的或可外部化的，并且定义了一个符合规范的 writeReplace 方法，则返回 true。否则，返回 false。
     */
    boolean hasWriteReplaceMethod() {
        requireInitialized();
        return (writeReplaceMethod != null);
    }

    /**
     * 如果表示的类是可序列化的或可外部化的，并且定义了一个符合规范的 readResolve 方法，则返回 true。否则，返回 false。
     */
    boolean hasReadResolveMethod() {
        requireInitialized();
        return (readResolveMethod != null);
    }

    /**
     * 创建表示类的新实例。如果类是可外部化的，则调用其公共无参数构造函数；否则，如果类是可序列化的，则调用第一个非可序列化超类的无参数构造函数。如果此类描述符未与类关联，关联的类是非可序列化的，或者适当的无参数构造函数不可访问/不可用，则抛出 UnsupportedOperationException。
     */
    Object newInstance()
        throws InstantiationException, InvocationTargetException,
               UnsupportedOperationException
    {
        requireInitialized();
        if (cons != null) {
            try {
                if (domains == null || domains.length == 0) {
                    return cons.newInstance();
                } else {
                    JavaSecurityAccess jsa = SharedSecrets.getJavaSecurityAccess();
                    PrivilegedAction<?> pea = () -> {
                        try {
                            return cons.newInstance();
                        } catch (InstantiationException
                                 | InvocationTargetException
                                 | IllegalAccessException x) {
                            throw new UndeclaredThrowableException(x);
                        }
                    }; // 不能使用 PrivilegedExceptionAction 与 jsa
                    try {
                        return jsa.doIntersectionPrivilege(pea,
                                   AccessController.getContext(),
                                   new AccessControlContext(domains));
                    } catch (UndeclaredThrowableException x) {
                        Throwable cause = x.getCause();
                        if (cause instanceof InstantiationException)
                            throw (InstantiationException) cause;
                        if (cause instanceof InvocationTargetException)
                            throw (InvocationTargetException) cause;
                        if (cause instanceof IllegalAccessException)
                            throw (IllegalAccessException) cause;
                        // 不应该发生
                        throw x;
                    }
                }
            } catch (IllegalAccessException ex) {
                // 不应该发生，因为访问检查已被抑制
                throw new InternalError(ex);
            } catch (InstantiationError err) {
                InstantiationException ex = new InstantiationException();
                ex.initCause(err);
                throw ex;
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * 调用表示的可序列化类的 writeObject 方法。如果此类描述符未与类关联，或者类是可外部化的、非可序列化的，或者未定义 writeObject，则抛出 UnsupportedOperationException。
     */
    void invokeWriteObject(Object obj, ObjectOutputStream out)
        throws IOException, UnsupportedOperationException
    {
        requireInitialized();
        if (writeObjectMethod != null) {
            try {
                writeObjectMethod.invoke(obj, new Object[]{ out });
            } catch (InvocationTargetException ex) {
                Throwable th = ex.getTargetException();
                if (th instanceof IOException) {
                    throw (IOException) th;
                } else {
                    throwMiscException(th);
                }
            } catch (IllegalAccessException ex) {
                // 不应该发生，因为访问检查已被抑制
                throw new InternalError(ex);
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * 调用表示的可序列化类的 readObject 方法。如果此类描述符未与类关联，或者类是可外部化的、非可序列化的，或者未定义 readObject，则抛出 UnsupportedOperationException。
     */
    void invokeReadObject(Object obj, ObjectInputStream in)
        throws ClassNotFoundException, IOException,
               UnsupportedOperationException
    {
        requireInitialized();
        if (readObjectMethod != null) {
            try {
                readObjectMethod.invoke(obj, new Object[]{ in });
            } catch (InvocationTargetException ex) {
                Throwable th = ex.getTargetException();
                if (th instanceof ClassNotFoundException) {
                    throw (ClassNotFoundException) th;
                } else if (th instanceof IOException) {
                    throw (IOException) th;
                } else {
                    throwMiscException(th);
                }
            } catch (IllegalAccessException ex) {
                // 不应该发生，因为访问检查已被抑制
                throw new InternalError(ex);
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * 调用表示的可序列化类的 readObjectNoData 方法。如果此类描述符未与类关联，或者类是可外部化的、非可序列化的，或者未定义 readObjectNoData，则抛出 UnsupportedOperationException。
     */
    void invokeReadObjectNoData(Object obj)
        throws IOException, UnsupportedOperationException
    {
        requireInitialized();
        if (readObjectNoDataMethod != null) {
            try {
                readObjectNoDataMethod.invoke(obj, (Object[]) null);
            } catch (InvocationTargetException ex) {
                Throwable th = ex.getTargetException();
                if (th instanceof ObjectStreamException) {
                    throw (ObjectStreamException) th;
                } else {
                    throwMiscException(th);
                }
            } catch (IllegalAccessException ex) {
                // 不应该发生，因为访问检查已被抑制
                throw new InternalError(ex);
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }


                /**
     * 调用表示的可序列化类的 writeReplace 方法并返回结果。如果此类描述符未与类关联，或者类不可序列化或未定义 writeReplace，则抛出 UnsupportedOperationException。
     */
    Object invokeWriteReplace(Object obj)
        throws IOException, UnsupportedOperationException
    {
        requireInitialized();
        if (writeReplaceMethod != null) {
            try {
                return writeReplaceMethod.invoke(obj, (Object[]) null);
            } catch (InvocationTargetException ex) {
                Throwable th = ex.getTargetException();
                if (th instanceof ObjectStreamException) {
                    throw (ObjectStreamException) th;
                } else {
                    throwMiscException(th);
                    throw new InternalError(th);  // never reached
                }
            } catch (IllegalAccessException ex) {
                // 应该不会发生，因为访问检查已被抑制
                throw new InternalError(ex);
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * 调用表示的可序列化类的 readResolve 方法并返回结果。如果此类描述符未与类关联，或者类不可序列化或未定义 readResolve，则抛出 UnsupportedOperationException。
     */
    Object invokeReadResolve(Object obj)
        throws IOException, UnsupportedOperationException
    {
        requireInitialized();
        if (readResolveMethod != null) {
            try {
                return readResolveMethod.invoke(obj, (Object[]) null);
            } catch (InvocationTargetException ex) {
                Throwable th = ex.getTargetException();
                if (th instanceof ObjectStreamException) {
                    throw (ObjectStreamException) th;
                } else {
                    throwMiscException(th);
                    throw new InternalError(th);  // never reached
                }
            } catch (IllegalAccessException ex) {
                // 应该不会发生，因为访问检查已被抑制
                throw new InternalError(ex);
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * 表示对象的序列化形式中由给定类描述符描述的数据部分。如果 "hasData" 为 false，则对象的序列化形式不包含与类描述符关联的数据。
     */
    static class ClassDataSlot {

        /** 占用此槽的类描述符 */
        final ObjectStreamClass desc;
        /** 序列化形式是否包含此槽描述符的数据 */
        final boolean hasData;

        ClassDataSlot(ObjectStreamClass desc, boolean hasData) {
            this.desc = desc;
            this.hasData = hasData;
        }
    }

    /**
     * 返回表示此类描述符描述的序列化对象的数据布局（包括超类数据）的 ClassDataSlot 实例数组。ClassDataSlots 按继承顺序排列，其中包含“较高”超类的 ClassDataSlots 优先。最后一个 ClassDataSlot 包含对此描述符的引用。
     */
    ClassDataSlot[] getClassDataLayout() throws InvalidClassException {
        // REMIND: synchronize instead of relying on volatile?
        if (dataLayout == null) {
            dataLayout = getClassDataLayout0();
        }
        return dataLayout;
    }

    private ClassDataSlot[] getClassDataLayout0()
        throws InvalidClassException
    {
        ArrayList<ClassDataSlot> slots = new ArrayList<>();
        Class<?> start = cl, end = cl;

        // 查找最近的非可序列化超类
        while (end != null && Serializable.class.isAssignableFrom(end)) {
            end = end.getSuperclass();
        }

        HashSet<String> oscNames = new HashSet<>(3);

        for (ObjectStreamClass d = this; d != null; d = d.superDesc) {
            if (oscNames.contains(d.name)) {
                throw new InvalidClassException("循环引用。");
            } else {
                oscNames.add(d.name);
            }

            // 在继承层次结构中搜索具有匹配名称的类
            String searchName = (d.cl != null) ? d.cl.getName() : d.name;
            Class<?> match = null;
            for (Class<?> c = start; c != end; c = c.getSuperclass()) {
                if (searchName.equals(c.getName())) {
                    match = c;
                    break;
                }
            }

            // 为 match 以下的每个未匹配类添加“无数据”槽
            if (match != null) {
                for (Class<?> c = start; c != match; c = c.getSuperclass()) {
                    slots.add(new ClassDataSlot(
                        ObjectStreamClass.lookup(c, true), false));
                }
                start = match.getSuperclass();
            }

            // 记录描述符/类对
            slots.add(new ClassDataSlot(d.getVariantFor(match), true));
        }

        // 为任何剩余的未匹配类添加“无数据”槽
        for (Class<?> c = start; c != end; c = c.getSuperclass()) {
            slots.add(new ClassDataSlot(
                ObjectStreamClass.lookup(c, true), false));
        }

        // 按从超类到子类的顺序排列槽
        Collections.reverse(slots);
        return slots.toArray(new ClassDataSlot[slots.size()]);
    }

    /**
     * 返回表示类的原始字段值的序列化大小（以字节为单位）。
     */
    int getPrimDataSize() {
        return primDataSize;
    }

    /**
     * 返回表示类的非原始可序列化字段的数量。
     */
    int getNumObjFields() {
        return numObjFields;
    }

    /**
     * 获取对象 obj 的可序列化原始字段值，并将它们序列化到从偏移量 0 开始的字节数组 buf 中。如果 obj 非空，调用者有责任确保 obj 是正确的类型。
     */
    void getPrimFieldValues(Object obj, byte[] buf) {
        fieldRefl.getPrimFieldValues(obj, buf);
    }

                /**
     * 设置对象 obj 的可序列化基本字段，使用从字节数组 buf 开始偏移量为 0 的值进行反序列化。调用者有责任确保 obj 是正确的类型（如果非空）。
     */
    void setPrimFieldValues(Object obj, byte[] buf) {
        fieldRefl.setPrimFieldValues(obj, buf);
    }

    /**
     * 获取对象 obj 的可序列化对象字段值，并将它们存储在从偏移量 0 开始的数组 vals 中。调用者有责任确保 obj 是正确的类型（如果非空）。
     */
    void getObjFieldValues(Object obj, Object[] vals) {
        fieldRefl.getObjFieldValues(obj, vals);
    }

    /**
     * 使用从数组 vals 开始偏移量为 0 的值设置对象 obj 的可序列化对象字段。调用者有责任确保 obj 是正确的类型（如果非空）。
     */
    void setObjFieldValues(Object obj, Object[] vals) {
        fieldRefl.setObjFieldValues(obj, vals);
    }

    /**
     * 计算并设置可序列化字段的偏移量，以及基本数据大小和对象字段计数的总和。如果字段顺序非法，则抛出 InvalidClassException。
     */
    private void computeFieldOffsets() throws InvalidClassException {
        primDataSize = 0;
        numObjFields = 0;
        int firstObjIndex = -1;

        for (int i = 0; i < fields.length; i++) {
            ObjectStreamField f = fields[i];
            switch (f.getTypeCode()) {
                case 'Z':
                case 'B':
                    f.setOffset(primDataSize++);
                    break;

                case 'C':
                case 'S':
                    f.setOffset(primDataSize);
                    primDataSize += 2;
                    break;

                case 'I':
                case 'F':
                    f.setOffset(primDataSize);
                    primDataSize += 4;
                    break;

                case 'J':
                case 'D':
                    f.setOffset(primDataSize);
                    primDataSize += 8;
                    break;

                case '[':
                case 'L':
                    f.setOffset(numObjFields++);
                    if (firstObjIndex == -1) {
                        firstObjIndex = i;
                    }
                    break;

                default:
                    throw new InternalError();
            }
        }
        if (firstObjIndex != -1 &&
            firstObjIndex + numObjFields != fields.length)
        {
            throw new InvalidClassException(name, "非法字段顺序");
        }
    }

    /**
     * 如果给定的类与该类描述符关联的类相同，则返回此类描述符的引用。否则，返回与此类描述符绑定到给定类的变体。
     */
    private ObjectStreamClass getVariantFor(Class<?> cl)
        throws InvalidClassException
    {
        if (this.cl == cl) {
            return this;
        }
        ObjectStreamClass desc = new ObjectStreamClass();
        if (isProxy) {
            desc.initProxy(cl, null, superDesc);
        } else {
            desc.initNonProxy(this, cl, null, superDesc);
        }
        return desc;
    }

    /**
     * 返回给定类的公共无参构造函数，如果没有找到则返回 null。返回的构造函数（如果有）的访问检查被禁用，因为定义类可能仍然是非公共的。
     */
    private static Constructor<?> getExternalizableConstructor(Class<?> cl) {
        try {
            Constructor<?> cons = cl.getDeclaredConstructor((Class<?>[]) null);
            cons.setAccessible(true);
            return ((cons.getModifiers() & Modifier.PUBLIC) != 0) ?
                cons : null;
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }

    /**
     * 给定一个类，确定其超类是否有从该类可访问的构造函数。这是一个特殊目的的方法，旨在为可序列化类及其超类进行访问检查，直到但不包括第一个非可序列化的超类。这也意味着超类总是非空的，因为可序列化类必须是一个类（而不是接口），而 Object 不是可序列化的。
     *
     * @param cl 从该类进行访问检查
     * @return 超类是否有从 cl 可访问的构造函数
     */
    private static boolean superHasAccessibleConstructor(Class<?> cl) {
        Class<?> superCl = cl.getSuperclass();
        assert Serializable.class.isAssignableFrom(cl);
        assert superCl != null;
        if (packageEquals(cl, superCl)) {
            // 如果找到任何非私有构造函数，则可访问
            for (Constructor<?> ctor : superCl.getDeclaredConstructors()) {
                if ((ctor.getModifiers() & Modifier.PRIVATE) == 0) {
                    return true;
                }
            }
            return false;
        } else {
            // 检查父类是否为受保护或公共
            if ((superCl.getModifiers() & (Modifier.PROTECTED | Modifier.PUBLIC)) == 0) {
                return false;
            }
            // 如果任何构造函数为受保护或公共，则可访问
            for (Constructor<?> ctor : superCl.getDeclaredConstructors()) {
                if ((ctor.getModifiers() & (Modifier.PROTECTED | Modifier.PUBLIC)) != 0) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * 返回第一个非可序列化超类的子类可访问的无参构造函数，如果没有找到则返回 null。返回的构造函数（如果有）的访问检查被禁用。
     */
    private static Constructor<?> getSerializableConstructor(Class<?> cl) {
        Class<?> initCl = cl;
        while (Serializable.class.isAssignableFrom(initCl)) {
            Class<?> prev = initCl;
            if ((initCl = initCl.getSuperclass()) == null ||
                (!disableSerialConstructorChecks && !superHasAccessibleConstructor(prev))) {
                return null;
            }
        }
        try {
            Constructor<?> cons = initCl.getDeclaredConstructor((Class<?>[]) null);
            int mods = cons.getModifiers();
            if ((mods & Modifier.PRIVATE) != 0 ||
                ((mods & (Modifier.PUBLIC | Modifier.PROTECTED)) == 0 &&
                 !packageEquals(cl, initCl)))
            {
                return null;
            }
            cons = reflFactory.newConstructorForSerialization(cl, cons);
            cons.setAccessible(true);
            return cons;
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }

                /**
     * 返回给定类定义或通过继承可访问的具有给定签名的非静态、非抽象方法，如果未找到匹配项，则返回 null。
     * 返回的方法（如果有）将禁用访问检查。
     */
    private static Method getInheritableMethod(Class<?> cl, String name,
                                               Class<?>[] argTypes,
                                               Class<?> returnType)
    {
        Method meth = null;
        Class<?> defCl = cl;
        while (defCl != null) {
            try {
                meth = defCl.getDeclaredMethod(name, argTypes);
                break;
            } catch (NoSuchMethodException ex) {
                defCl = defCl.getSuperclass();
            }
        }

        if ((meth == null) || (meth.getReturnType() != returnType)) {
            return null;
        }
        meth.setAccessible(true);
        int mods = meth.getModifiers();
        if ((mods & (Modifier.STATIC | Modifier.ABSTRACT)) != 0) {
            return null;
        } else if ((mods & (Modifier.PUBLIC | Modifier.PROTECTED)) != 0) {
            return meth;
        } else if ((mods & Modifier.PRIVATE) != 0) {
            return (cl == defCl) ? meth : null;
        } else {
            return packageEquals(cl, defCl) ? meth : null;
        }
    }

    /**
     * 返回给定类定义的具有给定签名的非静态私有方法，如果未找到，则返回 null。
     * 返回的方法（如果有）将禁用访问检查。
     */
    private static Method getPrivateMethod(Class<?> cl, String name,
                                           Class<?>[] argTypes,
                                           Class<?> returnType)
    {
        try {
            Method meth = cl.getDeclaredMethod(name, argTypes);
            meth.setAccessible(true);
            int mods = meth.getModifiers();
            return ((meth.getReturnType() == returnType) &&
                    ((mods & Modifier.STATIC) == 0) &&
                    ((mods & Modifier.PRIVATE) != 0)) ? meth : null;
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }

    /**
     * 如果类定义在相同的运行时包中，则返回 true，否则返回 false。
     */
    private static boolean packageEquals(Class<?> cl1, Class<?> cl2) {
        return (cl1.getClassLoader() == cl2.getClassLoader() &&
                getPackageName(cl1).equals(getPackageName(cl2)));
    }

    /**
     * 返回给定类的包名。
     */
    private static String getPackageName(Class<?> cl) {
        String s = cl.getName();
        int i = s.lastIndexOf('[');
        if (i >= 0) {
            s = s.substring(i + 2);
        }
        i = s.lastIndexOf('.');
        return (i >= 0) ? s.substring(0, i) : "";
    }

    /**
     * 比较类名是否相等，忽略包名。如果类名相等，则返回 true，否则返回 false。
     */
    private static boolean classNamesEqual(String name1, String name2) {
        name1 = name1.substring(name1.lastIndexOf('.') + 1);
        name2 = name2.substring(name2.lastIndexOf('.') + 1);
        return name1.equals(name2);
    }

    /**
     * 返回给定类的 JVM 类型签名。
     */
    private static String getClassSignature(Class<?> cl) {
        StringBuilder sbuf = new StringBuilder();
        while (cl.isArray()) {
            sbuf.append('[');
            cl = cl.getComponentType();
        }
        if (cl.isPrimitive()) {
            if (cl == Integer.TYPE) {
                sbuf.append('I');
            } else if (cl == Byte.TYPE) {
                sbuf.append('B');
            } else if (cl == Long.TYPE) {
                sbuf.append('J');
            } else if (cl == Float.TYPE) {
                sbuf.append('F');
            } else if (cl == Double.TYPE) {
                sbuf.append('D');
            } else if (cl == Short.TYPE) {
                sbuf.append('S');
            } else if (cl == Character.TYPE) {
                sbuf.append('C');
            } else if (cl == Boolean.TYPE) {
                sbuf.append('Z');
            } else if (cl == Void.TYPE) {
                sbuf.append('V');
            } else {
                throw new InternalError();
            }
        } else {
            sbuf.append('L' + cl.getName().replace('.', '/') + ';');
        }
        return sbuf.toString();
    }

    /**
     * 返回给定参数列表和返回类型的 JVM 类型签名。
     */
    private static String getMethodSignature(Class<?>[] paramTypes,
                                             Class<?> retType)
    {
        StringBuilder sbuf = new StringBuilder();
        sbuf.append('(');
        for (int i = 0; i < paramTypes.length; i++) {
            sbuf.append(getClassSignature(paramTypes[i]));
        }
        sbuf.append(')');
        sbuf.append(getClassSignature(retType));
        return sbuf.toString();
    }

    /**
     * 用于抛出一个异常，该异常可以是 RuntimeException、Error 或某些意外类型（在这种情况下，它将被包装在 IOException 中）。
     */
    private static void throwMiscException(Throwable th) throws IOException {
        if (th instanceof RuntimeException) {
            throw (RuntimeException) th;
        } else if (th instanceof Error) {
            throw (Error) th;
        } else {
            IOException ex = new IOException("unexpected exception type");
            ex.initCause(th);
            throw ex;
        }
    }

    /**
     * 返回描述给定类的可序列化字段的 ObjectStreamField 数组。由类的实际字段支持的可序列化字段由具有相应非空 Field 对象的 ObjectStreamFields 表示。
     * 如果（显式声明的）可序列化字段无效，则抛出 InvalidClassException。
     */
    private static ObjectStreamField[] getSerialFields(Class<?> cl)
        throws InvalidClassException
    {
        ObjectStreamField[] fields;
        if (Serializable.class.isAssignableFrom(cl) &&
            !Externalizable.class.isAssignableFrom(cl) &&
            !Proxy.isProxyClass(cl) &&
            !cl.isInterface())
        {
            if ((fields = getDeclaredSerialFields(cl)) == null) {
                fields = getDefaultSerialFields(cl);
            }
            Arrays.sort(fields);
        } else {
            fields = NO_FIELDS;
        }
        return fields;
    }


                /**
     * 返回给定类中显式定义的“serialPersistentFields”字段表示的可序列化字段，或如果未定义合适的
     * “serialPersistentFields”字段，则返回null。由类的实际字段支持的可序列化字段由具有相应非null Field对象的
     * ObjectStreamFields表示。为了与过去的版本兼容，具有null值的“serialPersistentFields”字段
     * 被视为未声明“serialPersistentFields”。如果声明的可序列化字段无效（例如，多个字段共享同一个名称），
     * 则抛出InvalidClassException。
     */
    private static ObjectStreamField[] getDeclaredSerialFields(Class<?> cl)
        throws InvalidClassException
    {
        ObjectStreamField[] serialPersistentFields = null;
        try {
            Field f = cl.getDeclaredField("serialPersistentFields");
            int mask = Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL;
            if ((f.getModifiers() & mask) == mask) {
                f.setAccessible(true);
                serialPersistentFields = (ObjectStreamField[]) f.get(null);
            }
        } catch (Exception ex) {
        }
        if (serialPersistentFields == null) {
            return null;
        } else if (serialPersistentFields.length == 0) {
            return NO_FIELDS;
        }

        ObjectStreamField[] boundFields =
            new ObjectStreamField[serialPersistentFields.length];
        Set<String> fieldNames = new HashSet<>(serialPersistentFields.length);

        for (int i = 0; i < serialPersistentFields.length; i++) {
            ObjectStreamField spf = serialPersistentFields[i];

            String fname = spf.getName();
            if (fieldNames.contains(fname)) {
                throw new InvalidClassException(
                    "多个可序列化字段名为 " + fname);
            }
            fieldNames.add(fname);

            try {
                Field f = cl.getDeclaredField(fname);
                if ((f.getType() == spf.getType()) &&
                    ((f.getModifiers() & Modifier.STATIC) == 0))
                {
                    boundFields[i] =
                        new ObjectStreamField(f, spf.isUnshared(), true);
                }
            } catch (NoSuchFieldException ex) {
            }
            if (boundFields[i] == null) {
                boundFields[i] = new ObjectStreamField(
                    fname, spf.getType(), spf.isUnshared());
            }
        }
        return boundFields;
    }

    /**
     * 返回一个ObjectStreamFields数组，对应于给定类声明的所有非静态非瞬态字段。每个ObjectStreamField
     * 包含一个表示其字段的Field对象。如果没有默认的可序列化字段，则返回NO_FIELDS。
     */
    private static ObjectStreamField[] getDefaultSerialFields(Class<?> cl) {
        Field[] clFields = cl.getDeclaredFields();
        ArrayList<ObjectStreamField> list = new ArrayList<>();
        int mask = Modifier.STATIC | Modifier.TRANSIENT;

        for (int i = 0; i < clFields.length; i++) {
            if ((clFields[i].getModifiers() & mask) == 0) {
                list.add(new ObjectStreamField(clFields[i], false, true));
            }
        }
        int size = list.size();
        return (size == 0) ? NO_FIELDS :
            list.toArray(new ObjectStreamField[size]);
    }

    /**
     * 返回给定类中声明的显式序列版本UID值，如果没有则返回null。
     */
    private static Long getDeclaredSUID(Class<?> cl) {
        try {
            Field f = cl.getDeclaredField("serialVersionUID");
            int mask = Modifier.STATIC | Modifier.FINAL;
            if ((f.getModifiers() & mask) == mask) {
                f.setAccessible(true);
                return Long.valueOf(f.getLong(null));
            }
        } catch (Exception ex) {
        }
        return null;
    }

    /**
     * 计算给定类的默认序列版本UID值。
     */
    private static long computeDefaultSUID(Class<?> cl) {
        if (!Serializable.class.isAssignableFrom(cl) || Proxy.isProxyClass(cl))
        {
            return 0L;
        }

        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            DataOutputStream dout = new DataOutputStream(bout);

            dout.writeUTF(cl.getName());

            int classMods = cl.getModifiers() &
                (Modifier.PUBLIC | Modifier.FINAL |
                 Modifier.INTERFACE | Modifier.ABSTRACT);

            /*
             * 补偿javac中的一个bug，即接口只有在声明方法时才设置ABSTRACT位
             */
            Method[] methods = cl.getDeclaredMethods();
            if ((classMods & Modifier.INTERFACE) != 0) {
                classMods = (methods.length > 0) ?
                    (classMods | Modifier.ABSTRACT) :
                    (classMods & ~Modifier.ABSTRACT);
            }
            dout.writeInt(classMods);

            if (!cl.isArray()) {
                /*
                 * 补偿1.2FCS中的变化，其中Class.getInterfaces()被修改为返回数组类的Cloneable和Serializable。
                 */
                Class<?>[] interfaces = cl.getInterfaces();
                String[] ifaceNames = new String[interfaces.length];
                for (int i = 0; i < interfaces.length; i++) {
                    ifaceNames[i] = interfaces[i].getName();
                }
                Arrays.sort(ifaceNames);
                for (int i = 0; i < ifaceNames.length; i++) {
                    dout.writeUTF(ifaceNames[i]);
                }
            }

            Field[] fields = cl.getDeclaredFields();
            MemberSignature[] fieldSigs = new MemberSignature[fields.length];
            for (int i = 0; i < fields.length; i++) {
                fieldSigs[i] = new MemberSignature(fields[i]);
            }
            Arrays.sort(fieldSigs, new Comparator<MemberSignature>() {
                public int compare(MemberSignature ms1, MemberSignature ms2) {
                    return ms1.name.compareTo(ms2.name);
                }
            });
            for (int i = 0; i < fieldSigs.length; i++) {
                MemberSignature sig = fieldSigs[i];
                int mods = sig.member.getModifiers() &
                    (Modifier.PUBLIC | Modifier.PRIVATE | Modifier.PROTECTED |
                     Modifier.STATIC | Modifier.FINAL | Modifier.VOLATILE |
                     Modifier.TRANSIENT);
                if (((mods & Modifier.PRIVATE) == 0) ||
                    ((mods & (Modifier.STATIC | Modifier.TRANSIENT)) == 0))
                {
                    dout.writeUTF(sig.name);
                    dout.writeInt(mods);
                    dout.writeUTF(sig.signature);
                }
            }


                        if (hasStaticInitializer(cl)) {
                dout.writeUTF("<clinit>");
                dout.writeInt(Modifier.STATIC);
                dout.writeUTF("()V");
            }

            Constructor<?>[] cons = cl.getDeclaredConstructors();
            MemberSignature[] consSigs = new MemberSignature[cons.length];
            for (int i = 0; i < cons.length; i++) {
                consSigs[i] = new MemberSignature(cons[i]);
            }
            Arrays.sort(consSigs, new Comparator<MemberSignature>() {
                public int compare(MemberSignature ms1, MemberSignature ms2) {
                    return ms1.signature.compareTo(ms2.signature);
                }
            });
            for (int i = 0; i < consSigs.length; i++) {
                MemberSignature sig = consSigs[i];
                int mods = sig.member.getModifiers() &
                    (Modifier.PUBLIC | Modifier.PRIVATE | Modifier.PROTECTED |
                     Modifier.STATIC | Modifier.FINAL |
                     Modifier.SYNCHRONIZED | Modifier.NATIVE |
                     Modifier.ABSTRACT | Modifier.STRICT);
                if ((mods & Modifier.PRIVATE) == 0) {
                    dout.writeUTF("<init>");
                    dout.writeInt(mods);
                    dout.writeUTF(sig.signature.replace('/', '.'));
                }
            }

            MemberSignature[] methSigs = new MemberSignature[methods.length];
            for (int i = 0; i < methods.length; i++) {
                methSigs[i] = new MemberSignature(methods[i]);
            }
            Arrays.sort(methSigs, new Comparator<MemberSignature>() {
                public int compare(MemberSignature ms1, MemberSignature ms2) {
                    int comp = ms1.name.compareTo(ms2.name);
                    if (comp == 0) {
                        comp = ms1.signature.compareTo(ms2.signature);
                    }
                    return comp;
                }
            });
            for (int i = 0; i < methSigs.length; i++) {
                MemberSignature sig = methSigs[i];
                int mods = sig.member.getModifiers() &
                    (Modifier.PUBLIC | Modifier.PRIVATE | Modifier.PROTECTED |
                     Modifier.STATIC | Modifier.FINAL |
                     Modifier.SYNCHRONIZED | Modifier.NATIVE |
                     Modifier.ABSTRACT | Modifier.STRICT);
                if ((mods & Modifier.PRIVATE) == 0) {
                    dout.writeUTF(sig.name);
                    dout.writeInt(mods);
                    dout.writeUTF(sig.signature.replace('/', '.'));
                }
            }

            dout.flush();

            MessageDigest md = MessageDigest.getInstance("SHA");
            byte[] hashBytes = md.digest(bout.toByteArray());
            long hash = 0;
            for (int i = Math.min(hashBytes.length, 8) - 1; i >= 0; i--) {
                hash = (hash << 8) | (hashBytes[i] & 0xFF);
            }
            return hash;
        } catch (IOException ex) {
            throw new InternalError(ex);
        } catch (NoSuchAlgorithmException ex) {
            throw new SecurityException(ex.getMessage());
        }
    }

    /**
     * 如果给定的类定义了一个静态初始化方法，则返回 true，否则返回 false。
     */
    private native static boolean hasStaticInitializer(Class<?> cl);

    /**
     * 用于在计算 serialVersionUID 时计算和缓存字段/构造函数/方法签名的类。
     */
    private static class MemberSignature {

        public final Member member;
        public final String name;
        public final String signature;

        public MemberSignature(Field field) {
            member = field;
            name = field.getName();
            signature = getClassSignature(field.getType());
        }

        public MemberSignature(Constructor<?> cons) {
            member = cons;
            name = cons.getName();
            signature = getMethodSignature(
                cons.getParameterTypes(), Void.TYPE);
        }

        public MemberSignature(Method meth) {
            member = meth;
            name = meth.getName();
            signature = getMethodSignature(
                meth.getParameterTypes(), meth.getReturnType());
        }
    }

    /**
     * 用于批量设置和检索可序列化字段值的类。
     */
    // 提醒：动态生成这些？
    private static class FieldReflector {

        /** 用于执行不安全操作的句柄 */
        private static final Unsafe unsafe = Unsafe.getUnsafe();

        /** 要操作的字段 */
        private final ObjectStreamField[] fields;
        /** 原始字段的数量 */
        private final int numPrimFields;
        /** 用于读取字段的不安全字段键 - 可能包含重复项 */
        private final long[] readKeys;
        /** 用于写入字段的不安全字段键 - 没有重复项 */
        private final long[] writeKeys;
        /** 字段数据偏移量 */
        private final int[] offsets;
        /** 字段类型代码 */
        private final char[] typeCodes;
        /** 字段类型 */
        private final Class<?>[] types;

        /**
         * 构造一个 FieldReflector，能够设置/获取包含非空反射 Field 对象的 ObjectStreamFields 子集的值。
         * 包含 null Fields 的 ObjectStreamFields 被视为填充，对于这些字段，get 操作返回默认值，set 操作丢弃给定值。
         */
        FieldReflector(ObjectStreamField[] fields) {
            this.fields = fields;
            int nfields = fields.length;
            readKeys = new long[nfields];
            writeKeys = new long[nfields];
            offsets = new int[nfields];
            typeCodes = new char[nfields];
            ArrayList<Class<?>> typeList = new ArrayList<>();
            Set<Long> usedKeys = new HashSet<>();


            for (int i = 0; i < nfields; i++) {
                ObjectStreamField f = fields[i];
                Field rf = f.getField();
                long key = (rf != null) ?
                    unsafe.objectFieldOffset(rf) : Unsafe.INVALID_FIELD_OFFSET;
                readKeys[i] = key;
                writeKeys[i] = usedKeys.add(key) ?
                    key : Unsafe.INVALID_FIELD_OFFSET;
                offsets[i] = f.getOffset();
                typeCodes[i] = f.getTypeCode();
                if (!f.isPrimitive()) {
                    typeList.add((rf != null) ? rf.getType() : null);
                }
            }


                        types = typeList.toArray(new Class<?>[typeList.size()]);
            numPrimFields = nfields - types.length;
        }

        /**
         * 返回表示此反射器操作的字段的 ObjectStreamFields 列表。列表中包含的 ObjectStreamFields 的共享/非共享值和 Field 对象
         * 反映了它们与本地定义的可序列化字段的绑定。
         */
        ObjectStreamField[] getFields() {
            return fields;
        }

        /**
         * 获取对象 obj 的可序列化原始字段值，并将它们序列化到从偏移量 0 开始的字节数组 buf 中。调用者负责确保 obj 是正确的类型。
         */
        void getPrimFieldValues(Object obj, byte[] buf) {
            if (obj == null) {
                throw new NullPointerException();
            }
            /* 假设已在获取此 FieldReflector 的类描述符上调用了 checkDefaultSerialize()，则数组中的任何字段键
             * 都不应等于 Unsafe.INVALID_FIELD_OFFSET。
             */
            for (int i = 0; i < numPrimFields; i++) {
                long key = readKeys[i];
                int off = offsets[i];
                switch (typeCodes[i]) {
                    case 'Z':
                        Bits.putBoolean(buf, off, unsafe.getBoolean(obj, key));
                        break;

                    case 'B':
                        buf[off] = unsafe.getByte(obj, key);
                        break;

                    case 'C':
                        Bits.putChar(buf, off, unsafe.getChar(obj, key));
                        break;

                    case 'S':
                        Bits.putShort(buf, off, unsafe.getShort(obj, key));
                        break;

                    case 'I':
                        Bits.putInt(buf, off, unsafe.getInt(obj, key));
                        break;

                    case 'F':
                        Bits.putFloat(buf, off, unsafe.getFloat(obj, key));
                        break;

                    case 'J':
                        Bits.putLong(buf, off, unsafe.getLong(obj, key));
                        break;

                    case 'D':
                        Bits.putDouble(buf, off, unsafe.getDouble(obj, key));
                        break;

                    default:
                        throw new InternalError();
                }
            }
        }

        /**
         * 使用从偏移量 0 开始的字节数组 buf 中反序列化的值设置对象 obj 的可序列化原始字段。调用者负责确保 obj 是正确的类型。
         */
        void setPrimFieldValues(Object obj, byte[] buf) {
            if (obj == null) {
                throw new NullPointerException();
            }
            for (int i = 0; i < numPrimFields; i++) {
                long key = writeKeys[i];
                if (key == Unsafe.INVALID_FIELD_OFFSET) {
                    continue;           // 丢弃值
                }
                int off = offsets[i];
                switch (typeCodes[i]) {
                    case 'Z':
                        unsafe.putBoolean(obj, key, Bits.getBoolean(buf, off));
                        break;

                    case 'B':
                        unsafe.putByte(obj, key, buf[off]);
                        break;

                    case 'C':
                        unsafe.putChar(obj, key, Bits.getChar(buf, off));
                        break;

                    case 'S':
                        unsafe.putShort(obj, key, Bits.getShort(buf, off));
                        break;

                    case 'I':
                        unsafe.putInt(obj, key, Bits.getInt(buf, off));
                        break;

                    case 'F':
                        unsafe.putFloat(obj, key, Bits.getFloat(buf, off));
                        break;

                    case 'J':
                        unsafe.putLong(obj, key, Bits.getLong(buf, off));
                        break;

                    case 'D':
                        unsafe.putDouble(obj, key, Bits.getDouble(buf, off));
                        break;

                    default:
                        throw new InternalError();
                }
            }
        }

        /**
         * 获取对象 obj 的可序列化对象字段值，并将它们存储在从偏移量 0 开始的数组 vals 中。调用者负责确保 obj 是正确的类型。
         */
        void getObjFieldValues(Object obj, Object[] vals) {
            if (obj == null) {
                throw new NullPointerException();
            }
            /* 假设已在获取此 FieldReflector 的类描述符上调用了 checkDefaultSerialize()，则数组中的任何字段键
             * 都不应等于 Unsafe.INVALID_FIELD_OFFSET。
             */
            for (int i = numPrimFields; i < fields.length; i++) {
                switch (typeCodes[i]) {
                    case 'L':
                    case '[':
                        vals[offsets[i]] = unsafe.getObject(obj, readKeys[i]);
                        break;

                    default:
                        throw new InternalError();
                }
            }
        }

        /**
         * 使用从偏移量 0 开始的数组 vals 中的值设置对象 obj 的可序列化对象字段。调用者负责确保 obj 是正确的类型；
         * 但是，尝试使用错误类型的值设置字段将触发适当的 ClassCastException。
         */
        void setObjFieldValues(Object obj, Object[] vals) {
            if (obj == null) {
                throw new NullPointerException();
            }
            for (int i = numPrimFields; i < fields.length; i++) {
                long key = writeKeys[i];
                if (key == Unsafe.INVALID_FIELD_OFFSET) {
                    continue;           // 丢弃值
                }
                switch (typeCodes[i]) {
                    case 'L':
                    case '[':
                        Object val = vals[offsets[i]];
                        if (val != null &&
                            !types[i - numPrimFields].isInstance(val))
                        {
                            Field f = fields[i].getField();
                            throw new ClassCastException(
                                "cannot assign instance of " +
                                val.getClass().getName() + " to field " +
                                f.getDeclaringClass().getName() + "." +
                                f.getName() + " of type " +
                                f.getType().getName() + " in instance of " +
                                obj.getClass().getName());
                        }
                        unsafe.putObject(obj, key, val);
                        break;


                                default:
                        throw new InternalError();
                }
            }
        }
    }

    /**
     * 匹配给定的可序列化字段集与给定的本地类描述符中描述的可序列化字段，并返回一个 FieldReflector 实例，该实例能够设置/获取匹配字段子集的值（不匹配的字段被视为填充物，get 操作返回默认值，set 操作丢弃给定值）。如果两个字段集之间存在无法解决的类型冲突，则抛出 InvalidClassException。
     */
    private static FieldReflector getReflector(ObjectStreamField[] fields,
                                               ObjectStreamClass localDesc)
        throws InvalidClassException
    {
        // 如果没有字段，类无关紧要
        Class<?> cl = (localDesc != null && fields.length > 0) ?
            localDesc.cl : null;
        processQueue(Caches.reflectorsQueue, Caches.reflectors);
        FieldReflectorKey key = new FieldReflectorKey(cl, fields,
                                                      Caches.reflectorsQueue);
        Reference<?> ref = Caches.reflectors.get(key);
        Object entry = null;
        if (ref != null) {
            entry = ref.get();
        }
        EntryFuture future = null;
        if (entry == null) {
            EntryFuture newEntry = new EntryFuture();
            Reference<?> newRef = new SoftReference<>(newEntry);
            do {
                if (ref != null) {
                    Caches.reflectors.remove(key, ref);
                }
                ref = Caches.reflectors.putIfAbsent(key, newRef);
                if (ref != null) {
                    entry = ref.get();
                }
            } while (ref != null && entry == null);
            if (entry == null) {
                future = newEntry;
            }
        }

        if (entry instanceof FieldReflector) {  // 先检查常见情况
            return (FieldReflector) entry;
        } else if (entry instanceof EntryFuture) {
            entry = ((EntryFuture) entry).get();
        } else if (entry == null) {
            try {
                entry = new FieldReflector(matchFields(fields, localDesc));
            } catch (Throwable th) {
                entry = th;
            }
            future.set(entry);
            Caches.reflectors.put(key, new SoftReference<Object>(entry));
        }

        if (entry instanceof FieldReflector) {
            return (FieldReflector) entry;
        } else if (entry instanceof InvalidClassException) {
            throw (InvalidClassException) entry;
        } else if (entry instanceof RuntimeException) {
            throw (RuntimeException) entry;
        } else if (entry instanceof Error) {
            throw (Error) entry;
        } else {
            throw new InternalError("unexpected entry: " + entry);
        }
    }

    /**
     * FieldReflector 缓存查找键。如果键引用相同的类和等效的字段格式，则认为键相等。
     */
    private static class FieldReflectorKey extends WeakReference<Class<?>> {

        private final String[] sigs;
        private final int hash;
        private final boolean nullClass;

        FieldReflectorKey(Class<?> cl, ObjectStreamField[] fields,
                          ReferenceQueue<Class<?>> queue)
        {
            super(cl, queue);
            nullClass = (cl == null);
            sigs = new String[2 * fields.length];
            for (int i = 0, j = 0; i < fields.length; i++) {
                ObjectStreamField f = fields[i];
                sigs[j++] = f.getName();
                sigs[j++] = f.getSignature();
            }
            hash = System.identityHashCode(cl) + Arrays.hashCode(sigs);
        }

        public int hashCode() {
            return hash;
        }

        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }

            if (obj instanceof FieldReflectorKey) {
                FieldReflectorKey other = (FieldReflectorKey) obj;
                Class<?> referent;
                return (nullClass ? other.nullClass
                                  : ((referent = get()) != null) &&
                                    (referent == other.get())) &&
                        Arrays.equals(sigs, other.sigs);
            } else {
                return false;
            }
        }
    }

    /**
     * 匹配给定的可序列化字段集与从给定的本地类描述符（其中包含绑定到反射 Field 对象的字段）中获取的可序列化字段。返回一个 ObjectStreamField 列表，其中每个 ObjectStreamField 的签名与本地字段匹配的包含该字段的 Field 对象；不匹配的 ObjectStreamField 包含 null Field 对象。返回的 ObjectStreamField 的共享/非共享设置也反映了匹配的本地 ObjectStreamField 的设置。如果两个字段集之间存在无法解决的类型冲突，则抛出 InvalidClassException。
     */
    private static ObjectStreamField[] matchFields(ObjectStreamField[] fields,
                                                   ObjectStreamClass localDesc)
        throws InvalidClassException
    {
        ObjectStreamField[] localFields = (localDesc != null) ?
            localDesc.fields : NO_FIELDS;

        /*
         * 即使 fields == localFields，我们也不能在这里直接返回 localFields。
         * 在之前的序列化实现中，如果 ObjectStreamField 表示一个非原始字段并且属于一个非本地类描述符，ObjectStreamField.getType() 返回 Object.class。
         * 为了保持这种（值得商榷的）行为，matchFields 返回的 ObjectStreamField 实例不能报告除 Object.class 之外的非原始类型；因此不能直接返回 localFields。
         */

        ObjectStreamField[] matches = new ObjectStreamField[fields.length];
        for (int i = 0; i < fields.length; i++) {
            ObjectStreamField f = fields[i], m = null;
            for (int j = 0; j < localFields.length; j++) {
                ObjectStreamField lf = localFields[j];
                if (f.getName().equals(lf.getName())) {
                    if ((f.isPrimitive() || lf.isPrimitive()) &&
                        f.getTypeCode() != lf.getTypeCode())
                    {
                        throw new InvalidClassException(localDesc.name,
                            "incompatible types for field " + f.getName());
                    }
                    if (lf.getField() != null) {
                        m = new ObjectStreamField(
                            lf.getField(), lf.isUnshared(), false);
                    } else {
                        m = new ObjectStreamField(
                            lf.getName(), lf.getSignature(), lf.isUnshared());
                    }
                }
            }
            if (m == null) {
                m = new ObjectStreamField(
                    f.getName(), f.getSignature(), false);
            }
            m.setOffset(f.getOffset());
            matches[i] = m;
        }
        return matches;
    }

                /**
     * 从指定的映射中移除已在指定引用队列中排队的所有键。
     */
    static void processQueue(ReferenceQueue<Class<?>> queue,
                             ConcurrentMap<? extends
                             WeakReference<Class<?>>, ?> map)
    {
        Reference<? extends Class<?>> ref;
        while((ref = queue.poll()) != null) {
            map.remove(ref);
        }
    }

    /**
     * 用于 Class 对象的弱键。
     *
     **/
    static class WeakClassKey extends WeakReference<Class<?>> {
        /**
         * 保存引用对象的身份哈希码，以在引用对象被清除后保持一致的哈希码。
         */
        private final int hash;

        /**
         * 创建一个新的 WeakClassKey，指向给定的对象，并注册到队列中。
         */
        WeakClassKey(Class<?> cl, ReferenceQueue<Class<?>> refQueue) {
            super(cl, refQueue);
            hash = System.identityHashCode(cl);
        }

        /**
         * 返回原始引用对象的身份哈希码。
         */
        public int hashCode() {
            return hash;
        }

        /**
         * 如果给定的对象是这个相同的 WeakClassKey 实例，或者，如果此对象的引用对象未被清除，且给定的对象是另一个具有与此对象相同的非空引用对象的 WeakClassKey 实例，则返回 true。
         */
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }

            if (obj instanceof WeakClassKey) {
                Object referent = get();
                return (referent != null) &&
                       (referent == ((WeakClassKey) obj).get());
            } else {
                return false;
            }
        }
    }
}
