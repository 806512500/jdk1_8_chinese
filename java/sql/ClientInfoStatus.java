/*
 * 版权所有 (c) 2006, Oracle 和/或其附属公司。保留所有权利。
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

package java.sql;

import java.util.*;

/**
 * 枚举类型，表示通过调用 <code>Connection.setClientInfo</code> 设置属性失败的原因
 * @since 1.6
 */

public enum ClientInfoStatus {

    /**
     * 由于某些未知原因，客户端信息属性无法设置
     * @since 1.6
     */
    REASON_UNKNOWN,

    /**
     * 指定的客户端信息属性名称不是已识别的属性名称。
     * @since 1.6
     */
    REASON_UNKNOWN_PROPERTY,

    /**
     * 指定的客户端信息属性值无效。
     * @since 1.6
     */
    REASON_VALUE_INVALID,

    /**
     * 指定的客户端信息属性值太大。
     * @since 1.6
     */
    REASON_VALUE_TRUNCATED
}
