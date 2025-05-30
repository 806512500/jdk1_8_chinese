
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * This file is available under and governed by the GNU General Public
 * License version 2 only, as published by the Free Software Foundation.
 * However, the following notice accompanied the original version of this
 * file:
 *
 * Written by Doug Lea with assistance from members of JCP JSR-166
 * Expert Group and released to the public domain, as explained at
 * http://creativecommons.org/publicdomain/zero/1.0/
 */

package java.util.concurrent;
import java.util.function.Supplier;
import java.util.function.Consumer;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.BiFunction;
import java.util.concurrent.Executor;

/**
 * 一个可能异步计算的阶段，当另一个 CompletionStage 完成时执行一个操作或计算一个值。
 * 一个阶段在其计算终止时完成，但这可能会触发其他依赖阶段。此接口定义的功能只有几种基本形式，
 * 但扩展为一组方法以适应不同的使用风格：<ul>
 *
 * <li>阶段执行的计算可以表示为 Function、Consumer 或 Runnable（分别使用包含
 * <em>apply</em>、<em>accept</em> 或 <em>run</em> 的方法名称），具体取决于是否需要参数和/或产生结果。
 * 例如，{@code stage.thenApply(x -> square(x)).thenAccept(x ->
 * System.out.print(x)).thenRun(() -> System.out.println())}。另一种形式 (<em>compose</em>)
 * 应用阶段本身，而不是它们的结果。</li>
 *
 * <li> 一个阶段的执行可以由一个阶段的完成触发，或者由两个阶段的完成触发，或者由两个阶段中的任何一个完成触发。
 * 依赖于单个阶段的方法使用前缀 <em>then</em>。由 <em>两个</em> 阶段的完成触发的方法可以
 * <em>组合</em> 它们的结果或效果，使用相应命名的方法。由 <em>两个</em> 阶段中的任何一个完成触发的方法
 * 不保证使用哪个结果或效果进行依赖阶段的计算。</li>
 *
 * <li> 阶段之间的依赖关系控制计算的触发，但不保证任何特定的顺序。此外，新阶段的计算执行可以安排为三种方式之一：
 * 默认执行、默认异步执行（使用带有后缀 <em>async</em> 的方法，利用阶段的默认异步执行设施）或自定义（通过提供的 {@link Executor}）。
 * 默认和异步模式的执行属性由 CompletionStage 实现指定，而不是此接口。带有显式 Executor 参数的方法可能具有任意的执行属性，
 * 并且可能不支持并发执行，但以适应异步的方式安排处理。</li>
 *
 * <li> 两种方法形式支持处理触发阶段是否正常完成或异常完成：方法 {@link
 * #whenComplete whenComplete} 允许无论结果如何注入一个动作，同时保留结果在其完成中的状态。
 * 方法 {@link #handle handle} 还允许阶段计算一个替换结果，这可能使其他依赖阶段能够进一步处理。
 * 在所有其他情况下，如果一个阶段的计算因（未检查的）异常或错误而突然终止，则所有依赖于其完成的阶段也会异常完成，
 * 以 {@link CompletionException} 作为其原因。如果一个阶段依赖于 <em>两个</em> 阶段，且这两个阶段都异常完成，
 * 则 CompletionException 可能对应于这两个异常中的任何一个。如果一个阶段依赖于 <em>两个</em> 阶段中的任何一个，
 * 且只有一个异常完成，则不保证依赖阶段是正常完成还是异常完成。在方法 {@code whenComplete} 的情况下，
 * 当提供的动作本身遇到异常时，如果阶段尚未异常完成，则该阶段将异常完成。</li>
 *
 * </ul>
 *
 * <p>所有方法都遵循上述触发、执行和异常完成规范（这些规范在单个方法规范中不再重复）。此外，虽然用于传递完成结果的参数
 * （即类型为 {@code T} 的参数）可以为 null，但传递任何其他参数的 null 值将导致抛出 {@link
 * NullPointerException}。
 *
 * <p>此接口不定义用于最初创建、正常或异常强制完成、探测完成状态或结果或等待阶段完成的方法。
 * CompletionStage 的实现可能提供实现这些效果的手段，视情况而定。方法 {@link #toCompletableFuture}
 * 通过提供一个通用的转换类型，实现了不同实现之间的互操作性。
 *
 * @author Doug Lea
 * @since 1.8
 */
public interface CompletionStage<T> {

    /**
     * 返回一个新的 CompletionStage，当此阶段正常完成时，使用此阶段的结果作为提供的函数的参数执行。
     *
     * 有关异常完成的规则，请参阅 {@link CompletionStage} 文档。
     *
     * @param fn 用于计算返回的 CompletionStage 值的函数
     * @param <U> 函数的返回类型
     * @return 新的 CompletionStage
     */
    public <U> CompletionStage<U> thenApply(Function<? super T,? extends U> fn);

    /**
     * 返回一个新的 CompletionStage，当此阶段正常完成时，使用此阶段的默认异步执行设施，使用此阶段的结果作为提供的函数的参数执行。
     *
     * 有关异常完成的规则，请参阅 {@link CompletionStage} 文档。
     *
     * @param fn 用于计算返回的 CompletionStage 值的函数
     * @param <U> 函数的返回类型
     * @return 新的 CompletionStage
     */
    public <U> CompletionStage<U> thenApplyAsync
        (Function<? super T,? extends U> fn);

    /**
     * 返回一个新的 CompletionStage，当此阶段正常完成时，使用提供的 Executor，使用此阶段的结果作为提供的函数的参数执行。
     *
     * 有关异常完成的规则，请参阅 {@link CompletionStage} 文档。
     *
     * @param fn 用于计算返回的 CompletionStage 值的函数
     * @param executor 用于异步执行的执行器
     * @param <U> 函数的返回类型
     * @return 新的 CompletionStage
     */
    public <U> CompletionStage<U> thenApplyAsync
        (Function<? super T,? extends U> fn,
         Executor executor);

    /**
     * 返回一个新的 CompletionStage，当此阶段正常完成时，使用此阶段的结果作为提供的动作的参数执行。
     *
     * 有关异常完成的规则，请参阅 {@link CompletionStage} 文档。
     *
     * @param action 在完成返回的 CompletionStage 之前执行的动作
     * @return 新的 CompletionStage
     */
    public CompletionStage<Void> thenAccept(Consumer<? super T> action);

    /**
     * 返回一个新的 CompletionStage，当此阶段正常完成时，使用此阶段的默认异步执行设施，使用此阶段的结果作为提供的动作的参数执行。
     *
     * 有关异常完成的规则，请参阅 {@link CompletionStage} 文档。
     *
     * @param action 在完成返回的 CompletionStage 之前执行的动作
     * @return 新的 CompletionStage
     */
    public CompletionStage<Void> thenAcceptAsync(Consumer<? super T> action);

    /**
     * 返回一个新的 CompletionStage，当此阶段正常完成时，使用提供的 Executor，使用此阶段的结果作为提供的动作的参数执行。
     *
     * 有关异常完成的规则，请参阅 {@link CompletionStage} 文档。
     *
     * @param action 在完成返回的 CompletionStage 之前执行的动作
     * @param executor 用于异步执行的执行器
     * @return 新的 CompletionStage
     */
    public CompletionStage<Void> thenAcceptAsync(Consumer<? super T> action,
                                                 Executor executor);
    /**
     * 返回一个新的 CompletionStage，当此阶段正常完成时，执行给定的动作。
     *
     * 有关异常完成的规则，请参阅 {@link CompletionStage} 文档。
     *
     * @param action 在完成返回的 CompletionStage 之前执行的动作
     * @return 新的 CompletionStage
     */
    public CompletionStage<Void> thenRun(Runnable action);

    /**
     * 返回一个新的 CompletionStage，当此阶段正常完成时，使用此阶段的默认异步执行设施，执行给定的动作。
     *
     * 有关异常完成的规则，请参阅 {@link CompletionStage} 文档。
     *
     * @param action 在完成返回的 CompletionStage 之前执行的动作
     * @return 新的 CompletionStage
     */
    public CompletionStage<Void> thenRunAsync(Runnable action);

    /**
     * 返回一个新的 CompletionStage，当此阶段正常完成时，使用提供的 Executor 执行给定的动作。
     *
     * 有关异常完成的规则，请参阅 {@link CompletionStage} 文档。
     *
     * @param action 在完成返回的 CompletionStage 之前执行的动作
     * @param executor 用于异步执行的执行器
     * @return 新的 CompletionStage
     */
    public CompletionStage<Void> thenRunAsync(Runnable action,
                                              Executor executor);

    /**
     * 返回一个新的 CompletionStage，当此阶段和给定的其他阶段都正常完成时，使用两个结果作为提供的函数的参数执行。
     *
     * 有关异常完成的规则，请参阅 {@link CompletionStage} 文档。
     *
     * @param other 其他 CompletionStage
     * @param fn 用于计算返回的 CompletionStage 值的函数
     * @param <U> 其他 CompletionStage 的结果类型
     * @param <V> 函数的返回类型
     * @return 新的 CompletionStage
     */
    public <U,V> CompletionStage<V> thenCombine
        (CompletionStage<? extends U> other,
         BiFunction<? super T,? super U,? extends V> fn);

    /**
     * 返回一个新的 CompletionStage，当此阶段和给定的其他阶段都正常完成时，使用此阶段的默认异步执行设施，使用两个结果作为提供的函数的参数执行。
     *
     * 有关异常完成的规则，请参阅 {@link CompletionStage} 文档。
     *
     * @param other 其他 CompletionStage
     * @param fn 用于计算返回的 CompletionStage 值的函数
     * @param <U> 其他 CompletionStage 的结果类型
     * @param <V> 函数的返回类型
     * @return 新的 CompletionStage
     */
    public <U,V> CompletionStage<V> thenCombineAsync
        (CompletionStage<? extends U> other,
         BiFunction<? super T,? super U,? extends V> fn);

    /**
     * 返回一个新的 CompletionStage，当此阶段和给定的其他阶段都正常完成时，使用提供的执行器，使用两个结果作为提供的函数的参数执行。
     *
     * 有关异常完成的规则，请参阅 {@link CompletionStage} 文档。
     *
     * @param other 其他 CompletionStage
     * @param fn 用于计算返回的 CompletionStage 值的函数
     * @param executor 用于异步执行的执行器
     * @param <U> 其他 CompletionStage 的结果类型
     * @param <V> 函数的返回类型
     * @return 新的 CompletionStage
     */
    public <U,V> CompletionStage<V> thenCombineAsync
        (CompletionStage<? extends U> other,
         BiFunction<? super T,? super U,? extends V> fn,
         Executor executor);

    /**
     * 返回一个新的 CompletionStage，当此阶段和给定的其他阶段都正常完成时，使用两个结果作为提供的动作的参数执行。
     *
     * 有关异常完成的规则，请参阅 {@link CompletionStage} 文档。
     *
     * @param other 其他 CompletionStage
     * @param action 在完成返回的 CompletionStage 之前执行的动作
     * @param <U> 其他 CompletionStage 的结果类型
     * @return 新的 CompletionStage
     */
    public <U> CompletionStage<Void> thenAcceptBoth
        (CompletionStage<? extends U> other,
         BiConsumer<? super T, ? super U> action);

    /**
     * 返回一个新的 CompletionStage，当此阶段和给定的其他阶段都正常完成时，使用此阶段的默认异步执行设施，使用两个结果作为提供的动作的参数执行。
     *
     * @param other 其他 CompletionStage
     * @param action 在完成返回的 CompletionStage 之前执行的动作
     * @param <U> 其他 CompletionStage 的结果类型
     * @return 新的 CompletionStage
     */
    public <U> CompletionStage<Void> thenAcceptBothAsync
        (CompletionStage<? extends U> other,
         BiConsumer<? super T, ? super U> action);


                /**
     * 返回一个新的 CompletionStage，当此阶段和其他给定阶段都正常完成时，使用提供的执行器执行，以两个结果作为提供的函数的参数。
     *
     * @param other 其他 CompletionStage
     * @param action 在完成返回的 CompletionStage 之前执行的操作
     * @param executor 用于异步执行的执行器
     * @param <U> 其他 CompletionStage 的结果类型
     * @return 新的 CompletionStage
     */
    public <U> CompletionStage<Void> thenAcceptBothAsync
        (CompletionStage<? extends U> other,
         BiConsumer<? super T, ? super U> action,
         Executor executor);

    /**
     * 返回一个新的 CompletionStage，当此阶段和其他给定阶段都正常完成时，执行给定的操作。
     *
     * 请参阅 {@link CompletionStage} 文档中的异常完成规则。
     *
     * @param other 其他 CompletionStage
     * @param action 在完成返回的 CompletionStage 之前执行的操作
     * @return 新的 CompletionStage
     */
    public CompletionStage<Void> runAfterBoth(CompletionStage<?> other,
                                              Runnable action);
    /**
     * 返回一个新的 CompletionStage，当此阶段和其他给定阶段都正常完成时，使用此阶段的默认异步执行设施执行给定的操作。
     *
     * 请参阅 {@link CompletionStage} 文档中的异常完成规则。
     *
     * @param other 其他 CompletionStage
     * @param action 在完成返回的 CompletionStage 之前执行的操作
     * @return 新的 CompletionStage
     */
    public CompletionStage<Void> runAfterBothAsync(CompletionStage<?> other,
                                                   Runnable action);

    /**
     * 返回一个新的 CompletionStage，当此阶段和其他给定阶段都正常完成时，使用提供的执行器执行给定的操作。
     *
     * 请参阅 {@link CompletionStage} 文档中的异常完成规则。
     *
     * @param other 其他 CompletionStage
     * @param action 在完成返回的 CompletionStage 之前执行的操作
     * @param executor 用于异步执行的执行器
     * @return 新的 CompletionStage
     */
    public CompletionStage<Void> runAfterBothAsync(CompletionStage<?> other,
                                                   Runnable action,
                                                   Executor executor);
    /**
     * 返回一个新的 CompletionStage，当此阶段或其他给定阶段之一正常完成时，使用提供的函数的相应结果作为参数执行。
     *
     * 请参阅 {@link CompletionStage} 文档中的异常完成规则。
     *
     * @param other 其他 CompletionStage
     * @param fn 用于计算返回的 CompletionStage 的值的函数
     * @param <U> 函数的返回类型
     * @return 新的 CompletionStage
     */
    public <U> CompletionStage<U> applyToEither
        (CompletionStage<? extends T> other,
         Function<? super T, U> fn);

    /**
     * 返回一个新的 CompletionStage，当此阶段或其他给定阶段之一正常完成时，使用此阶段的默认异步执行设施执行，使用提供的函数的相应结果作为参数。
     *
     * 请参阅 {@link CompletionStage} 文档中的异常完成规则。
     *
     * @param other 其他 CompletionStage
     * @param fn 用于计算返回的 CompletionStage 的值的函数
     * @param <U> 函数的返回类型
     * @return 新的 CompletionStage
     */
    public <U> CompletionStage<U> applyToEitherAsync
        (CompletionStage<? extends T> other,
         Function<? super T, U> fn);

    /**
     * 返回一个新的 CompletionStage，当此阶段或其他给定阶段之一正常完成时，使用提供的执行器执行，使用提供的函数的相应结果作为参数。
     *
     * 请参阅 {@link CompletionStage} 文档中的异常完成规则。
     *
     * @param other 其他 CompletionStage
     * @param fn 用于计算返回的 CompletionStage 的值的函数
     * @param executor 用于异步执行的执行器
     * @param <U> 函数的返回类型
     * @return 新的 CompletionStage
     */
    public <U> CompletionStage<U> applyToEitherAsync
        (CompletionStage<? extends T> other,
         Function<? super T, U> fn,
         Executor executor);

    /**
     * 返回一个新的 CompletionStage，当此阶段或其他给定阶段之一正常完成时，使用提供的操作的相应结果作为参数执行。
     *
     * 请参阅 {@link CompletionStage} 文档中的异常完成规则。
     *
     * @param other 其他 CompletionStage
     * @param action 在完成返回的 CompletionStage 之前执行的操作
     * @return 新的 CompletionStage
     */
    public CompletionStage<Void> acceptEither
        (CompletionStage<? extends T> other,
         Consumer<? super T> action);

    /**
     * 返回一个新的 CompletionStage，当此阶段或其他给定阶段之一正常完成时，使用此阶段的默认异步执行设施执行，使用提供的操作的相应结果作为参数。
     *
     * 请参阅 {@link CompletionStage} 文档中的异常完成规则。
     *
     * @param other 其他 CompletionStage
     * @param action 在完成返回的 CompletionStage 之前执行的操作
     * @return 新的 CompletionStage
     */
    public CompletionStage<Void> acceptEitherAsync
        (CompletionStage<? extends T> other,
         Consumer<? super T> action);

    /**
     * 返回一个新的 CompletionStage，当此阶段或其他给定阶段之一正常完成时，使用提供的执行器执行，使用提供的操作的相应结果作为参数。
     *
     * 请参阅 {@link CompletionStage} 文档中的异常完成规则。
     *
     * @param other 其他 CompletionStage
     * @param action 在完成返回的 CompletionStage 之前执行的操作
     * @param executor 用于异步执行的执行器
     * @return 新的 CompletionStage
     */
    public CompletionStage<Void> acceptEitherAsync
        (CompletionStage<? extends T> other,
         Consumer<? super T> action,
         Executor executor);

    /**
     * 返回一个新的 CompletionStage，当此阶段或其他给定阶段之一正常完成时，执行给定的操作。
     *
     * 请参阅 {@link CompletionStage} 文档中的异常完成规则。
     *
     * @param other 其他 CompletionStage
     * @param action 在完成返回的 CompletionStage 之前执行的操作
     * @return 新的 CompletionStage
     */
    public CompletionStage<Void> runAfterEither(CompletionStage<?> other,
                                                Runnable action);

    /**
     * 返回一个新的 CompletionStage，当此阶段或其他给定阶段之一正常完成时，使用此阶段的默认异步执行设施执行给定的操作。
     *
     * 请参阅 {@link CompletionStage} 文档中的异常完成规则。
     *
     * @param other 其他 CompletionStage
     * @param action 在完成返回的 CompletionStage 之前执行的操作
     * @return 新的 CompletionStage
     */
    public CompletionStage<Void> runAfterEitherAsync
        (CompletionStage<?> other,
         Runnable action);

    /**
     * 返回一个新的 CompletionStage，当此阶段或其他给定阶段之一正常完成时，使用提供的执行器执行给定的操作。
     *
     * 请参阅 {@link CompletionStage} 文档中的异常完成规则。
     *
     * @param other 其他 CompletionStage
     * @param action 在完成返回的 CompletionStage 之前执行的操作
     * @param executor 用于异步执行的执行器
     * @return 新的 CompletionStage
     */
    public CompletionStage<Void> runAfterEitherAsync
        (CompletionStage<?> other,
         Runnable action,
         Executor executor);

    /**
     * 返回一个新的 CompletionStage，当此阶段正常完成时，使用此阶段作为提供的函数的参数执行。
     *
     * 请参阅 {@link CompletionStage} 文档中的异常完成规则。
     *
     * @param fn 返回新的 CompletionStage 的函数
     * @param <U> 返回的 CompletionStage 的结果类型
     * @return CompletionStage
     */
    public <U> CompletionStage<U> thenCompose
        (Function<? super T, ? extends CompletionStage<U>> fn);

    /**
     * 返回一个新的 CompletionStage，当此阶段正常完成时，使用此阶段的默认异步执行设施执行，使用此阶段作为提供的函数的参数。
     *
     * 请参阅 {@link CompletionStage} 文档中的异常完成规则。
     *
     * @param fn 返回新的 CompletionStage 的函数
     * @param <U> 返回的 CompletionStage 的结果类型
     * @return CompletionStage
     */
    public <U> CompletionStage<U> thenComposeAsync
        (Function<? super T, ? extends CompletionStage<U>> fn);

    /**
     * 返回一个新的 CompletionStage，当此阶段正常完成时，使用提供的执行器执行，使用此阶段的结果作为提供的函数的参数。
     *
     * 请参阅 {@link CompletionStage} 文档中的异常完成规则。
     *
     * @param fn 返回新的 CompletionStage 的函数
     * @param executor 用于异步执行的执行器
     * @param <U> 返回的 CompletionStage 的结果类型
     * @return CompletionStage
     */
    public <U> CompletionStage<U> thenComposeAsync
        (Function<? super T, ? extends CompletionStage<U>> fn,
         Executor executor);

    /**
     * 返回一个新的 CompletionStage，当此阶段异常完成时，使用此阶段的异常作为提供的函数的参数执行。否则，如果此阶段正常完成，则返回的阶段也正常完成，具有相同的值。
     *
     * @param fn 用于计算返回的 CompletionStage 的值的函数，如果此 CompletionStage 异常完成
     * @return 新的 CompletionStage
     */
    public CompletionStage<T> exceptionally
        (Function<Throwable, ? extends T> fn);

    /**
     * 返回一个新的 CompletionStage，具有与此阶段相同的结果或异常，当此阶段完成时执行给定的操作。
     *
     * <p>当此阶段完成时，给定的操作将使用此阶段的结果（如果没有则为 {@code null}）和异常（如果没有则为 {@code null}）作为参数调用。返回的阶段在操作返回时完成。如果提供的操作本身遇到异常，则返回的阶段将异常完成，除非此阶段也异常完成。
     *
     * @param action 要执行的操作
     * @return 新的 CompletionStage
     */
    public CompletionStage<T> whenComplete
        (BiConsumer<? super T, ? super Throwable> action);

    /**
     * 返回一个新的 CompletionStage，具有与此阶段相同的结果或异常，当此阶段完成时使用此阶段的默认异步执行设施执行给定的操作。
     *
     * <p>当此阶段完成时，给定的操作将使用此阶段的结果（如果没有则为 {@code null}）和异常（如果没有则为 {@code null}）作为参数调用。返回的阶段在操作返回时完成。如果提供的操作本身遇到异常，则返回的阶段将异常完成，除非此阶段也异常完成。
     *
     * @param action 要执行的操作
     * @return 新的 CompletionStage
     */
    public CompletionStage<T> whenCompleteAsync
        (BiConsumer<? super T, ? super Throwable> action);

    /**
     * 返回一个新的 CompletionStage，具有与此阶段相同的结果或异常，当此阶段完成时使用提供的执行器执行给定的操作。
     *
     * <p>当此阶段完成时，给定的操作将使用此阶段的结果（如果没有则为 {@code null}）和异常（如果没有则为 {@code null}）作为参数调用。返回的阶段在操作返回时完成。如果提供的操作本身遇到异常，则返回的阶段将异常完成，除非此阶段也异常完成。
     *
     * @param action 要执行的操作
     * @param executor 用于异步执行的执行器
     * @return 新的 CompletionStage
     */
    public CompletionStage<T> whenCompleteAsync
        (BiConsumer<? super T, ? super Throwable> action,
         Executor executor);

    /**
     * 返回一个新的 CompletionStage，当此阶段正常或异常完成时，使用此阶段的结果和异常作为提供的函数的参数执行。
     *
     * <p>当此阶段完成时，给定的函数将使用此阶段的结果（如果没有则为 {@code null}）和异常（如果没有则为 {@code null}）作为参数调用，函数的结果用于完成返回的阶段。
     *
     * @param fn 用于计算返回的 CompletionStage 的值的函数
     * @param <U> 函数的返回类型
     * @return 新的 CompletionStage
     */
    public <U> CompletionStage<U> handle
        (BiFunction<? super T, Throwable, ? extends U> fn);

    /**
     * 返回一个新的 CompletionStage，当此阶段正常或异常完成时，使用此阶段的默认异步执行设施执行，使用此阶段的结果和异常作为提供的函数的参数。
     *
     * <p>当此阶段完成时，给定的函数将使用此阶段的结果（如果没有则为 {@code null}）和异常（如果没有则为 {@code null}）作为参数调用，函数的结果用于完成返回的阶段。
     *
     * @param fn 用于计算返回的 CompletionStage 的值的函数
     * @param <U> 函数的返回类型
     * @return 新的 CompletionStage
     */
    public <U> CompletionStage<U> handleAsync
        (BiFunction<? super T, Throwable, ? extends U> fn);


                /**
     * 返回一个新的 CompletionStage，当此阶段正常或异常完成时，使用提供的执行器执行，
     * 并将此阶段的结果和异常作为提供的函数的参数。
     *
     * <p>当此阶段完成时，给定的函数将使用此阶段的结果（如果没有则为 {@code null}）
     * 和异常（如果没有则为 {@code null}）作为参数调用，并且函数的结果用于完成返回的阶段。
     *
     * @param fn 用于计算返回的 CompletionStage 值的函数
     * @param executor 用于异步执行的执行器
     * @param <U> 函数的返回类型
     * @return 新的 CompletionStage
     */
    public <U> CompletionStage<U> handleAsync
        (BiFunction<? super T, Throwable, ? extends U> fn,
         Executor executor);

    /**
     * 返回一个 {@link CompletableFuture}，保持与此阶段相同的完成属性。如果此阶段已经是一个
     * CompletableFuture，此方法可能会返回此阶段本身。否则，调用此方法的效果可能等同于
     * {@code thenApply(x -> x)}，但返回一个 {@code CompletableFuture} 类型的实例。
     * 一个不选择与其他组件互操作的 CompletionStage 实现可能会抛出 {@code UnsupportedOperationException}。
     *
     * @return CompletableFuture
     * @throws UnsupportedOperationException 如果此实现不与 CompletableFuture 互操作
     */
    public CompletableFuture<T> toCompletableFuture();

}
