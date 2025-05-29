/*
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
 * 由 Doug Lea 在 JCP JSR-166 专家小组成员的帮助下编写，并根据
 * http://creativecommons.org/publicdomain/zero/1.0/ 解释的条款发布到公共领域。
 */

package java.util.concurrent;

/**
 * 一个混合风格的接口，用于标记在给定延迟后应被处理的对象。
 *
 * <p>此接口的实现必须定义一个 {@code compareTo} 方法，该方法提供的顺序应与
 * 其 {@code getDelay} 方法一致。
 *
 * @since 1.5
 * @author Doug Lea
 */
public interface Delayed extends Comparable<Delayed> {

    /**
     * 以给定的时间单位返回与此对象关联的剩余延迟。
     *
     * @param unit 时间单位
     * @return 剩余延迟；零或负值表示延迟已过期
     */
    long getDelay(TimeUnit unit);
}