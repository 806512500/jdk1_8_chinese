
/*
 * Copyright (c) 1996, 2021, Oracle and/or its affiliates. All rights reserved.
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

import java.io.ObjectStreamClass.WeakClassKey;
import java.lang.ref.ReferenceQueue;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BinaryOperator;

import static java.io.ObjectStreamClass.processQueue;

import sun.misc.JavaOISAccess;
import sun.misc.ObjectInputFilter;
import sun.misc.ObjectInputFilter.Config;
import sun.misc.ObjectStreamClassValidator;
import sun.misc.SharedSecrets;
import sun.reflect.misc.ReflectUtil;
import sun.security.action.GetBooleanAction;
import sun.security.action.GetIntegerAction;
import sun.util.logging.PlatformLogger;

/**
 * ObjectInputStream 反序列化之前使用 ObjectOutputStream 序列化的原始数据和对象。
 *
 * <p>ObjectOutputStream 和 ObjectInputStream 可以与 FileOutputStream 和 FileInputStream 一起使用，为对象图提供持久存储。ObjectInputStream 用于恢复之前序列化的对象。其他用途包括使用套接字流在主机之间传递对象，或在远程通信系统中编组和解组参数和参数。
 *
 * <p>ObjectInputStream 确保从流中创建的所有对象图的类型与 Java 虚拟机中的类匹配。类根据需要使用标准机制加载。
 *
 * <p>只有支持 java.io.Serializable 或 java.io.Externalizable 接口的对象才能从流中读取。
 *
 * <p>方法 <code>readObject</code> 用于从流中读取对象。应使用 Java 的安全类型转换来获取所需类型。在 Java 中，字符串和数组是对象，并在序列化过程中作为对象处理。读取时需要转换为预期类型。
 *
 * <p>可以使用 DataInput 上的适当方法从流中读取基本数据类型。
 *
 * <p>对象的默认反序列化机制将每个字段的内容恢复为其写入时的值和类型。声明为 transient 或 static 的字段被反序列化过程忽略。对其他对象的引用会导致在必要时从流中读取这些对象。使用引用共享机制正确恢复对象图。反序列化时总是分配新对象，以防止现有对象被覆盖。
 *
 * <p>读取对象类似于运行新对象的构造函数。为对象分配内存并初始化为零（NULL）。调用无参数构造函数以处理不可序列化的类，然后从流中恢复可序列化类的字段，从最接近 java.lang.object 的可序列化类开始，到对象的最具体类结束。
 *
 * <p>例如，从 ObjectOutputStream 示例中写入的流中读取：
 * <br>
 * <pre>
 *      FileInputStream fis = new FileInputStream("t.tmp");
 *      ObjectInputStream ois = new ObjectInputStream(fis);
 *
 *      int i = ois.readInt();
 *      String today = (String) ois.readObject();
 *      Date date = (Date) ois.readObject();
 *
 *      ois.close();
 * </pre>
 *
 * <p>类通过实现 java.io.Serializable 或 java.io.Externalizable 接口来控制它们如何被序列化。
 *
 * <p>实现 Serializable 接口允许对象序列化保存和恢复对象的整个状态，并允许类在流写入和读取之间的时间内进行演变。它自动遍历对象之间的引用，保存和恢复整个图。
 *
 * <p>需要在序列化和反序列化过程中进行特殊处理的可序列化类应实现以下方法：
 *
 * <pre>
 * private void writeObject(java.io.ObjectOutputStream stream)
 *     throws IOException;
 * private void readObject(java.io.ObjectInputStream stream)
 *     throws IOException, ClassNotFoundException;
 * private void readObjectNoData()
 *     throws ObjectStreamException;
 * </pre>
 *
 * <p>readObject 方法负责使用 writeObject 方法写入到流中的数据读取并恢复对象的特定类的状态。该方法不需要关心其超类或子类的状态。通过从 ObjectInputStream 为各个字段读取数据并为对象的适当字段赋值来恢复状态。读取基本数据类型由 DataInput 支持。
 *
 * <p>任何尝试读取超出 writeObject 方法写入的自定义数据边界的对象数据将导致抛出带有 eof 字段值为 true 的 OptionalDataException。超出分配数据末尾的非对象读取将以与指示流末尾相同的方式反映数据末尾：字节读取将返回 -1 作为读取的字节或读取的字节数，基本读取将抛出 EOFException。如果没有对应的 writeObject 方法，则默认序列化数据的末尾标记分配数据的末尾。
 *
 * <p>从 readExternal 方法中发出的原始和对象读取调用的行为相同——如果流已经位于由对应的 writeExternal 方法写入的数据末尾，对象读取将抛出带有 eof 为 true 的 OptionalDataException，字节读取将返回 -1，基本读取将抛出 EOFException。请注意，对于使用旧的 <code>ObjectStreamConstants.PROTOCOL_VERSION_1</code> 协议写入的流，此行为不适用，因为在该协议中，由 writeExternal 方法写入的数据末尾没有标记，因此无法检测。
 *
 * <p>readObjectNoData 方法负责在反序列化流未将给定类列为正在反序列化的对象的超类时初始化该类的特定对象的状态。这可能发生在接收方使用的反序列化实例的类版本与发送方不同，且接收方的版本扩展了发送方版本未扩展的类的情况下。这也可能发生在序列化流被篡改的情况下；因此，readObjectNoData 有助于在“敌对”或不完整的源流中正确初始化反序列化对象。
 *
 * <p>序列化不会读取或分配值给任何不实现 java.io.Serializable 接口的对象的字段。不可序列化的对象的子类可以是可序列化的。在这种情况下，不可序列化的类必须有一个无参数构造函数，以便其字段可以被初始化。在这种情况下，子类负责保存和恢复不可序列化类的状态。通常情况下，该类的字段是可访问的（公共、包级或受保护的），或者有 get 和 set 方法可以用来恢复状态。
 *
 * <p>在反序列化对象时发生的任何异常都将被 ObjectInputStream 捕获并中止读取过程。
 *
 * <p>实现 Externalizable 接口允许对象完全控制对象序列化形式的内容和格式。Externalizable 接口的方法 writeExternal 和 readExternal 用于保存和恢复对象的状态。当由类实现时，它们可以使用 ObjectOutput 和 ObjectInput 的所有方法来写入和读取自己的状态。对象负责处理发生的任何版本控制。
 *
 * <p>枚举常量的反序列化方式与普通可序列化或可外部化对象不同。枚举常量的序列化形式仅包含其名称；常量的字段值不会传输。要反序列化枚举常量，ObjectInputStream 从流中读取常量名称；然后通过调用静态方法 <code>Enum.valueOf(Class, String)</code> 并将枚举常量的基本类型和接收到的常量名称作为参数来获取反序列化的常量。与其他可序列化或可外部化对象一样，枚举常量可以作为序列化流中后续出现的回引的目标。枚举常量的反序列化过程不能自定义：枚举类型定义的任何类特定的 readObject、readObjectNoData 和 readResolve 方法在反序列化过程中都会被忽略。同样，任何 serialPersistentFields 或 serialVersionUID 字段声明也会被忽略——所有枚举类型的 serialVersionUID 值固定为 0L。
 *
 * @author      Mike Warres
 * @author      Roger Riggs
 * @see java.io.DataInput
 * @see java.io.ObjectOutputStream
 * @see java.io.Serializable
 * @see <a href="../../../platform/serialization/spec/input.html"> Object Serialization Specification, Section 3, Object Input Classes</a>
 * @since   JDK1.1
 */
public class ObjectInputStream
    extends InputStream implements ObjectInput, ObjectStreamConstants
{
    /** 表示 null 的句柄值 */
    private static final int NULL_HANDLE = -1;


                /** 用于内部句柄表中未共享对象的标记 */
    private static final Object unsharedMarker = new Object();

    /** 映射基本类型名称到相应类对象的表 */
    private static final HashMap<String, Class<?>> primClasses
        = new HashMap<>(8, 1.0F);
    static {
        primClasses.put("boolean", boolean.class);
        primClasses.put("byte", byte.class);
        primClasses.put("char", char.class);
        primClasses.put("short", short.class);
        primClasses.put("int", int.class);
        primClasses.put("long", long.class);
        primClasses.put("float", float.class);
        primClasses.put("double", double.class);
        primClasses.put("void", void.class);
    }

    private static class Caches {
        /** 子类安全审核结果的缓存 */
        static final ConcurrentMap<WeakClassKey,Boolean> subclassAudits =
            new ConcurrentHashMap<>();

        /** 已审核子类的 WeakReferences 队列 */
        static final ReferenceQueue<Class<?>> subclassAuditsQueue =
            new ReferenceQueue<>();

        /**
         * 允许在对象读取后设置过滤器的属性。
         * 参见 {@link #setObjectInputFilter(ObjectInputFilter)}
         */
        static final boolean SET_FILTER_AFTER_READ =
                privilegedGetProperty("jdk.serialSetFilterAfterRead");

        private static boolean privilegedGetProperty(String theProp) {
            if (System.getSecurityManager() == null) {
                return Boolean.getBoolean(theProp);
            } else {
                return AccessController.doPrivileged(
                        new GetBooleanAction(theProp));
            }
        }

        /**
         * 用于覆盖代理允许的接口数量实现限制的属性。属性值限制在 0..65535 之间。
         * 代理允许的最大接口数量限制为 65535，由
         * {@link java.lang.reflect.Proxy#newProxyInstance(ClassLoader, Class[], InvocationHandler)} 限制。
         */
        static final int PROXY_INTERFACE_LIMIT = Math.max(0, Math.min(65535, privilegedGetProperty("jdk.serialProxyInterfaceLimit", 65535)));

        private static Integer privilegedGetProperty(String theProp, int defaultVal) {
            if (System.getSecurityManager() == null) {
                return Integer.getInteger(theProp, defaultVal);
            } else {
                return AccessController.doPrivileged(
                        new GetIntegerAction(theProp, defaultVal));
            }
        }
    }

    static {
        /* 设置访问权限，以便 sun.misc 可以调用包私有函数。 */
        JavaOISAccess javaOISAccess = new JavaOISAccess() {
            public void setObjectInputFilter(ObjectInputStream stream, ObjectInputFilter filter) {
                stream.setInternalObjectInputFilter(filter);
            }

            public ObjectInputFilter getObjectInputFilter(ObjectInputStream stream) {
                return stream.getInternalObjectInputFilter();
            }

            public void checkArray(ObjectInputStream stream, Class<?> arrayType, int arrayLength)
                throws InvalidClassException
            {
                stream.checkArray(arrayType, arrayLength);
            }
        };

        sun.misc.SharedSecrets.setJavaOISAccess(javaOISAccess);
    }

    /*
     * 单独的类，以延迟初始化日志记录直到需要时。
     */
    private static class Logging {

        /*
         * 用于 ObjectInputFilter 结果的日志记录器。
         * 如果日志记录器设置为 INFO 或 WARNING，则设置过滤器日志记录器。
         * （假设它不会改变）。
         */
        private static final PlatformLogger traceLogger;
        private static final PlatformLogger infoLogger;
        static {
            PlatformLogger filterLog = PlatformLogger.getLogger("java.io.serialization");
            infoLogger = (filterLog != null &&
                filterLog.isLoggable(PlatformLogger.Level.INFO)) ? filterLog : null;
            traceLogger = (filterLog != null &&
                filterLog.isLoggable(PlatformLogger.Level.FINER)) ? filterLog : null;
        }
    }

    /** 用于处理块数据转换的过滤流 */
    private final BlockDataInputStream bin;
    /** 验证回调列表 */
    private final ValidationList vlist;
    /** 递归深度 */
    private long depth;
    /** 对任何类型对象、类、枚举、代理等的总引用数 */
    private long totalObjectRefs;
    /** 流是否已关闭 */
    private boolean closed;

    /** 线路句柄 -> 对象/异常映射 */
    private final HandleTable handles;
    /** 用于在调用堆栈上传递句柄值的临时字段 */
    private int passHandle = NULL_HANDLE;
    /** 当到达字段值块末尾且没有 TC_ENDBLOCKDATA 时设置的标志 */
    private boolean defaultDataEnd = false;

    /** 用于读取基本字段值的缓冲区 */
    private byte[] primVals;

    /** 如果为 true，则调用 readObjectOverride() 而不是 readObject() */
    private final boolean enableOverride;
    /** 如果为 true，则调用 resolveObject() */
    private boolean enableResolve;

    /**
     * 在调用类定义的 readObject 方法期间的上下文；持有当前正在反序列化的对象和当前类的描述符。
     * 在非 readObject 调用期间为 null。
     */
    private SerialCallbackContext curContext;

    /**
     * 从流中读取的类描述符和类的过滤器；可能为 null。
     */
    private ObjectInputFilter serialFilter;

    /**
     * 如果设置了流特定的过滤器，则为 true；初始为 false。
     */
    private boolean streamFilterSet;

    /**
     * 创建一个从指定输入流读取的 ObjectInputStream。
     * 从流中读取并验证序列化流头。
     * 此构造函数将阻塞，直到对应的 ObjectOutputStream 写入并刷新了头。
     *
     * <p>如果安装了安全经理，当此构造函数直接或间接由覆盖了 ObjectInputStream.readFields 或 ObjectInputStream.readUnshared
     * 方法的子类构造函数调用时，将检查 "enableSubclassImplementation" SerializablePermission。
     *
     * @param   in 要从中读取的输入流
     * @throws  StreamCorruptedException 如果流头不正确
     * @throws  IOException 如果在读取流头时发生 I/O 错误
     * @throws  SecurityException 如果未信任的子类非法覆盖了安全敏感的方法
     * @throws  NullPointerException 如果 <code>in</code> 为 <code>null</code>
     * @see     ObjectInputStream#ObjectInputStream()
     * @see     ObjectInputStream#readFields()
     * @see     ObjectOutputStream#ObjectOutputStream(OutputStream)
     */
    public ObjectInputStream(InputStream in) throws IOException {
        verifySubclass();
        bin = new BlockDataInputStream(in);
        handles = new HandleTable(10);
        vlist = new ValidationList();
        serialFilter = Config.getSerialFilterFactorySingleton().apply(null, Config.getSerialFilter());
        enableOverride = false;
        readStreamHeader();
        bin.setBlockDataMode(true);
    }

                /**
     * 为完全重写 ObjectInputStream 的子类提供一种方法，使其不必为仅在此 ObjectInputStream 实现中使用的私有数据分配空间。
     *
     * <p>如果安装了安全管理器，此方法首先调用安全管理器的 <code>checkPermission</code> 方法，使用
     * <code>SerializablePermission("enableSubclassImplementation")</code>
     * 权限确保启用子类化是安全的。
     *
     * @throws  SecurityException 如果存在安全管理器且其
     *          <code>checkPermission</code> 方法拒绝启用
     *          子类化。
     * @throws  IOException 如果在创建此流时发生 I/O 错误
     * @see SecurityManager#checkPermission
     * @see java.io.SerializablePermission
     */
    protected ObjectInputStream() throws IOException, SecurityException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(SUBCLASS_IMPLEMENTATION_PERMISSION);
        }
        bin = null;
        handles = null;
        vlist = null;
        serialFilter = Config.getSerialFilterFactorySingleton().apply(null, Config.getSerialFilter());
        enableOverride = true;
    }

    /**
     * 从 ObjectInputStream 中读取一个对象。对象的类、类的签名以及类及其所有超类的非瞬态和非静态字段的值都会被读取。
     * 类的默认反序列化可以通过使用 writeObject 和 readObject 方法来覆盖。此对象引用的对象会递归地读取，以便 readObject
     * 重建一个完整的等效对象图。
     *
     * <p>当根对象及其所有字段和它引用的对象完全恢复时，根对象才完全恢复。此时，根据它们注册的优先级顺序执行对象验证回调。回调由对象（在 readObject
     * 特殊方法中）在单独恢复时注册。
     *
     * <p>对于 InputStream 的问题和不应该被反序列化的类，会抛出异常。所有异常都会导致 InputStream 失效并使其处于不确定状态；由调用者决定忽略或恢复流状态。
     *
     * @throws  ClassNotFoundException 无法找到序列化对象的类。
     * @throws  InvalidClassException 序列化使用的一个类有问题。
     * @throws  StreamCorruptedException 流中的控制信息不一致。
     * @throws  OptionalDataException 流中发现了原始数据而不是对象。
     * @throws  IOException 通常的输入/输出相关异常。
     */
    public final Object readObject()
        throws IOException, ClassNotFoundException {
        return readObject(Object.class);
    }

    /**
     * 仅读取一个字符串。
     *
     * @return 读取的字符串
     * @throws  EOFException 如果到达文件末尾。
     * @throws  IOException 如果发生其他 I/O 错误。
     */
    private String readString() throws IOException {
        try {
            return (String) readObject(String.class);
        } catch (ClassNotFoundException cnf) {
            throw new IllegalStateException(cnf);
        }
    }

    /**
     * 从 ObjectInputStream 中读取预期类型的对象的内部方法。仅从 {@code readObject()} 和 {@code readString()} 调用。
     * 仅支持 {@code Object.class} 和 {@code String.class}。
     *
     * @param type 期望的类型；必须是 Object.class 或 String.class
     * @return 类型的对象
     * @throws  IOException 通常的输入/输出相关异常。
     * @throws  ClassNotFoundException 无法找到序列化对象的类。
     */
    private final Object readObject(Class<?> type)
        throws IOException, ClassNotFoundException
    {
        if (enableOverride) {
            return readObjectOverride();
        }

        if (! (type == Object.class || type == String.class))
            throw new AssertionError("内部错误");

        // 如果是嵌套读取，passHandle 包含外部对象的句柄
        int outerHandle = passHandle;
        try {
            Object obj = readObject0(type, false);
            handles.markDependency(outerHandle, passHandle);
            ClassNotFoundException ex = handles.lookupException(passHandle);
            if (ex != null) {
                throw ex;
            }
            if (depth == 0) {
                vlist.doCallbacks();
            }
            return obj;
        } finally {
            passHandle = outerHandle;
            if (closed && depth == 0) {
                clear();
            }
        }
    }

    /**
     * 由 ObjectInputStream 的受信任子类调用，这些子类使用受保护的无参构造函数构造了 ObjectInputStream。
     * 期望子类提供一个具有 "final" 修饰符的重写方法。
     *
     * @return 从流中读取的对象。
     * @throws  ClassNotFoundException 无法找到序列化对象的类定义。
     * @throws  OptionalDataException 流中发现了原始数据而不是对象。
     * @throws  IOException 如果在从底层流读取时发生 I/O 错误
     * @see #ObjectInputStream()
     * @see #readObject()
     * @since 1.2
     */
    protected Object readObjectOverride()
        throws IOException, ClassNotFoundException
    {
        return null;
    }

    /**
     * 从 ObjectInputStream 中读取一个“不共享”的对象。此方法与 readObject 完全相同，除了它防止后续对 readObject 和 readUnshared 的调用返回通过此调用获得的反序列化实例的额外引用。具体来说：
     * <ul>
     *   <li>如果 readUnshared 用于反序列化回引（流中对象的表示形式，该对象已先前写入流），将抛出 ObjectStreamException。
     *
     *   <li>如果 readUnshared 成功返回，则任何后续尝试反序列化回引到 readUnshared 反序列化的流句柄将导致抛出 ObjectStreamException。
     * </ul>
     * 通过 readUnshared 反序列化对象会使与返回对象关联的流句柄失效。请注意，这本身并不总是保证 readUnshared 返回的引用是唯一的；
     * 反序列化的对象可能定义了一个返回其他方可见对象的 readResolve 方法，或者 readUnshared 可能返回一个可以通过流中的其他地方或通过外部手段获得的类对象或枚举常量。如果反序列化的对象定义了一个 readResolve 方法，并且该方法的调用返回了一个数组，那么 readUnshared 返回该数组的浅拷贝；这保证了返回的数组对象是唯一的，不能通过再次调用 ObjectInputStream 的 readObject 或 readUnshared 从底层数据流中获得。
     *
     * <p>重写此方法的 ObjectInputStream 子类只能在具有 "enableSubclassImplementation" SerializablePermission 的安全上下文中构造；在没有此权限的情况下尝试实例化此类子类将导致抛出 SecurityException。
     *
     * @return 反序列化对象的引用
     * @throws  ClassNotFoundException 如果无法找到要反序列化的对象的类
     * @throws  StreamCorruptedException 如果流中的控制信息不一致
     * @throws  ObjectStreamException 如果要反序列化的对象已在流中出现
     * @throws  OptionalDataException 如果流中的下一个数据是原始数据
     * @throws  IOException 如果在反序列化过程中发生 I/O 错误
     * @since   1.4
     */
    public Object readUnshared() throws IOException, ClassNotFoundException {
        // 如果是嵌套读取，passHandle 包含外部对象的句柄
        int outerHandle = passHandle;
        try {
            Object obj = readObject0(Object.class, true);
            handles.markDependency(outerHandle, passHandle);
            ClassNotFoundException ex = handles.lookupException(passHandle);
            if (ex != null) {
                throw ex;
            }
            if (depth == 0) {
                vlist.doCallbacks();
            }
            return obj;
        } finally {
            passHandle = outerHandle;
            if (closed && depth == 0) {
                clear();
            }
        }
    }


/**
 * 从当前流中读取当前类的非静态和非瞬态字段。此方法只能从正在反序列化的类的 readObject 方法中调用。如果以其他方式调用，将抛出 NotActiveException。
 *
 * @throws  ClassNotFoundException 如果无法找到序列化对象的类。
 * @throws  IOException 如果发生 I/O 错误。
 * @throws  NotActiveException 如果流当前未读取对象。
 */
public void defaultReadObject()
    throws IOException, ClassNotFoundException
{
    SerialCallbackContext ctx = curContext;
    if (ctx == null) {
        throw new NotActiveException("not in call to readObject");
    }
    Object curObj = ctx.getObj();
    ObjectStreamClass curDesc = ctx.getDesc();
    bin.setBlockDataMode(false);
    defaultReadFields(curObj, curDesc);
    bin.setBlockDataMode(true);
    if (!curDesc.hasWriteObjectData()) {
        /*
         * 修复 4360508：由于流中不包含终止的 TC_ENDBLOCKDATA 标签，设置标志，以便其他地方的读取代码知道模拟自定义数据结束的行为。
         */
        defaultDataEnd = true;
    }
    ClassNotFoundException ex = handles.lookupException(passHandle);
    if (ex != null) {
        throw ex;
    }
}

/**
 * 从流中读取持久字段，并按名称提供这些字段。
 *
 * @return 代表正在反序列化的对象的持久字段的 <code>GetField</code> 对象。
 * @throws  ClassNotFoundException 如果无法找到序列化对象的类。
 * @throws  IOException 如果发生 I/O 错误。
 * @throws  NotActiveException 如果流当前未读取对象。
 * @since 1.2
 */
public ObjectInputStream.GetField readFields()
    throws IOException, ClassNotFoundException
{
    SerialCallbackContext ctx = curContext;
    if (ctx == null) {
        throw new NotActiveException("not in call to readObject");
    }
    Object curObj = ctx.getObj();
    ObjectStreamClass curDesc = ctx.getDesc();
    bin.setBlockDataMode(false);
    GetFieldImpl getField = new GetFieldImpl(curDesc);
    getField.readFields();
    bin.setBlockDataMode(true);
    if (!curDesc.hasWriteObjectData()) {
        /*
         * 修复 4360508：由于流中不包含终止的 TC_ENDBLOCKDATA 标签，设置标志，以便其他地方的读取代码知道模拟自定义数据结束的行为。
         */
        defaultDataEnd = true;
    }

    return getField;
}

/**
 * 注册一个对象，以便在图返回之前进行验证。虽然与 resolveObject 类似，但这些验证是在整个图重新构建之后调用的。通常，readObject 方法会将对象注册到流中，以便在所有对象恢复后执行最终的一组验证。
 *
 * @param   obj 接收验证回调的对象。
 * @param   prio 控制回调顺序；零是一个不错的选择。使用较高的数字可以更早地回调，较低的数字可以稍后回调。在相同优先级内，回调的顺序没有特定顺序。
 * @throws  NotActiveException 流当前未读取对象，因此注册回调无效。
 * @throws  InvalidObjectException 验证对象为 null。
 */
public void registerValidation(ObjectInputValidation obj, int prio)
    throws NotActiveException, InvalidObjectException
{
    if (depth == 0) {
        throw new NotActiveException("stream inactive");
    }
    vlist.register(obj, prio);
}

/**
 * 加载指定流类描述的本地类等效类。子类可以实现此方法以允许从替代源获取类。
 *
 * <p>在 <code>ObjectOutputStream</code> 中对应的 方法是 <code>annotateClass</code>。此方法仅对流中的每个唯一类调用一次。子类可以实现此方法以使用替代加载机制，但必须返回一个 <code>Class</code> 对象。一旦返回，如果类不是数组类，则其 serialVersionUID 将与序列化类的 serialVersionUID 进行比较，如果存在不匹配，反序列化将失败并抛出 {@link InvalidClassException}。
 *
 * <p><code>ObjectInputStream</code> 中此方法的默认实现返回调用
 * <pre>
 *     Class.forName(desc.getName(), false, loader)
 * </pre>
 * 的结果，其中 <code>loader</code> 的确定方式如下：如果当前线程堆栈上的方法中有一个声明类是由用户定义的类加载器定义的（并且不是生成的以实现反射调用），则 <code>loader</code> 是对应于当前执行帧最近的此类方法的类加载器；否则，<code>loader</code> 是 <code>null</code>。如果此调用导致 <code>ClassNotFoundException</code> 且传递的 <code>ObjectStreamClass</code> 实例的名称是 Java 语言中的基本类型或 void 的关键字，则将返回表示该基本类型或 void 的 <code>Class</code> 对象（例如，名称为 <code>"int"</code> 的 <code>ObjectStreamClass</code> 将解析为 <code>Integer.TYPE</code>）。否则，将 <code>ClassNotFoundException</code> 抛给此方法的调用者。
 *
 * @param   desc <code>ObjectStreamClass</code> 类的实例。
 * @return 与 <code>desc</code> 对应的 <code>Class</code> 对象。
 * @throws  IOException 通常的输入/输出异常。
 * @throws  ClassNotFoundException 如果无法找到序列化对象的类。
 */
protected Class<?> resolveClass(ObjectStreamClass desc)
    throws IOException, ClassNotFoundException
{
    String name = desc.getName();
    try {
        return Class.forName(name, false, latestUserDefinedLoader());
    } catch (ClassNotFoundException ex) {
        Class<?> cl = primClasses.get(name);
        if (cl != null) {
            return cl;
        } else {
            throw ex;
        }
    }
}

                /**
     * 返回一个实现代理类描述符中命名的接口的代理类；子类可以实现此方法以从流中读取自定义数据，以及动态代理类的描述符，
     * 使它们能够使用接口和代理类的替代加载机制。
     *
     * <p>此方法在流中的每个唯一代理类描述符中恰好被调用一次。
     *
     * <p><code>ObjectOutputStream</code> 中对应的方 法是 <code>annotateProxyClass</code>。对于给定的 <code>ObjectInputStream</code>
     * 子类，该子类覆盖了此方法，则对应的 <code>ObjectOutputStream</code> 子类中的 <code>annotateProxyClass</code> 方法必须写入
     * 由该方法读取的任何数据或对象。
     *
     * <p><code>ObjectInputStream</code> 中此方法的默认实现通过调用 <code>Proxy.getProxyClass</code> 并传递 <code>interfaces</code>
     * 参数中命名的接口的 <code>Class</code> 对象列表来返回结果。每个接口名 <code>i</code> 的 <code>Class</code> 对象是通过调用
     * <pre>
     *     Class.forName(i, false, loader)
     * </pre>
     * 其中 <code>loader</code> 是执行堆栈上第一个非 <code>null</code> 类加载器，或者如果堆栈上没有非 <code>null</code> 类加载器，则为 <code>null</code>
     * （与 <code>resolveClass</code> 方法使用的类加载器选择相同）。除非解析的接口中有非公共接口，否则此相同的 <code>loader</code> 值也会传递给
     * <code>Proxy.getProxyClass</code>；如果存在非公共接口，则传递其类加载器（如果遇到多个非公共接口类加载器，则抛出 <code>IllegalAccessError</code>）。
     * 如果 <code>Proxy.getProxyClass</code> 抛出 <code>IllegalArgumentException</code>，<code>resolveProxyClass</code> 将抛出一个包含
     * <code>IllegalArgumentException</code> 的 <code>ClassNotFoundException</code>。
     *
     * @param interfaces 代理类描述符中反序列化的接口名列表
     * @return 指定接口的代理类
     * @throws IOException 由底层 <code>InputStream</code> 抛出的任何异常
     * @throws ClassNotFoundException 如果找不到代理类或任何命名的接口
     * @see ObjectOutputStream#annotateProxyClass(Class)
     * @since 1.3
     */
    protected Class<?> resolveProxyClass(String[] interfaces)
        throws IOException, ClassNotFoundException
    {
        ClassLoader latestLoader = latestUserDefinedLoader();
        ClassLoader nonPublicLoader = null;
        boolean hasNonPublicInterface = false;

        // 在非公共接口（如果有）的类加载器中定义代理
        Class<?>[] classObjs = new Class<?>[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
            Class<?> cl = Class.forName(interfaces[i], false, latestLoader);
            if ((cl.getModifiers() & Modifier.PUBLIC) == 0) {
                if (hasNonPublicInterface) {
                    if (nonPublicLoader != cl.getClassLoader()) {
                        throw new IllegalAccessError(
                            "conflicting non-public interface class loaders");
                    }
                } else {
                    nonPublicLoader = cl.getClassLoader();
                    hasNonPublicInterface = true;
                }
            }
            classObjs[i] = cl;
        }
        try {
            return Proxy.getProxyClass(
                hasNonPublicInterface ? nonPublicLoader : latestLoader,
                classObjs);
        } catch (IllegalArgumentException e) {
            throw new ClassNotFoundException(null, e);
        }
    }

    /**
     * 此方法允许 <code>ObjectInputStream</code> 的受信任子类在反序列化期间用另一个对象替换一个对象。替换对象的功能在调用 <code>enableResolveObject</code>
     * 之前是禁用的。<code>enableResolveObject</code> 方法检查请求解析对象的流是否可以信任。每个可序列化对象的引用都会传递给 <code>resolveObject</code>。
     * 为了确保对象的私有状态不会无意中暴露，只有受信任的流可以使用 <code>resolveObject</code>。
     *
     * <p>此方法在对象被读取但返回 <code>readObject</code> 之前调用。默认的 <code>resolveObject</code> 方法只是返回相同的对象。
     *
     * <p>当子类正在替换对象时，它必须确保替换的对象与存储引用的每个字段兼容。如果对象的类型不是字段或数组元素类型的子类，则通过抛出异常来终止序列化，
     * 并且对象不会被存储。
     *
     * <p>此方法仅在首次遇到每个对象时调用。所有后续对该对象的引用都将重定向到新对象。
     *
     * @param   obj 要替换的对象
     * @return  替换后的对象
     * @throws  IOException 任何通常的输入/输出异常。
     */
    protected Object resolveObject(Object obj) throws IOException {
        return obj;
    }

    /**
     * 允许流在从流中读取的对象被替换。启用后，<code>resolveObject</code> 方法将被调用以处理每个正在被反序列化的对象。
     *
     * <p>如果 <i>enable</i> 为 true，并且安装了安全管理器，此方法首先调用安全管理器的 <code>checkPermission</code> 方法，
     * 使用 <code>SerializablePermission("enableSubstitution")</code> 权限来确保启用流允许从流中读取的对象被替换是安全的。
     *
     * @param   enable 如果为 true，则启用 <code>resolveObject</code> 用于每个正在被反序列化的对象
     * @return  调用此方法前的先前设置
     * @throws  SecurityException 如果存在安全管理器，并且其 <code>checkPermission</code> 方法拒绝启用流允许从流中读取的对象被替换。
     * @see SecurityManager#checkPermission
     * @see java.io.SerializablePermission
     */
    protected boolean enableResolveObject(boolean enable)
        throws SecurityException
    {
        if (enable == enableResolve) {
            return enable;
        }
        if (enable) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                sm.checkPermission(SUBSTITUTION_PERMISSION);
            }
        }
        enableResolve = enable;
        return !enableResolve;
    }

                /**
     * readStreamHeader 方法用于允许子类读取并验证自己的流头。它读取并验证魔数和版本号。
     *
     * @throws  IOException 如果从底层 <code>InputStream</code> 读取时发生 I/O 错误
     * @throws  StreamCorruptedException 如果流中的控制信息不一致
     */
    protected void readStreamHeader()
        throws IOException, StreamCorruptedException
    {
        short s0 = bin.readShort();
        short s1 = bin.readShort();
        if (s0 != STREAM_MAGIC || s1 != STREAM_VERSION) {
            throw new StreamCorruptedException(
                String.format("无效的流头: %04X%04X", s0, s1));
        }
    }

    /**
     * 从序列化流中读取一个类描述符。当 ObjectInputStream 期望序列化流中的下一个项是类描述符时调用此方法。
     * ObjectInputStream 的子类可以覆盖此方法以读取以非标准格式写入的类描述符（由覆盖了
     * <code>writeClassDescriptor</code> 方法的 ObjectOutputStream 的子类写入）。默认情况下，
     * 此方法根据 Object 序列化规范中定义的格式读取类描述符。
     *
     * @return  读取的类描述符
     * @throws  IOException 如果发生 I/O 错误。
     * @throws  ClassNotFoundException 如果在类描述符表示中使用的序列化对象的类找不到
     * @see java.io.ObjectOutputStream#writeClassDescriptor(java.io.ObjectStreamClass)
     * @since 1.3
     */
    protected ObjectStreamClass readClassDescriptor()
        throws IOException, ClassNotFoundException
    {
        ObjectStreamClass desc = new ObjectStreamClass();
        desc.readNonProxy(this);
        return desc;
    }

    /**
     * 读取一个字节的数据。如果无输入可用，此方法将阻塞。
     *
     * @return  读取的字节，如果到达流的末尾，则返回 -1。
     * @throws  IOException 如果发生 I/O 错误。
     */
    public int read() throws IOException {
        return bin.read();
    }

    /**
     * 读取到一个字节数组中。此方法将在有输入可用之前阻塞。考虑使用 java.io.DataInputStream.readFully 读取
     * 恰好 'length' 个字节。
     *
     * @param   buf 存储数据的缓冲区
     * @param   off 数据的起始偏移量
     * @param   len 最大读取的字节数
     * @return  实际读取的字节数，如果到达流的末尾，则返回 -1。
     * @throws  IOException 如果发生 I/O 错误。
     * @see java.io.DataInputStream#readFully(byte[],int,int)
     */
    public int read(byte[] buf, int off, int len) throws IOException {
        if (buf == null) {
            throw new NullPointerException();
        }
        int endoff = off + len;
        if (off < 0 || len < 0 || endoff > buf.length || endoff < 0) {
            throw new IndexOutOfBoundsException();
        }
        return bin.read(buf, off, len, false);
    }

    /**
     * 返回可以不阻塞读取的字节数。
     *
     * @return  可用的字节数。
     * @throws  IOException 如果从底层 <code>InputStream</code> 读取时发生 I/O 错误
     */
    public int available() throws IOException {
        return bin.available();
    }

    /**
     * 关闭输入流。必须调用此方法以释放与流关联的任何资源。
     *
     * @throws  IOException 如果发生 I/O 错误。
     */
    public void close() throws IOException {
        /*
         * 即使流已经关闭，也要将多余的关闭传播到底层流，以保持与以前实现的一致性。
         */
        closed = true;
        if (depth == 0) {
            clear();
        }
        bin.close();
    }

    /**
     * 读取一个布尔值。
     *
     * @return  读取的布尔值。
     * @throws  EOFException 如果到达文件末尾。
     * @throws  IOException 如果发生其他 I/O 错误。
     */
    public boolean readBoolean() throws IOException {
        return bin.readBoolean();
    }

    /**
     * 读取一个 8 位字节。
     *
     * @return  读取的 8 位字节。
     * @throws  EOFException 如果到达文件末尾。
     * @throws  IOException 如果发生其他 I/O 错误。
     */
    public byte readByte() throws IOException  {
        return bin.readByte();
    }

    /**
     * 读取一个无符号 8 位字节。
     *
     * @return  读取的 8 位字节。
     * @throws  EOFException 如果到达文件末尾。
     * @throws  IOException 如果发生其他 I/O 错误。
     */
    public int readUnsignedByte()  throws IOException {
        return bin.readUnsignedByte();
    }

    /**
     * 读取一个 16 位字符。
     *
     * @return  读取的 16 位字符。
     * @throws  EOFException 如果到达文件末尾。
     * @throws  IOException 如果发生其他 I/O 错误。
     */
    public char readChar()  throws IOException {
        return bin.readChar();
    }

    /**
     * 读取一个 16 位短整型。
     *
     * @return  读取的 16 位短整型。
     * @throws  EOFException 如果到达文件末尾。
     * @throws  IOException 如果发生其他 I/O 错误。
     */
    public short readShort()  throws IOException {
        return bin.readShort();
    }

    /**
     * 读取一个无符号 16 位短整型。
     *
     * @return  读取的 16 位短整型。
     * @throws  EOFException 如果到达文件末尾。
     * @throws  IOException 如果发生其他 I/O 错误。
     */
    public int readUnsignedShort() throws IOException {
        return bin.readUnsignedShort();
    }

    /**
     * 读取一个 32 位整型。
     *
     * @return  读取的 32 位整型。
     * @throws  EOFException 如果到达文件末尾。
     * @throws  IOException 如果发生其他 I/O 错误。
     */
    public int readInt()  throws IOException {
        return bin.readInt();
    }


    /**
     * 读取一个64位长整型。
     *
     * @return 读取的64位长整型。
     * @throws  EOFException 如果到达文件末尾。
     * @throws  IOException 如果发生其他I/O错误。
     */
    public long readLong()  throws IOException {
        return bin.readLong();
    }

    /**
     * 读取一个32位浮点数。
     *
     * @return 读取的32位浮点数。
     * @throws  EOFException 如果到达文件末尾。
     * @throws  IOException 如果发生其他I/O错误。
     */
    public float readFloat() throws IOException {
        return bin.readFloat();
    }

    /**
     * 读取一个64位双精度浮点数。
     *
     * @return 读取的64位双精度浮点数。
     * @throws  EOFException 如果到达文件末尾。
     * @throws  IOException 如果发生其他I/O错误。
     */
    public double readDouble() throws IOException {
        return bin.readDouble();
    }

    /**
     * 读取字节，直到所有字节都被读取。
     *
     * @param   buf 用于存储读取数据的缓冲区。
     * @throws  EOFException 如果到达文件末尾。
     * @throws  IOException 如果发生其他I/O错误。
     */
    public void readFully(byte[] buf) throws IOException {
        bin.readFully(buf, 0, buf.length, false);
    }

    /**
     * 读取字节，直到所有字节都被读取。
     *
     * @param   buf 用于存储读取数据的缓冲区。
     * @param   off 数据的起始偏移量。
     * @param   len 最多读取的字节数。
     * @throws  EOFException 如果到达文件末尾。
     * @throws  IOException 如果发生其他I/O错误。
     */
    public void readFully(byte[] buf, int off, int len) throws IOException {
        int endoff = off + len;
        if (off < 0 || len < 0 || endoff > buf.length || endoff < 0) {
            throw new IndexOutOfBoundsException();
        }
        bin.readFully(buf, off, len, false);
    }

    /**
     * 跳过指定数量的字节。
     *
     * @param   len 要跳过的字节数。
     * @return  实际跳过的字节数。
     * @throws  IOException 如果发生I/O错误。
     */
    public int skipBytes(int len) throws IOException {
        return bin.skipBytes(len);
    }

    /**
     * 读取以\n, \r, \r\n或EOF终止的一行。
     *
     * @return 该行的字符串副本。
     * @throws  IOException 如果从底层的<code>InputStream</code>读取时发生I/O错误。
     * @deprecated 此方法不能正确地将字节转换为字符。
     *          请参见DataInputStream以获取详细信息和替代方案。
     */
    @Deprecated
    public String readLine() throws IOException {
        return bin.readLine();
    }

    /**
     * 读取一个以
     * <a href="DataInput.html#modified-utf-8">修改的UTF-8</a>
     * 格式表示的字符串。
     *
     * @return 该字符串。
     * @throws  IOException 如果从底层的<code>InputStream</code>读取时发生I/O错误。
     * @throws  UTFDataFormatException 如果读取的字节不表示有效的修改的UTF-8编码的字符串。
     */
    public String readUTF() throws IOException {
        return bin.readUTF();
    }

    /**
     * 返回此流的序列化过滤器。
     * 过滤器是通过调用
     * {@link Config#getSerialFilterFactory() JVM范围的过滤器工厂}
     * 由{@linkplain #ObjectInputStream() 构造函数}或最近一次调用
     * {@link #setObjectInputFilter setObjectInputFilter}生成的结果。
     *
     * @return 该流的序列化过滤器；可能为null。
     */
    private final ObjectInputFilter getInternalObjectInputFilter() {
        return serialFilter;
    }

    /**
     * 设置流的反序列化过滤器。
     *
     * 反序列化过滤器设置为通过调用
     * {@linkplain Config#getSerialFilterFactorySingleton() JVM范围的过滤器工厂}
     * 使用{@linkplain #getInternalObjectInputFilter() 当前过滤器}和{@code filter}参数生成的过滤器。
     * 当前过滤器是在
     * {@linkplain #ObjectInputStream() ObjectInputStream构造函数}中通过调用
     * {@linkplain Config#getSerialFilterFactorySingleton() JVM范围的过滤器工厂}设置的，可能为{@code null}。
     * {@linkplain Config#setObjectInputFilter(ObjectInputFilter) 此方法}只能在从流中读取任何对象之前调用一次；
     * 例如，通过调用{@link #readObject}或{@link #readUnshared}。
     *
     * <p>不允许用{@code null}过滤器替换{@code non-null}过滤器。
     * 如果{@linkplain #getInternalObjectInputFilter() 当前过滤器}为{@code non-null}，
     * 则过滤器工厂返回的值必须为{@code non-null}。
     *
     * <p>过滤器的{@link ObjectInputFilter#checkInput checkInput}方法
     * 会为流中的每个类和引用调用。
     * 过滤器可以检查类、数组长度、引用数量、图的深度和输入流的大小中的任何或全部。
     * <p>
     * 如果过滤器返回{@link ObjectInputFilter.Status#REJECTED Status.REJECTED}、
     * {@code null}或抛出{@link RuntimeException}，
     * 则当前的{@code readObject}或{@code readUnshared}
     * 将抛出{@link InvalidClassException}，否则反序列化将继续不受干扰。
     *
     * @implSpec
     * 当过滤器不为{@code null}时，它会在{@link #readObject readObject}
     * 和{@link #readUnshared readUnshared}期间为流中的每个对象
     * （常规对象或类）调用，包括以下内容：
     * <ul>
     *     <li>从流中先前反序列化的每个对象引用
     *     （类为{@code null}，数组长度为-1），
     *     <li>每个常规类（类不为{@code null}，数组长度为-1），
     *     <li>流中显式引用的每个接口类
     *         （不会为流中类实现的接口调用），
     *     <li>每个动态代理类及其所有接口
     *     （类不为{@code null}，数组长度为-1），
     *     <li>每个数组使用数组类型和数组长度进行过滤
     *     （类为数组类型，数组长度为请求的长度），
     *     <li>每个由其类的{@code readResolve}方法替换的对象
     *         使用替换对象的类（如果不为{@code null}）和
     *         如果是数组，则使用数组长度，否则为-1，
     *     <li>每个由{@link #resolveObject resolveObject}替换的对象
     *         使用替换对象的类（如果不为{@code null}）和
     *         如果是数组，则使用数组长度，否则为-1。
     * </ul>
     *
     * 当调用{@link ObjectInputFilter#checkInput checkInput}方法时，
     * 它可以访问当前类、数组长度、
     * 已从流中读取的引用数量、
     * 嵌套调用{@link #readObject readObject}或
     * {@link #readUnshared readUnshared}的深度，
     * 以及从输入流中消耗的字节数（实现依赖）。
     * <p>
     * 每次调用{@link #readObject readObject}或
     * {@link #readUnshared readUnshared}时，深度增加1
     * 在读取对象之前，然后在正常或异常返回前减少1。
     * 深度从{@code 1}开始，每个嵌套对象增加1，
     * 每个嵌套调用返回时减少1。
     * 流中的引用计数从{@code 1}开始，
     * 在读取对象之前增加。
     *
     * @param filter 过滤器，可能为null。
     * @throws SecurityException 如果存在安全管理器且未授予
     *       {@code SerializablePermission("serialFilter")}权限。
     * @throws IllegalStateException 如果已读取对象，
     *       如果过滤器工厂返回{@code null}而
     *       {@linkplain #getInternalObjectInputFilter() 当前过滤器}为非null，或
     *       如果已设置过滤器。
     * @since 8u
     */
    private final void setInternalObjectInputFilter(ObjectInputFilter filter) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new SerializablePermission("serialFilter"));
        }
        if (streamFilterSet) {
            throw new IllegalStateException("filter can not be set more than once");
        }
        if (totalObjectRefs > 0 && !Caches.SET_FILTER_AFTER_READ) {
            throw new IllegalStateException(
                    "filter can not be set after an object has been read");
        }
        streamFilterSet = true;
        // 委托给serialFilterFactory以计算流过滤器
        ObjectInputFilter next = Config.getSerialFilterFactorySingleton()
                .apply(serialFilter, filter);
        if (serialFilter != null && next == null) {
            throw new IllegalStateException("filter can not be replaced with null filter");
        }
        serialFilter = next;
    }


                /**
     * 如果非空，则调用序列化过滤器。
     * 如果过滤器拒绝或抛出异常，则抛出 InvalidClassException。
     *
     * @param clazz 类；可以为 null
     * @param arrayLength 请求的数组长度；如果不创建数组，则使用 {@code -1}
     * @throws InvalidClassException 如果被过滤器拒绝或
     *        抛出 {@link RuntimeException}
     */
    private void filterCheck(Class<?> clazz, int arrayLength)
            throws InvalidClassException {
        if (serialFilter != null) {
            RuntimeException ex = null;
            ObjectInputFilter.Status status;
            // 如果被子类覆盖，则流的信息不可用，返回 0
            long bytesRead = (bin == null) ? 0 : bin.getBytesRead();
            try {
                status = serialFilter.checkInput(new FilterValues(clazz, arrayLength,
                        totalObjectRefs, depth, bytesRead));
            } catch (RuntimeException e) {
                // 预防性拦截异常以记录日志
                status = ObjectInputFilter.Status.REJECTED;
                ex = e;
            }
            if (status == null  ||
                    status == ObjectInputFilter.Status.REJECTED) {
                // 记录失败的过滤器检查的调试日志
                if (Logging.infoLogger != null) {
                    Logging.infoLogger.info(
                            "ObjectInputFilter {0}: {1}, array length: {2}, nRefs: {3}, depth: {4}, bytes: {5}, ex: {6}",
                            status, clazz, arrayLength, totalObjectRefs, depth, bytesRead,
                            Objects.toString(ex, "n/a"));
                }
                InvalidClassException ice = new InvalidClassException("filter status: " + status);
                ice.initCause(ex);
                throw ice;
            } else {
                // 记录成功的日志
                if (Logging.traceLogger != null) {
                    Logging.traceLogger.finer(
                            "ObjectInputFilter {0}: {1}, array length: {2}, nRefs: {3}, depth: {4}, bytes: {5}, ex: {6}",
                            status, clazz, arrayLength, totalObjectRefs, depth, bytesRead,
                            Objects.toString(ex, "n/a"));
                }
            }
        }
    }

    /**
     * 检查给定的数组类型和长度，确保创建此类数组是允许的。
     * 数组类型参数必须表示实际的数组类型。
     *
     * 通过 SharedSecrets 调用此私有方法。
     *
     * @param arrayType 数组类型
     * @param arrayLength 数组长度
     * @throws NullPointerException 如果 arrayType 为 null
     * @throws IllegalArgumentException 如果 arrayType 不是实际的数组类型
     * @throws NegativeArraySizeException 如果 arrayLength 为负数
     * @throws InvalidClassException 如果过滤器拒绝创建
     */
    private void checkArray(Class<?> arrayType, int arrayLength) throws InvalidClassException {
        Objects.requireNonNull(arrayType);
        if (! arrayType.isArray()) {
            throw new IllegalArgumentException("not an array type");
        }

        if (arrayLength < 0) {
            throw new NegativeArraySizeException();
        }

        filterCheck(arrayType, arrayLength);
    }

    /**
     * 提供从输入流中读取的持久字段的访问。
     */
    public static abstract class GetField {

        /**
         * 获取描述流中字段的 ObjectStreamClass。
         *
         * @return 描述可序列化字段的描述符类
         */
        public abstract ObjectStreamClass getObjectStreamClass();

        /**
         * 如果命名的字段在流中默认且没有值，则返回 true。
         *
         * @param  name 字段的名称
         * @return 如果且仅如果命名的字段是默认的，则返回 true
         * @throws IOException 如果从底层 <code>InputStream</code> 读取时发生 I/O 错误
         * @throws IllegalArgumentException 如果 <code>name</code> 不对应于可序列化的字段
         */
        public abstract boolean defaulted(String name) throws IOException;

        /**
         * 从持久字段中获取命名的布尔字段的值。
         *
         * @param  name 字段的名称
         * @param  val 如果 <code>name</code> 没有值，则使用的默认值
         * @return 命名的 <code>boolean</code> 字段的值
         * @throws IOException 如果从底层 <code>InputStream</code> 读取时发生 I/O 错误
         * @throws IllegalArgumentException 如果 <code>name</code> 的类型不是可序列化的或字段类型不正确
         */
        public abstract boolean get(String name, boolean val)
            throws IOException;

        /**
         * 从持久字段中获取命名的字节字段的值。
         *
         * @param  name 字段的名称
         * @param  val 如果 <code>name</code> 没有值，则使用的默认值
         * @return 命名的 <code>byte</code> 字段的值
         * @throws IOException 如果从底层 <code>InputStream</code> 读取时发生 I/O 错误
         * @throws IllegalArgumentException 如果 <code>name</code> 的类型不是可序列化的或字段类型不正确
         */
        public abstract byte get(String name, byte val) throws IOException;

        /**
         * 从持久字段中获取命名的字符字段的值。
         *
         * @param  name 字段的名称
         * @param  val 如果 <code>name</code> 没有值，则使用的默认值
         * @return 命名的 <code>char</code> 字段的值
         * @throws IOException 如果从底层 <code>InputStream</code> 读取时发生 I/O 错误
         * @throws IllegalArgumentException 如果 <code>name</code> 的类型不是可序列化的或字段类型不正确
         */
        public abstract char get(String name, char val) throws IOException;


                    /**
         * 从持久化字段中获取指定名称的 short 类型字段的值。
         *
         * @param  name 字段的名称
         * @param  val 如果 <code>name</code> 没有值时使用的默认值
         * @return 指定名称的 <code>short</code> 类型字段的值
         * @throws IOException 从底层 <code>InputStream</code> 读取时发生 I/O 错误
         * @throws IllegalArgumentException 如果 <code>name</code> 的类型不可序列化或字段类型不正确
         */
        public abstract short get(String name, short val) throws IOException;

        /**
         * 从持久化字段中获取指定名称的 int 类型字段的值。
         *
         * @param  name 字段的名称
         * @param  val 如果 <code>name</code> 没有值时使用的默认值
         * @return 指定名称的 <code>int</code> 类型字段的值
         * @throws IOException 从底层 <code>InputStream</code> 读取时发生 I/O 错误
         * @throws IllegalArgumentException 如果 <code>name</code> 的类型不可序列化或字段类型不正确
         */
        public abstract int get(String name, int val) throws IOException;

        /**
         * 从持久化字段中获取指定名称的 long 类型字段的值。
         *
         * @param  name 字段的名称
         * @param  val 如果 <code>name</code> 没有值时使用的默认值
         * @return 指定名称的 <code>long</code> 类型字段的值
         * @throws IOException 从底层 <code>InputStream</code> 读取时发生 I/O 错误
         * @throws IllegalArgumentException 如果 <code>name</code> 的类型不可序列化或字段类型不正确
         */
        public abstract long get(String name, long val) throws IOException;

        /**
         * 从持久化字段中获取指定名称的 float 类型字段的值。
         *
         * @param  name 字段的名称
         * @param  val 如果 <code>name</code> 没有值时使用的默认值
         * @return 指定名称的 <code>float</code> 类型字段的值
         * @throws IOException 从底层 <code>InputStream</code> 读取时发生 I/O 错误
         * @throws IllegalArgumentException 如果 <code>name</code> 的类型不可序列化或字段类型不正确
         */
        public abstract float get(String name, float val) throws IOException;

        /**
         * 从持久化字段中获取指定名称的 double 类型字段的值。
         *
         * @param  name 字段的名称
         * @param  val 如果 <code>name</code> 没有值时使用的默认值
         * @return 指定名称的 <code>double</code> 类型字段的值
         * @throws IOException 从底层 <code>InputStream</code> 读取时发生 I/O 错误
         * @throws IllegalArgumentException 如果 <code>name</code> 的类型不可序列化或字段类型不正确
         */
        public abstract double get(String name, double val) throws IOException;

        /**
         * 从持久化字段中获取指定名称的 Object 类型字段的值。
         *
         * @param  name 字段的名称
         * @param  val 如果 <code>name</code> 没有值时使用的默认值
         * @return 指定名称的 <code>Object</code> 类型字段的值
         * @throws IOException 从底层 <code>InputStream</code> 读取时发生 I/O 错误
         * @throws IllegalArgumentException 如果 <code>name</code> 的类型不可序列化或字段类型不正确
         */
        public abstract Object get(String name, Object val) throws IOException;
    }

    /**
     * 验证此（可能是子类）实例的构造不会违反安全约束：子类不得覆盖安全敏感的非最终方法，否则将检查
     * "enableSubclassImplementation" SerializablePermission。
     */
    private void verifySubclass() {
        Class<?> cl = getClass();
        if (cl == ObjectInputStream.class) {
            return;
        }
        SecurityManager sm = System.getSecurityManager();
        if (sm == null) {
            return;
        }
        processQueue(Caches.subclassAuditsQueue, Caches.subclassAudits);
        WeakClassKey key = new WeakClassKey(cl, Caches.subclassAuditsQueue);
        Boolean result = Caches.subclassAudits.get(key);
        if (result == null) {
            result = Boolean.valueOf(auditSubclass(cl));
            Caches.subclassAudits.putIfAbsent(key, result);
        }
        if (result.booleanValue()) {
            return;
        }
        sm.checkPermission(SUBCLASS_IMPLEMENTATION_PERMISSION);
    }

    /**
     * 对给定的子类执行反射检查，以验证它不会覆盖安全敏感的非最终方法。如果子类“安全”，则返回 true，否则返回 false。
     */
    private static boolean auditSubclass(final Class<?> subcl) {
        Boolean result = AccessController.doPrivileged(
            new PrivilegedAction<Boolean>() {
                public Boolean run() {
                    for (Class<?> cl = subcl;
                         cl != ObjectInputStream.class;
                         cl = cl.getSuperclass())
                    {
                        try {
                            cl.getDeclaredMethod(
                                "readUnshared", (Class[]) null);
                            return Boolean.FALSE;
                        } catch (NoSuchMethodException ex) {
                        }
                        try {
                            cl.getDeclaredMethod("readFields", (Class[]) null);
                            return Boolean.FALSE;
                        } catch (NoSuchMethodException ex) {
                        }
                    }
                    return Boolean.TRUE;
                }
            }
        );
        return result.booleanValue();
    }


                /**
     * 清除内部数据结构。
     */
    private void clear() {
        handles.clear();
        vlist.clear();
    }

    /**
     * 底层 readObject 实现。
     * @param type 预期要反序列化的类型；非空
     * @param unshared 如果对象不能是共享对象的引用，则为 true，否则为 false
     */
    private Object readObject0(Class<?> type, boolean unshared) throws IOException {
        boolean oldMode = bin.getBlockDataMode();
        if (oldMode) {
            int remain = bin.currentBlockRemaining();
            if (remain > 0) {
                throw new OptionalDataException(remain);
            } else if (defaultDataEnd) {
                /*
                 * 修复 4360508：流当前位于通过默认序列化写入的字段值块的末尾；
                 * 由于没有终止的 TC_ENDBLOCKDATA 标签，显式模拟自定义数据结束行为。
                 */
                throw new OptionalDataException(true);
            }
            bin.setBlockDataMode(false);
        }

        byte tc;
        while ((tc = bin.peekByte()) == TC_RESET) {
            bin.readByte();
            handleReset();
        }

        depth++;
        totalObjectRefs++;
        try {
            switch (tc) {
                case TC_NULL:
                    return readNull();

                case TC_REFERENCE:
                    // 检查现有对象的类型
                    return type.cast(readHandle(unshared));

                case TC_CLASS:
                    if (type == String.class) {
                        throw new ClassCastException("不能将类转换为 java.lang.String");
                    }
                    return readClass(unshared);

                case TC_CLASSDESC:
                case TC_PROXYCLASSDESC:
                    if (type == String.class) {
                        throw new ClassCastException("不能将类转换为 java.lang.String");
                    }
                    return readClassDesc(unshared);

                case TC_STRING:
                case TC_LONGSTRING:
                    return checkResolve(readString(unshared));

                case TC_ARRAY:
                    if (type == String.class) {
                        throw new ClassCastException("不能将数组转换为 java.lang.String");
                    }
                    return checkResolve(readArray(unshared));

                case TC_ENUM:
                    if (type == String.class) {
                        throw new ClassCastException("不能将枚举转换为 java.lang.String");
                    }
                    return checkResolve(readEnum(unshared));

                case TC_OBJECT:
                    if (type == String.class) {
                        throw new ClassCastException("不能将对象转换为 java.lang.String");
                    }
                    return checkResolve(readOrdinaryObject(unshared));

                case TC_EXCEPTION:
                    if (type == String.class) {
                        throw new ClassCastException("不能将异常转换为 java.lang.String");
                    }
                    IOException ex = readFatalException();
                    throw new WriteAbortedException("写入中止", ex);

                case TC_BLOCKDATA:
                case TC_BLOCKDATALONG:
                    if (oldMode) {
                        bin.setBlockDataMode(true);
                        bin.peek();             // 强制读取头
                        throw new OptionalDataException(
                            bin.currentBlockRemaining());
                    } else {
                        throw new StreamCorruptedException(
                            "意外的块数据");
                    }

                case TC_ENDBLOCKDATA:
                    if (oldMode) {
                        throw new OptionalDataException(true);
                    } else {
                        throw new StreamCorruptedException(
                            "意外的块数据结束");
                    }

                default:
                    throw new StreamCorruptedException(
                        String.format("无效的类型代码: %02X", tc));
            }
        } finally {
            depth--;
            bin.setBlockDataMode(oldMode);
        }
    }

    /**
     * 如果启用了 resolveObject 并且给定对象没有关联的异常，则调用 resolveObject 确定对象的替代对象，并相应地更新句柄表。
     * 返回替代对象，或如果未发生替代，则返回提供的对象。期望在调用此方法之前将 passHandle 设置为给定对象的句柄。
     */
    private Object checkResolve(Object obj) throws IOException {
        if (!enableResolve || handles.lookupException(passHandle) != null) {
            return obj;
        }
        Object rep = resolveObject(obj);
        if (rep != obj) {
            // 原始对象的类型已被过滤，但 resolveObject 可能已替换它；过滤替代对象的类型
            if (rep != null) {
                if (rep.getClass().isArray()) {
                    filterCheck(rep.getClass(), Array.getLength(rep));
                } else {
                    filterCheck(rep.getClass(), -1);
                }
            }
            handles.setObject(passHandle, rep);
        }
        return rep;
    }

    /**
     * 读取字符串，不允许其在流中被替换。从 ObjectStreamClass.read() 内部调用。
     */
    String readTypeString() throws IOException {
        int oldHandle = passHandle;
        try {
            byte tc = bin.peekByte();
            switch (tc) {
                case TC_NULL:
                    return (String) readNull();

                case TC_REFERENCE:
                    return (String) readHandle(false);

                case TC_STRING:
                case TC_LONGSTRING:
                    return readString(false);


                            default:
                    throw new StreamCorruptedException(
                        String.format("无效的类型代码: %02X", tc));
            }
        } finally {
            passHandle = oldHandle;
        }
    }

    /**
     * 读取空代码，将 passHandle 设置为 NULL_HANDLE 并返回 null。
     */
    private Object readNull() throws IOException {
        if (bin.readByte() != TC_NULL) {
            throw new InternalError();
        }
        passHandle = NULL_HANDLE;
        return null;
    }

    /**
     * 读取对象句柄，将 passHandle 设置为读取的句柄，并返回与句柄关联的对象。
     */
    private Object readHandle(boolean unshared) throws IOException {
        if (bin.readByte() != TC_REFERENCE) {
            throw new InternalError();
        }
        passHandle = bin.readInt() - baseWireHandle;
        if (passHandle < 0 || passHandle >= handles.size()) {
            throw new StreamCorruptedException(
                String.format("无效的句柄值: %08X", passHandle +
                baseWireHandle));
        }
        if (unshared) {
            // 提醒: 这里应该抛出什么类型的异常？
            throw new InvalidObjectException(
                "不能将回引读取为非共享对象");
        }

        Object obj = handles.lookupObject(passHandle);
        if (obj == unsharedMarker) {
            // 提醒: 这里应该抛出什么类型的异常？
            throw new InvalidObjectException(
                "不能读取对非共享对象的回引");
        }
        filterCheck(null, -1);       // 仅检查引用数和深度，不检查类
        return obj;
    }

    /**
     * 读取并返回类对象。将 passHandle 设置为类对象分配的句柄。如果类无法解析（在这种情况下，ClassNotFoundException 将与类的句柄关联在句柄表中），则返回 null。
     */
    private Class<?> readClass(boolean unshared) throws IOException {
        if (bin.readByte() != TC_CLASS) {
            throw new InternalError();
        }
        ObjectStreamClass desc = readClassDesc(false);
        Class<?> cl = desc.forClass();
        passHandle = handles.assign(unshared ? unsharedMarker : cl);

        ClassNotFoundException resolveEx = desc.getResolveException();
        if (resolveEx != null) {
            handles.markException(passHandle, resolveEx);
        }

        handles.finish(passHandle);
        return cl;
    }

    /**
     * 读取并返回（可能为 null）类描述符。将 passHandle 设置为类描述符分配的句柄。如果类描述符无法解析为本地 VM 中的类，则将 ClassNotFoundException 与类描述符的句柄关联。
     */
    private ObjectStreamClass readClassDesc(boolean unshared)
        throws IOException
    {
        byte tc = bin.peekByte();
        ObjectStreamClass descriptor;
        switch (tc) {
            case TC_NULL:
                descriptor = (ObjectStreamClass) readNull();
                break;
            case TC_REFERENCE:
                descriptor = (ObjectStreamClass) readHandle(unshared);
                // 应仅引用已初始化的类描述符
                descriptor.checkInitialized();
                break;
            case TC_PROXYCLASSDESC:
                descriptor = readProxyDesc(unshared);
                break;
            case TC_CLASSDESC:
                descriptor = readNonProxyDesc(unshared);
                break;
            default:
                throw new StreamCorruptedException(
                    String.format("无效的类型代码: %02X", tc));
        }
        if (descriptor != null) {
            validateDescriptor(descriptor);
        }
        return descriptor;
    }

    private boolean isCustomSubclass() {
        // 如果此类是 ObjectInputStream 的自定义子类，则返回 true
        return getClass().getClassLoader()
                    != ObjectInputStream.class.getClassLoader();
    }

    /**
     * 读取并返回动态代理类的类描述符。将 passHandle 设置为代理类描述符分配的句柄。如果代理类描述符无法解析为本地 VM 中的类，则将 ClassNotFoundException 与描述符的句柄关联。
     */
    private ObjectStreamClass readProxyDesc(boolean unshared)
        throws IOException
    {
        if (bin.readByte() != TC_PROXYCLASSDESC) {
            throw new InternalError();
        }

        ObjectStreamClass desc = new ObjectStreamClass();
        int descHandle = handles.assign(unshared ? unsharedMarker : desc);
        passHandle = NULL_HANDLE;

        int numIfaces = bin.readInt();
        if (numIfaces > 65535) {
            // 报告超出规范限制
            throw new InvalidObjectException("接口限制超出: " +
                    numIfaces +
                    ", 限制: " + Caches.PROXY_INTERFACE_LIMIT);
        }
        String[] ifaces = new String[numIfaces];
        for (int i = 0; i < numIfaces; i++) {
            ifaces[i] = bin.readUTF();
        }

        // 重新检查实现限制并抛出带有接口名称的异常
        if (numIfaces > Caches.PROXY_INTERFACE_LIMIT) {
            throw new InvalidObjectException("接口限制超出: " +
                    numIfaces +
                    ", 限制: " + Caches.PROXY_INTERFACE_LIMIT +
                    "; " + Arrays.toString(ifaces));
        }
        Class<?> cl = null;
        ClassNotFoundException resolveEx = null;
        bin.setBlockDataMode(true);
        try {
            if ((cl = resolveProxyClass(ifaces)) == null) {
                resolveEx = new ClassNotFoundException("null 类");
            } else if (!Proxy.isProxyClass(cl)) {
                throw new InvalidClassException("不是代理");
            } else {
                // ReflectUtil.checkProxyPackageAccess 进行的测试
                // 等同于 isCustomSubclass，因此这里不需要条件调用 isCustomSubclass == true。
                ReflectUtil.checkProxyPackageAccess(
                        getClass().getClassLoader(),
                        cl.getInterfaces());
                // 过滤接口
                for (Class<?> clazz : cl.getInterfaces()) {
                    filterCheck(clazz, -1);
                }
            }
        } catch (ClassNotFoundException ex) {
            resolveEx = ex;
        } catch (OutOfMemoryError memerr) {
            IOException ex = new InvalidObjectException("代理接口限制超出: " +
                    Arrays.toString(ifaces));
            ex.initCause(memerr);
            throw ex;
        }


                    // 在读取其他内容之前调用 filterCheck
        filterCheck(cl, -1);

        skipCustomData();

        try {
            totalObjectRefs++;
            depth++;
            desc.initProxy(cl, resolveEx, readClassDesc(false));
        } catch (OutOfMemoryError memerr) {
            IOException ex = new InvalidObjectException("Proxy interface limit exceeded: " +
                    Arrays.toString(ifaces));
            ex.initCause(memerr);
            throw ex;
        } finally {
            depth--;
        }

        handles.finish(descHandle);
        passHandle = descHandle;
        return desc;
    }

    /**
     * 读取并返回一个不是动态代理类的类描述符。设置 passHandle 为类描述符分配的句柄。如果类描述符无法在本地 VM 中解析为类，
     * 则与描述符的句柄关联一个 ClassNotFoundException。
     */
    private ObjectStreamClass readNonProxyDesc(boolean unshared)
        throws IOException
    {
        if (bin.readByte() != TC_CLASSDESC) {
            throw new InternalError();
        }

        ObjectStreamClass desc = new ObjectStreamClass();
        int descHandle = handles.assign(unshared ? unsharedMarker : desc);
        passHandle = NULL_HANDLE;

        ObjectStreamClass readDesc = null;
        try {
            readDesc = readClassDescriptor();
        } catch (ClassNotFoundException ex) {
            throw (IOException) new InvalidClassException(
                "failed to read class descriptor").initCause(ex);
        }

        Class<?> cl = null;
        ClassNotFoundException resolveEx = null;
        bin.setBlockDataMode(true);
        final boolean checksRequired = isCustomSubclass();
        try {
            if ((cl = resolveClass(readDesc)) == null) {
                resolveEx = new ClassNotFoundException("null class");
            } else if (checksRequired) {
                ReflectUtil.checkPackageAccess(cl);
            }
        } catch (ClassNotFoundException ex) {
            resolveEx = ex;
        }

        // 在读取其他内容之前调用 filterCheck
        filterCheck(cl, -1);

        skipCustomData();

        try {
            totalObjectRefs++;
            depth++;
            desc.initNonProxy(readDesc, cl, resolveEx, readClassDesc(false));

            if (cl != null) {
                // 检查本地类描述符的超类是否已进行序列化过滤，以防它未出现在流中。

                // 查找具有本地类描述符的下一个超描述符。
                // 没有本地类的描述符将被忽略。
                ObjectStreamClass superLocal = null;
                for (ObjectStreamClass sDesc = desc.getSuperDesc(); sDesc != null; sDesc = sDesc.getSuperDesc()) {
                    if ((superLocal = sDesc.getLocalDesc()) != null) {
                        break;
                    }
                }

                // 扫描本地描述符的超类，以匹配上述找到的本地描述符。
                // 对于每个匹配前的超描述符，调用序列化过滤器。
                // 对于每个尚未过滤但如果实例由此 Java 运行时序列化将被过滤的类，调用过滤器。
                for (ObjectStreamClass lDesc = desc.getLocalDesc().getSuperDesc();
                     lDesc != null && lDesc != superLocal;
                     lDesc = lDesc.getSuperDesc()) {
                    filterCheck(lDesc.forClass(), -1);
                }
            }
        } finally {
            depth--;
        }

        handles.finish(descHandle);
        passHandle = descHandle;

        return desc;
    }

    /**
     * 读取并返回新字符串。设置 passHandle 为新字符串分配的句柄。
     */
    private String readString(boolean unshared) throws IOException {
        String str;
        byte tc = bin.readByte();
        switch (tc) {
            case TC_STRING:
                str = bin.readUTF();
                break;

            case TC_LONGSTRING:
                str = bin.readLongUTF();
                break;

            default:
                throw new StreamCorruptedException(
                    String.format("invalid type code: %02X", tc));
        }
        passHandle = handles.assign(unshared ? unsharedMarker : str);
        handles.finish(passHandle);
        return str;
    }

    /**
     * 读取并返回数组对象，如果数组类无法解析则返回 null。设置 passHandle 为数组分配的句柄。
     */
    private Object readArray(boolean unshared) throws IOException {
        if (bin.readByte() != TC_ARRAY) {
            throw new InternalError();
        }

        ObjectStreamClass desc = readClassDesc(false);
        int len = bin.readInt();

        filterCheck(desc.forClass(), len);

        Object array = null;
        Class<?> cl, ccl = null;
        if ((cl = desc.forClass()) != null) {
            ccl = cl.getComponentType();
            array = Array.newInstance(ccl, len);
        }

        int arrayHandle = handles.assign(unshared ? unsharedMarker : array);
        ClassNotFoundException resolveEx = desc.getResolveException();
        if (resolveEx != null) {
            handles.markException(arrayHandle, resolveEx);
        }

        if (ccl == null) {
            for (int i = 0; i < len; i++) {
                readObject0(Object.class, false);
            }
        } else if (ccl.isPrimitive()) {
            if (ccl == Integer.TYPE) {
                bin.readInts((int[]) array, 0, len);
            } else if (ccl == Byte.TYPE) {
                bin.readFully((byte[]) array, 0, len, true);
            } else if (ccl == Long.TYPE) {
                bin.readLongs((long[]) array, 0, len);
            } else if (ccl == Float.TYPE) {
                bin.readFloats((float[]) array, 0, len);
            } else if (ccl == Double.TYPE) {
                bin.readDoubles((double[]) array, 0, len);
            } else if (ccl == Short.TYPE) {
                bin.readShorts((short[]) array, 0, len);
            } else if (ccl == Character.TYPE) {
                bin.readChars((char[]) array, 0, len);
            } else if (ccl == Boolean.TYPE) {
                bin.readBooleans((boolean[]) array, 0, len);
            } else {
                throw new InternalError();
            }
        } else {
            Object[] oa = (Object[]) array;
            for (int i = 0; i < len; i++) {
                oa[i] = readObject0(Object.class, false);
                handles.markDependency(arrayHandle, passHandle);
            }
        }


                    handles.finish(arrayHandle);
        passHandle = arrayHandle;
        return array;
    }

    /**
     * 读取并返回枚举常量，如果枚举类型无法解析，则返回 null。将 passHandle 设置为枚举常量的分配句柄。
     */
    private Enum<?> readEnum(boolean unshared) throws IOException {
        if (bin.readByte() != TC_ENUM) {
            throw new InternalError();
        }

        ObjectStreamClass desc = readClassDesc(false);
        if (!desc.isEnum()) {
            throw new InvalidClassException("非枚举类: " + desc);
        }

        int enumHandle = handles.assign(unshared ? unsharedMarker : null);
        ClassNotFoundException resolveEx = desc.getResolveException();
        if (resolveEx != null) {
            handles.markException(enumHandle, resolveEx);
        }

        String name = readString(false);
        Enum<?> result = null;
        Class<?> cl = desc.forClass();
        if (cl != null) {
            try {
                @SuppressWarnings("unchecked")
                Enum<?> en = Enum.valueOf((Class)cl, name);
                result = en;
            } catch (IllegalArgumentException ex) {
                throw (IOException) new InvalidObjectException(
                    "枚举常量 " + name + " 不存在于 " +
                    cl).initCause(ex);
            }
            if (!unshared) {
                handles.setObject(enumHandle, result);
            }
        }

        handles.finish(enumHandle);
        passHandle = enumHandle;
        return result;
    }

    /**
     * 读取并返回“普通”（即，不是字符串、类、ObjectStreamClass、数组或枚举常量）对象，如果对象的类无法解析，则返回 null（在这种情况下，将与对象的句柄关联一个 ClassNotFoundException）。将 passHandle 设置为对象的分配句柄。
     */
    private Object readOrdinaryObject(boolean unshared)
        throws IOException
    {
        if (bin.readByte() != TC_OBJECT) {
            throw new InternalError();
        }

        ObjectStreamClass desc = readClassDesc(false);
        desc.checkDeserialize();

        Class<?> cl = desc.forClass();
        if (cl == String.class || cl == Class.class
                || cl == ObjectStreamClass.class) {
            throw new InvalidClassException("无效的类描述符");
        }

        Object obj;
        try {
            obj = desc.isInstantiable() ? desc.newInstance() : null;
        } catch (Exception ex) {
            throw (IOException) new InvalidClassException(
                desc.forClass().getName(),
                "无法创建实例").initCause(ex);
        }

        passHandle = handles.assign(unshared ? unsharedMarker : obj);
        ClassNotFoundException resolveEx = desc.getResolveException();
        if (resolveEx != null) {
            handles.markException(passHandle, resolveEx);
        }

        if (desc.isExternalizable()) {
            readExternalData((Externalizable) obj, desc);
        } else {
            readSerialData(obj, desc);
        }

        handles.finish(passHandle);

        if (obj != null &&
            handles.lookupException(passHandle) == null &&
            desc.hasReadResolveMethod())
        {
            Object rep = desc.invokeReadResolve(obj);
            if (unshared && rep.getClass().isArray()) {
                rep = cloneArray(rep);
            }
            if (rep != obj) {
                // 过滤替换对象
                if (rep != null) {
                    if (rep.getClass().isArray()) {
                        filterCheck(rep.getClass(), Array.getLength(rep));
                    } else {
                        filterCheck(rep.getClass(), -1);
                    }
                }
                handles.setObject(passHandle, obj = rep);
            }
        }

        return obj;
    }

    /**
     * 如果 obj 非空，则通过调用 obj 的 readExternal() 方法读取外部化数据；否则，尝试跳过外部化数据。
     * 期望在调用此方法之前，passHandle 已被设置为 obj 的句柄。
     */
    private void readExternalData(Externalizable obj, ObjectStreamClass desc)
        throws IOException
    {
        SerialCallbackContext oldContext = curContext;
        if (oldContext != null)
            oldContext.check();
        curContext = null;
        try {
            boolean blocked = desc.hasBlockExternalData();
            if (blocked) {
                bin.setBlockDataMode(true);
            }
            if (obj != null) {
                try {
                    obj.readExternal(this);
                } catch (ClassNotFoundException ex) {
                    /*
                     * 在大多数情况下，句柄表已经在这一点上将 CNFException 传播到 passHandle；此标记调用是为了处理 readExternal
                     * 方法自身构造并抛出新的 CNFException 的情况。
                     */
                     handles.markException(passHandle, ex);
                }
            }
            if (blocked) {
                skipCustomData();
            }
        } finally {
            if (oldContext != null)
                oldContext.check();
            curContext = oldContext;
        }
        /*
         * 在这一点上，如果外部化数据不是以块数据形式写入，并且外部化类在本地不存在（即 obj == null）或 readExternal() 刚刚抛出了
         * CNFException，那么流可能处于不一致状态，因为一些（或全部）外部化数据可能未被消耗。由于在这种情况下没有“正确”的操作，
         * 我们模仿过去的序列化实现的行为，盲目地希望流是同步的；如果它不是，并且流中仍有额外的外部化数据，后续的读取很可能会抛出 StreamCorruptedException。
         */
    }

                /**
     * 读取（或尝试跳过，如果 obj 为 null 或标记有 ClassNotFoundException）流中对象的每个可序列化类的实例数据，从超类到子类。期望在调用此方法之前，passHandle 已设置为 obj 的句柄。
     */
    private void readSerialData(Object obj, ObjectStreamClass desc)
        throws IOException
    {
        ObjectStreamClass.ClassDataSlot[] slots = desc.getClassDataLayout();
        for (int i = 0; i < slots.length; i++) {
            ObjectStreamClass slotDesc = slots[i].desc;

            if (slots[i].hasData) {
                if (obj == null || handles.lookupException(passHandle) != null) {
                    defaultReadFields(null, slotDesc); // 跳过字段值
                } else if (slotDesc.hasReadObjectMethod()) {
                    ThreadDeath t = null;
                    boolean reset = false;
                    SerialCallbackContext oldContext = curContext;
                    if (oldContext != null)
                        oldContext.check();
                    try {
                        curContext = new SerialCallbackContext(obj, slotDesc);

                        bin.setBlockDataMode(true);
                        slotDesc.invokeReadObject(obj, this);
                    } catch (ClassNotFoundException ex) {
                        /*
                         * 在大多数情况下，句柄表已经在这一点将 CNFException 传播到 passHandle；此标记调用是为了处理自定义 readObject 方法创建并抛出新的 CNFException 的情况。
                         */
                        handles.markException(passHandle, ex);
                    } finally {
                        do {
                            try {
                                curContext.setUsed();
                                if (oldContext!= null)
                                    oldContext.check();
                                curContext = oldContext;
                                reset = true;
                            } catch (ThreadDeath x) {
                                t = x;  // 延迟到 reset 为 true 时处理
                            }
                        } while (!reset);
                        if (t != null)
                            throw t;
                    }

                    /*
                     * defaultDataEnd 可能已被自定义 readObject() 方法在调用 defaultReadObject() 或 readFields() 时间接设置；清除它以恢复正常的读取行为。
                     */
                    defaultDataEnd = false;
                } else {
                    defaultReadFields(obj, slotDesc);
                    }

                if (slotDesc.hasWriteObjectData()) {
                    skipCustomData();
                } else {
                    bin.setBlockDataMode(false);
                }
            } else {
                if (obj != null &&
                    slotDesc.hasReadObjectNoDataMethod() &&
                    handles.lookupException(passHandle) == null)
                {
                    slotDesc.invokeReadObjectNoData(obj);
                }
            }
        }
            }

    /**
     * 跳过所有块数据和对象，直到遇到 TC_ENDBLOCKDATA。
     */
    private void skipCustomData() throws IOException {
        int oldHandle = passHandle;
        for (;;) {
            if (bin.getBlockDataMode()) {
                bin.skipBlockData();
                bin.setBlockDataMode(false);
            }
            switch (bin.peekByte()) {
                case TC_BLOCKDATA:
                case TC_BLOCKDATALONG:
                    bin.setBlockDataMode(true);
                    break;

                case TC_ENDBLOCKDATA:
                    bin.readByte();
                    passHandle = oldHandle;
                    return;

                default:
                    readObject0(Object.class, false);
                    break;
            }
        }
    }

    /**
     * 读取给定类描述符声明的可序列化字段的值。如果 obj 不为 null，则在 obj 中设置字段值。期望在调用此方法之前，passHandle 已设置为 obj 的句柄。
     */
    private void defaultReadFields(Object obj, ObjectStreamClass desc)
        throws IOException
    {
        Class<?> cl = desc.forClass();
        if (cl != null && obj != null && !cl.isInstance(obj)) {
            throw new ClassCastException();
        }

        int primDataSize = desc.getPrimDataSize();
        if (primVals == null || primVals.length < primDataSize) {
            primVals = new byte[primDataSize];
        }
            bin.readFully(primVals, 0, primDataSize, false);
        if (obj != null) {
            desc.setPrimFieldValues(obj, primVals);
        }

        int objHandle = passHandle;
        ObjectStreamField[] fields = desc.getFields(false);
        Object[] objVals = new Object[desc.getNumObjFields()];
        int numPrimFields = fields.length - objVals.length;
        for (int i = 0; i < objVals.length; i++) {
            ObjectStreamField f = fields[numPrimFields + i];
            objVals[i] = readObject0(Object.class, f.isUnshared());
            if (f.getField() != null) {
                handles.markDependency(objHandle, passHandle);
            }
        }
        if (obj != null) {
            desc.setObjFieldValues(obj, objVals);
        }
        passHandle = objHandle;
    }

    /**
     * 读取并返回导致序列化中止的 IOException。在读取致命异常之前，丢弃所有流状态。将 passHandle 设置为致命异常的句柄。
     */
    private IOException readFatalException() throws IOException {
        if (bin.readByte() != TC_EXCEPTION) {
            throw new InternalError();
        }
        clear();
        // 检查对象是否跟随 TC_EXCEPTION 类型代码
        byte tc = bin.peekByte();
        if (tc != TC_OBJECT &&
            tc != TC_REFERENCE) {
            throw new StreamCorruptedException(
                    String.format("无效的类型代码: %02X", tc));
        }
        return (IOException) readObject0(Object.class, false);
    }


                /**
     * 如果递归深度为0，清除内部数据结构；否则，
     * 抛出 StreamCorruptedException。当遇到 TC_RESET 类型码时调用此方法。
     */
    private void handleReset() throws StreamCorruptedException {
        if (depth > 0) {
            throw new StreamCorruptedException(
                "意外重置；递归深度: " + depth);
        }
        clear();
    }

    /**
     * 将指定的字节范围转换为浮点值。
     */
    // 提醒：一旦热点内联 Float.intBitsToFloat 就移除
    private static native void bytesToFloats(byte[] src, int srcpos,
                                             float[] dst, int dstpos,
                                             int nfloats);

    /**
     * 将指定的字节范围转换为双精度值。
     */
    // 提醒：一旦热点内联 Double.longBitsToDouble 就移除
    private static native void bytesToDoubles(byte[] src, int srcpos,
                                              double[] dst, int dstpos,
                                              int ndoubles);

    /**
     * 返回堆栈上的第一个非特权类加载器（不包括反射生成的帧），如果仅在堆栈上找到由引导类加载器和扩展类加载器加载的类，
     * 则返回扩展类加载器。此方法也通过反射由以下 RMI-IIOP 类调用：
     *
     *     com.sun.corba.se.internal.util.JDKClassLoader
     *
     * 除非对上述类进行相应的修改，否则不应删除此方法或更改其签名。
     */
    private static ClassLoader latestUserDefinedLoader() {
        return sun.misc.VM.latestUserDefinedLoader();
    }

    /**
     * 默认 GetField 实现。
     */
    private class GetFieldImpl extends GetField {

        /** 描述可序列化字段的类描述符 */
        private final ObjectStreamClass desc;
        /** 基本字段值 */
        private final byte[] primVals;
        /** 对象字段值 */
        private final Object[] objVals;
        /** 对象字段值句柄 */
        private final int[] objHandles;

        /**
         * 为给定的类描述符中定义的字段创建 GetFieldImpl 对象。
         */
        GetFieldImpl(ObjectStreamClass desc) {
            this.desc = desc;
            primVals = new byte[desc.getPrimDataSize()];
            objVals = new Object[desc.getNumObjFields()];
            objHandles = new int[objVals.length];
        }

        public ObjectStreamClass getObjectStreamClass() {
            return desc;
        }

        public boolean defaulted(String name) throws IOException {
            return (getFieldOffset(name, null) < 0);
        }

        public boolean get(String name, boolean val) throws IOException {
            int off = getFieldOffset(name, Boolean.TYPE);
            return (off >= 0) ? Bits.getBoolean(primVals, off) : val;
        }

        public byte get(String name, byte val) throws IOException {
            int off = getFieldOffset(name, Byte.TYPE);
            return (off >= 0) ? primVals[off] : val;
        }

        public char get(String name, char val) throws IOException {
            int off = getFieldOffset(name, Character.TYPE);
            return (off >= 0) ? Bits.getChar(primVals, off) : val;
        }

        public short get(String name, short val) throws IOException {
            int off = getFieldOffset(name, Short.TYPE);
            return (off >= 0) ? Bits.getShort(primVals, off) : val;
        }

        public int get(String name, int val) throws IOException {
            int off = getFieldOffset(name, Integer.TYPE);
            return (off >= 0) ? Bits.getInt(primVals, off) : val;
        }

        public float get(String name, float val) throws IOException {
            int off = getFieldOffset(name, Float.TYPE);
            return (off >= 0) ? Bits.getFloat(primVals, off) : val;
        }

        public long get(String name, long val) throws IOException {
            int off = getFieldOffset(name, Long.TYPE);
            return (off >= 0) ? Bits.getLong(primVals, off) : val;
        }

        public double get(String name, double val) throws IOException {
            int off = getFieldOffset(name, Double.TYPE);
            return (off >= 0) ? Bits.getDouble(primVals, off) : val;
        }

        public Object get(String name, Object val) throws IOException {
            int off = getFieldOffset(name, Object.class);
            if (off >= 0) {
                int objHandle = objHandles[off];
                handles.markDependency(passHandle, objHandle);
                return (handles.lookupException(objHandle) == null) ?
                    objVals[off] : null;
            } else {
                return val;
            }
        }

        /**
         * 从流中读取基本和对象字段值。
         */
        void readFields() throws IOException {
            bin.readFully(primVals, 0, primVals.length, false);

            int oldHandle = passHandle;
            ObjectStreamField[] fields = desc.getFields(false);
            int numPrimFields = fields.length - objVals.length;
            for (int i = 0; i < objVals.length; i++) {
                objVals[i] =
                    readObject0(Object.class, fields[numPrimFields + i].isUnshared());
                objHandles[i] = passHandle;
            }
            passHandle = oldHandle;
        }

        /**
         * 返回具有给定名称和类型的字段的偏移量。指定的类型为 null 匹配所有类型，Object.class 匹配所有非基本类型，
         * 任何其他非 null 类型仅匹配可赋值的类型。如果在（传入的）类描述符中未找到匹配的字段，但在关联的本地类描述符中存在匹配的字段，
         * 则返回 -1。如果传入和本地类描述符中均无匹配项，则抛出 IllegalArgumentException。
         */
        private int getFieldOffset(String name, Class<?> type) {
            ObjectStreamField field = desc.getField(name, type);
            if (field != null) {
                return field.getOffset();
            } else if (desc.getLocalDesc().getField(name, type) != null) {
                return -1;
            } else {
                throw new IllegalArgumentException("没有这样的字段 " + name +
                                                   " 类型为 " + type);
            }
        }
    }


                /**
     * 优先级列表，用于在对象图完全反序列化后执行回调。
     */
    private static class ValidationList {

        private static class Callback {
            final ObjectInputValidation obj;
            final int priority;
            Callback next;
            final AccessControlContext acc;

            Callback(ObjectInputValidation obj, int priority, Callback next,
                AccessControlContext acc)
            {
                this.obj = obj;
                this.priority = priority;
                this.next = next;
                this.acc = acc;
            }
        }

        /** 回调的链表 */
        private Callback list;

        /**
         * 创建新的（空的）ValidationList。
         */
        ValidationList() {
        }

        /**
         * 注册回调。如果回调对象为 null，则抛出 InvalidObjectException。
         */
        void register(ObjectInputValidation obj, int priority)
            throws InvalidObjectException
        {
            if (obj == null) {
                throw new InvalidObjectException("null callback");
            }

            Callback prev = null, cur = list;
            while (cur != null && priority < cur.priority) {
                prev = cur;
                cur = cur.next;
            }
            AccessControlContext acc = AccessController.getContext();
            if (prev != null) {
                prev.next = new Callback(obj, priority, cur, acc);
            } else {
                list = new Callback(obj, priority, list, acc);
            }
        }

        /**
         * 调用所有注册的回调并清除回调列表。
         * 优先级较高的回调先被调用；优先级相同的回调可以以任意顺序调用。如果任何回调抛出 InvalidObjectException，
         * 则回调过程终止并将异常向上抛出。
         */
        void doCallbacks() throws InvalidObjectException {
            try {
                while (list != null) {
                    AccessController.doPrivileged(
                        new PrivilegedExceptionAction<Void>()
                    {
                        public Void run() throws InvalidObjectException {
                            list.obj.validateObject();
                            return null;
                        }
                    }, list.acc);
                    list = list.next;
                }
            } catch (PrivilegedActionException ex) {
                list = null;
                throw (InvalidObjectException) ex.getException();
            }
        }

        /**
         * 将回调列表重置为其初始（空的）状态。
         */
        public void clear() {
            list = null;
        }
    }

    /**
     * 保存要传递给 ObjectInputFilter 的值的快照。
     */
    static class FilterValues implements ObjectInputFilter.FilterInfo {
        final Class<?> clazz;
        final long arrayLength;
        final long totalObjectRefs;
        final long depth;
        final long streamBytes;

        public FilterValues(Class<?> clazz, long arrayLength, long totalObjectRefs,
                            long depth, long streamBytes) {
            this.clazz = clazz;
            this.arrayLength = arrayLength;
            this.totalObjectRefs = totalObjectRefs;
            this.depth = depth;
            this.streamBytes = streamBytes;
        }

        @Override
        public Class<?> serialClass() {
            return clazz;
        }

        @Override
        public long arrayLength() {
            return arrayLength;
        }

        @Override
        public long references() {
            return totalObjectRefs;
        }

        @Override
        public long depth() {
            return depth;
        }

        @Override
        public long streamBytes() {
            return streamBytes;
        }
    }

    /**
     * 支持单字节窥探操作的输入流。
     */
    private static class PeekInputStream extends InputStream {

        /** 底层流 */
        private final InputStream in;
        /** 窥探的字节 */
        private int peekb = -1;
        /** 从流中读取的总字节数 */
        private long totalBytesRead = 0;

        /**
         * 在给定的底层流上创建新的 PeekInputStream。
         */
        PeekInputStream(InputStream in) {
            this.in = in;
        }

        /**
         * 窥探流中的下一个字节值。类似于 read()，但不会消耗读取的值。
         */
        int peek() throws IOException {
            if (peekb >= 0) {
                return peekb;
            }
            peekb = in.read();
            totalBytesRead += peekb >= 0 ? 1 : 0;
            return peekb;
        }

        public int read() throws IOException {
            if (peekb >= 0) {
                int v = peekb;
                peekb = -1;
                return v;
            } else {
                int nbytes = in.read();
                totalBytesRead += nbytes >= 0 ? 1 : 0;
                return nbytes;
            }
        }

        public int read(byte[] b, int off, int len) throws IOException {
            int nbytes;
            if (len == 0) {
                return 0;
            } else if (peekb < 0) {
                nbytes = in.read(b, off, len);
                totalBytesRead += nbytes >= 0 ? nbytes : 0;
                return nbytes;
            } else {
                b[off++] = (byte) peekb;
                len--;
                peekb = -1;
                nbytes = in.read(b, off, len);
                totalBytesRead += nbytes >= 0 ? nbytes : 0;
                return (nbytes >= 0) ? (nbytes + 1) : 1;
            }
        }

        void readFully(byte[] b, int off, int len) throws IOException {
            int n = 0;
            while (n < len) {
                int count = read(b, off + n, len - n);
                if (count < 0) {
                    throw new EOFException();
                }
                n += count;
            }
        }


                    public long skip(long n) throws IOException {
            if (n <= 0) {
                return 0;
            }
            int skipped = 0;
            if (peekb >= 0) {
                peekb = -1;
                skipped++;
                n--;
            }
            n = skipped + in.skip(n);
            totalBytesRead += n;
            return n;
        }

        public int available() throws IOException {
            return in.available() + ((peekb >= 0) ? 1 : 0);
        }

        public void close() throws IOException {
            in.close();
        }

        public long getBytesRead() {
            return totalBytesRead;
        }
    }

    /**
     * 输入流具有两种模式：在默认模式下，以与 DataOutputStream 相同的格式输入数据；在“块数据”模式下，输入的数据由块数据标记包围（有关详细信息，请参阅对象序列化规范）。缓冲取决于块数据模式：在默认模式下，不会提前缓冲任何数据；在块数据模式下，当前数据块的所有数据将一次性读取（并缓冲）。
     */
    private class BlockDataInputStream
        extends InputStream implements DataInput
    {
        /** 最大数据块长度 */
        private static final int MAX_BLOCK_SIZE = 1024;
        /** 最大数据块头长度 */
        private static final int MAX_HEADER_SIZE = 5;
        /** （可调）字符缓冲区长度（用于读取字符串） */
        private static final int CHAR_BUF_SIZE = 256;
        /** readBlockHeader() 返回值，表示读取头可能阻塞 */
        private static final int HEADER_BLOCKED = -2;

        /** 用于读取一般/块数据的缓冲区 */
        private final byte[] buf = new byte[MAX_BLOCK_SIZE];
        /** 用于读取块数据头的缓冲区 */
        private final byte[] hbuf = new byte[MAX_HEADER_SIZE];
        /** 用于快速字符串读取的字符缓冲区 */
        private final char[] cbuf = new char[CHAR_BUF_SIZE];

        /** 块数据模式 */
        private boolean blkmode = false;

        // 块数据状态字段；仅在 blkmode 为 true 时有意义
        /** 当前在 buf 中的偏移量 */
        private int pos = 0;
        /** buf 中有效数据的结束偏移量，或 -1 表示没有更多块数据 */
        private int end = -1;
        /** 当前块中尚未从流中读取的字节数 */
        private int unread = 0;

        /** 底层流（包装在可预览过滤流中） */
        private final PeekInputStream in;
        /** 循环流（用于跨越数据块的数据读取） */
        private final DataInputStream din;

        /**
         * 在给定的底层流上创建新的 BlockDataInputStream。默认情况下，块数据模式关闭。
         */
        BlockDataInputStream(InputStream in) {
            this.in = new PeekInputStream(in);
            din = new DataInputStream(this);
        }

        /**
         * 将块数据模式设置为给定模式（true 表示开启，false 表示关闭）并返回之前的模式值。如果新模式与旧模式相同，则不采取任何操作。如果在仍有未读块数据的情况下将块数据模式从开启切换为关闭，则抛出 IllegalStateException。
         */
        boolean setBlockDataMode(boolean newmode) throws IOException {
            if (blkmode == newmode) {
                return blkmode;
            }
            if (newmode) {
                pos = 0;
                end = 0;
                unread = 0;
            } else if (pos < end) {
                throw new IllegalStateException("未读的块数据");
            }
            blkmode = newmode;
            return !blkmode;
        }

        /**
         * 如果流当前处于块数据模式，则返回 true，否则返回 false。
         */
        boolean getBlockDataMode() {
            return blkmode;
        }

        /**
         * 如果处于块数据模式，跳过当前组数据块的末尾（但不取消块数据模式）。如果不在块数据模式下，则抛出 IllegalStateException。
         */
        void skipBlockData() throws IOException {
            if (!blkmode) {
                throw new IllegalStateException("不在块数据模式下");
            }
            while (end >= 0) {
                refill();
            }
        }

        /**
         * 尝试读取下一个块数据头（如果有）。如果 canBlock 为 false 且无法在可能阻塞的情况下读取完整头，则返回 HEADER_BLOCKED；如果流中的下一个元素是块数据头，则返回头指定的块数据长度；否则返回 -1。
         */
        private int readBlockHeader(boolean canBlock) throws IOException {
            if (defaultDataEnd) {
                /*
                 * 修复 4360508：流当前位于通过默认序列化写入的字段值块的末尾；由于没有终止的 TC_ENDBLOCKDATA 标记，显式模拟自定义数据结束的行为。
                 */
                return -1;
            }
            try {
                for (;;) {
                    int avail = canBlock ? Integer.MAX_VALUE : in.available();
                    if (avail == 0) {
                        return HEADER_BLOCKED;
                    }

                    int tc = in.peek();
                    switch (tc) {
                        case TC_BLOCKDATA:
                            if (avail < 2) {
                                return HEADER_BLOCKED;
                            }
                            in.readFully(hbuf, 0, 2);
                            return hbuf[1] & 0xFF;

                        case TC_BLOCKDATALONG:
                            if (avail < 5) {
                                return HEADER_BLOCKED;
                            }
                            in.readFully(hbuf, 0, 5);
                            int len = Bits.getInt(hbuf, 1);
                            if (len < 0) {
                                throw new StreamCorruptedException(
                                    "非法的块数据头长度: " +
                                    len);
                            }
                            return len;

                                    /*
                         * TC_RESET 可能在数据块之间发生。
                         * 不幸的是，这种情况必须在比其他类型代码更低的级别解析，
                         * 因为原始数据读取可能跨越由 TC_RESET 分隔的数据块。
                         */
                        case TC_RESET:
                            in.read();
                            handleReset();
                            break;

                        default:
                            if (tc >= 0 && (tc < TC_BASE || tc > TC_MAX)) {
                                throw new StreamCorruptedException(
                                    String.format("无效的类型代码: %02X",
                                    tc));
                            }
                            return -1;
                    }
                }
            } catch (EOFException ex) {
                throw new StreamCorruptedException(
                    "读取块数据头时意外到达文件末尾");
            }
        }

        /**
         * 用块数据填充内部缓冲区 buf。调用时 buf 中的任何数据都被视为已消耗。
         * 设置 pos、end 和 unread 字段以反映可用块数据的新数量；
         * 如果流中的下一个元素不是数据块，则将 pos 和 unread 设置为 0，end 设置为 -1。
         */
        private void refill() throws IOException {
            try {
                do {
                    pos = 0;
                    if (unread > 0) {
                        int n =
                            in.read(buf, 0, Math.min(unread, MAX_BLOCK_SIZE));
                        if (n >= 0) {
                            end = n;
                            unread -= n;
                        } else {
                            throw new StreamCorruptedException(
                                "数据块中间意外到达文件末尾");
                        }
                    } else {
                        int n = readBlockHeader(true);
                        if (n >= 0) {
                            end = 0;
                            unread = n;
                        } else {
                            end = -1;
                            unread = 0;
                        }
                    }
                } while (pos == end);
            } catch (IOException ex) {
                pos = 0;
                end = -1;
                unread = 0;
                throw ex;
            }
        }

        /**
         * 如果处于块数据模式，返回当前数据块中未消耗的字节数。
         * 如果不处于块数据模式，则抛出 IllegalStateException。
         */
        int currentBlockRemaining() {
            if (blkmode) {
                return (end >= 0) ? (end - pos) + unread : 0;
            } else {
                throw new IllegalStateException();
            }
        }

        /**
         * 查看（但不消耗）并返回流中的下一个字节值，或者如果已到达流/块数据的末尾（如果处于块数据模式）则返回 -1。
         */
        int peek() throws IOException {
            if (blkmode) {
                if (pos == end) {
                    refill();
                }
                return (end >= 0) ? (buf[pos] & 0xFF) : -1;
            } else {
                return in.peek();
            }
        }

        /**
         * 查看（但不消耗）并返回流中的下一个字节值，或者如果已到达流/块数据的末尾则抛出 EOFException。
         */
        byte peekByte() throws IOException {
            int val = peek();
            if (val < 0) {
                throw new EOFException();
            }
            return (byte) val;
        }


        /* ----------------- 通用输入流方法 ------------------ */
        /*
         * 以下方法等同于 InputStream 中的对应方法，不同之处在于它们解释数据块边界，
         * 并在块数据模式下从数据块中读取请求的数据。
         */

        public int read() throws IOException {
            if (blkmode) {
                if (pos == end) {
                    refill();
                }
                return (end >= 0) ? (buf[pos++] & 0xFF) : -1;
            } else {
                return in.read();
            }
        }

        public int read(byte[] b, int off, int len) throws IOException {
            return read(b, off, len, false);
        }

        public long skip(long len) throws IOException {
            long remain = len;
            while (remain > 0) {
                if (blkmode) {
                    if (pos == end) {
                        refill();
                    }
                    if (end < 0) {
                        break;
                    }
                    int nread = (int) Math.min(remain, end - pos);
                    remain -= nread;
                    pos += nread;
                } else {
                    int nread = (int) Math.min(remain, MAX_BLOCK_SIZE);
                    if ((nread = in.read(buf, 0, nread)) < 0) {
                        break;
                    }
                    remain -= nread;
                }
            }
            return len - remain;
        }

        public int available() throws IOException {
            if (blkmode) {
                if ((pos == end) && (unread == 0)) {
                    int n;
                    while ((n = readBlockHeader(false)) == 0) ;
                    switch (n) {
                        case HEADER_BLOCKED:
                            break;

                        case -1:
                            pos = 0;
                            end = -1;
                            break;

                        default:
                            pos = 0;
                            end = 0;
                            unread = n;
                            break;
                    }
                }
                // 如果可能，避免不必要的 in.available() 调用
                int unreadAvail = (unread > 0) ?
                    Math.min(in.available(), unread) : 0;
                return (end >= 0) ? (end - pos) + unreadAvail : 0;
            } else {
                return in.available();
            }
        }


                    public void close() throws IOException {
            if (blkmode) {
                pos = 0;
                end = -1;
                unread = 0;
            }
            in.close();
        }

        /**
         * 尝试将 len 字节读入字节数组 b 的偏移 off 处。返回读取的字节数，或者如果已到达流/块数据的末尾，则返回 -1。
         * 如果 copy 为 true，则将值读入中间缓冲区，然后再复制到 b（以避免暴露 b 的引用）。
         */
        int read(byte[] b, int off, int len, boolean copy) throws IOException {
            if (len == 0) {
                return 0;
            } else if (blkmode) {
                if (pos == end) {
                    refill();
                }
                if (end < 0) {
                    return -1;
                }
                int nread = Math.min(len, end - pos);
                System.arraycopy(buf, pos, b, off, nread);
                pos += nread;
                return nread;
            } else if (copy) {
                int nread = in.read(buf, 0, Math.min(len, MAX_BLOCK_SIZE));
                if (nread > 0) {
                    System.arraycopy(buf, 0, b, off, nread);
                }
                return nread;
            } else {
                return in.read(b, off, len);
            }
        }

        /* ----------------- 基本数据输入方法 ------------------ */
        /*
         * 以下方法等同于 DataInputStream 中的对应方法，但它们解释数据块边界并在块数据模式下从数据块中读取请求的数据。
         */

        public void readFully(byte[] b) throws IOException {
            readFully(b, 0, b.length, false);
        }

        public void readFully(byte[] b, int off, int len) throws IOException {
            readFully(b, off, len, false);
        }

        public void readFully(byte[] b, int off, int len, boolean copy)
            throws IOException
        {
            while (len > 0) {
                int n = read(b, off, len, copy);
                if (n < 0) {
                    throw new EOFException();
                }
                off += n;
                len -= n;
            }
        }

        public int skipBytes(int n) throws IOException {
            return din.skipBytes(n);
        }

        public boolean readBoolean() throws IOException {
            int v = read();
            if (v < 0) {
                throw new EOFException();
            }
            return (v != 0);
        }

        public byte readByte() throws IOException {
            int v = read();
            if (v < 0) {
                throw new EOFException();
            }
            return (byte) v;
        }

        public int readUnsignedByte() throws IOException {
            int v = read();
            if (v < 0) {
                throw new EOFException();
            }
            return v;
        }

        public char readChar() throws IOException {
            if (!blkmode) {
                pos = 0;
                in.readFully(buf, 0, 2);
            } else if (end - pos < 2) {
                return din.readChar();
            }
            char v = Bits.getChar(buf, pos);
            pos += 2;
            return v;
        }

        public short readShort() throws IOException {
            if (!blkmode) {
                pos = 0;
                in.readFully(buf, 0, 2);
            } else if (end - pos < 2) {
                return din.readShort();
            }
            short v = Bits.getShort(buf, pos);
            pos += 2;
            return v;
        }

        public int readUnsignedShort() throws IOException {
            if (!blkmode) {
                pos = 0;
                in.readFully(buf, 0, 2);
            } else if (end - pos < 2) {
                return din.readUnsignedShort();
            }
            int v = Bits.getShort(buf, pos) & 0xFFFF;
            pos += 2;
            return v;
        }

        public int readInt() throws IOException {
            if (!blkmode) {
                pos = 0;
                in.readFully(buf, 0, 4);
            } else if (end - pos < 4) {
                return din.readInt();
            }
            int v = Bits.getInt(buf, pos);
            pos += 4;
            return v;
        }

        public float readFloat() throws IOException {
            if (!blkmode) {
                pos = 0;
                in.readFully(buf, 0, 4);
            } else if (end - pos < 4) {
                return din.readFloat();
            }
            float v = Bits.getFloat(buf, pos);
            pos += 4;
            return v;
        }

        public long readLong() throws IOException {
            if (!blkmode) {
                pos = 0;
                in.readFully(buf, 0, 8);
            } else if (end - pos < 8) {
                return din.readLong();
            }
            long v = Bits.getLong(buf, pos);
            pos += 8;
            return v;
        }

        public double readDouble() throws IOException {
            if (!blkmode) {
                pos = 0;
                in.readFully(buf, 0, 8);
            } else if (end - pos < 8) {
                return din.readDouble();
            }
            double v = Bits.getDouble(buf, pos);
            pos += 8;
            return v;
        }

        public String readUTF() throws IOException {
            return readUTFBody(readUnsignedShort());
        }

        @SuppressWarnings("deprecation")
        public String readLine() throws IOException {
            return din.readLine();      // 已弃用，不值得优化
        }

        /* -------------- 基本数据数组输入方法 --------------- */
        /*
         * 以下方法读取一组基本数据值。虽然等同于反复调用相应的基本读取方法，但这些方法经过优化，可以更高效地读取一组基本数据值。
         */


void readBooleans(boolean[] v, int off, int len) throws IOException {
    int stop, endoff = off + len;
    while (off < endoff) {
        if (!blkmode) {
            int span = Math.min(endoff - off, MAX_BLOCK_SIZE);
            in.readFully(buf, 0, span);
            stop = off + span;
            pos = 0;
        } else if (end - pos < 1) {
            v[off++] = din.readBoolean();
            continue;
        } else {
            stop = Math.min(endoff, off + end - pos);
        }

        while (off < stop) {
            v[off++] = Bits.getBoolean(buf, pos++);
        }
    }
}

void readChars(char[] v, int off, int len) throws IOException {
    int stop, endoff = off + len;
    while (off < endoff) {
        if (!blkmode) {
            int span = Math.min(endoff - off, MAX_BLOCK_SIZE >> 1);
            in.readFully(buf, 0, span << 1);
            stop = off + span;
            pos = 0;
        } else if (end - pos < 2) {
            v[off++] = din.readChar();
            continue;
        } else {
            stop = Math.min(endoff, off + ((end - pos) >> 1));
        }

        while (off < stop) {
            v[off++] = Bits.getChar(buf, pos);
            pos += 2;
        }
    }
}

void readShorts(short[] v, int off, int len) throws IOException {
    int stop, endoff = off + len;
    while (off < endoff) {
        if (!blkmode) {
            int span = Math.min(endoff - off, MAX_BLOCK_SIZE >> 1);
            in.readFully(buf, 0, span << 1);
            stop = off + span;
            pos = 0;
        } else if (end - pos < 2) {
            v[off++] = din.readShort();
            continue;
        } else {
            stop = Math.min(endoff, off + ((end - pos) >> 1));
        }

        while (off < stop) {
            v[off++] = Bits.getShort(buf, pos);
            pos += 2;
        }
    }
}

void readInts(int[] v, int off, int len) throws IOException {
    int stop, endoff = off + len;
    while (off < endoff) {
        if (!blkmode) {
            int span = Math.min(endoff - off, MAX_BLOCK_SIZE >> 2);
            in.readFully(buf, 0, span << 2);
            stop = off + span;
            pos = 0;
        } else if (end - pos < 4) {
            v[off++] = din.readInt();
            continue;
        } else {
            stop = Math.min(endoff, off + ((end - pos) >> 2));
        }

        while (off < stop) {
            v[off++] = Bits.getInt(buf, pos);
            pos += 4;
        }
    }
}

void readFloats(float[] v, int off, int len) throws IOException {
    int span, endoff = off + len;
    while (off < endoff) {
        if (!blkmode) {
            span = Math.min(endoff - off, MAX_BLOCK_SIZE >> 2);
            in.readFully(buf, 0, span << 2);
            pos = 0;
        } else if (end - pos < 4) {
            v[off++] = din.readFloat();
            continue;
        } else {
            span = Math.min(endoff - off, ((end - pos) >> 2));
        }

        bytesToFloats(buf, pos, v, off, span);
        off += span;
        pos += span << 2;
    }
}

void readLongs(long[] v, int off, int len) throws IOException {
    int stop, endoff = off + len;
    while (off < endoff) {
        if (!blkmode) {
            int span = Math.min(endoff - off, MAX_BLOCK_SIZE >> 3);
            in.readFully(buf, 0, span << 3);
            stop = off + span;
            pos = 0;
        } else if (end - pos < 8) {
            v[off++] = din.readLong();
            continue;
        } else {
            stop = Math.min(endoff, off + ((end - pos) >> 3));
        }

        while (off < stop) {
            v[off++] = Bits.getLong(buf, pos);
            pos += 8;
        }
    }
}

void readDoubles(double[] v, int off, int len) throws IOException {
    int span, endoff = off + len;
    while (off < endoff) {
        if (!blkmode) {
            span = Math.min(endoff - off, MAX_BLOCK_SIZE >> 3);
            in.readFully(buf, 0, span << 3);
            pos = 0;
        } else if (end - pos < 8) {
            v[off++] = din.readDouble();
            continue;
        } else {
            span = Math.min(endoff - off, ((end - pos) >> 3));
        }

        bytesToDoubles(buf, pos, v, off, span);
        off += span;
        pos += span << 3;
    }
}

/**
 * Reads in string written in "long" UTF format.  "Long" UTF format is
 * identical to standard UTF, except that it uses an 8 byte header
 * (instead of the standard 2 bytes) to convey the UTF encoding length.
 */
String readLongUTF() throws IOException {
    return readUTFBody(readLong());
}

/**
 * Reads in the "body" (i.e., the UTF representation minus the 2-byte
 * or 8-byte length header) of a UTF encoding, which occupies the next
 * utflen bytes.
 */
private String readUTFBody(long utflen) throws IOException {
    StringBuilder sbuf;
    if (utflen > 0 && utflen < Integer.MAX_VALUE) {
        // a reasonable initial capacity based on the UTF length
        int initialCapacity = Math.min((int)utflen, 0xFFFF);
        sbuf = new StringBuilder(initialCapacity);
    } else {
        sbuf = new StringBuilder();
    }


                        if (!blkmode) {
                end = pos = 0;
            }

            while (utflen > 0) {
                int avail = end - pos;
                if (avail >= 3 || (long) avail == utflen) {
                    utflen -= readUTFSpan(sbuf, utflen);
                } else {
                    if (blkmode) {
                        // near block boundary, read one byte at a time
                        utflen -= readUTFChar(sbuf, utflen);
                    } else {
                        // shift and refill buffer manually
                        if (avail > 0) {
                            System.arraycopy(buf, pos, buf, 0, avail);
                        }
                        pos = 0;
                        end = (int) Math.min(MAX_BLOCK_SIZE, utflen);
                        in.readFully(buf, avail, end - avail);
                    }
                }
            }

            return sbuf.toString();
        }

        /**
         * 从内部缓冲区（从偏移量 pos 开始，到偏移量 end 之前结束）读取 UTF 编码的字符，
         * 消耗不超过 utflen 字节。将读取的字符追加到 sbuf。返回消耗的字节数。
         */
        private long readUTFSpan(StringBuilder sbuf, long utflen)
            throws IOException
        {
            int cpos = 0;
            int start = pos;
            int avail = Math.min(end - pos, CHAR_BUF_SIZE);
            // 除非所有 UTF 字节都在缓冲区中，否则在最后一个字符之前停止
            int stop = pos + ((utflen > avail) ? avail - 2 : (int) utflen);
            boolean outOfBounds = false;

            try {
                while (pos < stop) {
                    int b1, b2, b3;
                    b1 = buf[pos++] & 0xFF;
                    switch (b1 >> 4) {
                        case 0:
                        case 1:
                        case 2:
                        case 3:
                        case 4:
                        case 5:
                        case 6:
                        case 7:   // 1 字节格式: 0xxxxxxx
                            cbuf[cpos++] = (char) b1;
                            break;

                        case 12:
                        case 13:  // 2 字节格式: 110xxxxx 10xxxxxx
                            b2 = buf[pos++];
                            if ((b2 & 0xC0) != 0x80) {
                                throw new UTFDataFormatException();
                            }
                            cbuf[cpos++] = (char) (((b1 & 0x1F) << 6) |
                                                   ((b2 & 0x3F) << 0));
                            break;

                        case 14:  // 3 字节格式: 1110xxxx 10xxxxxx 10xxxxxx
                            b3 = buf[pos + 1];
                            b2 = buf[pos + 0];
                            pos += 2;
                            if ((b2 & 0xC0) != 0x80 || (b3 & 0xC0) != 0x80) {
                                throw new UTFDataFormatException();
                            }
                            cbuf[cpos++] = (char) (((b1 & 0x0F) << 12) |
                                                   ((b2 & 0x3F) << 6) |
                                                   ((b3 & 0x3F) << 0));
                            break;

                        default:  // 10xx xxxx, 1111 xxxx
                            throw new UTFDataFormatException();
                    }
                }
            } catch (ArrayIndexOutOfBoundsException ex) {
                outOfBounds = true;
            } finally {
                if (outOfBounds || (pos - start) > utflen) {
                    /*
                     * 修复 4450867: 如果一个畸形的 UTF 字符导致转换循环扫描到预期的 UTF
                     * 字符串结束位置之后，只消耗预期数量的 UTF 字节。
                     */
                    pos = start + (int) utflen;
                    throw new UTFDataFormatException();
                }
            }

            sbuf.append(cbuf, 0, cpos);
            return pos - start;
        }

        /**
         * 一次读取一个字节，读取单个 UTF 编码的字符，将字符追加到 sbuf，并返回消耗的字节数。
         * 当在块数据模式下读取 UTF 字符串时，此方法用于处理可能跨越块数据边界的 UTF 编码字符。
         */
        private int readUTFChar(StringBuilder sbuf, long utflen)
            throws IOException
        {
            int b1, b2, b3;
            b1 = readByte() & 0xFF;
            switch (b1 >> 4) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:     // 1 字节格式: 0xxxxxxx
                    sbuf.append((char) b1);
                    return 1;

                case 12:
                case 13:    // 2 字节格式: 110xxxxx 10xxxxxx
                    if (utflen < 2) {
                        throw new UTFDataFormatException();
                    }
                    b2 = readByte();
                    if ((b2 & 0xC0) != 0x80) {
                        throw new UTFDataFormatException();
                    }
                    sbuf.append((char) (((b1 & 0x1F) << 6) |
                                        ((b2 & 0x3F) << 0)));
                    return 2;

                case 14:    // 3 字节格式: 1110xxxx 10xxxxxx 10xxxxxx
                    if (utflen < 3) {
                        if (utflen == 2) {
                            readByte();         // 消耗剩余的字节
                        }
                        throw new UTFDataFormatException();
                    }
                    b2 = readByte();
                    b3 = readByte();
                    if ((b2 & 0xC0) != 0x80 || (b3 & 0xC0) != 0x80) {
                        throw new UTFDataFormatException();
                    }
                    sbuf.append((char) (((b1 & 0x0F) << 12) |
                                        ((b2 & 0x3F) << 6) |
                                        ((b3 & 0x3F) << 0)));
                    return 3;


                            default:   // 10xx xxxx, 1111 xxxx
                    throw new UTFDataFormatException();
            }
        }

        /**
         * 返回从输入流中读取的字节数。
         * @return 从输入流中读取的字节数
         */
        long getBytesRead() {
            return in.getBytesRead();
        }
    }

    /**
     * 未同步的表，用于跟踪线路上的句柄到对象的映射，以及与反序列化对象关联的ClassNotFoundExceptions。
     * 此类实现了一个异常传播算法，用于确定哪些对象应与ClassNotFoundExceptions关联，同时考虑对象图中的循环和不连续性（例如，跳过的字段）。
     *
     * <p>表的一般使用方法如下：在反序列化过程中，首先通过调用assign方法为给定对象分配一个句柄。
     * 此方法将分配的句柄置于“打开”状态，在此状态下，可以通过调用markDependency方法注册对其他句柄的异常状态的依赖关系，
     * 或通过调用markException方法直接将异常与句柄关联。当一个句柄被标记为异常时，HandleTable将负责将异常传播给任何依赖于（传递地）异常标记对象的其他对象。
     *
     * <p>一旦句柄的所有异常信息/依赖关系都已注册，应通过调用其上的finish方法来“关闭”句柄。
     * 关闭句柄的行为允许异常传播算法积极地剪枝依赖关系链接，减少异常跟踪的性能/内存影响。
     *
     * <p>请注意，使用的异常传播算法依赖于句柄按LIFO顺序分配/完成；然而，为了简单以及节省内存，它并不强制执行此约束。
     */
    // REMIND: 添加异常传播算法的完整描述？
    private static class HandleTable {

        /* 状态代码，指示对象是否有关联的异常 */
        private static final byte STATUS_OK = 1;
        private static final byte STATUS_UNKNOWN = 2;
        private static final byte STATUS_EXCEPTION = 3;

        /** 数组映射句柄 -> 对象状态 */
        byte[] status;
        /** 数组映射句柄 -> 对象/异常（取决于状态） */
        Object[] entries;
        /** 数组映射句柄 -> 依赖句柄列表（如果有） */
        HandleList[] deps;
        /** 最低未解决的依赖关系 */
        int lowDep = -1;
        /** 表中的句柄数 */
        int size = 0;

        /**
         * 创建具有给定初始容量的句柄表。
         */
        HandleTable(int initialCapacity) {
            status = new byte[initialCapacity];
            entries = new Object[initialCapacity];
            deps = new HandleList[initialCapacity];
        }

        /**
         * 为给定对象分配下一个可用的句柄，并返回分配的句柄。一旦对象完全反序列化（并且所有对其他对象的依赖关系都已识别），
         * 应通过将其传递给finish()来“关闭”句柄。
         */
        int assign(Object obj) {
            if (size >= entries.length) {
                grow();
            }
            status[size] = STATUS_UNKNOWN;
            entries[size] = obj;
            return size++;
        }

        /**
         * 注册一个句柄对另一个句柄的依赖关系（在异常状态上）。依赖的句柄必须是“打开”的（即，已分配但尚未完成）。
         * 如果依赖句柄或目标句柄是NULL_HANDLE，则不采取任何行动。
         */
        void markDependency(int dependent, int target) {
            if (dependent == NULL_HANDLE || target == NULL_HANDLE) {
                return;
            }
            switch (status[dependent]) {

                case STATUS_UNKNOWN:
                    switch (status[target]) {
                        case STATUS_OK:
                            // 忽略对没有异常的对象的依赖
                            break;

                        case STATUS_EXCEPTION:
                            // 立即传播异常
                            markException(dependent,
                                (ClassNotFoundException) entries[target]);
                            break;

                        case STATUS_UNKNOWN:
                            // 添加到目标的依赖列表
                            if (deps[target] == null) {
                                deps[target] = new HandleList();
                            }
                            deps[target].add(dependent);

                            // 记录看到的最低未解决的目标
                            if (lowDep < 0 || lowDep > target) {
                                lowDep = target;
                            }
                            break;

                        default:
                            throw new InternalError();
                    }
                    break;

                case STATUS_EXCEPTION:
                    break;

                default:
                    throw new InternalError();
            }
        }

        /**
         * 将ClassNotFoundException（如果尚未关联）与当前活动的句柄关联，并根据需要将其传播到其他引用对象。
         * 指定的句柄必须是“打开”的（即，已分配但尚未完成）。
         */
        void markException(int handle, ClassNotFoundException ex) {
            switch (status[handle]) {
                case STATUS_UNKNOWN:
                    status[handle] = STATUS_EXCEPTION;
                    entries[handle] = ex;

                    // 将异常传播给依赖项
                    HandleList dlist = deps[handle];
                    if (dlist != null) {
                        int ndeps = dlist.size();
                        for (int i = 0; i < ndeps; i++) {
                            markException(dlist.get(i), ex);
                        }
                        deps[handle] = null;
                    }
                    break;


                            case STATUS_EXCEPTION:
                    break;

                default:
                    throw new InternalError();
            }
        }

        /**
         * 标记给定的句柄为完成，这意味着不会再为该句柄标记新的依赖项。
         * 调用分配和完成方法必须按后进先出（LIFO）顺序进行。
         */
        void finish(int handle) {
            int end;
            if (lowDep < 0) {
                // 没有待处理的未知项，仅解析当前句柄
                end = handle + 1;
            } else if (lowDep >= handle) {
                // 现在可以清除待处理的未知项，解析所有向上的句柄
                end = size;
                lowDep = -1;
            } else {
                // 存在未解决的回引，暂时无法解析任何内容
                return;
            }

            // 在选定的句柄范围内将 STATUS_UNKNOWN 更改为 STATUS_OK
            for (int i = handle; i < end; i++) {
                switch (status[i]) {
                    case STATUS_UNKNOWN:
                        status[i] = STATUS_OK;
                        deps[i] = null;
                        break;

                    case STATUS_OK:
                    case STATUS_EXCEPTION:
                        break;

                    default:
                        throw new InternalError();
                }
            }
        }

        /**
         * 为给定的句柄分配一个新对象。与句柄关联的先前对象将被遗忘。
         * 如果给定的句柄已经关联了一个异常，则此方法无效。
         * 在句柄分配后，可以随时调用此方法。
         */
        void setObject(int handle, Object obj) {
            switch (status[handle]) {
                case STATUS_UNKNOWN:
                case STATUS_OK:
                    entries[handle] = obj;
                    break;

                case STATUS_EXCEPTION:
                    break;

                default:
                    throw new InternalError();
            }
        }

        /**
         * 查找并返回与给定句柄关联的对象。
         * 如果给定的句柄是 NULL_HANDLE，或者它关联了一个 ClassNotFoundException，则返回 null。
         */
        Object lookupObject(int handle) {
            return (handle != NULL_HANDLE &&
                    status[handle] != STATUS_EXCEPTION) ?
                entries[handle] : null;
        }

        /**
         * 查找并返回与给定句柄关联的 ClassNotFoundException。
         * 如果给定的句柄是 NULL_HANDLE，或者没有与句柄关联的 ClassNotFoundException，则返回 null。
         */
        ClassNotFoundException lookupException(int handle) {
            return (handle != NULL_HANDLE &&
                    status[handle] == STATUS_EXCEPTION) ?
                (ClassNotFoundException) entries[handle] : null;
        }

        /**
         * 重置表到其初始状态。
         */
        void clear() {
            Arrays.fill(status, 0, size, (byte) 0);
            Arrays.fill(entries, 0, size, null);
            Arrays.fill(deps, 0, size, null);
            lowDep = -1;
            size = 0;
        }

        /**
         * 返回表中注册的句柄数量。
         */
        int size() {
            return size;
        }

        /**
         * 扩展内部数组的容量。
         */
        private void grow() {
            int newCapacity = (entries.length << 1) + 1;

            byte[] newStatus = new byte[newCapacity];
            Object[] newEntries = new Object[newCapacity];
            HandleList[] newDeps = new HandleList[newCapacity];

            System.arraycopy(status, 0, newStatus, 0, size);
            System.arraycopy(entries, 0, newEntries, 0, size);
            System.arraycopy(deps, 0, newDeps, 0, size);

            status = newStatus;
            entries = newEntries;
            deps = newDeps;
        }

        /**
         * 简单的可增长的（整数）句柄列表。
         */
        private static class HandleList {
            private int[] list = new int[4];
            private int size = 0;

            public HandleList() {
            }

            public void add(int handle) {
                if (size >= list.length) {
                    int[] newList = new int[list.length << 1];
                    System.arraycopy(list, 0, newList, 0, list.length);
                    list = newList;
                }
                list[size++] = handle;
            }

            public int get(int index) {
                if (index >= size) {
                    throw new ArrayIndexOutOfBoundsException();
                }
                return list[index];
            }

            public int size() {
                return size;
            }
        }
    }

    /**
     * 在使用非共享读取时克隆数组的方法。
     */
    private static Object cloneArray(Object array) {
        if (array instanceof Object[]) {
            return ((Object[]) array).clone();
        } else if (array instanceof boolean[]) {
            return ((boolean[]) array).clone();
        } else if (array instanceof byte[]) {
            return ((byte[]) array).clone();
        } else if (array instanceof char[]) {
            return ((char[]) array).clone();
        } else if (array instanceof double[]) {
            return ((double[]) array).clone();
        } else if (array instanceof float[]) {
            return ((float[]) array).clone();
        } else if (array instanceof int[]) {
            return ((int[]) array).clone();
        } else if (array instanceof long[]) {
            return ((long[]) array).clone();
        } else if (array instanceof short[]) {
            return ((short[]) array).clone();
        } else {
            throw new AssertionError();
        }
    }

    private void validateDescriptor(ObjectStreamClass descriptor) {
        ObjectStreamClassValidator validating = validator;
        if (validating != null) {
            validating.validateDescriptor(descriptor);
        }
    }

                // 控制对 ObjectStreamClassValidator 的访问
    private volatile ObjectStreamClassValidator validator;

    private static void setValidator(ObjectInputStream ois, ObjectStreamClassValidator validator) {
        ois.validator = validator;
    }
    static {
        SharedSecrets.setJavaObjectInputStreamAccess(ObjectInputStream::setValidator);
        SharedSecrets.setJavaObjectInputStreamReadString(ObjectInputStream::readString);
    }
}
