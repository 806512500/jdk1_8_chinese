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
 * 由 Doug Lea 在 JCP JSR-166 专家小组成员的帮助下编写，并根据以下网址的解释发布到公共领域：
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
 * 一个可能的异步计算阶段，当另一个 CompletionStage 完成时执行一个动作或计算一个值。
 * 一个阶段在其计算终止时完成，但这可能会触发其他依赖阶段。此接口中定义的功能只有几种基本形式，这些形式扩展为一组方法，以捕捉一系列使用风格：<ul>
 *
 * <li>由阶段执行的计算可以表示为 Function、Consumer 或 Runnable（分别使用包含 <em>apply</em>、<em>accept</em> 或 <em>run</em> 的方法名称），具体取决于它是否需要参数和/或产生结果。
 * 例如，{@code stage.thenApply(x -> square(x)).thenAccept(x ->
 * System.out.print(x)).thenRun(() -> System.out.println())}。另一种形式（<em>compose</em>）应用于阶段本身，而不是它们的结果。</li>
 *
 * <li> 一个阶段的执行可能由一个阶段的完成触发，或两个阶段的完成，或两个阶段中的任何一个的完成触发。对单个阶段的依赖使用带有前缀 <em>then</em> 的方法进行安排。
 * 那些由两个阶段的完成触发的方法可以<em>结合</em>它们的结果或效果，使用相应命名的方法。那些由两个阶段中的任何一个的完成触发的方法不保证使用哪个结果或效果进行依赖阶段的计算。</li>
 *
 * <li> 阶段之间的依赖关系控制计算的触发，但不保证任何特定的顺序。此外，新阶段的计算执行可以以三种方式安排：默认执行、默认异步执行（使用带有后缀 <em>async</em> 的方法，这些方法使用阶段的默认异步执行设施），
 * 或自定义（通过提供的 {@link Executor}）。默认和异步模式的执行属性由 CompletionStage 实现指定，而不是此接口。带有显式 Executor 参数的方法可能具有任意的执行属性，甚至可能不支持并发执行，但它们的安排方式适合异步处理。</li>
 *
 * <li> 两种方法形式支持处理触发阶段是正常完成还是异常完成：方法 {@link
 * #whenComplete whenComplete} 允许无论结果如何都注入一个动作，同时在完成时保持结果。方法 {@link #handle handle} 还允许阶段计算一个替代结果，这可能使其他依赖阶段能够进一步处理。
 * 在所有其他情况下，如果一个阶段的计算因（未检查的）异常或错误而突然终止，那么所有需要其完成的依赖阶段也会异常完成，带有 {@link
 * CompletionException} 作为其原因。如果一个阶段依赖于两个阶段的<em>两者</em>，并且两者都异常完成，那么 CompletionException 可能对应于这两个异常中的任何一个。
 * 如果一个阶段依赖于两个阶段中的<em>任何一个</em>，并且其中只有一个异常完成，不保证依赖阶段是正常完成还是异常完成。在 {@code whenComplete} 方法的情况下，当提供的动作本身遇到异常时，
 * 如果阶段尚未异常完成，则该阶段将异常完成，异常为该动作遇到的异常。</li>
 *
 * </ul>
 *
 * <p>所有方法都遵循上述触发、执行和异常完成规范（这些规范在个别方法规范中不再重复）。此外，虽然用于传递完成结果（即，接受类型为 {@code T} 的参数的方法）的参数可以为 null，
 * 但传递任何其他参数的 null 值将导致抛出 {@link NullPointerException}。
 *
 * <p>此接口不定义用于最初创建、正常或异常强制完成、探测完成状态或结果或等待阶段完成的方法。CompletionStage 的实现可以根据需要提供实现此类效果的手段。
 * 方法 {@link #toCompletableFuture} 通过提供一个通用的转换类型，实现了此接口的不同实现之间的互操作性。
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
     * @param fn 用于计算返回的 CompletionStage 的值的函数
     * @param <U> 函数的返回类型
     * @return 新的 CompletionStage
     */
    public <U> CompletionStage<U> thenApply(Function<? super T,? extends U> fn);

    /**
     * 返回一个新的 CompletionStage，当此阶段正常完成时，使用此阶段的默认异步执行设施，以此阶段的结果作为提供的函数的参数执行。
     *
     * 有关异常完成的规则，请参阅 {@link CompletionStage} 文档。
     *
     * @param fn 用于计算返回的 CompletionStage 的值的函数
     * @param <U> 函数的返回类型
     * @return 新的 CompletionStage
     */
    public <U> CompletionStage<U> thenApplyAsync
        (Function<? super T,? extends U> fn);


    /**
     * 返回一个新的 CompletionStage，当此阶段正常完成时，
     * 使用提供的 Executor 执行，此阶段的结果作为提供的函数的参数。
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
     * 返回一个新的 CompletionStage，当此阶段正常完成时，
     * 使用此阶段的结果作为提供的操作的参数执行。
     *
     * 有关异常完成的规则，请参阅 {@link CompletionStage} 文档。
     *
     * @param action 在完成返回的 CompletionStage 之前执行的操作
     * @return 新的 CompletionStage
     */
    public CompletionStage<Void> thenAccept(Consumer<? super T> action);

    /**
     * 返回一个新的 CompletionStage，当此阶段正常完成时，
     * 使用此阶段的默认异步执行设施，此阶段的结果作为提供的操作的参数执行。
     *
     * 有关异常完成的规则，请参阅 {@link CompletionStage} 文档。
     *
     * @param action 在完成返回的 CompletionStage 之前执行的操作
     * @return 新的 CompletionStage
     */
    public CompletionStage<Void> thenAcceptAsync(Consumer<? super T> action);

    /**
     * 返回一个新的 CompletionStage，当此阶段正常完成时，
     * 使用提供的 Executor 执行，此阶段的结果作为提供的操作的参数。
     *
     * 有关异常完成的规则，请参阅 {@link CompletionStage} 文档。
     *
     * @param action 在完成返回的 CompletionStage 之前执行的操作
     * @param executor 用于异步执行的执行器
     * @return 新的 CompletionStage
     */
    public CompletionStage<Void> thenAcceptAsync(Consumer<? super T> action,
                                                 Executor executor);
    /**
     * 返回一个新的 CompletionStage，当此阶段正常完成时，执行给定的操作。
     *
     * 有关异常完成的规则，请参阅 {@link CompletionStage} 文档。
     *
     * @param action 在完成返回的 CompletionStage 之前执行的操作
     * @return 新的 CompletionStage
     */
    public CompletionStage<Void> thenRun(Runnable action);

    /**
     * 返回一个新的 CompletionStage，当此阶段正常完成时，
     * 使用此阶段的默认异步执行设施执行给定的操作。
     *
     * 有关异常完成的规则，请参阅 {@link CompletionStage} 文档。
     *
     * @param action 在完成返回的 CompletionStage 之前执行的操作
     * @return 新的 CompletionStage
     */
    public CompletionStage<Void> thenRunAsync(Runnable action);

    /**
     * 返回一个新的 CompletionStage，当此阶段正常完成时，
     * 使用提供的 Executor 执行给定的操作。
     *
     * 有关异常完成的规则，请参阅 {@link CompletionStage} 文档。
     *
     * @param action 在完成返回的 CompletionStage 之前执行的操作
     * @param executor 用于异步执行的执行器
     * @return 新的 CompletionStage
     */
    public CompletionStage<Void> thenRunAsync(Runnable action,
                                              Executor executor);

    /**
     * 返回一个新的 CompletionStage，当此阶段和其他给定阶段都正常完成时，
     * 使用两个结果作为提供的函数的参数执行。
     *
     * 有关异常完成的规则，请参阅 {@link CompletionStage} 文档。
     *
     * @param other 另一个 CompletionStage
     * @param fn 用于计算返回的 CompletionStage 值的函数
     * @param <U> 另一个 CompletionStage 的结果类型
     * @param <V> 函数的返回类型
     * @return 新的 CompletionStage
     */
    public <U,V> CompletionStage<V> thenCombine
        (CompletionStage<? extends U> other,
         BiFunction<? super T,? super U,? extends V> fn);

    /**
     * 返回一个新的 CompletionStage，当此阶段和其他给定阶段都正常完成时，
     * 使用此阶段的默认异步执行设施，两个结果作为提供的函数的参数执行。
     *
     * 有关异常完成的规则，请参阅 {@link CompletionStage} 文档。
     *
     * @param other 另一个 CompletionStage
     * @param fn 用于计算返回的 CompletionStage 值的函数
     * @param <U> 另一个 CompletionStage 的结果类型
     * @param <V> 函数的返回类型
     * @return 新的 CompletionStage
     */
    public <U,V> CompletionStage<V> thenCombineAsync
        (CompletionStage<? extends U> other,
         BiFunction<? super T,? super U,? extends V> fn);

    /**
     * 返回一个新的 CompletionStage，当此阶段和其他给定阶段都正常完成时，
     * 使用提供的执行器，两个结果作为提供的函数的参数执行。
     *
     * 有关异常完成的规则，请参阅 {@link CompletionStage} 文档。
     *
     * @param other 另一个 CompletionStage
     * @param fn 用于计算返回的 CompletionStage 值的函数
     * @param executor 用于异步执行的执行器
     * @param <U> 另一个 CompletionStage 的结果类型
     * @param <V> 函数的返回类型
     * @return 新的 CompletionStage
     */
    public <U,V> CompletionStage<V> thenCombineAsync
        (CompletionStage<? extends U> other,
         BiFunction<? super T,? super U,? extends V> fn,
         Executor executor);


    /**
     * 返回一个新的 CompletionStage，当此阶段和给定的其他阶段都正常完成时，使用两个结果作为提供的操作的参数执行。
     *
     * 有关异常完成的规则，请参阅 {@link CompletionStage} 文档。
     *
     * @param other 另一个 CompletionStage
     * @param action 在完成返回的 CompletionStage 之前执行的操作
     * @param <U> 另一个 CompletionStage 的结果类型
     * @return 新的 CompletionStage
     */
    public <U> CompletionStage<Void> thenAcceptBoth
        (CompletionStage<? extends U> other,
         BiConsumer<? super T, ? super U> action);

    /**
     * 返回一个新的 CompletionStage，当此阶段和给定的其他阶段都正常完成时，使用此阶段的默认异步执行设施，使用两个结果作为提供的操作的参数执行。
     *
     * @param other 另一个 CompletionStage
     * @param action 在完成返回的 CompletionStage 之前执行的操作
     * @param <U> 另一个 CompletionStage 的结果类型
     * @return 新的 CompletionStage
     */
    public <U> CompletionStage<Void> thenAcceptBothAsync
        (CompletionStage<? extends U> other,
         BiConsumer<? super T, ? super U> action);

    /**
     * 返回一个新的 CompletionStage，当此阶段和给定的其他阶段都正常完成时，使用提供的执行器，使用两个结果作为提供的函数的参数执行。
     *
     * @param other 另一个 CompletionStage
     * @param action 在完成返回的 CompletionStage 之前执行的操作
     * @param executor 用于异步执行的执行器
     * @param <U> 另一个 CompletionStage 的结果类型
     * @return 新的 CompletionStage
     */
    public <U> CompletionStage<Void> thenAcceptBothAsync
        (CompletionStage<? extends U> other,
         BiConsumer<? super T, ? super U> action,
         Executor executor);

    /**
     * 返回一个新的 CompletionStage，当此阶段和给定的其他阶段都正常完成时，执行给定的操作。
     *
     * 有关异常完成的规则，请参阅 {@link CompletionStage} 文档。
     *
     * @param other 另一个 CompletionStage
     * @param action 在完成返回的 CompletionStage 之前执行的操作
     * @return 新的 CompletionStage
     */
    public CompletionStage<Void> runAfterBoth(CompletionStage<?> other,
                                              Runnable action);
    /**
     * 返回一个新的 CompletionStage，当此阶段和给定的其他阶段都正常完成时，使用此阶段的默认异步执行设施执行给定的操作。
     *
     * 有关异常完成的规则，请参阅 {@link CompletionStage} 文档。
     *
     * @param other 另一个 CompletionStage
     * @param action 在完成返回的 CompletionStage 之前执行的操作
     * @return 新的 CompletionStage
     */
    public CompletionStage<Void> runAfterBothAsync(CompletionStage<?> other,
                                                   Runnable action);

    /**
     * 返回一个新的 CompletionStage，当此阶段和给定的其他阶段都正常完成时，使用提供的执行器执行给定的操作。
     *
     * 有关异常完成的规则，请参阅 {@link CompletionStage} 文档。
     *
     * @param other 另一个 CompletionStage
     * @param action 在完成返回的 CompletionStage 之前执行的操作
     * @param executor 用于异步执行的执行器
     * @return 新的 CompletionStage
     */
    public CompletionStage<Void> runAfterBothAsync(CompletionStage<?> other,
                                                   Runnable action,
                                                   Executor executor);
    /**
     * 返回一个新的 CompletionStage，当此阶段或给定的其他阶段之一正常完成时，使用相应的结果作为提供的函数的参数执行。
     *
     * 有关异常完成的规则，请参阅 {@link CompletionStage} 文档。
     *
     * @param other 另一个 CompletionStage
     * @param fn 用于计算返回的 CompletionStage 值的函数
     * @param <U> 函数的返回类型
     * @return 新的 CompletionStage
     */
    public <U> CompletionStage<U> applyToEither
        (CompletionStage<? extends T> other,
         Function<? super T, U> fn);

    /**
     * 返回一个新的 CompletionStage，当此阶段或给定的其他阶段之一正常完成时，使用此阶段的默认异步执行设施，使用相应的结果作为提供的函数的参数执行。
     *
     * 有关异常完成的规则，请参阅 {@link CompletionStage} 文档。
     *
     * @param other 另一个 CompletionStage
     * @param fn 用于计算返回的 CompletionStage 值的函数
     * @param <U> 函数的返回类型
     * @return 新的 CompletionStage
     */
    public <U> CompletionStage<U> applyToEitherAsync
        (CompletionStage<? extends T> other,
         Function<? super T, U> fn);

    /**
     * 返回一个新的 CompletionStage，当此阶段或给定的其他阶段之一正常完成时，使用提供的执行器，使用相应的结果作为提供的函数的参数执行。
     *
     * 有关异常完成的规则，请参阅 {@link CompletionStage} 文档。
     *
     * @param other 另一个 CompletionStage
     * @param fn 用于计算返回的 CompletionStage 值的函数
     * @param executor 用于异步执行的执行器
     * @param <U> 函数的返回类型
     * @return 新的 CompletionStage
     */
    public <U> CompletionStage<U> applyToEitherAsync
        (CompletionStage<? extends T> other,
         Function<? super T, U> fn,
         Executor executor);


    /**
     * 返回一个新的 CompletionStage，当此阶段或给定的其他阶段正常完成时，使用相应的结果作为参数执行提供的操作。
     *
     * 有关异常完成的规则，请参阅 {@link CompletionStage} 文档。
     *
     * @param other 另一个 CompletionStage
     * @param action 在完成返回的 CompletionStage 之前要执行的操作
     * @return 新的 CompletionStage
     */
    public CompletionStage<Void> acceptEither
        (CompletionStage<? extends T> other,
         Consumer<? super T> action);

    /**
     * 返回一个新的 CompletionStage，当此阶段或给定的其他阶段正常完成时，使用此阶段的默认异步执行设施，使用相应的结果作为参数执行提供的操作。
     *
     * 有关异常完成的规则，请参阅 {@link CompletionStage} 文档。
     *
     * @param other 另一个 CompletionStage
     * @param action 在完成返回的 CompletionStage 之前要执行的操作
     * @return 新的 CompletionStage
     */
    public CompletionStage<Void> acceptEitherAsync
        (CompletionStage<? extends T> other,
         Consumer<? super T> action);

    /**
     * 返回一个新的 CompletionStage，当此阶段或给定的其他阶段正常完成时，使用提供的执行器，使用相应的结果作为参数执行提供的函数。
     *
     * 有关异常完成的规则，请参阅 {@link CompletionStage} 文档。
     *
     * @param other 另一个 CompletionStage
     * @param action 在完成返回的 CompletionStage 之前要执行的操作
     * @param executor 用于异步执行的执行器
     * @return 新的 CompletionStage
     */
    public CompletionStage<Void> acceptEitherAsync
        (CompletionStage<? extends T> other,
         Consumer<? super T> action,
         Executor executor);

    /**
     * 返回一个新的 CompletionStage，当此阶段或给定的其他阶段正常完成时，执行提供的操作。
     *
     * 有关异常完成的规则，请参阅 {@link CompletionStage} 文档。
     *
     * @param other 另一个 CompletionStage
     * @param action 在完成返回的 CompletionStage 之前要执行的操作
     * @return 新的 CompletionStage
     */
    public CompletionStage<Void> runAfterEither(CompletionStage<?> other,
                                                Runnable action);

    /**
     * 返回一个新的 CompletionStage，当此阶段或给定的其他阶段正常完成时，使用此阶段的默认异步执行设施执行提供的操作。
     *
     * 有关异常完成的规则，请参阅 {@link CompletionStage} 文档。
     *
     * @param other 另一个 CompletionStage
     * @param action 在完成返回的 CompletionStage 之前要执行的操作
     * @return 新的 CompletionStage
     */
    public CompletionStage<Void> runAfterEitherAsync
        (CompletionStage<?> other,
         Runnable action);

    /**
     * 返回一个新的 CompletionStage，当此阶段或给定的其他阶段正常完成时，使用提供的执行器执行提供的操作。
     *
     * 有关异常完成的规则，请参阅 {@link CompletionStage} 文档。
     *
     * @param other 另一个 CompletionStage
     * @param action 在完成返回的 CompletionStage 之前要执行的操作
     * @param executor 用于异步执行的执行器
     * @return 新的 CompletionStage
     */
    public CompletionStage<Void> runAfterEitherAsync
        (CompletionStage<?> other,
         Runnable action,
         Executor executor);

    /**
     * 返回一个新的 CompletionStage，当此阶段正常完成时，使用此阶段的结果作为参数执行提供的函数。
     *
     * 有关异常完成的规则，请参阅 {@link CompletionStage} 文档。
     *
     * @param fn 返回新 CompletionStage 的函数
     * @param <U> 返回的 CompletionStage 的结果类型
     * @return CompletionStage
     */
    public <U> CompletionStage<U> thenCompose
        (Function<? super T, ? extends CompletionStage<U>> fn);

    /**
     * 返回一个新的 CompletionStage，当此阶段正常完成时，使用此阶段的默认异步执行设施，使用此阶段的结果作为参数执行提供的函数。
     *
     * 有关异常完成的规则，请参阅 {@link CompletionStage} 文档。
     *
     * @param fn 返回新 CompletionStage 的函数
     * @param <U> 返回的 CompletionStage 的结果类型
     * @return CompletionStage
     */
    public <U> CompletionStage<U> thenComposeAsync
        (Function<? super T, ? extends CompletionStage<U>> fn);

    /**
     * 返回一个新的 CompletionStage，当此阶段正常完成时，使用提供的执行器，使用此阶段的结果作为参数执行提供的函数。
     *
     * 有关异常完成的规则，请参阅 {@link CompletionStage} 文档。
     *
     * @param fn 返回新 CompletionStage 的函数
     * @param executor 用于异步执行的执行器
     * @param <U> 返回的 CompletionStage 的结果类型
     * @return CompletionStage
     */
    public <U> CompletionStage<U> thenComposeAsync
        (Function<? super T, ? extends CompletionStage<U>> fn,
         Executor executor);

    /**
     * 返回一个新的 CompletionStage，当此阶段异常完成时，使用此阶段的异常作为参数执行提供的函数。否则，如果此阶段正常完成，则返回的阶段也正常完成，具有相同的结果值。
     *
     * @param fn 如果此 CompletionStage 异常完成，则用于计算返回的 CompletionStage 值的函数
     * @return 新的 CompletionStage
     */
    public CompletionStage<T> exceptionally
        (Function<Throwable, ? extends T> fn);


    /**
     * 返回一个新的 CompletionStage，其结果或异常与本阶段相同，并在本阶段完成时执行给定的操作。
     *
     * <p>当本阶段完成时，给定的操作将使用本阶段的结果（如果无结果则为 {@code null}）和异常（如果无异常则为 {@code null}）作为参数调用。返回的阶段在操作返回时完成。如果提供的操作本身遇到异常，则返回的阶段将以该异常异常完成，除非本阶段也异常完成。
     *
     * @param action 要执行的操作
     * @return 新的 CompletionStage
     */
    public CompletionStage<T> whenComplete
        (BiConsumer<? super T, ? super Throwable> action);

    /**
     * 返回一个新的 CompletionStage，其结果或异常与本阶段相同，并在本阶段完成时使用本阶段的默认异步执行设施执行给定的操作。
     *
     * <p>当本阶段完成时，给定的操作将使用本阶段的结果（如果无结果则为 {@code null}）和异常（如果无异常则为 {@code null}）作为参数调用。返回的阶段在操作返回时完成。如果提供的操作本身遇到异常，则返回的阶段将以该异常异常完成，除非本阶段也异常完成。
     *
     * @param action 要执行的操作
     * @return 新的 CompletionStage
     */
    public CompletionStage<T> whenCompleteAsync
        (BiConsumer<? super T, ? super Throwable> action);

    /**
     * 返回一个新的 CompletionStage，其结果或异常与本阶段相同，并在本阶段完成时使用提供的执行器执行给定的操作。
     *
     * <p>当本阶段完成时，给定的操作将使用本阶段的结果（如果无结果则为 {@code null}）和异常（如果无异常则为 {@code null}）作为参数调用。返回的阶段在操作返回时完成。如果提供的操作本身遇到异常，则返回的阶段将以该异常异常完成，除非本阶段也异常完成。
     *
     * @param action 要执行的操作
     * @param executor 用于异步执行的执行器
     * @return 新的 CompletionStage
     */
    public CompletionStage<T> whenCompleteAsync
        (BiConsumer<? super T, ? super Throwable> action,
         Executor executor);

    /**
     * 返回一个新的 CompletionStage，当本阶段正常或异常完成时，使用本阶段的结果和异常作为参数执行提供的函数。
     *
     * <p>当本阶段完成时，给定的函数将使用本阶段的结果（如果无结果则为 {@code null}）和异常（如果无异常则为 {@code null}）作为参数调用，函数的结果用于完成返回的阶段。
     *
     * @param fn 用于计算返回的 CompletionStage 值的函数
     * @param <U> 函数的返回类型
     * @return 新的 CompletionStage
     */
    public <U> CompletionStage<U> handle
        (BiFunction<? super T, Throwable, ? extends U> fn);

    /**
     * 返回一个新的 CompletionStage，当本阶段正常或异常完成时，使用本阶段的默认异步执行设施，使用本阶段的结果和异常作为参数执行提供的函数。
     *
     * <p>当本阶段完成时，给定的函数将使用本阶段的结果（如果无结果则为 {@code null}）和异常（如果无异常则为 {@code null}）作为参数调用，函数的结果用于完成返回的阶段。
     *
     * @param fn 用于计算返回的 CompletionStage 值的函数
     * @param <U> 函数的返回类型
     * @return 新的 CompletionStage
     */
    public <U> CompletionStage<U> handleAsync
        (BiFunction<? super T, Throwable, ? extends U> fn);

    /**
     * 返回一个新的 CompletionStage，当本阶段正常或异常完成时，使用提供的执行器，使用本阶段的结果和异常作为参数执行提供的函数。
     *
     * <p>当本阶段完成时，给定的函数将使用本阶段的结果（如果无结果则为 {@code null}）和异常（如果无异常则为 {@code null}）作为参数调用，函数的结果用于完成返回的阶段。
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
     * 返回一个 {@link CompletableFuture}，保持与本阶段相同的完成属性。如果本阶段已经是一个 CompletableFuture，此方法可能直接返回本阶段本身。否则，调用此方法的效果可能等同于 {@code thenApply(x -> x)}，但返回一个类型为 {@code CompletableFuture} 的实例。如果 CompletionStage 实现选择不与其他实现互操作，可能会抛出 {@code UnsupportedOperationException}。
     *
     * @return CompletableFuture
     * @throws UnsupportedOperationException 如果此实现不与 CompletableFuture 互操作
     */
    public CompletableFuture<T> toCompletableFuture();
}
