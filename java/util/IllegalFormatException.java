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
 * 当格式字符串包含非法语法或与给定参数不兼容的格式说明符时抛出的未检查异常。只有对应于特定错误的此异常的显式子类型才应被实例化。
 *
 * @since 1.5
 */
public class IllegalFormatException extends IllegalArgumentException {

    private static final long serialVersionUID = 18830826L;

    // 包私有以防止显式实例化
    IllegalFormatException() { }
}