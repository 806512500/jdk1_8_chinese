/*
 * 版权所有 (c) 1996, 2008, Oracle 和/或其附属公司。保留所有权利。
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

package java.util.zip;

/**
 * 表示已发生数据格式错误。
 *
 * @author      David Connelly
 */
public
class DataFormatException extends Exception {
    private static final long serialVersionUID = 2219632870893641452L;

    /**
     * 构造一个没有详细消息的 DataFormatException。
     */
    public DataFormatException() {
        super();
    }

    /**
     * 使用指定的详细消息构造一个 DataFormatException。
     * 详细消息是一个描述此特定异常的字符串。
     * @param s 包含详细消息的字符串
     */
    public DataFormatException(String s) {
        super(s);
    }
}
