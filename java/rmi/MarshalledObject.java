
/*
 * 版权所有 (c) 1997, 2016, Oracle 和/或其关联公司。保留所有权利。
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

package java.rmi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamConstants;
import java.io.OutputStream;
import java.io.Serializable;
import java.security.AccessController;
import java.security.PrivilegedAction;

import sun.rmi.server.MarshalInputStream;
import sun.rmi.server.MarshalOutputStream;

import sun.misc.ObjectInputFilter;

/**
 * <code>MarshalledObject</code> 包含一个字节流，其中包含传递给其构造函数的对象的序列化表示。 <code>get</code>
 * 方法返回一个从包含的字节流中反序列化的新副本。 包含的对象使用与 RMI 调用参数和返回值的序列化和反序列化相同的序列化语义进行序列化和反序列化：
 * 当创建序列化形式时：
 *
 * <ul>
 * <li> 类用可以从其加载类的代码库 URL 进行注释（如果可用），并且
 * <li> <code>MarshalledObject</code> 中的任何远程对象都由其存根的序列化实例表示。
 * </ul>
 *
 * <p>当通过 <code>get</code> 方法检索对象副本时，如果类在本地不可用，它将从类描述符序列化时注释的适当位置加载。
 *
 * <p><code>MarshalledObject</code> 有助于在 RMI 调用中传递对象，这些对象不会立即由远程对等方自动反序列化。
 *
 * @param <T> 此 <code>MarshalledObject</code> 中包含的对象类型
 *
 * @author  Ann Wollrath
 * @author  Peter Jones
 * @since   1.2
 */
public final class MarshalledObject<T> implements Serializable {
    /**
     * @serial 序列化表示的字节。如果 <code>objBytes</code> 为
     * <code>null</code>，则序列化的对象是一个 <code>null</code>
     * 引用。
     */
    private byte[] objBytes = null;

    /**
     * @serial 位置注释的字节，这些注释被 <code>equals</code> 忽略。如果 <code>locBytes</code> 为 null，则在序列化过程中没有非
     * <code>null</code> 注释。
     */
    private byte[] locBytes = null;

    /**
     * @serial 包含对象的存储哈希码。
     *
     * @see #hashCode
     */
    private int hash;

    /** 从流中创建实例时使用的过滤器；可能是 null。 */
    private transient ObjectInputFilter objectInputFilter = null;

    /** 表示与 1.2 版本的类兼容。 */
    private static final long serialVersionUID = 8988374069173025854L;

    /**
     * 创建一个新的 <code>MarshalledObject</code>，其中包含提供的对象当前状态的序列化表示。对象使用 RMI 调用参数序列化的语义进行序列化。
     *
     * @param obj 要序列化的对象（必须是可序列化的）
     * @exception IOException 如果发生 <code>IOException</code>；如果 <code>obj</code> 不是
     * 可序列化的，可能会发生 <code>IOException</code>。
     * @since 1.2
     */
    public MarshalledObject(T obj) throws IOException {
        if (obj == null) {
            hash = 13;
            return;
        }

        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ByteArrayOutputStream lout = new ByteArrayOutputStream();
        MarshalledObjectOutputStream out =
            new MarshalledObjectOutputStream(bout, lout);
        out.writeObject(obj);
        out.flush();
        objBytes = bout.toByteArray();
        // 如果没有注释，locBytes 为 null
        locBytes = (out.hadAnnotations() ? lout.toByteArray() : null);

        /*
         * 从对象的序列化表示中计算哈希值
         * 以便在 VM 之间发送时哈希码可以比较。
         */
        int h = 0;
        for (int i = 0; i < objBytes.length; i++) {
            h = 31 * h + objBytes[i];
        }
        hash = h;
    }

    /**
     * 读取对象的状态并保存流的序列化过滤器，以便在对象反序列化时使用。
     *
     * @param stream 流
     * @throws IOException 如果发生 I/O 错误
     * @throws ClassNotFoundException 如果找不到类
     */
    private void readObject(ObjectInputStream stream)
        throws IOException, ClassNotFoundException {
        stream.defaultReadObject();     // 读取所有字段
        objectInputFilter = ObjectInputFilter.Config.getObjectInputFilter(stream);
    }

    /**
     * 返回包含的 marshalledobject 的新副本。内部表示使用 RMI 调用参数反序列化的语义进行反序列化。
     *
     * @return 包含对象的副本
     * @exception IOException 如果在从内部表示反序列化对象时发生 <code>IOException</code>
     * @exception ClassNotFoundException 如果在从内部表示反序列化对象时发生
     * <code>ClassNotFoundException</code>
     * 无法找到
     * @since 1.2
     */
    public T get() throws IOException, ClassNotFoundException {
        if (objBytes == null)   // 必须是一个 null 对象
            return null;

        ByteArrayInputStream bin = new ByteArrayInputStream(objBytes);
        // 如果没有注释，locBytes 为 null
        ByteArrayInputStream lin =
            (locBytes == null ? null : new ByteArrayInputStream(locBytes));
        MarshalledObjectInputStream in =
            new MarshalledObjectInputStream(bin, lin, objectInputFilter);
        @SuppressWarnings("unchecked")
        T obj = (T) in.readObject();
        in.close();
        return obj;
    }


                /**
     * 返回此 <code>MarshalledObject</code> 的哈希码。
     *
     * @return 一个哈希码
     */
    public int hashCode() {
        return hash;
    }

    /**
     * 将此 <code>MarshalledObject</code> 与另一个对象进行比较。
     * 当且仅当参数引用一个包含与本对象完全相同的序列化表示的 <code>MarshalledObject</code> 时返回 true。
     * 比较时忽略任何类代码库注释，这意味着如果两个对象的序列化表示相同（除了序列化表示中每个类的代码库外），则它们是等效的。
     *
     * @param obj 要与此 <code>MarshalledObject</code> 比较的对象
     * @return 如果参数包含等效的序列化对象，则返回 <code>true</code>；否则返回 <code>false</code>
     * @since 1.2
     */
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (obj != null && obj instanceof MarshalledObject) {
            MarshalledObject<?> other = (MarshalledObject<?>) obj;

            // 如果任一对象为 null 引用，则两个都必须为 null
            if (objBytes == null || other.objBytes == null)
                return objBytes == other.objBytes;

            // 快速、简单的测试
            if (objBytes.length != other.objBytes.length)
                return false;

            //!! 有关于在 1.2 中添加数组比较方法的讨论 -- 如果如此，应该重写此部分。 -arnold
            for (int i = 0; i < objBytes.length; ++i) {
                if (objBytes[i] != other.objBytes[i])
                    return false;
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * 该类用于为 <code>MarshalledObject</code> 序列化对象。它将位置注释放在一边，以便两个 <code>MarshalledObject</code>
     * 如果仅在位置注释上不同，也可以进行相等性比较。使用此流写入的对象应从 <code>MarshalledObjectInputStream</code> 读回。
     *
     * @see java.rmi.MarshalledObject
     * @see MarshalledObjectInputStream
     */
    private static class MarshalledObjectOutputStream
        extends MarshalOutputStream
    {
        /** 用于写入位置对象的流。 */
        private ObjectOutputStream locOut;

        /** <code>true</code> 如果写入了非 <code>null</code> 注释。 */
        private boolean hadAnnotations;

        /**
         * 创建一个新的 <code>MarshalledObjectOutputStream</code>，其非位置字节将写入 <code>objOut</code>，
         * 位置注释（如果有）将写入 <code>locOut</code>。
         */
        MarshalledObjectOutputStream(OutputStream objOut, OutputStream locOut)
            throws IOException
        {
            super(objOut);
            this.useProtocolVersion(ObjectStreamConstants.PROTOCOL_VERSION_2);
            this.locOut = new ObjectOutputStream(locOut);
            hadAnnotations = false;
        }

        /**
         * 如果已向此流写入任何非 <code>null</code> 位置注释，则返回 <code>true</code>。
         */
        boolean hadAnnotations() {
            return hadAnnotations;
        }

        /**
         * 覆盖 MarshalOutputStream.writeLocation 实现以将注释写入位置流。
         */
        protected void writeLocation(String loc) throws IOException {
            hadAnnotations |= (loc != null);
            locOut.writeObject(loc);
        }


        public void flush() throws IOException {
            super.flush();
            locOut.flush();
        }
    }

    /**
     * <code>MarshalledObjectOutputStream</code> 的对应类。
     *
     * @see MarshalledObjectOutputStream
     */
    private static class MarshalledObjectInputStream
        extends MarshalInputStream
    {
        /**
         * 从中读取注释的流。如果这是 <code>null</code>，则所有注释都是 <code>null</code>。
         */
        private ObjectInputStream locIn;

        /**
         * 创建一个新的 <code>MarshalledObjectInputStream</code>，它从 <code>objIn</code> 读取对象，
         * 从 <code>locIn</code> 读取注释。如果 <code>locIn</code> 是 <code>null</code>，则所有注释都将是 <code>null</code>。
         */
        MarshalledObjectInputStream(InputStream objIn, InputStream locIn,
                    ObjectInputFilter filter)
            throws IOException
        {
            super(objIn);
            this.locIn = (locIn == null ? null : new ObjectInputStream(locIn));
            if (filter != null) {
                AccessController.doPrivileged(new PrivilegedAction<Void>() {
                    @Override
                    public Void run() {
                        ObjectInputFilter.Config.setObjectInputFilter(MarshalledObjectInputStream.this, filter);
                        if (MarshalledObjectInputStream.this.locIn != null) {
                            ObjectInputFilter.Config.setObjectInputFilter(MarshalledObjectInputStream.this.locIn, filter);
                        }
                        return null;
                    }
                });
            }
        }

        /**
         * 覆盖 MarshalInputStream.readLocation 以从给定的流返回位置，或者如果给定的位置流为 <code>null</code>，则返回 <code>null</code>。
         */
        protected Object readLocation()
            throws IOException, ClassNotFoundException
        {
            return (locIn == null ? null : locIn.readObject());
        }
    }

}
