
/*
 * 版权所有 (c) 2000, 2013, Oracle 和/或其附属公司。保留所有权利。
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

package java.lang;

import java.util.Objects;

/**
 * 由 {@link
 * Throwable#getStackTrace()} 返回的堆栈跟踪中的一个元素。每个元素表示一个单独的堆栈帧。
 * 除了堆栈顶部的帧外，所有堆栈帧都表示一个方法调用。堆栈顶部的帧表示生成堆栈跟踪的执行点。
 * 通常，这是与堆栈跟踪对应的可抛出对象的创建点。
 *
 * @since  1.4
 * @author Josh Bloch
 */
public final class StackTraceElement implements java.io.Serializable {
    // 通常由 VM 初始化（公共构造函数在 1.5 中添加）
    private String declaringClass;
    private String methodName;
    private String fileName;
    private int    lineNumber;

    /**
     * 创建一个表示指定执行点的堆栈跟踪元素。
     *
     * @param declaringClass 包含堆栈跟踪元素表示的执行点的类的完全限定名称
     * @param methodName 包含堆栈跟踪元素表示的执行点的方法名称
     * @param fileName 包含堆栈跟踪元素表示的执行点的文件名称，如果此信息不可用，则为 {@code null}
     * @param lineNumber 包含堆栈跟踪元素表示的执行点的源代码行的行号，如果此信息不可用，则为负数。值 -2 表示包含执行点的方法是本地方法
     * @throws NullPointerException 如果 {@code declaringClass} 或 {@code methodName} 为 null
     * @since 1.5
     */
    public StackTraceElement(String declaringClass, String methodName,
                             String fileName, int lineNumber) {
        this.declaringClass = Objects.requireNonNull(declaringClass, "声明类为 null");
        this.methodName     = Objects.requireNonNull(methodName, "方法名称为 null");
        this.fileName       = fileName;
        this.lineNumber     = lineNumber;
    }

    /**
     * 返回包含此堆栈跟踪元素表示的执行点的源文件的名称。通常，这对应于相关 {@code class} 文件的 {@code SourceFile} 属性（如《Java 虚拟机规范》第 4.7.7 节所述）。
     * 在某些系统中，名称可能指代源代码单元，如源代码库中的条目，而不仅仅是文件。
     *
     * @return 包含此堆栈跟踪元素表示的执行点的文件的名称，如果此信息不可用，则为 {@code null}。
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * 返回包含此堆栈跟踪元素表示的执行点的源代码行的行号。通常，这是从相关 {@code class} 文件的 {@code LineNumberTable} 属性派生的（如《Java 虚拟机规范》第 4.7.8 节所述）。
     *
     * @return 包含此堆栈跟踪元素表示的执行点的源代码行的行号，如果此信息不可用，则为负数。
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * 返回包含此堆栈跟踪元素表示的执行点的类的完全限定名称。
     *
     * @return 包含此堆栈跟踪元素表示的执行点的 {@code Class} 的完全限定名称。
     */
    public String getClassName() {
        return declaringClass;
    }

    /**
     * 返回包含此堆栈跟踪元素表示的执行点的方法的名称。如果执行点包含在实例或类初始化器中，此方法将返回适当的 <i>特殊方法名称</i>，即 {@code <init>} 或 {@code <clinit>}，如《Java 虚拟机规范》第 3.9 节所述。
     *
     * @return 包含此堆栈跟踪元素表示的执行点的方法的名称。
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * 如果包含此堆栈跟踪元素表示的执行点的方法是本地方法，则返回 true。
     *
     * @return 如果包含此堆栈跟踪元素表示的执行点的方法是本地方法，则返回 {@code true}。
     */
    public boolean isNativeMethod() {
        return lineNumber == -2;
    }

    /**
     * 返回此堆栈跟踪元素的字符串表示形式。此字符串的格式取决于实现，但以下示例可以视为典型：
     * <ul>
     * <li>
     *   {@code "MyClass.mash(MyClass.java:9)"} - 这里，{@code "MyClass"} 是包含此堆栈跟踪元素表示的执行点的类的 <i>完全限定名称</i>，
     *   {@code "mash"} 是包含执行点的方法的名称，{@code "MyClass.java"} 是包含执行点的源文件，{@code "9"} 是包含执行点的源代码行的行号。
     * <li>
     *   {@code "MyClass.mash(MyClass.java)"} - 与上述相同，但行号不可用。
     * <li>
     *   {@code "MyClass.mash(Unknown Source)"} - 与上述相同，但文件名和行号均不可用。
     * <li>
     *   {@code "MyClass.mash(Native Method)"} - 与上述相同，但文件名和行号均不可用，且已知包含执行点的方法是本地方法。
     * </ul>
     * @see    Throwable#printStackTrace()
     */
    public String toString() {
        return getClassName() + "." + methodName +
            (isNativeMethod() ? "(Native Method)" :
             (fileName != null && lineNumber >= 0 ?
              "(" + fileName + ":" + lineNumber + ")" :
              (fileName != null ?  "("+fileName+")" : "(Unknown Source)")));
    }

                    /**
     * 如果指定的对象是另一个表示与该实例相同执行点的
     * {@code StackTraceElement} 实例，则返回 true。两个堆栈跟踪元素 {@code a} 和
     * {@code b} 相等当且仅当：
     * <pre>{@code
     *     equals(a.getFileName(), b.getFileName()) &&
     *     a.getLineNumber() == b.getLineNumber()) &&
     *     equals(a.getClassName(), b.getClassName()) &&
     *     equals(a.getMethodName(), b.getMethodName())
     * }</pre>
     * 其中 {@code equals} 的语义与 {@link
     * java.util.Objects#equals(Object, Object) Objects.equals} 相同。
     *
     * @param  obj 要与该堆栈跟踪元素进行比较的对象。
     * @return 如果指定的对象是另一个表示与该实例相同
     *         执行点的 {@code StackTraceElement} 实例，则返回 true。
     */
    public boolean equals(Object obj) {
        if (obj==this)
            return true;
        if (!(obj instanceof StackTraceElement))
            return false;
        StackTraceElement e = (StackTraceElement)obj;
        return e.declaringClass.equals(declaringClass) &&
            e.lineNumber == lineNumber &&
            Objects.equals(methodName, e.methodName) &&
            Objects.equals(fileName, e.fileName);
    }

    /**
     * 返回该堆栈跟踪元素的哈希码值。
     */
    public int hashCode() {
        int result = 31*declaringClass.hashCode() + methodName.hashCode();
        result = 31*result + Objects.hashCode(fileName);
        result = 31*result + lineNumber;
        return result;
    }

    private static final long serialVersionUID = 6992337162326171013L;
}
