
/*
 * Copyright (c) 1996, 2013, Oracle and/or its affiliates. All rights reserved.
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

package java.io;

import java.io.ObjectStreamClass.WeakClassKey;
import java.lang.ref.ReferenceQueue;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import static java.io.ObjectStreamClass.processQueue;
import java.io.SerialCallbackContext;
import sun.reflect.misc.ReflectUtil;

/**
 * 一个 ObjectOutputStream 将基本数据类型和 Java 对象图写入 OutputStream。这些对象可以使用
 * ObjectInputStream 读取（重构）。如果流是一个文件，可以实现对象的持久化存储。如果流是一个网络
 * 套接字流，对象可以在另一台主机或另一个进程中重构。
 *
 * <p>只有支持 java.io.Serializable 接口的对象才能写入流。每个可序列化对象的类都会被编码，包括
 * 类名和类签名、对象字段和数组的值，以及从初始对象引用的任何其他对象的闭包。
 *
 * <p>使用 writeObject 方法将对象写入流。任何对象，包括字符串和数组，都使用 writeObject 写入。
 * 可以写入多个对象或基本类型。必须使用相同类型并按相同顺序从对应的 ObjectInputStream 读取这些对象。
 *
 * <p>也可以使用 DataOutput 接口提供的方法将基本数据类型写入流。字符串也可以使用 writeUTF 方法写入。
 *
 * <p>默认的序列化机制为对象写入其类、类签名和所有非瞬态和非静态字段的值。对其他对象的引用（除瞬态或
 * 静态字段外）也会导致这些对象被写入。多个对单个对象的引用使用引用共享机制编码，以便可以将对象图恢复
 * 到与最初写入时相同的形状。
 *
 * <p>例如，写入一个对象，该对象可以被 ObjectInputStream 示例读取：
 * <br>
 * <pre>
 *      FileOutputStream fos = new FileOutputStream("t.tmp");
 *      ObjectOutputStream oos = new ObjectOutputStream(fos);
 *
 *      oos.writeInt(12345);
 *      oos.writeObject("Today");
 *      oos.writeObject(new Date());
 *
 *      oos.close();
 * </pre>
 *
 * <p>需要在序列化和反序列化过程中进行特殊处理的类必须实现具有以下确切签名的特殊方法：
 * <br>
 * <pre>
 * private void readObject(java.io.ObjectInputStream stream)
 *     throws IOException, ClassNotFoundException;
 * private void writeObject(java.io.ObjectOutputStream stream)
 *     throws IOException
 * private void readObjectNoData()
 *     throws ObjectStreamException;
 * </pre>
 *
 * <p>writeObject 方法负责为其特定类写入对象的状态，以便对应的 readObject 方法可以恢复它。该方法
 * 不需要关心属于对象的超类或子类的状态。通过使用 writeObject 方法或 DataOutput 支持的基本数据类型
 * 方法将各个字段写入 ObjectOutputStream 来保存状态。
 *
 * <p>序列化不会写入不实现 java.io.Serializable 接口的任何对象的字段。非序列化类的子类可以是序列化
 * 的。在这种情况下，非序列化类必须有一个无参数构造函数以允许其字段被初始化。在这种情况下，子类负责
 * 保存和恢复非序列化类的状态。通常情况下，该类的字段是可访问的（公共、包级或受保护的）或有 get 和
 * set 方法可以用于恢复状态。
 *
 * <p>可以通过实现 writeObject 和 readObject 方法来防止对象的序列化，这些方法抛出 NotSerializableException。
 * 该异常将被 ObjectOutputStream 捕获并中止序列化过程。
 *
 * <p>实现 Externalizable 接口允许对象完全控制其序列化形式的内容和格式。Externalizable 接口的方法
 * writeExternal 和 readExternal 用于保存和恢复对象的状态。当由类实现时，它们可以使用 ObjectOutput
 * 和 ObjectInput 的所有方法来写入和读取自己的状态。对象负责处理任何发生的情况。
 *
 * <p>枚举常量的序列化方式与普通可序列化或可外部化对象不同。枚举常量的序列化形式仅由其名称组成；常量
 * 的字段值不会传输。为了序列化枚举常量，ObjectOutputStream 写入常量的 name 方法返回的字符串。像
 * 其他可序列化或可外部化对象一样，枚举常量可以作为后续出现在序列化流中的后向引用的目标。枚举常量的
 * 序列化过程不能自定义；枚举类型定义的任何类特定的 writeObject 和 writeReplace 方法在序列化过程中
 * 都会被忽略。同样，任何 serialPersistentFields 或 serialVersionUID 字段声明也会被忽略——所有
 * 枚举类型都有一个固定的 serialVersionUID 为 0L。
 *
 * <p>基本数据（不包括可序列化字段和可外部化数据）以块数据记录的形式写入 ObjectOutputStream。块数据
 * 记录由头和数据组成。块数据头由一个标记和跟随头的字节数组成。连续的基本数据写入会被合并到一个块数据
 * 记录中。用于块数据记录的阻塞因子为 1024 字节。每个块数据记录将被填充到 1024 字节，或者在块数据模式
 * 终止时写入。调用 ObjectOutputStream 的 writeObject、defaultWriteObject 和 writeFields 方法会
 * 初始终止任何现有的块数据记录。
 *
 * @author      Mike Warres
 * @author      Roger Riggs
 * @see java.io.DataOutput
 * @see java.io.ObjectInputStream
 * @see java.io.Serializable
 * @see java.io.Externalizable
 * @see <a href="../../../platform/serialization/spec/output.html">Object Serialization Specification, Section 2, Object Output Classes</a>
 * @since       JDK1.1
 */
public class ObjectOutputStream
    extends OutputStream implements ObjectOutput, ObjectStreamConstants
{

    private static class Caches {
        /** 子类安全审核结果缓存 */
        static final ConcurrentMap<WeakClassKey,Boolean> subclassAudits =
            new ConcurrentHashMap<>();

        /** 审核子类的 WeakReferences 队列 */
        static final ReferenceQueue<Class<?>> subclassAuditsQueue =
            new ReferenceQueue<>();
    }

    /** 用于处理块数据转换的过滤流 */
    private final BlockDataOutputStream bout;
    /** 对象到线程句柄映射 */
    private final HandleTable handles;
    /** 对象到替换对象映射 */
    private final ReplaceTable subs;
    /** 流协议版本 */
    private int protocol = PROTOCOL_VERSION_2;
    /** 递归深度 */
    private int depth;

    /** 用于写入基本字段值的缓冲区 */
    private byte[] primVals;

    /** 如果为 true，则调用 writeObjectOverride() 而不是 writeObject() */
    private final boolean enableOverride;
    /** 如果为 true，则调用 replaceObject() */
    private boolean enableReplace;

    // 仅在调用 writeObject()/writeExternal() 期间有效的值
    /**
     * 在调用类定义的 writeObject 方法期间的上下文；持有当前正在序列化的对象和当前类的描述符。
     * 在 writeObject 上调用期间为 null。
     */
    private SerialCallbackContext curContext;
    /** 当前的 PutField 对象 */
    private PutFieldImpl curPut;

    /** 自定义调试跟踪信息存储 */
    private final DebugTraceInfoStack debugInfoStack;

    /**
     * "sun.io.serialization.extendedDebugInfo" 属性的值，作为关于异常位置的扩展信息的 true 或 false
     */
    private static final boolean extendedDebugInfo =
        java.security.AccessController.doPrivileged(
            new sun.security.action.GetBooleanAction(
                "sun.io.serialization.extendedDebugInfo")).booleanValue();

    /**
     * 创建一个写入指定 OutputStream 的 ObjectOutputStream。此构造函数将序列化流头写入底层流；调用者
     * 可能希望立即刷新流，以确保接收 ObjectInputStream 的构造函数在读取头时不会阻塞。
     *
     * <p>如果安装了安全经理，此构造函数将在直接或间接由覆盖 ObjectOutputStream.putFields 或
     * ObjectOutputStream.writeUnshared 方法的子类构造函数调用时，检查 "enableSubclassImplementation"
     * SerializablePermission。
     *
     * @param   out 要写入的输出流
     * @throws  IOException 如果在写入流头时发生 I/O 错误
     * @throws  SecurityException 如果不受信任的子类非法覆盖安全敏感方法
     * @throws  NullPointerException 如果 <code>out</code> 为 <code>null</code>
     * @since   1.4
     * @see     ObjectOutputStream#ObjectOutputStream()
     * @see     ObjectOutputStream#putFields()
     * @see     ObjectInputStream#ObjectInputStream(InputStream)
     */
    public ObjectOutputStream(OutputStream out) throws IOException {
        verifySubclass();
        bout = new BlockDataOutputStream(out);
        handles = new HandleTable(10, (float) 3.00);
        subs = new ReplaceTable(10, (float) 3.00);
        enableOverride = false;
        writeStreamHeader();
        bout.setBlockDataMode(true);
        if (extendedDebugInfo) {
            debugInfoStack = new DebugTraceInfoStack();
        } else {
            debugInfoStack = null;
        }
    }

    /**
     * 为完全重新实现 ObjectOutputStream 的子类提供一种方法，使其不必为仅用于此实现的私有数据分配空间。
     *
     * <p>如果安装了安全经理，此方法首先调用安全经理的 <code>checkPermission</code> 方法，使用
     * <code>SerializablePermission("enableSubclassImplementation")</code> 权限，以确保启用子类化是安全的。
     *
     * @throws  SecurityException 如果存在安全经理且其 <code>checkPermission</code> 方法拒绝启用子类化
     * @throws  IOException 如果在创建此流时发生 I/O 错误
     * @see SecurityManager#checkPermission
     * @see java.io.SerializablePermission
     */
    protected ObjectOutputStream() throws IOException, SecurityException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(SUBCLASS_IMPLEMENTATION_PERMISSION);
        }
        bout = null;
        handles = null;
        subs = null;
        enableOverride = true;
        debugInfoStack = null;
    }

    /**
     * 指定写入流时使用的流协议版本。
     *
     * <p>此例程提供了一个钩子，使当前版本的序列化可以在格式与以前版本的流格式向后兼容的情况下写入。
     *
     * <p>将尽一切努力避免引入额外的向后不兼容性；然而，有时没有其他选择。
     *
     * @param   version 使用 java.io.ObjectStreamConstants 中的 ProtocolVersion。
     * @throws  IllegalStateException 如果在序列化任何对象后调用此方法。
     * @throws  IllegalArgumentException 如果传递了无效的版本。
     * @throws  IOException 如果发生 I/O 错误
     * @see java.io.ObjectStreamConstants#PROTOCOL_VERSION_1
     * @see java.io.ObjectStreamConstants#PROTOCOL_VERSION_2
     * @since   1.2
     */
    public void useProtocolVersion(int version) throws IOException {
        if (handles.size() != 0) {
            // REMIND: implement better check for pristine stream?
            throw new IllegalStateException("stream non-empty");
        }
        switch (version) {
            case PROTOCOL_VERSION_1:
            case PROTOCOL_VERSION_2:
                protocol = version;
                break;

            default:
                throw new IllegalArgumentException(
                    "unknown version: " + version);
        }
    }

    /**
     * 将指定的对象写入 ObjectOutputStream。对象的类、类签名以及类及其所有超类的非瞬态和非静态字段的值
     * 都会被写入。可以使用 writeObject 和 readObject 方法覆盖类的默认序列化。对象引用的对象会递归地
     * 写入，以便可以重构一个完整的等效对象图。
     *
     * <p>对于 OutputStream 的问题和不应序列化的类，会抛出异常。所有异常都是致命的，流将处于不确定状态，
     * 由调用者决定忽略或恢复流状态。
     *
     * @throws  InvalidClassException 序列化使用的类有问题。
     * @throws  NotSerializableException 要序列化的某些对象未实现 java.io.Serializable 接口。
     * @throws  IOException 由底层 OutputStream 抛出的任何异常。
     */
    public final void writeObject(Object obj) throws IOException {
        if (enableOverride) {
            writeObjectOverride(obj);
            return;
        }
        try {
            writeObject0(obj, false);
        } catch (IOException ex) {
            if (depth == 0) {
                writeFatalException(ex);
            }
            throw ex;
        }
    }


                /**
     * 子类用于覆盖默认的 writeObject 方法的方法。
     * 此方法由使用受保护的无参构造函数构造 ObjectInputStream 的受信任子类调用。
     * 期望子类提供一个具有 "final" 修饰符的重写方法。
     *
     * @param   obj 要写入底层流的对象
     * @throws  IOException 如果在写入底层流时发生 I/O 错误
     * @see #ObjectOutputStream()
     * @see #writeObject(Object)
     * @since 1.2
     */
    protected void writeObjectOverride(Object obj) throws IOException {
    }

    /**
     * 将一个“不共享”的对象写入 ObjectOutputStream。此方法与 writeObject 完全相同，只是它总是将给定的对象作为流中的新、唯一对象写入（而不是指向先前序列化的实例的后向引用）。具体来说：
     * <ul>
     *   <li>通过 writeUnshared 写入的对象总是以与新出现的对象（尚未写入流的对象）相同的方式序列化，无论该对象是否已写入。
     *
     *   <li>如果使用 writeObject 写入一个先前使用 writeUnshared 写入的对象，先前的 writeUnshared 操作将被视为写入了一个单独的对象。换句话说，ObjectOutputStream 永远不会生成指向由 writeUnshared 调用写入的对象数据的后向引用。
     * </ul>
     * 虽然通过 writeUnshared 写入对象本身不保证在反序列化时获得该对象的唯一引用，但它允许在流中多次定义单个对象，因此接收方多次调用 readUnshared 不会发生冲突。请注意，上述规则仅适用于使用 writeUnshared 写入的基本级对象，而不适用于要序列化的对象图中任何传递引用的子对象。
     *
     * <p>覆盖此方法的 ObjectOutputStream 子类只能在具有 "enableSubclassImplementation" SerializablePermission 的安全上下文中构造；在没有此权限的情况下尝试实例化此类子类将导致抛出 SecurityException。
     *
     * @param   obj 要写入流的对象
     * @throws  NotSerializableException 如果要序列化的对象图中的对象未实现 Serializable 接口
     * @throws  InvalidClassException 如果要序列化的对象的类存在问题
     * @throws  IOException 如果在序列化过程中发生 I/O 错误
     * @since 1.4
     */
    public void writeUnshared(Object obj) throws IOException {
        try {
            writeObject0(obj, true);
        } catch (IOException ex) {
            if (depth == 0) {
                writeFatalException(ex);
            }
            throw ex;
        }
    }

    /**
     * 将当前类的非静态和非瞬态字段写入此流。此方法只能从正在序列化的类的 writeObject 方法中调用。如果以其他方式调用，将抛出 NotActiveException。
     *
     * @throws  IOException 如果在写入底层 <code>OutputStream</code> 时发生 I/O 错误
     */
    public void defaultWriteObject() throws IOException {
        SerialCallbackContext ctx = curContext;
        if (ctx == null) {
            throw new NotActiveException("not in call to writeObject");
        }
        Object curObj = ctx.getObj();
        ObjectStreamClass curDesc = ctx.getDesc();
        bout.setBlockDataMode(false);
        defaultWriteFields(curObj, curDesc);
        bout.setBlockDataMode(true);
    }

    /**
     * 检索用于将持久字段缓冲到流中的对象。当调用 writeFields 方法时，这些字段将被写入流中。
     *
     * @return  一个持有可序列化字段的 Putfield 类的实例
     * @throws  IOException 如果发生 I/O 错误
     * @since 1.2
     */
    public ObjectOutputStream.PutField putFields() throws IOException {
        if (curPut == null) {
            SerialCallbackContext ctx = curContext;
            if (ctx == null) {
                throw new NotActiveException("not in call to writeObject");
            }
            Object curObj = ctx.getObj();
            ObjectStreamClass curDesc = ctx.getDesc();
            curPut = new PutFieldImpl(curDesc);
        }
        return curPut;
    }

    /**
     * 将缓冲的字段写入流中。
     *
     * @throws  IOException 如果在写入底层流时发生 I/O 错误
     * @throws  NotActiveException 当类的 writeObject 方法未被调用来写入对象状态时抛出
     * @since 1.2
     */
    public void writeFields() throws IOException {
        if (curPut == null) {
            throw new NotActiveException("no current PutField object");
        }
        bout.setBlockDataMode(false);
        curPut.writeFields();
        bout.setBlockDataMode(true);
    }

    /**
     * 重置将忽略已写入流中的任何对象的状态。状态将重置为与新的 ObjectOutputStream 相同。当前流中的点将被标记为重置，因此对应的 ObjectInputStream 也将在同一位置重置。先前写入流中的对象将不会被视为已经在流中。它们将再次被写入流中。
     *
     * @throws  IOException 如果在序列化对象时调用 reset()。
     */
    public void reset() throws IOException {
        if (depth != 0) {
            throw new IOException("stream active");
        }
        bout.setBlockDataMode(false);
        bout.writeByte(TC_RESET);
        clear();
        bout.setBlockDataMode(true);
    }

    /**
     * 子类可以实现此方法以允许在流中存储类数据。默认情况下，此方法不执行任何操作。ObjectInputStream 中的对应方法是 resolveClass。此方法在流中的每个唯一类上恰好被调用一次。类名和签名已经写入流中。此方法可以自由地使用 ObjectOutputStream 来保存任何认为合适的类表示（例如，类文件的字节）。ObjectInputStream 的对应子类中的 resolveClass 方法必须读取并使用 annotateClass 写入的任何数据或对象。
     *
     * @param   cl 要注释自定义数据的类
     * @throws  IOException 由底层 OutputStream 抛出的任何异常。
     */
    protected void annotateClass(Class<?> cl) throws IOException {
    }

    /**
     * 子类可以实现此方法以在流中存储动态代理类描述符的自定义数据。
     *
     * <p>此方法在流中的每个唯一代理类描述符上恰好被调用一次。ObjectOutputStream 中的默认实现不执行任何操作。
     *
     * <p>ObjectInputStream 中的对应方法是 resolveProxyClass。对于给定的 ObjectOutputStream 子类，ObjectInputStream 的对应子类中的 resolveProxyClass 方法必须读取并使用 annotateProxyClass 写入的任何数据或对象。
     *
     * @param   cl 要注释自定义数据的代理类
     * @throws  IOException 由底层 OutputStream 抛出的任何异常
     * @see ObjectInputStream#resolveProxyClass(String[])
     * @since   1.3
     */
    protected void annotateProxyClass(Class<?> cl) throws IOException {
    }

    /**
     * 此方法允许 ObjectOutputStream 的受信任子类在序列化过程中用一个对象替换另一个对象。替换对象的功能在调用 enableReplaceObject 之前是禁用的。enableReplaceObject 方法会检查请求替换的流是否可信。每个写入序列化流的对象的第一次出现都会传递给 replaceObject。对对象的后续引用将被替换为原始调用 replaceObject 返回的对象。为了确保对象的私有状态不会无意中暴露，只有受信任的流可以使用 replaceObject。
     *
     * <p>ObjectOutputStream.writeObject 方法接受类型为 Object 的参数（而不是 Serializable 类型），以允许在非序列化对象被序列化对象替换的情况下。
     *
     * <p>当子类在替换对象时，必须确保在反序列化期间进行相应的替换，或者确保替换的对象与要存储引用的每个字段兼容。如果对象的类型不是字段或数组元素类型的子类，则会引发异常，对象将不会被存储。
     *
     * <p>此方法仅在首次遇到每个对象时调用一次。对对象的所有后续引用将被重定向到新对象。此方法应返回要替换的对象或原始对象。
     *
     * <p>可以返回 null 作为要替换的对象，但可能会在包含对原始对象引用的类中导致 NullReferenceException，因为它们可能期望对象而不是 null。
     *
     * @param   obj 要替换的对象
     * @return  替换指定对象的替代对象
     * @throws  IOException 由底层 OutputStream 抛出的任何异常。
     */
    protected Object replaceObject(Object obj) throws IOException {
        return obj;
    }

    /**
     * 启用流替换流中的对象。启用后，replaceObject 方法将被调用以序列化每个对象。
     *
     * <p>如果 <code>enable</code> 为 true，并且安装了安全经理，此方法首先调用安全经理的 <code>checkPermission</code> 方法，使用 <code>SerializablePermission("enableSubstitution")</code> 权限，以确保启用流替换对象是安全的。
     *
     * @param   enable 布尔参数，用于启用对象替换
     * @return  调用此方法之前的方法设置
     * @throws  SecurityException 如果存在安全经理，并且其 <code>checkPermission</code> 方法拒绝启用流替换对象。
     * @see SecurityManager#checkPermission
     * @see java.io.SerializablePermission
     */
    protected boolean enableReplaceObject(boolean enable)
        throws SecurityException
    {
        if (enable == enableReplace) {
            return enable;
        }
        if (enable) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                sm.checkPermission(SUBSTITUTION_PERMISSION);
            }
        }
        enableReplace = enable;
        return !enableReplace;
    }

    /**
     * writeStreamHeader 方法提供给子类，以便在流中附加或前置它们自己的头。它将魔数和版本写入流中。
     *
     * @throws  IOException 如果在写入底层流时发生 I/O 错误
     */
    protected void writeStreamHeader() throws IOException {
        bout.writeShort(STREAM_MAGIC);
        bout.writeShort(STREAM_VERSION);
    }

    /**
     * 将指定的类描述符写入 ObjectOutputStream。类描述符用于标识写入流中的对象的类。ObjectOutputStream 的子类可以覆盖此方法，以自定义类描述符写入序列化流的方式。ObjectInputStream 中的对应方法 <code>readClassDescriptor</code> 应该被覆盖，以从其自定义流表示中重新构建类描述符。默认情况下，此方法根据 Object Serialization 规范中定义的格式写入类描述符。
     *
     * <p>请注意，如果 ObjectOutputStream 不使用旧的序列化流格式（通过调用 ObjectOutputStream 的 <code>useProtocolVersion</code> 方法设置），则只会调用此方法。如果此序列化流使用旧格式 (<code>PROTOCOL_VERSION_1</code>)，类描述符将内部以无法被覆盖或自定义的方式写入。
     *
     * @param   desc 要写入流的类描述符
     * @throws  IOException 如果发生 I/O 错误。
     * @see java.io.ObjectInputStream#readClassDescriptor()
     * @see #useProtocolVersion(int)
     * @see java.io.ObjectStreamConstants#PROTOCOL_VERSION_1
     * @since 1.3
     */
    protected void writeClassDescriptor(ObjectStreamClass desc)
        throws IOException
    {
        desc.writeNonProxy(this);
    }

    /**
     * 写入一个字节。此方法将阻塞，直到字节实际写入。
     *
     * @param   val 要写入流的字节
     * @throws  IOException 如果发生 I/O 错误。
     */
    public void write(int val) throws IOException {
        bout.write(val);
    }

    /**
     * 写入一个字节数组。此方法将阻塞，直到字节实际写入。
     *
     * @param   buf 要写入的数据
     * @throws  IOException 如果发生 I/O 错误。
     */
    public void write(byte[] buf) throws IOException {
        bout.write(buf, 0, buf.length, false);
    }

    /**
     * 写入字节数组的子数组。
     *
     * @param   buf 要写入的数据
     * @param   off 数据中的起始偏移量
     * @param   len 要写入的字节数
     * @throws  IOException 如果发生 I/O 错误。
     */
    public void write(byte[] buf, int off, int len) throws IOException {
        if (buf == null) {
            throw new NullPointerException();
        }
        int endoff = off + len;
        if (off < 0 || len < 0 || endoff > buf.length || endoff < 0) {
            throw new IndexOutOfBoundsException();
        }
        bout.write(buf, off, len, false);
    }

    /**
     * 刷新流。这将写入任何缓冲的输出字节并刷新到底层流。
     *
     * @throws  IOException 如果发生 I/O 错误。
     */
    public void flush() throws IOException {
        bout.flush();
    }


                /**
     * 清空内部数据结构。
     */
    private void clear() {
        subs.clear();
        handles.clear();
    }

    /**
     * 写入对象或非共享对象的基本实现。
     */
    private void writeObject0(Object obj, boolean unshared)
        throws IOException
    {
        boolean oldMode = bout.setBlockDataMode(false);
        depth++;
        try {
            // 处理已写入和不可替换的对象
            int h;
            if ((obj = subs.lookup(obj)) == null) {
                writeNull();
                return;
            } else if (!unshared && (h = handles.lookup(obj)) != -1) {
                writeHandle(h);
                return;
            } else if (obj instanceof Class) {
                writeClass((Class) obj, unshared);
                return;
            } else if (obj instanceof ObjectStreamClass) {
                writeClassDesc((ObjectStreamClass) obj, unshared);
                return;
            }

            // 检查替换对象
            Object orig = obj;
            Class<?> cl = obj.getClass();
            ObjectStreamClass desc;
            for (;;) {
                // 提醒：是否跳过字符串/数组的检查？
                Class<?> repCl;
                desc = ObjectStreamClass.lookup(cl, true);
                if (!desc.hasWriteReplaceMethod() ||
                    (obj = desc.invokeWriteReplace(obj)) == null ||
                    (repCl = obj.getClass()) == cl)
                {
                    break;
                }
                cl = repCl;
            }
            if (enableReplace) {
                Object rep = replaceObject(obj);
                if (rep != obj && rep != null) {
                    cl = rep.getClass();
                    desc = ObjectStreamClass.lookup(cl, true);
                }
                obj = rep;
            }


                        // 如果对象被替换，再次运行原始检查
            if (obj != orig) {
                subs.assign(orig, obj);
                if (obj == null) {
                    writeNull();
                    return;
                } else if (!unshared && (h = handles.lookup(obj)) != -1) {
                    writeHandle(h);
                    return;
                } else if (obj instanceof Class) {
                    writeClass((Class) obj, unshared);
                    return;
                } else if (obj instanceof ObjectStreamClass) {
                    writeClassDesc((ObjectStreamClass) obj, unshared);
                    return;
                }
            }

            // 剩余情况
            if (obj instanceof String) {
                writeString((String) obj, unshared);
            } else if (cl.isArray()) {
                writeArray(obj, desc, unshared);
            } else if (obj instanceof Enum) {
                writeEnum((Enum<?>) obj, desc, unshared);
            } else if (obj instanceof Serializable) {
                writeOrdinaryObject(obj, desc, unshared);
            } else {
                if (extendedDebugInfo) {
                    throw new NotSerializableException(
                        cl.getName() + "\n" + debugInfoStack.toString());
                } else {
                    throw new NotSerializableException(cl.getName());
                }
            }
        } finally {
            depth--;
            bout.setBlockDataMode(oldMode);
        }
    }

    /**
     * 向流中写入 null 代码。
     */
    private void writeNull() throws IOException {
        bout.writeByte(TC_NULL);
    }

    /**
     * 向流中写入给定的对象句柄。
     */
    private void writeHandle(int handle) throws IOException {
        bout.writeByte(TC_REFERENCE);
        bout.writeInt(baseWireHandle + handle);
    }

    /**
     * 向流中写入给定类的表示。
     */
    private void writeClass(Class<?> cl, boolean unshared) throws IOException {
        bout.writeByte(TC_CLASS);
        writeClassDesc(ObjectStreamClass.lookup(cl, true), false);
        handles.assign(unshared ? null : cl);
    }

    /**
     * 向流中写入给定类描述符的表示。
     */
    private void writeClassDesc(ObjectStreamClass desc, boolean unshared)
        throws IOException
    {
        int handle;
        if (desc == null) {
            writeNull();
        } else if (!unshared && (handle = handles.lookup(desc)) != -1) {
            writeHandle(handle);
        } else if (desc.isProxy()) {
            writeProxyDesc(desc, unshared);
        } else {
            writeNonProxyDesc(desc, unshared);
        }
    }

    private boolean isCustomSubclass() {
        // 如果这个类是 ObjectOutputStream 的自定义子类，则返回 true
        return getClass().getClassLoader()
                   != ObjectOutputStream.class.getClassLoader();
    }

    /**
     * 向流中写入表示动态代理类的类描述符。
     */
    private void writeProxyDesc(ObjectStreamClass desc, boolean unshared)
        throws IOException
    {
        bout.writeByte(TC_PROXYCLASSDESC);
        handles.assign(unshared ? null : desc);

        Class<?> cl = desc.forClass();
        Class<?>[] ifaces = cl.getInterfaces();
        bout.writeInt(ifaces.length);
        for (int i = 0; i < ifaces.length; i++) {
            bout.writeUTF(ifaces[i].getName());
        }

        bout.setBlockDataMode(true);
        if (cl != null && isCustomSubclass()) {
            ReflectUtil.checkPackageAccess(cl);
        }
        annotateProxyClass(cl);
        bout.setBlockDataMode(false);
        bout.writeByte(TC_ENDBLOCKDATA);

        writeClassDesc(desc.getSuperDesc(), false);
    }

    /**
     * 向流中写入表示标准类（即，不是动态代理类）的类描述符。
     */
    private void writeNonProxyDesc(ObjectStreamClass desc, boolean unshared)
        throws IOException
    {
        bout.writeByte(TC_CLASSDESC);
        handles.assign(unshared ? null : desc);

        if (protocol == PROTOCOL_VERSION_1) {
            // 使用旧协议时不调用类描述符写入钩子
            desc.writeNonProxy(this);
        } else {
            writeClassDescriptor(desc);
        }

        Class<?> cl = desc.forClass();
        bout.setBlockDataMode(true);
        if (cl != null && isCustomSubclass()) {
            ReflectUtil.checkPackageAccess(cl);
        }
        annotateClass(cl);
        bout.setBlockDataMode(false);
        bout.writeByte(TC_ENDBLOCKDATA);

        writeClassDesc(desc.getSuperDesc(), false);
    }

    /**
     * 根据字符串长度使用标准或长 UTF 格式向流中写入给定字符串。
     */
    private void writeString(String str, boolean unshared) throws IOException {
        handles.assign(unshared ? null : str);
        long utflen = bout.getUTFLength(str);
        if (utflen <= 0xFFFF) {
            bout.writeByte(TC_STRING);
            bout.writeUTF(str, utflen);
        } else {
            bout.writeByte(TC_LONGSTRING);
            bout.writeLongUTF(str, utflen);
        }
    }

    /**
     * 向流中写入给定数组对象。
     */
    private void writeArray(Object array,
                            ObjectStreamClass desc,
                            boolean unshared)
        throws IOException
    {
        bout.writeByte(TC_ARRAY);
        writeClassDesc(desc, false);
        handles.assign(unshared ? null : array);

        Class<?> ccl = desc.forClass().getComponentType();
        if (ccl.isPrimitive()) {
            if (ccl == Integer.TYPE) {
                int[] ia = (int[]) array;
                bout.writeInt(ia.length);
                bout.writeInts(ia, 0, ia.length);
            } else if (ccl == Byte.TYPE) {
                byte[] ba = (byte[]) array;
                bout.writeInt(ba.length);
                bout.write(ba, 0, ba.length, true);
            } else if (ccl == Long.TYPE) {
                long[] ja = (long[]) array;
                bout.writeInt(ja.length);
                bout.writeLongs(ja, 0, ja.length);
            } else if (ccl == Float.TYPE) {
                float[] fa = (float[]) array;
                bout.writeInt(fa.length);
                bout.writeFloats(fa, 0, fa.length);
            } else if (ccl == Double.TYPE) {
                double[] da = (double[]) array;
                bout.writeInt(da.length);
                bout.writeDoubles(da, 0, da.length);
            } else if (ccl == Short.TYPE) {
                short[] sa = (short[]) array;
                bout.writeInt(sa.length);
                bout.writeShorts(sa, 0, sa.length);
            } else if (ccl == Character.TYPE) {
                char[] ca = (char[]) array;
                bout.writeInt(ca.length);
                bout.writeChars(ca, 0, ca.length);
            } else if (ccl == Boolean.TYPE) {
                boolean[] za = (boolean[]) array;
                bout.writeInt(za.length);
                bout.writeBooleans(za, 0, za.length);
            } else {
                throw new InternalError();
            }
        } else {
            Object[] objs = (Object[]) array;
            int len = objs.length;
            bout.writeInt(len);
            if (extendedDebugInfo) {
                debugInfoStack.push(
                    "array (class \"" + array.getClass().getName() +
                    "\", size: " + len  + ")");
            }
            try {
                for (int i = 0; i < len; i++) {
                    if (extendedDebugInfo) {
                        debugInfoStack.push(
                            "element of array (index: " + i + ")");
                    }
                    try {
                        writeObject0(objs[i], false);
                    } finally {
                        if (extendedDebugInfo) {
                            debugInfoStack.pop();
                        }
                    }
                }
            } finally {
                if (extendedDebugInfo) {
                    debugInfoStack.pop();
                }
            }
        }
    }

    /**
     * 向流中写入给定的枚举常量。
     */
    private void writeEnum(Enum<?> en,
                           ObjectStreamClass desc,
                           boolean unshared)
        throws IOException
    {
        bout.writeByte(TC_ENUM);
        ObjectStreamClass sdesc = desc.getSuperDesc();
        writeClassDesc((sdesc.forClass() == Enum.class) ? desc : sdesc, false);
        handles.assign(unshared ? null : en);
        writeString(en.name(), false);
    }

    /**
     * 向流中写入“普通”（即，不是字符串、类、ObjectStreamClass、数组或枚举常量）可序列化对象的表示。
     */
    private void writeOrdinaryObject(Object obj,
                                     ObjectStreamClass desc,
                                     boolean unshared)
        throws IOException
    {
        if (extendedDebugInfo) {
            debugInfoStack.push(
                (depth == 1 ? "root " : "") + "object (class \"" +
                obj.getClass().getName() + "\", " + obj.toString() + ")");
        }
        try {
            desc.checkSerialize();

            bout.writeByte(TC_OBJECT);
            writeClassDesc(desc, false);
            handles.assign(unshared ? null : obj);
            if (desc.isExternalizable() && !desc.isProxy()) {
                writeExternalData((Externalizable) obj);
            } else {
                writeSerialData(obj, desc);
            }
        } finally {
            if (extendedDebugInfo) {
                debugInfoStack.pop();
            }
        }
    }

    /**
     * 通过调用给定对象的 writeExternal() 方法写入其外部化数据。
     */
    private void writeExternalData(Externalizable obj) throws IOException {
        PutFieldImpl oldPut = curPut;
        curPut = null;

        if (extendedDebugInfo) {
            debugInfoStack.push("writeExternal data");
        }
        SerialCallbackContext oldContext = curContext;
        try {
            curContext = null;
            if (protocol == PROTOCOL_VERSION_1) {
                obj.writeExternal(this);
            } else {
                bout.setBlockDataMode(true);
                obj.writeExternal(this);
                bout.setBlockDataMode(false);
                bout.writeByte(TC_ENDBLOCKDATA);
            }
        } finally {
            curContext = oldContext;
            if (extendedDebugInfo) {
                debugInfoStack.pop();
            }
        }

        curPut = oldPut;
    }

    /**
     * 从超类到子类，为给定对象的每个可序列化类写入实例数据。
     */
    private void writeSerialData(Object obj, ObjectStreamClass desc)
        throws IOException
    {
        ObjectStreamClass.ClassDataSlot[] slots = desc.getClassDataLayout();
        for (int i = 0; i < slots.length; i++) {
            ObjectStreamClass slotDesc = slots[i].desc;
            if (slotDesc.hasWriteObjectMethod()) {
                PutFieldImpl oldPut = curPut;
                curPut = null;
                SerialCallbackContext oldContext = curContext;

                if (extendedDebugInfo) {
                    debugInfoStack.push(
                        "custom writeObject data (class \"" +
                        slotDesc.getName() + "\")");
                }
                try {
                    curContext = new SerialCallbackContext(obj, slotDesc);
                    bout.setBlockDataMode(true);
                    slotDesc.invokeWriteObject(obj, this);
                    bout.setBlockDataMode(false);
                    bout.writeByte(TC_ENDBLOCKDATA);
                } finally {
                    curContext.setUsed();
                    curContext = oldContext;
                    if (extendedDebugInfo) {
                        debugInfoStack.pop();
                    }
                }

                curPut = oldPut;
            } else {
                defaultWriteFields(obj, slotDesc);
            }
        }
    }

    /**
     * 获取并写入给定对象的可序列化字段的值到流中。给定的类描述符指定了要写入的字段值及其写入顺序。
     */
    private void defaultWriteFields(Object obj, ObjectStreamClass desc)
        throws IOException
    {
        Class<?> cl = desc.forClass();
        if (cl != null && obj != null && !cl.isInstance(obj)) {
            throw new ClassCastException();
        }

        desc.checkDefaultSerialize();

        int primDataSize = desc.getPrimDataSize();
        if (primVals == null || primVals.length < primDataSize) {
            primVals = new byte[primDataSize];
        }
        desc.getPrimFieldValues(obj, primVals);
        bout.write(primVals, 0, primDataSize, false);

        ObjectStreamField[] fields = desc.getFields(false);
        Object[] objVals = new Object[desc.getNumObjFields()];
        int numPrimFields = fields.length - objVals.length;
        desc.getObjFieldValues(obj, objVals);
        for (int i = 0; i < objVals.length; i++) {
            if (extendedDebugInfo) {
                debugInfoStack.push(
                    "field (class \"" + desc.getName() + "\", name: \"" +
                    fields[numPrimFields + i].getName() + "\", type: \"" +
                    fields[numPrimFields + i].getType() + "\")");
            }
            try {
                writeObject0(objVals[i],
                             fields[numPrimFields + i].isUnshared());
            } finally {
                if (extendedDebugInfo) {
                    debugInfoStack.pop();
                }
            }
        }
    }

    /**
     * 尝试写入导致序列化中止的致命 IOException 到流中。
     */
    private void writeFatalException(IOException ex) throws IOException {
        /*
         * 注意：序列化规范指出，如果在尝试将原始致命异常序列化到流中时发生第二个 IOException，
         * 则应抛出 StreamCorruptedException（第 2.1 节）。然而，由于序列化实现中的一个错误，
         * StreamCorruptedException 很少（如果有的话）被实际抛出——而是抛出了底层流的“根”异常。
         * 为了保持一致性，这里遵循了这种历史行为。
         */
        clear();
        boolean oldMode = bout.setBlockDataMode(false);
        try {
            bout.writeByte(TC_EXCEPTION);
            writeObject0(ex, false);
            clear();
        } finally {
            bout.setBlockDataMode(oldMode);
        }
    }

    /**
     * 将指定范围的浮点值转换为字节值。
     */
    // 提醒：一旦 HotSpot 内联 Float.floatToIntBits，就移除此方法
    private static native void floatsToBytes(float[] src, int srcpos,
                                             byte[] dst, int dstpos,
                                             int nfloats);


                /**
     * 将指定范围的 double 值转换为 byte 值。
     */
    // REMIND: remove once hotspot inlines Double.doubleToLongBits
    private static native void doublesToBytes(double[] src, int srcpos,
                                              byte[] dst, int dstpos,
                                              int ndoubles);

    /**
     * 默认的 PutField 实现。
     */
    private class PutFieldImpl extends PutField {

        /** 描述可序列化字段的类描述符 */
        private final ObjectStreamClass desc;
        /** 原始字段值 */
        private final byte[] primVals;
        /** 对象字段值 */
        private final Object[] objVals;

        /**
         * 为给定类描述符中定义的字段创建 PutFieldImpl 对象。
         */
        PutFieldImpl(ObjectStreamClass desc) {
            this.desc = desc;
            primVals = new byte[desc.getPrimDataSize()];
            objVals = new Object[desc.getNumObjFields()];
        }

        public void put(String name, boolean val) {
            Bits.putBoolean(primVals, getFieldOffset(name, Boolean.TYPE), val);
        }

        public void put(String name, byte val) {
            primVals[getFieldOffset(name, Byte.TYPE)] = val;
        }

        public void put(String name, char val) {
            Bits.putChar(primVals, getFieldOffset(name, Character.TYPE), val);
        }

        public void put(String name, short val) {
            Bits.putShort(primVals, getFieldOffset(name, Short.TYPE), val);
        }

        public void put(String name, int val) {
            Bits.putInt(primVals, getFieldOffset(name, Integer.TYPE), val);
        }

        public void put(String name, float val) {
            Bits.putFloat(primVals, getFieldOffset(name, Float.TYPE), val);
        }

        public void put(String name, long val) {
            Bits.putLong(primVals, getFieldOffset(name, Long.TYPE), val);
        }

        public void put(String name, double val) {
            Bits.putDouble(primVals, getFieldOffset(name, Double.TYPE), val);
        }

        public void put(String name, Object val) {
            objVals[getFieldOffset(name, Object.class)] = val;
        }

        // 在 ObjectOutputStream.PutField 中已弃用
        public void write(ObjectOutput out) throws IOException {
            /*
             * 应用程序*不应*使用此方法写入 PutField
             * 数据，因为如果 PutField
             * 对象写入任何原始数据（由于未正确设置/取消设置块数据模式，如在 OOS.writeFields() 中所做的那样），这将导致流损坏。这个错误的实现仅为了行为兼容性而保留，以支持使用 OOS.PutField.write() 仅写入非原始数据的应用程序。
             *
             * 未在此处实现未共享对象的序列化，因为对于向后兼容性来说不是必需的；此外，给定的 ObjectOutput 实例可能不支持未共享语义。使用 PutField API 写入未共享对象的应用程序必须使用 OOS.writeFields()。
             */
            if (ObjectOutputStream.this != out) {
                throw new IllegalArgumentException("错误的流");
            }
            out.write(primVals, 0, primVals.length);

            ObjectStreamField[] fields = desc.getFields(false);
            int numPrimFields = fields.length - objVals.length;
            // REMIND: warn if numPrimFields > 0?
            for (int i = 0; i < objVals.length; i++) {
                if (fields[numPrimFields + i].isUnshared()) {
                    throw new IOException("无法写入未共享对象");
                }
                out.writeObject(objVals[i]);
            }
        }

        /**
         * 将缓冲的原始数据和对象字段写入流。
         */
        void writeFields() throws IOException {
            bout.write(primVals, 0, primVals.length, false);

            ObjectStreamField[] fields = desc.getFields(false);
            int numPrimFields = fields.length - objVals.length;
            for (int i = 0; i < objVals.length; i++) {
                if (extendedDebugInfo) {
                    debugInfoStack.push(
                        "field (class \"" + desc.getName() + "\", name: \"" +
                        fields[numPrimFields + i].getName() + "\", type: \"" +
                        fields[numPrimFields + i].getType() + "\")");
                }
                try {
                    writeObject0(objVals[i],
                                 fields[numPrimFields + i].isUnshared());
                } finally {
                    if (extendedDebugInfo) {
                        debugInfoStack.pop();
                    }
                }
            }
        }

        /**
         * 返回具有给定名称和类型的字段的偏移量。指定的类型为 null 匹配所有类型，Object.class 匹配所有非原始类型，任何其他非 null 类型仅匹配可分配类型。如果未找到匹配的字段，则抛出 IllegalArgumentException。
         */
        private int getFieldOffset(String name, Class<?> type) {
            ObjectStreamField field = desc.getField(name, type);
            if (field == null) {
                throw new IllegalArgumentException("没有这样的字段 " + name +
                                                   " 与类型 " + type);
            }
            return field.getOffset();
        }
    }

    /**
     * 带有两种模式的缓冲输出流：默认模式下，以与 DataOutputStream 相同的格式输出数据；在“块数据”模式下，数据被块数据标记包围（有关详细信息，请参阅对象序列化规范）。
     */
    private static class BlockDataOutputStream
        extends OutputStream implements DataOutput
    {
        /** 最大数据块长度 */
        private static final int MAX_BLOCK_SIZE = 1024;
        /** 最大数据块头长度 */
        private static final int MAX_HEADER_SIZE = 5;
        /** （可调）字符缓冲区长度（用于写入字符串） */
        private static final int CHAR_BUF_SIZE = 256;

        /** 用于写入一般/块数据的缓冲区 */
        private final byte[] buf = new byte[MAX_BLOCK_SIZE];
        /** 用于写入块数据头的缓冲区 */
        private final byte[] hbuf = new byte[MAX_HEADER_SIZE];
        /** 用于快速字符串写入的字符缓冲区 */
        private final char[] cbuf = new char[CHAR_BUF_SIZE];

        /** 块数据模式 */
        private boolean blkmode = false;
        /** 当前 buf 中的偏移量 */
        private int pos = 0;

        /** 底层输出流 */
        private final OutputStream out;
        /** 循环流（用于跨越数据块的数据写入） */
        private final DataOutputStream dout;

        /**
         * 在给定的底层流上创建新的 BlockDataOutputStream。默认情况下，块数据模式关闭。
         */
        BlockDataOutputStream(OutputStream out) {
            this.out = out;
            dout = new DataOutputStream(this);
        }

        /**
         * 将块数据模式设置为给定模式（true == 开启，false == 关闭）并返回前一个模式值。如果新模式与旧模式相同，则不采取任何操作。如果新模式与旧模式不同，则在切换到新模式之前刷新任何缓冲的数据。
         */
        boolean setBlockDataMode(boolean mode) throws IOException {
            if (blkmode == mode) {
                return blkmode;
            }
            drain();
            blkmode = mode;
            return !blkmode;
        }

        /**
         * 如果流当前处于块数据模式，则返回 true，否则返回 false。
         */
        boolean getBlockDataMode() {
            return blkmode;
        }

        /* ----------------- 通用输出流方法 ----------------- */
        /*
         * 以下方法等同于 OutputStream 中的对应方法，不同之处在于在块数据模式下，它们将写入的数据划分为数据块。
         */

        public void write(int b) throws IOException {
            if (pos >= MAX_BLOCK_SIZE) {
                drain();
            }
            buf[pos++] = (byte) b;
        }

        public void write(byte[] b) throws IOException {
            write(b, 0, b.length, false);
        }

        public void write(byte[] b, int off, int len) throws IOException {
            write(b, off, len, false);
        }

        public void flush() throws IOException {
            drain();
            out.flush();
        }

        public void close() throws IOException {
            flush();
            out.close();
        }

        /**
         * 从给定数组中写入指定范围的字节值。如果 copy 为 true，则在写入底层流之前将值复制到中间缓冲区（以避免暴露对原始字节数组的引用）。
         */
        void write(byte[] b, int off, int len, boolean copy)
            throws IOException
        {
            if (!(copy || blkmode)) {           // 直接写入
                drain();
                out.write(b, off, len);
                return;
            }

            while (len > 0) {
                if (pos >= MAX_BLOCK_SIZE) {
                    drain();
                }
                if (len >= MAX_BLOCK_SIZE && !copy && pos == 0) {
                    // 避免不必要的复制
                    writeBlockHeader(MAX_BLOCK_SIZE);
                    out.write(b, off, MAX_BLOCK_SIZE);
                    off += MAX_BLOCK_SIZE;
                    len -= MAX_BLOCK_SIZE;
                } else {
                    int wlen = Math.min(len, MAX_BLOCK_SIZE - pos);
                    System.arraycopy(b, off, buf, pos, wlen);
                    pos += wlen;
                    off += wlen;
                    len -= wlen;
                }
            }
        }

        /**
         * 将此流中的所有缓冲数据写入底层流，但不刷新底层流。
         */
        void drain() throws IOException {
            if (pos == 0) {
                return;
            }
            if (blkmode) {
                writeBlockHeader(pos);
            }
            out.write(buf, 0, pos);
            pos = 0;
        }

        /**
         * 写入块数据头。长度小于 256 字节的数据块以 2 字节头为前缀；所有其他块以 5 字节头开始。
         */
        private void writeBlockHeader(int len) throws IOException {
            if (len <= 0xFF) {
                hbuf[0] = TC_BLOCKDATA;
                hbuf[1] = (byte) len;
                out.write(hbuf, 0, 2);
            } else {
                hbuf[0] = TC_BLOCKDATALONG;
                Bits.putInt(hbuf, 1, len);
                out.write(hbuf, 0, 5);
            }
        }


        /* ----------------- 原始数据输出方法 ----------------- */
        /*
         * 以下方法等同于 DataOutputStream 中的对应方法，不同之处在于在块数据模式下，它们将写入的数据划分为数据块。
         */

        public void writeBoolean(boolean v) throws IOException {
            if (pos >= MAX_BLOCK_SIZE) {
                drain();
            }
            Bits.putBoolean(buf, pos++, v);
        }

        public void writeByte(int v) throws IOException {
            if (pos >= MAX_BLOCK_SIZE) {
                drain();
            }
            buf[pos++] = (byte) v;
        }

        public void writeChar(int v) throws IOException {
            if (pos + 2 <= MAX_BLOCK_SIZE) {
                Bits.putChar(buf, pos, (char) v);
                pos += 2;
            } else {
                dout.writeChar(v);
            }
        }

        public void writeShort(int v) throws IOException {
            if (pos + 2 <= MAX_BLOCK_SIZE) {
                Bits.putShort(buf, pos, (short) v);
                pos += 2;
            } else {
                dout.writeShort(v);
            }
        }

        public void writeInt(int v) throws IOException {
            if (pos + 4 <= MAX_BLOCK_SIZE) {
                Bits.putInt(buf, pos, v);
                pos += 4;
            } else {
                dout.writeInt(v);
            }
        }

        public void writeFloat(float v) throws IOException {
            if (pos + 4 <= MAX_BLOCK_SIZE) {
                Bits.putFloat(buf, pos, v);
                pos += 4;
            } else {
                dout.writeFloat(v);
            }
        }

        public void writeLong(long v) throws IOException {
            if (pos + 8 <= MAX_BLOCK_SIZE) {
                Bits.putLong(buf, pos, v);
                pos += 8;
            } else {
                dout.writeLong(v);
            }
        }

        public void writeDouble(double v) throws IOException {
            if (pos + 8 <= MAX_BLOCK_SIZE) {
                Bits.putDouble(buf, pos, v);
                pos += 8;
            } else {
                dout.writeDouble(v);
            }
        }

        public void writeBytes(String s) throws IOException {
            int endoff = s.length();
            int cpos = 0;
            int csize = 0;
            for (int off = 0; off < endoff; ) {
                if (cpos >= csize) {
                    cpos = 0;
                    csize = Math.min(endoff - off, CHAR_BUF_SIZE);
                    s.getChars(off, off + csize, cbuf, 0);
                }
                if (pos >= MAX_BLOCK_SIZE) {
                    drain();
                }
                int n = Math.min(csize - cpos, MAX_BLOCK_SIZE - pos);
                int stop = pos + n;
                while (pos < stop) {
                    buf[pos++] = (byte) cbuf[cpos++];
                }
                off += n;
            }
        }

        public void writeChars(String s) throws IOException {
            int endoff = s.length();
            for (int off = 0; off < endoff; ) {
                int csize = Math.min(endoff - off, CHAR_BUF_SIZE);
                s.getChars(off, off + csize, cbuf, 0);
                writeChars(cbuf, 0, csize);
                off += csize;
            }
        }

        public void writeUTF(String s) throws IOException {
            writeUTF(s, getUTFLength(s));
        }


        /* -------------- 原始数据数组输出方法 -------------- */
        /*
         * 以下方法写入原始数据值的范围。虽然等同于反复调用相应的原始写入方法，但这些方法针对更高效地写入原始数据值组进行了优化。
         */

        void writeBooleans(boolean[] v, int off, int len) throws IOException {
            int endoff = off + len;
            while (off < endoff) {
                if (pos >= MAX_BLOCK_SIZE) {
                    drain();
                }
                int stop = Math.min(endoff, off + (MAX_BLOCK_SIZE - pos));
                while (off < stop) {
                    Bits.putBoolean(buf, pos++, v[off++]);
                }
            }
        }


        void writeChars(char[] v, int off, int len) throws IOException {
            int limit = MAX_BLOCK_SIZE - 2;
            int endoff = off + len;
            while (off < endoff) {
                if (pos <= limit) {
                    int avail = (MAX_BLOCK_SIZE - pos) >> 1;
                    int stop = Math.min(endoff, off + avail);
                    while (off < stop) {
                        Bits.putChar(buf, pos, v[off++]);
                        pos += 2;
                    }
                } else {
                    dout.writeChar(v[off++]);
                }
            }
        }

        void writeShorts(short[] v, int off, int len) throws IOException {
            int limit = MAX_BLOCK_SIZE - 2;
            int endoff = off + len;
            while (off < endoff) {
                if (pos <= limit) {
                    int avail = (MAX_BLOCK_SIZE - pos) >> 1;
                    int stop = Math.min(endoff, off + avail);
                    while (off < stop) {
                        Bits.putShort(buf, pos, v[off++]);
                        pos += 2;
                    }
                } else {
                    dout.writeShort(v[off++]);
                }
            }
        }

        void writeInts(int[] v, int off, int len) throws IOException {
            int limit = MAX_BLOCK_SIZE - 4;
            int endoff = off + len;
            while (off < endoff) {
                if (pos <= limit) {
                    int avail = (MAX_BLOCK_SIZE - pos) >> 2;
                    int stop = Math.min(endoff, off + avail);
                    while (off < stop) {
                        Bits.putInt(buf, pos, v[off++]);
                        pos += 4;
                    }
                } else {
                    dout.writeInt(v[off++]);
                }
            }
        }

        void writeFloats(float[] v, int off, int len) throws IOException {
            int limit = MAX_BLOCK_SIZE - 4;
            int endoff = off + len;
            while (off < endoff) {
                if (pos <= limit) {
                    int avail = (MAX_BLOCK_SIZE - pos) >> 2;
                    int chunklen = Math.min(endoff - off, avail);
                    floatsToBytes(v, off, buf, pos, chunklen);
                    off += chunklen;
                    pos += chunklen << 2;
                } else {
                    dout.writeFloat(v[off++]);
                }
            }
        }

        void writeLongs(long[] v, int off, int len) throws IOException {
            int limit = MAX_BLOCK_SIZE - 8;
            int endoff = off + len;
            while (off < endoff) {
                if (pos <= limit) {
                    int avail = (MAX_BLOCK_SIZE - pos) >> 3;
                    int stop = Math.min(endoff, off + avail);
                    while (off < stop) {
                        Bits.putLong(buf, pos, v[off++]);
                        pos += 8;
                    }
                } else {
                    dout.writeLong(v[off++]);
                }
            }
        }

        void writeDoubles(double[] v, int off, int len) throws IOException {
            int limit = MAX_BLOCK_SIZE - 8;
            int endoff = off + len;
            while (off < endoff) {
                if (pos <= limit) {
                    int avail = (MAX_BLOCK_SIZE - pos) >> 3;
                    int chunklen = Math.min(endoff - off, avail);
                    doublesToBytes(v, off, buf, pos, chunklen);
                    off += chunklen;
                    pos += chunklen << 3;
                } else {
                    dout.writeDouble(v[off++]);
                }
            }
        }

        /**
         * 返回给定字符串的 UTF 编码长度（以字节为单位）。
         */
        long getUTFLength(String s) {
            int len = s.length();
            long utflen = 0;
            for (int off = 0; off < len; ) {
                int csize = Math.min(len - off, CHAR_BUF_SIZE);
                s.getChars(off, off + csize, cbuf, 0);
                for (int cpos = 0; cpos < csize; cpos++) {
                    char c = cbuf[cpos];
                    if (c >= 0x0001 && c <= 0x007F) {
                        utflen++;
                    } else if (c > 0x07FF) {
                        utflen += 3;
                    } else {
                        utflen += 2;
                    }
                }
                off += csize;
            }
            return utflen;
        }

        /**
         * 以 UTF 格式写入给定字符串。此方法用于已知字符串的 UTF 编码长度的情况；显式指定长度可以避免预扫描字符串以确定其 UTF 长度。
         */
        void writeUTF(String s, long utflen) throws IOException {
            if (utflen > 0xFFFFL) {
                throw new UTFDataFormatException();
            }
            writeShort((int) utflen);
            if (utflen == (long) s.length()) {
                writeBytes(s);
            } else {
                writeUTFBody(s);
            }
        }

        /**
         * 以“长”UTF 格式写入给定字符串。“长”UTF 格式与标准 UTF 相同，只是它使用 8 字节的头部（而不是标准的 2 字节）来传达 UTF 编码长度。
         */
        void writeLongUTF(String s) throws IOException {
            writeLongUTF(s, getUTFLength(s));
        }

        /**
         * 以“长”UTF 格式写入给定字符串，其中字符串的 UTF 编码长度已知。
         */
        void writeLongUTF(String s, long utflen) throws IOException {
            writeLong(utflen);
            if (utflen == (long) s.length()) {
                writeBytes(s);
            } else {
                writeUTFBody(s);
            }
        }

        /**
         * 写入给定字符串的 UTF 编码的“主体”（即，UTF 表示形式减去 2 字节或 8 字节的长度头部）。
         */
        private void writeUTFBody(String s) throws IOException {
            int limit = MAX_BLOCK_SIZE - 3;
            int len = s.length();
            for (int off = 0; off < len; ) {
                int csize = Math.min(len - off, CHAR_BUF_SIZE);
                s.getChars(off, off + csize, cbuf, 0);
                for (int cpos = 0; cpos < csize; cpos++) {
                    char c = cbuf[cpos];
                    if (pos <= limit) {
                        if (c <= 0x007F && c != 0) {
                            buf[pos++] = (byte) c;
                        } else if (c > 0x07FF) {
                            buf[pos + 2] = (byte) (0x80 | ((c >> 0) & 0x3F));
                            buf[pos + 1] = (byte) (0x80 | ((c >> 6) & 0x3F));
                            buf[pos + 0] = (byte) (0xE0 | ((c >> 12) & 0x0F));
                            pos += 3;
                        } else {
                            buf[pos + 1] = (byte) (0x80 | ((c >> 0) & 0x3F));
                            buf[pos + 0] = (byte) (0xC0 | ((c >> 6) & 0x1F));
                            pos += 2;
                        }
                    } else {    // 逐字节写入以规范化块
                        if (c <= 0x007F && c != 0) {
                            write(c);
                        } else if (c > 0x07FF) {
                            write(0xE0 | ((c >> 12) & 0x0F));
                            write(0x80 | ((c >> 6) & 0x3F));
                            write(0x80 | ((c >> 0) & 0x3F));
                        } else {
                            write(0xC0 | ((c >> 6) & 0x1F));
                            write(0x80 | ((c >> 0) & 0x3F));
                        }
                    }
                }
                off += csize;
            }
        }
    }

    /**
     * 轻量级身份哈希表，将对象映射到按升序分配的整数句柄。
     */
    private static class HandleTable {

        /* 表中的映射数/下一个可用句柄 */
        private int size;
        /* 确定何时扩展哈希脊的大小阈值 */
        private int threshold;
        /* 计算大小阈值的因子 */
        private final float loadFactor;
        /* 映射哈希值 -> 候选句柄值 */
        private int[] spine;
        /* 映射句柄值 -> 下一个候选句柄值 */
        private int[] next;
        /* 映射句柄值 -> 关联对象 */
        private Object[] objs;

        /**
         * 创建具有给定容量和负载因子的新 HandleTable。
         */
        HandleTable(int initialCapacity, float loadFactor) {
            this.loadFactor = loadFactor;
            spine = new int[initialCapacity];
            next = new int[initialCapacity];
            objs = new Object[initialCapacity];
            threshold = (int) (initialCapacity * loadFactor);
            clear();
        }

        /**
         * 将下一个可用句柄分配给给定对象，并返回句柄值。句柄从 0 开始按升序分配。
         */
        int assign(Object obj) {
            if (size >= next.length) {
                growEntries();
            }
            if (size >= threshold) {
                growSpine();
            }
            insert(obj, size);
            return size++;
        }

        /**
         * 查找并返回与给定对象关联的句柄，如果未找到映射则返回 -1。
         */
        int lookup(Object obj) {
            if (size == 0) {
                return -1;
            }
            int index = hash(obj) % spine.length;
            for (int i = spine[index]; i >= 0; i = next[i]) {
                if (objs[i] == obj) {
                    return i;
                }
            }
            return -1;
        }

        /**
         * 重置表为其初始（空）状态。
         */
        void clear() {
            Arrays.fill(spine, -1);
            Arrays.fill(objs, 0, size, null);
            size = 0;
        }

        /**
         * 返回表中当前的映射数。
         */
        int size() {
            return size;
        }

        /**
         * 将对象 -> 句柄映射插入表中。假设表足够大以容纳新映射。
         */
        private void insert(Object obj, int handle) {
            int index = hash(obj) % spine.length;
            objs[handle] = obj;
            next[handle] = spine[index];
            spine[index] = handle;
        }

        /**
         * 扩展哈希“脊”——相当于增加传统哈希表中的桶数。
         */
        private void growSpine() {
            spine = new int[(spine.length << 1) + 1];
            threshold = (int) (spine.length * loadFactor);
            Arrays.fill(spine, -1);
            for (int i = 0; i < size; i++) {
                insert(objs[i], i);
            }
        }

        /**
         * 通过延长条目数组增加哈希表容量。
         */
        private void growEntries() {
            int newLength = (next.length << 1) + 1;
            int[] newNext = new int[newLength];
            System.arraycopy(next, 0, newNext, 0, size);
            next = newNext;

            Object[] newObjs = new Object[newLength];
            System.arraycopy(objs, 0, newObjs, 0, size);
            objs = newObjs;
        }

        /**
         * 返回给定对象的哈希值。
         */
        private int hash(Object obj) {
            return System.identityHashCode(obj) & 0x7FFFFFFF;
        }
    }

    /**
     * 轻量级身份哈希表，将对象映射到替换对象。
     */
    private static class ReplaceTable {

        /* 映射对象 -> 索引 */
        private final HandleTable htab;
        /* 映射索引 -> 替换对象 */
        private Object[] reps;

        /**
         * 创建具有给定容量和负载因子的新 ReplaceTable。
         */
        ReplaceTable(int initialCapacity, float loadFactor) {
            htab = new HandleTable(initialCapacity, loadFactor);
            reps = new Object[initialCapacity];
        }

        /**
         * 输入从对象到替换对象的映射。
         */
        void assign(Object obj, Object rep) {
            int index = htab.assign(obj);
            while (index >= reps.length) {
                grow();
            }
            reps[index] = rep;
        }

        /**
         * 查找并返回给定对象的替换对象。如果未找到替换对象，则返回查找对象本身。
         */
        Object lookup(Object obj) {
            int index = htab.lookup(obj);
            return (index >= 0) ? reps[index] : obj;
        }

        /**
         * 重置表为其初始（空）状态。
         */
        void clear() {
            Arrays.fill(reps, 0, htab.size(), null);
            htab.clear();
        }

        /**
         * 返回表中当前的映射数。
         */
        int size() {
            return htab.size();
        }

        /**
         * 增加表的容量。
         */
        private void grow() {
            Object[] newReps = new Object[(reps.length << 1) + 1];
            System.arraycopy(reps, 0, newReps, 0, reps.length);
            reps = newReps;
        }
    }

    /**
     * 用于保存序列化过程状态的调试信息堆栈，以便嵌入到异常消息中。
     */
    private static class DebugTraceInfoStack {
        private final List<String> stack;

        DebugTraceInfoStack() {
            stack = new ArrayList<>();
        }

        /**
         * 从包含的列表中移除所有元素。
         */
        void clear() {
            stack.clear();
        }

        /**
         * 从包含的列表中移除顶部的对象。
         */
        void pop() {
            stack.remove(stack.size()-1);
        }

        /**
         * 将字符串推入包含的列表的顶部。
         */
        void push(String entry) {
            stack.add("\t- " + entry);
        }

        /**
         * 返回此对象的字符串表示形式。
         */
        public String toString() {
            StringBuilder buffer = new StringBuilder();
            if (!stack.isEmpty()) {
                for(int i = stack.size(); i > 0; i-- ) {
                    buffer.append(stack.get(i-1) + ((i != 1) ? "\n" : ""));
                }
            }
            return buffer.toString();
        }
    }

}
