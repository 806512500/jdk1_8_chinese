/*
 * 版权所有 (c) 1996, 2005, Oracle 和/或其附属公司。保留所有权利。
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

/*
 * (C) 版权所有 Taligent, Inc. 1996, 1997 - 保留所有权利
 * (C) 版权所有 IBM Corp. 1996 - 1998 - 保留所有权利
 *
 * 本源代码和文档的原始版本
 * 版权和所有权归 Taligent, Inc. 所有，它是 IBM 的全资子公司。这些材料是根据 Taligent 和 Sun 之间的许可协议提供的。这项技术受多项美国和国际专利保护。
 *
 * 本通知和对 Taligent 的归属不得删除。Taligent 是 Taligent, Inc. 的注册商标。
 *
 */

package java.util;

/**
 * 表示资源缺失。
 * @see java.lang.Exception
 * @see ResourceBundle
 * @author      Mark Davis
 * @since       JDK1.1
 */
public
class MissingResourceException extends RuntimeException {

    /**
     * 使用指定的信息构造一个 MissingResourceException。
     * 详细消息是一个描述此特定异常的字符串。
     * @param s 详细消息
     * @param className 资源类的名称
     * @param key 缺失资源的键。
     */
    public MissingResourceException(String s, String className, String key) {
        super(s);
        this.className = className;
        this.key = key;
    }

    /**
     * 使用 <code>message</code>，<code>className</code>，<code>key</code> 和 <code>cause</code> 构造一个 <code>MissingResourceException</code>。
     * 此构造函数是包私有的，供 <code>ResourceBundle.getBundle</code> 使用。
     *
     * @param message
     *        详细消息
     * @param className
     *        资源类的名称
     * @param key
     *        缺失资源的键。
     * @param cause
     *        原因（稍后通过 {@link Throwable.getCause()} 方法检索）。允许为 null 值，表示原因不存在或未知。
     */
    MissingResourceException(String message, String className, String key, Throwable cause) {
        super(message, cause);
        this.className = className;
        this.key = key;
    }

    /**
     * 获取构造函数传递的参数。
     *
     * @return 资源类的名称
     */
    public String getClassName() {
        return className;
    }

    /**
     * 获取构造函数传递的参数。
     *
     * @return 缺失资源的键
     */
    public String getKey() {
        return key;
    }

    //============ 私有成员 ============

    // 与 JDK1.1 的序列化兼容性
    private static final long serialVersionUID = -4876345176062000401L;

    /**
     * 用户请求的资源包的类名。
     * @serial
     */
    private String className;

    /**
     * 用户请求的特定资源的名称。
     * @serial
     */
    private String key;
}