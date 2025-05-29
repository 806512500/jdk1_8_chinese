/*
 * 版权所有 (c) 2000, 2013, Oracle 和/或其关联公司。保留所有权利。
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


package java.util.logging;

/**
 * Filter 可用于提供比日志级别提供的控制更为精细的控制，以决定哪些内容会被记录。
 * <p>
 * 每个 Logger 和每个 Handler 都可以关联一个过滤器。Logger 或 Handler 会调用 isLoggable 方法来检查
 * 给定的 LogRecord 是否应该被发布。如果 isLoggable 返回 false，LogRecord 将被丢弃。
 *
 * @since 1.4
 */
@FunctionalInterface
public interface Filter {

    /**
     * 检查给定的日志记录是否应该被发布。
     * @param record 一个 LogRecord
     * @return 如果日志记录应该被发布，则返回 true。
     */
    public boolean isLoggable(LogRecord record);
}
