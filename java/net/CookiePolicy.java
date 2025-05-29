/*
 * 版权所有 (c) 2005, 2013, Oracle 和/或其附属公司。保留所有权利。
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

package java.net;

/**
 * CookiePolicy 实现决定应接受哪些 cookie 以及应拒绝哪些 cookie。提供了三种预定义的策略实现，
 * 即 ACCEPT_ALL、ACCEPT_NONE 和 ACCEPT_ORIGINAL_SERVER。
 *
 * <p>详情参见 RFC 2965 第 3.3 节和第 7 节。
 *
 * @author Edward Wang
 * @since 1.6
 */
public interface CookiePolicy {
    /**
     * 一种预定义的策略，接受所有 cookie。
     */
    public static final CookiePolicy ACCEPT_ALL = new CookiePolicy(){
        public boolean shouldAccept(URI uri, HttpCookie cookie) {
            return true;
        }
    };

    /**
     * 一种预定义的策略，不接受任何 cookie。
     */
    public static final CookiePolicy ACCEPT_NONE = new CookiePolicy(){
        public boolean shouldAccept(URI uri, HttpCookie cookie) {
            return false;
        }
    };

    /**
     * 一种预定义的策略，仅接受来自原始服务器的 cookie。
     */
    public static final CookiePolicy ACCEPT_ORIGINAL_SERVER  = new CookiePolicy(){
        public boolean shouldAccept(URI uri, HttpCookie cookie) {
            if (uri == null || cookie == null)
                return false;
            return HttpCookie.domainMatches(cookie.getDomain(), uri.getHost());
        }
    };


    /**
     * 用于确定是否应接受此 cookie。
     *
     * @param uri       用于咨询接受策略的 URI
     * @param cookie    问题中的 HttpCookie 对象
     * @return          如果应接受此 cookie，则返回 {@code true}；
     *                  否则，返回 {@code false}
     */
    public boolean shouldAccept(URI uri, HttpCookie cookie);
}
