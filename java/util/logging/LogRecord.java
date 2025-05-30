
/*
 * Copyright (c) 2000, 2016, Oracle and/or its affiliates. All rights reserved.
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

package java.util.logging;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.io.*;

import sun.misc.JavaLangAccess;
import sun.misc.SharedSecrets;

/**
 * LogRecord 对象用于在日志框架和各个日志处理程序之间传递日志请求。
 * <p>
 * 当 LogRecord 被传递到日志框架时，它逻辑上属于框架，客户端应用程序不应再使用或更新它。
 * <p>
 * 注意，如果客户端应用程序未指定明确的源方法名和源类名，则 LogRecord 类将在首次访问时（由于调用 getSourceMethodName 或
 * getSourceClassName）通过分析调用堆栈自动推断它们。因此，如果日志处理程序希望将 LogRecord 传递给另一个线程，或通过 RMI 传输，
 * 并希望随后获取方法名或类名信息，应调用 getSourceClassName 或 getSourceMethodName 以强制填充这些值。
 * <p>
 * <b> 序列化说明：</b>
 * <ul>
 * <li>LogRecord 类是可序列化的。
 *
 * <li> 由于参数数组中的对象可能不可序列化，因此在序列化过程中，参数数组中的所有对象都会被写入为相应的字符串（使用 Object.toString）。
 *
 * <li> 资源包不会作为序列化形式的一部分传输，但资源包名称会传输，接收对象的 readObject 方法将尝试定位合适的资源包。
 *
 * </ul>
 *
 * @since 1.4
 */

public class LogRecord implements java.io.Serializable {
    private static final AtomicLong globalSequenceNumber
        = new AtomicLong(0);

    /**
     * 线程ID的默认值将是当前线程的线程ID，以便于关联，除非它大于 MIN_SEQUENTIAL_THREAD_ID，
     * 在这种情况下，我们更努力地保持线程ID的唯一性，避免由于32位溢出导致的冲突。不幸的是，
     * LogRecord.getThreadID() 返回 int，而 Thread.getId() 返回 long。
     */
    private static final int MIN_SEQUENTIAL_THREAD_ID = Integer.MAX_VALUE / 2;

    private static final AtomicInteger nextThreadId
        = new AtomicInteger(MIN_SEQUENTIAL_THREAD_ID);

    private static final ThreadLocal<Integer> threadIds = new ThreadLocal<>();

    /**
     * @serial 日志消息级别
     */
    private Level level;

    /**
     * @serial 序列号
     */
    private long sequenceNumber;

    /**
     * @serial 发出日志调用的类
     */
    private String sourceClassName;

    /**
     * @serial 发出日志调用的方法
     */
    private String sourceMethodName;

    /**
     * @serial 未本地化的原始消息文本
     */
    private String message;

    /**
     * @serial 发出日志调用的线程的线程ID。
     */
    private int threadID;

    /**
     * @serial 事件时间，自1970年以来的毫秒数
     */
    private long millis;

    /**
     * @serial 与日志消息关联的 Throwable（如果有）
     */
    private Throwable thrown;

    /**
     * @serial 源 Logger 的名称。
     */
    private String loggerName;

    /**
     * @serial 用于本地化日志消息的资源包名称。
     */
    private String resourceBundleName;

    private transient boolean needToInferCaller;
    private transient Object parameters[];
    private transient ResourceBundle resourceBundle;

    /**
     * 返回新 LogRecord 的线程ID的默认值。
     */
    private int defaultThreadID() {
        long tid = Thread.currentThread().getId();
        if (tid < MIN_SEQUENTIAL_THREAD_ID) {
            return (int) tid;
        } else {
            Integer id = threadIds.get();
            if (id == null) {
                id = nextThreadId.getAndIncrement();
                threadIds.set(id);
            }
            return id;
        }
    }

    /**
     * 使用给定的级别和消息值构造一个 LogRecord。
     * <p>
     * 序列属性将被初始化为一个新的唯一值。这些序列值在 VM 内按递增顺序分配。
     * <p>
     * millis 属性将被初始化为当前时间。
     * <p>
     * 线程ID属性将被初始化为当前线程的唯一ID。
     * <p>
     * 所有其他属性将被初始化为 "null"。
     *
     * @param level  日志级别值
     * @param msg  原始未本地化的日志消息（可以为 null）
     */
    public LogRecord(Level level, String msg) {
        // 确保 level 不为 null，通过调用随机方法。
        level.getClass();
        this.level = level;
        message = msg;
        // 分配一个线程ID和一个唯一的序列号。
        sequenceNumber = globalSequenceNumber.getAndIncrement();
        threadID = defaultThreadID();
        millis = System.currentTimeMillis();
        needToInferCaller = true;
   }

    /**
     * 获取源 Logger 的名称。
     *
     * @return 源 Logger 的名称（可以为 null）
     */
    public String getLoggerName() {
        return loggerName;
    }

    /**
     * 设置源 Logger 的名称。
     *
     * @param name   源 Logger 的名称（可以为 null）
     */
    public void setLoggerName(String name) {
        loggerName = name;
    }

    /**
     * 获取本地化资源包
     * <p>
     * 这是应该用于在格式化消息之前本地化消息字符串的 ResourceBundle。结果可能为 null，
     * 如果消息不可本地化，或者没有合适的 ResourceBundle 可用。
     * @return 本地化资源包
     */
    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    /**
     * 设置本地化资源包。
     *
     * @param bundle  本地化包（可以为 null）
     */
    public void setResourceBundle(ResourceBundle bundle) {
        resourceBundle = bundle;
    }

    /**
     * 获取本地化资源包名称
     * <p>
     * 这是应该用于在格式化消息之前本地化消息字符串的 ResourceBundle 的名称。结果可能为 null，
     * 如果消息不可本地化。
     * @return 本地化资源包名称
     */
    public String getResourceBundleName() {
        return resourceBundleName;
    }

    /**
     * 设置本地化资源包名称。
     *
     * @param name  本地化包名称（可以为 null）
     */
    public void setResourceBundleName(String name) {
        resourceBundleName = name;
    }

    /**
     * 获取日志消息级别，例如 Level.SEVERE。
     * @return 日志消息级别
     */
    public Level getLevel() {
        return level;
    }

    /**
     * 设置日志消息级别，例如 Level.SEVERE。
     * @param level 日志消息级别
     */
    public void setLevel(Level level) {
        if (level == null) {
            throw new NullPointerException();
        }
        this.level = level;
    }

    /**
     * 获取序列号。
     * <p>
     * 序列号通常在 LogRecord 构造函数中分配，该构造函数为每个新的 LogRecord 分配唯一的序列号，按递增顺序分配。
     * @return 序列号
     */
    public long getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * 设置序列号。
     * <p>
     * 序列号通常在 LogRecord 构造函数中分配，因此通常不需要使用此方法。
     * @param seq 序列号
     */
    public void setSequenceNumber(long seq) {
        sequenceNumber = seq;
    }

    /**
     * 获取发出日志请求的类的名称。
     * <p>
     * 注意，这个 sourceClassName 未经过验证，可能会被伪造。此信息可能是作为日志调用的一部分提供的，
     * 也可能是由日志框架自动推断的。在后一种情况下，信息可能只是近似的，实际上可能描述了堆栈帧上的早期调用。
     * <p>
     * 如果无法获取信息，则可能为 null。
     *
     * @return 源类名
     */
    public String getSourceClassName() {
        if (needToInferCaller) {
            inferCaller();
        }
        return sourceClassName;
    }

    /**
     * 设置发出日志请求的类的名称。
     *
     * @param sourceClassName 源类名（可以为 null）
     */
    public void setSourceClassName(String sourceClassName) {
        this.sourceClassName = sourceClassName;
        needToInferCaller = false;
    }

    /**
     * 获取发出日志请求的方法的名称。
     * <p>
     * 注意，这个 sourceMethodName 未经过验证，可能会被伪造。此信息可能是作为日志调用的一部分提供的，
     * 也可能是由日志框架自动推断的。在后一种情况下，信息可能只是近似的，实际上可能描述了堆栈帧上的早期调用。
     * <p>
     * 如果无法获取信息，则可能为 null。
     *
     * @return 源方法名
     */
    public String getSourceMethodName() {
        if (needToInferCaller) {
            inferCaller();
        }
        return sourceMethodName;
    }

    /**
     * 设置发出日志请求的方法的名称。
     *
     * @param sourceMethodName 源方法名（可以为 null）
     */
    public void setSourceMethodName(String sourceMethodName) {
        this.sourceMethodName = sourceMethodName;
        needToInferCaller = false;
    }

    /**
     * 获取未本地化或格式化的原始日志消息。
     * <p>
     * 可能为 null，这等同于空字符串 ""。
     * <p>
     * 此消息可能是最终文本或本地化键。
     * <p>
     * 在格式化过程中，如果源 Logger 有一个本地化 ResourceBundle，并且该 ResourceBundle 有此消息字符串的条目，
     * 则消息字符串将被替换为本地化值。
     *
     * @return 原始消息字符串
     */
    public String getMessage() {
        return message;
    }

    /**
     * 设置未本地化或格式化的原始日志消息。
     *
     * @param message 原始消息字符串（可以为 null）
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * 获取日志消息的参数。
     *
     * @return 日志消息参数。如果没有参数，则可能为 null。
     */
    public Object[] getParameters() {
        return parameters;
    }

    /**
     * 设置日志消息的参数。
     *
     * @param parameters 日志消息参数。（可以为 null）
     */
    public void setParameters(Object parameters[]) {
        this.parameters = parameters;
    }

    /**
     * 获取消息来源的线程标识符。
     * <p>
     * 这是在 Java VM 内的线程标识符，可能或可能不映射到任何操作系统 ID。
     *
     * @return 线程ID
     */
    public int getThreadID() {
        return threadID;
    }

    /**
     * 设置消息来源的线程标识符。
     * @param threadID  线程ID
     */
    public void setThreadID(int threadID) {
        this.threadID = threadID;
    }

    /**
     * 获取自1970年以来的事件时间（毫秒）。
     *
     * @return 自1970年以来的事件时间（毫秒）
     */
    public long getMillis() {
        return millis;
    }

    /**
     * 设置事件时间。
     *
     * @param millis 自1970年以来的事件时间（毫秒）
     */
    public void setMillis(long millis) {
        this.millis = millis;
    }

    /**
     * 获取与日志记录关联的任何可抛出对象。
     * <p>
     * 如果事件涉及异常，这将是异常对象。否则为 null。
     *
     * @return 可抛出对象
     */
    public Throwable getThrown() {
        return thrown;
    }

    /**
     * 设置与日志事件关联的可抛出对象。
     *
     * @param thrown  可抛出对象（可以为 null）
     */
    public void setThrown(Throwable thrown) {
        this.thrown = thrown;
    }

    private static final long serialVersionUID = 5372048053134512534L;

    /**
     * @serialData 默认字段，后跟两个字节的版本号（主字节，后跟次字节），后跟日志记录参数数组的信息。
     * 如果没有参数数组，则写入 -1。如果有参数数组（可能长度为零），则写入数组长度作为整数，后跟每个参数的字符串值。
     * 如果参数为 null，则写入 null 字符串。否则，写入 Object.toString() 的输出。
     */
    private void writeObject(ObjectOutputStream out) throws IOException {
        // 我们必须先调用 defaultWriteObject。
        out.defaultWriteObject();

        // 写入我们的版本号。
        out.writeByte(1);
        out.writeByte(0);
        if (parameters == null) {
            out.writeInt(-1);
            return;
        }
        out.writeInt(parameters.length);
        // 写入参数的字符串值。
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i] == null) {
                out.writeObject(null);
            } else {
                out.writeObject(parameters[i].toString());
            }
        }
    }

    private void readObject(ObjectInputStream in)
                        throws IOException, ClassNotFoundException {
        // 我们必须先调用 defaultReadObject。
        in.defaultReadObject();


                    // 读取版本号。
        byte major = in.readByte();
        byte minor = in.readByte();
        if (major != 1) {
            throw new IOException("LogRecord: 版本错误: " + major + "." + minor);
        }
        int len = in.readInt();
        if (len < -1) {
            throw new NegativeArraySizeException();
        } else if (len == -1) {
            parameters = null;
        } else if (len < 255) {
            parameters = new Object[len];
            for (int i = 0; i < parameters.length; i++) {
                parameters[i] = in.readObject();
            }
        } else {
            List<Object> params = new ArrayList<>(Math.min(len, 1024));
            for (int i = 0; i < len; i++) {
                params.add(in.readObject());
            }
            parameters = params.toArray(new Object[params.size()]);
        }
        // 如有必要，尝试重新生成资源包。
        if (resourceBundleName != null) {
            try {
                // 使用系统类加载器确保资源包实例与空加载器使用的实例不同
                final ResourceBundle bundle =
                        ResourceBundle.getBundle(resourceBundleName,
                                Locale.getDefault(),
                                ClassLoader.getSystemClassLoader());
                resourceBundle = bundle;
            } catch (MissingResourceException ex) {
                // 这里不是抛出异常的好地方，所以我们只是将资源包设为 null。
                resourceBundle = null;
            }
        }

        needToInferCaller = false;
    }

    // 私有方法，用于推断调用者类和方法名
    private void inferCaller() {
        needToInferCaller = false;
        JavaLangAccess access = SharedSecrets.getJavaLangAccess();
        Throwable throwable = new Throwable();
        int depth = access.getStackTraceDepth(throwable);

        boolean lookingForLogger = true;
        for (int ix = 0; ix < depth; ix++) {
            // 直接调用 getStackTraceElement 可以防止 VM 构建整个堆栈帧的成本。
            StackTraceElement frame =
                access.getStackTraceElement(throwable, ix);
            String cname = frame.getClassName();
            boolean isLoggerImpl = isLoggerImplFrame(cname);
            if (lookingForLogger) {
                // 跳过所有帧，直到找到第一个日志记录器帧。
                if (isLoggerImpl) {
                    lookingForLogger = false;
                }
            } else {
                if (!isLoggerImpl) {
                    // 跳过反射调用
                    if (!cname.startsWith("java.lang.reflect.") && !cname.startsWith("sun.reflect.")) {
                       // 我们找到了相关的帧。
                       setSourceClassName(cname);
                       setSourceMethodName(frame.getMethodName());
                       return;
                    }
                }
            }
        }
        // 我们没有找到合适的帧，所以就这样吧。这里我们只是尽力而为。
    }

    private boolean isLoggerImplFrame(String cname) {
        // 日志记录可能是为平台日志记录器创建的
        return (cname.equals("java.util.logging.Logger") ||
                cname.startsWith("java.util.logging.LoggingProxyImpl") ||
                cname.startsWith("sun.util.logging."));
    }
}
