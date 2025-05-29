/*
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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

package java.net;

import java.util.List;
import java.util.Map;

/**
 * CookieStore 对象表示一个 cookie 存储。可以存储和检索 cookie。
 *
 * <p>{@link CookieManager} 将调用 {@code CookieStore.add} 保存每个传入的 HTTP 响应中的 cookie，
 * 并调用 {@code CookieStore.get} 为每个传出的 HTTP 请求检索 cookie。CookieStore 负责移除已过期的 HttpCookie 实例。
 *
 * @author Edward Wang
 * @since 1.6
 */
public interface CookieStore {
    /**
     * 向存储中添加一个 HTTP cookie。此方法在每个传入的 HTTP 响应时被调用。
     *
     * <p>要存储的 cookie 可能与 URI 关联，也可能不关联。如果它不与 URI 关联，cookie 的域和路径属性将指示其来源。
     * 如果它与 URI 关联且其域和路径属性未指定，则给定的 URI 将指示此 cookie 的来源。
     *
     * <p>如果给定 URI 已经存在一个对应的 cookie，则用新的 cookie 替换它。
     *
     * @param uri       与该 cookie 关联的 URI。
     *                  如果为 {@code null}，则此 cookie 不与任何 URI 关联
     * @param cookie    要存储的 cookie
     *
     * @throws NullPointerException 如果 {@code cookie} 为 {@code null}
     *
     * @see #get
     *
     */
    public void add(URI uri, HttpCookie cookie);


    /**
     * 检索与给定 URI 关联或其域与给定 URI 匹配的 cookie。仅返回未过期的 cookie。
     * 此方法在每个传出的 HTTP 请求时被调用。
     *
     * @return          一个不可变的 HttpCookie 列表，
     *                  如果没有 cookie 匹配给定的 URI，则返回空列表
     *
     * @param uri       与要返回的 cookie 关联的 URI
     *
     * @throws NullPointerException 如果 {@code uri} 为 {@code null}
     *
     * @see #add
     *
     */
    public List<HttpCookie> get(URI uri);


    /**
     * 获取 cookie 存储中所有未过期的 cookie。
     *
     * @return          一个不可变的 http cookie 列表；
     *                  如果存储中没有 http cookie，则返回空列表
     */
    public List<HttpCookie> getCookies();


    /**
     * 获取标识此 cookie 存储中的 cookie 的所有 URI。
     *
     * @return          一个不可变的 URI 列表；
     *                  如果此 cookie 存储中的 cookie 没有与任何 URI 关联，则返回空列表
     */
    public List<URI> getURIs();


    /**
     * 从存储中移除一个 cookie。
     *
     * @param uri       与该 cookie 关联的 URI。
     *                  如果为 {@code null}，则要移除的 cookie 在添加时未与 URI 关联；
     *                  如果不为 {@code null}，则要移除的 cookie 在添加时与给定的 URI 关联。
     * @param cookie    要移除的 cookie
     *
     * @return          如果此存储包含指定的 cookie，则返回 {@code true}
     *
     * @throws NullPointerException 如果 {@code cookie} 为 {@code null}
     */
    public boolean remove(URI uri, HttpCookie cookie);


    /**
     * 从此 cookie 存储中移除所有 cookie。
     *
     * @return          如果此存储因调用而发生变化，则返回 {@code true}
     */
    public boolean removeAll();
}
