```
/*
 * 版权所有 (c) 2003, Oracle 和/或其附属公司。保留所有权利。
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
 * 当给定的标志组合非法时抛出的未检查异常。
 *
 * <p> 除非另有说明，否则向此类的任何方法或构造函数传递 <tt>null</tt> 参数将导致抛出 {@link
 * NullPointerException}。
 *
 * @since 1.5
 */
public class IllegalFormatFlagsException extends IllegalFormatException {

    private static final long serialVersionUID = 790824L;

    private String flags;

    /**
     * 使用指定的标志构造此类的实例。
     *
     * @param  f
     *         包含非法组合的格式标志集
     */
    public IllegalFormatFlagsException(String f) {
        if (f == null)
            throw new NullPointerException();
        this.flags = f;
    }

    /**
     * 返回包含非法组合的标志集。
     *
     * @return  标志
     */
    public String getFlags() {
        return flags;
    }

    public String getMessage() {
        return "Flags = '" + flags + "'";
    }
}