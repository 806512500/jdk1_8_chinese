/*
 * 版权所有 (c) 1996, 2006, Oracle 和/或其关联公司。保留所有权利。
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

package java.io;

/**
 * 当序列化运行时检测到类的以下问题之一时抛出。
 * <UL>
 * <LI> 类的序列化版本与从流中读取的类描述符的版本不匹配
 * <LI> 类包含未知的数据类型
 * <LI> 类没有可访问的无参数构造函数
 * </UL>
 *
 * @author  未署名
 * @since   JDK1.1
 */
public class InvalidClassException extends ObjectStreamException {

    private static final long serialVersionUID = -4333316296251054416L;

    /**
     * 无效类的名称。
     *
     * @serial 无效类的名称。
     */
    public String classname;

    /**
     * 根据指定的原因报告一个 InvalidClassException。
     *
     * @param reason  描述异常原因的字符串。
     */
    public InvalidClassException(String reason) {
        super(reason);
    }

    /**
     * 构造一个 InvalidClassException 对象。
     *
     * @param cname   命名无效类的字符串。
     * @param reason  描述异常原因的字符串。
     */
    public InvalidClassException(String cname, String reason) {
        super(reason);
        classname = cname;
    }

    /**
     * 生成消息并包含类名（如果存在）。
     */
    public String getMessage() {
        if (classname == null)
            return super.getMessage();
        else
            return classname + "; " + super.getMessage();
    }
}
