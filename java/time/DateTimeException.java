/*
 * 版权所有 (c) 2012, 2013, Oracle 和/或其附属公司。保留所有权利。
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
 *
 *
 *
 *
 *
 * 版权所有 (c) 2008-2012, Stephen Colebourne & Michael Nascimento Santos
 *
 * 保留所有权利。
 *
 * 重新分发源代码和二进制形式，无论是否修改，都必须保留上述版权声明，
 * 本许可条件列表和以下免责声明。
 *
 * 重新分发二进制形式必须在随附的文档和/或其他材料中复制上述版权声明，
 * 本许可条件列表和以下免责声明。
 *
 * 未经特定事先书面许可，不得使用 JSR-310 或其贡献者的名字来支持或推广从本软件派生的产品。
 *
 * 本软件由版权所有者和贡献者“按原样”提供，不附带任何明示或暗示的保证，包括但不限于
 * 适销性和特定用途适用性的暗示保证。在任何情况下，版权所有者或贡献者均不对任何直接、间接、偶然、特殊、
 * 范例性或后果性损害（包括但不限于采购替代商品或服务；使用损失、数据丢失或利润损失；
 * 或业务中断）负责，无论是在合同、严格责任还是侵权行为（包括疏忽或其他）中引起的，
 * 无论是否已被告知可能发生此类损害。
 */
package java.time;

/**
 * 用于表示计算日期时间时出现问题的异常。
 * <p>
 * 此异常用于表示创建、查询和操作日期时间对象时的问题。
 *
 * @implSpec
 * 本类旨在单线程中使用。
 *
 * @since 1.8
 */
public class DateTimeException extends RuntimeException {

    /**
     * 序列化版本。
     */
    private static final long serialVersionUID = -1632418723876261839L;

    /**
     * 使用指定的消息构造新的日期时间异常。
     *
     * @param message  用于此异常的消息，可以为 null
     */
    public DateTimeException(String message) {
        super(message);
    }

    /**
     * 使用指定的消息和原因构造新的日期时间异常。
     *
     * @param message  用于此异常的消息，可以为 null
     * @param cause  异常的原因，可以为 null
     */
    public DateTimeException(String message, Throwable cause) {
        super(message, cause);
    }

}
