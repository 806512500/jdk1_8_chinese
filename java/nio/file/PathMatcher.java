/*
 * 版权所有 (c) 2007, 2013, Oracle 和/或其附属公司。保留所有权利。
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

package java.nio.file;

/**
 * 由执行路径匹配操作的对象实现的接口。
 *
 * @since 1.7
 *
 * @see FileSystem#getPathMatcher
 * @see Files#newDirectoryStream(Path,String)
 */
@FunctionalInterface
public interface PathMatcher {
    /**
     * 判断给定的路径是否匹配此匹配器的模式。
     *
     * @param   path
     *          要匹配的路径
     *
     * @return  如果且仅当路径匹配此匹配器的模式时，返回 {@code true}
     */
    boolean matches(Path path);
}
